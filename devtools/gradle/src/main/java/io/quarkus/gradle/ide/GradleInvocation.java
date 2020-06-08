package io.quarkus.gradle.ide;

public enum GradleInvocation {
    INTELLIJ_RUNNER("intellij"),
    INTELLIJ_WITH_GRADLE_RUNNER("intellij-gradle"),
    GRADLE_RUNNER("gradle");

    private final String name;

    GradleInvocation(String name) {
        this.name = name;
    }

    public boolean isRunFromIde() {
        return this == INTELLIJ_RUNNER || this == INTELLIJ_WITH_GRADLE_RUNNER;
    }

    public static GradleInvocation of(String invocation) {
        for (GradleInvocation v : values()) {
            if (v.name.equals(invocation)) {
                return v;
            }
        }
        return GRADLE_RUNNER;
    }

}
