package io.quarkus.bootstrap.resolver.gradle;

import io.quarkus.bootstrap.resolver.gradle.workspace.GradleIdeWorkspace;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class GradleIdeWorkspaceProvider {

    private static final String INTELLIJ_RUNNER = "intellij";
    private static final String INTELLIJ_WITH_GRADLE_RUNNER = "intellij-gradle";
    private static final String GRADLE_HOME = "GRADLE_HOME";
    private static final List<String> BUILD_GRADLE_FILES = Arrays.asList("build.gradle", "build.gradle.kts");
    private static final List<String> SETTING_FILES = Arrays.asList("settings.gradle", "settings.gradle.kts");
    private static final Path GRADLE_APP_MODEL_RELATIVE_PATH = Paths.get("build",
            "tmp", "quarkusTestConfig", "quarkus-app-model.dat");
    private static final Path CACHED_APP_MODEL_RELATIVE_PATH = Paths.get("build",
            "tmp", "quarkusTestConfig", "quarkus-app-model-cached.dat");
    private static final String GRADLE_TEST_LOCATION = File.separator + "build" + File.separator + "classes" + File.separator
            + "java" + File.separator + "test";
    private static final String INTELLIJ_TEST_LOCATION = File.separator + "out" + File.separator + "test" + File.separator
            + "classes";

    public Optional<GradleIdeWorkspace> getGradleIdeWorkspace(Path appClassLocation) {
        if (appClassLocation == null) {
            return Optional.empty();
        }
        TestInvocationInfo testInvocationInfo = getTestInvocationInfo(appClassLocation);
        Path projectPath = testInvocationInfo.projectPath;
        if (testInvocationInfo.projectPath == null) {
            return Optional.empty();
        }
        Path gradleProjectRootPath = getGradleProjectRoot(projectPath);
        Path buildGradlePath = getGradleBuildPath(projectPath);
        if (gradleProjectRootPath == null || buildGradlePath == null) {
            return Optional.empty();
        }
        Path gradleHomePath = System.getenv(GRADLE_HOME) != null
                ? Paths.get(System.getenv(GRADLE_HOME))
                : null;
        Path gradleWrapperPropertiesPath = Paths.get(gradleProjectRootPath.toString(), "wrapper", "gradle-wrapper.properties");
        Path gradleAppModelPath = Paths.get(projectPath.toString(), GRADLE_APP_MODEL_RELATIVE_PATH.toString());
        Path cachedAppModelPath = Paths.get(projectPath.toString(), CACHED_APP_MODEL_RELATIVE_PATH.toString());
        int workspaceId = System.getProperty("java.class.path").hashCode();
        GradleIdeWorkspace gradleIdeWorkspace = new GradleIdeWorkspace(
                projectPath,
                gradleProjectRootPath,
                gradleHomePath,
                buildGradlePath,
                gradleWrapperPropertiesPath,
                gradleAppModelPath,
                cachedAppModelPath,
                testInvocationInfo.runner,
                workspaceId);
        return Optional.of(gradleIdeWorkspace);
    }

    private TestInvocationInfo getTestInvocationInfo(Path appClassLocation) {
        if (appClassLocation.toString().contains(GRADLE_TEST_LOCATION)) {
            // It looks like user is using Gradle Runner
            int startOf = appClassLocation.toString().indexOf(GRADLE_TEST_LOCATION);
            String path = appClassLocation.toString().substring(0, startOf);
            return new TestInvocationInfo(Paths.get(path), INTELLIJ_WITH_GRADLE_RUNNER);
        } else if (appClassLocation.toString().contains(INTELLIJ_TEST_LOCATION)) {
            // It looks like user is using IntelliJ Runner
            int startOf = appClassLocation.toString().indexOf(INTELLIJ_TEST_LOCATION);
            String path = appClassLocation.toString().substring(0, startOf);
            return new TestInvocationInfo(Paths.get(path), INTELLIJ_RUNNER);
        }
        // It looks like it is not using IntelliJ runner or Gradle Runner but something else
        return new TestInvocationInfo(null, null);
    }

    private Path getGradleBuildPath(Path projectPath) {
        return BUILD_GRADLE_FILES.stream()
                .map(buildFile -> Paths.get(projectPath.toString(), buildFile))
                .filter(path -> Files.exists(path))
                .findFirst()
                .orElse(null);
    }

    private Path getGradleProjectRoot(Path projectPath) {
        // From: https://docs.gradle.org/current/dsl/org.gradle.api.initialization.Settings.html#N1927E,
        // project root is where settings file is
        for (Path currentPath = projectPath; currentPath != null; currentPath = currentPath.getParent()) {
            for (String settingsFile : SETTING_FILES) {
                if (Files.exists(Paths.get(currentPath.toString(), settingsFile))) {
                    return currentPath;
                }
            }
        }
        return null;
    }

    private static class TestInvocationInfo {

        private final Path projectPath;
        private final String runner;

        public TestInvocationInfo(Path projectPath, String runner) {
            this.projectPath = projectPath;
            this.runner = runner;
        }

    }

}
