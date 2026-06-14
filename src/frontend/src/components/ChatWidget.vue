<template>
  <div class="chat-widget">
    <div v-if="!isOpen" class="chat-button" @click="toggleChat">
      <i class="pi pi-comments"></i>
    </div>
    
    <div v-else class="chat-window shadow-lg">
      <div class="chat-header">
        <div class="header-info">
          <img src="https://www.w3schools.com/howto/img_avatar.png" alt="Admin" class="avatar" />
          <div class="status-info">
            <span class="name">Hỗ trợ khách hàng</span>
            <span class="status"><i class="pi pi-circle-fill online-icon"></i> Trực tuyến</span>
          </div>
        </div>
        <div class="header-actions">
          <i class="pi pi-minus" @click="toggleChat"></i>
          <i class="pi pi-times" @click="toggleChat"></i>
        </div>
      </div>
      
      <div class="chat-messages" ref="messageContainer">
        <div class="welcome-msg">
          👋 Xin chào! Chúng tôi có thể giúp gì cho bạn?
        </div>
        <div v-for="(msg, index) in messages" :key="index" :class="['message', msg.from.toString() === (state.user?.id || '').toString() ? 'sent' : 'received']">
          <div class="message-content">
            {{ msg.message }}
          </div>
        </div>
      </div>
      
      <div class="chat-input-area">
        <input 
          v-model="newMessage" 
          type="text" 
          placeholder="Nhập tin nhắn..." 
          @keyup.enter="sendMessage"
        />
        <div class="input-actions">
          <i class="pi pi-face-smile"></i>
          <button @click="sendMessage" :disabled="!newMessage.trim()">
            <i class="pi pi-send"></i>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick, watch } from 'vue';
import { io } from 'socket.io-client';
import { state } from '../app/MyApp';

const isOpen = ref(false);
const newMessage = ref('');
const messages = ref([]);
const messageContainer = ref(null);
const socket = ref(null);

const toggleChat = () => {
  isOpen.value = !isOpen.value;
  if (isOpen.value) {
    scrollToBottom();
  }
};

const scrollToBottom = async () => {
  await nextTick();
  if (messageContainer.value) {
    messageContainer.value.scrollTop = messageContainer.value.scrollHeight;
  }
};

const sendMessage = () => {
  if (!newMessage.value.trim()) return;

  const msgData = {
    from: (state.user?.id || 'guest-' + Math.random().toString(36).substr(2, 9)).toString(),
    to: 'admin',
    message: newMessage.value,
    timestamp: new Date().getTime()
  };

  socket.value.emit('send_message', msgData);
  newMessage.value = '';
  scrollToBottom();
};

const loginToSocket = () => {
    if (state.isAuthenticated && state.user && socket.value?.connected) {
        socket.value.emit('login', {
            id: state.user.id.toString(),
            name: state.user.nameUser,
            email: state.user.email,
            phone: state.user.phone,
            avatar: state.user.avatar,
            address: state.user.address,
            createdAt: state.user.createdAt,
            role: state.user.userType?.name || 'user'
        });
    }
};

onMounted(() => {
  socket.value = io('http://localhost:9092');

  socket.value.on('connect', () => {
    loginToSocket();
    
    // Request history
    if (state.user?.id) {
        socket.value.emit('get_history', {
            user1: state.user.id.toString(),
            user2: 'admin'
        });
    }
  });

  socket.value.on('receive_message', (data) => {
    messages.value.push(data);
    scrollToBottom();
  });

  socket.value.on('history_response', (history) => {
    messages.value = history.map(m => ({
        from: m.fromUser,
        to: m.toUser,
        message: m.content,
        timestamp: m.createdAt
    }));
    scrollToBottom();
  });
});

watch(() => state.user, (newVal) => {
    if (newVal && state.isAuthenticated) {
        loginToSocket();
    }
}, { immediate: true });

onUnmounted(() => {
  if (socket.value) {
    socket.value.disconnect();
  }
});

watch(messages, () => {
  scrollToBottom();
}, { deep: true });
</script>

<style scoped>
.chat-widget {
  position: fixed;
  bottom: 100px;
  right: 24px;
  z-index: 9999;
  font-family: 'Inter', sans-serif;
}

.chat-button {
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
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
  transition: transform 0.3s;
}

.chat-button:hover {
  transform: scale(1.1);
}

.chat-window {
  width: 350px;
  height: 500px;
  background: white;
  border-radius: 15px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
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

.chat-messages {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  background-color: #f8f9fa;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.welcome-msg {
  text-align: center;
  color: #888;
  font-size: 13px;
  margin-bottom: 20px;
}

.message {
  max-width: 80%;
  padding: 10px 15px;
  border-radius: 15px;
  font-size: 14px;
  line-height: 1.4;
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

.input-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.input-actions i {
  color: #888;
  font-size: 20px;
  cursor: pointer;
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
