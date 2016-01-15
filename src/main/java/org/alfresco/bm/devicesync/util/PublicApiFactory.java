package org.alfresco.bm.devicesync.util;

import org.springframework.social.alfresco.api.Alfresco;

/**
 * A factory for creating a public api connection for a specific user.
 * 
 * @author steveglover
 *
 */
public interface PublicApiFactory
{
    /**
     * Get a public api connection for the "username"
     * 
     * @param username
     *            the user name of the user
     * @return a public api connection for the given user
     * @throws Exception
     */
    Alfresco getPublicApi(String username);

    /**
     * Get a public api connection for the admin user of the given network.
     * 
     * Note: may not be implemented by all factories.
     * 
     * @param networkId
     *            the network id of the network admin user
     * @return a public api connection for the admin user of the given network
     * @throws Exception
     */
    Alfresco getTenantAdminPublicApi(String networkId);

    /**
     * Get a public api connection for the admin user.
     * 
     * Note: may not be implemented by all factories.
     * 
     * @return a public api connection for the admin user
     * @throws Exception
     */
    Alfresco getAdminPublicApi();
}
