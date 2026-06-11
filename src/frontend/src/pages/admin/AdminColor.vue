<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import axios from 'axios'
import Toast from 'primevue/toast'
import { useToast } from 'primevue/usetoast'
import Dialog from 'primevue/dialog'
import Button from 'primevue/button'
import InputText from 'primevue/inputtext'

const toast = useToast()
const API_URL = 'http://localhost:8081/color'

// Data
const items = ref<any[]>([])
const totalItems = ref(0)
const totalPages = ref(0)
const loading = ref(false)

// Dialog state
const displayDialog = ref(false)
const isEdit = ref(false)
const currentItem = ref({ id: null, nameColor: '' })

// Import state
const importFileRef = ref<HTMLInputElement | null>(null)
const importPreviewList = ref<any[]>([])
const visibleImportDialog = ref(false)
const isConfirmingImport = ref(false)

// Filters
const search = ref('')
const pageSize = ref(10)
const currentPage = ref(0)
const sortOption = ref('newest')
const pageSizeOptions = [10, 20, 50, 100, 200]

// Load data
const loadData = async () => {
  loading.value = true
  try {
    const params: any = {
      page: currentPage.value,
      size: pageSize.value,
      sort: sortOption.value
    }
    if (search.value.trim()) {
      params.search = search.value.trim()
    }
    const res = await axios.get(`${API_URL}/all`, { params })
    items.value = res.data.content || []
    totalItems.value = res.data.totalElements || 0
    totalPages.value = res.data.totalPages || 0
  } catch (err) {
    console.error(err)
    toast.add({ severity: 'error', summary: 'Lỗi', detail: 'Tải danh sách thất bại', life: 3000 })
  } finally {
    loading.value = false
  }
}

// Add / Edit
const openAdd = () => {
  currentItem.value = { id: null, nameColor: '' }
  isEdit.value = false
  displayDialog.value = true
}

const openEdit = (item: any) => {
  currentItem.value = { id: item.id, nameColor: item.nameColor }
  isEdit.value = true
  displayDialog.value = true
}

const saveItem = async () => {
  if (!currentItem.value.nameColor.trim()) {
    toast.add({ severity: 'warn', summary: 'Cảnh báo', detail: 'Vui lòng nhập tên màu', life: 2000 })
    return
  }

  try {
    if (isEdit.value) {
      await axios.put(`${API_URL}/update/${currentItem.value.id}`, currentItem.value)
      toast.add({ severity: 'success', summary: 'Thành công', detail: 'Đã cập nhật', life: 2000 })
    } else {
      await axios.post(`${API_URL}/add`, currentItem.value)
      toast.add({ severity: 'success', summary: 'Thành công', detail: 'Đã thêm mới', life: 2000 })
    }
    displayDialog.value = false
    loadData()
  } catch (err: any) {
    const msg = err.response?.data || 'Lưu thất bại'
    toast.add({ severity: 'error', summary: 'Lỗi', detail: msg, life: 3000 })
  }
}

// Delete
const deleteItem = async (id: number) => {
  if (!confirm('Bạn có chắc muốn xóa màu này?')) return
  try {
    await axios.delete(`${API_URL}/delete/${id}`)
    toast.add({ severity: 'success', summary: 'Thành công', detail: 'Đã xóa', life: 2000 })
    loadData()
  } catch (err) {
    toast.add({ severity: 'error', summary: 'Lỗi', detail: 'Xóa thất bại', life: 2000 })
  }
}

// Export Excel
const exportExcel = async () => {
  try {
    const res = await axios.get(`${API_URL}/export-excel`, { responseType: 'blob' })
    const url = window.URL.createObjectURL(new Blob([res.data]))
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', 'colors.xlsx')
    document.body.appendChild(link)
    link.click()
    link.remove()
    window.URL.revokeObjectURL(url)
    toast.add({ severity: 'success', summary: 'Thành công', detail: 'Đã tải file Excel', life: 2000 })
  } catch (err) {
    toast.add({ severity: 'error', summary: 'Lỗi', detail: 'Xuất Excel thất bại', life: 2000 })
  }
}

// Import Excel logic
const triggerImportFile = () => importFileRef.value?.click()
const onImportFileSelect = async (event: Event) => {
    const input = event.target as HTMLInputElement
    if (!input.files || input.files.length === 0) return
    const file = input.files[0]
    const formData = new FormData()
    formData.append('file', file)
    loading.value = true
    try {
        const res = await axios.post(`${API_URL}/preview-import`, formData, {
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
        const res = await axios.post(`${API_URL}/confirm-import`, importPreviewList.value)
        toast.add({ severity: 'success', summary: 'Thành công', detail: res.data, life: 2500 })
        visibleImportDialog.value = false
        loadData()
    } catch (err: any) {
        toast.add({ severity: 'error', summary: 'Lỗi', detail: err.response?.data || 'Import thất bại', life: 3000 })
    } finally {
        isConfirmingImport.value = false
    }
}

// Format date
const formatDate = (date: string | null) => {
  if (!date) return '—'
  return new Date(date).toLocaleDateString('vi-VN', {
    day: '2-digit', month: '2-digit', year: 'numeric',
    hour: '2-digit', minute: '2-digit'
  })
}

// Pagination logic
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

let timeout: any = null
watch(search, () => {
  clearTimeout(timeout)
  timeout = setTimeout(() => { currentPage.value = 0; loadData() }, 400)
})
watch([pageSize, sortOption], () => { currentPage.value = 0; loadData() })
watch(currentPage, () => loadData())

onMounted(() => {
  loadData()
})
</script>

<template>
  <div class="c-page">
    <Toast />

    <div class="c-header">
      <div class="c-header-left">
        <i class="fas fa-paint-brush c-icon"></i>
        <h2>Quản lý Master Colors</h2>
      </div>
      <div class="c-header-actions">
        <button class="c-btn c-btn-add" @click="openAdd">
          <i class="fas fa-plus"></i> Thêm màu sắc
        </button>
        <button class="c-btn c-btn-import" @click="triggerImportFile">
          <i class="fas fa-file-import"></i> Nhập Excel
        </button>
        <input type="file" ref="importFileRef" hidden accept=".xlsx, .xls" @change="onImportFileSelect" />
        <button class="c-btn c-btn-export" @click="exportExcel">
          <i class="fas fa-file-excel"></i> Xuất Excel
        </button>
      </div>
    </div>

    <div class="c-toolbar">
      <div class="c-toolbar-left">
        <div class="c-select-group">
          <label>Hiển thị:</label>
          <select v-model="pageSize">
            <option v-for="o in pageSizeOptions" :key="o" :value="o">{{ o }}</option>
          </select>
        </div>
        <div class="c-select-group">
          <label>Sắp xếp:</label>
          <select v-model="sortOption">
            <option value="newest">Mới nhất</option>
            <option value="oldest">Cũ nhất</option>
            <option value="az">Tên A-Z</option>
            <option value="za">Tên Z-A</option>
          </select>
        </div>
      </div>
      <div class="c-toolbar-right">
        <div class="c-search-box">
          <i class="fas fa-search"></i>
          <input type="text" v-model="search" placeholder="Tìm theo ID, tên màu...">
        </div>
      </div>
    </div>

    <div class="c-table-wrapper">
      <table class="c-table">
        <thead>
          <tr>
            <th style="width:60px">STT</th>
            <th style="width:100px">ID</th>
            <th>Tên màu</th>
            <th style="width:180px">Ngày tạo</th>
            <th style="width:180px">Ngày sửa</th>
            <th style="width:120px">Thao tác</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td colspan="6" class="text-center py-5"><i class="fas fa-spinner fa-spin"></i> Đang tải...</td>
          </tr>
          <tr v-else-if="items.length === 0">
            <td colspan="6" class="text-center py-5">Không tìm thấy dữ liệu</td>
          </tr>
          <tr v-for="(item, idx) in items" :key="item.id" class="c-row">
            <td class="text-center">{{ currentPage * pageSize + idx + 1 }}</td>
            <td class="text-center">{{ item.id }}</td>
            <td><span class="c-badge">{{ item.nameColor }}</span></td>
            <td class="text-center">{{ formatDate(item.createdAt) }}</td>
            <td class="text-center">{{ formatDate(item.updatedAt) }}</td>
            <td class="text-center">
              <div class="c-actions">
                <button class="c-action-btn c-edit" @click="openEdit(item)"><i class="fas fa-edit"></i></button>
                <button class="c-action-btn c-delete" @click="deleteItem(item.id)"><i class="fas fa-trash"></i></button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div class="c-pagination-bar" v-if="totalPages > 0">
      <div>Hiển thị {{ currentPage * pageSize + 1 }} - {{ Math.min((currentPage+1)*pageSize, totalItems) }} / {{ totalItems }} mục</div>
      <div class="c-pagination">
        <button class="c-page-btn" :disabled="currentPage === 0" @click="goToPage(currentPage - 1)"><i class="fas fa-chevron-left"></i></button>
        <button v-for="p in pages" :key="p" class="c-page-btn" :class="{ 'c-page-active': p === currentPage }" @click="goToPage(p)">{{ p + 1 }}</button>
        <button class="c-page-btn" :disabled="currentPage >= totalPages - 1" @click="goToPage(currentPage + 1)"><i class="fas fa-chevron-right"></i></button>
      </div>
    </div>

    <!-- DIALOG THÊM/SỬA -->
    <Dialog v-model:visible="displayDialog" :header="isEdit ? '✏️ Sửa màu sắc' : '➕ Thêm màu sắc mới'" :modal="true" class="c-dialog" :style="{ width: '400px' }">
      <div class="c-form">
        <div class="c-field">
          <label>Tên màu</label>
          <InputText v-model="currentItem.nameColor" placeholder="VD: Đỏ, Xanh, #FF0000..." class="c-input" />
        </div>
      </div>
      <template #footer>
        <div class="c-dialog-footer">
          <Button label="Hủy" icon="pi pi-times" class="p-button-text p-button-secondary" @click="displayDialog = false"/>
          <Button label="Lưu lại" icon="pi pi-check" class="p-button-primary c-save-btn" @click="saveItem" :loading="loading" />
        </div>
      </template>
    </Dialog>

    <!-- DIALOG REVIEW IMPORT -->
    <Dialog v-model:visible="visibleImportDialog" modal class="p-import-dialog" :style="{ width: '600px' }" header="📥 Nhập màu sắc từ Excel">
        <div class="p-import-review">
            <div class="p-info-bar">
                <i class="fas fa-info-circle"></i>
                <span>Xem trước <b>{{ importPreviewList.length }}</b> màu sắc sẽ được nhập. Hệ thống sẽ tự động bỏ qua màu đã tồn tại.</span>
            </div>
            
            <div class="p-import-table-wrapper">
                <table class="p-import-table">
                    <thead>
                        <tr>
                            <th style="width: 60px">STT</th>
                            <th>Tên màu sắc</th>
                            <th style="width: 150px">Trạng thái</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr v-for="(col, idx) in importPreviewList" :key="idx">
                            <td class="text-center font-bold">{{ idx + 1 }}</td>
                            <td class="font-semibold text-gray-800">{{ col.nameColor }}</td>
                            <td class="text-center">
                                <span v-if="col.exists" class="p-tag-warn">Đã tồn tại</span>
                                <span v-else class="p-tag-new">Mới</span>
                            </td>
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
                        <i class="fas fa-check-circle mr-2"></i>Xác nhận nhập
                    </template>
                </button>
            </div>
        </template>
    </Dialog>
  </div>
</template>

<style scoped>
.c-page { padding: 24px; font-family: 'Inter', sans-serif; }
.c-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; border-bottom: 2px solid #f1f5f9; padding-bottom: 16px; }
.c-header-left { display: flex; align-items: center; gap: 12px; }
.c-icon { font-size: 28px; color: #ec4899; }
.c-header-left h2 { margin: 0; font-size: 22px; font-weight: 700; color: #1e293b; }

.c-btn { display: flex; align-items: center; gap: 8px; padding: 10px 18px; border: none; border-radius: 8px; font-weight: 600; cursor: pointer; transition: 0.2s; color: white; }
.c-btn-add { background: #ec4899; }
.c-btn-import { background: #6366f1; margin-left: 10px; }
.c-btn-export { background: #10b981; margin-left: 10px; }
.c-btn:hover { opacity: 0.9; transform: translateY(-1px); }
.c-header-actions { display: flex; }

.c-toolbar { display: flex; justify-content: space-between; align-items: center; background: #f8fafc; padding: 12px 16px; border-radius: 12px; margin-bottom: 16px; border: 1px solid #e2e8f0; }
.c-toolbar-left { display: flex; gap: 20px; }
.c-select-group { display: flex; align-items: center; gap: 8px; font-size: 14px; }
.c-select-group select { padding: 6px 12px; border-radius: 6px; border: 1px solid #cbd5e1; outline: none; }
.c-search-box { display: flex; align-items: center; gap: 8px; background: white; border: 1px solid #cbd5e1; padding: 8px 16px; border-radius: 8px; width: 300px; }
.c-search-box input { border: none; outline: none; flex: 1; font-size: 14px; }

.c-table-wrapper { border-radius: 12px; overflow: hidden; border: 1px solid #e2e8f0; box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1); }
.c-table { width: 100%; border-collapse: collapse; background: white; }
.c-table thead { background: #f1f5f9; }
.c-table th { padding: 14px 16px; text-align: left; font-size: 13px; font-weight: 600; color: #475569; text-transform: uppercase; }
.c-table td { padding: 14px 16px; border-top: 1px solid #f1f5f9; color: #1e293b; font-size: 14px; }
.c-row:hover { background: #f8fafc; }
.c-badge { background: #fce7f3; color: #9d174d; padding: 4px 10px; border-radius: 6px; font-weight: 600; font-size: 12px; border: 1px solid #fbcfe8; }

.c-actions { display: flex; gap: 8px; justify-content: center; }
.c-action-btn { width: 32px; height: 32px; border: none; border-radius: 6px; cursor: pointer; transition: 0.2s; display: flex; align-items: center; justify-content: center; font-size: 14px; }
.c-edit { background: #fef3c7; color: #92400e; }
.c-delete { background: #fee2e2; color: #991b1b; }

.c-pagination-bar { display: flex; justify-content: space-between; align-items: center; margin-top: 20px; font-size: 14px; color: #64748b; }
.c-pagination { display: flex; gap: 6px; }
.c-page-btn { padding: 6px 12px; border: 1px solid #e2e8f0; border-radius: 6px; background: white; cursor: pointer; }
.c-page-active { background: #ec4899; color: white; border-color: #ec4899; }
.text-center { text-align: center; }

/* IMPORT DIALOG */
.p-import-review { padding: 10px 20px 20px; }
.p-info-bar { background: #eff6ff; border: 1px solid #bfdbfe; color: #1e40af; padding: 12px 16px; border-radius: 10px; margin-bottom: 16px; display: flex; align-items: center; gap: 10px; font-size: 14px; }
.p-import-table-wrapper { border: 1px solid #e2e8f0; border-radius: 10px; overflow: auto; max-height: 400px; }
.p-import-table { width: 100%; border-collapse: collapse; }
.p-import-table th { background: #f8fafc; padding: 12px; font-size: 11px; color: #64748b; text-transform: uppercase; border-bottom: 2px solid #e2e8f0; }
.p-import-table td { padding: 10px 12px; border-bottom: 1px solid #f1f5f9; font-size: 13px; }
.p-tag-new { background: #f0fdf4; color: #166534; padding: 2px 8px; border-radius: 4px; font-size: 11px; font-weight: 700; }
.p-tag-warn { background: #fffbeb; color: #92400e; padding: 2px 8px; border-radius: 4px; font-size: 11px; font-weight: 700; }

.p-modal-footer { display: flex; justify-content: flex-end; gap: 10px; padding: 12px 0; }
.p-footer-btn { padding: 8px 20px; border-radius: 8px; border: none; font-weight: 700; cursor: pointer; display: flex; align-items: center; }
.p-btn-cancel { background: #f1f5f9; color: #475569; }
.p-btn-confirm-import { background: #6366f1; color: white; }

.c-form { display: flex; flex-direction: column; gap: 16px; padding: 10px 0; }
.c-field { display: flex; flex-direction: column; gap: 6px; }
.c-field label { font-size: 14px; font-weight: 600; color: #475569; }
.c-input { width: 100%; border-radius: 8px; }
.c-dialog-footer { display: flex; justify-content: flex-end; gap: 10px; }
.c-save-btn { border-radius: 8px; padding: 8px 20px; }
</style>
