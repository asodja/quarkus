package io.quarkus.bootstrap.resolver.gradle;

import static io.quarkus.bootstrap.util.AppModelCacheUtils.tryReadCachedCpPath;
import static io.quarkus.bootstrap.util.AppModelCacheUtils.tryWriteCachedCp;

import io.quarkus.bootstrap.model.AppModel;
import io.quarkus.bootstrap.resolver.gradle.workspace.GradleIdeWorkspace;
import io.quarkus.bootstrap.util.IoUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;
import org.jboss.logging.Logger;

public class BootstrapGradleIdeAppModelProvider {

    private static final Logger log = Logger.getLogger(BootstrapGradleIdeAppModelProvider.class);
    private static final String QUARKUS_TEST_CONFIG_TASK = "quarkusTestConfig";
    private final GradleIdeWorkspaceProvider workspaceProvider = new GradleIdeWorkspaceProvider();

    public AppModel getAppModel(Path appClassLocation) {
        Optional<GradleIdeWorkspace> projectOptional = workspaceProvider.getGradleIdeWorkspace(appClassLocation);
        if (!projectOptional.isPresent() || !isRunnableGradleProject(projectOptional.get())) {
            return null;
        }
        GradleIdeWorkspace gradleProject = projectOptional.get();
        Path appModelPath = gradleProject.getCachedAppModelPath();
        int workspaceId = gradleProject.getWorkspaceId();
        long buildGradleLastModified = gradleProject.getBuildGradlePath().toFile().lastModified();
        AppModel appModel = tryReadCachedCpPath(null, appModelPath, buildGradleLastModified, workspaceId);
        return appModel == null ? generateNewCachedAppModel(gradleProject) : appModel;
    }

    private boolean isRunnableGradleProject(GradleIdeWorkspace gradleProject) {
        return pathExists(gradleProject.getGradleProjectRoot())
                && pathExists(gradleProject.getBuildGradlePath())
                // One of GRADLE_HOME or wrapper has to be set to be able to run connector
                && (pathExists(gradleProject.getGradleHomePath())
                        || pathExists(gradleProject.getGradleWrapperPropertiesPath()));
    }

    private AppModel generateNewCachedAppModel(GradleIdeWorkspace gradleProject) {
        try {
            log.info("Generating new Gradle Cache model for workspace id: " + gradleProject.getWorkspaceId());
            runGradleTestConfigTask(gradleProject);
            AppModel appModel = readGradleTaskAppModel(gradleProject);
            if (appModel != null) {
                tryWriteCachedCp(gradleProject.getCachedAppModelPath(), appModel, gradleProject.getWorkspaceId());
            }
            return appModel;
        } catch (Exception e) {
            log.error("Failed generating new Gradle App Model with GradleConnector.", e);
            return null;
        }
    }

    private void runGradleTestConfigTask(GradleIdeWorkspace gradleProject) {
        try (ProjectConnection connection = getGradleConnector(gradleProject).connect()) {
            connection.newBuild()
                    .withArguments(QUARKUS_TEST_CONFIG_TASK, "--runner", gradleProject.getRunner())
                    .run();
        }
    }

    private GradleConnector getGradleConnector(GradleIdeWorkspace gradleProject) {
        GradleConnector connector = GradleConnector.newConnector()
                .forProjectDirectory(gradleProject.getProjectPath().toFile());
        // Connector auto-discovers wrapper if it exists, else lets use gradle from GRADLE_HOME
        return pathExists(gradleProject.getGradleWrapperPropertiesPath())
                ? connector
                : connector.useInstallation(gradleProject.getGradleHomePath().toFile());
    }

    private AppModel readGradleTaskAppModel(GradleIdeWorkspace gradleProject) {
        Path modelPath = gradleProject.getGradleAppModelPath();
        if (pathExists(modelPath)) {
            try (InputStream existing = Files.newInputStream(modelPath)) {
                return (AppModel) new ObjectInputStream(existing).readObject();
            } catch (IOException | ClassNotFoundException e) {
                log.error("Failed to load serialized app mode", e);
                IoUtils.recursiveDelete(modelPath);
            }
        }
        log.error("Failed to locate serialized application model at " + modelPath.toString());
        return null;
    }

    private boolean pathExists(Path path) {
        return path != null && Files.exists(path);
    }

}
