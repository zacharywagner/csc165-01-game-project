package server;

import java.io.IOException;
import java.net.InetAddress;
import java.util.UUID;

import tage.networking.server.GameConnectionServer;
import tage.networking.server.IClientInfo;

public class GameServer extends GameConnectionServer<UUID> {
	private UUID hostClientId;

    public GameServer(int localPort) throws IOException {
        super(localPort, ProtocolType.UDP);
    }

    @Override
    public void processPacket(Object o, InetAddress senderIP, int senderPort) {
        String message = (String)o;
		String[] messageTokens = message.split(",");
		
		if(messageTokens.length > 0) {
            // JOIN -- Case where client just joined the server
			// Received Message Format: (join,localId)
			if(messageTokens[0].compareTo("join") == 0) {
                try {
                    IClientInfo ci;					
					ci = getServerSocket().createClientInfo(senderIP, senderPort);
					UUID clientID = UUID.fromString(messageTokens[1]);
					addClient(ci, clientID);
					System.out.println("Join request received from - " + clientID.toString());
					boolean isHost = false;
					if(hostClientId == null) {
						hostClientId = clientID;
						isHost = true;
					}
					sendJoinedMessage(clientID, true, isHost);
				} 
				catch (IOException e) {	e.printStackTrace(); }
            }
			
			// BYE -- Case where clients leaves the server
			// Received Message Format: (bye,localId)
			if(messageTokens[0].compareTo("bye") == 0) {
                UUID clientID = UUID.fromString(messageTokens[1]);
				System.out.println("Exit request received from - " + clientID.toString());
				sendByeMessages(clientID);
				removeClient(clientID);
			}
			
			// CREATE -- Case where server receives a create message (to specify avatar location)
			// Received Message Format: (create,localId,x,y,z)
			if(messageTokens[0].compareTo("create") == 0) {
                UUID clientID = UUID.fromString(messageTokens[1]);
				String[] pos = {messageTokens[2], messageTokens[3], messageTokens[4]};
				sendCreateMessages(clientID, pos);
				sendWantsDetailsMessages(clientID);
			}
			
			// DETAILS-FOR --- Case where server receives a details for message
			// Received Message Format: (dsfr,remoteId,localId,x,y,z)
			if(messageTokens[0].compareTo("dsfr") == 0) {
                UUID clientID = UUID.fromString(messageTokens[1]);
				UUID remoteID = UUID.fromString(messageTokens[2]);
				String[] pos = {messageTokens[3], messageTokens[4], messageTokens[5]};
				sendDetailsForMessage(clientID, remoteID, pos);
			}

			// UPDATE-NPC --- Case where client needs NPC status
			// Recevied Message Format: (updatenpc,clientId,npcIndex,x,y,z)
			if(messageTokens[0].compareTo("updatenpc") == 0) {
				UUID clientID = UUID.fromString(messageTokens[1]);
				String npcIndex = messageTokens[2];
				String[] pos = {messageTokens[3], messageTokens[4], messageTokens[5]};
				sendDetailsForNpcMessage(clientID, npcIndex, pos);
			}
			
			// MOVE --- Case where server receives a move message
			// Received Message Format: (move,localId,x,y,z)
			if(messageTokens[0].compareTo("move") == 0) {
                UUID clientID = UUID.fromString(messageTokens[1]);
				String[] pos = {messageTokens[2], messageTokens[3], messageTokens[4]};
				sendMoveMessages(clientID, pos);
	        }

			// SENDSHOT --- Case where server receives a SENDSHOT message
			// Format (sendshot,clientIc,dirx,diry,dirz,isPlayers,locx,locy,locz,speed)
			if(messageTokens[0].compareTo("sendshot") == 0) {
				relayMessage(message, messageTokens[1]);
			}

			// CREATENPC --- Case where server receives a CREATENPC message
			// Format (createnpc,clientId,npcid,x,y,z)
			if(messageTokens[0].compareTo("createNPC") == 0) {
				relayMessage(message, messageTokens[1]);
			}

			// MOVENPC --- Case where server receives a MOVENPC message
			// Format (movenpc,clientId,npcid,x,y,z)
			if(messageTokens[0].compareTo("movenpc") == 0) {
				relayMessage(message, messageTokens[1]);
			}

			// DESTROYNPC --- Case where server receives a DESTROYNPC message
			// Format (movenpc,clientId,npcid)
			if(messageTokens[0].compareTo("destroynpc") == 0) {
				relayMessage(message, messageTokens[1]);
			}
        }
    }

    // Informs the client who just requested to join the server if their if their 
	// request was able to be granted. 
	// Message Format: (join,success) or (join,failure)
	
	public void sendJoinedMessage(UUID clientID, boolean success, boolean isHost) {
        try {
            System.out.println("trying to confirm join");
			String message = new String("join,");
			if(success)
				message += "success,";
			else
				message += "failure,";
			if(isHost)
				message += "host";
			else
				message += "guest";
			sendPacket(message, clientID);
		} 
		catch (IOException e) { e.printStackTrace();}
    }
	
	// Informs a client that the avatar with the identifier remoteId has left the server. 
	// This message is meant to be sent to all client currently connected to the server 
	// when a client leaves the server.
	// Message Format: (bye,remoteId)
	
	public void sendByeMessages(UUID clientID) {
        try {
            String message = new String("bye," + clientID.toString());
			forwardPacketToAll(message, clientID);
		} 
		catch (IOException e) {	e.printStackTrace();}
    }
	
	// Informs a client that a new avatar has joined the server with the unique identifier 
	// remoteId. This message is intended to be send to all clients currently connected to 
	// the server when a new client has joined the server and sent a create message to the 
	// server. This message also triggers WANTS_DETAILS messages to be sent to all client 
	// connected to the server. 
	// Message Format: (create,remoteId,x,y,z) where x, y, and z represent the position

	public void sendCreateMessages(UUID clientID, String[] position) {
        try {
            String message = new String("create," + clientID.toString());
			message += "," + position[0];
			message += "," + position[1];
			message += "," + position[2];	
			forwardPacketToAll(message, clientID);
		} 
		catch (IOException e) {	e.printStackTrace();}
    }
	
	// Informs a client of the details for a remote client�s avatar. This message is in response 
	// to the server receiving a DETAILS_FOR message from a remote client. That remote client�s 
	// message�s localId becomes the remoteId for this message, and the remote client�s message�s 
	// remoteId is used to send this message to the proper client. 
	// Message Format: (dsfr,remoteId,x,y,z) where x, y, and z represent the position.

	public void sendDetailsForMessage(UUID clientID, UUID remoteId, String[] position) {
        try {
            String message = new String("dsfr," + remoteId.toString());
			message += "," + position[0];
			message += "," + position[1];
			message += "," + position[2];	
			sendPacket(message, clientID);
		} 
		catch (IOException e) {	e.printStackTrace();}
    }
	
	// Informs a local client that a remote client wants the local client�s avatar�s information. 
	// This message is meant to be sent to all clients connected to the server when a new client 
	// joins the server. 
	// Message Format: (wsds,remoteId)
	
	public void sendWantsDetailsMessages(UUID clientID) {
        try {
            String message = new String("wsds," + clientID.toString());	
			forwardPacketToAll(message, clientID);
		} 
		catch (IOException e) {	e.printStackTrace();}
    }

	// Shares NPC info with other clients to update their NPC tables.
	// Message Format: (npcstatus,npcIndex,x,y,z)
	public void sendDetailsForNpcMessage(UUID clientID, String npcIndex, String[] position) {
		try {
			String message = new String("npcstatus");
			message += "," + npcIndex;
			message += "," + position[0];
			message += "," + position[1];
			message += "," + position[2];
			forwardPacketToAll(message, clientID);
		}
		catch (IOException e) {e.printStackTrace();}
	}
	
	// Informs a client that a remote client�s avatar has changed position. x, y, and z represent 
	// the new position of the remote avatar. This message is meant to be forwarded to all clients
	// connected to the server when it receives a MOVE message from the remote client.   
	// Message Format: (move,remoteId,x,y,z) where x, y, and z represent the position.

	public void sendMoveMessages(UUID clientID, String[] position) {
        try {
            String message = new String("move," + clientID.toString());
			message += "," + position[0];
			message += "," + position[1];
			message += "," + position[2];
			forwardPacketToAll(message, clientID);
		} 
		catch (IOException e) {	e.printStackTrace();}
    }

	public void relayMessage(String message, String recipientID) {
		try {
			UUID clientID = UUID.fromString(recipientID);
			forwardPacketToAll(message, clientID);	
		}
		catch (IOException e) { e.printStackTrace();}
	}
}
