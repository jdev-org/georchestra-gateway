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
package org.georchestra.gateway.security.ldap;

import static java.util.Optional.ofNullable;

import java.util.Optional;

import org.georchestra.gateway.security.ldap.LdapConfigProperties.Server;
import org.georchestra.gateway.security.ldap.LdapConfigProperties.Users;
import org.georchestra.gateway.security.ldap.activedirectory.ActiveDirectoryLdapServerConfig;
import org.georchestra.gateway.security.ldap.basic.LdapServerConfig;
import org.georchestra.gateway.security.ldap.extended.ExtendedLdapConfig;
import org.springframework.util.StringUtils;

/**
 */
class LdapConfigBuilder {

    public LdapServerConfig asBasicLdapConfig(String name, Server config) {
        return LdapServerConfig.builder()//
                .name(name)//
                .enabled(config.isEnabled())//
                .url(config.getUrl())//
                .baseDn(config.getBaseDn())//
                .usersRdn(config.getUsers().getRdn())//
                .usersSearchFilter(config.getUsers().getSearchFilter())//
                .rolesRdn(config.getRoles().getRdn())//
                .rolesSearchFilter(config.getRoles().getSearchFilter())//
                .build();
    }

    public ExtendedLdapConfig asExtendedLdapConfig(String name, Server config) {
        return ExtendedLdapConfig.builder()//
                .name(name)//
                .enabled(config.isEnabled())//
                .url(config.getUrl())//
                .baseDn(config.getBaseDn())//
                .usersRdn(config.getUsers().getRdn())//
                .usersSearchFilter(config.getUsers().getSearchFilter())//
                .rolesRdn(config.getRoles().getRdn())//
                .rolesSearchFilter(config.getRoles().getSearchFilter())//
                .orgsRdn(config.getOrgs().getRdn())//
                .build();
    }

    public ActiveDirectoryLdapServerConfig asActiveDirectoryConfig(String name, Server config) {
        return ActiveDirectoryLdapServerConfig.builder()//
                .name(name)//
                .enabled(config.isEnabled())//
                .url(config.getUrl())//
                .domain(toOptional(config.getDomain()))//
                .rootDn(toOptional(config.getBaseDn()))//
                .searchFilter(ofNullable(config.getUsers()).map(Users::getSearchFilter).flatMap(this::toOptional))//
                .build();
    }

    private Optional<String> toOptional(String value) {
        return ofNullable(StringUtils.hasText(value) ? value : null);
    }
}
