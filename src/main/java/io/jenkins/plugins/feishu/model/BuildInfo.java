package io.jenkins.plugins.feishu.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @className: BuildInfo
 * @package: io.jenkins.plugins.feishu.model
 * @describe: 构建信息
 * @author: willdas
 * @date: 2023/04/14 18:08
 **/
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class BuildInfo {

    private String actionType;
    private String branch;
    private String userName;
}
