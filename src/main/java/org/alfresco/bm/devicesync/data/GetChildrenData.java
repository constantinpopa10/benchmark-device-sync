package org.alfresco.bm.devicesync.data;

import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;

/**
 * 
 * @author sglover
 *
 */
public class GetChildrenData extends TreeWalkData
{
    private static final long serialVersionUID = -2534206801540654960L;
    private static final String FIELD_FOLDER_ID = "folderId";
    private static final String FIELD_NUM_FOLDERS_FETCHED = "numFoldersFetched";
    private static final String FIELD_NUM_DOCUMENTS_FETCHED = "numDocumentsFetched";

    private String folderId;
    private int numFoldersFetched;
    private int numDocumentsFetched;

    public GetChildrenData(int numFolders, int numDocuments)
    {
        super(numFolders, numDocuments);
    }

    public GetChildrenData(int numFolders, int numDocuments, String username,
            String siteId)
    {
        super(numFolders, numDocuments, username, siteId);
    }

    public GetChildrenData(String folderId, int numFolders, int numDocuments,
            String username, String siteId, int totalContentSize,
            int maxContentSize, int minContentSize, int maxFolderDepth,
            int numFoldersFetched, int numDocumentsFetched)
    {
        super(numFolders, numDocuments, username, siteId, totalContentSize,
                maxContentSize, minContentSize, maxFolderDepth);
        this.folderId = folderId;
        this.numFoldersFetched = numFoldersFetched;
        this.numDocumentsFetched = numDocumentsFetched;
    }

    public void setNumFoldersFetched(int numFoldersFetched)
    {
        this.numFoldersFetched = numFoldersFetched;
    }

    public int getNumFoldersFetched()
    {
        return numFoldersFetched;
    }

    public int getNumDocumentsFetched()
    {
        return numDocumentsFetched;
    }

    public void setNumDocumentsFetched(int numDocumentsFetched)
    {
        this.numDocumentsFetched = numDocumentsFetched;
    }

    public String getFolderId()
    {
        return folderId;
    }

    public void setFolderId(String folderId)
    {
        this.folderId = folderId;
    }

    public DBObject toDBObject()
    {
        BasicDBObjectBuilder builder = BasicDBObjectBuilder
                .start(FIELD_NUM_DOCUMENTS, numDocuments)
                .add(FIELD_NUM_FOLDERS, numFolders)
                .add(FIELD_USERNAME, username).add(FIELD_SITE_ID, siteId)
                .add(FIELD_MAX_CONTENT_SIZE, maxContentSize)
                .add(FIELD_MIN_CONTENT_SIZE, minContentSize)
                .add(FIELD_TOTAL_CONTENT_SIZE, totalContentSize)
                .add(FIELD_MAX_FOLDER_DEPTH, maxFolderDepth)
                .add(FIELD_FOLDER_ID, folderId)
                .add(FIELD_NUM_FOLDERS_FETCHED, numFoldersFetched)
                .add(FIELD_NUM_DOCUMENTS_FETCHED, numDocumentsFetched);
        DBObject dbObject = builder.get();
        return dbObject;
    }

    public static GetChildrenData fromDBObject(DBObject dbObject)
    {
        int numFolders = (Integer) dbObject.get(FIELD_NUM_FOLDERS);
        int numDocuments = (Integer) dbObject.get(FIELD_NUM_DOCUMENTS);
        String username = (String) dbObject.get(FIELD_USERNAME);
        String siteId = (String) dbObject.get(FIELD_SITE_ID);
        Integer maxContentSize = (Integer) dbObject.get(FIELD_MAX_CONTENT_SIZE);
        Integer minContentSize = (Integer) dbObject.get(FIELD_MIN_CONTENT_SIZE);
        Integer totalContentSize = (Integer) dbObject
                .get(FIELD_TOTAL_CONTENT_SIZE);
        Integer maxFolderDepth = (Integer) dbObject.get(FIELD_MAX_FOLDER_DEPTH);
        String folderId = (String) dbObject.get(FIELD_FOLDER_ID);
        int numFoldersFetched = (Integer) dbObject
                .get(FIELD_NUM_FOLDERS_FETCHED);
        int numDocumentsFetched = (Integer) dbObject
                .get(FIELD_NUM_DOCUMENTS_FETCHED);
        GetChildrenData getContentData = new GetChildrenData(folderId,
                numFolders, numDocuments, username, siteId, totalContentSize,
                maxContentSize, minContentSize, maxFolderDepth,
                numFoldersFetched, numDocumentsFetched);
        return getContentData;
    }
}
