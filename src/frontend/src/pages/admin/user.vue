<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import axios from 'axios'
import Toast from 'primevue/toast'
import { useToast } from 'primevue/usetoast'
import Dialog from 'primevue/dialog'
import Button from 'primevue/button'
import InputText from 'primevue/inputtext'
import Dropdown from 'primevue/dropdown'
import type { UserType, UserStatus } from '../../interfaces/app'

const API_URL = 'http://localhost:8081'
const toast = useToast()

const userList = ref<any[]>([])
const totalItems = ref(0)
const totalPages = ref(0)
const loading = ref(false)

const selectedUser = ref<any>(null)
const visibleDialog = ref(false)
const editMode = ref(false)

const userTypeList = ref<UserType[]>([])
const userStatusList = ref<UserStatus[]>([])

const search = ref('')
const pageSize = ref(10)
const currentPage = ref(0)
const sortOption = ref('newest')
const pageSizeOptions = [10, 20, 50, 100, 200]

const loadUsers = async () => {
  loading.value = true
  try {
    const params: Record<string, string | number> = {
      page: currentPage.value,
      size: pageSize.value,
      sort: sortOption.value
    }
    if (search.value.trim()) {
      params.search = search.value.trim()
    }
    const res = await axios.get(`${API_URL}/user/all`, { params })
    userList.value = res.data.content || []
    totalItems.value = res.data.totalElements || 0
    totalPages.value = res.data.totalPages || 0
  } catch {
    toast.add({ severity: 'error', summary: 'Lỗi', detail: 'Không thể tải user', life: 2000 })
  } finally {
    loading.value = false
  }
}

const loadMetaData = async () => {
  try {
    const [typeRes, statusRes] = await Promise.all([
      axios.get(`${API_URL}/usertype/all`),
      axios.get(`${API_URL}/userstatus/all`)
    ])
    userTypeList.value = typeRes.data
    userStatusList.value = statusRes.data
  } catch {
    toast.add({ severity: 'error', summary: 'Lỗi', detail: 'Không thể tải UserType/UserStatus', life: 2000 })
  }
}

const formatDateTime = () => {
  const now = new Date()
  const pad = (n: number) => n.toString().padStart(2, '0')
  return `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())} ${pad(now.getHours())}:${pad(now.getMinutes())}:${pad(now.getSeconds())}`
}

const openAddDialog = () => {
  selectedUser.value = {
    id: null,
    nameUser: '',
    phone: '',
    email: '',
    address: '',
    password: '',
    verify: '',
    regtime: formatDateTime(),
    salt: Math.random().toString(36).substring(2, 8),
    userType: null,
    userStatus: null
  }
  editMode.value = false
  visibleDialog.value = true
}

const openEditDialog = (user: any) => {
  selectedUser.value = {
    ...user,
    password: '',
    regtime: formatDateTime(),
    salt: Math.random().toString(36).substring(2, 8)
  }
  editMode.value = true
  visibleDialog.value = true
}

const deleteUser = async (id: number) => {
  if (!confirm('Bạn có chắc chắn muốn xóa user này?')) return
  try {
    await axios.delete(`${API_URL}/delete/${id}`)
    toast.add({ severity: 'success', summary: 'Thành công', detail: 'Đã xóa user', life: 1500 })
    loadUsers()
  } catch {
    toast.add({ severity: 'error', summary: 'Lỗi', detail: 'Xóa thất bại', life: 2000 })
  }
}

const saveUser = async () => {
  try {
    if (!selectedUser.value.nameUser) {
      toast.add({ severity: 'warn', summary: 'Thiếu thông tin', detail: 'Vui lòng nhập tên người dùng!', life: 2000 })
      return
    }
    if (!selectedUser.value.email) {
      toast.add({ severity: 'warn', summary: 'Thiếu thông tin', detail: 'Vui lòng nhập email!', life: 2000 })
      return
    }
    if (!selectedUser.value.password) {
      toast.add({ severity: 'warn', summary: 'Thiếu thông tin', detail: 'Vui lòng nhập password!', life: 2000 })
      return
    }
    if (!selectedUser.value.userType) {
      toast.add({ severity: 'warn', summary: 'Thiếu thông tin', detail: 'Vui lòng chọn loại người dùng!', life: 2000 })
      return
    }
    if (!selectedUser.value.userStatus) {
      toast.add({ severity: 'warn', summary: 'Thiếu thông tin', detail: 'Vui lòng chọn trạng thái!', life: 2000 })
      return
    }

    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
    if (!emailRegex.test(selectedUser.value.email)) {
      toast.add({
        severity: 'error',
        summary: 'Email không hợp lệ',
        detail: 'Vui lòng nhập đúng định dạng email (vd: example@gmail.com)',
        life: 2500
      })
      return
    }

    const payload = {
      nameUser: selectedUser.value.nameUser,
      phone: selectedUser.value.phone,
      email: selectedUser.value.email,
      address: selectedUser.value.address,
      password: selectedUser.value.password,
      verify: selectedUser.value.verify,
      regtime: selectedUser.value.regtime,
      salt: selectedUser.value.salt,
      userType: selectedUser.value.userType ? { id: selectedUser.value.userType.id } : null,
      userStatus: selectedUser.value.userStatus ? { id: selectedUser.value.userStatus.id } : null
    }

    if (editMode.value) {
      await axios.put(`${API_URL}/update/${selectedUser.value.id}`, payload)
      toast.add({ severity: 'success', summary: 'Cập nhật', detail: 'Sửa user thành công', life: 1500 })
    } else {
      await axios.post(`${API_URL}/add`, payload)
      toast.add({ severity: 'success', summary: 'Thêm mới', detail: 'Thêm user thành công', life: 1500 })
    }

    visibleDialog.value = false
    loadUsers()
  } catch (err) {
    console.error('Save error:', err)
    toast.add({ severity: 'error', summary: 'Lỗi', detail: 'Không thể lưu user', life: 2000 })
  }
}

const exportExcel = async () => {
  try {
    const res = await axios.get(`${API_URL}/user/export-excel`, { responseType: 'blob' })
    const url = window.URL.createObjectURL(new Blob([res.data]))
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', 'users.xlsx')
    document.body.appendChild(link)
    link.click()
    link.remove()
    window.URL.revokeObjectURL(url)
    toast.add({ severity: 'success', summary: 'Thành công', detail: 'Đã tải file Excel', life: 2000 })
  } catch {
    toast.add({ severity: 'error', summary: 'Lỗi', detail: 'Xuất Excel thất bại', life: 2000 })
  }
}

const pages = computed(() => {
  const arr: number[] = []
  const max = 5
  let start = Math.max(0, currentPage.value - 2)
  let end = Math.min(totalPages.value, start + max)
  if (end - start < max) start = Math.max(0, end - max)
  for (let i = start; i < end; i++) arr.push(i)
  return arr
})

const goToPage = (p: number) => {
  if (p >= 0 && p < totalPages.value) currentPage.value = p
}

let timeout: ReturnType<typeof setTimeout> | null = null
watch(search, () => {
  if (timeout) clearTimeout(timeout)
  timeout = setTimeout(() => {
    currentPage.value = 0
    loadUsers()
  }, 400)
})
watch([pageSize, sortOption], () => {
  currentPage.value = 0
  loadUsers()
})
watch(currentPage, () => loadUsers())

onMounted(() => {
  loadUsers()
  loadMetaData()
})
</script>

<template>
  <div class="u-page">
    <Toast />

    <div class="u-header">
      <div class="u-header-left">
        <i class="fas fa-users u-icon"></i>
        <h2>Quản lý Người dùng</h2>
      </div>
      <div class="u-header-actions">
        <button class="u-btn u-btn-add" @click="openAddDialog">
          <i class="fas fa-plus"></i> Thêm người dùng
        </button>
        <button class="u-btn u-btn-export" @click="exportExcel">
          <i class="fas fa-file-excel"></i> Xuất Excel
        </button>
      </div>
    </div>

    <div class="u-toolbar">
      <div class="u-toolbar-left">
        <div class="u-select-group">
          <label>Hiển thị:</label>
          <select v-model="pageSize">
            <option v-for="o in pageSizeOptions" :key="o" :value="o">{{ o }}</option>
          </select>
        </div>
        <div class="u-select-group">
          <label>Sắp xếp:</label>
          <select v-model="sortOption">
            <option value="newest">Mới nhất</option>
            <option value="oldest">Cũ nhất</option>
            <option value="az">A-Z</option>
            <option value="za">Z-A</option>
          </select>
        </div>
      </div>
      <div class="u-toolbar-right">
        <div class="u-search-box">
          <i class="fas fa-search"></i>
          <input type="text" v-model="search" placeholder="Tìm theo tên, email, SĐT...">
        </div>
      </div>
    </div>

    <div class="u-table-wrapper">
      <table class="u-table">
        <thead>
          <tr>
            <th style="width:60px">STT</th>
            <th>Tên</th>
            <th style="width:130px">SĐT</th>
            <th>Email</th>
            <th>Địa chỉ</th>
            <th style="width:150px">Loại tài khoản</th>
            <th style="width:130px">Trạng thái</th>
            <th style="width:170px">Thời gian</th>
            <th style="width:120px">Thao tác</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td colspan="9" class="text-center py-5"><i class="fas fa-spinner fa-spin"></i> Đang tải...</td>
          </tr>
          <tr v-else-if="userList.length === 0">
            <td colspan="9" class="text-center py-5">Không tìm thấy dữ liệu</td>
          </tr>
          <tr v-for="(user, idx) in userList" :key="user.id" class="u-row">
            <td class="text-center">{{ currentPage * pageSize + idx + 1 }}</td>
            <td><span class="u-badge">{{ user.nameUser }}</span></td>
            <td>{{ user.phone || '—' }}</td>
            <td>{{ user.email }}</td>
            <td>{{ user.address || '—' }}</td>
            <td>{{ user.userType?.name || '—' }}</td>
            <td>{{ user.userStatus?.name || '—' }}</td>
            <td class="text-center">{{ user.regtime || '—' }}</td>
            <td class="text-center">
              <div class="u-actions">
                <button class="u-action-btn u-edit" @click="openEditDialog(user)"><i class="fas fa-edit"></i></button>
                <button class="u-action-btn u-delete" @click="deleteUser(user.id)"><i class="fas fa-trash"></i></button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div class="u-pagination-bar" v-if="totalPages > 0">
      <div>
        Hiển thị {{ currentPage * pageSize + 1 }} -
        {{ Math.min((currentPage + 1) * pageSize, totalItems) }} / {{ totalItems }} mục
      </div>
      <div class="u-pagination">
        <button class="u-page-btn" :disabled="currentPage === 0" @click="goToPage(currentPage - 1)">
          <i class="fas fa-chevron-left"></i>
        </button>
        <button
          v-for="p in pages"
          :key="p"
          class="u-page-btn"
          :class="{ 'u-page-active': p === currentPage }"
          @click="goToPage(p)"
        >{{ p + 1 }}</button>
        <button class="u-page-btn" :disabled="currentPage >= totalPages - 1" @click="goToPage(currentPage + 1)">
          <i class="fas fa-chevron-right"></i>
        </button>
      </div>
    </div>

    <Dialog
      v-model:visible="visibleDialog"
      :header="editMode ? '✏️ Sửa người dùng' : '🆕 Thêm người dùng'"
      modal
      class="u-dialog"
      :style="{ width: '600px' }"
    >
      <div class="u-form">
        <InputText v-model="selectedUser.nameUser" placeholder="Tên user" class="u-input" />
        <InputText v-model="selectedUser.phone" placeholder="Số điện thoại" class="u-input" />
        <InputText v-model="selectedUser.email" placeholder="Email" type="email" class="u-input" />
        <InputText v-model="selectedUser.salt" placeholder="Salt" class="u-input" />
        <InputText v-model="selectedUser.password" placeholder="Mật khẩu" type="password" class="u-input" />
        <InputText v-model="selectedUser.regtime" placeholder="Regtime (YYYY-MM-DD HH:mm:ss)" class="u-input" />
        <InputText v-model="selectedUser.address" placeholder="Địa chỉ" class="u-input" />
        <Dropdown
          v-model="selectedUser.userType"
          :options="userTypeList"
          optionLabel="name"
          placeholder="Chọn loại tài khoản"
          class="u-input"
        />
        <Dropdown
          v-model="selectedUser.userStatus"
          :options="userStatusList"
          optionLabel="name"
          placeholder="Chọn trạng thái"
          class="u-input"
        />
      </div>
      <template #footer>
        <div class="u-dialog-footer">
          <Button label="Hủy" icon="pi pi-times" class="p-button-text p-button-secondary" @click="visibleDialog = false" />
          <Button label="Lưu" icon="pi pi-check" class="p-button-primary u-save-btn" @click="saveUser" />
        </div>
      </template>
    </Dialog>
  </div>
</template>

<style scoped>
.u-page { padding: 24px; font-family: 'Inter', sans-serif; }
.u-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; border-bottom: 2px solid #f1f5f9; padding-bottom: 16px; }
.u-header-left { display: flex; align-items: center; gap: 12px; }
.u-icon { font-size: 28px; color: #f59e0b; }
.u-header-left h2 { margin: 0; font-size: 22px; font-weight: 700; color: #1e293b; }

.u-btn { display: flex; align-items: center; gap: 8px; padding: 10px 18px; border: none; border-radius: 8px; font-weight: 600; cursor: pointer; transition: 0.2s; }
.u-btn-add { background: #f59e0b; color: white; }
.u-btn-export { background: #10b981; color: white; margin-left: 10px; }
.u-header-actions { display: flex; }

.u-toolbar { display: flex; justify-content: space-between; align-items: center; background: #f8fafc; padding: 12px 16px; border-radius: 12px; margin-bottom: 16px; border: 1px solid #e2e8f0; }
.u-toolbar-left { display: flex; gap: 20px; }
.u-select-group { display: flex; align-items: center; gap: 8px; font-size: 14px; }
.u-select-group select { padding: 6px 12px; border-radius: 6px; border: 1px solid #cbd5e1; outline: none; }
.u-search-box { display: flex; align-items: center; gap: 8px; background: white; border: 1px solid #cbd5e1; padding: 8px 16px; border-radius: 8px; width: 320px; }
.u-search-box input { border: none; outline: none; flex: 1; font-size: 14px; }

.u-table-wrapper { border-radius: 12px; overflow: hidden; border: 1px solid #e2e8f0; box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1); }
.u-table { width: 100%; border-collapse: collapse; background: white; }
.u-table thead { background: #f1f5f9; }
.u-table th { padding: 14px 16px; text-align: left; font-size: 13px; font-weight: 600; color: #475569; text-transform: uppercase; }
.u-table td { padding: 14px 16px; border-top: 1px solid #f1f5f9; color: #1e293b; font-size: 14px; }
.u-row:hover { background: #f8fafc; }
.u-badge { background: #fef3c7; color: #92400e; padding: 4px 10px; border-radius: 6px; font-weight: 600; font-size: 12px; }

.u-actions { display: flex; gap: 8px; justify-content: center; }
.u-action-btn { width: 32px; height: 32px; border: none; border-radius: 6px; cursor: pointer; transition: 0.2s; display: flex; align-items: center; justify-content: center; font-size: 14px; }
.u-edit { background: #fef3c7; color: #92400e; }
.u-delete { background: #fee2e2; color: #991b1b; }
.u-edit:hover { background: #fde68a; }
.u-delete:hover { background: #fecaca; }

.u-pagination-bar { display: flex; justify-content: space-between; align-items: center; margin-top: 20px; font-size: 14px; color: #64748b; }
.u-pagination { display: flex; gap: 6px; }
.u-page-btn { padding: 6px 12px; border: 1px solid #e2e8f0; border-radius: 6px; background: white; cursor: pointer; }
.u-page-btn:disabled { opacity: 0.5; cursor: not-allowed; }
.u-page-active { background: #f59e0b; color: white; border-color: #f59e0b; }
.text-center { text-align: center; }
.py-5 { padding: 20px 0; }

.u-form { display: flex; flex-direction: column; gap: 12px; padding: 10px 0; }
.u-input { width: 100%; border-radius: 8px; }
.u-dialog-footer { display: flex; justify-content: flex-end; gap: 10px; }
.u-save-btn { border-radius: 8px; padding: 8px 20px; }
</style>
