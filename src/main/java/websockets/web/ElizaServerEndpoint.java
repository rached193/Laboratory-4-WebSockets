package websockets.web;

import java.io.IOException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.websocket.CloseReason;
import javax.websocket.CloseReason.CloseCodes;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.glassfish.grizzly.Grizzly;

import websockets.service.Eliza;

@ServerEndpoint(value = "/eliza")
public class ElizaServerEndpoint {

	private static final Logger LOGGER = Grizzly.logger(ElizaServerEndpoint.class);
	private Eliza eliza = new Eliza();

	@OnOpen
	public void onOpen(Session session) {
		LOGGER.info("Connected ... " + session.getId());
		session.getAsyncRemote().sendText("The doctor is in.");
		session.getAsyncRemote().sendText("What's on your mind?");
		session.getAsyncRemote().sendText("---");
	}

	@OnMessage
	public void onMessage(String message, Session session) throws IOException {
		Scanner currentLine = new Scanner(message.toLowerCase());
		if (currentLine.findInLine("bye") == null) {
			LOGGER.info(message);
			session.getAsyncRemote().sendText(eliza.respond(currentLine));
			session.getAsyncRemote().sendText("---");
		} else {
			session.close(new CloseReason(CloseCodes.NORMAL_CLOSURE, "Alright then, goodbye!"));
		}
	}

	@OnClose
	public void onClose(Session session, CloseReason closeReason) {
		LOGGER.info(String.format("Session %s closed because of %s", session.getId(), closeReason));
	}

	@OnError
	public void onError(Session session, Throwable errorReason) {
		LOGGER.log(Level.SEVERE,
				String.format("Session %s closed because of %s", session.getId(), errorReason.getClass().getName()),
				errorReason);
	}

}
