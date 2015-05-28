package com.ctrip.hermes.core.transport.command;

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.ctrip.hermes.core.config.CoreConfig;
import com.ctrip.hermes.core.message.PartialDecodedMessage;
import com.ctrip.hermes.core.message.ProducerMessage;
import com.ctrip.hermes.core.message.codec.MessageCodec;
import com.ctrip.hermes.core.result.SendResult;
import com.ctrip.hermes.core.service.SystemClockService;
import com.ctrip.hermes.core.transport.ManualRelease;
import com.ctrip.hermes.core.utils.HermesPrimitiveCodec;
import com.ctrip.hermes.core.utils.PlexusComponentLocator;
import com.google.common.util.concurrent.SettableFuture;

/**
 * @author Leo Liang(jhliang@ctrip.com)
 *
 */
@ManualRelease
public class SendMessageCommand extends AbstractCommand {

	private static final long serialVersionUID = 8443575812437722822L;

	private AtomicInteger m_msgCounter = new AtomicInteger(0);

	private String m_topic;

	private int m_partition;

	private ConcurrentMap<Integer, List<ProducerMessage<?>>> m_msgs = new ConcurrentHashMap<>();

	private transient Map<Integer, MessageBatchWithRawData> m_decodedBatches = new HashMap<>();

	private transient Map<Integer, SettableFuture<SendResult>> m_futures = new HashMap<>();

	public SendMessageCommand() {
		super(CommandType.MESSAGE_SEND);
	}

	public SendMessageCommand(String topic, int partition) {
		super(CommandType.MESSAGE_SEND);
		m_topic = topic;
		m_partition = partition;
	}

	public String getTopic() {
		return m_topic;
	}

	public int getPartition() {
		return m_partition;
	}

	public void addMessage(ProducerMessage<?> msg, SettableFuture<SendResult> future) {
		validate(msg);

		int msgSeqNo = m_msgCounter.getAndIncrement();
		msg.setMsgSeqNo(msgSeqNo);

		if (msg.isPriority()) {
			m_msgs.putIfAbsent(0, new LinkedList<ProducerMessage<?>>());
			m_msgs.get(0).add(msg);
		} else {
			m_msgs.putIfAbsent(1, new LinkedList<ProducerMessage<?>>());
			m_msgs.get(1).add(msg);
		}

		m_futures.put(msgSeqNo, future);
	}

	private void validate(ProducerMessage<?> msg) {
		if (!m_topic.equals(msg.getTopic()) || m_partition != msg.getPartition()) {
			throw new IllegalArgumentException(String.format(
			      "Illegal message[topic=%s, partition=%s] try to add to SendMessageCommand[topic=%s, partition=%s]",
			      msg.getTopic(), msg.getPartition(), m_topic, m_partition));
		}
	}

	public Map<Integer, MessageBatchWithRawData> getMessageRawDataBatches() {
		return m_decodedBatches;
	}

	public int getMessageCount() {
		return m_msgCounter.get();
	}

	public void onResultReceived(SendMessageResultCommand result) {
		for (Map.Entry<Integer, SettableFuture<SendResult>> entry : m_futures.entrySet()) {
			if (result.isSuccess(entry.getKey())) {
				entry.getValue().set(new SendResult(true));
			} else {
				entry.getValue().setException(new RuntimeException("Send failed"));
			}
		}
	}

	@Override
	public void parse0(ByteBuf buf) {
		m_rawBuf = buf;

		HermesPrimitiveCodec codec = new HermesPrimitiveCodec(buf);

		m_msgCounter.set(codec.readInt());

		m_topic = codec.readString();
		m_partition = codec.readInt();

		readDatas(buf, codec, m_topic);

	}

	@Override
	public void toBytes0(ByteBuf buf) {
		HermesPrimitiveCodec codec = new HermesPrimitiveCodec(buf);

		codec.writeInt(m_msgCounter.get());

		codec.writeString(m_topic);
		codec.writeInt(m_partition);

		writeDatas(buf, codec, m_msgs);
	}

	private void writeDatas(ByteBuf buf, HermesPrimitiveCodec codec, Map<Integer, List<ProducerMessage<?>>> msgs) {
		codec.writeInt(msgs.size());
		for (Map.Entry<Integer, List<ProducerMessage<?>>> entry : m_msgs.entrySet()) {
			// priority flag
			codec.writeInt(entry.getKey());

			writeMsgs(entry.getValue(), codec, buf);
		}
	}

	private void writeMsgs(List<ProducerMessage<?>> msgs, HermesPrimitiveCodec codec, ByteBuf buf) {
		MessageCodec msgCodec = PlexusComponentLocator.lookup(MessageCodec.class);
		// write msgSeqs
		codec.writeInt(msgs.size());

		// seqNos
		for (ProducerMessage<?> msg : msgs) {
			codec.writeInt(msg.getMsgSeqNo());
		}

		// placeholder for payload len
		int indexBeforeLen = buf.writerIndex();
		codec.writeInt(-1);

		int indexBeforePayload = buf.writerIndex();
		// payload
		for (ProducerMessage<?> msg : msgs) {
			msgCodec.encode(msg, buf);
		}
		int indexAfterPayload = buf.writerIndex();
		int payloadLen = indexAfterPayload - indexBeforePayload;

		// refill payload len
		buf.writerIndex(indexBeforeLen);
		codec.writeInt(payloadLen);

		buf.writerIndex(indexAfterPayload);
	}

	private void readDatas(ByteBuf buf, HermesPrimitiveCodec codec, String topic) {
		int size = codec.readInt();
		for (int i = 0; i < size; i++) {
			int priority = codec.readInt();

			m_decodedBatches.put(priority, readMsgs(topic, codec, buf));
		}

	}

	private MessageBatchWithRawData readMsgs(String topic, HermesPrimitiveCodec codec, ByteBuf buf) {
		int size = codec.readInt();

		List<Integer> msgSeqs = new ArrayList<>();

		for (int j = 0; j < size; j++) {
			msgSeqs.add(codec.readInt());
		}

		int payloadLen = codec.readInt();

		ByteBuf rawData = buf.readSlice(payloadLen);

		return new MessageBatchWithRawData(topic, msgSeqs, rawData);

	}

	public static class MessageBatchWithRawData {
		private String m_topic;

		private List<Integer> m_msgSeqs;

		private ByteBuf m_rawData;

		private List<PartialDecodedMessage> m_msgs;

		public MessageBatchWithRawData(String topic, List<Integer> msgSeqs, ByteBuf rawData) {
			m_topic = topic;
			m_msgSeqs = msgSeqs;
			m_rawData = rawData;
		}

		public String getTopic() {
			return m_topic;
		}

		public List<Integer> getMsgSeqs() {
			return m_msgSeqs;
		}

		public ByteBuf getRawData() {
			return m_rawData.duplicate();
		}

		public List<PartialDecodedMessage> getMessages() {

			if (m_msgs == null) {
				synchronized (this) {
					if (m_msgs == null) {
						m_msgs = new ArrayList<>();

						ByteBuf tmpBuf = m_rawData.duplicate();
						MessageCodec messageCodec = PlexusComponentLocator.lookup(MessageCodec.class);

						while (tmpBuf.readableBytes() > 0) {
							m_msgs.add(messageCodec.decodePartial(tmpBuf));
						}

					}
				}
			}

			return m_msgs;
		}
	}

	public void onTimeout() {
		Exception e = new RuntimeException("Send timeout");
		for (Map.Entry<Integer, SettableFuture<SendResult>> entry : m_futures.entrySet()) {
			entry.getValue().setException(e);
		}
	}

	public long getExpireTime() {
		return PlexusComponentLocator.lookup(SystemClockService.class).now()
		      + PlexusComponentLocator.lookup(CoreConfig.class).getSendMessageReadResultTimeout();
	}

	public Collection<List<ProducerMessage<?>>> getProducerMessages() {
		return m_msgs.values();
	}

}
