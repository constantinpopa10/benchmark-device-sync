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
    private static final String FIELD_NUM_FOLDERS_FETCHED = "numFoldersFetched";
    private static final String FIELD_NUM_DOCUMENTS_FETCHED = "numDocumentsFetched";

    private int numFoldersFetched;
    private int numDocumentsFetched;

    public GetChildrenData()
    {
    }

    public GetChildrenData(int numFolders, int numDocuments, String username, String objectId)
    {
        super(numFolders, numDocuments, username, objectId);
    }

    public GetChildrenData(int numFolders, int numDocuments, String username,
            String siteId, String sitePath)
    {
        super(numFolders, numDocuments, username, siteId, sitePath);
    }

    public GetChildrenData(String folderId, int numFolders, int numDocuments,
            String username, String siteId, int totalContentSize,
            int maxContentSize, int minContentSize, int maxFolderDepth,
            int numFoldersFetched, int numDocumentsFetched)
    {
        super(numFolders, numDocuments, username, siteId, totalContentSize,
                maxContentSize, minContentSize, maxFolderDepth, null, folderId);
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

    public DBObject toDBObject()
    {
        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();
        toDBObject(builder);
        return builder.get();
    }

    public void toDBObject(BasicDBObjectBuilder builder)
    {
        super.toDBObject(builder);
        builder.add(FIELD_NUM_FOLDERS_FETCHED, numFoldersFetched)
            .add(FIELD_NUM_DOCUMENTS_FETCHED, numDocumentsFetched);
    }

    public static GetChildrenData fromDBObject(DBObject dbObject)
    {
        GetChildrenData data = new GetChildrenData();
        int numFoldersFetched = (Integer) dbObject.get(FIELD_NUM_FOLDERS_FETCHED);
        int numDocumentsFetched = (Integer) dbObject.get(FIELD_NUM_DOCUMENTS_FETCHED);
        data.setNumFoldersFetched(numFoldersFetched);
        data.setNumDocumentsFetched(numDocumentsFetched);
        return data;
    }

    public static void fromDBObject(GetChildrenData data, DBObject dbObject)
    {
        int numFoldersFetched = (Integer) dbObject.get(FIELD_NUM_FOLDERS_FETCHED);
        int numDocumentsFetched = (Integer) dbObject.get(FIELD_NUM_DOCUMENTS_FETCHED);
        data.setNumFoldersFetched(numFoldersFetched);
        data.setNumDocumentsFetched(numDocumentsFetched);
    }
}
