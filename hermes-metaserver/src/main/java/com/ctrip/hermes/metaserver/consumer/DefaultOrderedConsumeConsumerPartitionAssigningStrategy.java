package com.ctrip.hermes.metaserver.consumer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.unidal.lookup.annotation.Named;

import com.ctrip.hermes.meta.entity.Partition;

/**
 * @author Leo Liang(jhliang@ctrip.com)
 *
 */
@Named(type = OrderedConsumeConsumerPartitionAssigningStrategy.class)
public class DefaultOrderedConsumeConsumerPartitionAssigningStrategy implements
      OrderedConsumeConsumerPartitionAssigningStrategy {

	@Override
	public Map<Integer, Set<String>> assign(List<Partition> partitions, Set<String> consumers,
	      Map<Integer, Set<String>> originAssignment) {
		Map<Integer, Set<String>> result = new HashMap<>();
		int partitionCount = partitions.size();
		int consumerCount = consumers.size();
		List<String> consumerNameList = new ArrayList<>(consumers);

		if (partitionCount == 0 || consumerCount == 0) {
			return result;
		}

		for (Partition partition : partitions) {
			result.put(partition.getId(), new HashSet<String>());
		}

		int consumerPos = 0;
		for (Partition partition : partitions) {
			result.get(partition.getId()).add(consumerNameList.get(consumerPos));
			consumerPos = (consumerPos + 1) % consumerCount;
		}

		return result;
	}
}
