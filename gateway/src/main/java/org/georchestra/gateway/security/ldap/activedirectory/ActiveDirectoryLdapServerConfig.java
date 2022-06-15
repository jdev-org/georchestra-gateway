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
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * geOrchestra.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.georchestra.gateway.security.ldap.activedirectory;

import java.util.Optional;

import lombok.Builder;
import lombok.Generated;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
@Generated
public class ActiveDirectoryLdapServerConfig {
    private @NonNull String name;
    private boolean enabled;
    private @NonNull String url;

    private @NonNull Optional<String> searchFilter;
    private @NonNull Optional<String> userBase;

    private @NonNull Optional<String> adminDn;
    private @NonNull Optional<String> adminPassword;

}