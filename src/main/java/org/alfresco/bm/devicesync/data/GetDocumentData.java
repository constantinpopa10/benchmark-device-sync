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
    private static final String FIELD_DOCUMENT_ID = "documentId";

    private String documentId;

    public GetDocumentData(int numFolders, int numDocuments)
    {
        super(numFolders, numDocuments);
    }

    public GetDocumentData(int numFolders, int numDocuments, String username,
            String siteId)
    {
        super(numFolders, numDocuments, username, siteId);
    }

    public GetDocumentData(String documentId, int numFolders, int numDocuments,
            String username, String siteId, int totalContentSize,
            int maxContentSize, int minContentSize, int maxFolderDepth)
    {
        super(numFolders, numDocuments, username, siteId, totalContentSize,
                maxContentSize, minContentSize, maxFolderDepth);
        this.documentId = documentId;
    }

    public String getDocumentId()
    {
        return documentId;
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
                .add(FIELD_DOCUMENT_ID, documentId);
        DBObject dbObject = builder.get();
        return dbObject;
    }

    public static GetDocumentData fromDBObject(DBObject dbObject)
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
        String documentId = (String) dbObject.get(FIELD_DOCUMENT_ID);
        GetDocumentData getContentData = new GetDocumentData(documentId,
                numFolders, numDocuments, username, siteId, totalContentSize,
                maxContentSize, minContentSize, maxFolderDepth);
        return getContentData;
    }
}
