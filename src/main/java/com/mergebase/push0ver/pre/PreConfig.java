package com.mergebase.push0ver.pre;

import com.atlassian.bamboo.collections.ActionParametersMap;
import com.atlassian.bamboo.task.AbstractTaskConfigurator;
import com.atlassian.bamboo.task.TaskDefinition;
import com.atlassian.bamboo.utils.error.ErrorCollection;

import java.util.Map;

public class PreConfig extends AbstractTaskConfigurator {
    private String[] toFill = {"tasklocaldir", "allowAllConnect", "defaultcheckbox", "taskusername", "mavenhome",
            "taskpassword", "taskurl", "taskreleaserepo", "tasksnaprepo", "tasknoderepo"
    };

    public Map<String, String> generateTaskConfigMap(final ActionParametersMap params, final TaskDefinition previousTaskDefinition) {
        final Map<String, String> config = super.generateTaskConfigMap(params, previousTaskDefinition);
        fillConfig(config, params, toFill);
        return config;
    }

    public void validate(final ActionParametersMap params, final ErrorCollection errorCollection) {
        super.validate(params, errorCollection);
    }

    public void populateContextForCreate(final Map<String, Object> context) {
        super.populateContextForCreate(context);
        context.put("defaultcheckbox", "true");
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
