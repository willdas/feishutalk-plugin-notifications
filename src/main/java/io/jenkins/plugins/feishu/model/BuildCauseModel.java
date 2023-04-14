package io.jenkins.plugins.feishu.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class BuildCauseModel {

    private Data data;
    private String shortDescription;
    private String userName;

    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @lombok.Data
    public static class Data {
        private String actionType;
        private String branch;
        private String userEmail;
    }
}
