package com.ctrip.hermes.portal.resource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.unidal.tuple.Pair;

import com.ctrip.hermes.core.utils.PlexusComponentLocator;
import com.ctrip.hermes.meta.entity.ConsumerGroup;
import com.ctrip.hermes.meta.entity.Topic;
import com.ctrip.hermes.metaservice.service.PortalMetaService;
import com.ctrip.hermes.portal.resource.assists.RestException;
import com.ctrip.hermes.portal.resource.view.MonitorTopicBriefView;
import com.ctrip.hermes.portal.resource.view.MonitorTopicDelayDetailView;
import com.ctrip.hermes.portal.service.monitor.MonitorService;

@Path("/monitor/")
@Singleton
@Produces(MediaType.APPLICATION_JSON)
public class MonitorResource {
	private MonitorService m_monitorService = PlexusComponentLocator.lookup(MonitorService.class);

	private PortalMetaService m_metaService = PlexusComponentLocator.lookup(PortalMetaService.class);

	@GET
	@Path("brief/topics")
	public Response getTopics() {
		List<MonitorTopicBriefView> list = new ArrayList<MonitorTopicBriefView>();
		for (Entry<String, Topic> entry : m_metaService.getTopics().entrySet()) {
			Topic t = entry.getValue();
			int avgDelay = 0;
			for (ConsumerGroup consumer : t.getConsumerGroups()) {
				Pair<Date, Date> delay = m_monitorService.getDelay(t.getName(), consumer.getId());
				avgDelay += delay.getKey().getTime() - delay.getValue().getTime();
			}
			avgDelay /= t.getConsumerGroups().size() == 0 ? 1 : t.getConsumerGroups().size();
			list.add(new MonitorTopicBriefView(t.getName(), m_monitorService.getLatestProduced(t.getName()), avgDelay));
		}

		Collections.sort(list, new Comparator<MonitorTopicBriefView>() {
			@Override
			public int compare(MonitorTopicBriefView left, MonitorTopicBriefView right) {
				int ret = right.getDangerLevel() - left.getDangerLevel();
				return ret = ret == 0 ? left.getTopic().compareTo(right.getTopic()) : ret;
			}
		});

		return Response.status(Status.OK).entity(list).build();
	}

	@GET
	@Path("detail/topics/{topic}/delay")
	public Response getTopicDelay(@PathParam("topic") String name) {
		List<MonitorTopicDelayDetailView> list = new ArrayList<MonitorTopicDelayDetailView>();

		Topic topic = m_metaService.findTopicByName(name);
		for (ConsumerGroup consumer : topic.getConsumerGroups()) {
			for (Entry<Integer, Pair<Date, Date>> e : m_monitorService.getDelayDetails(name, consumer.getId()).entrySet()) {
				MonitorTopicDelayDetailView view = new MonitorTopicDelayDetailView();
				view.setPartitionId(e.getKey());
				view.setConsumer(consumer.getName());
				view.setDelay((int) (e.getValue().getKey().getTime() - e.getValue().getValue().getTime()));
				list.add(view);
			}
		}

		return Response.status(Status.OK).entity(list).build();
	}

	@GET
	@Path("delay/{topic}/{groupId}")
	public Response getConsumeDelay(@PathParam("topic") String topic, @PathParam("groupId") int groupId) {
		Pair<Date, Date> delay = m_monitorService.getDelay(topic, groupId);
		if (delay == null) {
			throw new RestException(String.format("Delay [%s, %s] not found.", topic, groupId), Status.NOT_FOUND);
		}
		return Response.status(Status.OK).entity(delay).build();
	}

	@GET
	@Path("delay/{topic}/{groupId}/detail")
	public Response getConsumeDelayDetail(@PathParam("topic") String topic, @PathParam("groupId") int groupId) {
		Map<Integer, Pair<Date, Date>> delayDetails = m_monitorService.getDelayDetails(topic, groupId);
		if (delayDetails == null || delayDetails.size() == 0) {
			throw new RestException(String.format("Delay [%s, %s] not found.", topic, groupId), Status.NOT_FOUND);
		}
		return Response.status(Status.OK).entity(delayDetails).build();
	}
}
