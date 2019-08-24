package com.mergebase.push0ver.post;

import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.task.TaskContext;
import com.atlassian.bamboo.task.TaskException;
import com.atlassian.bamboo.task.TaskResult;
import com.atlassian.bamboo.task.TaskResultBuilder;
import com.atlassian.bamboo.task.TaskType;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.mergebase.push0ver.App;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

//@ExportAsService( {PostTask.class} )
@Named("PostTask")
public class PostTask implements TaskType {
    private static final String PLUGIN_STORAGE_KEY = "push0ver.adminui";

    @ComponentImport
    private final PluginSettingsFactory pluginSettingsFactory;

    @Inject
    public PostTask(PluginSettingsFactory pluginSettingsFactory) {
        this.pluginSettingsFactory = pluginSettingsFactory;
    }

    @Override
    public TaskResult execute(TaskContext taskContext) throws TaskException {
        final BuildLogger log = taskContext.getBuildLogger();
        TaskResultBuilder taskResultBuilder = TaskResultBuilder.newBuilder(taskContext);
        PluginSettings settings = pluginSettingsFactory.createGlobalSettings();
        Map<String, Object> context = new HashMap<String, Object>();

        context.put("username", settings.get(PLUGIN_STORAGE_KEY + ".username"));
        context.put("password", settings.get(PLUGIN_STORAGE_KEY + ".password"));
        context.put("url", settings.get(PLUGIN_STORAGE_KEY + ".url"));
        context.put("releaserepo", settings.get(PLUGIN_STORAGE_KEY + ".releaserepo"));
        context.put("snaprepo", settings.get(PLUGIN_STORAGE_KEY + ".snaprepo"));
        context.put("noderepo", settings.get(PLUGIN_STORAGE_KEY + ".noderepo"));
        context.put("globalclient", settings.get(PLUGIN_STORAGE_KEY + ".globalclient"));

        final String globalConfig = taskContext.getConfigurationMap().get("checkbox");
        final String pushCheckBox = taskContext.getConfigurationMap().get("pushcheckbox");
        String taskClient = taskContext.getConfigurationMap().get("taskclient");
        if ("true".equals(globalConfig)) {
            taskClient = context.get("globalclient").toString();
            if ("".equals(taskClient)) {
                taskClient = "false";
            }
        }
        final boolean sslTrustAll = "true".equalsIgnoreCase(taskClient.trim());
        String localdir = nullTrim(taskContext.getConfigurationMap().get("tasklocaldir"));
        String mavenHome = nullTrim(taskContext.getConfigurationMap().get("mavenhome"));

        String taskUsername = nullTrim(taskContext.getConfigurationMap().get("taskusername"));
        String taskPassword = nullTrim(taskContext.getConfigurationMap().get("taskpassword"));
        String taskUrl = nullTrim(taskContext.getConfigurationMap().get("taskurl"));
        String taskReleaseRepo = nullTrim(taskContext.getConfigurationMap().get("taskreleaserepo"));
        String taskSnapRepo = nullTrim(taskContext.getConfigurationMap().get("tasksnaprepo"));
        String taskNodeRepo = nullTrim(taskContext.getConfigurationMap().get("tasknoderepo"));

        String push = "";
        if (pushCheckBox.equals("true")) {
            push = "push";
        }

        if (globalConfig.equals("true")) {
            taskUsername = checkGlobalConfig(taskUsername, "username", context);
            taskPassword = checkGlobalConfig(taskPassword, "password", context);
            taskUrl = checkGlobalConfig(taskUrl, "url", context);
            taskReleaseRepo = checkGlobalConfig(taskReleaseRepo, "releaserepo", context);
            taskSnapRepo = checkGlobalConfig(taskSnapRepo, "snaprepo", context);
            taskNodeRepo = checkGlobalConfig(taskNodeRepo, "noderepo", context);
        }

        log.addBuildLogEntry("push0ver - RELEASE REPO:  " + taskReleaseRepo);
        log.addBuildLogEntry("push0ver - NODE REPO:  " + taskNodeRepo);

        if (taskUsername.equals("Empty") || taskPassword.equals("Empty")) {
            log.addBuildLogEntry("push0ver - REPOSITORY Username/Password NOT SET");
        }

        String bambooWorkDir = taskContext.getWorkingDirectory().getPath();
        Properties p = new Properties();

        if ("".equals(localdir)) {
            localdir = bambooWorkDir;
        } else if (!localdir.startsWith("/")) {
            p.setProperty("git.repo", bambooWorkDir);
            if (!bambooWorkDir.substring(bambooWorkDir.length() - 1).equals("/")) {
                localdir = bambooWorkDir + "/" + localdir;
            }
        }
        log.addBuildLogEntry("push0ver - WORKING DIR:  " + localdir);

        try {
            String[] arg = new String[]{localdir, push};
            p.setProperty("repo.name", taskReleaseRepo);
            p.setProperty("snap.repo", taskSnapRepo);
            p.setProperty("art.username", taskUsername);
            p.setProperty("art.password", taskPassword);
            p.setProperty("art.url", taskUrl);
            p.setProperty("mvn.home", mavenHome);
            p.setProperty("noderepo.name", taskNodeRepo);
            p.setProperty("ssl.trustAll", Boolean.toString(sslTrustAll));
            App.invoke(arg, p, logLine -> {
                return log.addBuildLogEntry(logLine);
            });
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            String stacktrace = sw.toString();
            log.addBuildLogEntry("push0ver - STACKTRACE:     " + stacktrace);
            taskResultBuilder.failed();
        }

        return taskResultBuilder.build();
    }

    private String checkGlobalConfig(String taskTerm, String otherTerm, Map<String, Object> context) {
        if (taskTerm.trim().length() > 2 || context.get(otherTerm) == null) {
            return taskTerm;
        } else if (context.get(otherTerm).toString().length() > 2) {
            return context.get(otherTerm).toString();
        } else {
            throw new RuntimeException("You forgot to enter a " + otherTerm);
        }
    }

    private static String nullTrim(String s) {
        return s != null ? s.trim() : "";
    }
}
