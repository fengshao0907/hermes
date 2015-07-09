package com.ctrip.hermes.core.message.codec;

import java.nio.ByteBuffer;

import org.junit.Assert;
import org.junit.Test;

import com.alibaba.fastjson.JSON;
import com.ctrip.hermes.core.message.ProducerMessage;
import com.ctrip.hermes.core.message.codec.MessageCodecUtils;
import com.ctrip.hermes.core.message.codec.DefaultMessageCodec;

public class MessageCodecUtilsTest {

	@Test
	public void getJsonPayloadByByteBuffer() {
		ProducerMessage<String> proMsg = new ProducerMessage<String>();
		String expected = "Hello Ctrip";
		proMsg.setTopic("kafka.SimpleTextTopic");
		proMsg.setBody(expected);
		proMsg.setPartitionKey("MyPartition");
		proMsg.setKey("MyKey");
		proMsg.setBornTime(System.currentTimeMillis());
		DefaultMessageCodec codec = new DefaultMessageCodec();
		byte[] proMsgByte = codec.encode(proMsg);
		ByteBuffer byteBuffer = ByteBuffer.wrap(proMsgByte);
		ByteBuffer payload = MessageCodecUtils.getPayload(byteBuffer);
		Object actual = JSON.parseObject(payload.array(), String.class);
		Assert.assertEquals(expected, actual);
	}
	
	@Test
	public void getJsonPayloadByByteArray() {
		ProducerMessage<String> proMsg = new ProducerMessage<String>();
		String expected = "Hello Ctrip";
		proMsg.setTopic("kafka.SimpleTextTopic");
		proMsg.setBody(expected);
		proMsg.setPartitionKey("MyPartition");
		proMsg.setKey("MyKey");
		proMsg.setBornTime(System.currentTimeMillis());
		DefaultMessageCodec codec = new DefaultMessageCodec();
		byte[] proMsgByte = codec.encode(proMsg);
		byte[] payload = MessageCodecUtils.getPayload(proMsgByte);
		Object actual = JSON.parseObject(payload, String.class);
		Assert.assertEquals(expected, actual);
	}
}
