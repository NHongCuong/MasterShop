package com.sportshop.socket;

import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DataListener;
import com.corundumstudio.socketio.listener.DisconnectListener;
import com.sportshop.entity.MessageEntity;
import com.sportshop.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
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
        this.server.addEventListener("request_user_list", Void.class, onRequestUserList());
        this.server.addEventListener("typing", Map.class, (client, data, ackSender) -> {
            String to = String.valueOf(data.get("to"));
            server.getRoomOperations(to).sendEvent("typing", data);
        });

        this.server.addEventListener("react_message", Map.class, onReactMessage());
        this.server.addEventListener("recall_message", Map.class, onRecallMessage());
    }

    @SuppressWarnings("rawtypes")
    private DataListener<Map> onReactMessage() {
        return (client, data, ackSender) -> {
            Long messageId = Long.valueOf(String.valueOf(data.get("messageId")));
            String reactions = String.valueOf(data.get("reactions"));
            
            MessageEntity message = messageRepository.findById(messageId).orElse(null);
            if (message != null) {
                message.setReactions(reactions);
                messageRepository.save(message);
                
                // Prepare safe response map
                Map<String, Object> response = new HashMap<>();
                response.put("id", message.getId());
                response.put("reactions", message.getReactions());
                response.put("isDeleted", message.getIsDeleted() != null && message.getIsDeleted());

                // Broadcast update to both parties
                server.getRoomOperations(message.getToUser()).sendEvent("message_updated", response);
                server.getRoomOperations(message.getFromUser()).sendEvent("message_updated", response);
            }
        };
    }

    @SuppressWarnings("rawtypes")
    private DataListener<Map> onRecallMessage() {
        return (client, data, ackSender) -> {
            Long messageId = Long.valueOf(String.valueOf(data.get("messageId")));
            
            MessageEntity message = messageRepository.findById(messageId).orElse(null);
            if (message != null) {
                message.setIsDeleted(true);
                message.setContent("Tin nhắn đã được thu hồi");
                messageRepository.save(message);
                
                // Prepare safe response map
                Map<String, Object> response = new HashMap<>();
                response.put("id", message.getId());
                response.put("message", message.getContent());
                response.put("isDeleted", true);

                // Broadcast update
                server.getRoomOperations(message.getToUser()).sendEvent("message_updated", response);
                server.getRoomOperations(message.getFromUser()).sendEvent("message_updated", response);
            }
        };
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

    @SuppressWarnings("rawtypes")
    private DataListener<Map> onLogin() {
        return (client, data, ackSender) -> {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) data;
            String userId = String.valueOf(map.get("id"));
            clientUserMap.put(client.getSessionId().toString(), userId);
            onlineUsers.put(userId, map);
            
            // Join a personal room for targeted messaging
            client.joinRoom(userId);
            
            System.out.println("User logged in: " + userId);
            
            // Send current list directly to this client immediately
            client.sendEvent("update_user_list", onlineUsers.values());
            
            // Broadcast to everyone else
            broadcastOnlineUsers();
        };
    }

    private DataListener<Void> onRequestUserList() {
        return (client, data, ackSender) -> {
            client.sendEvent("update_user_list", onlineUsers.values());
        };
    }

    @SuppressWarnings("rawtypes")
    private DataListener<Map> onSendMessage() {
        return (client, data, ackSender) -> {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) data;
            String toUserId = String.valueOf(map.get("to"));
            String fromUserId = String.valueOf(map.get("from"));
            String messageString = String.valueOf(map.get("message"));
            Long replyToId = map.get("replyToId") != null ? Long.valueOf(String.valueOf(map.get("replyToId"))) : null;
            
            // Save to database
            MessageEntity message = MessageEntity.builder()
                    .fromUser(fromUserId)
                    .toUser(toUserId)
                    .content(messageString)
                    .replyToId(replyToId)
                    .build();
            messageRepository.save(message);

            Map<String, Object> response = new HashMap<>();
            response.put("id", message.getId());
            response.put("from", fromUserId);
            response.put("to", toUserId);
            response.put("message", messageString);
            response.put("replyToId", replyToId);
            response.put("timestamp", message.getCreatedAt().getTime());

            // If reply, get the original message content for frontend convenience
            if (replyToId != null) {
                messageRepository.findById(replyToId).ifPresent(m -> {
                    response.put("replyContent", m.getContent());
                });
            }

            // Send to target user room
            server.getRoomOperations(toUserId).sendEvent("receive_message", response);
            // Also send back to sender room for synchronization across tabs/devices
            server.getRoomOperations(fromUserId).sendEvent("receive_message", response);
        };
    }

    @SuppressWarnings("rawtypes")
    private DataListener<Map> onGetHistory() {
        return (client, data, ackSender) -> {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) data;
            String user1 = String.valueOf(map.get("user1"));
            String user2 = String.valueOf(map.get("user2"));
            
            List<MessageEntity> history = messageRepository.findChatHistory(user1, user2);
            List<Map<String, Object>> response = new ArrayList<>();
            for (MessageEntity m : history) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", m.getId());
                item.put("from", m.getFromUser());
                item.put("to", m.getToUser());
                item.put("message", m.getContent());
                item.put("replyToId", m.getReplyToId());
                item.put("reactions", m.getReactions());
                item.put("isDeleted", m.getIsDeleted() != null && m.getIsDeleted());
                item.put("timestamp", m.getCreatedAt().getTime());
                
                if (m.getReplyToId() != null) {
                    messageRepository.findById(m.getReplyToId()).ifPresent(original -> {
                        item.put("replyContent", original.getContent());
                    });
                }
                response.add(item);
            }
            client.sendEvent("history_response", response);
        };
    }

    private void broadcastOnlineUsers() {
        server.getBroadcastOperations().sendEvent("update_user_list", onlineUsers.values());
    }
}
