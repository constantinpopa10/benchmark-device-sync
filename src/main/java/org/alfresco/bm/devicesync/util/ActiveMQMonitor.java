/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.bm.devicesync.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * 
 * @author sglover
 *
 */
public class ActiveMQMonitor
{
    public static final String UTF_8_ENCODING = "UTF-8";
    public static final String MIME_TYPE_JSON = "application/json";

    private final String activeMQHost;
    private final int activeMQPort;
    private String username = "admin";
    private String password = "admin";
    private ObjectMapper mapper;
    private CloseableHttpClient client;

    public ActiveMQMonitor(String activeMQHost, int activeMQPort)
    {
        this.activeMQHost = activeMQHost;
        this.activeMQPort = activeMQPort;
        this.mapper = new ObjectMapper();
        this.client = buildClient();
    }

    public static class ActiveMQStats
    {
        private BrokerStats brokerStats;
        private List<DestinationStats> destinationStats = new LinkedList<>();

        void withBrokerStats(BrokerStats brokerStats)
        {
            this.brokerStats = brokerStats;
        }

        void addDestinationStats(DestinationStats stats)
        {
            destinationStats.add(stats);
        }

        public List<DestinationStats> getDestinationStats()
        {
            return destinationStats;
        }

        public BrokerStats getBrokerStats()
        {
            return brokerStats;
        }

        @Override
        public String toString()
        {
            return "ActiveMQStats [brokerStats=" + brokerStats
                    + ", destinationStats=" + destinationStats + "]";
        }
    }

    public static class BrokerStats
    {
        private double memoryPercentUsage;
        private double storePercentUsage;
        private double tempPercentUsage;

        public BrokerStats()
        {
            super();
        }

        public BrokerStats withMemoryPercentUsage(double memoryPercentUsage)
        {
            this.memoryPercentUsage = memoryPercentUsage;
            return this;
        }

        public BrokerStats withStorePercentUsage(double storePercentUsage)
        {
            this.storePercentUsage = storePercentUsage;
            return this;
        }

        public BrokerStats withTempPercentUsage(double tempPercentUsage)
        {
            this.tempPercentUsage = tempPercentUsage;
            return this;
        }

        public double getMemoryPercentUsage()
        {
            return memoryPercentUsage;
        }

        public double getStorePercentUsage()
        {
            return storePercentUsage;
        }

        public double getTempPercentUsage()
        {
            return tempPercentUsage;
        }

        @Override
        public String toString()
        {
            return "BrokerStats [memoryPercentUsage=" + memoryPercentUsage
                    + ", storePercentUsage=" + storePercentUsage
                    + ", tempPercentUsage=" + tempPercentUsage + "]";
        }
    }

    public static class DestinationStats
    {
        private String destinationType;
        private String destinationName;
        private double averageEnqueueTime;
        private double enqueueCount;
        private double dequeueCount;
        private double dispatchCount;
        private double memoryPercentUsage;
        private double queueSize;
        private double maxEnqueueTime;
        private double blockedSends;
        private double averageBlockedTime;

        public DestinationStats(String destinationType, String destinationName)
        {
            this.destinationType = destinationType;
            this.destinationName = destinationName;
        }

        public DestinationStats(double averageEnqueueTime, double enqueueCount,
                double dequeueCount, double dispatchCount)
        {
            super();
            this.averageEnqueueTime = averageEnqueueTime;
            this.enqueueCount = enqueueCount;
            this.dequeueCount = dequeueCount;
            this.dispatchCount = dispatchCount;
        }

        public double getBlockedSends()
        {
            return blockedSends;
        }

        public double getAverageBlockedTime()
        {
            return averageBlockedTime;
        }

        public double getMaxEnqueueTime()
        {
            return maxEnqueueTime;
        }

        public double getQueueSize()
        {
            return queueSize;
        }

        public String getDestinationType()
        {
            return destinationType;
        }

        public String getDestinationName()
        {
            return destinationName;
        }

        public double getMemoryPercentUsage()
        {
            return memoryPercentUsage;
        }

        public void setMemoryPercentUsage(double memoryPercentUsage)
        {
            this.memoryPercentUsage = memoryPercentUsage;
        }

        DestinationStats setAverageEnqueueTime(double averageEnqueueTime)
        {
            this.averageEnqueueTime = averageEnqueueTime;
            return this;
        }

        DestinationStats setEnqueueCount(double enqueueCount)
        {
            this.enqueueCount = enqueueCount;
            return this;
        }

        DestinationStats setDequeueCount(double dequeueCount)
        {
            this.dequeueCount = dequeueCount;
            return this;
        }

        DestinationStats setDispatchCount(double dispatchCount)
        {
            this.dispatchCount = dispatchCount;
            return this;
        }

        DestinationStats setQueueSize(double queueSize)
        {
            this.queueSize = queueSize;
            return this;
        }

        DestinationStats setMaxEnqueueTime(double maxEnqueueTime)
        {
            this.maxEnqueueTime = maxEnqueueTime;
            return this;
        }

        DestinationStats setBlockedSends(double blockedSends)
        {
            this.blockedSends = blockedSends;
            return this;
        }

        DestinationStats setAverageBlockedTime(double averageBlockedTime)
        {
            this.averageBlockedTime = averageBlockedTime;
            return this;
        }

        public double getAverageEnqueueTime()
        {
            return averageEnqueueTime;
        }

        public double getEnqueueCount()
        {
            return enqueueCount;
        }

        public double getDequeueCount()
        {
            return dequeueCount;
        }

        public double getDispatchCount()
        {
            return dispatchCount;
        }

        @Override
        public String toString()
        {
            return "DestinationStats [destinationType=" + destinationType
                    + ", destinationName=" + destinationName
                    + ", averageEnqueueTime=" + averageEnqueueTime
                    + ", enqueueCount=" + enqueueCount + ", dequeueCount="
                    + dequeueCount + ", dispatchCount=" + dispatchCount
                    + ", memoryPercentUsage=" + memoryPercentUsage
                    + ", queueSize=" + queueSize + ", maxEnqueueTime="
                    + maxEnqueueTime + ", blockedSends=" + blockedSends
                    + ", averageBlockedTime=" + averageBlockedTime + "]";
        }
    }

    public ActiveMQStats getStats() throws IOException
    {
        ActiveMQStats activeMQStats = new ActiveMQStats();
        BrokerStats brokerStats = getBrokerStats();
        activeMQStats.withBrokerStats(brokerStats);
//        DestinationStats destStats = getStats("Topic",
//                "alfresco.repo.events.nodes");
        DestinationStats destStats = getStats("Queue",
                "Consumer.DesktopSync.VirtualTopic.alfresco.repo.events.nodes");
        activeMQStats.addDestinationStats(destStats);
//        destStats = getStats("Queue", "alfresco.sync.changes.request");
//        activeMQStats.addDestinationStats(destStats);
//        destStats = getStats("Queue", "alfresco.sync.changes.response");
//        activeMQStats.addDestinationStats(destStats);
//        destStats = getStats("Queue", "alfresco.sync.changes.clear");
//        activeMQStats.addDestinationStats(destStats);
        return activeMQStats;
    }

    private CloseableHttpClient buildClient()
    {
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(new AuthScope(activeMQHost, activeMQPort),
                new UsernamePasswordCredentials(username, password));

        PoolingHttpClientConnectionManager poolingConnManager = new PoolingHttpClientConnectionManager();
        // Increase max total connection to 200
        poolingConnManager.setMaxTotal(500);
        // Increase default max connection per route to 20
        poolingConnManager.setDefaultMaxPerRoute(200);

        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(5 * 1000)
                .setConnectionRequestTimeout(5 * 1000)
                .setSocketTimeout(5 * 1000).build();
        CloseableHttpClient client = HttpClients.custom()
                .setConnectionManager(poolingConnManager)
                .setDefaultRequestConfig(config)
                .setDefaultCredentialsProvider(credsProvider).build();
        return client;
    }

    private DestinationStats getStats(String destinationType,
            String destinationName) throws IOException
    {
        DestinationStats stats = new DestinationStats(destinationType,
                destinationName);

        StringBuilder sb = new StringBuilder("http://");
        sb.append(activeMQHost);
        sb.append(":");
        sb.append(activeMQPort);
        sb.append("/api/jolokia");
        String url = sb.toString();

        CloseableHttpResponse httpResponse = null;

        HttpPost httpPost = new HttpPost(url);
        Request[] post = new Request[]
        {
                new Request("read",
                        "org.apache.activemq:type=Broker,brokerName=localhost,destinationType="
                                + destinationType + ",destinationName="
                                + destinationName, "AverageEnqueueTime"),
                new Request("read",
                        "org.apache.activemq:type=Broker,brokerName=localhost,destinationType="
                                + destinationType + ",destinationName="
                                + destinationName, "EnqueueCount"),
                new Request("read",
                        "org.apache.activemq:type=Broker,brokerName=localhost,destinationType="
                                + destinationType + ",destinationName="
                                + destinationName, "DequeueCount"),
                new Request("read",
                        "org.apache.activemq:type=Broker,brokerName=localhost,destinationType="
                                + destinationType + ",destinationName="
                                + destinationName, "DispatchCount"),
                new Request("read",
                        "org.apache.activemq:type=Broker,brokerName=localhost,destinationType="
                                + destinationType + ",destinationName="
                                + destinationName, "MemoryPercentUsage"),
                new Request("read",
                        "org.apache.activemq:type=Broker,brokerName=localhost,destinationType="
                                + destinationType + ",destinationName="
                                + destinationName, "AverageBlockedTime"),
                new Request("read",
                        "org.apache.activemq:type=Broker,brokerName=localhost,destinationType="
                                + destinationType + ",destinationName="
                                + destinationName, "QueueSize"),
                new Request("read",
                        "org.apache.activemq:type=Broker,brokerName=localhost,destinationType="
                                + destinationType + ",destinationName="
                                + destinationName, "BlockedSends"),
                new Request("read",
                        "org.apache.activemq:type=Broker,brokerName=localhost,destinationType="
                                + destinationType + ",destinationName="
                                + destinationName, "MaxEnqueueTime") };
        String str = mapper.writeValueAsString(post);
        HttpEntity postEntity = new StringEntity(str);
        httpPost.setEntity(postEntity);
        httpResponse = client.execute(httpPost);

        try
        {
            StatusLine status = httpResponse.getStatusLine();
            // Expecting "OK" status
            if (status.getStatusCode() == HttpStatus.SC_OK)
            {
                HttpEntity entity = httpResponse.getEntity();
                InputStream in = entity.getContent();
                try
                {
                    ByteBuffer bb = ByteBuffer.allocate(1024 * 40);
                    ReadableByteChannel inChannel = Channels.newChannel(in);
                    int read = -1;
                    do
                    {
                        read = inChannel.read(bb);
                    }
                    while (read > 0);
                    bb.flip();
                    Response[] response = mapper.readValue(bb.array(),
                            Response[].class);
                    for (Response r : response)
                    {
                        if (r.getRequest().getAttribute()
                                .equals("AverageEnqueueTime"))
                        {
                            stats.setAverageEnqueueTime(r.getValue() != null ? r
                                    .getValue() : 0.0);
                        }
                        else if (r.getRequest().getAttribute()
                                .equals("EnqueueCount"))
                        {
                            stats.setEnqueueCount(r.getValue() != null ? r
                                    .getValue() : 0.0);
                        }
                        else if (r.getRequest().getAttribute()
                                .equals("DequeueCount"))
                        {
                            stats.setDequeueCount(r.getValue() != null ? r
                                    .getValue() : 0.0);
                        }
                        else if (r.getRequest().getAttribute()
                                .equals("DispatchCount"))
                        {
                            stats.setDispatchCount(r.getValue() != null ? r
                                    .getValue() : 0.0);
                        }
                        else if (r.getRequest().getAttribute()
                                .equals("MemoryPercentUsage"))
                        {
                            stats.setMemoryPercentUsage(r.getValue() != null ? r
                                    .getValue() : 0.0);
                        }
                        else if (r.getRequest().getAttribute()
                                .equals("AverageBlockedTime"))
                        {
                            stats.setAverageBlockedTime(r.getValue() != null ? r
                                    .getValue() : 0.0);
                        }
                        else if (r.getRequest().getAttribute()
                                .equals("QueueSize"))
                        {
                            stats.setQueueSize(r.getValue() != null ? r
                                    .getValue() : 0.0);
                        }
                        else if (r.getRequest().getAttribute()
                                .equals("MaxEnqueueTime"))
                        {
                            stats.setMaxEnqueueTime(r.getValue() != null ? r
                                    .getValue() : 0.0);
                        }
                        else if (r.getRequest().getAttribute()
                                .equals("BlockedSends"))
                        {
                            stats.setBlockedSends(r.getValue() != null ? r
                                    .getValue() : 0.0);
                        }
                        else if (r.getRequest().getAttribute()
                                .equals("DispatchCount"))
                        {
                            stats.setDispatchCount(r.getValue() != null ? r
                                    .getValue() : 0.0);
                        }
                    }
                }
                finally
                {
                    if (in != null)
                    {
                        in.close();
                    }
                }
            }
            else
            {
                // TODO
            }
        }
        finally
        {
            if (httpResponse != null)
            {
                httpResponse.close();
            }
            if (httpPost != null)
            {
                httpPost.releaseConnection();
            }
        }

        return stats;
    }

    private BrokerStats getBrokerStats() throws IOException
    {
        BrokerStats brokerStats = new BrokerStats();

        StringBuilder sb = new StringBuilder("http://");
        sb.append(activeMQHost);
        sb.append(":");
        sb.append(activeMQPort);
        sb.append("/api/jolokia");
        String url = sb.toString();

        CloseableHttpResponse httpResponse = null;

        HttpPost httpPost = new HttpPost(url);
        Request[] post = new Request[]
        {
                new Request("read",
                        "org.apache.activemq:type=Broker,brokerName=localhost",
                        "MemoryPercentUsage"),
                new Request("read",
                        "org.apache.activemq:type=Broker,brokerName=localhost",
                        "StorePercentUsage"),
                new Request("read",
                        "org.apache.activemq:type=Broker,brokerName=localhost",
                        "TempPercentUsage") };
        String str = mapper.writeValueAsString(post);
        HttpEntity postEntity = new StringEntity(str);
        httpPost.setEntity(postEntity);
        httpResponse = client.execute(httpPost);

        try
        {
            StatusLine status = httpResponse.getStatusLine();
            // Expecting "OK" status
            if (status.getStatusCode() == HttpStatus.SC_OK)
            {
                HttpEntity entity = httpResponse.getEntity();
                InputStream in = entity.getContent();
                try
                {
                    ByteBuffer bb = ByteBuffer.allocate(1024 * 10);
                    ReadableByteChannel inChannel = Channels.newChannel(in);
                    int read = -1;
                    do
                    {
                        read = inChannel.read(bb);
                    }
                    while (read != -1);
                    bb.flip();
                    Response[] response = mapper.readValue(bb.array(),
                            Response[].class);
                    for (Response r : response)
                    {
                        if (r.getRequest().getAttribute()
                                .equals("MemoryPercentUsage"))
                        {
                            double memoryPercentUsage = r.getValue() != null ? r
                                    .getValue() : 0.0;
                            brokerStats
                                    .withMemoryPercentUsage(memoryPercentUsage);
                        }
                        else if (r.getRequest().getAttribute()
                                .equals("StorePercentUsage"))
                        {
                            double storePercentUsage = r.getValue() != null ? r
                                    .getValue() : 0.0;
                            brokerStats
                                    .withStorePercentUsage(storePercentUsage);
                        }
                        else if (r.getRequest().getAttribute()
                                .equals("TempPercentUsage"))
                        {
                            double tempPercentUsage = r.getValue() != null ? r
                                    .getValue() : 0.0;
                            brokerStats.withTempPercentUsage(tempPercentUsage);
                        }
                    }
                }
                finally
                {
                    if (in != null)
                    {
                        in.close();
                    }
                }
            }
            else
            {
                // TODO
            }
        }
        finally
        {
            if (httpResponse != null)
            {
                httpResponse.close();
            }
            if (httpPost != null)
            {
                httpPost.releaseConnection();
            }
        }

        return brokerStats;
    }

    private static class Response
    {
        private Request request;
        private Double value;
        private Long timestamp;
        private int status;
        private String stacktrace;
        private String error_type;
        private String error;

        public String getError_type()
        {
            return error_type;
        }

        public void setError_type(String error_type)
        {
            this.error_type = error_type;
        }

        public String getError()
        {
            return error;
        }

        public void setError(String error)
        {
            this.error = error;
        }

        public String getStacktrace()
        {
            return stacktrace;
        }

        public void setStacktrace(String stacktrace)
        {
            this.stacktrace = stacktrace;
        }

        public Request getRequest()
        {
            return request;
        }

        public void setRequest(Request request)
        {
            this.request = request;
        }

        public Double getValue()
        {
            return value;
        }

        public void setValue(Double value)
        {
            this.value = value;
        }

        public Long getTimestamp()
        {
            return timestamp;
        }

        public void setTimestamp(Long timestamp)
        {
            this.timestamp = timestamp;
        }

        public int getStatus()
        {
            return status;
        }

        public void setStatus(int status)
        {
            this.status = status;
        }

        @Override
        public String toString()
        {
            return "EnqueueTimeResponse [request=" + request + ", value="
                    + value + ", timestamp=" + timestamp + ", status=" + status
                    + ", stacktrace=" + stacktrace + ", error_type="
                    + error_type + ", error=" + error + "]";
        }
    }

    private static class Request
    {
        private String type;
        private String mbean;
        private String attribute;

        public Request()
        {
        }

        public Request(String type, String mbean, String attribute)
        {
            super();
            this.type = type;
            this.mbean = mbean;
            this.attribute = attribute;
        }

        public String getType()
        {
            return type;
        }

        public void setType(String type)
        {
            this.type = type;
        }

        public String getMbean()
        {
            return mbean;
        }

        public void setMbean(String mbean)
        {
            this.mbean = mbean;
        }

        public String getAttribute()
        {
            return attribute;
        }

        public void setAttribute(String attribute)
        {
            this.attribute = attribute;
        }

        @Override
        public String toString()
        {
            return "Request [type=" + type + ", mbean=" + mbean
                    + ", attribute=" + attribute + "]";
        }
    }
}
