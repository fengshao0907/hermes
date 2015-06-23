package com.ctrip.hermes.metaserver.meta.watcher;

import java.util.concurrent.ExecutorService;

import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ctrip.hermes.core.utils.PlexusComponentLocator;
import com.ctrip.hermes.metaserver.commons.GuardedWatcher;
import com.ctrip.hermes.metaserver.commons.WatcherGuard;
import com.ctrip.hermes.metaserver.meta.MetaHolder;
import com.ctrip.hermes.metaservice.zk.ZKClient;
import com.ctrip.hermes.metaservice.zk.ZKPathUtils;

public class MetaServerListWatcher extends GuardedWatcher {

	private final static Logger log = LoggerFactory.getLogger(MetaServerListWatcher.class);

	public MetaServerListWatcher(int version, WatcherGuard guard, ExecutorService executor) {
		super(version, guard, executor, EventType.NodeChildrenChanged);
	}

	@Override
	protected void doProcess(WatchedEvent event) {
		log.info("Metaserver list changed");
		try {
			MetaHolder metaHolder = PlexusComponentLocator.lookup(MetaHolder.class);
			ZkReader zkReader = PlexusComponentLocator.lookup(ZkReader.class);
			CuratorFramework client = PlexusComponentLocator.lookup(ZKClient.class).getClient();
			client.getChildren().usingWatcher(this).forPath(ZKPathUtils.getMetaServersZkPath());
			metaHolder.update(zkReader.listMetaServers());
		} catch (Exception e) {
			log.error("Error update metaserver list from zk", e);
		}
	}

}
