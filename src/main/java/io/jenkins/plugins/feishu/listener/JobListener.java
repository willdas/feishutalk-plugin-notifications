package io.jenkins.plugins.feishu.listener;

import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.Descriptor;
import hudson.model.Result;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;
import hudson.tasks.Publisher;
import io.jenkins.plugins.feishu.FeiShuNotifier;
import io.jenkins.plugins.feishu.service.FeiShuService;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * @className: JobListener
 * @package: io.jenkins.plugins.feishu.listener
 * @describe: 任务监听器
 * @author: willdas
 * @date: 2023/04/14 18:10
 **/
@Extension
public class JobListener extends RunListener<AbstractBuild> {

    public JobListener() {
        super(AbstractBuild.class);
    }

    @Override
    public void onStarted(AbstractBuild r, TaskListener listener) {
        this.getService(r, listener).start();
    }

    @Override
    public void onCompleted(AbstractBuild r, @Nonnull TaskListener listener) {
        Result result = r.getResult();
        if (null != result && result.equals(Result.FAILURE)) {
            this.getService(r, listener).failed();
        } else {
            this.getService(r, listener).success();
        }
    }

    private FeiShuService getService(AbstractBuild build, TaskListener listener) {
        Map<Descriptor<Publisher>, Publisher> map = build.getProject().getPublishersList().toMap();
        for (Publisher publisher : map.values()) {
            if (publisher instanceof FeiShuNotifier) {
                return ((FeiShuNotifier) publisher).newService(build, listener);
            }
        }
        return null;
    }
}
