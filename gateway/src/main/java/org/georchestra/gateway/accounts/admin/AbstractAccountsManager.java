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
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * geOrchestra.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.georchestra.gateway.accounts.admin;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import org.georchestra.ds.orgs.Org;
import org.georchestra.gateway.security.exceptions.DuplicatedEmailFoundException;
import org.georchestra.security.model.GeorchestraUser;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@RequiredArgsConstructor
public abstract class AbstractAccountsManager implements AccountManager {

    private final @NonNull ApplicationEventPublisher eventPublisher;

    protected final ReadWriteLock lock = new ReentrantReadWriteLock();

    @Override
    public GeorchestraUser getOrCreate(@NonNull GeorchestraUser mappedUser) throws DuplicatedEmailFoundException {
        return find(mappedUser).orElseGet(() -> createIfMissing(mappedUser));
    }

    public Optional<GeorchestraUser> findByEmailAndOAuth2OrgId(GeorchestraUser mappedUser) {
        Optional<GeorchestraUser> user = null;
        // search user by email
        if ((null != mappedUser.getOAuth2Provider()) && (null != mappedUser.getOAuth2Uid())
                && (null != mappedUser.getEmail())) {
            user = findByEmail(mappedUser.getEmail());
        }
        return user;
    }

    public Optional<GeorchestraUser> find(GeorchestraUser mappedUser) {
        lock.readLock().lock();
        try {
            return findInternal(mappedUser);
        } finally {
            lock.readLock().unlock();
        }
    }

    protected Optional<GeorchestraUser> findInternal(GeorchestraUser mappedUser) {
        if ((null != mappedUser.getOAuth2Provider()) && (null != mappedUser.getOAuth2Uid())) {
            // proconnect
            if (mappedUser.getOAuth2Provider().equals("proconnect")) {
                return findByEmail(mappedUser.getEmail());
            } else {
                // others
                return findByOAuth2Uid(mappedUser.getOAuth2Provider(), mappedUser.getOAuth2Uid());
            }
        }
        return findByUsername(mappedUser.getUsername());
    }

    public Org findOrgByUser(GeorchestraUser existingUser) {
        String existUserOrgCN = existingUser.getOrganization();
        return findOrg(existUserOrgCN).orElse(null);
    }

    /**
     * Control that orgUniqueId from provider match with georchestra orgUniqueId
     * 
     * @param mapped
     * @param existingUser
     * @return false if provider user's orgUniqueId is not same as LDAP user's
     *         orgUniqueId
     */
    public Boolean isSameOrgUniqueId(GeorchestraUser mapped, GeorchestraUser existingUser) {
        if (null == existingUser.getOrganization()) {
            return false;
        }
        // Compare mapped orgUniqueId with existing user's org uniqueOrgId
        Org existUserOrg = findOrgByUser(existingUser);
        String existOrgUniqueId = existUserOrg.getOrgUniqueId();
        // return false if provider user's orgUniqueId is not same as LDAP user's
        // orgUniqueId
        return mapped.getOAuth2OrgId().equals(existOrgUniqueId);
    }

    @Override
    public void createUserOrgUniqueIdIfMissing(@NonNull GeorchestraUser mapped) throws DuplicatedEmailFoundException {
        lock.writeLock().lock();
        try {
            // verify if user exist
            GeorchestraUser existing = findInternal(mapped).orElse(null);
            // verify if user org match between ldap and OAuth2 info
            if (!isSameOrgUniqueId(mapped, existing)) {
                // we find or create org from this orgUniqueId and add user to this org
                // unlink
                unlinkUserOrg(existing);
                // create org if necessary and add user to org
                ensureOrgUniqueIdExists(mapped);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    protected GeorchestraUser createIfMissing(GeorchestraUser mapped) throws DuplicatedEmailFoundException {
        lock.writeLock().lock();
        try {
            // verify if user exist
            GeorchestraUser existing = findInternal(mapped).orElse(null);
            // not exists
            if (null == existing) {
                // create
                createInternal(mapped);
                existing = findInternal(mapped).orElseThrow(() -> new IllegalStateException(
                        "User " + mapped.getUsername() + " not found right after creation"));
                eventPublisher.publishEvent(new AccountCreated(existing));
            }

            createUserOrgUniqueIdIfMissing(mapped);

            return existing;

        } finally {
            lock.writeLock().unlock();
        }
    }

    protected abstract Optional<GeorchestraUser> findByOAuth2Uid(String oauth2Provider, String oauth2Uid);

    protected abstract Optional<GeorchestraUser> findByUsername(String username);

    protected abstract Optional<GeorchestraUser> findByEmail(String email);

    protected abstract void createInternal(GeorchestraUser mapped);

    protected abstract void ensureOrgUniqueIdExists(GeorchestraUser mapped);

    protected abstract Optional<Org> findOrg(String orgId);

    protected abstract void unlinkUserOrg(GeorchestraUser existingUser);

}
