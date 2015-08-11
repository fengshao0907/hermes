package com.ctrip.hermes.metaserver.event.impl;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.ctrip.hermes.metaserver.broker.BrokerAssignmentHolder;
import com.ctrip.hermes.metaserver.commons.ClientContext;
import com.ctrip.hermes.metaserver.commons.EndpointMaker;
import com.ctrip.hermes.metaserver.event.Event;
import com.ctrip.hermes.metaserver.event.EventEngineContext;
import com.ctrip.hermes.metaserver.event.EventHandler;
import com.ctrip.hermes.metaserver.event.EventType;
import com.ctrip.hermes.metaserver.meta.MetaHolder;

/**
 * @author Leo Liang(jhliang@ctrip.com)
 *
 */
@Named(type = EventHandler.class, value = "BrokerListChangedEventHandler")
public class BrokerListChangedEventHandler extends BaseEventHandler {
	private static final Logger log = LoggerFactory.getLogger(BrokerListChangedEventHandler.class);

	@Inject
	private BrokerAssignmentHolder m_brokerAssignmentHolder;

	@Inject
	private EndpointMaker m_endpointMaker;

	@Inject
	private MetaHolder m_metaHolder;

	@Override
	public EventType eventType() {
		return EventType.BROKER_LIST_CHANGED;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void processEvent(EventEngineContext context, Event event) throws Exception {
		Object data = event.getData();
		if (data != null) {
			Map<String, ClientContext> brokers = (Map<String, ClientContext>) data;

			m_brokerAssignmentHolder.reassign(brokers);
			log.info("[FOR_TEST] Broker assignment reassigned, since broker list changed.");
			m_metaHolder.update(m_endpointMaker.makeEndpoints(context, m_brokerAssignmentHolder.getAssignments()));
		}
	}

	@Override
	protected Role role() {
		return Role.LEADER;
	}

}
