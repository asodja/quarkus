package io.quarkus.bootstrap.resolver.gradle.workspace;

import java.nio.file.Path;

public class GradleIdeWorkspace {

    private final Path projectPath;
    private final Path gradleProjectRoot;
    private final Path gradleHomePath;
    private final Path gradleWrapperPropertiesPath;
    private final Path buildGradlePath;
    private final Path gradleAppModelPath;
    private final Path cachedAppModelPath;
    private final String runner;
    private final int workspaceId;

    public GradleIdeWorkspace(Path projectPath,
            Path gradleProjectRoot,
            Path gradleHomePath,
            Path buildGradlePath,
            Path gradleWrapperPropertiesPath,
            Path gradleAppModelPath,
            Path cachedAppModelPath,
            String runner,
            int workspaceId) {
        this.projectPath = projectPath;
        this.gradleProjectRoot = gradleProjectRoot;
        this.gradleHomePath = gradleHomePath;
        this.buildGradlePath = buildGradlePath;
        this.gradleWrapperPropertiesPath = gradleWrapperPropertiesPath;
        this.gradleAppModelPath = gradleAppModelPath;
        this.cachedAppModelPath = cachedAppModelPath;
        this.runner = runner;
        this.workspaceId = workspaceId;
    }

    public Path getProjectPath() {
        return projectPath;
    }

    public Path getGradleWrapperPropertiesPath() {
        return gradleWrapperPropertiesPath;
    }

    public Path getGradleProjectRoot() {
        return gradleProjectRoot;
    }

    public Path getGradleHomePath() {
        return gradleHomePath;
    }

    public Path getBuildGradlePath() {
        return buildGradlePath;
    }

    public Path getGradleAppModelPath() {
        return gradleAppModelPath;
    }

    public Path getCachedAppModelPath() {
        return cachedAppModelPath;
    }

    public String getRunner() {
        return runner;
    }

    public int getWorkspaceId() {
        return workspaceId;
    }

}
