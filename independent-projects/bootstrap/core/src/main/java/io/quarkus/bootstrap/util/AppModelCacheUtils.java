package io.quarkus.bootstrap.util;

import static io.quarkus.bootstrap.BootstrapAppModelFactory.CP_CACHE_FORMAT_ID;

import io.quarkus.bootstrap.model.AppArtifact;
import io.quarkus.bootstrap.model.AppDependency;
import io.quarkus.bootstrap.model.AppModel;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.jboss.logging.Logger;

public class AppModelCacheUtils {

    private static final Logger log = Logger.getLogger(AppModelCacheUtils.class);

    public static AppModel tryReadCachedCpPath(AppArtifact appArtifact, Path cachedCpPath, long workspaceLastModified,
            int workspaceId) {
        if (!Files.exists(cachedCpPath) || workspaceLastModified > cachedCpPath.toFile().lastModified()) {
            return null;
        }
        try (DataInputStream reader = new DataInputStream(Files.newInputStream(cachedCpPath))) {
            int cachedFormat = reader.readInt();
            if (!isValidCpCacheFormat(appArtifact, cachedCpPath, cachedFormat)) {
                return null;
            }

            int readWorkspaceId = reader.readInt();
            if (!isValidWorkspaceId(appArtifact, workspaceId, readWorkspaceId)) {
                return null;
            }

            ObjectInputStream in = new ObjectInputStream(reader);
            AppModel appModel = (AppModel) in.readObject();
            log.debugf("Loaded cached AppMode %s from %s", appModel, cachedCpPath);
            validateDependencies(appModel.getFullDeploymentDeps());
            return appModel;
        } catch (IOException | ClassNotFoundException e) {
            log.warn("Failed to read deployment classpath cache from " + cachedCpPath + " for " + appArtifact, e);
            return null;
        }
    }

    private static void validateDependencies(List<AppDependency> appDependencies) throws IOException {
        for (AppDependency dependency : appDependencies) {
            for (Path path : dependency.getArtifact().getPaths()) {
                if (!Files.exists(path)) {
                    throw new IOException("Cached artifact does not exist: " + path);
                }
            }
        }
    }

    private static boolean isValidCpCacheFormat(AppArtifact appArtifact, Path cachedCpPath, int cachedFormatId) {
        if (cachedFormatId != CP_CACHE_FORMAT_ID) {
            debug("Unsupported classpath cache format in %s for %s", cachedCpPath, appArtifact);
            return false;
        }
        return true;
    }

    private static boolean isValidWorkspaceId(AppArtifact appArtifact, int readWorkspaceId, int workspaceId) {
        if (readWorkspaceId != workspaceId) {
            debug("Cached deployment classpath has expired for %s", appArtifact);
            return false;
        }
        return true;
    }

    private static void debug(String msg, Object... args) {
        if (log.isDebugEnabled()) {
            log.debug(String.format(msg, args));
        }
    }

    public static void tryWriteCachedCp(Path cachedCpPath, AppModel appModel, int workspaceId) {
        if (cachedCpPath != null) {
            try (DataOutputStream out = new DataOutputStream(Files.newOutputStream(cachedCpPath))) {
                Files.createDirectories(cachedCpPath.getParent());
                out.writeInt(CP_CACHE_FORMAT_ID);
                out.writeInt(workspaceId);
                ObjectOutputStream obj = new ObjectOutputStream(out);
                obj.writeObject(appModel);
            } catch (IOException e) {
                log.warn("Failed to write classpath cache", e);
            }
        }
    }

}
