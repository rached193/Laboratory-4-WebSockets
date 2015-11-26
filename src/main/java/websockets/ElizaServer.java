package websockets;

import java.util.HashMap;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.grizzly.Grizzly;
import org.glassfish.tyrus.server.Server;

import websockets.web.ElizaServerEndpoint;

public class ElizaServer {
	private static final Logger LOGGER = Grizzly.logger(ElizaServer.class);

	public static void main(String[] args) {
		runServer();
	}

	public static void runServer() {
		Server server = new Server("localhost", 8025, "/websockets", new HashMap<String, Object>(),
				ElizaServerEndpoint.class);

		try (Scanner s = new Scanner(System.in)) {
			server.start();
			LOGGER.info("Press 's' to shutdown now the server...");
			while (!s.hasNext("s"))
				;
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, e.toString(), e);
		} finally {
			server.stop();
			LOGGER.info("Server stopped");
		}
	}
}
