package org.alfresco.bm.devicesync.data;

import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;

/**
 * 
 * @author sglover
 *
 */
public class GetDocumentData extends TreeWalkData
{
    private static final long serialVersionUID = -2534206801540654960L;

    public GetDocumentData()
    {
    }

    public GetDocumentData(int numFolders, int numDocuments)
    {
        super(numFolders, numDocuments);
    }

    public GetDocumentData(int numFolders, int numDocuments, String username, String objectId)
    {
        super(numFolders, numDocuments, username, objectId);
    }

    public GetDocumentData(int numFolders, int numDocuments, String username,
            String siteId, String sitePath)
    {
        super(numFolders, numDocuments, username, siteId, sitePath);
    }

    public GetDocumentData(String documentId, int numFolders, int numDocuments,
            String username, String siteId, int totalContentSize,
            int maxContentSize, int minContentSize, int maxFolderDepth)
    {
        super(numFolders, numDocuments, username, siteId, totalContentSize,
                maxContentSize, minContentSize, maxFolderDepth, null, documentId);
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
    }

    public static GetDocumentData fromDBObject(DBObject dbObject)
    {
        GetDocumentData getDocumentData = new GetDocumentData();
        TreeWalkData.fromDBObject(getDocumentData, dbObject);
        return getDocumentData;
    }

    public static void fromDBObject(GetDocumentData getDocumentData, DBObject dbObject)
    {
        TreeWalkData.fromDBObject(getDocumentData, dbObject);
    }
}
