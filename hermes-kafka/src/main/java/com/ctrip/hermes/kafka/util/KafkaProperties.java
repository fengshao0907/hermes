package com.ctrip.hermes.kafka.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.unidal.net.Networks;

public class KafkaProperties {

	/**
	 * 
	 * @return
	 */
	public static Properties getDefaultKafkaProducerProperties() {
		Properties producerProperties = new Properties();
		InputStream producerStream = KafkaProperties.class.getResourceAsStream("/hermes-producer-defaultkafka.properties");
		if (producerStream != null) {
			try {
				producerProperties.load(producerStream);
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					producerStream.close();
				} catch (IOException e) {
				}
			}
		}
		return producerProperties;
	}

	/**
	 * 
	 * @return
	 */
	public static Properties getDefaultKafkaConsumerProperties() {
		Properties consumerProperties = new Properties();
		InputStream consumerStream = KafkaProperties.class.getResourceAsStream("/hermes-consumer-defaultkafka.properties");
		if (consumerStream != null) {
			try {
				consumerProperties.load(consumerStream);
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					consumerStream.close();
				} catch (IOException e) {
				}
			}
		}
		return consumerProperties;
	}

	/**
	 * 
	 * @param producerProp
	 * @return
	 */
	public static Properties overrideByCtripDefaultProducerSetting(Properties producerProp) {
		producerProp.put("value.serializer", ByteArraySerializer.class.getCanonicalName());
		producerProp.put("key.serializer", StringSerializer.class.getCanonicalName());
		if (!producerProp.containsKey("client.id")) {
			producerProp.put("client.id", Networks.forIp().getLocalHostAddress());
		}

		return producerProp;
	}

	/**
	 * 
	 * @param consumerProp
	 * @return
	 */
	public static Properties overrideByCtripDefaultConsumerSetting(Properties consumerProp) {
		return consumerProp;
	}
}
