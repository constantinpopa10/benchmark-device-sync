package org.alfresco.bm.devicesync.util;

import org.alfresco.bm.user.UserData;
import org.alfresco.bm.user.UserDataService;
import org.springframework.social.alfresco.api.Alfresco;
import org.springframework.social.alfresco.api.CMISEndpoint;
import org.springframework.social.alfresco.api.impl.ConnectionDetails;
import org.springframework.social.alfresco.connect.BasicAuthAlfrescoConnectionFactory;
import org.springframework.social.connect.Connection;

/**
 * A public api factory that uses basic authentication to communicate with a repository.
 * 
 * @author steveglover
 *
 */
public class BasicAuthPublicApiFactory implements PublicApiFactory
{
    private String repoScheme;
    private String repoHost;
    private int repoPort;
    private String syncScheme;
    private String syncHost;
    private int syncPort;
    private String subsScheme;
    private String subsHost;
    private int subsPort;
    private int maxNumberOfConnections;
    private int connectionTimeoutMs;
    private int socketTimeoutMs;
    private int socketTtlMs;
    private String context;
    private boolean ignoreServletName;
    private String publicApiServletName;
    private String serviceServletName;
    private CMISEndpoint preferredCMISEndPoint;

    private UserDataService userDataService;
    
    public BasicAuthPublicApiFactory(String repoScheme, String repoHost, int repoPort,
    		String syncScheme, String syncHost, int syncPort,
    		String subsScheme, String subsHost, int subsPort,
    		CMISEndpoint preferredCMISEndPoint, int maxNumberOfConnections, int connectionTimeoutMs, 
            int socketTimeoutMs, int socketTtlMs, UserDataService userDataService)
    {
    	this(repoScheme, repoHost, repoPort, syncScheme, syncHost, syncPort,
    			subsScheme, subsHost, subsPort,
    			preferredCMISEndPoint, maxNumberOfConnections, connectionTimeoutMs, socketTimeoutMs,
    			socketTtlMs, userDataService, "alfresco", "api", "service");
    	this.preferredCMISEndPoint = preferredCMISEndPoint;
    }

    public BasicAuthPublicApiFactory(String repoScheme, String repoHost, int repoPort,
    		String syncScheme, String syncHost, int syncPort,
    		String subsScheme, String subsHost, int subsPort,
    		CMISEndpoint preferredCMISEndPoint, int maxNumberOfConnections, int connectionTimeoutMs,
    		int socketTimeoutMs, int socketTtlMs,
    		UserDataService userDataService, String context, String publicApiServletName, String serviceServletName)
    {
        super();
        this.repoScheme = repoScheme;
        this.syncScheme = syncScheme;
        this.repoHost = repoHost;
        this.repoPort = repoPort;
        this.syncHost = syncHost;
        this.syncPort = syncPort;
        this.subsHost = subsHost;
        this.subsPort = subsPort;
        this.subsScheme = subsScheme;
        this.preferredCMISEndPoint = preferredCMISEndPoint;
        this.maxNumberOfConnections= maxNumberOfConnections;
        this.connectionTimeoutMs = connectionTimeoutMs;
        this.socketTimeoutMs = socketTimeoutMs;
        this.socketTtlMs= socketTtlMs; 
        this.userDataService = userDataService;
        this.context = context;
        this.publicApiServletName = publicApiServletName;
        this.serviceServletName = serviceServletName;
    }

	public String getContext()
	{
		return context;
	}

	public boolean isIgnoreServletName()
	{
		return ignoreServletName;
	}

	public String getRepoScheme()
    {
        return repoScheme;
    }

    public String getSyncScheme()
	{
		return syncScheme;
	}

	public String getRepoHost()
    {
        return repoHost;
    }

    public int getRepoPort()
    {
        return repoPort;
    }

    public String getSyncHost()
	{
		return syncHost;
	}

	public int getSyncPort()
	{
		return syncPort;
	}

	@Override
    public Alfresco getPublicApi(String username)
    {
        Alfresco alfresco = null;
        UserData user = userDataService.findUserByUsername(username);
        if(user != null)
        {
            ConnectionDetails repoConnectionDetails = new ConnectionDetails(repoScheme, repoHost, repoPort,
            		username, user.getPassword(), context,
            		publicApiServletName, serviceServletName, maxNumberOfConnections, connectionTimeoutMs,
            		socketTimeoutMs, socketTtlMs);
            ConnectionDetails syncConnectionDetails = new ConnectionDetails(syncScheme, syncHost, syncPort,
            		username, user.getPassword(), context,
            		publicApiServletName, serviceServletName, maxNumberOfConnections, connectionTimeoutMs, 
            		socketTimeoutMs, socketTtlMs);
            ConnectionDetails subsConnectionDetails = new ConnectionDetails(subsScheme, subsHost, subsPort,
            		username, user.getPassword(), context,
            		publicApiServletName, serviceServletName, maxNumberOfConnections, connectionTimeoutMs, 
            		socketTimeoutMs, socketTtlMs);
            BasicAuthAlfrescoConnectionFactory basicAuthConnectionFactory =
            		new BasicAuthAlfrescoConnectionFactory(repoConnectionDetails, syncConnectionDetails,
            				subsConnectionDetails);
            Connection<Alfresco> connection = basicAuthConnectionFactory.createConnection();
            alfresco = connection.getApi();
        }
        else
        {
            throw new RuntimeException("Username not held in local data mirror: " + username);
        }
        
        if (alfresco == null)
        {
            throw new RuntimeException("Unable to retrieve API connection to Alfresco.");
        }

        return alfresco;
    }
    
    @Override
    public Alfresco getTenantAdminPublicApi(String networkId)
    {
        ConnectionDetails repoConnectionDetails = new ConnectionDetails(repoScheme, repoHost, repoPort,
        		"admin@" + networkId, "admin", context,
        		publicApiServletName, serviceServletName, maxNumberOfConnections, connectionTimeoutMs,
        		socketTimeoutMs, socketTtlMs);
        ConnectionDetails syncConnectionDetails = new ConnectionDetails(syncScheme, syncHost, syncPort,
        		"admin@" + networkId, "admin", context,
        		publicApiServletName, serviceServletName, maxNumberOfConnections, connectionTimeoutMs,
        		socketTimeoutMs, socketTtlMs);
        ConnectionDetails subsConnectionDetails = new ConnectionDetails(subsScheme, subsHost, subsPort,
        		"admin@" + networkId, "admin", context,
        		publicApiServletName, serviceServletName, maxNumberOfConnections, connectionTimeoutMs, 
        		socketTimeoutMs, socketTtlMs);
        BasicAuthAlfrescoConnectionFactory connectionFactory =
        		new BasicAuthAlfrescoConnectionFactory(repoConnectionDetails, syncConnectionDetails,
        				subsConnectionDetails);
        Connection<Alfresco> connection = connectionFactory.createConnection();
        Alfresco alfresco = connection.getApi();
        return alfresco;
    }
    
    @Override
    public Alfresco getAdminPublicApi()
    {
        ConnectionDetails repoConnectionDetails = new ConnectionDetails(repoScheme, repoHost, repoPort,
        		"admin", "admin", context,
        		publicApiServletName, serviceServletName, maxNumberOfConnections, connectionTimeoutMs,
        		socketTimeoutMs, socketTtlMs);
        ConnectionDetails syncConnectionDetails = new ConnectionDetails(syncScheme, syncHost, syncPort,
        		"admin", "admin", context,
        		publicApiServletName, serviceServletName, maxNumberOfConnections, connectionTimeoutMs,
        		socketTimeoutMs, socketTtlMs);
        ConnectionDetails subsConnectionDetails = new ConnectionDetails(subsScheme, subsHost, subsPort,
        		"admin", "admin", context,
        		publicApiServletName, serviceServletName, maxNumberOfConnections, connectionTimeoutMs, 
        		socketTimeoutMs, socketTtlMs);
        BasicAuthAlfrescoConnectionFactory connectionFactory =
        		new BasicAuthAlfrescoConnectionFactory(repoConnectionDetails, syncConnectionDetails,
        				subsConnectionDetails);
        Connection<Alfresco> connection = connectionFactory.createConnection();
        Alfresco alfresco = connection.getApi();
        return alfresco;
    }
}
