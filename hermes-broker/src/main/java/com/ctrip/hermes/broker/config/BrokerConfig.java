package com.ctrip.hermes.broker.config;

import java.util.UUID;

import org.unidal.lookup.annotation.Named;

/**
 * @author Leo Liang(jhliang@ctrip.com)
 *
 */
@Named(type = BrokerConfig.class)
public class BrokerConfig {
	private String m_sessionId = UUID.randomUUID().toString();

	private long m_leaseRenewTimeMillsBeforeExpire = 2 * 1000L;

	public String getSessionId() {
		return m_sessionId;
	}

	public long getLeaseRenewTimeMillsBeforeExpire() {
		return m_leaseRenewTimeMillsBeforeExpire;
	}

	public int getLongPollingServiceThreadCount() {
		return 3;
	}

	public long getLongPollingCheckIntervalMillis() {
		return 100L;
	}

	public int getDumperBatchSize() {
		return 20;
	}

	public long getDumperNoMessageWaitIntervalMillis() {
		return 50L;
	}

	public long getAckManagerCheckIntervalMillis() {
		return 100;
	}

	public int getAckManagerOpHandlingBatchSize() {
		return 50;
	}

	public int getAckManagerOpQueueSize() {
		return 10000;
	}

	public int getLeaseContainerThreadCount() {
		return 10;
	}

	public long getDefaultLeaseRenewDelayMillis() {
		return 500L;
	}

	public long getDefaultLeaseAcquireDelayMillis() {
		return 100L;
	}

	public int getListeningPort() {
		return 4376;
	}

	public int getClientMaxIdleSeconds() {
		return 3600;
	}
}
