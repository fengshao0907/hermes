package com.ctrip.hermes.metaserver.consumer;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ctrip.hermes.meta.entity.Partition;

/**
 * @author Leo Liang(jhliang@ctrip.com)
 *
 */
public interface OrderedConsumeConsumerPartitionAssigningStrategy {

	public Map<Integer, Set<String>> assign(List<Partition> partitions, Set<String> consumers,
	      Map<Integer, Set<String>> originAssignment);
}
