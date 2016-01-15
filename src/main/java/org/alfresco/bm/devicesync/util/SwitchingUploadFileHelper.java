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
public class SwitchingUploadFileHelper implements UploadFileHelper
{
    private UploadFileHelper delegate;

    public SwitchingUploadFileHelper(String type,
            SpoofUploadFileHelper spoofUploadFileHelper,
            CMISUploadFileHelper cmisUploadFileHelper)
    {
        if (type.equals("cmis"))
        {
            delegate = cmisUploadFileHelper;
        }
        else if (type.equals("spoof"))
        {
            delegate = spoofUploadFileHelper;
        }
        else
        {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public DBObject doUpload(UploadFileData uploadFileData,
            SubscriptionData subscriptionData, UploadListener uploadListener,
            String filenamePrefix) throws IOException
    {
        return delegate.doUpload(uploadFileData, subscriptionData,
                uploadListener, filenamePrefix);
    }
}
