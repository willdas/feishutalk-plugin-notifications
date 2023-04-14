package io.jenkins.plugins.feishu.model;

import io.jenkins.plugins.feishu.contants.NotifyConstants;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @className: FeiShuRichMessageDTO
 * @package: com.dt.message.dto
 * @describe: 富文本消息
 * @author:   willdas
 * @date: 2021/11/02 2:27 下午
 **/
@Builder
@Data
public class FeiShuRichMessageModel {

    private static final String MSG_TYPE = "post";
    /**
     * 消息的类型，此处固定填 “post”
     */
    private String msg_type;
    /**
     * 消息的内容
     */
    private Content content;

    @Builder
    @Data
    public static class Content {
        /**
         * 富文本消息体内容
         */
        private Post post;
    }

    @Builder
    @Data
    public static class Post {
        /**
         * 中文消息体
         */
        private ZhCn zh_cn;
    }

    @Builder
    @Data
    public static class ZhCn {
        /**
         * 消息的标题
         */
        private String title;
        /**
         * 消息的内容
         */
        private List<List<ContentTag>> content;
    }

    @Builder
    @Data
    public static class ContentTag {
        /**
         * 文本标识
         */
        private String tag;
        /**
         * 消息文本
         */
        private String text;
        /**
         * at的用户ID
         */
        private String user_id;
    }

    public static FeiShuRichMessageModel buildDTO(List<List<ContentTag>> contentList) {
        final ZhCn zhCn = ZhCn.builder()
                .title(NotifyConstants.NOTIFY_TITLE)
                .content(contentList)
                .build();
        return FeiShuRichMessageModel.builder().msg_type(MSG_TYPE).content(Content.builder().post(Post.builder().zh_cn(zhCn).build()).build()).build();
    }
}
