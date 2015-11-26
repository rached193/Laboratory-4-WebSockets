package websockets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import javax.websocket.ClientEndpointConfig;
import javax.websocket.DeploymentException;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.Session;

import org.glassfish.tyrus.client.ClientManager;
import org.glassfish.tyrus.server.Server;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import websockets.web.ElizaServerEndpoint;

public class ElizaServerTest {

	private Server server;

	@Before
	public void setup() throws DeploymentException {
		server = new Server("localhost", 8025, "/websockets", new HashMap<String, Object>(), ElizaServerEndpoint.class);
		server.start();
	}

	@Test(timeout = 1000)
	public void onOpen() throws DeploymentException, IOException, URISyntaxException, InterruptedException {
		CountDownLatch latch = new CountDownLatch(3);
		List<String> list = new ArrayList<>();
		ClientEndpointConfig configuration = ClientEndpointConfig.Builder.create().build();
		ClientManager client = ClientManager.createClient();
		client.connectToServer(new Endpoint() {

			@Override
			public void onOpen(Session session, EndpointConfig config) {
				session.addMessageHandler(new MessageHandler.Whole<String>() {

					@Override
					public void onMessage(String message) {
						list.add(message);
						latch.countDown();
					}
				});
			}

		}, configuration, new URI("ws://localhost:8025/websockets/eliza"));
		latch.await();
		assertEquals(3, list.size());
		assertEquals("The doctor is in.", list.get(0));
	}

	@Test(timeout = 1000)
	public void onChat() throws DeploymentException, IOException, URISyntaxException, InterruptedException {
		CountDownLatch latch = new CountDownLatch(5);
		List<String> list = new ArrayList<>();
		ClientEndpointConfig configuration = ClientEndpointConfig.Builder.create().build();
		ClientManager client = ClientManager.createClient();
		client.connectToServer(new Endpoint() {

			@Override
			public void onOpen(Session session, EndpointConfig config) {

				session.getAsyncRemote().sendText("I really need to sleep more and have a nice beer-dream");

				session.addMessageHandler(new MessageHandler.Whole<String>() {

					@Override
					public void onMessage(String message) {
						list.add(message);
						latch.countDown();
					}
				});
			}

		}, configuration, new URI("ws://localhost:8025/websockets/eliza"));
		latch.await();
		assertEquals(5, list.size());

		//response copied from Eliza.java
		String[] response =  { "What does that dream suggest to you?", "Do you dream often?",
				"What persons appear in your dreams?", "Are you disturbed by your dreams?" };

		assertTrue(response[0].equals(list.get(3)) || response[1].equals(list.get(3)) || response[2].equals(list.get(3))
				|| response[3].equals(list.get(3)));

	}

	@After
	public void close() {
		server.stop();
	}
}
