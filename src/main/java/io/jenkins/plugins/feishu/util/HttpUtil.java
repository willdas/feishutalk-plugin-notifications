package io.jenkins.plugins.feishu.util;


import org.apache.http.*;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import java.io.*;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;


/**
 * @className: HttpUtil
 * @package: com.dt.common.util.http
 * @describe: Http请求工具类
 * @author:willdas
 * @date: 2021/10/14 9:38 上午
 **/
public class HttpUtil {

    private static CloseableHttpClient httpClient = null;

    static {
        if (httpClient == null) {
            synchronized (CloseableHttpClient.class) {
                if (httpClient == null) {
                    httpClient = buildHttpClient();
                }
            }
        }
    }

    /**
     * 发送post表单请求
     *
     * @param hostName
     * @param urlPath
     * @param headerMap
     * @param bodyMap
     * @return
     */
    public static String post(String hostName, String urlPath, Map<String, String> headerMap, Map<String, Object> bodyMap) throws IOException {
        HttpPost httpPost = new HttpPost(hostName + urlPath);
        config(httpPost);
        setPostParams(httpPost, headerMap, bodyMap);
        try (CloseableHttpResponse httpResponse = httpClient.execute(httpPost)) {
            return getMsg(httpResponse);
        }
    }

    /**
     * 发送postJSON请求
     *
     * @param hostName
     * @param urlPath
     * @param headerMap
     * @param jsonString
     * @return
     */
    public static String postForJSON(String hostName, String urlPath, Map<String, String> headerMap, String jsonString) throws IOException {
        HttpPost httpPost = new HttpPost(hostName + urlPath);
        config(httpPost);
        addHeads(httpPost, headerMap);
        StringEntity entity = new StringEntity(jsonString, StandardCharsets.UTF_8.name());
        entity.setContentEncoding(StandardCharsets.UTF_8.name());
        entity.setContentType("application/json");
        httpPost.setEntity(entity);
        try (CloseableHttpResponse httpResponse = httpClient.execute(httpPost);) {
            return getMsg(httpResponse);
        }
    }

    /**
     * @methodName: get
     * @param: [urlString]
     * @desc: 发送get请求
     * @auther:（willdas）
     * @date: 2019/10/08 15:15
     **/
    public static String get(String hostName, String urlPath, Map<String, Object> queryMap, Map<String, String> headerMap) throws IOException {
        HttpGet httpGet = new HttpGet(buildGetUrl(hostName, urlPath, queryMap));
        config(httpGet);
        addHeads(httpGet, headerMap);
        try (CloseableHttpResponse httpResponse = httpClient.execute(httpGet)) {
            return getMsg(httpResponse);
        }
    }

    /**
     * @methodName: get InputStream
     * @param: [urlString]
     * @desc: 发送get请求
     * @auther:（willdas）
     * @date: 2019/10/08 15:15
     **/
    public static InputStream getInputStream(String hostName, String urlPath) throws IOException {
        HttpGet httpGet = new HttpGet(buildGetUrl(hostName, urlPath, null));
        config(httpGet);
        try (CloseableHttpResponse httpResponse = httpClient.execute(httpGet);) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream(4096);
            httpResponse.getEntity().writeTo(outputStream);
            EntityUtils.consume(httpResponse.getEntity());
            return new ByteArrayInputStream(outputStream.toByteArray());
        }
    }

    /**
     * @methodName: createHttpClient
     * @param: [maxTotal, maxPerRoute, maxRoute, hostname, port]
     * @desc: 创建HttpClient
     * @auther:（willdas）
     * @date: 2019/10/08 17:17
     **/
    private static CloseableHttpClient buildHttpClient() {
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(200);
        cm.setDefaultMaxPerRoute(100);

        // 请求重试处理
        HttpRequestRetryHandler httpRequestRetryHandler = new HttpRequestRetryHandler() {
            @Override
            public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
                if (executionCount > 3) {// 如果已经重试了3次，就放弃
                    return false;
                }
                if (exception instanceof InterruptedIOException) {// 超时
                    return true;
                }
                if (exception instanceof NoHttpResponseException) {// 如果服务器丢掉了连接，那么就重试
                    return true;
                }
                if (exception instanceof SSLHandshakeException) {// 不要重试SSL握手异常
                    return true;
                }
                if (exception instanceof SSLException) { // SSL握手异常
                    return true;
                }
                if (exception instanceof UnknownHostException) {// 目标服务器不可达
                    return false;
                }
                if (exception instanceof ConnectTimeoutException) {// 连接被拒绝
                    return false;
                }

                HttpClientContext clientContext = HttpClientContext.adapt(context);
                HttpRequest request = clientContext.getRequest();
                // 如果请求是幂等的，就再次尝试
                if (!(request instanceof HttpEntityEnclosingRequest)) {
                    return true;
                }
                return false;
            }
        };

        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(cm)
                .setRetryHandler(httpRequestRetryHandler).build();
        return httpClient;
    }

    /**
     * @methodName: setPostParams
     * @param: [httpPost, headerMap, bodyMap]
     * @desc: 设置POST请求参数 body
     * @auther:（willdas）
     * @date: 2019/10/08 15:06
     **/
    private static void setPostParams(HttpPost httpPost, Map<String, String> headerMap, Map<String, Object> bodyMap) throws UnsupportedEncodingException {
        addHeads(httpPost, headerMap);
        if (bodyMap != null) {
            List<NameValuePair> nvps = new ArrayList<NameValuePair>();
            for (Map.Entry<String, Object> entry : bodyMap.entrySet()) {
                nvps.add(new BasicNameValuePair(entry.getKey(), entry.getValue().toString()));
            }
            httpPost.setEntity(new UrlEncodedFormEntity(nvps, StandardCharsets.UTF_8));
        }
    }

    /**
     * @methodName: setGetParams
     * @param: [httpPost, headerMap, queryMap]
     * @desc: 设置GET请求参数
     * @auther:（willdas）
     * @date: 2019/10/15 10:18
     **/
    private static String buildGetUrl(String hostName, String urlPath, Map<String, Object> queryMap) throws IOException {
        StringBuffer sbf = new StringBuffer();
        sbf.append(hostName);
        if (Objects.nonNull(urlPath) && urlPath.length() > 0) {
            sbf.append(urlPath);
        }
        if (queryMap != null) {
            sbf.append("?");
            // 装填参数
            List<NameValuePair> naps = new ArrayList<NameValuePair>();
            for (Map.Entry<String, Object> entry : queryMap.entrySet()) {
                naps.add(new BasicNameValuePair(entry.getKey(), entry.getValue().toString()));
            }
            sbf.append(EntityUtils.toString(new UrlEncodedFormEntity(naps, Consts.UTF_8)));
        }
        return sbf.toString();
    }

    /**
     * @methodName: setHeads
     * @param: [httpPost, headerMap]
     * @desc: 设置Head头参数
     * @auther:（willdas）
     * @date: 2019/10/15 10:17
     **/
    private static void addHeads(HttpRequestBase httpRequestBase, Map<String, String> headerMap) {
        if (headerMap != null) {
            for (Map.Entry<String, String> entry : headerMap.entrySet()) {
                httpRequestBase.addHeader(entry.getKey(), entry.getValue());
            }
        }
    }


    /**
     * @methodName: config
     * @param: [httpPost]
     * @desc: 设置超时时间
     * @auther:（willdas）
     * @date: 2019/10/08 15:05
     **/
    private static void config(HttpRequestBase httpRequestBase) {
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(3000)
                .setConnectionRequestTimeout(3000)
                .setSocketTimeout(3000)
                .build();
        httpRequestBase.setConfig(requestConfig);
    }

    /**
     * @methodName: getMsg
     * @param: [httpResponse]
     * @desc: 返回数据信息
     * @auther:（willdas）
     * @date: 2019/10/08 15:12
     **/
    private static String getMsg(CloseableHttpResponse httpResponse) throws IOException {
        HttpEntity resEntity = httpResponse.getEntity();
        String result = EntityUtils.toString(resEntity, "utf-8");
        EntityUtils.consume(resEntity);
        return result;
    }
}
