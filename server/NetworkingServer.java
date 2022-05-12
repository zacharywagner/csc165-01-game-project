package server;

import java.io.IOException;

public class NetworkingServer {
	private GameServer ourGameServer;

	public NetworkingServer(int serverPort) {
        try {
            ourGameServer = new GameServer(serverPort);
		} 
		catch (IOException e) {	e.printStackTrace(); }
	}

	public static void main(String[] args) {
        if(args.length == 1) {
            NetworkingServer app = new NetworkingServer(Integer.parseInt(args[0]));
		}
	}
}
