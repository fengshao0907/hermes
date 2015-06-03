package com.ctrip.hermes.metaserver.consumer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;
import org.unidal.tuple.Pair;

import com.ctrip.hermes.core.utils.CollectionUtil;
import com.ctrip.hermes.core.utils.CollectionUtil.Transformer;
import com.ctrip.hermes.meta.entity.ConsumerGroup;
import com.ctrip.hermes.meta.entity.Partition;
import com.ctrip.hermes.meta.entity.Topic;
import com.ctrip.hermes.metaserver.commons.ActiveClientListHolder;
import com.ctrip.hermes.metaserver.commons.BaseAssignmentHolder;
import com.ctrip.hermes.metaserver.config.MetaServerConfig;
import com.ctrip.hermes.metaserver.meta.MetaHolder;

/**
 * @author Leo Liang(jhliang@ctrip.com)
 *
 */
@Named(type = ConsumerAssignmentHolder.class)
public class ConsumerAssignmentHolder extends BaseAssignmentHolder<Pair<String, String>, Integer> {

	@Inject
	private MetaServerConfig m_config;

	@Inject
	private MetaHolder m_metaHolder;

	@Inject
	private OrderedConsumeConsumerPartitionAssigningStrategy m_partitionAssigningStrategy;

	@Inject
	private ActiveConsumerListHolder m_activeConsumerListHolder;

	@Override
	protected Assignment createNewAssignment(Pair<String, String> topicGroup, Set<String> consumers) {
		Topic topic = m_metaHolder.getMeta().findTopic(topicGroup.getKey());
		if (topic != null) {
			List<Partition> partitions = topic.getPartitions();
			if (partitions == null || partitions.isEmpty()) {
				return null;
			}

			ConsumerGroup consumerGroup = topic.findConsumerGroup(topicGroup.getValue());
			if (consumerGroup == null) {
				return null;
			}

			Map<String, List<Integer>> assigns = null;
			if (consumerGroup.isOrderedConsume()) {
				assigns = m_partitionAssigningStrategy.assign(partitions, consumers);
			} else {
				assigns = nonOrderedConsumeAssign(partitions, consumers);
			}

			if (assigns == null) {
				return null;
			}

			Assignment assignment = new Assignment();

			for (Map.Entry<String, List<Integer>> entry : assigns.entrySet()) {
				for (Integer partition : entry.getValue()) {
					assignment.addAssignment(partition, entry.getKey());
				}
			}

			return assignment;
		} else {
			return null;
		}
	}

	private Map<String, List<Integer>> nonOrderedConsumeAssign(List<Partition> partitions, Set<String> consumers) {
		Map<String, List<Integer>> result = new HashMap<>();

		List<Integer> partitionIds = new ArrayList<>();

		CollectionUtil.collect(partitions, new Transformer() {

			@Override
			public Object transform(Object partition) {
				return ((Partition) partition).getId();
			}
		}, partitionIds);

		for (String consumer : consumers) {
			result.put(consumer, partitionIds);
		}

		return result;
	}

	@Override
	protected long getClientTimeoutMillis() {
		return m_config.getConsumerHeartbeatTimeoutMillis();
	}

	@Override
	protected long getAssignmentCheckIntervalMillis() {
		return m_config.getActiveConsumerCheckIntervalTimeMillis();
	}

	@Override
	protected String getAssignmentCheckerName() {
		return "ConsumerRebalanceChecker";
	}

	@Override
	protected ActiveClientListHolder<Pair<String, String>> getActiveClientListHolder() {
		return m_activeConsumerListHolder;
	}

}