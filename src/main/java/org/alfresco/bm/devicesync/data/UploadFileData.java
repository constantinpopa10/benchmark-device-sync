package org.alfresco.bm.devicesync.data;

import java.util.List;

import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;

/**
 * 
 * @author sglover
 *
 */
public class UploadFileData
{
    private String username;
    private String subscriberId;
    private String subscriptionId;
    private String siteId;
    private String siteRole;
    private String path;
    private Integer numChildren;
    private Integer numChildFolders;
    private String nodeType;
    private String nodeId;
    private List<List<String>> parentNodeIds;

    public UploadFileData(String username, String subscriberId,
            String subscriptionId, String siteId, String siteRole, String path,
            Integer numChildren, Integer numChildFolders, String nodeId,
            String nodeType, List<List<String>> parentNodeIds)
    {
        super();
        this.username = username;
        this.subscriberId = subscriberId;
        this.subscriptionId = subscriptionId;
        this.siteId = siteId;
        this.siteRole = siteRole;
        this.path = path;
        this.numChildren = numChildren;
        this.numChildFolders = numChildFolders;
        this.nodeId = nodeId;
        this.nodeType = nodeType;
        this.parentNodeIds = parentNodeIds;
    }

    public String getNodeId()
    {
        return nodeId;
    }

    public String getUsername()
    {
        return username;
    }

    public String getSubscriberId()
    {
        return subscriberId;
    }

    public String getSubscriptionId()
    {
        return subscriptionId;
    }

    public String getSiteId()
    {
        return siteId;
    }

    public String getSiteRole()
    {
        return siteRole;
    }

    public String getPath()
    {
        return path;
    }

    public Integer getNumChildren()
    {
        return numChildren;
    }

    public Integer getNumChildFolders()
    {
        return numChildFolders;
    }

    public List<List<String>> getParentNodeIds()
    {
        return parentNodeIds;
    }

    public DBObject toDBObject()
    {
        BasicDBObjectBuilder builder = BasicDBObjectBuilder
                .start("username", username).add("subscriberId", subscriberId)
                .add("subscriptionId", subscriptionId).add("siteId", siteId)
                .add("nodeId", nodeId).add("nodeType", nodeType)
                .add("siteRole", siteRole).add("path", path)
                .add("numChildren", numChildren)
                .add("numChildFolders", numChildFolders)
                .add("parentNodeIds", parentNodeIds);
        DBObject dbObject = builder.get();
        return dbObject;
    }

    public String getNodeType()
    {
        return nodeType;
    }

    @SuppressWarnings("unchecked")
    public static UploadFileData fromDBObject(DBObject dbObject)
    {
        String username = (String) dbObject.get("username");
        String subscriberId = (String) dbObject.get("subscriberId");
        String subscriptionId = (String) dbObject.get("subscriptionId");
        String siteId = (String) dbObject.get("siteId");
        String siteRole = (String) dbObject.get("siteRole");
        String path = (String) dbObject.get("path");
        String nodeId = (String) dbObject.get("nodeId");
        String nodeType = (String) dbObject.get("nodeType");
        Integer numChildren = (Integer) dbObject.get("numChildren");
        Integer numChildFolders = (Integer) dbObject.get("numChildFolders");
        List<List<String>> parentNodeIds = (List<List<String>>) dbObject
                .get("parentNodeIds");
        UploadFileData uploadFileData = new UploadFileData(username,
                subscriberId, subscriptionId, siteId, siteRole, path,
                numChildren, numChildFolders, nodeId, nodeType, parentNodeIds);
        return uploadFileData;
    }

    @Override
    public String toString()
    {
        return "UploadFileData [username=" + username + ", subscriberId="
                + subscriberId + ", subscriptionId=" + subscriptionId
                + ", siteId=" + siteId + ", siteRole=" + siteRole + ", path="
                + path + ", numChildren=" + numChildren + ", numChildFolders="
                + numChildFolders + ", nodeType=" + nodeType + ", nodeId="
                + nodeId + ", parentNodeIds=" + parentNodeIds + "]";
    }
}
