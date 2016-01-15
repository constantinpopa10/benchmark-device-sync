package org.alfresco.bm.devicesync;

import org.junit.Test;

import com.mongodb.DBObject;
import com.mongodb.util.JSON;

public class Test1
{
    // @Test
    public void test() throws Exception
    {
        String syncMetrics = "{\"version\":\"3.0.0\",\"gauges\":{\"org.alfresco.service.synchronization.dropwizard.resources.SynchronizationResource.activeSyncs\":{\"value\":0}},\"counters\":{\"activeSyncsMeter\":{\"count\":0},\"syncFailuresCounter\":{\"count\":0},\"syncSuccessesCounter\":{\"count\":0}},\"histograms\":{\"endToEndSyncTimesHistogram\":{\"count\":0,\"max\":0,\"mean\":0.0,\"min\":0,\"p50\":0.0,\"p75\":0.0,\"p95\":0.0,\"p98\":0.0,\"p99\":0.0,\"p999\":0.0,\"stddev\":0.0},\"syncNumClientChangesHistogram\":{\"count\":0,\"max\":0,\"mean\":0.0,\"min\":0,\"p50\":0.0,\"p75\":0.0,\"p95\":0.0,\"p98\":0.0,\"p99\":0.0,\"p999\":0.0,\"stddev\":0.0},\"syncNumRepoChangesHistogram\":{\"count\":0,\"max\":0,\"mean\":0.0,\"min\":0,\"p50\":0.0,\"p75\":0.0,\"p95\":0.0,\"p98\":0.0,\"p99\":0.0,\"p999\":0.0,\"stddev\":0.0},\"syncTimesHistogram\":{\"count\":0,\"max\":0,\"mean\":0.0,\"min\":0,\"p50\":0.0,\"p75\":0.0,\"p95\":0.0,\"p98\":0.0,\"p99\":0.0,\"p999\":0.0,\"stddev\":0.0}},\"meters\":{\"syncsMeter\":{\"count\":0,\"m15_rate\":0.0,\"m1_rate\":0.0,\"m5_rate\":0.0,\"mean_rate\":0.0,\"units\":\"events/second\"}},\"timers\":{\"endToEndSyncsTimer\":{\"count\":0,\"max\":0.0,\"mean\":0.0,\"min\":0.0,\"p50\":0.0,\"p75\":0.0,\"p95\":0.0,\"p98\":0.0,\"p99\":0.0,\"p999\":0.0,\"stddev\":0.0,\"m15_rate\":0.0,\"m1_rate\":0.0,\"m5_rate\":0.0,\"mean_rate\":0.0,\"duration_units\":\"seconds\",\"rate_units\":\"calls/second\"},\"syncsTimer\":{\"count\":0,\"max\":0.0,\"mean\":0.0,\"min\":0.0,\"p50\":0.0,\"p75\":0.0,\"p95\":0.0,\"p98\":0.0,\"p99\":0.0,\"p999\":0.0,\"stddev\":0.0,\"m15_rate\":0.0,\"m1_rate\":0.0,\"m5_rate\":0.0,\"mean_rate\":0.0,\"duration_units\":\"seconds\",\"rate_units\":\"calls/second\"}}}";
        DBObject syncDBObject = (DBObject) JSON.parse(syncMetrics);
        System.out.println(syncDBObject);
    }

    @Test
    public void test1()
    {
        String s = "/a/b/c/d.txt";
        int idx = s.lastIndexOf("/");
        String s1 = s.substring(0, idx);
        System.out.println(s1);
    }
}
