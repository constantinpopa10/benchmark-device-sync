package org.alfresco.bm.devicesync.util;

import java.util.List;

import org.alfresco.bm.devicesync.util.UploadFileHelper.UPLOAD_TYPE;

import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;

/**
 * 
 * @author sglover
 *
 */
public class UploadData
{
    private UPLOAD_TYPE uploadType;
    private String subscriptionPath;
    private String nodeId = null;
    private String nodeType = null;
    private String name = null;
    private List<String> paths = null;
    private Long fileLen = null;
    private String parentId = null;
    private String parentPath = null;
    private String filename = null;
    private String siteId;
    private String subscriptionId;
    private String username;
    private List<List<String>> parentNodeIds;

    public UploadData()
    {
    }

    public String getNodeId()
    {
        return nodeId;
    }

    public UploadData setNodeId(String nodeId)
    {
        this.nodeId = nodeId;
        return this;
    }

    public String getNodeType()
    {
        return nodeType;
    }

    public UploadData setNodeType(String nodeType)
    {
        this.nodeType = nodeType;
        return this;
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

    public DBObject getData()
    {
        DBObject data = BasicDBObjectBuilder
                .start("subscriptionPath", subscriptionPath)
                .add("parentId", parentId)
                .add("parentPath", parentPath)
                .add("siteId", siteId)
                .add("filename", filename)
                .add("fileLen", fileLen)
                .add("username", username)
                .add("subscriptionId", subscriptionId)
                .add("id", nodeId)
                .add("name", name)
                .add("paths", paths)
                .add("uploadType",
                        (uploadType != null ? uploadType.toString() : null))
                .add("parentNodeIds", parentNodeIds).get();
        return data;
    }

    public UPLOAD_TYPE getUploadType()
    {
        return uploadType;
    }

    public String getSubscriptionPath()
    {
        return subscriptionPath;
    }

    public String getName()
    {
        return name;
    }

    public List<String> getPaths()
    {
        return paths;
    }

    public Long getFileLen()
    {
        return fileLen;
    }

    public String getParentId()
    {
        return parentId;
    }

    public String getParentPath()
    {
        return parentPath;
    }

    public String getFilename()
    {
        return filename;
    }

    public String getSiteId()
    {
        return siteId;
    }

    public String getSubscriptionId()
    {
        return subscriptionId;
    }

    public String getUsername()
    {
        return username;
    }

    public List<List<String>> getParentNodeIds()
    {
        return parentNodeIds;
    }

    public UploadData setParentNodeIds(List<List<String>> parentNodeIds)
    {
        this.parentNodeIds = parentNodeIds;
        return this;
    }
}