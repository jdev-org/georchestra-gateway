/*
 * Copyright (C) 2022 by the geOrchestra PSC
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

import static org.assertj.core.api.Assertions.assertThat;

import org.georchestra.gateway.handler.predicate.QueryParamRoutePredicateFactory;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

/**
 * Verify context contributions for
 * {@link RoutePredicateFactoriesAutoConfiguration}
 */
class RoutePredicateFactoriesAutoConfigurationTest {

    private ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(RoutePredicateFactoriesAutoConfiguration.class));

    @Test
    void testContext() {
        runner.run(context -> {
            assertThat(context).hasSingleBean(QueryParamRoutePredicateFactory.class);
        });
    }

}
