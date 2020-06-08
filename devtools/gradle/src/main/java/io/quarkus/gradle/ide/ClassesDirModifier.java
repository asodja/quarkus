package io.quarkus.gradle.ide;

import static java.util.stream.Collectors.toSet;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.stream.Stream;

public class ClassesDirModifier {

    private static final Set<String> GRADLE_MAIN_CLASSES = Stream.of(new String[] {
            "build" + File.separator + "classes" + File.separator + "java" + File.separator + "main",
            "build" + File.separator + "classes" + File.separator + "kotlin" + File.separator + "main",
            "build" + File.separator + "classes" + File.separator + "scala" + File.separator + "main"
    }).collect(toSet());

    private static final Set<String> GRADLE_TEST_CLASSES = Stream.of(new String[] {
            "build" + File.separator + "classes" + File.separator + "java" + File.separator + "test",
            "build" + File.separator + "classes" + File.separator + "kotlin" + File.separator + "test",
            "build" + File.separator + "classes" + File.separator + "scala" + File.separator + "test"
    }).collect(toSet());
    
    private static final String INTELLIJ_MAIN_CLASSES = "out" + File.separator + "production" + File.separator + "classes";
    private static final String INTELLIJ_TEST_CLASSES = "out" + File.separator + "test" + File.separator + "classes";

    private final GradleInvocation gradleInvocation;

    public ClassesDirModifier(GradleInvocation gradleInvocation) {
        this.gradleInvocation = gradleInvocation;
    }

    public Path modify(Path path) {
        // Only have to replace paths for IntelliJ runner,
        if (gradleInvocation == GradleInvocation.INTELLIJ_RUNNER) {
            return modifyForIntelliJRunner(path);
        }
        return path;
    }

    private Path modifyForIntelliJRunner(Path path) {
        for (String classes : GRADLE_MAIN_CLASSES) {
            if (path.toString().contains(classes)) {
                return Paths.get(path.toString().replace(classes, INTELLIJ_MAIN_CLASSES));
            }
        }
        for (String classes : GRADLE_TEST_CLASSES) {
            if (path.toString().contains(classes)) {
                return Paths.get(path.toString().replace(classes, INTELLIJ_TEST_CLASSES));
            }
        }
        return path;
    }

}
