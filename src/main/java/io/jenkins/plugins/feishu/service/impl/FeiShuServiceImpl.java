package io.jenkins.plugins.feishu.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.TypeReference;
import hudson.model.AbstractBuild;
import hudson.model.TaskListener;
import io.jenkins.plugins.feishu.contants.NotifyConstants;
import io.jenkins.plugins.feishu.enums.ColorEnum;
import io.jenkins.plugins.feishu.model.BuildCauseModel;
import io.jenkins.plugins.feishu.model.BuildInfo;
import io.jenkins.plugins.feishu.model.FeiShuCardMessageModel;
import io.jenkins.plugins.feishu.model.NotifyModel;
import io.jenkins.plugins.feishu.service.FeiShuService;
import io.jenkins.plugins.feishu.util.HttpUtil;
import io.jenkins.plugins.feishu.util.LoggerUtil;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.*;

import static io.jenkins.plugins.feishu.contants.NotifyConstants.CONSOLE;
import static io.jenkins.plugins.feishu.model.FeiShuCardMessageModel.TagEnum.*;
import static io.jenkins.plugins.feishu.util.LoggerUtil.*;

/**
 * @className: FeiShuServiceImpl
 * @package: io.jenkins.plugins.feishu.service.impl
 * @author:（willdas）
 * @date: 2021/11/26 17:57
 **/
public class FeiShuServiceImpl implements FeiShuService {

    private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    private final String jenkinsURL;

    private final String url;

    private final Boolean onStart;

    private final Boolean onSuccess;

    private final Boolean onFailed;

    private TaskListener listener;

    private AbstractBuild build;

    public FeiShuServiceImpl(String jenkinsURL, String url, boolean onStart, boolean onSuccess, boolean onFailed, TaskListener listener, AbstractBuild build) {
        this.jenkinsURL = jenkinsURL;
        this.onStart = onStart;
        this.onSuccess = onSuccess;
        this.onFailed = onFailed;
        this.listener = listener;
        this.build = build;
        this.url = url;
    }

    @Override
    public void start() {
        line(listener, LoggerUtil.LineType.START);
        info(listener, "Causes:" + JSONArray.toJSONString(build.getCauses()));
        final BuildInfo buildInfo = this.getBuildInfo();
        String content = String.format("%s%s%s%s%s%s%s",
                NotifyConstants.BUILD_INFO,
                NotifyConstants.BRANCH_NAME, buildInfo.getBranch(),
                NotifyConstants.ACTION_TYPE, buildInfo.getActionType(),
                NotifyConstants.BUILDER, buildInfo.getUserName());
        if (onStart) {
            this.sendMessage(NotifyModel.builder().title(String.format("%s %s", build.getProject().getDisplayName(), NotifyConstants.START_BUILD)).titleColor(ColorEnum.BLUE.getName()).content("").content(content).startTime(build.getTime()).link(build.getUrl()).build());
        }
    }

    @Override
    public void success() {
        final BuildInfo buildInfo = this.getBuildInfo();
        final String[] displayName = build.getDisplayName().split(" ");
        String content = String.format("%s%s%s%s%s%s%s",
                NotifyConstants.BUILD_INFO,
                NotifyConstants.DOCKER_IMAGE_NAME, displayName[displayName.length - 1],
                NotifyConstants.DURATION, build.getDurationString(),
                NotifyConstants.BUILDER, buildInfo.getUserName());
        if (onSuccess) {
            this.sendMessage(NotifyModel.builder().title(String.format("%s %s", build.getProject().getDisplayName(), NotifyConstants.BUILD_SUCCESS)).titleColor(ColorEnum.GREEN.getName()).content(content).startTime(build.getTime()).link(build.getUrl()).build());
        }
        line(listener, LineType.END);
    }

    @Override
    public void failed() {
        String content = String.format("%s%s%s%s%s", NotifyConstants.BUILD_INFO, NotifyConstants.BUILD_LOGGER, build.getBuildStatusSummary().message, NotifyConstants.DURATION, build.getDurationString());
        if (onFailed) {
            this.sendMessage(NotifyModel.builder().title(String.format("%s %s", build.getProject().getDisplayName(), NotifyConstants.BUILD_FAIL)).titleColor(ColorEnum.RED.getName()).content(content).startTime(build.getTime()).link(build.getUrl()).build());
        }
        line(listener, LineType.END);
    }

    private void sendMessage(NotifyModel notifyModel) {
        List<FeiShuCardMessageModel.Elements> elementList = new ArrayList<>();
        final FeiShuCardMessageModel.Text startTime = FeiShuCardMessageModel.Text.builder()
                .tag(PLAIN_TEXT.getName())
                .content(NotifyConstants.START_TIME + formatter.format(notifyModel.getStartTime()))
                .build();
        final FeiShuCardMessageModel.Text content = FeiShuCardMessageModel.Text.builder()
                .tag(PLAIN_TEXT.getName())
                .content(notifyModel.getContent())
                .build();
        final FeiShuCardMessageModel.Elements contentElement = FeiShuCardMessageModel.Elements.builder().tag(DIV.getName()).text(content).build();
        if (Objects.nonNull(jenkinsURL)) {
            final FeiShuCardMessageModel.Extra extra = FeiShuCardMessageModel.Extra.builder()
                    .tag(BUTTON.getName())
                    .text(FeiShuCardMessageModel.Text.builder().tag(LARK_MD.getName()).content(NotifyConstants.DETAIL).build())
                    .type(FeiShuCardMessageModel.ExtraTypeEnum.PRIMARY.getName())
                    .url(jenkinsURL.concat(notifyModel.getLink()).concat(CONSOLE)).build();
            contentElement.setExtra(extra);
        }
        elementList.add(FeiShuCardMessageModel.Elements.builder().tag(DIV.getName()).text(startTime).build());
        elementList.add(contentElement);
        elementList.add(FeiShuCardMessageModel.Elements.builder()
                .tag(DIV.getName())
                .text(FeiShuCardMessageModel.Text.builder()
                        .tag(LARK_MD.getName())
                        .content(NotifyConstants.AT_ALL_USER)
                        .build()).build());
        try {
            final String json = JSON.toJSONString(FeiShuCardMessageModel.buildDTO(notifyModel.getTitle(), notifyModel.getTitleColor(), elementList));
            HttpUtil.postForJSON(url, "", null, json);
        } catch (Exception e) {
            error(listener, "sendMessage->异常:" + e);
        }
    }

    private BuildInfo getBuildInfo() {
         /*
         //主动触发
        final Cause.UserIdCause userIdCause = (Cause.UserIdCause) build.getCause(Cause.UserIdCause.class);
        String content = String.format("%s%s%s%s%s%s%s", NotifyConstants.BUILD_INFO, NotifyConstants.BUILD_LOGGER, build.getBuildStatusSummary().message, NotifyConstants.DURATION, build.getDurationString(), NotifyConstants.BUILD_AUTHOR, userIdCause.getUserName());
        */

        final List<BuildCauseModel> causeModelList = JSON.parseObject(JSONArray.toJSONString(this.build.getCauses()), new TypeReference<List<BuildCauseModel>>() {
        });
        String userName = causeModelList.get(0).getUserName();
        if (StringUtils.isEmpty(userName)) {
            userName = causeModelList.get(0).getData().getUserEmail().split("@")[0];
        }

        final BuildCauseModel.Data data = Optional.ofNullable(causeModelList.get(0).getData()).orElse(BuildCauseModel.Data.builder().build());
        return BuildInfo.builder().branch(data.getBranch()).actionType(data.getActionType()).userName(userName).build();
    }

    /*
    final StringBuilder stringBuilder = new StringBuilder();
        List<ChangeLogSet<? extends ChangeLogSet.Entry>> changeLogSets = build.getChangeSets();
        for (ChangeLogSet<? extends ChangeLogSet.Entry> changeLogSet : changeLogSets) {
            processChangeLogSet(stringBuilder, changeLogSet);
        }
    info(listener, "stringBuilder:" + stringBuilder.toString());
    private void processChangeLogSet(StringBuilder sb, ChangeLogSet cs) {
        for (Object o : cs) {
            Entry e = (Entry) o;
            sb.append(String.format("%s%s", NotifyConstants.COMMITER, e.getAuthor()));
            sb.append(String.format("%s%s", NotifyConstants.COMMIT_ID, e.getCommitId()));
            sb.append(String.format("%s%s", NotifyConstants.COMMIT_MSG, e.getMsg()));
            sb.append(String.format("%s%s", NotifyConstants.COMMIT_TIME, formatter.format(new Date(e.getTimestamp()))));
            try {
                for (ChangeLogSet.AffectedFile file : e.getAffectedFiles()) {
                    sb.append(file.getEditType().getName() + file.getPath());
                }
            } catch (UnsupportedOperationException ex) {
                for (String file : e.getAffectedPaths()) {
                    sb.append(file);
                }
            }
        }
    }*/
}
