package org.georchestra.gateway.app;

import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;

/**
 * Utility methods for resolving and checking static resources, and for
 * computing the web prefix used to serve static content.
 */
public class StaticResourcesUtils {
    /**
     * Resolves a resource path into a Spring {@link Resource} instance.
     * <p>
     * Supported prefixes:
     * <ul>
     * <li><b>classpath:</b> resolved as a {@link ClassPathResource}</li>
     * <li><b>file:</b> resolved as a {@link FileSystemResource}</li>
     * <li>otherwise</li>
     * </ul>
     * If no prefix is provided, the path is interpreted as a filesystem path.
     *
     * @param path fully qualified resource path
     * @return the resolved {@link Resource}
     */
    public static Resource resolveResource(String path) {
        if (path.startsWith("classpath:")) {
            return new ClassPathResource(path.substring(10));
        }
        if (path.startsWith("file:")) {
            return new FileSystemResource(path.substring(5));
        }
        return new FileSystemResource(path);
    }

    /**
     * Checks whether a resource exists and is readable.
     * <p>
     * Resolution is delegated to {@link #resolveResource(String)}.
     *
     * @param path fully qualified resource path
     * @return {@code true} if the resource exists and can be read
     */
    public static boolean resourceExists(String path) {
        return resolveResource(path).isReadable();
    }

    /**
     * Computes the web prefix used for serving static resources, based on Spring
     * MVC or WebFlux configuration.
     * <p>
     * Reads:
     * <ul>
     * <li>{@code spring.webflux.static-path-pattern}</li>
     * <li>{@code spring.mvc.static-path-pattern}</li>
     * </ul>
     * If no pattern is defined, "/" is returned. The resulting prefix is normalized
     * to start and end with a slash, and the terminal "/**" pattern is removed if
     * present.
     *
     * @param environment the Spring {@link Environment}
     * @return the normalized static web prefix (for example "/static/")
     */
    public static String computeStaticResourceWebPrefix(Environment environment) {
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
