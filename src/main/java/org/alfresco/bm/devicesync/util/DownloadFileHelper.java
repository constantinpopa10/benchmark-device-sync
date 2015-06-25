package org.alfresco.bm.devicesync.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.bm.user.UserData;
import org.alfresco.bm.user.UserDataService;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.OperationContext;
import org.apache.chemistry.opencmis.client.api.Repository;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.commons.io.IOUtils;

import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;

/**
 * 
 * @author sglover
 *
 */
public class DownloadFileHelper
{
    public static final String REPOSITORY_ID_USE_FIRST = "---";

    private final UserDataService userDataService;
    private OperationContext opContext;

    private String cmisBindingUrl;

    public DownloadFileHelper(UserDataService userDataService, String alfrescoHost, int alfrescoPort)
    {
        this.userDataService = userDataService;

        StringBuilder sb = new StringBuilder("http://");
        sb.append(alfrescoHost);
        sb.append(":");
        sb.append(alfrescoPort);
        sb.append("/alfresco/api/");
        sb.append("-default-");
        sb.append("/public/cmis/versions/1.1/browser");
        this.cmisBindingUrl = sb.toString();
    }

    private Session getCMISSession(String username, BindingType bindingType, String bindingUrl, String repositoryId)
    {
        UserData user = userDataService.findUserByUsername(username);
        if (user == null)
        {
            throw new RuntimeException("Unable to start CMIS session; user no longer exists: " + username);
        }
        String password = user.getPassword();

        // Build session parameters
        Map<String, String> parameters = new HashMap<String, String>();
        if (bindingType != null && bindingType.equals(BindingType.ATOMPUB))
        {
            parameters.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());
            parameters.put(SessionParameter.ATOMPUB_URL, bindingUrl);
        }
        else if (bindingType != null && bindingType.equals(BindingType.BROWSER))
        {
            parameters.put(SessionParameter.BINDING_TYPE, BindingType.BROWSER.value());
            parameters.put(SessionParameter.BROWSER_URL, bindingUrl);
        }
        else
        {
            throw new RuntimeException("Unsupported CMIS binding type: " + bindingType);
        }
        parameters.put(SessionParameter.USER, username);
        parameters.put(SessionParameter.PASSWORD, password);
        
        // First check if we need to choose a repository
        SessionFactory sessionFactory = SessionFactoryImpl.newInstance();
        List<Repository> repositories = sessionFactory.getRepositories(parameters);
        if (repositories.size() == 0)
        {
        	throw new RuntimeException("Unable to find any repositories at " + bindingUrl + " with user " + username);
        }
        if (repositoryId.equals(REPOSITORY_ID_USE_FIRST))
        {
            String repositoryIdFirst = repositories.get(0).getId();
            parameters.put(SessionParameter.REPOSITORY_ID, repositoryIdFirst);
        }
        else
        {
            parameters.put(SessionParameter.REPOSITORY_ID, repositoryId);
        }

        // Create the session
        Session session = SessionFactoryImpl.newInstance().createSession(parameters);
        if(opContext != null)
        {
        	session.setDefaultContext(opContext);
        }

        return session;
    }

    public DBObject download(String username, String path) throws IOException
    {
    	if(path.startsWith("/Company Home"))
    	{
    		path = path.substring("/Company Home".length());
    	}

    	Session session = getCMISSession(username, BindingType.BROWSER, cmisBindingUrl, "-default-");
    	Document document = (Document)session.getObjectByPath(path);
    	ContentStream contentStream = document.getContentStream();
    	SlurpingOutputStream out = new SlurpingOutputStream();
    	InputStream in = contentStream.getStream();

    	// would like to use try
    	try
    	{
    		IOUtils.copy(in, out);
    		long length = out.getLength();

        	DBObject dbObject = BasicDBObjectBuilder
        			.start("length", length)
        			.get();
        	return dbObject;
    	}
    	finally
    	{
    		if(in != null)
    		{
    			in.close();
    		}

    		if(out != null)
    		{
    			out.close();
    		}
    	}
    }

    private class SlurpingOutputStream extends OutputStream
	{
    	private long length;

		@Override
        public void write(int b) throws IOException
        {
			length++;
        }

		long getLength()
		{
			return length;
		}
	};
}
