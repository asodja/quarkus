package io.quarkus.gradle.tasks;

import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.options.Option;

public class QuarkusIdeTestConfigTask extends QuarkusTask {

    @Input
    @Option(option = "runner", description = "Runner that is used to start this task")
    private String runner;

    public QuarkusIdeTestConfigTask() {
        super("Used to set the necessary system properties for the Quarkus tests when " +
                "Bootstrapping from IDE without Gradle runner (e.g. when using IntelliJ test runner).");
    }

    @TaskAction
    public void setupTest() {
        getLogger().warn(getPath() + " should not be called from Gradle directly, it is only use for Bootstrapping from IDE.");
    }

    public void setRunner(String runner) {
        this.runner = runner;
    }

    public String getRunner() {
        return runner;
    }

}
