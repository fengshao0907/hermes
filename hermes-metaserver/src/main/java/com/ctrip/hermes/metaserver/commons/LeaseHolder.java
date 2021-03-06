package com.ctrip.hermes.metaserver.commons;

import java.util.Map;

import com.ctrip.hermes.core.lease.Lease;
import com.ctrip.hermes.core.lease.LeaseAcquireResponse;

/**
 * @author Leo Liang(jhliang@ctrip.com)
 *
 */
public interface LeaseHolder<Key> {

	public Map<Key, Map<String, ClientLeaseInfo>> getAllValidLeases() throws Exception;

	public LeaseAcquireResponse executeLeaseOperation(Key contextKey, LeaseOperationCallback callback) throws Exception;

	public Lease newLease(Key contextKey, String clientKey, Map<String, ClientLeaseInfo> existingValidLeases,
	      long leaseTimeMillis, String ip, int port) throws Exception;

	public void renewLease(Key contextKey, String clientKey, Map<String, ClientLeaseInfo> existingValidLeases,
	      ClientLeaseInfo existingLeaseInfo, long leaseTimeMillis, String ip, int port) throws Exception;

	public void updateContexts(Map<String, Map<String, ClientLeaseInfo>> path2ExistingLeases) throws Exception;

}