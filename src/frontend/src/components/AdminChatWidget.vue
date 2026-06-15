<template>
  <div class="admin-chat-container">
    <!-- Floating Toggle Button -->
    <div v-if="!isOpen" class="chat-toggle-btn shadow-lg" @click="toggleChat">
      <i class="pi pi-comments"></i>
      <span v-if="totalUnread > 0" class="badge-count">{{ totalUnread }}</span>
    </div>

    <!-- Main Chat Window -->
    <div v-else class="admin-chat-widget shadow-lg">
      <div class="chat-header" @click="toggleChat" style="cursor: pointer;">
        <div class="header-info">
          <img v-if="selectedUser" :src="selectedUser.avatar || 'https://www.w3schools.com/howto/img_avatar.png'" class="avatar" />
          <div class="status-info">
            <span class="name">{{ selectedUser ? selectedUser.name : 'CSKH MasterShop' }}</span>
            <span class="status" v-if="selectedUser"><i class="pi pi-circle-fill online-icon"></i> Đang chat</span>
          </div>
        </div>
        <div class="header-actions">
          <i class="pi pi-minus"></i>
          <i class="pi pi-times" @click.stop="() => { isOpen = false; state.selectedChatUserId = null; }"></i>
        </div>
      </div>
      
      <div class="user-selector p-2">
        <select v-model="selectedUserId" class="form-select form-select-sm" @change="handleUserChange">
          <option value="">-- Chọn khách hàng --</option>
          <option v-for="user in onlineUsers" :key="user.id" :value="user.id">
            {{ user.name }} {{ unreadPerUser[user.id] ? `(${unreadPerUser[user.id]})` : '' }}
          </option>
        </select>
      </div>

      <div class="chat-messages" ref="messageContainer">
        <div v-if="!selectedUserId" class="no-chat">
          Vui lòng chọn khách hàng để bắt đầu
        </div>
        <div v-else v-for="(msg, index) in filteredMessages" :key="index" :class="['message-wrapper', msg.from.toString() === 'admin' ? 'sent-wrapper' : 'received-wrapper']">
          <!-- Avatar for received messages (User) -->
          <img v-if="msg.from.toString() !== 'admin'" :src="selectedUser?.avatar || 'https://www.w3schools.com/howto/img_avatar.png'" class="msg-avatar" :title="selectedUser?.name" />

          <div class="message-container-inner" @mouseenter="hoveredMsg = msg.id" @mouseleave="hoveredMsg = null">
            <!-- Replied context -->
            <div v-if="msg.replyToId" class="reply-context">
               <i class="pi pi-reply"></i> {{ msg.replyContent || 'Đang trả lời...' }}
            </div>
            
            <div :class="['message', msg.from.toString() === 'admin' ? 'sent' : 'received', msg.isDeleted ? 'deleted' : '']">
              <div class="message-content">
                {{ msg.message }}
              </div>

              <!-- Action Menu -->
              <div v-if="hoveredMsg === msg.id && !msg.isDeleted" class="message-actions">
                <i class="pi pi-reply" title="Trả lời" @click="startReply(msg)"></i>
                <i class="pi pi-face-smile" title="Thả cảm xúc" @click="toggleReactions(msg.id)"></i>
                <i v-if="msg.from.toString() === 'admin'" class="pi pi-trash" title="Thu hồi" @click="recallMessage(msg.id)"></i>

                <div v-if="reactionTargetId === msg.id" class="mini-reaction-picker shadow">
                  <span v-for="e in commonEmojis" :key="e" @click="reactToMessage(msg.id, e)">{{ e }}</span>
                </div>
              </div>

              <!-- Reactions List -->
              <div v-if="msg.reactions" class="reactions-list">
                {{ msg.reactions }}
              </div>
            </div>
          </div>

          <!-- Avatar for sent messages (Admin) -->
          <img v-if="msg.from.toString() === 'admin'" src="https://www.w3schools.com/howto/img_avatar.png" class="msg-avatar" title="Admin" />
        </div>
      </div>

      <!-- Typing indicator (Đưa ra ngoài vùng cuộn) -->
      <div v-if="isTypingRemote" :class="['typing-indicator-absolute', replyingTo ? 'with-reply' : '']">
           <span class="typing-dots">Đang soạn tin nhắn</span>
      </div>
      
      <!-- Reply Preview -->
      <div v-if="replyingTo" class="reply-preview-bar">
        <div class="reply-info">
          <i class="pi pi-reply"></i> <span>Đang trả lời: {{ replyingTo.message }}</span>
        </div>
        <i class="pi pi-times" @click="replyingTo = null"></i>
      </div>

      <div class="chat-input-area">
        <div class="emoji-picker-container">
           <i class="pi pi-face-smile" @click="showEmojiPicker = !showEmojiPicker"></i>
           <div v-if="showEmojiPicker" class="emoji-picker-panel shadow">
             <span v-for="e in chatEmojis" :key="e" @click="addEmoji(e)">{{ e }}</span>
           </div>
        </div>
        <input 
          v-model="newMessage" 
          type="text" 
          placeholder="Nhập tin nhắn..." 
          @keyup.enter="sendMessage"
          :disabled="!selectedUserId"
        />
        <div class="input-actions">
          <button @click="sendMessage" :disabled="!newMessage.trim() || !selectedUserId">
            <i class="pi pi-send"></i>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick, computed, watch } from 'vue';
import { io } from 'socket.io-client';
import { state } from '../app/MyApp';

const props = defineProps(['initialUserId']);
const emit = defineEmits(['close']);

const isOpen = ref(false);
const unreadPerUser = ref({});
const totalUnread = computed(() => {
    return Object.values(unreadPerUser.value).reduce((a, b) => a + b, 0);
});

const selectedUserId = ref(props.initialUserId || '');
const newMessage = ref('');
const allMessages = ref([]);
const onlineUsers = ref([]);
const messageContainer = ref(null);
const socket = ref(null);
const isTypingRemote = ref(false);
const hoveredMsg = ref(null);
const replyingTo = ref(null);
const showEmojiPicker = ref(false);
const reactionTargetId = ref(null);

const commonEmojis = ['❤️', '👍', '😂', '😮', '😢', '😡'];
const chatEmojis = ['😊', '😂', '😍', '😭', '😡', '👍', '🙏', '❤️', '😇', '😘', '😥', '🎉', '🔥', '✨', '👌', '👏'];

let typingTimeout = null;
let remoteTypingTimeout = null;

const toggleChat = () => {
    isOpen.value = !isOpen.value;
    if (isOpen.value && selectedUserId.value) {
        unreadPerUser.value[selectedUserId.value] = 0;
        scrollToBottom();
    }
    // Reset global state if closing so clicking the same user again triggers the watch
    if (!isOpen.value) {
        state.selectedChatUserId = null;
    }
};

const selectedUser = computed(() => {
  return onlineUsers.value.find(u => u.id === selectedUserId.value);
});

const filteredMessages = computed(() => {
  if (!selectedUserId.value) return [];
  const selId = selectedUserId.value.toString();
  return allMessages.value.filter(m => 
    (m.from.toString() === 'admin' && m.to.toString() === selId) || 
    (m.from.toString() === selId && m.to.toString() === 'admin')
  );
});

const handleUserChange = () => {
  if (selectedUserId.value) {
    socket.value.emit('get_history', {
      user1: 'admin',
      user2: selectedUserId.value.toString()
    });
  }
  scrollToBottom();
};

const scrollToBottom = async () => {
  await nextTick();
  if (messageContainer.value) {
    messageContainer.value.scrollTop = messageContainer.value.scrollHeight;
  }
};

const sendMessage = () => {
  if (!newMessage.value.trim() || !selectedUserId.value) return;

  const msgData = {
    from: 'admin',
    to: selectedUserId.value.toString(),
    message: newMessage.value,
    replyToId: replyingTo.value ? replyingTo.value.id : null,
    timestamp: new Date().getTime()
  };

  socket.value.emit('send_message', msgData);
  newMessage.value = '';
  replyingTo.value = null;
  showEmojiPicker.value = false;
  scrollToBottom();
};

const startReply = (msg) => {
  replyingTo.value = msg;
};

const toggleReactions = (msgId) => {
  reactionTargetId.value = reactionTargetId.value === msgId ? null : msgId;
};

const reactToMessage = (messageId, emoji) => {
  socket.value.emit('react_message', { messageId, reactions: emoji });
  reactionTargetId.value = null;
};

const recallMessage = (messageId) => {
  if (confirm('Bạn muốn thu hồi tin nhắn này?')) {
    socket.value.emit('recall_message', { messageId });
  }
};

const addEmoji = (emoji) => {
  newMessage.value += emoji;
};

onMounted(() => {
  socket.value = io('http://localhost:9092');

  socket.value.on('connect', () => {
    socket.value.emit('login', {
      id: 'admin',
      name: state.user?.nameUser || 'Admin',
      role: 'admin'
    });
    
    // If initial user is provided, get history
    if (selectedUserId.value) {
        handleUserChange();
    }
  });

  socket.value.on('update_user_list', (users) => {
    onlineUsers.value = users.filter(u => u.role !== 'admin');
  });

  socket.value.on('receive_message', (data) => {
    allMessages.value.push(data);
    
    // Notification logic
    const fromId = data.from.toString();
    if (fromId !== 'admin') {
        if (!isOpen.value || selectedUserId.value.toString() !== fromId) {
            unreadPerUser.value[fromId] = (unreadPerUser.value[fromId] || 0) + 1;
        }
    }
    
    scrollToBottom();
  });

  socket.value.on('history_response', (history) => {
    const formattedHistory = history.map(m => ({
        id: m.id,
        from: m.from,
        to: m.to,
        message: m.message,
        replyToId: m.replyToId,
        replyContent: m.replyContent,
        reactions: m.reactions,
        isDeleted: m.isDeleted,
        timestamp: m.timestamp
    }));
    allMessages.value = formattedHistory; 
    scrollToBottom();
  });

  socket.value.on('message_updated', (updatedMsg) => {
    const idx = allMessages.value.findIndex(m => m.id === updatedMsg.id);
    if (idx !== -1) {
        allMessages.value[idx].reactions = updatedMsg.reactions;
        allMessages.value[idx].isDeleted = updatedMsg.isDeleted;
        if (updatedMsg.isDeleted) {
            allMessages.value[idx].message = "Tin nhắn đã được thu hồi";
        }
    }
  });

  socket.value.on('typing', (data) => {
    if (selectedUserId.value && data.from.toString() === selectedUserId.value.toString()) {
        isTypingRemote.value = data.isTyping;
        scrollToBottom();
        
        if (remoteTypingTimeout) clearTimeout(remoteTypingTimeout);
        if (data.isTyping) {
            remoteTypingTimeout = setTimeout(() => {
                isTypingRemote.value = false;
            }, 5000);
        }
    }
  });
});

onUnmounted(() => {
  if (socket.value) {
    socket.value.disconnect();
  }
});

watch(filteredMessages, () => {
  scrollToBottom();
}, { deep: true });

watch(() => props.initialUserId, (newId) => {
  if (newId) {
    selectedUserId.value = newId;
    isOpen.value = true;
    handleUserChange();
  }
});

watch(newMessage, (val) => {
    if (socket.value && socket.value.connected && selectedUserId.value) {
        socket.value.emit('typing', {
            from: 'admin',
            to: selectedUserId.value.toString(),
            isTyping: val.length > 0
        });

        if (typingTimeout) clearTimeout(typingTimeout);
        if (val.length > 0) {
            typingTimeout = setTimeout(() => {
                socket.value.emit('typing', {
                    from: 'admin',
                    to: selectedUserId.value.toString(),
                    isTyping: false
                });
            }, 3000);
        }
    }
});
</script>

<style scoped>
.admin-chat-container {
  position: fixed;
  bottom: 20px;
  right: 20px;
  z-index: 9999;
}

.chat-toggle-btn {
  width: 60px;
  height: 60px;
  background-color: #f1b42f;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 24px;
  cursor: pointer;
  position: relative;
  transition: transform 0.3s;
}

.chat-toggle-btn:hover {
  transform: scale(1.1);
}

.badge-count {
  position: absolute;
  top: -5px;
  right: -5px;
  background-color: #ff4757;
  color: white;
  font-size: 12px;
  width: 22px;
  height: 22px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  border: 2px solid white;
}

.admin-chat-widget {
  width: 350px;
  height: 500px;
  background: white;
  border-radius: 15px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  box-shadow: 0 10px 25px rgba(0,0,0,0.1);
}

.chat-header {
  background: #f1b42f;
  padding: 15px;
  color: white;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  border: 2px solid white;
}

.status-info {
  display: flex;
  flex-direction: column;
}

.name {
  font-weight: bold;
  font-size: 14px;
}

.status {
  font-size: 11px;
  opacity: 0.9;
  display: flex;
  align-items: center;
  gap: 4px;
}

.online-icon {
  font-size: 8px;
  color: #4cd137;
}

.header-actions i {
  margin-left: 10px;
  cursor: pointer;
  font-size: 14px;
}

.user-selector {
  border-bottom: 1px solid #eee;
}

.chat-messages {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  background-color: #f8f9fa;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.no-chat {
  text-align: center;
  color: #888;
  font-size: 13px;
  margin-top: 50%;
}

.message {
  width: fit-content;
  max-width: 100%;
  padding: 10px 15px;
  border-radius: 15px;
  font-size: 14px;
  line-height: 1.4;
  word-break: break-word;
}

.sent {
  align-self: flex-end;
  background-color: #f1b42f;
  color: white;
  border-bottom-right-radius: 2px;
}

.received {
  align-self: flex-start;
  background-color: white;
  color: #333;
  border-bottom-left-radius: 2px;
  box-shadow: 0 2px 5px rgba(0,0,0,0.05);
}

.typing-indicator-absolute {
    position: absolute;
    bottom: 65px;
    left: 20px;
    font-style: italic;
    color: #888;
    font-size: 11px;
    z-index: 10;
    pointer-events: none;
    transition: bottom 0.3s;
}

.typing-indicator-absolute.with-reply {
    bottom: 100px; /* Nhích lên khi có thanh Reply */
}

.typing-dots::after {
  content: '...';
  display: inline-block;
  width: 12px;
  animation: typing-animation 1.5s infinite;
}

@keyframes typing-animation {
  0% { content: ''; }
  33% { content: '.'; }
  66% { content: '..'; }
  100% { content: '...'; }
}

.message-wrapper {
  margin-bottom: 15px;
}

.sent-wrapper {
  display: flex;
  flex-direction: row;
  justify-content: flex-end;
  align-items: flex-end;
  gap: 8px;
}

.received-wrapper {
  display: flex;
  flex-direction: row;
  justify-content: flex-start;
  align-items: flex-end;
  gap: 8px;
}

.msg-avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  object-fit: cover;
  border: 1px solid #eee;
}


.message-container-inner {
  position: relative;
  max-width: 80%;
  display: flex;
  flex-direction: column;
}

.sent-wrapper .message-container-inner {
  align-items: flex-end;
}

.received-wrapper .message-container-inner {
  align-items: flex-start;
}

.reply-context {
  font-size: 11px;
  color: #888;
  padding: 2px 8px;
  background: rgba(0,0,0,0.05);
  border-radius: 10px;
  margin-bottom: 2px;
  max-width: 200px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.message-actions {
  position: absolute;
  top: -25px;
  right: 0;
  background: white;
  border-radius: 15px;
  padding: 2px 8px;
  display: flex;
  gap: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  z-index: 10;
}

.message-actions i {
  font-size: 12px;
  color: #666;
  cursor: pointer;
}

.message-actions i:hover {
  color: #f1b42f;
}

.mini-reaction-picker {
  position: absolute;
  top: 30px;
  right: 0;
  background: white;
  border-radius: 20px;
  padding: 5px;
  display: flex;
  gap: 5px;
  z-index: 11;
  font-size: 16px;
}

.mini-reaction-picker span {
  cursor: pointer;
  transition: transform 0.2s;
}

.mini-reaction-picker span:hover {
  transform: scale(1.3);
}

.reactions-list {
  position: absolute;
  bottom: -15px;
  right: 5px;
  background: white;
  border-radius: 10px;
  padding: 1px 4px;
  font-size: 14px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
}

.reply-preview-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #f8f9fa;
  padding: 8px 15px;
  border-top: 1px solid #eee;
  font-size: 12px;
  color: #666;
}

.reply-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.emoji-picker-container {
  position: relative;
}

.emoji-picker-container i {
  font-size: 20px;
  color: #888;
  cursor: pointer;
}

.emoji-picker-panel {
  position: absolute;
  bottom: 40px;
  left: 0;
  background: white;
  border-radius: 10px;
  padding: 10px;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 8px;
  width: 150px;
  font-size: 18px;
  z-index: 20;
}

.emoji-picker-panel span {
  cursor: pointer;
  text-align: center;
}

.emoji-picker-panel span:hover {
  transform: scale(1.2);
}

.deleted {
  opacity: 0.6;
  font-style: italic;
}

@keyframes typing-animation {
  0% { content: ''; }
  33% { content: '.'; }
  66% { content: '..'; }
  100% { content: '...'; }
}

.chat-input-area {
  padding: 15px;
  border-top: 1px solid #eee;
  display: flex;
  align-items: center;
  gap: 10px;
}

.chat-input-area input {
  flex: 1;
  border: 1px solid #ddd;
  border-radius: 20px;
  padding: 8px 15px;
  outline: none;
  font-size: 14px;
}

.input-actions button {
  background: #f1b42f;
  border: none;
  color: white;
  width: 35px;
  height: 35px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}

.input-actions button:disabled {
  background: #ccc;
}
</style>
