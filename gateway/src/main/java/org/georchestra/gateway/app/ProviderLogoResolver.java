/*
 * Copyright (C) 2024 by the geOrchestra PSC
 *
 * This file is part of geOrchestra.
 *
 * geOrchestra is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * geOrchestra is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * geOrchestra. If not, see <http://www.gnu.org/licenses/>.
 */
package org.georchestra.gateway.app;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Resolves the web-accessible path of OAuth provider logos, honoring custom
 * static resource locations defined in Spring configuration.
 */
@Component
class ProviderLogoResolver {

    static final String CLASSPATH_STATIC_LOCATION = "classpath:/static/";

    private final ResourceLoader resourceLoader;
    private final List<String> staticResourceLocations;
    private final String staticResourceWebPrefix;

    /**
     * Creates a resolver that looks up static resources using the provided Spring
     * infrastructure.
     *
     * @param resourceLoader loader used to access static resources across file
     *                       system and classpath locations
     * @param environment    Spring environment exposing static resource
     *                       configuration properties
     */
    ProviderLogoResolver(ResourceLoader resourceLoader, Environment environment) {
        this.resourceLoader = resourceLoader;
        this.staticResourceLocations = computeStaticResourceLocations(environment);
        this.staticResourceWebPrefix = computeStaticResourceWebPrefix(environment);
    }

    /**
     * Resolves the public URL of the OAuth provider logo, falling back to the
     * default icon when the specific logo cannot be found.
     *
     * @param providerKey OAuth2 provider identifier
     * @return web-accessible path of the resolved logo
     */
    String resolveProviderLogoPath(String providerKey) {
        String providerRelativePath = normalizeRelativePath(Path.of("login", "img", providerKey + ".png").toString());
        if (resourceExists(providerRelativePath)) {
            return buildWebPath(providerRelativePath);
        }
        return buildWebPath(normalizeRelativePath(Path.of("login", "img", "default.png").toString()));
    }

    /**
     * Checks whether the relative path exists in at least one configured static
     * resource location.
     *
     * @param normalizedRelativePath normalized relative resource location (never
     *                               prefixed with a slash)
     * @return {@code true} if the resource is available, {@code false} otherwise
     */
    private boolean resourceExists(String normalizedRelativePath) {
        for (String location : staticResourceLocations) {
            Resource resource = resourceLoader.getResource(location + normalizedRelativePath);
            if (resource.exists()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Builds the public URL for a static resource based on the configured web
     * prefix.
     *
     * @param normalizedRelativePath normalized relative resource location (never
     *                               prefixed with a slash)
     * @return request path that can be used by the UI to fetch the resource
     */
    private String buildWebPath(String normalizedRelativePath) {
        if ("/".equals(staticResourceWebPrefix)) {
            return "/" + normalizedRelativePath;
        }
        return staticResourceWebPrefix + normalizedRelativePath;
    }

    /**
     * Normalizes a relative path to a canonical form using forward slashes and no
     * leading separators.
     *
     * @param path path to normalize
     * @return normalized version of the path
     */
    private static String normalizeRelativePath(String path) {
        String normalized = path.replace('\\', '/');
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        return normalized;
    }

    /**
     * Builds the list of static resource locations combining configured locations
     * and the classpath fallback.
     *
     * @param environment Spring environment exposing resource configuration
     * @return ordered, de-duplicated list of resource base locations
     */
    private static List<String> computeStaticResourceLocations(Environment environment) {
        List<String> locations = new ArrayList<>();
        addLocations(locations, environment.getProperty("spring.web.resources.static-locations", String[].class));
        addLocations(locations, environment.getProperty("spring.resources.static-locations", String[].class));
        if (locations.isEmpty()) {
            locations.add(CLASSPATH_STATIC_LOCATION);
        } else if (locations.stream().noneMatch(location -> location.startsWith("classpath:"))) {
            locations.add(CLASSPATH_STATIC_LOCATION);
        }
        List<String> distinct = new ArrayList<>();
        for (String location : locations) {
            String candidate = appendTrailingSlash(location);
            if (!distinct.contains(candidate)) {
                distinct.add(candidate);
            }
        }
        return distinct;
    }

    /**
     * Adds non-empty candidate locations to the provided list.
     *
     * @param locations  existing list to complete
     * @param candidates optional candidate array read from configuration
     */
    private static void addLocations(List<String> locations, String[] candidates) {
        if (candidates == null) {
            return;
        }
        for (String candidate : candidates) {
            if (StringUtils.hasText(candidate)) {
                locations.add(candidate);
            }
        }
    }

    /**
     * Ensures configured resource location ends with a trailing slash to ease
     * concatenation.
     *
     * @param location base path to adjust
     * @return location guaranteed to end with a slash
     */
    private static String appendTrailingSlash(String location) {
        return location.endsWith("/") ? location : location + "/";
    }

    /**
     * Computes the URL prefix used to expose static resources (e.g.
     * {@code /static/} when configured).
     *
     * @param environment Spring environment exposing resource configuration
     * @return normalized web prefix including trailing slash or {@code /} when
     *         using the default mapping
     */
    private static String computeStaticResourceWebPrefix(Environment environment) {
        String pattern = environment.getProperty("spring.webflux.static-path-pattern");
        if (!StringUtils.hasText(pattern)) {
            pattern = environment.getProperty("spring.mvc.static-path-pattern");
        }
        if (!StringUtils.hasText(pattern)) {
            return "/";
        }
        pattern = pattern.trim();
        if (!pattern.startsWith("/")) {
            pattern = "/" + pattern;
        }
        if (pattern.endsWith("/**")) {
            pattern = pattern.substring(0, pattern.length() - 2);
        }
        if (pattern.isEmpty() || "/".equals(pattern)) {
            return "/";
        }
        if (!pattern.endsWith("/")) {
            pattern = pattern + "/";
        }
        return pattern;
    }
}
