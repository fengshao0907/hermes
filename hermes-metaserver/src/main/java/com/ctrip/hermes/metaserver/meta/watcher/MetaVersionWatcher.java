package com.ctrip.hermes.metaserver.meta.watcher;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;

import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ctrip.hermes.core.utils.PlexusComponentLocator;
import com.ctrip.hermes.meta.entity.Meta;
import com.ctrip.hermes.meta.entity.Topic;
import com.ctrip.hermes.metaserver.broker.BrokerAssignmentHolder;
import com.ctrip.hermes.metaserver.commons.GuardedWatcher;
import com.ctrip.hermes.metaserver.commons.WatcherGuard;
import com.ctrip.hermes.metaserver.meta.MetaHolder;
import com.ctrip.hermes.metaservice.service.MetaService;
import com.ctrip.hermes.metaservice.zk.ZKClient;
import com.ctrip.hermes.metaservice.zk.ZKPathUtils;

public class MetaVersionWatcher extends GuardedWatcher {

	private final static Logger log = LoggerFactory.getLogger(MetaVersionWatcher.class);

	public MetaVersionWatcher(int version, WatcherGuard guard, ExecutorService executor) {
		super(version, guard, executor, EventType.NodeDataChanged);
	}

	@Override
	protected void doProcess(WatchedEvent event) {
		log.info("Meta version of ZK changed");
		try {
			innerProcess(event);
		} catch (Exception e) {
			log.error("Error update base meta from DB", e);
		}
	}

	private void innerProcess(WatchedEvent event) throws Exception {
		CuratorFramework client = PlexusComponentLocator.lookup(ZKClient.class).getClient();
		client.getData().usingWatcher(this).forPath(ZKPathUtils.getBaseMetaVersionZkPath());

		MetaService metaService = PlexusComponentLocator.lookup(MetaService.class);
		Meta meta = metaService.findLatestMeta();

		MetaHolder metaHolder = PlexusComponentLocator.lookup(MetaHolder.class);
		metaHolder.setBaseMeta(meta);
		metaHolder.update(meta);

		BrokerAssignmentHolder brokerAssignmentHolder = PlexusComponentLocator.lookup(BrokerAssignmentHolder.class);
		brokerAssignmentHolder.reassign(new ArrayList<Topic>(meta.getTopics().values()));
	}

}
