package com.ctrip.hermes.portal.resource.view;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class TopicDelayBriefView {
	private static final long NON_PRODUCE_LIMIT = TimeUnit.DAYS.toMillis(7);

	private static final long DELAY_LIMIT = TimeUnit.MINUTES.toMillis(3);

	private String topic;

	private Date latestProduced = new Date(0);

	private int averageDelay = 0;

	private int dangerLevel = 0;
	
	public TopicDelayBriefView() {

	}

	public TopicDelayBriefView(String topic, Date date, int delay) {
		this.topic = topic;
		this.latestProduced = date;
		this.averageDelay = delay;

		long now = System.currentTimeMillis();
		if (now - this.latestProduced.getTime() > NON_PRODUCE_LIMIT) {
			this.dangerLevel = 1;
		} else if (this.averageDelay > DELAY_LIMIT) {
			this.dangerLevel = 2;
		}
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public Date getLatestProduced() {
		return latestProduced;
	}

	public void setLatestProduced(Date latestProduced) {
		this.latestProduced = latestProduced;
	}

	public int getAverageDelay() {
		return averageDelay;
	}

	public void setAverageDelay(int averageDelay) {
		this.averageDelay = averageDelay;
	}

	public int getDangerLevel() {
		return dangerLevel;
	}

	public void setDangerLevel(int dangerLevel) {
		this.dangerLevel = dangerLevel;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((topic == null) ? 0 : topic.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TopicDelayBriefView other = (TopicDelayBriefView) obj;
		if (topic == null) {
			if (other.topic != null)
				return false;
		} else if (!topic.equals(other.topic))
			return false;
		return true;
	}
}
