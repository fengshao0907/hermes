package com.ctrip.hermes.metaserver.meta;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.ctrip.hermes.core.utils.HermesThreadFactory;
import com.ctrip.hermes.meta.entity.Endpoint;
import com.ctrip.hermes.meta.entity.Meta;
import com.ctrip.hermes.meta.entity.Server;
import com.ctrip.hermes.metaserver.meta.watcher.ZkReader;
import com.ctrip.hermes.metaservice.service.MetaService;

/**
 * @author Leo Liang(jhliang@ctrip.com)
 *
 */
@Named(type = MetaHolder.class)
public class MetaHolder implements Initializable {

	@Inject
	private MetaService m_metaService;

	@Inject
	private ZkReader m_zkReader;

	private AtomicReference<Meta> m_mergedCache = new AtomicReference<>();

	private AtomicReference<Meta> m_baseCache = new AtomicReference<>();

	private AtomicReference<List<Server>> m_serverCache = new AtomicReference<>();

	// topic -> partition -> endpoint
	private AtomicReference<Map<String, Map<Integer, Endpoint>>> m_endpointCache = new AtomicReference<>();

	private ExecutorService m_updateTaskExecutor;

	private MetaMerger m_metaMerger = new MetaMerger();

	public Meta getMeta() {
		return m_mergedCache.get();
	}

	public void setMeta(Meta meta) {
		m_mergedCache.set(meta);
	}

	@Override
	public void initialize() throws InitializationException {
		m_updateTaskExecutor = Executors.newSingleThreadExecutor(HermesThreadFactory.create("RefreshMeta", true));
	}

	public void setBaseMeta(Meta baseMeta) {
		m_baseCache.set(baseMeta);
	}

	public void update(final Meta meta) {
		m_updateTaskExecutor.submit(new Runnable() {

			@Override
			public void run() {
				m_mergedCache.set(m_metaMerger.merge(meta, m_serverCache.get(), m_endpointCache.get()));
			}

		});
	}

	public void update(final List<Server> newServers) {
		m_serverCache.set(newServers);
		m_updateTaskExecutor.submit(new Runnable() {

			@Override
			public void run() {
				m_mergedCache.set(m_metaMerger.merge(m_baseCache.get(), newServers, m_endpointCache.get()));
			}

		});
	}

	public void update(final Map<String, Map<Integer, Endpoint>> newEndpoints) {
		m_endpointCache.set(newEndpoints);
		m_updateTaskExecutor.submit(new Runnable() {

			@Override
			public void run() {
				m_mergedCache.set(m_metaMerger.merge(m_baseCache.get(), m_serverCache.get(), newEndpoints));
			}

		});
	}

}
