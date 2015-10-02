package org.alfresco.bm.devicesync.eventprocessor;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.alfresco.bm.devicesync.data.TreeWalkData;
import org.alfresco.bm.event.AbstractEventProcessor;
import org.alfresco.bm.event.Event;
import org.alfresco.bm.event.EventResult;
import org.alfresco.bm.user.UserData;
import org.alfresco.bm.user.UserDataService;
import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.ObjectType;
import org.apache.chemistry.opencmis.client.api.OperationContext;
import org.apache.chemistry.opencmis.client.api.Repository;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.runtime.OperationContextImpl;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;

import com.mongodb.DBObject;

/**
 * 
 * @author sglover
 *
 */
public class TreeWalk extends AbstractEventProcessor
{
    private String cmisBindingUrl;
    private UserDataService userDataService;

    public TreeWalk(UserDataService userDataService, String alfrescoHost, int alfrescoPort)
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

    private Session getCMISSession(String username)
    {
        UserData user = userDataService.findUserByUsername(username);
        if (user == null)
        {
            throw new RuntimeException("Unable to start CMIS session; user no longer exists: " + username);
        }
        String password = user.getPassword();

        // Build session parameters
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put(SessionParameter.BINDING_TYPE, BindingType.BROWSER.value());
        parameters.put(SessionParameter.BROWSER_URL, cmisBindingUrl);
        parameters.put(SessionParameter.USER, username);
        parameters.put(SessionParameter.PASSWORD, password);
        
        // First check if we need to choose a repository
        SessionFactory sessionFactory = SessionFactoryImpl.newInstance();
        List<Repository> repositories = sessionFactory.getRepositories(parameters);
        if (repositories.size() == 0)
        {
            throw new RuntimeException("Unable to find any repositories at " + cmisBindingUrl + " with user " + username);
        }
        parameters.put(SessionParameter.REPOSITORY_ID, "-default-");

        // Create the session
        Session session = SessionFactoryImpl.newInstance().createSession(parameters);

        OperationContext opContext = new OperationContextImpl();
        opContext.setMaxItemsPerPage(100);
        opContext.setIncludePolicies(false);
        opContext.setLoadSecondaryTypeProperties(false);
        opContext.setIncludeRelationships(IncludeRelationships.NONE);
        opContext.setIncludePathSegments(false);
        opContext.setIncludeAllowableActions(false);
        opContext.setIncludeAcls(false);
        opContext.setCacheEnabled(true);
        session.setDefaultContext(opContext);

        return session;
    }

    private void treeWalk(Folder folder, TreeWalkData treeWalkData) throws IOException
    {
        treeWalkData.incrementMaxFolderDepth();

        ItemIterable<CmisObject> children = folder.getChildren();

        int numFolders = 0;
        int numDocuments = 0;

        // We have to iterate using paging
        List<Folder> childFolders = new LinkedList<>();
        long skip = 0L;
        ItemIterable<CmisObject> pageOfChildren = children.skipTo(skip);
        while (pageOfChildren.getPageNumItems() > 0L)
        {
            for (CmisObject child : pageOfChildren)
            {
                skip++;

                ObjectType childType = child.getType();
                String nodeType = childType.getId();
                if(nodeType.equals("cmis:folder"))
                {
                    numFolders++;
                    Folder childFolder = (Folder)child;
                    childFolders.add(childFolder);
                }
                else if(nodeType.equals("cmis:document"))
                {
                    Document document = (Document)child;
                    ContentStream stream = document.getContentStream();
                    InputStream in = stream.getStream();
                    byte[] buf = new byte[8092];
                    int contentSize = 0;
                    int c = -1;
                    do
                    {
                        c = in.read(buf);
                        if(c != -1)
                        {
                            contentSize += c;
                        }
                    }
                    while(c != -1);

                    treeWalkData.updateMaxContentSize(contentSize);
                    treeWalkData.updateMinContentSize(contentSize);
                    treeWalkData.incrementTotalContentSize(contentSize);

                    numDocuments++;
                }
            }
            // Get the next page of children
            pageOfChildren = children.skipTo(skip);
        }

        treeWalkData.incrementNumDocuments(numDocuments);
        treeWalkData.incrementNumFolders(numFolders);

        for(Folder childFolder : childFolders)
        {
            treeWalk(childFolder, treeWalkData);
        }
    }

    private void treeWalk(Session session, TreeWalkData treeWalkData) throws IOException
    {
        String siteId = treeWalkData.getSiteId();
        StringBuilder sb = new StringBuilder("/Sites/");
        sb.append(siteId);
        sb.append("/documentLibrary");
        String path = sb.toString();
        Folder folder = (Folder)session.getObjectByPath(path);
        treeWalk(folder, treeWalkData);
    }

    @Override
    protected EventResult processEvent(Event event) throws Exception
    {
        super.suspendTimer();

        DBObject dbObject = (DBObject)event.getData();
        TreeWalkData treeWalkData = TreeWalkData.fromDBObject(dbObject);
        String username = treeWalkData.getUsername();
        Session session = getCMISSession(username);

        try
        {
            super.resumeTimer();

            treeWalk(session, treeWalkData);

            super.suspendTimer();

            List<Event> nextEvents = new LinkedList<Event>();

            return new EventResult(treeWalkData.toDBObject(), nextEvents, true);
        }
        catch (Exception e)
        {
            logger.error("Exception occurred during event processing", e);
            throw e;
        }
    }
}
