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

    private static final long serialVersionUID = 946578159221599841L;

    private int numFolders;
    private int numDocuments;
    private String username;
    private String siteId;
    private int totalContentSize = 0;
    private int maxContentSize = 0;
    private int minContentSize = -1;
    private int maxFolderDepth = 0;

    public TreeWalkData(int numFolders, int numDocuments)
    {
        this.numFolders = numFolders;
        this.numDocuments = numDocuments;
    }

    public TreeWalkData(int numFolders, int numDocuments, String username,
            String siteId)
    {
        this(numFolders, numDocuments);
        this.username = username;
        this.siteId = siteId;
    }

    public TreeWalkData(int numFolders, int numDocuments, String username,
            String siteId, int totalContentSize, int maxContentSize,
            int minContentSize, int maxFolderDepth)
    {
        this(numFolders, numDocuments);
        this.username = username;
        this.siteId = siteId;
        this.totalContentSize = totalContentSize;
        this.maxContentSize = maxContentSize;
        this.minContentSize = minContentSize;
        this.maxFolderDepth = maxFolderDepth;
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
        if(size > maxContentSize)
        {
            maxContentSize = size;
        }
    }

    public void updateMinContentSize(int size)
    {
        if(minContentSize == -1)
        {
            minContentSize = Integer.MAX_VALUE;
        }
        else if(size < minContentSize)
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

    public DBObject toDBObject()
    {
        BasicDBObjectBuilder builder = BasicDBObjectBuilder
                .start(FIELD_NUM_DOCUMENTS, numDocuments)
                .add(FIELD_NUM_FOLDERS, numFolders)
                .add(FIELD_USERNAME, username)
                .add(FIELD_SITE_ID, siteId)
                .add(FIELD_MAX_CONTENT_SIZE, maxContentSize)
                .add(FIELD_MIN_CONTENT_SIZE, minContentSize)
                .add(FIELD_TOTAL_CONTENT_SIZE, totalContentSize)
                .add(FIELD_MAX_FOLDER_DEPTH, maxFolderDepth);
        DBObject dbObject = builder.get();
        return dbObject;
    }

    public static TreeWalkData fromDBObject(DBObject dbObject)
    {
        int numFolders = (Integer) dbObject.get(FIELD_NUM_FOLDERS);
        int numDocuments = (Integer) dbObject.get(FIELD_NUM_DOCUMENTS);
        String username = (String) dbObject.get(FIELD_USERNAME);
        String siteId = (String) dbObject.get(FIELD_SITE_ID);
        Integer maxContentSize = (Integer) dbObject.get(FIELD_MAX_CONTENT_SIZE);
        Integer minContentSize = (Integer) dbObject.get(FIELD_MIN_CONTENT_SIZE);
        Integer totalContentSize = (Integer) dbObject.get(FIELD_TOTAL_CONTENT_SIZE);
        Integer maxFolderDepth = (Integer) dbObject.get(FIELD_MAX_FOLDER_DEPTH);
        TreeWalkData treeWalkData = new TreeWalkData(numFolders, numDocuments,
                username, siteId, totalContentSize, maxContentSize, minContentSize,
                maxFolderDepth);
        return treeWalkData;
    }
}
