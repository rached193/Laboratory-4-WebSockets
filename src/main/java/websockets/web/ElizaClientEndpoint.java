package websockets.web;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;

import org.glassfish.grizzly.Grizzly;

import console.TextDevice;
import console.TextDevices;

import websockets.ElizaClient;

@ClientEndpoint
public class ElizaClientEndpoint {

	private TextDevice con = TextDevices.defaultTextDevice();

	private static final Logger LOGGER = Grizzly.logger(ElizaClientEndpoint.class);

	@OnOpen
	public void onOpen(Session session) throws IOException {
		LOGGER.info("Connected ... " + session.getId());
	}

	@OnMessage
	public void onMessage(String message, Session session) {
		if (message.equals("---")) {
			String line = con.readLine();
			session.getAsyncRemote().sendText(line);
		} else {
			con.printf("[Eliza] %s\n", message);
		}
	}

	@OnClose
	public void onClose(Session session, CloseReason closeReason) {
		con.printf("[Eliza] %s\n", closeReason.getReasonPhrase());
		LOGGER.info(String.format("Session %s closed because of %s", session.getId(), closeReason.getCloseCode()));
		ElizaClient.LATCH.countDown();
	}

	@OnError
	public void onError(Session session, Throwable errorReason) {
		LOGGER.log(Level.SEVERE,
				String.format("Session %s closed because of %s", session.getId(), errorReason.getClass().getName()),
				errorReason);
		ElizaClient.LATCH.countDown();
	}

}
