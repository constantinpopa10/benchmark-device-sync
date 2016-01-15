package org.alfresco.bm.devicesync.util;

import java.io.IOException;

import org.alfresco.bm.devicesync.data.SubscriptionData;
import org.alfresco.bm.devicesync.data.UploadFileData;

import com.mongodb.DBObject;

/**
 * 
 * @author sglover
 *
 */
public interface UploadFileHelper
{
    static enum UPLOAD_TYPE
    {
        CREATE, UPDATE
    };

    DBObject doUpload(UploadFileData uploadFileData,
            SubscriptionData subscriptionData, UploadListener uploadListener,
            String filenamePrefix) throws IOException;

    public interface UploadListener
    {
        void beforeUpload();

        void afterUpload();

        void onException(Exception e);
    }
}
