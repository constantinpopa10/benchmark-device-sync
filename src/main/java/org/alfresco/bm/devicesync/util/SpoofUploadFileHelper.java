package org.alfresco.bm.devicesync.util;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.alfresco.bm.devicesync.data.SubscriptionData;
import org.alfresco.bm.devicesync.data.UploadFileData;
import org.alfresco.bm.file.TestFileService;
import org.alfresco.bm.user.UserDataService;
import org.alfresco.events.types.NodeAddedEvent;
import org.alfresco.events.types.NodeContentPutEvent;
import org.alfresco.events.types.TransactionCommittedEvent;
import org.alfresco.repo.Client;
import org.alfresco.repomirror.dao.NodesDataService;
import org.gytheio.messaging.MessageProducer;

/**
 * 
 * @author sglover
 *
 */
public class SpoofUploadFileHelper extends AbstractUploadFileHelper
{
    public static final String REPOSITORY_ID_USE_FIRST = "---";

    private MessageProducer messageProducer;

    public SpoofUploadFileHelper(TestFileService testFileService,
            NodesDataService nodesDataService, UserDataService userDataService,
            MessageProducer messageProducer)
    {
        super(testFileService, nodesDataService, userDataService);
        this.messageProducer = messageProducer;
    }

    private TransactionCommittedEvent transactionCommitted(String txnId,
            String userId)
    {
        long seqNumber = -1;
        String networkId = "";
        Client client = null;
        TransactionCommittedEvent event = new TransactionCommittedEvent(
                seqNumber, txnId, networkId, System.currentTimeMillis(),
                userId, client);
        return event;
    }

    private NodeAddedEvent nodeAdded(UploadData uploadData, String txnId)
    {
        String name = uploadData.getName();
        long seqNumber = -1;
        List<String> paths = uploadData.getPaths();
        String networkId = "";
        String nodeId = "";
        String nodeType = uploadData.getNodeType();
        List<List<String>> parentNodeIds = uploadData.getParentNodeIds();
        String userId = uploadData.getUsername();
        Client client = null;
        Set<String> aspects = Collections.emptySet();
        Long modificationTime = System.currentTimeMillis();
        Map<String, Serializable> nodeProperties = Collections.emptyMap();
        NodeAddedEvent event = new NodeAddedEvent(seqNumber, name, txnId,
                System.currentTimeMillis(), networkId, uploadData.getSiteId(),
                nodeId, nodeType, paths, parentNodeIds, userId,
                modificationTime, client, aspects, nodeProperties);
        return event;
    }

    private NodeContentPutEvent contentPut(UploadData uploadData, String txnId)
    {
        String name = uploadData.getName();
        long seqNumber = -1;
        List<String> paths = uploadData.getPaths();
        String networkId = "";
        String nodeId = uploadData.getNodeId();
        String nodeType = uploadData.getNodeType();
        List<List<String>> parentNodeIds = uploadData.getParentNodeIds();
        String userId = uploadData.getUsername();
        Client client = null;
        Set<String> aspects = Collections.emptySet();
        Long modificationTime = System.currentTimeMillis();
        long size = uploadData.getFileLen();
        String mimeType = "text/plain";
        String encoding = "UTF-8";
        Map<String, Serializable> nodeProperties = Collections.emptyMap();
        NodeContentPutEvent event = new NodeContentPutEvent(seqNumber, name,
                txnId, System.currentTimeMillis(), networkId,
                uploadData.getSiteId(), nodeId, nodeType, paths, parentNodeIds,
                userId, modificationTime, size, mimeType, encoding, client,
                aspects, nodeProperties);
        return event;
    }

    @Override
    protected UploadData doUpdate(SubscriptionData subscriptionData,
            UploadFileData uploadFileData, File file, String filename,
            UploadListener uploadListener) throws IOException
    {
        String txnId = UUID.randomUUID().toString();

        UploadData uploadData = new UploadData().setFilename(filename)
                .setSubscriptionPath(subscriptionData.getPath())
                .setSiteId(subscriptionData.getSiteId())
                .setUsername(subscriptionData.getUsername())
                .setSubscriptionId(subscriptionData.getSubscriptionId())
                .setFileLen(100l).setParentId(null).setParentPath(null)
                .setNodeType("cm:content")
                .setNodeId(uploadFileData.getNodeId()).setName(filename)
                .setPaths(Arrays.asList(uploadFileData.getPath()))
                .setUploadType(UPLOAD_TYPE.UPDATE)
                .setParentNodeIds(uploadFileData.getParentNodeIds());

        uploadListener.beforeUpload();

        {
            NodeContentPutEvent event = contentPut(uploadData, txnId);
            messageProducer.send(event);
        }

        {
            TransactionCommittedEvent event = transactionCommitted(txnId,
                    subscriptionData.getUsername());
            messageProducer.send(event);
        }

        uploadListener.afterUpload();

        return uploadData;
    }

    @Override
    protected UploadData doCreate(SubscriptionData subscriptionData,
            UploadFileData uploadFileData, File file,
            Map<String, String> newFileProps, String filename,
            UploadListener uploadListener) throws IOException
    {
        String username = subscriptionData.getUsername();
        String parentPath = uploadFileData.getPath();
        String nodePath = parentPath + "/" + filename;
        String subscriptionId = subscriptionData.getSubscriptionId();
        String subscriptionPath = subscriptionData.getPath();
        String siteId = subscriptionData.getSiteId();

        String txnId = UUID.randomUUID().toString();

        // make up a nodeId (which won't exist in the repository)
        String nodeId = UUID.randomUUID().toString();
        UploadData uploadData = new UploadData().setFilename(filename)
                .setSubscriptionPath(subscriptionPath).setSiteId(siteId)
                .setUsername(username).setSubscriptionId(subscriptionId)
                .setFileLen(100l).setParentId(null).setParentPath(null)
                .setNodeId(nodeId).setNodeType("cm:content").setName(filename)
                .setPaths(Arrays.asList(nodePath))
                .setParentNodeIds(uploadFileData.getParentNodeIds())
                .setUploadType(UPLOAD_TYPE.UPDATE);

        uploadListener.beforeUpload();

        {
            NodeAddedEvent event = nodeAdded(uploadData, txnId);
            messageProducer.send(event);
        }

        {
            NodeContentPutEvent event = contentPut(uploadData, txnId);
            messageProducer.send(event);
        }

        {
            TransactionCommittedEvent event = transactionCommitted(txnId,
                    subscriptionData.getUsername());
            messageProducer.send(event);
        }

        uploadListener.afterUpload();

        return uploadData;
    }
}
