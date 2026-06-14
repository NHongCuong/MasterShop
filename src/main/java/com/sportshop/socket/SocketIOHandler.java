package com.sportshop.socket;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DataListener;
import com.corundumstudio.socketio.listener.DisconnectListener;
import com.sportshop.entity.MessageEntity;
import com.sportshop.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SocketIOHandler {

    private final SocketIOServer server;
    private final MessageRepository messageRepository;
    
    // Map to store online users: key = userId, value = user data
    private static final Map<String, Map<String, Object>> onlineUsers = new ConcurrentHashMap<>();
    // Map to store client connections: key = sessionId, value = userId
    private static final Map<String, String> clientUserMap = new ConcurrentHashMap<>();

    @Autowired
    public SocketIOHandler(SocketIOServer server, MessageRepository messageRepository) {
        this.server = server;
        this.messageRepository = messageRepository;
        this.server.addConnectListener(onConnected());
        this.server.addDisconnectListener(onDisconnected());
        this.server.addEventListener("login", Map.class, onLogin());
        this.server.addEventListener("send_message", Map.class, onSendMessage());
        this.server.addEventListener("get_history", Map.class, onGetHistory());
    }

    private ConnectListener onConnected() {
        return client -> {
            System.out.println("Client connected: " + client.getSessionId());
        };
    }

    private DisconnectListener onDisconnected() {
        return client -> {
            String sessionId = client.getSessionId().toString();
            String userId = clientUserMap.get(sessionId);
            if (userId != null) {
                clientUserMap.remove(sessionId);
                // Check if user has other sessions
                boolean stillOnline = clientUserMap.containsValue(userId);
                if (!stillOnline) {
                    onlineUsers.remove(userId);
                    broadcastOnlineUsers();
                }
            }
            System.out.println("Client disconnected: " + sessionId);
        };
    }

    private DataListener<Map> onLogin() {
        return (client, data, ackSender) -> {
            String userId = String.valueOf(data.get("id"));
            clientUserMap.put(client.getSessionId().toString(), userId);
            onlineUsers.put(userId, data);
            
            // Join a personal room for targeted messaging
            client.joinRoom(userId);
            
            System.out.println("User logged in: " + userId);
            broadcastOnlineUsers();
        };
    }

    private DataListener<Map> onSendMessage() {
        return (client, data, ackSender) -> {
            String toUserId = String.valueOf(data.get("to"));
            String fromUserId = String.valueOf(data.get("from"));
            String messageString = String.valueOf(data.get("message"));
            
            // Save to database
            MessageEntity message = MessageEntity.builder()
                    .fromUser(fromUserId)
                    .toUser(toUserId)
                    .content(messageString)
                    .build();
            messageRepository.save(message);

            Map<String, Object> response = new HashMap<>();
            response.put("from", fromUserId);
            response.put("to", toUserId);
            response.put("message", messageString);
            response.put("timestamp", message.getCreatedAt().getTime());

            // Send to target user room
            server.getRoomOperations(toUserId).sendEvent("receive_message", response);
            // Also send back to sender room for synchronization across tabs/devices
            server.getRoomOperations(fromUserId).sendEvent("receive_message", response);
        };
    }

    private DataListener<Map> onGetHistory() {
        return (client, data, ackSender) -> {
            String user1 = String.valueOf(data.get("user1"));
            String user2 = String.valueOf(data.get("user2"));
            
            List<MessageEntity> history = messageRepository.findChatHistory(user1, user2);
            client.sendEvent("history_response", history);
        };
    }

    private void broadcastOnlineUsers() {
        server.getBroadcastOperations().sendEvent("update_user_list", onlineUsers.values());
    }
}
