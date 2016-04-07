package org.alfresco.bm.devicesync.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.bm.user.UserData;
import org.alfresco.bm.user.UserDataService;
import org.apache.chemistry.opencmis.client.api.OperationContext;
import org.apache.chemistry.opencmis.client.api.Repository;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.runtime.OperationContextImpl;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;

/**
 * 
 * @author sglover
 *
 */
public class CMISSessionFactory
{
    private String cmisBindingUrl;
    private UserDataService userDataService;

    public CMISSessionFactory(UserDataService userDataService,
            String alfrescoScheme, String alfrescoHost, Integer alfrescoPort)
    {
        super();
        this.userDataService = userDataService;
        StringBuilder sb = new StringBuilder(alfrescoScheme);
        sb.append("://");
        sb.append(alfrescoHost);
        if(alfrescoPort != null)
        {
            sb.append(":");
            sb.append(alfrescoPort);
        }
        sb.append("/alfresco/api/");
        sb.append("-default-");
        sb.append("/public/cmis/versions/1.1/browser");
        this.cmisBindingUrl = sb.toString();
    }

    public Session getCMISSession(String username)
    {
        UserData user = userDataService.findUserByUsername(username);
        if (user == null)
        {
            throw new RuntimeException(
                    "Unable to start CMIS session; user no longer exists: "
                            + username);
        }
        String password = user.getPassword();

        // Build session parameters
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put(SessionParameter.BINDING_TYPE,
                BindingType.BROWSER.value());
        parameters.put(SessionParameter.BROWSER_URL, cmisBindingUrl);
        parameters.put(SessionParameter.USER, username);
        parameters.put(SessionParameter.PASSWORD, password);
        parameters.put(SessionParameter.COMPRESSION, "true");
        parameters.put(SessionParameter.BROWSER_SUCCINCT, "true");

        // First check if we need to choose a repository
        SessionFactory sessionFactory = SessionFactoryImpl.newInstance();
        List<Repository> repositories = sessionFactory
                .getRepositories(parameters);
        if (repositories.size() == 0)
        {
            throw new RuntimeException("Unable to find any repositories at "
                    + cmisBindingUrl + " with user " + username);
        }
        parameters.put(SessionParameter.REPOSITORY_ID, "-default-");

        // Create the session
        Session session = SessionFactoryImpl.newInstance().createSession(
                parameters);

        OperationContext opContext = new OperationContextImpl();
        opContext.setMaxItemsPerPage(100);
        opContext.setIncludePolicies(false);
        opContext.setLoadSecondaryTypeProperties(false);
        opContext.setIncludeRelationships(IncludeRelationships.NONE);
        opContext.setIncludePathSegments(false);
        opContext.setIncludeAllowableActions(false);
        opContext.setIncludeAcls(false);
        opContext.setCacheEnabled(true);
        opContext.setRenditionFilterString("");
        Set<String> propertyFilter = new HashSet<>();
        propertyFilter.add("cmis:name");
        propertyFilter.add("cmis:description");
        propertyFilter.add("cmis:title");
        propertyFilter.add("cmis:createdBy");
        propertyFilter.add("cmis:creationDate");
        propertyFilter.add("cmis:lastModifiedBy");
        propertyFilter.add("cmis:lastModificationDate");
        propertyFilter.add("cmis:contentStreamLength");
        propertyFilter.add("cmis:contentStreamMimeType");
        propertyFilter.add("cmis:contentStreamId");
        propertyFilter.add("cmis:isVersionSeriesCheckedOut");
        propertyFilter.add("cmis:versionSeriesCheckedOutBy");
        propertyFilter.add("cmis:parentId");
        propertyFilter.add("cmis:path");
        opContext.setFilter(propertyFilter);
        session.setDefaultContext(opContext);

        return session;
    }
}
