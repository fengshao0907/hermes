package com.ctrip.hermes.rest;

import java.util.Properties;
import java.util.UUID;

import kafka.admin.AdminUtils;
import kafka.api.TopicMetadata;
import kafka.server.KafkaConfig;
import kafka.server.KafkaServerStartable;

import org.I0Itec.zkclient.ZkClient;

import com.ctrip.hermes.kafka.admin.ZKStringSerializer;

public class MockKafka {

	private static Properties createProperties(MockZookeeper zkServer, String logDir, String port, String brokerId) {
		Properties properties = new Properties();
		properties.put("port", port);
		properties.put("broker.id", brokerId);
		properties.put("log.dirs", logDir);
		properties.put("host.name", "localhost");
		properties.put("offsets.topic.replication.factor", "1");
		properties.put("delete.topic.enable", "true");
		properties.put("zookeeper.connect", zkServer.getConnectionString());
		return properties;
	}

	private KafkaServerStartable kafkaServer;

	private MockZookeeper zkServer;

	public MockKafka(MockZookeeper zkServer) {
		this(zkServer, System.getProperty("java.io.tmpdir") + "/" + UUID.randomUUID().toString(), "9092", "1");
		start();
	}

	private MockKafka(Properties properties) {
		KafkaConfig kafkaConfig = new KafkaConfig(properties);
		kafkaServer = new KafkaServerStartable(kafkaConfig);
	}

	public MockKafka(MockZookeeper zkServer, String port, String brokerId) {
		this(zkServer, System.getProperty("java.io.tmpdir") + "/" + UUID.randomUUID().toString(), port, brokerId);
		start();
	}

	private MockKafka(MockZookeeper zkServer, String logDir, String port, String brokerId) {
		this(createProperties(zkServer, logDir, port, brokerId));
		this.zkServer = zkServer;
		System.out.println(String.format("Kafka %s:%s dir:%s", kafkaServer.serverConfig().brokerId(), kafkaServer
		      .serverConfig().port(), kafkaServer.serverConfig().logDirs()));
	}

	public void createTopic(String topic, int partition, int replication) {
		ZkClient zkClient = new ZkClient(zkServer.getConnectionString());
		zkClient.setZkSerializer(new ZKStringSerializer());
		AdminUtils.createTopic(zkClient, topic, partition, replication, new Properties());
		zkClient.close();
	}

	public void createTopic(String topic) {
		this.createTopic(topic, 1, 1);
	}

	public TopicMetadata fetchTopicMeta(String topic) {
		ZkClient zkClient = new ZkClient(zkServer.getConnectionString());
		zkClient.setZkSerializer(new ZKStringSerializer());
		TopicMetadata topicMetadata = AdminUtils.fetchTopicMetadataFromZk(topic, zkClient);
		zkClient.close();
		return topicMetadata;
	}

	/**
	 * Delete may not work
	 * 
	 * @param topic
	 */
	public void deleteTopic(String topic) {
		ZkClient zkClient = new ZkClient(zkServer.getConnectionString());
		zkClient.setZkSerializer(new ZKStringSerializer());
		AdminUtils.deleteTopic(zkClient, topic);
		zkClient.close();
	}

	public String getConnectionString() {
		return String.format("%s:%d", kafkaServer.serverConfig().hostName(), kafkaServer.serverConfig().port());
	}

	public void start() {
		kafkaServer.startup();
		System.out.println("embedded kafka is up");
	}

	public void stop() {
		kafkaServer.shutdown();
		System.out.println("embedded kafka down");
	}

	public MockZookeeper getZookeeperServer() {
		return this.zkServer;
	}

}