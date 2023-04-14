package io.jenkins.plugins.feishu;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import io.jenkins.plugins.feishu.service.FeiShuService;
import io.jenkins.plugins.feishu.service.impl.FeiShuServiceImpl;
import lombok.Data;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;

/**
 * Created by Marvin on 16/8/25.
 */
@Data
public class FeiShuNotifier extends Notifier {

    private String url;

    private boolean onStart;

    private boolean onSuccess;

    private boolean onFailed;

    public String getJenkinsURL() {
        return jenkinsURL;
    }

    private String jenkinsURL;


    @DataBoundConstructor
    public FeiShuNotifier(String url, boolean onStart, boolean onSuccess, boolean onFailed, String jenkinsURL) {
        super();
        this.url = url;
        this.onStart = onStart;
        this.onSuccess = onSuccess;
        this.onFailed = onFailed;
        this.jenkinsURL = jenkinsURL;
    }

    public FeiShuService newService(AbstractBuild build, TaskListener listener) {
        return new FeiShuServiceImpl(jenkinsURL, url, onStart, onSuccess, onFailed, listener, build);
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        return true;
    }


    @Override
    public DingdingNotifierDescriptor getDescriptor() {
        return (DingdingNotifierDescriptor) super.getDescriptor();
    }

    @Extension
    public static class DingdingNotifierDescriptor extends BuildStepDescriptor<Publisher> {


        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "飞书通知器配置";
        }

    }
}
