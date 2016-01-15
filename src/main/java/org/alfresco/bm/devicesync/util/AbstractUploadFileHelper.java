package org.alfresco.bm.devicesync.util;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.alfresco.bm.devicesync.data.SubscriptionData;
import org.alfresco.bm.devicesync.data.UploadFileData;
import org.alfresco.bm.file.TestFileService;
import org.alfresco.bm.user.UserDataService;
import org.alfresco.repomirror.dao.NodesDataService;
import org.apache.chemistry.opencmis.commons.PropertyIds;

import com.mongodb.DBObject;

/**
 * 
 * @author sglover
 *
 */
public abstract class AbstractUploadFileHelper implements UploadFileHelper
{
    public static final String REPOSITORY_ID_USE_FIRST = "---";

    protected final UserDataService userDataService;
    protected final TestFileService testFileService;
    protected final NodesDataService nodesDataService;
    protected Set<String> siteRoles = new HashSet<>();

    protected Random random = new Random(System.currentTimeMillis());

    public AbstractUploadFileHelper(TestFileService testFileService,
            NodesDataService nodesDataService, UserDataService userDataService)
    {
        this.testFileService = testFileService;
        this.nodesDataService = nodesDataService;
        this.userDataService = userDataService;
    }

    protected String getCMISPath(String repoPath)
    {
        if (repoPath != null && repoPath.startsWith("/Company Home"))
        {
            int idx = "/Company Home".length();
            repoPath = repoPath.substring(idx);
        }

        return repoPath;
    }

    protected String normalizeNodeId(String nodeId)
    {
        int idx = nodeId.indexOf(";");
        if (idx != -1)
        {
            nodeId = nodeId.substring(0, idx);
        }
        return nodeId;
    }

    // protected UploadData update(String filenamePrefix,
    // SubscriptionData subscriptionData, UploadListener uploadListener) throws
    // IOException
    // {
    // String subscriptionId = subscriptionData.getSubscriptionId();
    // String siteId = subscriptionData.getSiteId();
    // String username = subscriptionData.getUsername();
    // String subscriptionPath = subscriptionData.getPath();
    //
    // // upload to existing file
    // FileData fileData =
    // nodesDataService.randomNodeUnderFolder(subscriptionPath, Arrays.asList(
    // "cm:document", "cm:content"));
    // // A quick double-check
    // if (fileData == null)
    // {
    // throw new
    // RuntimeException("Unable to upload file; no content found under " +
    // subscriptionPath);
    // }
    // String nodePath = getCMISPath(fileData.getNodePath());
    // String nodeId = fileData.getNodeId();
    //
    // // The file name
    // File file = testFileService.getFile();
    // if (file == null)
    // {
    // throw new RuntimeException("No test files exist for upload: " +
    // testFileService);
    // }
    // String filename = UUID.randomUUID().toString() + "-" + filenamePrefix +
    // "-" + file.getName();
    //
    // return doUpdate(username, nodeId, nodePath, file, filename,
    // uploadListener, subscriptionId, subscriptionPath, siteId);
    // }

    protected UploadData update(String filenamePrefix,
            UploadFileData uploadFileData, SubscriptionData subscriptionData,
            UploadListener uploadListener) throws IOException
    {
        // The file name
        File file = testFileService.getFile();
        if (file == null)
        {
            throw new RuntimeException("No test files exist for upload: "
                    + testFileService);
        }
        String filename = UUID.randomUUID().toString() + "-" + filenamePrefix
                + "-" + file.getName();

        return doUpdate(subscriptionData, uploadFileData, file, filename,
                uploadListener);
    }

    protected abstract UploadData doUpdate(SubscriptionData subscriptionData,
            UploadFileData uploadFileData, File file, String filename,
            UploadListener uploadListener) throws IOException;

    protected abstract UploadData doCreate(SubscriptionData subscriptionData,
            UploadFileData uploadFileData, File file,
            Map<String, String> newFileProps, String filename,
            UploadListener uploadListener) throws IOException;

    // protected UploadData create(String filenamePrefix, SubscriptionData
    // subscriptionData,
    // UploadListener uploadListener) throws IOException
    // {
    // String subscriptionId = subscriptionData.getSubscriptionId();
    // String siteId = subscriptionData.getSiteId();
    // String username = subscriptionData.getUsername();
    // String subscriptionPath = subscriptionData.getPath();
    //
    // // create new file
    // String parentPath =
    // getCMISPath(nodesDataService.randomFolderUnderFolder(subscriptionPath));
    // if(parentPath != null)
    // {
    // // The file name
    // File file = testFileService.getFile();
    // if (file == null)
    // {
    // throw new RuntimeException("No test files exist for upload: " +
    // testFileService);
    // }
    // String filename = UUID.randomUUID().toString() + "-" + filenamePrefix +
    // "-" + file.getName();
    //
    // Map<String, String> newFileProps = new HashMap<String, String>();
    // newFileProps.put(PropertyIds.OBJECT_TYPE_ID, "cmis:document");
    // newFileProps.put(PropertyIds.NAME, filename);
    //
    // return doCreate(subscriptionData, uploadData, file, newFileProps,
    // filename, uploadListener);
    // }
    // else
    // {
    // throw new RuntimeException("Unable to find a node under " +
    // subscriptionPath);
    // }
    // }

    protected UploadData create(String filenamePrefix,
            UploadFileData uploadFileData, SubscriptionData subscriptionData,
            UploadListener uploadListener) throws IOException
    {
        // The file name
        File file = testFileService.getFile();
        if (file == null)
        {
            throw new RuntimeException("No test files exist for upload: "
                    + testFileService);
        }
        String filename = UUID.randomUUID().toString() + "-" + filenamePrefix
                + "-" + file.getName();

        Map<String, String> newFileProps = new HashMap<String, String>();
        newFileProps.put(PropertyIds.OBJECT_TYPE_ID, "cmis:document");
        newFileProps.put(PropertyIds.NAME, filename);

        return doCreate(subscriptionData, uploadFileData, file, newFileProps,
                filename, uploadListener);
    }

    public DBObject doUpload(UploadFileData uploadFileData,
            SubscriptionData subscriptionData, UploadListener uploadListener,
            String filenamePrefix) throws IOException
    {
        try
        {
            String username = subscriptionData.getUsername();

            // A quick double-check
            if (username == null)
            {
                throw new RuntimeException(
                        "Unable to start CMIS session without a username.");
            }

            UploadData uploadData = null;

            String nodeType = uploadFileData.getNodeType();
            if (nodeType.equals("cm:content"))
            {
                // we do an update
                uploadData = update(filenamePrefix, uploadFileData,
                        subscriptionData, uploadListener);
            }
            else if (nodeType.equals("cm:folder"))
            {
                // we do a create
                uploadData = create(filenamePrefix, uploadFileData,
                        subscriptionData, uploadListener);
            }

            return uploadData.getData();
        }
        catch (Exception e)
        {
            uploadListener.onException(e);

            e.printStackTrace();

            String siteId = subscriptionData.getSiteId();
            String username = subscriptionData.getUsername();
            String subscriptionPath = subscriptionData.getPath();
            String subscriptionId = subscriptionData.getSubscriptionId();

            UploadData uploadData = new UploadData()
                    .setSubscriptionPath(subscriptionPath).setSiteId(siteId)
                    .setUsername(username).setSubscriptionId(subscriptionId);
            DBObject data = uploadData.getData();
            throw new UploadFileException(e, data);
        }
    }
}
