package org.alfresco.bm.devicesync.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.bm.devicesync.data.SubscriptionData;
import org.alfresco.bm.devicesync.data.UploadFileData;
import org.alfresco.bm.file.TestFileService;
import org.alfresco.bm.user.UserData;
import org.alfresco.bm.user.UserDataService;
import org.alfresco.repomirror.dao.NodesDataService;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.OperationContext;
import org.apache.chemistry.opencmis.client.api.Repository;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;

/**
 * 
 * @author sglover
 *
 */
public class CMISUploadFileHelper extends AbstractUploadFileHelper
{
    public static final String REPOSITORY_ID_USE_FIRST = "---";

    private final int companyHomeLength = "/Company Home".length();

    private OperationContext opContext;

    private String cmisBindingUrl;

    public CMISUploadFileHelper(TestFileService testFileService,
            NodesDataService nodesDataService, UserDataService userDataService,
            String alfrescoHost, int alfrescoPort)
    {
        super(testFileService, nodesDataService, userDataService);
        StringBuilder sb = new StringBuilder("http://");
        sb.append(alfrescoHost);
        sb.append(":");
        sb.append(alfrescoPort);
        sb.append("/alfresco/api/");
        sb.append("-default-");
        sb.append("/public/cmis/versions/1.1/browser");
        this.cmisBindingUrl = sb.toString();
    }

    private Session getCMISSession(String username, BindingType bindingType,
            String bindingUrl, String repositoryId)
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
        if (bindingType != null && bindingType.equals(BindingType.ATOMPUB))
        {
            parameters.put(SessionParameter.BINDING_TYPE,
                    BindingType.ATOMPUB.value());
            parameters.put(SessionParameter.ATOMPUB_URL, bindingUrl);
        }
        else if (bindingType != null && bindingType.equals(BindingType.BROWSER))
        {
            parameters.put(SessionParameter.BINDING_TYPE,
                    BindingType.BROWSER.value());
            parameters.put(SessionParameter.BROWSER_URL, bindingUrl);
        }
        else
        {
            throw new RuntimeException("Unsupported CMIS binding type: "
                    + bindingType);
        }
        parameters.put(SessionParameter.USER, username);
        parameters.put(SessionParameter.PASSWORD, password);

        // First check if we need to choose a repository
        SessionFactory sessionFactory = SessionFactoryImpl.newInstance();
        List<Repository> repositories = sessionFactory
                .getRepositories(parameters);
        if (repositories.size() == 0)
        {
            throw new RuntimeException("Unable to find any repositories at "
                    + bindingUrl + " with user " + username);
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
        Session session = SessionFactoryImpl.newInstance().createSession(
                parameters);
        if (opContext != null)
        {
            session.setDefaultContext(opContext);
        }

        return session;
    }

    private String cmisPath(String path)
    {
        String cmisPath = null;

        if(path.startsWith("/Company Home"))
        {
            cmisPath = path.substring(companyHomeLength);
        }
        else
        {
            cmisPath = path;
        }

        return cmisPath;
    }

    @Override
    protected UploadData doUpdate(SubscriptionData subscriptionData,
            UploadFileData uploadFileData, File file, String filename,
            UploadListener uploadListener) throws IOException
    {
        String username = subscriptionData.getUsername();
        String nodePath = uploadFileData.getPath();
        String siteId = subscriptionData.getSiteId();
        String subscriptionId = subscriptionData.getSubscriptionId();
        String subscriptionPath = subscriptionData.getPath();
        List<List<String>> parentNodeIds = uploadFileData.getParentNodeIds();

        Session session = getCMISSession(username, BindingType.BROWSER,
                cmisBindingUrl, "-default-");
        Document document = (Document) session.getObjectByPath(nodePath);
        List<Folder> parents = document.getParents();
        Folder parent = parents.get(0);
        String parentId = normalizeNodeId(parent.getId());
        String parentPath = parent.getPath();

        // Open up a stream to the file
        InputStream is = new BufferedInputStream(new FileInputStream(file));
        long fileLen = file.length();
        try
        {
            ContentStream cs = new ContentStreamImpl(filename,
                    BigInteger.valueOf(fileLen), "application/octet-stream", is);

            // Make sure we only time the document creation
            uploadListener.beforeUpload();
            Document newFile = document.setContentStream(cs, true);
            uploadListener.afterUpload();

            String id = normalizeNodeId(newFile.getId());
            String name = newFile.getName();
            List<String> paths = newFile.getPaths();

            UploadData uploadData = new UploadData().setFilename(filename)
                    .setSubscriptionPath(subscriptionPath).setSiteId(siteId)
                    .setUsername(username).setSubscriptionId(subscriptionId)
                    .setFileLen(fileLen).setParentId(parentId)
                    .setParentPath(parentPath).setNodeId(id).setName(name)
                    .setPaths(paths).setParentNodeIds(parentNodeIds)
                    .setUploadType(UPLOAD_TYPE.UPDATE);
            return uploadData;
        }
        finally
        {
            if (is != null)
            {
                try
                {
                    is.close();
                }
                catch (IOException e)
                {
                }
            }
        }
    }

    @Override
    protected UploadData doCreate(SubscriptionData subscriptionData,
            UploadFileData uploadFileData, File file,
            Map<String, String> newFileProps, String filename,
            UploadListener uploadListener) throws IOException
    {
        String username = subscriptionData.getUsername();
        String parentPath = cmisPath(uploadFileData.getPath());
        String siteId = subscriptionData.getSiteId();
        String subscriptionId = subscriptionData.getSubscriptionId();
        String subscriptionPath = subscriptionData.getPath();
        List<List<String>> parentNodeIds = uploadFileData.getParentNodeIds();

        Session session = getCMISSession(username, BindingType.BROWSER,
                cmisBindingUrl, "-default-");
        Folder folder = (Folder) session.getObjectByPath(parentPath);
        String parentId = normalizeNodeId(folder.getId());

        // Open up a stream to the file
        InputStream is = new BufferedInputStream(new FileInputStream(file));
        long fileLen = file.length();
        try
        {
            ContentStream cs = new ContentStreamImpl(filename,
                    BigInteger.valueOf(fileLen), "application/octet-stream", is);

            // Make sure we only time the document creation
            uploadListener.beforeUpload();
            Document newFile = folder.createDocument(newFileProps, cs,
                    VersioningState.MAJOR);
            uploadListener.afterUpload();

            String nodeId = normalizeNodeId(newFile.getId());
            String name = newFile.getName();
            List<String> paths = newFile.getPaths();
            String nodePath = (paths != null && paths.size() > 0 ? paths.get(0)
                    : null);
            if(parentNodeIds != null)
            {
                // add the nodeId of the new node to the parent lists of the folder
                // parent
                for (List<String> l : parentNodeIds)
                {
                    l.add(0, nodeId);
                }
            }

            nodesDataService.addNode(siteId, username, nodeId, nodePath, name,
                    "cm:document", parentNodeIds);

            UploadData uploadData = new UploadData().setFilename(filename)
                    .setSubscriptionPath(subscriptionPath).setSiteId(siteId)
                    .setUsername(username).setSubscriptionId(subscriptionId)
                    .setFileLen(fileLen).setParentId(parentId)
                    .setParentPath(parentPath).setNodeId(nodeId).setName(name)
                    .setPaths(paths).setUploadType(UPLOAD_TYPE.CREATE);
            return uploadData;
        }
        finally
        {
            if (is != null)
            {
                try
                {
                    is.close();
                }
                catch (IOException e)
                {
                }
            }
        }
    }
}
