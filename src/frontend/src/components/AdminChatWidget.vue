<template>
  <div class="admin-chat-widget shadow-lg">
    <div class="chat-header">
      <div class="header-info">
        <img v-if="selectedUser" :src="selectedUser.avatar || 'https://www.w3schools.com/howto/img_avatar.png'" class="avatar" />
        <div class="status-info">
          <span class="name">{{ selectedUser ? selectedUser.name : 'Chưa chọn khách hàng' }}</span>
          <span class="status" v-if="selectedUser"><i class="pi pi-circle-fill online-icon"></i> 1 khách hàng đang online</span>
        </div>
      </div>
      <div class="header-actions">
        <i class="pi pi-ellipsis-v"></i>
        <i class="pi pi-times" @click="$emit('close')"></i>
      </div>
    </div>
    
    <div class="user-selector p-2">
      <select v-model="selectedUserId" class="form-select form-select-sm" @change="handleUserChange">
        <option value="">-- Chọn khách hàng --</option>
        <option v-for="user in onlineUsers" :key="user.id" :value="user.id">
          {{ user.name }} ({{ user.email }})
        </option>
      </select>
    </div>

    <div class="chat-messages" ref="messageContainer">
      <div v-if="!selectedUserId" class="no-chat">
        Chưa có tin nhắn nào
      </div>
      <div v-else v-for="(msg, index) in filteredMessages" :key="index" :class="['message', msg.from === 'admin' ? 'sent' : 'received']">
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
        :disabled="!selectedUserId"
      />
      <div class="input-actions">
        <i class="pi pi-face-smile"></i>
        <button @click="sendMessage" :disabled="!newMessage.trim() || !selectedUserId">
          <i class="pi pi-send"></i>
        </button>
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

const selectedUserId = ref(props.initialUserId || '');
const newMessage = ref('');
const allMessages = ref([]);
const onlineUsers = ref([]);
const messageContainer = ref(null);
const socket = ref(null);

const selectedUser = computed(() => {
  return onlineUsers.value.find(u => u.id === selectedUserId.value);
});

const filteredMessages = computed(() => {
  if (!selectedUserId.value) return [];
  return allMessages.value.filter(m => 
    (m.from === 'admin' && m.to === selectedUserId.value) || 
    (m.from === selectedUserId.value && m.to === 'admin')
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
    timestamp: new Date().getTime()
  };

  socket.value.emit('send_message', msgData);
  newMessage.value = '';
  scrollToBottom();
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
    scrollToBottom();
  });

  socket.value.on('history_response', (history) => {
    // Merge history into allMessages (avoid duplicates if possible, or just replace for simplicity)
    const formattedHistory = history.map(m => ({
        from: m.fromUser,
        to: m.toUser,
        message: m.content,
        timestamp: m.createdAt
    }));
    
    // Replace messages for current selected user in allMessages
    // Actually, it's easier to just store current view messages if we only care about the active chat
    // But for now, let's just use it to populate the current view
    allMessages.value = formattedHistory; 
    scrollToBottom();
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
  if (newId) selectedUserId.value = newId;
});
</script>

<style scoped>
.admin-chat-widget {
  width: 350px;
  height: 500px;
  background: white;
  border-radius: 15px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  position: fixed;
  bottom: 80px;
  right: 20px;
  z-index: 1001;
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
