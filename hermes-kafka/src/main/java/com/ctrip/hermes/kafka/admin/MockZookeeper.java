package com.ctrip.hermes.kafka.admin;

import java.io.IOException;

import org.apache.curator.test.TestingServer;

public class MockZookeeper {

	public static final int ZK_PORT = 2181;

	public static final String ZK_HOST = "localhost";

	public static String ZOOKEEPER_CONNECT = ZK_HOST + ":" + ZK_PORT;

	private TestingServer zkTestServer;

	public MockZookeeper() {
		try {
			zkTestServer = new TestingServer(ZK_PORT, false);
			start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void start() {
		try {
			zkTestServer.start();
			System.out.println("embedded zookeeper is up");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void stop() {
		if (zkTestServer != null) {
			try {
				zkTestServer.stop();
				System.out.println("embedded zookeeper is down");
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}
}
