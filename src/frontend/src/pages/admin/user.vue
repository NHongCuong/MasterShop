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
import Helper from '../../utils/Helper'

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

// Import State
const importFileRef = ref<HTMLInputElement | null>(null)
const importPreviewList = ref<any[]>([])
const visibleImportDialog = ref(false)
const isConfirmingImport = ref(false)

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

const formatDisplayDate = (value?: string | Date | null) => {
  if (!value) return '—'
  return new Date(value).toLocaleString('vi-VN')
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
    userType: null,
    userStatus: null
  }
  editMode.value = false
  visibleDialog.value = true
}

const openEditDialog = (user: any) => {
  selectedUser.value = {
    ...user,
    password: ''
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
    if (!editMode.value && !selectedUser.value.password) {
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
  } catch (err: any) {
    console.error('Save error:', err)
    toast.add({ severity: 'error', summary: 'Lỗi', detail: err.response?.data || 'Không thể lưu user', life: 2000 })
  }
}

const exportExcel = async () => {
  try {
    const res = await axios.get(`${API_URL}/user/export-excel`, { responseType: 'blob' })
    const url = window.URL.createObjectURL(res.data)
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', 'users.xlsx')
    document.body.appendChild(link)
    link.click()
    link.remove()
    toast.add({ severity: 'success', summary: 'Thành công', detail: 'Đã tải file Excel', life: 2000 })
  } catch {
    toast.add({ severity: 'error', summary: 'Lỗi', detail: 'Xuất Excel thất bại', life: 2000 })
  }
}

// Import logic
const triggerImportFile = () => importFileRef.value?.click()
const onImportFileSelect = async (event: Event) => {
  const input = event.target as HTMLInputElement
  if (!input.files || input.files.length === 0) return
  const file = input.files[0]
  const formData = new FormData()
  formData.append('file', file)
  loading.value = true
  try {
    const res = await axios.post(`${API_URL}/user/preview-import`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    importPreviewList.value = res.data
    visibleImportDialog.value = true
  } catch (err: any) {
    toast.add({ severity: 'error', summary: 'Lỗi', detail: err.response?.data || 'Không thể đọc file Excel', life: 3000 })
  } finally {
    loading.value = false
    input.value = ''
  }
}
const confirmImport = async () => {
  isConfirmingImport.value = true
  try {
    const res = await axios.post(`${API_URL}/user/confirm-import`, importPreviewList.value)
    toast.add({ severity: 'success', summary: 'Thành công', detail: res.data, life: 2500 })
    visibleImportDialog.value = false
    loadUsers()
  } catch (err: any) {
    toast.add({ severity: 'error', summary: 'Lỗi', detail: err.response?.data || 'Import thất bại', life: 3000 })
  } finally {
    isConfirmingImport.value = false
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
        <button class="u-btn u-btn-import" @click="triggerImportFile">
          <i class="fas fa-file-import"></i> Nhập Excel
        </button>
        <input type="file" ref="importFileRef" hidden accept=".xlsx, .xls" @change="onImportFileSelect" />
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
            <th style="width:170px">Ngày tạo</th>
            <th style="width:170px">Ngày sửa</th>
            <th style="width:120px">Thao tác</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td colspan="10" class="text-center py-5"><i class="fas fa-spinner fa-spin"></i> Đang tải...</td>
          </tr>
          <tr v-else-if="userList.length === 0">
            <td colspan="10" class="text-center py-5">Không tìm thấy dữ liệu</td>
          </tr>
          <tr v-for="(user, idx) in userList" :key="user.id" class="u-row">
            <td class="text-center">{{ currentPage * pageSize + idx + 1 }}</td>
            <td><span class="u-badge">{{ user.nameUser }}</span></td>
            <td>{{ user.phone || '—' }}</td>
            <td>{{ user.email }}</td>
            <td>{{ user.address || '—' }}</td>
            <td>{{ user.userType?.name || '—' }}</td>
            <td>{{ user.userStatus?.name || '—' }}</td>
            <td class="text-center">{{ formatDisplayDate(user.createdAt) }}</td>
            <td class="text-center">{{ formatDisplayDate(user.updatedAt) }}</td>
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

    <!-- DIALOG THÊM / SỬA USER -->
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
        <InputText
          v-model="selectedUser.password"
          :placeholder="editMode ? 'Mật khẩu mới (để trống nếu không đổi)' : 'Mật khẩu'"
          type="password"
          class="u-input"
        />
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

    <!-- DIALOG REVIEW IMPORT -->
    <Dialog v-model:visible="visibleImportDialog" modal class="p-import-dialog" :style="{ width: '85vw' }" header="📥 Nhập người dùng từ Excel">
        <div class="p-import-review">
            <div class="p-info-bar">
                <i class="fas fa-info-circle"></i>
                <span>Xem trước <b>{{ importPreviewList.length }}</b> người dùng sẽ được nhập. Kiểm tra dữ liệu trước khi xác nhận.</span>
            </div>
            
            <div class="p-import-table-wrapper">
                <table class="p-import-table">
                    <thead>
                        <tr>
                            <th style="width: 50px">STT</th>
                            <th>TÊN NGƯỜI DÙNG</th>
                            <th style="width: 150px">SĐT</th>
                            <th>EMAIL</th>
                            <th>ĐỊA CHỈ</th>
                            <th style="width: 150px">LOẠI TÀI KHOẢN</th>
                            <th style="width: 150px">TRẠNG THÁI</th>
                            <th style="width: 150px">MẬT KHẨU</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr v-for="(u, idx) in importPreviewList" :key="idx">
                            <td class="text-center font-bold">{{ idx + 1 }}</td>
                            <td class="p-import-name">{{ u.nameUser }}</td>
                            <td>{{ u.phone }}</td>
                            <td>{{ u.email }}</td>
                            <td>{{ u.address }}</td>
                            <td>
                                <span v-if="u.userType?.id" class="p-import-tag-success">{{ u.userType.name }}</span>
                                <span v-else class="p-import-tag-error">{{ u.userType?.name || 'Mặc định' }}</span>
                            </td>
                            <td>
                                <span v-if="u.userStatus?.id" class="p-import-tag-success">{{ u.userStatus.name }}</span>
                                <span v-else class="p-import-tag-error">{{ u.userStatus?.name || 'Mặc định' }}</span>
                            </td>
                            <td class="text-gray-500 italic">******</td>
                        </tr>
                    </tbody>
                </table>
            </div>
        </div>
        
        <template #footer>
            <div class="p-modal-footer">
                <button class="p-footer-btn p-btn-cancel" @click="visibleImportDialog = false" :disabled="isConfirmingImport">
                    <i class="fas fa-times mr-2"></i>Hủy bỏ
                </button>
                <button class="p-footer-btn p-btn-confirm-import" @click="confirmImport" :disabled="isConfirmingImport">
                    <template v-if="isConfirmingImport">
                        <i class="fas fa-spinner fa-spin mr-2"></i>Đang nhập...
                    </template>
                    <template v-else>
                        <i class="fas fa-check-circle mr-2"></i>Xác nhận nhập {{ importPreviewList.length }} người dùng
                    </template>
                </button>
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

.u-btn { display: flex; align-items: center; gap: 8px; padding: 10px 18px; border: none; border-radius: 8px; font-weight: 600; cursor: pointer; transition: 0.2s; color: white; }
.u-btn-add { background: #f59e0b; box-shadow: 0 4px 6px -1px rgba(245, 158, 11, 0.3); }
.u-btn-import { background: #6366f1; box-shadow: 0 4px 6px -1px rgba(99, 102, 241, 0.3); margin-left: 10px; }
.u-btn-export { background: #10b981; box-shadow: 0 4px 6px -1px rgba(16, 185, 129, 0.3); margin-left: 10px; }
.u-btn:hover { transform: translateY(-2px); opacity: 0.9; }
.u-header-actions { display: flex; }

.u-toolbar { display: flex; justify-content: space-between; align-items: center; background: white; padding: 16px; border-radius: 12px; margin-bottom: 16px; border: 1px solid #e2e8f0; box-shadow: 0 1px 3px rgba(0,0,0,0.05); }
.u-toolbar-left { display: flex; gap: 20px; }
.u-select-group { display: flex; align-items: center; gap: 8px; font-size: 14px; color: #64748b; }
.u-select-group select { padding: 6px 12px; border-radius: 6px; border: 1px solid #cbd5e1; outline: none; background: #fff; cursor: pointer; }
.u-search-box { display: flex; align-items: center; gap: 8px; background: #f8fafc; border: 1px solid #cbd5e1; padding: 8px 16px; border-radius: 8px; width: 320px; transition: 0.2s; }
.u-search-box:focus-within { border-color: #3b82f6; background: white; box-shadow: 0 0 0 4px rgba(59, 130, 246, 0.1); }
.u-search-box input { border: none; outline: none; flex: 1; font-size: 14px; background: transparent; }

.u-table-wrapper { border-radius: 12px; overflow-x: auto; overflow-y: hidden; border: 1px solid #e2e8f0; background: white; }
.u-table { width: 100%; min-width: 1180px; border-collapse: collapse; }
.u-table thead { background: #f8fafc; border-bottom: 2px solid #f1f5f9; }
.u-table th { padding: 14px 16px; text-align: left; font-size: 12px; font-weight: 700; color: #64748b; text-transform: uppercase; letter-spacing: 0.025em; }
.u-table td { padding: 14px 16px; border-bottom: 1px solid #f1f5f9; color: #334155; font-size: 14px; }
.u-table th, .u-table td { white-space: nowrap; }
.u-row:hover { background: #f8fafc; }
.u-badge { background: #eff6ff; color: #1d4ed8; padding: 4px 10px; border-radius: 6px; font-weight: 600; font-size: 13px; border: 1px solid #dbeafe; }

.u-actions { display: flex; gap: 8px; justify-content: center; }
.u-action-btn { width: 32px; height: 32px; border: none; border-radius: 6px; cursor: pointer; transition: 0.2s; display: flex; align-items: center; justify-content: center; font-size: 14px; }
.u-edit { background: #fef3c7; color: #92400e; }
.u-delete { background: #fee2e2; color: #991b1b; }
.u-edit:hover { background: #fde68a; box-shadow: 0 4px 6px -1px rgba(245, 158, 11, 0.2); }
.u-delete:hover { background: #fecaca; box-shadow: 0 4px 6px -1px rgba(239, 68, 68, 0.2); }

.u-pagination-bar { display: flex; justify-content: space-between; align-items: center; margin-top: 20px; font-size: 14px; color: #64748b; }
.u-pagination { display: flex; gap: 6px; }
.u-page-btn { width: 34px; height: 34px; border: 1px solid #e2e8f0; border-radius: 8px; background: white; cursor: pointer; display: flex; align-items: center; justify-content: center; color: #475569; transition: 0.2s; }
.u-page-btn:hover:not(:disabled) { border-color: #f59e0b; color: #f59e0b; }
.u-page-btn:disabled { opacity: 0.5; cursor: not-allowed; }
.u-page-active { background: #f59e0b; color: white; border-color: #f59e0b; font-weight: 700; }

.text-center { text-align: center; }
.py-5 { padding: 40px 0; }

.u-form { display: flex; flex-direction: column; gap: 14px; padding: 10px 0; }
.u-input { width: 100%; }
.u-dialog-footer { display: flex; justify-content: flex-end; gap: 12px; }
.u-save-btn { border-radius: 8px; padding: 10px 24px; font-weight: 700; }

/* IMPORT DIALOG STYLES */
.p-import-dialog { border-radius: 12px; overflow: hidden; }
.p-import-review { padding: 12px 24px 24px; }
.p-info-bar { background: #eff6ff; border: 1px solid #bfdbfe; color: #1e40af; padding: 12px 20px; border-radius: 10px; margin-bottom: 20px; display: flex; align-items: center; gap: 12px; font-size: 14px; }
.p-info-bar i { font-size: 18px; }
.p-import-table-wrapper { border: 1px solid #e2e8f0; border-radius: 12px; overflow-x: auto; max-height: 500px; }
.p-import-table { width: 100%; border-collapse: collapse; background: #fff; }
.p-import-table th { position: sticky; top: 0; background: #f8fafc; padding: 12px 16px; text-align: left; font-size: 12px; font-weight: 700; color: #64748b; text-transform: uppercase; border-bottom: 2px solid #e2e8f0; white-space: nowrap; }
.p-import-table td { padding: 12px 16px; border-bottom: 1px solid #f1f5f9; font-size: 13px; color: #334155; }
.p-import-name { font-weight: 600; color: #1e293b; min-width: 200px; }
.p-import-tag-success { background: #f0fdf4; color: #15803d; padding: 2px 8px; border-radius: 6px; font-weight: 600; font-size: 11px; border: 1px solid #bbf7d0; }
.p-import-tag-error { background: #fff1f2; color: #be123c; padding: 2px 8px; border-radius: 6px; font-weight: 600; font-size: 11px; border: 1px solid #fecdd3; }
.p-modal-footer { display: flex; justify-content: flex-end; gap: 12px; padding: 12px 24px; background: #f8fafc; border-top: 1px solid #e2e8f0; }
.p-footer-btn { display: flex; align-items: center; gap: 8px; padding: 10px 20px; border: none; border-radius: 8px; font-weight: 700; cursor: pointer; transition: 0.2s; }
.p-btn-cancel { background: #f1f5f9; color: #475569; }
.p-btn-cancel:hover { background: #e2e8f0; }
.p-btn-confirm-import { background: linear-gradient(135deg, #f59e0b, #d97706); color: #fff; box-shadow: 0 4px 6px -1px rgba(245, 158, 11, 0.3); }
.p-btn-confirm-import:hover { transform: translateY(-2px); box-shadow: 0 10px 15px -3px rgba(245, 158, 11, 0.4); }
</style>
