<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import axios from 'axios'
import Toast from 'primevue/toast'
import { useToast } from 'primevue/usetoast'
import Dialog from 'primevue/dialog'
import Dropdown from 'primevue/dropdown'
import Button from 'primevue/button'
import InputText from 'primevue/inputtext'

const toast = useToast()
const API_URL = 'http://localhost:8081/admin/product-dimensions'
const BASE_PRODUCT_API = 'http://localhost:8081/product/all'

// Data
const items = ref<any[]>([])
const totalItems = ref(0)
const totalPages = ref(0)
const loading = ref(false)

// Options
const productOptions = ref<any[]>([])

// Dialog state
const displayDialog = ref(false)
const isEdit = ref(false)
const currentItem = ref({
  id: null,
  nameD: '',
  productId: null
})

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

const loadProducts = async () => {
  try {
    const res = await axios.get(BASE_PRODUCT_API)
    const productData = Array.isArray(res.data) ? res.data : (res.data.content || [])
    productOptions.value = productData.map((p: any) => ({ label: p.name, value: p.id }))
  } catch (err) {
    console.error(err)
  }
}

// Add / Edit
const openAdd = () => {
  currentItem.value = { id: null, nameD: '', productId: null }
  isEdit.value = false
  displayDialog.value = true
}

const openEdit = (item: any) => {
  currentItem.value = { id: item.id, nameD: item.nameD, productId: item.productId }
  isEdit.value = true
  displayDialog.value = true
}

const saveItem = async () => {
  if (!currentItem.value.nameD.trim() || !currentItem.value.productId) {
    toast.add({ severity: 'warn', summary: 'Cảnh báo', detail: 'Vui lòng nhập đầy đủ thông tin', life: 2000 })
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
  if (!confirm('Bạn có chắc muốn xóa kích cỡ này?')) return
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
    link.setAttribute('download', 'product_dimensions.xlsx')
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
    const validItems = importPreviewList.value.filter(x => x.isValid && !x.exists)
    if (validItems.length === 0) {
        toast.add({ severity: 'warn', summary: 'Thông báo', detail: 'Không có dữ liệu mới hợp lệ để nhập', life: 3000 })
        return
    }
    isConfirmingImport.value = true
    try {
        const res = await axios.post(`${API_URL}/confirm-import`, validItems)
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

// Pagination
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

// Watchers
let timeout: any = null
watch(search, () => {
  clearTimeout(timeout)
  timeout = setTimeout(() => { currentPage.value = 0; loadData() }, 400)
})
watch([pageSize, sortOption], () => { currentPage.value = 0; loadData() })
watch(currentPage, () => loadData())

onMounted(() => {
  loadData()
  loadProducts()
})
</script>

<template>
  <div class="pd-page">
    <Toast />

    <div class="pd-header">
      <div class="pd-header-left">
        <i class="fas fa-ruler-combined pd-icon"></i>
        <h2>Quản lý Product Dimensions</h2>
      </div>
      <div class="pd-header-actions">
        <button class="pd-btn pd-btn-add" @click="openAdd">
          <i class="fas fa-plus"></i> Thêm kích cỡ
        </button>
        <button class="pd-btn pd-btn-import" @click="triggerImportFile">
          <i class="fas fa-file-import"></i> Nhập Excel
        </button>
        <input type="file" ref="importFileRef" hidden accept=".xlsx, .xls" @change="onImportFileSelect" />
        <button class="pd-btn pd-btn-export" @click="exportExcel">
          <i class="fas fa-file-excel"></i> Xuất Excel
        </button>
      </div>
    </div>

    <div class="pd-toolbar">
      <div class="pd-toolbar-left">
        <div class="pd-select-group">
          <label>Hiển thị:</label>
          <select v-model="pageSize">
            <option v-for="o in pageSizeOptions" :key="o" :value="o">{{ o }}</option>
          </select>
        </div>
        <div class="pd-select-group">
          <label>Sắp xếp:</label>
          <select v-model="sortOption">
            <option value="newest">Mới nhất</option>
            <option value="oldest">Cũ nhất</option>
            <option value="az">Tên A-Z</option>
            <option value="za">Tên Z-A</option>
          </select>
        </div>
      </div>
      <div class="pd-toolbar-right">
        <div class="pd-search-box">
          <i class="fas fa-search"></i>
          <input type="text" v-model="search" placeholder="Tìm theo tên kích cỡ, sản phẩm...">
        </div>
      </div>
    </div>

    <div class="pd-table-wrapper">
      <table class="pd-table">
        <thead>
          <tr>
            <th style="width:60px">STT</th>
            <th>Tên kích cỡ</th>
            <th>Tên sản phẩm</th>
            <th style="width:180px">Ngày tạo</th>
            <th style="width:180px">Ngày sửa</th>
            <th style="width:120px">Thao tác</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td colspan="6" class="pd-empty"><i class="fas fa-spinner fa-spin"></i> Đang tải...</td>
          </tr>
          <tr v-else-if="items.length === 0">
            <td colspan="6" class="pd-empty">Không tìm thấy dữ liệu</td>
          </tr>
          <tr v-for="(item, idx) in items" :key="item.id" class="pd-row">
            <td class="text-center">{{ currentPage * pageSize + idx + 1 }}</td>
            <td><span class="pd-badge">{{ item.nameD }}</span></td>
            <td>{{ item.productName }}</td>
            <td class="text-center">{{ formatDate(item.createdAt) }}</td>
            <td class="text-center">{{ formatDate(item.updatedAt) }}</td>
            <td class="text-center">
              <div class="pd-actions">
                <button class="pd-action-btn pd-edit" title="Sửa" @click="openEdit(item)"><i class="fas fa-edit"></i></button>
                <button class="pd-action-btn pd-delete" title="Xóa" @click="deleteItem(item.id)"><i class="fas fa-trash"></i></button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div class="pd-pagination-bar" v-if="totalPages > 0">
      <div>Hiển thị {{ currentPage * pageSize + 1 }} - {{ Math.min((currentPage+1)*pageSize, totalItems) }} / {{ totalItems }} mục</div>
      <div class="pd-pagination">
        <button class="pd-page-btn" :disabled="currentPage === 0" @click="goToPage(currentPage - 1)"><i class="fas fa-chevron-left"></i></button>
        <button v-for="p in pages" :key="p" class="pd-page-btn" :class="{ 'pd-page-active': p === currentPage }" @click="goToPage(p)">{{ p + 1 }}</button>
        <button class="pd-page-btn" :disabled="currentPage >= totalPages - 1" @click="goToPage(currentPage + 1)"><i class="fas fa-chevron-right"></i></button>
      </div>
    </div>

    <!-- Dialog Thêm/Sửa -->
    <Dialog v-model:visible="displayDialog" :header="isEdit ? '✏️ Sửa kích cỡ' : '➕ Thêm kích cỡ mới'" :modal="true" class="pd-dialog" :style="{ width: '450px' }">
      <div class="pd-form">
        <div class="pd-field">
          <label>Sản phẩm</label>
          <Dropdown v-model="currentItem.productId" :options="productOptions" optionLabel="label" optionValue="value" placeholder="Chọn sản phẩm" filter class="pd-dropdown" />
        </div>
        <div class="pd-field">
          <label>Tên kích cỡ</label>
          <InputText v-model="currentItem.nameD" placeholder="VD: 40x50cm, Full size..." class="pd-input" />
        </div>
      </div>
      <template #footer>
        <div class="pd-dialog-footer">
          <Button label="Hủy" icon="pi pi-times" class="p-button-text p-button-secondary" @click="displayDialog = false"/>
          <Button label="Lưu lại" icon="pi pi-check" class="p-button-primary pd-save-btn" @click="saveItem" :loading="loading" />
        </div>
      </template>
    </Dialog>

    <!-- Dialog Review Import -->
    <Dialog v-model:visible="visibleImportDialog" modal class="pd-import-dialog" :style="{ width: '800px' }" header="📥 Nhập kích cỡ sản phẩm">
        <div class="pd-import-review">
            <div class="pd-info-bar">
                <i class="fas fa-info-circle"></i>
                <span>Xem trước <b>{{ importPreviewList.length }}</b> mục. Hệ thống sẽ tự động tìm ID dựa trên tên và kiểm tra tồn tại.</span>
            </div>
            
            <div class="pd-import-table-wrapper">
                <table class="pd-import-table">
                    <thead>
                        <tr>
                            <th style="width: 50px">STT</th>
                            <th>Tên kích cỡ</th>
                            <th>Tên sản phẩm</th>
                            <th style="width: 250px">Trạng thái</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr v-for="(item, idx) in importPreviewList" :key="idx">
                            <td class="text-center font-bold">{{ idx + 1 }}</td>
                            <td class="font-semibold">{{ item.nameD }}</td>
                            <td>{{ item.productName }}</td>
                            <td class="text-center">
                                <span v-if="!item.isValid" class="p-tag-error">{{ item.errors }}</span>
                                <span v-else-if="item.exists" class="p-tag-warn">Đã tồn tại</span>
                                <span v-else class="p-tag-new">Mới</span>
                            </td>
                        </tr>
                    </tbody>
                </table>
            </div>
        </div>
        
        <template #footer>
            <div class="pd-modal-footer">
                <button class="pd-footer-btn pd-btn-cancel" @click="visibleImportDialog = false" :disabled="isConfirmingImport">
                    <i class="fas fa-times mr-2"></i>Hủy bỏ
                </button>
                <button class="pd-footer-btn pd-btn-confirm-import" @click="confirmImport" :disabled="isConfirmingImport">
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
.pd-page { padding: 24px; font-family: 'Inter', sans-serif; }
.pd-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; border-bottom: 2px solid #f1f5f9; padding-bottom: 16px; }
.pd-header-left { display: flex; align-items: center; gap: 12px; }
.pd-icon { font-size: 28px; color: #3b82f6; }
.pd-header-left h2 { margin: 0; font-size: 22px; font-weight: 700; color: #1e293b; }

.pd-btn { display: flex; align-items: center; gap: 8px; padding: 10px 18px; border: none; border-radius: 8px; font-weight: 600; cursor: pointer; transition: 0.2s; color: white; }
.pd-btn-add { background: #3b82f6; }
.pd-btn-import { background: #6366f1; }
.pd-btn-export { background: #10b981; }
.pd-header-actions { display: flex; gap: 10px; }

.pd-toolbar { display: flex; justify-content: space-between; align-items: center; background: #f8fafc; padding: 12px 16px; border-radius: 12px; margin-bottom: 16px; border: 1px solid #e2e8f0; }
.pd-toolbar-left { display: flex; gap: 20px; }
.pd-select-group { display: flex; align-items: center; gap: 8px; font-size: 14px; }
.pd-select-group select { padding: 6px 12px; border-radius: 6px; border: 1px solid #cbd5e1; outline: none; }
.pd-search-box { display: flex; align-items: center; gap: 8px; background: white; border: 1px solid #cbd5e1; padding: 8px 16px; border-radius: 8px; width: 300px; }
.pd-search-box input { border: none; outline: none; flex: 1; font-size: 14px; }

.pd-table-wrapper { border-radius: 12px; overflow: hidden; border: 1px solid #e2e8f0; box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1); }
.pd-table { width: 100%; border-collapse: collapse; background: white; }
.pd-table thead { background: #f1f5f9; }
.pd-table th { padding: 14px 16px; text-align: left; font-size: 13px; font-weight: 600; color: #475569; text-transform: uppercase; }
.pd-table td { padding: 14px 16px; border-top: 1px solid #f1f5f9; color: #1e293b; font-size: 14px; }
.pd-row:hover { background: #f8fafc; }
.pd-badge { background: #dbeafe; color: #1e40af; padding: 4px 10px; border-radius: 6px; font-weight: 600; font-size: 12px; }

.pd-actions { display: flex; gap: 8px; justify-content: center; }
.pd-action-btn { width: 32px; height: 32px; border: none; border-radius: 6px; cursor: pointer; transition: 0.2s; display: flex; align-items: center; justify-content: center; font-size: 14px; }
.pd-edit { background: #fef3c7; color: #92400e; }
.pd-delete { background: #fee2e2; color: #991b1b; }

.pd-pagination-bar { display: flex; justify-content: space-between; align-items: center; margin-top: 20px; font-size: 14px; color: #64748b; }
.pd-pagination { display: flex; gap: 6px; }
.pd-page-btn { padding: 6px 12px; border: 1px solid #e2e8f0; border-radius: 6px; background: white; cursor: pointer; }
.pd-page-active { background: #3b82f6; color: white; border-color: #3b82f6; }
.text-center { text-align: center; }
.pd-empty { text-align: center; padding: 40px; color: #94a3b8; }

/* Import Review Styles */
.pd-import-review { padding: 10px 20px 20px; }
.pd-info-bar { background: #eff6ff; border: 1px solid #bfdbfe; color: #1e40af; padding: 12px 16px; border-radius: 10px; margin-bottom: 16px; display: flex; align-items: center; gap: 10px; font-size: 13px; }
.pd-import-table-wrapper { border: 1px solid #e2e8f0; border-radius: 10px; overflow: auto; max-height: 400px; }
.pd-import-table { width: 100%; border-collapse: collapse; }
.pd-import-table th { background: #f8fafc; padding: 12px; font-size: 11px; color: #64748b; text-transform: uppercase; border-bottom: 2px solid #e2e8f0; text-align: left; }
.pd-import-table td { padding: 10px 12px; border-bottom: 1px solid #f1f5f9; font-size: 13px; }

.p-tag-new { background: #f0fdf4; color: #166534; padding: 2px 8px; border-radius: 4px; font-size: 11px; font-weight: 700; }
.p-tag-warn { background: #fffbeb; color: #92400e; padding: 2px 8px; border-radius: 4px; font-size: 11px; font-weight: 700; }
.p-tag-error { background: #fef2f2; color: #991b1b; padding: 2px 8px; border-radius: 4px; font-size: 11px; font-weight: 700; }

.pd-modal-footer { display: flex; justify-content: flex-end; gap: 10px; padding: 12px 0; }
.pd-footer-btn { padding: 8px 20px; border-radius: 8px; border: none; font-weight: 700; cursor: pointer; display: flex; align-items: center; }
.pd-btn-cancel { background: #f1f5f9; color: #475569; }
.pd-btn-confirm-import { background: #6366f1; color: white; }

.pd-form { display: flex; flex-direction: column; gap: 16px; padding: 10px 0; }
.pd-field { display: flex; flex-direction: column; gap: 6px; }
.pd-field label { font-size: 14px; font-weight: 600; color: #475569; }
.pd-dropdown, .pd-input { width: 100%; border-radius: 8px; }
</style>
