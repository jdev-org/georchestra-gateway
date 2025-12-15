package org.georchestra.gateway.accounts.admin.ldap;

import org.georchestra.ds.DataServiceException;
import org.georchestra.ds.roles.RoleDao;
import org.georchestra.ds.users.AccountDao;
import org.georchestra.ds.users.Account;
import org.georchestra.ds.orgs.OrgsDao;
import org.georchestra.gateway.security.GeorchestraGatewaySecurityConfigProperties;
import org.georchestra.gateway.security.ldap.extended.DemultiplexingUsersApi;
import org.georchestra.gateway.security.oauth2.OpenIdConnectCustomConfig;
import org.georchestra.security.model.GeorchestraUser;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.ldap.NameNotFoundException;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LdapAccountsManagerTest {

    public @Test void testEnsureRoleExist() throws DataServiceException {
        RoleDao roleDao = mock(RoleDao.class);
        when(roleDao.findByCommonName(anyString())).thenThrow(new NameNotFoundException("FAKE_ROLE"));

        LdapAccountsManager toTest = new LdapAccountsManager(mock(ApplicationEventPublisher.class), null, roleDao, null,
                null, null, null);

        toTest.ensureRoleExists("FAKE_ROLE");
        // No exception thrown
    }

    @Test
    void createInternal_setsPendingWhenModeratedSignupEnabledByDefault() throws Exception {
        AccountDao accountDao = mock(AccountDao.class);
        LdapAccountsManager toTest = newLdapAccountsManager(accountDao, roleDao(), new OpenIdConnectCustomConfig(),
                securityConfig().setModeratedSignup(true));

        GeorchestraUser user = sampleUser(null);

        toTest.createInternal(user);

        Account captured = captureInsertedAccount(accountDao);
        assertTrue(captured.isPending());
    }

    @Test
    void createInternal_setsPendingWhenProviderEnforcesModeratedSignup() throws Exception {
        AccountDao accountDao = mock(AccountDao.class);
        OpenIdConnectCustomConfig providersConfig = providersConfig("provider-a", true);

        LdapAccountsManager toTest = newLdapAccountsManager(accountDao, roleDao(), providersConfig, securityConfig());

        GeorchestraUser user = sampleUser("provider-a");

        toTest.createInternal(user);

        Account captured = captureInsertedAccount(accountDao);
        assertTrue(captured.isPending());
    }

    @Test
    void createInternal_doesNotSetPendingWhenProviderDisablesModeratedSignup() throws Exception {
        AccountDao accountDao = mock(AccountDao.class);
        OpenIdConnectCustomConfig providersConfig = providersConfig("provider-b", false);

        GeorchestraGatewaySecurityConfigProperties securityConfig = securityConfig().setModeratedSignup(true);
        LdapAccountsManager toTest = newLdapAccountsManager(accountDao, roleDao(), providersConfig, securityConfig);

        GeorchestraUser user = sampleUser("provider-b");

        toTest.createInternal(user);

        Account captured = captureInsertedAccount(accountDao);
        assertFalse(captured.isPending());
    }

    private GeorchestraUser sampleUser(String provider) {
        GeorchestraUser user = new GeorchestraUser();
        user.setUsername("jdoe");
        user.setEmail("jdoe@example.org");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setOrganization("");
        user.setRoles(new java.util.ArrayList<>());
        if (provider != null) {
            user.setOAuth2Provider(provider);
            user.setOAuth2Uid("uid-" + provider);
        }
        return user;
    }

    private GeorchestraGatewaySecurityConfigProperties securityConfig() {
        return new GeorchestraGatewaySecurityConfigProperties().setDefaultOrganization("");
    }

    private OpenIdConnectCustomConfig providersConfig(String providerName, Boolean moderatedSignup) {
        OpenIdConnectCustomConfig config = new OpenIdConnectCustomConfig();
        OpenIdConnectCustomConfig providerConfig = new OpenIdConnectCustomConfig();
        providerConfig.setModeratedSignup(moderatedSignup);
        config.getProvider().put(providerName, providerConfig);
        return config;
    }

    private LdapAccountsManager newLdapAccountsManager(AccountDao accountDao, RoleDao roleDao,
            OpenIdConnectCustomConfig providersConfig, GeorchestraGatewaySecurityConfigProperties securityConfig) {
        return new LdapAccountsManager(mock(ApplicationEventPublisher.class), accountDao, roleDao, mock(OrgsDao.class),
                mock(DemultiplexingUsersApi.class), securityConfig, providersConfig);
    }

    private RoleDao roleDao() {
        RoleDao roleDao = mock(RoleDao.class);
        return roleDao;
    }

    private Account captureInsertedAccount(AccountDao accountDao) throws Exception {
        ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
        verify(accountDao).insert(accountCaptor.capture());
        return accountCaptor.getValue();
    }
}
