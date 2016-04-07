package org.alfresco.bm.devicesync.data;

import java.io.Serializable;

import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;

/**
 * 
 * @author sglover
 *
 */
public class TreeWalkData implements Serializable
{
    public static String FIELD_NUM_FOLDERS = "numFolders";
    public static String FIELD_NUM_DOCUMENTS = "numDocuments";
    public static String FIELD_USERNAME = "username";
    public static String FIELD_SITE_ID = "siteId";
    public static String FIELD_MAX_CONTENT_SIZE = "maxContentSize";
    public static String FIELD_MIN_CONTENT_SIZE = "minContentSize";
    public static String FIELD_TOTAL_CONTENT_SIZE = "totalContentSize";
    public static String FIELD_MAX_FOLDER_DEPTH = "maxFolderDepth";
    public static String FIELD_OBJECT_ID = "objectId";;
    public static String FIELD_SITE_PATH = "sitePath";

    private static final long serialVersionUID = 946578159221599841L;

    protected int numFolders;
    protected int numDocuments;
    protected String username;
    protected String siteId;
    protected String sitePath;
    protected String objectId;
    protected int totalContentSize = 0;
    protected int maxContentSize = 0;
    protected int minContentSize = -1;
    protected int maxFolderDepth = 0;

    public TreeWalkData()
    {
    }

    public TreeWalkData(int numFolders, int numDocuments)
    {
        this.numFolders = numFolders;
        this.numDocuments = numDocuments;
    }

    public TreeWalkData(int numFolders, int numDocuments, String username, String objectId)
    {
        this.numFolders = numFolders;
        this.numDocuments = numDocuments;
        this.username = username;
        this.objectId = objectId;
    }

    public TreeWalkData(int numFolders, int numDocuments, String username,
            String siteId, String sitePath)
    {
        this(numFolders, numDocuments);
        this.username = username;
        this.siteId = siteId;
        this.sitePath = sitePath;
    }

    public TreeWalkData(int numFolders, int numDocuments, String username,
            String siteId, int totalContentSize, int maxContentSize,
            int minContentSize, int maxFolderDepth, String sitePath, String objectId)
    {
        this(numFolders, numDocuments);
        this.username = username;
        this.siteId = siteId;
        this.totalContentSize = totalContentSize;
        this.maxContentSize = maxContentSize;
        this.minContentSize = minContentSize;
        this.maxFolderDepth = maxFolderDepth;
        this.sitePath = sitePath;
        this.objectId = objectId;
    }

    public String getObjectId()
    {
        return objectId;
    }

    public int getMaxFolderDepth()
    {
        return maxFolderDepth;
    }

    public int getTotalContentSize()
    {
        return totalContentSize;
    }

    public int getMaxContentSize()
    {
        return maxContentSize;
    }

    public int getMinContentSize()
    {
        return minContentSize;
    }

    public int getNumFolders()
    {
        return numFolders;
    }

    public int getNumDocuments()
    {
        return numDocuments;
    }

    public void setTotalContentSize(int totalContentSize)
    {
        this.totalContentSize = totalContentSize;
    }

    public void setMaxContentSize(int maxContentSize)
    {
        this.maxContentSize = maxContentSize;
    }

    public void setMinContentSize(int minContentSize)
    {
        this.minContentSize = minContentSize;
    }

    public void setMaxFolderDepth(int maxFolderDepth)
    {
        this.maxFolderDepth = maxFolderDepth;
    }

    public void setNumFolders(int numFolders)
    {
        this.numFolders = numFolders;
    }

    public void setNumDocuments(int numDocuments)
    {
        this.numDocuments = numDocuments;
    }

    public void incrementNumFolders(int numFolders)
    {
        this.numFolders += numFolders;
    }

    public void incrementTotalContentSize(int size)
    {
        this.totalContentSize += size;
    }

    public void incrementMaxFolderDepth()
    {
        this.maxFolderDepth++;
    }

    public void updateMaxContentSize(int size)
    {
        if (size > maxContentSize)
        {
            maxContentSize = size;
        }
    }

    public void updateMinContentSize(int size)
    {
        if (minContentSize == -1)
        {
            minContentSize = Integer.MAX_VALUE;
        }
        else if (size < minContentSize)
        {
            minContentSize = size;
        }
    }

    public void incrementNumDocuments(int numDocuments)
    {
        this.numDocuments += numDocuments;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public String getSiteId()
    {
        return siteId;
    }

    public void setSiteId(String siteId)
    {
        this.siteId = siteId;
    }

    public String getSitePath()
    {
        return sitePath;
    }

    public void setSitePath(String sitePath)
    {
        this.sitePath = sitePath;
    }

    public void setObjectId(String objectId)
    {
        this.objectId = objectId;
    }

    public String getPath()
    {
        String siteId = getSiteId();
        StringBuilder sb = new StringBuilder("/Sites/");
        sb.append(siteId);
        sb.append("/documentLibrary");
        if(sitePath != null)
        {
            sb.append("/");
            sb.append(sitePath);
        }
        String path = sb.toString();
        return path;
    }

    public DBObject toDBObject()
    {
        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();
        toDBObject(builder);
        return builder.get();
    }

    public void toDBObject(BasicDBObjectBuilder builder)
    {
        builder.add(FIELD_NUM_DOCUMENTS, numDocuments)
                .add(FIELD_NUM_FOLDERS, numFolders)
                .add(FIELD_USERNAME, username).add(FIELD_SITE_ID, siteId)
                .add(FIELD_MAX_CONTENT_SIZE, maxContentSize)
                .add(FIELD_MIN_CONTENT_SIZE, minContentSize)
                .add(FIELD_TOTAL_CONTENT_SIZE, totalContentSize)
                .add(FIELD_MAX_FOLDER_DEPTH, maxFolderDepth)
                .add(FIELD_SITE_PATH, sitePath)
                .add(FIELD_OBJECT_ID, objectId);
    }

    public static TreeWalkData fromDBObject(DBObject dbObject)
    {
        TreeWalkData treeWalkData = new TreeWalkData();
        fromDBObject(treeWalkData, dbObject);
        return treeWalkData;
    }

    public static void fromDBObject(TreeWalkData treeWalkData, DBObject dbObject)
    {
        int numFolders = (Integer) dbObject.get(FIELD_NUM_FOLDERS);
        int numDocuments = (Integer) dbObject.get(FIELD_NUM_DOCUMENTS);
        String username = (String) dbObject.get(FIELD_USERNAME);
        String siteId = (String) dbObject.get(FIELD_SITE_ID);
        String objectId = (String) dbObject.get(FIELD_OBJECT_ID);
        String sitePath = (String) dbObject.get(FIELD_SITE_PATH);
        Integer maxContentSize = (Integer) dbObject.get(FIELD_MAX_CONTENT_SIZE);
        Integer minContentSize = (Integer) dbObject.get(FIELD_MIN_CONTENT_SIZE);
        Integer totalContentSize = (Integer) dbObject
                .get(FIELD_TOTAL_CONTENT_SIZE);
        Integer maxFolderDepth = (Integer) dbObject.get(FIELD_MAX_FOLDER_DEPTH);
        treeWalkData.setNumFolders(numFolders);
        treeWalkData.setNumDocuments(numDocuments);
        treeWalkData.setUsername(username);
        treeWalkData.setSiteId(siteId);
        treeWalkData.setTotalContentSize(totalContentSize);
        treeWalkData.setMaxContentSize(maxContentSize);
        treeWalkData.setMinContentSize(minContentSize);
        treeWalkData.setMaxFolderDepth(maxFolderDepth);
        treeWalkData.setObjectId(objectId);
        treeWalkData.setSitePath(sitePath);
    }
}
