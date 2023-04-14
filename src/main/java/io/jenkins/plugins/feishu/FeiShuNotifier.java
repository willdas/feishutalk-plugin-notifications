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
import io.jenkins.plugins.feishu.contants.NotifyConstants;
import io.jenkins.plugins.feishu.service.FeiShuService;
import io.jenkins.plugins.feishu.service.impl.FeiShuServiceImpl;
import lombok.Data;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Created by Marvin on 16/8/25.
 */
@Data
public class FeiShuNotifier extends Notifier {

    private final String jenkinsURL;

    private final String url;

    private final Boolean onStart;

    private final Boolean onSuccess;

    private final Boolean onFailed;

    @DataBoundConstructor
    public FeiShuNotifier(String jenkinsURL, String url, boolean onStart, boolean onSuccess, boolean onFailed) {
        super();
        this.jenkinsURL = jenkinsURL;
        this.url = url;
        this.onStart = onStart;
        this.onSuccess = onSuccess;
        this.onFailed = onFailed;
    }

    public FeiShuService newService(AbstractBuild build, TaskListener listener) {
        return new FeiShuServiceImpl(jenkinsURL, url, onStart, onSuccess, onFailed, listener, build);
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) {
        return true;
    }

    @Override
    public FeiShuNotifierDescriptor getDescriptor() {
        return (FeiShuNotifierDescriptor) super.getDescriptor();
    }

    @Extension
    public static class FeiShuNotifierDescriptor extends BuildStepDescriptor<Publisher> {
        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return NotifyConstants.DISPLAY_NAME;
        }
    }
}
