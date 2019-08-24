package com.mergebase.push0ver.post;

import com.atlassian.bamboo.collections.ActionParametersMap;
import com.atlassian.bamboo.task.AbstractTaskConfigurator;
import com.atlassian.bamboo.task.TaskDefinition;
import com.atlassian.bamboo.utils.error.ErrorCollection;

import java.util.Map;

public class PostConfig extends AbstractTaskConfigurator {
    private String checkBox = "false";
    private String pushCheckBox = "true";
    private String taskClient = "false";
    String[] toFill = {"tasklocaldir", "taskreleaserepo", "tasksnaprepo", "tasknoderepo", "taskurl", "taskusername", "taskpassword", "mavenhome",
            "checkbox", "pushcheckbox", "taskclient"};

    public Map<String, String> generateTaskConfigMap(final ActionParametersMap params, final TaskDefinition previousTaskDefinition) {
        final Map<String, String> config = super.generateTaskConfigMap(params, previousTaskDefinition);
        fillConfig(config, params, toFill);
        checkBox = params.getString("checkbox");
        pushCheckBox = params.getString("pushcheckbox");
        taskClient = params.getString("taskclient");
        return config;
    }

    public void validate(final ActionParametersMap params, final ErrorCollection errorCollection) {
        super.validate(params, errorCollection);
    }

    public void populateContextForCreate(final Map<String, Object> context) {
        super.populateContextForCreate(context);
        context.put("checkbox", checkBox);
        context.put("pushcheckbox", pushCheckBox);
        context.put("taskclient", taskClient);
    }

    public void populateContextForEdit(final Map<String, Object> context, final TaskDefinition taskDefinition) {
        super.populateContextForEdit(context, taskDefinition);
        fillContext(context, taskDefinition, toFill);
    }

    public void populateContextForView(final Map<String, Object> context, final TaskDefinition taskDefinition) {
        super.populateContextForView(context, taskDefinition);
        fillContext(context, taskDefinition, toFill);
    }

    private void fillContext(Map<String, Object> context, TaskDefinition taskDefinition, String[] toFill) {
        for (String s : toFill) {
            context.put(s, taskDefinition.getConfiguration().get(s));
        }
    }

    private void fillConfig(Map<String, String> config, ActionParametersMap params, String[] toFill) {
        for (String s : toFill) {
            config.put(s, params.getString(s));
        }
    }

}
