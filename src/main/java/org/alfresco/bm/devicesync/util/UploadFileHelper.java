package org.alfresco.bm.devicesync.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.alfresco.bm.devicesync.dao.SubscriptionsService;
import org.alfresco.bm.devicesync.data.SubscriptionData;
import org.alfresco.bm.devicesync.data.UploadFileData;
import org.alfresco.bm.file.TestFileService;
import org.alfresco.bm.user.UserData;
import org.alfresco.bm.user.UserDataService;
import org.alfresco.repomirror.dao.NodesDataService;
import org.alfresco.repomirror.data.FileData;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.OperationContext;
import org.apache.chemistry.opencmis.client.api.Repository;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;

import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;

/**
 * 
 * @author sglover
 *
 */
public class UploadFileHelper
{
    public static final String REPOSITORY_ID_USE_FIRST = "---";

    private final UserDataService userDataService;
    private OperationContext opContext;
    private final TestFileService testFileService;
    private final NodesDataService nodesDataService;
	private Set<String> siteRoles = new HashSet<>();

    private String cmisBindingUrl;

    private Random random = new Random(System.currentTimeMillis());

    public UploadFileHelper(TestFileService testFileService, NodesDataService nodesDataService,
    		SubscriptionsService subscriptionsService, UserDataService userDataService,
    		String alfrescoHost, int alfrescoPort)
    {
        this.testFileService = testFileService;
        this.nodesDataService = nodesDataService;
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

    private String getCMISPath(String repoPath)
    {
    	if(repoPath != null && repoPath.startsWith("/Company Home"))
    	{
    		int idx = "/Company Home".length(); 
    		repoPath = repoPath.substring(idx);
    	}

    	return repoPath;
    }

	private String normalizeNodeId(String nodeId)
	{
		int idx = nodeId.indexOf(";");
		if(idx != -1)
		{
			nodeId = nodeId.substring(0, idx);
		}
		return nodeId;
	}

	private UploadData update(String filenamePrefix,
			SubscriptionData subscriptionData, UploadListener uploadListener) throws IOException
	{
		String subscriptionId = subscriptionData.getSubscriptionId();
        String siteId = subscriptionData.getSiteId();
        String username = subscriptionData.getUsername();
        String subscriptionPath = subscriptionData.getPath();

    	// upload to existing file
        FileData fileData = nodesDataService.randomNodeUnderFolder(subscriptionPath, Arrays.asList(
        		"cm:document", "cm:content"));
        // A quick double-check
        if (fileData == null)
        {
        	throw new RuntimeException("Unable to upload file; no content found under " + subscriptionPath);
        }
        String nodePath = getCMISPath(fileData.getNodePath());

        // The file name
        File file = testFileService.getFile();
        if (file == null)
        {
        	throw new RuntimeException("No test files exist for upload: " + testFileService);
        }
        String filename = UUID.randomUUID().toString() + "-" + filenamePrefix + "-" + file.getName();

        Session session = getCMISSession(username, BindingType.BROWSER, cmisBindingUrl, "-default-");
        Document document = (Document)session.getObjectByPath(nodePath);
        String existingDocId = document.getId();
        List<Folder> parents = document.getParents();
        Folder parent = parents.get(0);
        String parentId = normalizeNodeId(parent.getId());
        String parentPath = parent.getPath();

        // Open up a stream to the file
        InputStream is = new BufferedInputStream(new FileInputStream(file));
        long fileLen = file.length();
        try
        {
            ContentStream cs = new ContentStreamImpl(filename, BigInteger.valueOf(fileLen), "application/octet-stream", is);

            // Make sure we only time the document creation
            uploadListener.beforeUpload();
            Document newFile = document.setContentStream(cs, true);
            uploadListener.afterUpload();

            String id = normalizeNodeId(newFile.getId());
            String name = newFile.getName();
            List<String> paths = newFile.getPaths();

            UploadData uploadData = new UploadData()
        	.setFilename(filename)
        	.setSubscriptionPath(subscriptionPath)
        	.setSiteId(siteId)
        	.setUsername(username)
        	.setSubscriptionId(subscriptionId)
        	.setFileLen(fileLen)
        	.setParentId(parentId)
        	.setParentPath(parentPath)
        	.setId(id)
        	.setName(name)
        	.setPaths(paths)
        	.setExistingDocId(existingDocId)
        	.setUploadType(UPLOAD_TYPE.UPDATE);
            return uploadData;
        }
        finally
        {
            if (is != null)
            {
                try { is.close(); } catch (IOException e) {}
            }
        }
	}

	private UploadData create(String filenamePrefix, SubscriptionData subscriptionData,
			UploadListener uploadListener) throws IOException
	{
		String subscriptionId = subscriptionData.getSubscriptionId();
        String siteId = subscriptionData.getSiteId();
        String username = subscriptionData.getUsername();
        String subscriptionPath = subscriptionData.getPath();

    	// create new file
        String parentPath = getCMISPath(nodesDataService.randomFolderUnderFolder(subscriptionPath));
        if(parentPath != null)
        {
	        // The file name
	        File file = testFileService.getFile();
	        if (file == null)
	        {
	        	throw new RuntimeException("No test files exist for upload: " + testFileService);
	        }
	        String filename = UUID.randomUUID().toString() + "-" + filenamePrefix + "-" + file.getName();
	
	        Map<String, String> newFileProps = new HashMap<String, String>();
	        newFileProps.put(PropertyIds.OBJECT_TYPE_ID, "cmis:document");
	        newFileProps.put(PropertyIds.NAME, filename);
	
	        Session session = getCMISSession(username, BindingType.BROWSER, cmisBindingUrl, "-default-");
	        Folder folder = (Folder)session.getObjectByPath(parentPath);
	        String parentId = normalizeNodeId(folder.getId());
	
	        // Open up a stream to the file
	        InputStream is = new BufferedInputStream(new FileInputStream(file));
	        long fileLen = file.length();
	        try
	        {
	            ContentStream cs = new ContentStreamImpl(filename, BigInteger.valueOf(fileLen), "application/octet-stream", is);
	
	            // Make sure we only time the document creation
	            uploadListener.beforeUpload();
	            Document newFile = folder.createDocument(newFileProps, cs, VersioningState.MAJOR);
	            uploadListener.afterUpload();

	            String nodeId = normalizeNodeId(newFile.getId());
	            String name = newFile.getName();
	            List<String> paths = newFile.getPaths();
	            String nodePath = (paths != null && paths.size() > 0 ? paths.get(0) : null);

	            nodesDataService.addNode(siteId, username, nodeId, nodePath, name, "cm:document");

	            UploadData uploadData = new UploadData()
	            	.setFilename(filename)
	            	.setSubscriptionPath(subscriptionPath)
	            	.setSiteId(siteId)
	            	.setUsername(username)
	            	.setSubscriptionId(subscriptionId)
	            	.setFileLen(fileLen)
	            	.setParentId(parentId)
	            	.setParentPath(parentPath)
	            	.setId(nodeId)
	            	.setName(name)
	            	.setPaths(paths)
	            	.setUploadType(UPLOAD_TYPE.CREATE);
	            return uploadData;
	        }
	        finally
	        {
	            if (is != null)
	            {
	                try { is.close(); } catch (IOException e) {}
	            }
	        }
        }
        else
        {
        	throw new RuntimeException("Unable to find a node under " + subscriptionPath);
        }
	}

	public static enum UPLOAD_TYPE { CREATE, UPDATE };

	private static class UploadData
	{
		private UPLOAD_TYPE uploadType;
		private String subscriptionPath;
		private String id = null;
		private String name = null;
        private List<String> paths = null;
        private Long fileLen = null;
        private String parentId = null;
        private String parentPath = null;
        private String filename = null;
        private String existingDocId = null;
        private String siteId;
        private String subscriptionId;
        private String username;

		public UploadData()
        {
        }

		public UploadData setUploadType(UPLOAD_TYPE uploadType)
		{
			this.uploadType = uploadType;
			return this;
		}

		public UploadData setFileLen(Long fileLen)
		{
			this.fileLen = fileLen;
			return this;
		}


		public UploadData setSubscriptionPath(String subscriptionPath)
		{
			this.subscriptionPath = subscriptionPath;
			return this;
		}


		public UploadData setId(String id)
		{
			this.id = id;
			return this;
		}


		public UploadData setName(String name)
		{
			this.name = name;
			return this;
		}


		public UploadData setPaths(List<String> paths)
		{
			this.paths = paths;
			return this;
		}


		public UploadData setParentId(String parentId)
		{
			this.parentId = parentId;
			return this;
		}


		public UploadData setParentPath(String parentPath)
		{
			this.parentPath = parentPath;
			return this;
		}


		public UploadData setFilename(String filename)
		{
			this.filename = filename;
			return this;
		}


		public UploadData setExistingDocId(String existingDocId)
		{
			this.existingDocId = existingDocId;
			return this;
		}


		public UploadData setSiteId(String siteId)
		{
			this.siteId = siteId;
			return this;
		}


		public UploadData setSubscriptionId(String subscriptionId)
		{
			this.subscriptionId = subscriptionId;
			return this;
		}


		public UploadData setUsername(String username)
		{
			this.username = username;
			return this;
		}

		DBObject getData()
		{
	        DBObject data = BasicDBObjectBuilder
	        		.start("subscriptionPath", subscriptionPath)
	        		.add("parentId", parentId)
	        		.add("parentPath", parentPath)
	        		.add("siteId", siteId)
	        		.add("filename", filename)
	        		.add("fileLen", fileLen)
	        		.add("existingDocId", existingDocId)
	        		.add("username", username)
	        		.add("subscriptionId", subscriptionId)
	        		.add("id", id)
	        		.add("name", name)
	        		.add("paths", paths)
	        		.add("uploadType", (uploadType != null ? uploadType.toString() : null))
	        		.get();
	        return data;
		}
	}

	public DBObject doUpload(UploadFileData uploadFileData, SubscriptionData subscriptionData,
			UploadListener uploadListener, String filenamePrefix) throws IOException
	{
        try
        {
	        String username = subscriptionData.getUsername();
	        int numChildren = (uploadFileData.getNumChildren() != null ? uploadFileData.getNumChildren() : -1);
	        int numChildFolders = (uploadFileData.getNumChildFolders() != null ? uploadFileData.getNumChildFolders() : -1);
	        int numContentChildren = (numChildren != -1 && numChildFolders != -1 ? numChildren - numChildFolders : -1);

	        // A quick double-check
	        if (username == null)
	        {
	            throw new RuntimeException("Unable to start CMIS session without a username.");
	        }

        	String siteRole = uploadFileData.getSiteRole();

            UploadData uploadData = null;
        	if(numContentChildren < 1 || !siteRoles.contains(siteRole))
        	{
        		uploadData = create(filenamePrefix, subscriptionData, uploadListener);
        	}
        	else
        	{
		        int rand = random.nextInt(100);
		        if(rand < 50)
		        {
		        	uploadData = update(filenamePrefix, subscriptionData, uploadListener);
		        }
		        else
		        {
		        	uploadData = create(filenamePrefix, subscriptionData, uploadListener);
		        }
        	}

            return uploadData.getData();
        }
        catch(Exception e)
        {
            uploadListener.onException(e);

            e.printStackTrace();

	        String siteId = subscriptionData.getSiteId();
	        String username = subscriptionData.getUsername();
	        String subscriptionPath = subscriptionData.getPath();
	        String subscriptionId = subscriptionData.getSubscriptionId();

            UploadData uploadData = new UploadData()
            	.setSubscriptionPath(subscriptionPath)
            	.setSiteId(siteId)
            	.setUsername(username)
            	.setSubscriptionId(subscriptionId);
	        DBObject data = uploadData.getData();
	        throw new UploadFileException(e, data);
        }
	}

	public interface UploadListener
	{
		void beforeUpload();
		void afterUpload();
		void onException(Exception e);
	}
}
