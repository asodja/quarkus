package io.quarkus.gradle.tasks;

import javax.inject.Inject;

import org.gradle.api.tasks.TaskAction;

public class QuarkusResolveDeploymentTask extends QuarkusTask {

    @Inject
    public QuarkusResolveDeploymentTask() {
        super("Resolves deployment configuration");
    }

    @TaskAction
    public void resolve() {
        // Shortcut to resolve deployment configuration
        extension().getAppModelResolver();
    }

}
