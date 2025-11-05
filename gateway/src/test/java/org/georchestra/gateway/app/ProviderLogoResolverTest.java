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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.DefaultResourceLoader;

class ProviderLogoResolverTest {

    @TempDir
    Path staticRoot;

    @Test
    void resolveProviderLogoIfDatadirImageExist() throws IOException {
        ConfigurableEnvironment environment = environmentWithProperties(Map.of(
                "spring.web.resources.static-locations", staticRoot.toUri().toString(),
                "spring.webflux.static-path-pattern", "/static/**"));

        Files.createDirectories(staticRoot.resolve("login/img"));
        Files.write(staticRoot.resolve("login/img/custom.png"), new byte[] { 1 });

        ProviderLogoResolver resolver = new ProviderLogoResolver(new DefaultResourceLoader(), environment);

        assertEquals("/static/login/img/custom.png", resolver.resolveProviderLogoPath("custom"));
    }

    @Test
    void resolveProviderLogoIfDatadirImageMissing() {
        ConfigurableEnvironment environment = environmentWithProperties(Map.of("spring.web.resources.static-locations",
                "file:" + staticRoot.toUri(), "spring.webflux.static-path-pattern", "/static/**"));

        ProviderLogoResolver resolver = new ProviderLogoResolver(new DefaultResourceLoader(), environment);

        assertEquals("/static/login/img/default.png", resolver.resolveProviderLogoPath("missing"));
    }

    @Test
    void resolveProviderLogoIfWrongLocation() {
        ConfigurableEnvironment environment = environmentWithProperties(Map.of("spring.web.resources.static-locations",
                "file:/does/not/exist", "spring.webflux.static-path-pattern", "/dist/**"));

        ProviderLogoResolver resolver = new ProviderLogoResolver(new DefaultResourceLoader(), environment);

        assertEquals("/dist/login/img/default.png", resolver.resolveProviderLogoPath("missing"));
    }

    @Test
    void resolveProviderLogoWhenNoStaticDatadir() {
        ConfigurableEnvironment environment = environmentWithProperties(Map.of());

        ProviderLogoResolver resolver = new ProviderLogoResolver(new DefaultResourceLoader(), environment);

        assertEquals("/login/img/default.png", resolver.resolveProviderLogoPath("missing"));
    }

    private ConfigurableEnvironment environmentWithProperties(Map<String, Object> properties) {
        ConfigurableEnvironment environment = new StandardEnvironment();
        if (!properties.isEmpty()) {
            environment.getPropertySources().addFirst(new MapPropertySource("test", properties));
        }
        return environment;
    }
}
