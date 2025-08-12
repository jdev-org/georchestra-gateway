/*
 * Copyright (C) 2023 by the geOrchestra PSC
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
package org.georchestra.gateway.autoconfigure.app;

import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.reactive.error.ErrorWebFluxAutoConfiguration;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.reactive.function.server.ServerRequest;

/**
 * Custom error attributes that remap specific exceptions to appropriate HTTP
 * status codes.
 * <p>
 * This class extends {@link DefaultErrorAttributes} to modify error responses
 * in a Spring WebFlux application. It ensures that:
 * <ul>
 * <li>{@link UnknownHostException} and {@link ConnectException} return an HTTP
 * 503 ({@link HttpStatus#SERVICE_UNAVAILABLE}) instead of the default HTTP
 * 500.</li>
 * <li>{@link AccessDeniedException} results in an HTTP 403
 * ({@link HttpStatus#FORBIDDEN}).</li>
 * </ul>
 * </p>
 *
 * <p>
 * In dynamic environments like Kubernetes and Docker Compose, service
 * unavailability may manifest as DNS resolution failures, which would normally
 * result in HTTP 500. This class ensures that such failures correctly return
 * HTTP 503.
 * </p>
 *
 * <p>
 * This class is injected into the {@link ErrorWebExceptionHandler} and replaces
 * the default error handling provided by {@link ErrorWebFluxAutoConfiguration}.
 * </p>
 *
 * @see DefaultErrorAttributes
 * @see ErrorWebFluxAutoConfiguration
 * @see ErrorWebExceptionHandler
 */
public class CustomErrorAttributes extends DefaultErrorAttributes {

    /** Path to the geOrchestra custom stylesheet, if configured. */
    private @Value("${georchestraStylesheet:}") String georchestraStylesheet;

    /** URL of the logo displayed in the header. */
    private @Value("${logoUrl:}") String logoUrl;

    /**
     * Overrides the default error attributes to remap specific exceptions to
     * appropriate HTTP status codes.
     * <p>
     * This method retrieves the original error attributes and modifies the status
     * code for the following exceptions:
     * <ul>
     * <li>{@link UnknownHostException} and {@link ConnectException} → HTTP 503
     * (Service Unavailable)</li>
     * <li>{@link AccessDeniedException} → HTTP 403 (Forbidden)</li>
     * </ul>
     * </p>
     *
     * <p>
     * <b>Example Modified Response:</b>
     * </p>
     * 
     * <pre>
     * {
     *   "status": 503,
     *   "error": "Service Unavailable",
     *   "message": "Upstream service unavailable"
     * }
     * </pre>
     *
     * @param request the server request that caused the error
     * @param options options for error attribute filtering
     * @return a modified map of error attributes with updated status codes
     */
    @Override
    public Map<String, Object> getErrorAttributes(ServerRequest request, ErrorAttributeOptions options) {
        Map<String, Object> attributes = super.getErrorAttributes(request, options);
        Throwable error = super.getError(request);

        if (error instanceof UnknownHostException || error instanceof ConnectException) {
            attributes.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
            attributes.put("error", HttpStatus.SERVICE_UNAVAILABLE.getReasonPhrase());
        } else if (error instanceof AccessDeniedException) {
            attributes.put("status", HttpStatus.FORBIDDEN.value());
            attributes.put("error", HttpStatus.FORBIDDEN.getReasonPhrase());
        }
        attributes.put("georchestraStylesheet", georchestraStylesheet);
        attributes.put("logoUrl", logoUrl);

        return attributes;
    }
}
