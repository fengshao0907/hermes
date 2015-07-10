package com.ctrip.hermes.portal.resource;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Singleton;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.tuple.Pair;

import com.alibaba.fastjson.JSON;
import com.ctrip.hermes.core.bo.SchemaView;
import com.ctrip.hermes.core.bo.TopicView;
import com.ctrip.hermes.core.exception.MessageSendException;
import com.ctrip.hermes.core.utils.PlexusComponentLocator;
import com.ctrip.hermes.core.utils.StringUtils;
import com.ctrip.hermes.meta.entity.Codec;
import com.ctrip.hermes.meta.entity.ConsumerGroup;
import com.ctrip.hermes.meta.entity.Endpoint;
import com.ctrip.hermes.meta.entity.Storage;
import com.ctrip.hermes.meta.entity.Topic;
import com.ctrip.hermes.metaservice.service.CodecService;
import com.ctrip.hermes.metaservice.service.PortalMetaService;
import com.ctrip.hermes.metaservice.service.SchemaService;
import com.ctrip.hermes.metaservice.service.TopicService;
import com.ctrip.hermes.portal.resource.assists.RestException;
import com.ctrip.hermes.portal.service.monitor.MonitorService;
import com.ctrip.hermes.producer.api.Producer;

@Path("/topics/")
@Singleton
@Produces(MediaType.APPLICATION_JSON)
public class TopicResource {

	private static final Logger logger = LoggerFactory.getLogger(TopicResource.class);

	private TopicService topicService = PlexusComponentLocator.lookup(TopicService.class);

	private PortalMetaService metaService = PlexusComponentLocator.lookup(PortalMetaService.class);

	private SchemaService schemaService = PlexusComponentLocator.lookup(SchemaService.class);

	private CodecService codecService = PlexusComponentLocator.lookup(CodecService.class);

	private MonitorService monitorService = PlexusComponentLocator.lookup(MonitorService.class);

	private Pair<Boolean, ?> validateTopicView(TopicView topic) {
		boolean passed = true;
		String reason = "";
		if (StringUtils.isBlank(topic.getName())) {
			reason = "Topic name is required";
			passed = false;
		} else if (StringUtils.isBlank(topic.getStorageType())) {
			reason = "Storage type is required";
			passed = false;
		} else if (StringUtils.isBlank(topic.getEndpointType())) {
			switch (topic.getStorageType()) {
			case Storage.KAFKA:
				topic.setEndpointType(Endpoint.KAFKA);
				break;
			case Storage.MYSQL:
				topic.setEndpointType(Endpoint.BROKER);
				break;
			}
		}

		return new Pair<>(passed, passed ? topic : reason);
	}

	@POST
	public Response createTopic(String content) {
		logger.info("Creating topic with payload {}.", content);
		if (StringUtils.isEmpty(content)) {
			logger.error("Payload content is empty, create topic failed.");
			throw new RestException("HTTP POST body is empty", Status.BAD_REQUEST);
		}

		Pair<Boolean, ?> result = null;
		try {
			result = validateTopicView(JSON.parseObject(content, TopicView.class));
		} catch (Exception e) {
			logger.error("Can not parse payload: {}, create topic failed.", content);
			throw new RestException(e, Status.BAD_REQUEST);
		}
		if (!result.getKey()) {
			throw new RestException((String) result.getValue());
		}

		TopicView topicView = (TopicView) result.getValue();

		Topic topic = topicView.toMetaTopic();
		if (topicService.findTopicByName(topic.getName()) != null) {
			throw new RestException("Topic already exists.", Status.CONFLICT);
		}

		try {
			topicView = new TopicView(topicService.createTopic(topic));
		} catch (Exception e) {
			logger.error("Create topic failed: {}.", content, e);
			throw new RestException(e, Status.INTERNAL_SERVER_ERROR);
		}
		return Response.status(Status.CREATED).entity(topicView).build();
	}

	@POST
	@Path("{topic}/send")
	public Response sendMessage(@PathParam("topic") String topic, String content) {
		try {
			Producer.getInstance().message(topic, "0", content).withRefKey(content).sendSync();
		} catch (MessageSendException e) {
			throw new RestException(e, Status.INTERNAL_SERVER_ERROR);
		}
		return Response.status(Status.OK).build();
	}

	@GET
	public List<TopicView> findTopics(@QueryParam("pattern") String pattern) {
		logger.debug("find topics, pattern {}", pattern);
		if (StringUtils.isEmpty(pattern)) {
			pattern = ".*";
		}

		List<Topic> topics = topicService.findTopics(pattern);
		List<TopicView> returnResult = new ArrayList<TopicView>();
		try {
			for (Topic topic : topics) {
				TopicView topicView = prepareTopicView(topic);

				Storage storage = metaService.findStorageByTopic(topic.getName());
				topicView.setStorage(storage);

				if (topic.getSchemaId() != null) {
					try {
						SchemaView schemaView = schemaService.getSchemaView(topic.getSchemaId());
						topicView.setSchema(schemaView);
					} catch (DalNotFoundException e) {
					}
				}

				Codec codec = codecService.getCodec(topic.getName());
				topicView.setCodec(codec);

				returnResult.add(topicView);
			}
		} catch (Exception e) {
			logger.warn("find topics failed", e);
			throw new RestException(e, Status.INTERNAL_SERVER_ERROR);
		}
		return returnResult;
	}

	private TopicView prepareTopicView(Topic topic) {
		TopicView topicView = new TopicView(topic);
		List<ConsumerGroup> consumers = metaService.findConsumersByTopic(topic.getName());
		long sum = 0;
		for (ConsumerGroup consumer : consumers) {
			Pair<Date, Date> delay = monitorService.getDelay(topic.getName(), consumer.getId());
			sum += (delay.getKey().getTime() - delay.getValue().getTime()) / 1000;
		}
		topicView.setAverageDelaySeconds(consumers.size() > 0 ? sum / consumers.size() : 0);
		topicView.setLatestProduced(monitorService.getLatestProduced(topic.getName()));
		return topicView;
	}

	@GET
	@Path("{name}")
	public TopicView getTopic(@PathParam("name") String name) {
		logger.debug("get topic {}", name);
		Topic topic = topicService.findTopicByName(name);
		if (topic == null) {
			throw new RestException("Topic not found: " + name, Status.NOT_FOUND);
		}

		TopicView topicView = prepareTopicView(topic);

		// Fill Storage
		Storage storage = metaService.findStorageByTopic(topic.getName());
		topicView.setStorage(storage);

		// Fill Schema
		if (topic.getSchemaId() != null) {
			SchemaView schemaView;
			try {
				schemaView = schemaService.getSchemaView(topic.getSchemaId());
				topicView.setSchema(schemaView);
			} catch (Exception e) {
				throw new RestException(e, Status.INTERNAL_SERVER_ERROR);
			}
		}

		// Fill Codec
		Codec codec = codecService.getCodec(topic.getName());
		topicView.setCodec(codec);

		return topicView;
	}

	@GET
	@Path("{name}/schemas")
	public List<SchemaView> getSchemas(@PathParam("name") String name) {
		logger.debug("get schemas, name: {}", name);
		List<SchemaView> returnResult = null;
		TopicView topic = getTopic(name);
		try {
			returnResult = schemaService.listSchemaView(topic.toMetaTopic());
		} catch (DalException e) {
			logger.warn("get schemas failed, name {}", name);
			throw new RestException(e, Status.INTERNAL_SERVER_ERROR);
		}
		return returnResult;
	}

	@PUT
	@Path("{name}")
	public Response updateTopic(@PathParam("name") String name, String content) {
		logger.debug("update {} content {}", name, content);
		if (StringUtils.isEmpty(content)) {
			throw new RestException("HTTP PUT body is empty", Status.BAD_REQUEST);
		}
		TopicView topicView = null;
		try {
			topicView = JSON.parseObject(content, TopicView.class);
			topicView.setName(name);
		} catch (Exception e) {
			logger.warn("parse topic failed, content {}", content);
			throw new RestException(e, Status.BAD_REQUEST);
		}

		Topic topic = topicView.toMetaTopic();

		if (topicService.findTopicByName(topic.getName()) == null) {
			throw new RestException("Topic does not exists.", Status.NOT_FOUND);
		}
		try {
			topic = topicService.updateTopic(topic);
			topicView = new TopicView(topic);
		} catch (Exception e) {
			logger.warn("update topic failed", e);
			throw new RestException(e, Status.INTERNAL_SERVER_ERROR);
		}
		return Response.status(Status.OK).entity(topicView).build();
	}

	@DELETE
	@Path("{name}")
	public Response deleteTopic(@PathParam("name") String name) {
		logger.debug("delete {}", name);
		try {
			topicService.deleteTopic(name);
		} catch (Exception e) {
			logger.warn("delete topic failed", e);
			throw new RestException(e, Status.INTERNAL_SERVER_ERROR);
		}
		return Response.status(Status.OK).build();
	}

	@POST
	@Path("{name}/deploy")
	public Response deployTopic(@PathParam("name") String name) {
		logger.debug("deploy {}", name);
		TopicView topicView = getTopic(name);
		try {
			Topic topic = topicView.toMetaTopic();
			if ("kafka".equalsIgnoreCase(topic.getStorageType())) {
				topicService.createTopicInKafka(topic);
			}
		} catch (Exception e) {
			logger.warn("deploy topic failed", e);
			throw new RestException(e, Status.INTERNAL_SERVER_ERROR);
		}
		return Response.status(Status.OK).build();
	}

	@POST
	@Path("{name}/undeploy")
	public Response undeployTopic(@PathParam("name") String name) {
		logger.debug("undeploy {}", name);
		TopicView topicView = getTopic(name);
		try {
			Topic topic = topicView.toMetaTopic();
			if ("kafka".equalsIgnoreCase(topic.getStorageType())) {
				topicService.deleteTopicInKafka(topic);
			}
		} catch (Exception e) {
			logger.warn("undeploy topic failed", e);
			throw new RestException(e, Status.INTERNAL_SERVER_ERROR);
		}
		return Response.status(Status.OK).build();
	}

	@POST
	@Path("{name}/config")
	public Response configTopic(@PathParam("name") String name) {
		logger.debug("config {}", name);
		TopicView topicView = getTopic(name);
		try {
			Topic topic = topicView.toMetaTopic();
			if ("kafka".equalsIgnoreCase(topic.getStorageType())) {
				topicService.configTopicInKafka(topic);
			}
		} catch (Exception e) {
			logger.warn("config topic failed", e);
			throw new RestException(e, Status.INTERNAL_SERVER_ERROR);
		}
		return Response.status(Status.OK).build();
	}
}
