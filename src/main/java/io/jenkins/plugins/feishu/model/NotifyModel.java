package io.jenkins.plugins.feishu.model;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

/**
 * @className: NotifyModel
 * @package: io.jenkins.plugins.feishu.model
 * @describe: 消息内容主体
 * @author: willdas
 * @date: 2023/04/14 18:09
 **/
@Builder
@Data
public class NotifyModel {

    private String title;

    private String titleColor;

    private String content;

    private String link;

    private Date startTime;
}
