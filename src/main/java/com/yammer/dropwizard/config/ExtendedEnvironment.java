package com.yammer.dropwizard.config;

import com.google.common.collect.*;
import com.sun.jersey.core.reflection.AnnotatedMethod;
import com.sun.jersey.core.reflection.MethodList;
import com.yammer.dropwizard.json.ObjectMapperFactory;
import com.yammer.dropwizard.tasks.Task;
import com.yammer.dropwizard.validation.Validator;
import com.yammer.metrics.core.HealthCheck;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.Path;
import java.util.EventListener;

/**
 * This class is here to enable us to pull out various things from the {@link Environment}.
 */
public class ExtendedEnvironment extends Environment {

    final private static Logger logger = LoggerFactory.getLogger(ExtendedEnvironment.class);
    /**
     * Creates a new environment.
     *
     * @param name                the name of the service
     * @param configuration       the service's {@link com.yammer.dropwizard.config.Configuration}
     * @param objectMapperFactory the {@link com.yammer.dropwizard.json.ObjectMapperFactory} for the service
     */
    public ExtendedEnvironment(String name,
                               Configuration configuration,
                               ObjectMapperFactory objectMapperFactory,
                               Validator validator) {
        super(name, configuration, objectMapperFactory, validator);

    }

    @Override
    public ImmutableSet<Task> getTasks() {
        return super.getTasks();
    }

    @Override
    public ImmutableSet<HealthCheck> getHealthChecks() {
        return super.getHealthChecks();
    }

    @Override
    public Resource getBaseResource() {
        return super.getBaseResource();
    }

    @Override
    public ImmutableSet<String> getProtectedTargets() {
        return super.getProtectedTargets();
    }

    @Override
    public ImmutableMap<String, ServletHolder> getServlets() {
        return super.getServlets();
    }

    @Override
    public ImmutableMultimap<String, FilterHolder> getFilters() {
        return super.getFilters();
    }

    @Override
    public ImmutableSet<EventListener> getServletListeners() {
        return super.getServletListeners();
    }

    /**
     * Log all the resources.
     */
    private void logResources() {
        final ImmutableSet.Builder<String> builder = ImmutableSet.builder();

        for (Class<?> klass : super.getJerseyResourceConfig().getClasses()) {
            if (klass.isAnnotationPresent(Path.class)) {
                builder.add(klass.getCanonicalName());
            }
        }

        for (Object o : super.getJerseyResourceConfig().getSingletons()) {
            if (o.getClass().isAnnotationPresent(Path.class)) {
                builder.add(o.getClass().getCanonicalName());
            }
        }

        logger.debug("resources = {}", builder.build());
    }

    /**
     * Log all endpoints.
     */
    public void logEndpoints(Configuration configuration) {
        final StringBuilder stringBuilder = new StringBuilder(1024).append("The following paths were found for the configured resources:\n\n");

        final ImmutableList.Builder<Class<?>> builder = ImmutableList.builder();
        for (Object o : super.getJerseyResourceConfig().getSingletons()) {
            if (o.getClass().isAnnotationPresent(Path.class)) {
                builder.add(o.getClass());
            }
        }
        for (Class<?> klass : super.getJerseyResourceConfig().getClasses()) {
            if (klass.isAnnotationPresent(Path.class)) {
                builder.add(klass);
            }
        }

        for (Class<?> klass : builder.build()) {
            final String path = klass.getAnnotation(Path.class).value();
            String rootPath = configuration.getHttpConfiguration().getRootPath();
            if (rootPath.endsWith("/*")) {
                rootPath = rootPath.substring(0, rootPath.length() - (path.startsWith("/") ? 2 : 1));
            }

            final ImmutableList.Builder<String> endpoints = ImmutableList.builder();
            for (AnnotatedMethod method : annotatedMethods(klass)) {
                final StringBuilder pathBuilder = new StringBuilder()
                        .append(rootPath)
                        .append(path);
                if (method.isAnnotationPresent(Path.class)) {
                    final String methodPath = method.getAnnotation(Path.class).value();
                    if (!methodPath.startsWith("/") && !path.endsWith("/")) {
                        pathBuilder.append('/');
                    }
                    pathBuilder.append(methodPath);
                }
                for (HttpMethod verb : method.getMetaMethodAnnotations(HttpMethod.class)) {
                    endpoints.add(String.format("    %-7s %s (%s)",
                            verb.value(),
                            pathBuilder.toString(),
                            klass.getCanonicalName()));
                }
            }

            for (String line : Ordering.natural().sortedCopy(endpoints.build())) {
                stringBuilder.append(line).append('\n');
            }
        }

        logger.info(stringBuilder.toString());
    }


    private MethodList annotatedMethods(Class<?> resource) {
        return new MethodList(resource, true).hasMetaAnnotation(HttpMethod.class);
    }

    /**
     * Validate the Jersey resources before launching.
     */
    public void validateJerseyResources() {
         logResources();
    }
}