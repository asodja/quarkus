package io.quarkus.gradle.convention;

import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.dsl.DependencyHandler;

public class QuarkusDependencyConvention {

    private final Configuration configuration;
    private final DependencyHandler dependencyHandler;

    public QuarkusDependencyConvention(DependencyHandler dependencyHandler, Configuration configuration) {
        this.dependencyHandler = dependencyHandler;
        this.configuration = configuration;
    }

    public Dependency quarkusEnforcedPlatform(Object var) {
        Dependency dependency = dependencyHandler.enforcedPlatform(var);
        configuration.getDependencies().add(dependency);
        return dependency;
    }

    // Implement also
    //   quarkusPlatform(Object var1), quarkusPlatform(Object var1, Action<? super Dependency> var2)
    //   quarkusEnforcedPlatform(Object var1, Action<? super Dependency> var2);
    // so users can choose

}
