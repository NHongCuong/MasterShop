<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import axios from 'axios'
import Toast from 'primevue/toast'
import { useToast } from 'primevue/usetoast'
import Dialog from 'primevue/dialog'
import Button from 'primevue/button'
import InputText from 'primevue/inputtext'

const toast = useToast()
const API = 'http://localhost:8081/voucher'

const list = ref<any[]>([])
const totalItems = ref(0)
const totalPages = ref(0)
const loading = ref(false)

const selected = ref<any>(null)
const visibleDialog = ref(false)
const editMode = ref(false)

const search = ref('')
const pageSize = ref(10)
const currentPage = ref(0)
const sortOption = ref('newest')
const pageSizeOptions = [10, 20, 50, 100]

const loadData = async () => {
  loading.value = true
  try {
    const params: Record<string, string | number> = {
      page: currentPage.value,
      size: pageSize.value,
      sort: sortOption.value
    }
    if (search.value.trim()) params.search = search.value.trim()
    const res = await axios.get(`${API}/all`, { params })
    list.value = res.data.content || []
    totalItems.value = res.data.totalElements || 0
    totalPages.value = res.data.totalPages || 0
  } catch {
    toast.add({ severity: 'error', summary: 'Lỗi', detail: 'Không thể tải dữ liệu', life: 2000 })
  } finally {
    loading.value = false
  }
}

const openAdd = () => {
  selected.value = { id: null, maVoucher: '', discountPercent: 0 }
  editMode.value = false
  visibleDialog.value = true
}

const openEdit = (item: any) => {
  selected.value = { ...item }
  editMode.value = true
  visibleDialog.value = true
}

const deleteItem = async (id: number) => {
  if (!confirm('Bạn có chắc muốn xóa voucher này?')) return
  try {
    await axios.delete(`${API}/delete/${id}`)
    toast.add({ severity: 'success', summary: 'Thành công', detail: 'Đã xóa voucher', life: 1500 })
    loadData()
  } catch {
    toast.add({ severity: 'error', summary: 'Lỗi', detail: 'Xóa thất bại', life: 2000 })
  }
}

const save = async () => {
  if (!selected.value.maVoucher?.trim()) {
    toast.add({ severity: 'warn', summary: 'Cảnh báo', detail: 'Vui lòng nhập mã voucher', life: 2000 })
    return
  }
  try {
    const payload = {
      maVoucher: selected.value.maVoucher.trim().toUpperCase(),
      discountPercent: Number(selected.value.discountPercent) || 0
    }
    if (editMode.value) {
      await axios.put(`${API}/update/${selected.value.id}`, payload)
      toast.add({ severity: 'success', summary: 'Cập nhật', detail: 'Sửa voucher thành công', life: 1500 })
    } else {
      await axios.post(`${API}/add`, payload)
      toast.add({ severity: 'success', summary: 'Thêm mới', detail: 'Thêm voucher thành công', life: 1500 })
    }
    visibleDialog.value = false
    loadData()
  } catch (err: any) {
    const msg = err.response?.data || 'Không thể lưu voucher'
    toast.add({ severity: 'error', summary: 'Lỗi', detail: msg, life: 2000 })
  }
}

const formatDate = (date: string | null) => {
  if (!date) return '—'
  return new Date(date).toLocaleDateString('vi-VN', {
    day: '2-digit', month: '2-digit', year: 'numeric',
    hour: '2-digit', minute: '2-digit'
  })
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
  timeout = setTimeout(() => { currentPage.value = 0; loadData() }, 400)
})
watch([pageSize, sortOption], () => { currentPage.value = 0; loadData() })
watch(currentPage, () => loadData())

onMounted(() => loadData())
</script>

<template>
  <div class="vc-page">
    <Toast />

    <div class="vc-header">
      <div class="vc-header-left">
        <i class="fas fa-ticket-alt vc-icon"></i>
        <h2>Quản lý Voucher</h2>
      </div>
      <div class="vc-header-actions">
        <button class="vc-btn vc-btn-add" @click="openAdd">
          <i class="fas fa-plus"></i> Thêm voucher
        </button>
      </div>
    </div>

    <div class="vc-toolbar">
      <div class="vc-toolbar-left">
        <div class="vc-select-group">
          <label>Hiển thị:</label>
          <select v-model="pageSize">
            <option v-for="o in pageSizeOptions" :key="o" :value="o">{{ o }}</option>
          </select>
        </div>
        <div class="vc-select-group">
          <label>Sắp xếp:</label>
          <select v-model="sortOption">
            <option value="newest">Mới nhất</option>
            <option value="oldest">Cũ nhất</option>
            <option value="az">A-Z</option>
            <option value="za">Z-A</option>
          </select>
        </div>
      </div>
      <div class="vc-toolbar-right">
        <div class="vc-search-box">
          <i class="fas fa-search"></i>
          <input type="text" v-model="search" placeholder="Tìm theo ID, mã voucher...">
        </div>
      </div>
    </div>

    <div class="vc-table-wrapper">
      <table class="vc-table">
        <thead>
          <tr>
            <th style="width:60px">STT</th>
            <th style="width:80px">ID</th>
            <th>Mã Voucher</th>
            <th style="width:150px">Giảm giá (%)</th>
            <th style="width:180px">Ngày tạo</th>
            <th style="width:180px">Ngày sửa</th>
            <th style="width:120px">Thao tác</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td colspan="7" class="text-center py-5"><i class="fas fa-spinner fa-spin"></i> Đang tải...</td>
          </tr>
          <tr v-else-if="list.length === 0">
            <td colspan="7" class="text-center py-5">Không tìm thấy dữ liệu</td>
          </tr>
          <tr v-for="(item, idx) in list" :key="item.id" class="vc-row">
            <td class="text-center">{{ currentPage * pageSize + idx + 1 }}</td>
            <td class="text-center">{{ item.id }}</td>
            <td><span class="vc-badge">{{ item.maVoucher }}</span></td>
            <td class="text-center">
              <span class="vc-discount-badge" v-if="item.discountPercent > 0">-{{ item.discountPercent }}%</span>
              <span v-else class="text-muted">—</span>
            </td>
            <td class="text-center">{{ formatDate(item.createdAt) }}</td>
            <td class="text-center">{{ formatDate(item.updatedAt) }}</td>
            <td class="text-center">
              <div class="vc-actions">
                <button class="vc-action-btn vc-edit" @click="openEdit(item)"><i class="fas fa-edit"></i></button>
                <button class="vc-action-btn vc-delete" @click="deleteItem(item.id)"><i class="fas fa-trash"></i></button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div class="vc-pagination-bar" v-if="totalPages > 0">
      <div>
        Hiển thị {{ currentPage * pageSize + 1 }} -
        {{ Math.min((currentPage + 1) * pageSize, totalItems) }} / {{ totalItems }} mục
      </div>
      <div class="vc-pagination">
        <button class="vc-page-btn" :disabled="currentPage === 0" @click="goToPage(currentPage - 1)">
          <i class="fas fa-chevron-left"></i>
        </button>
        <button
          v-for="p in pages" :key="p"
          class="vc-page-btn" :class="{ 'vc-page-active': p === currentPage }"
          @click="goToPage(p)">{{ p + 1 }}</button>
        <button class="vc-page-btn" :disabled="currentPage >= totalPages - 1" @click="goToPage(currentPage + 1)">
          <i class="fas fa-chevron-right"></i>
        </button>
      </div>
    </div>

    <Dialog
      v-model:visible="visibleDialog"
      :header="editMode ? '✏️ Sửa Voucher' : '➕ Thêm Voucher Mới'"
      modal
      class="vc-dialog"
      :style="{ width: '440px' }"
    >
      <div class="vc-form">
        <div v-if="editMode" class="vc-field">
          <label>ID</label>
          <InputText v-model="selected.id" class="vc-input" disabled />
        </div>
        <div class="vc-field">
          <label>Mã Voucher <span class="text-danger">*</span></label>
          <InputText v-model="selected.maVoucher" placeholder="VD: SALE10, GIAM50..." class="vc-input" />
          <small class="text-muted">Mã sẽ được tự động chuyển thành chữ hoa</small>
        </div>
        <div class="vc-field">
          <label>Phần trăm giảm giá (%)</label>
          <InputText v-model="selected.discountPercent" type="number" min="0" max="100" placeholder="VD: 10" class="vc-input" />
          <small class="text-muted">Nhập 0 nếu không áp dụng giảm theo %</small>
        </div>
      </div>
      <template #footer>
        <div class="vc-dialog-footer">
          <Button label="Hủy" icon="pi pi-times" class="p-button-text p-button-secondary" @click="visibleDialog = false" />
          <Button label="Lưu lại" icon="pi pi-check" class="p-button-primary vc-save-btn" @click="save" />
        </div>
      </template>
    </Dialog>
  </div>
</template>

<style scoped>
.vc-page { padding: 24px; font-family: 'Inter', sans-serif; }
.vc-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; border-bottom: 2px solid #f1f5f9; padding-bottom: 16px; }
.vc-header-left { display: flex; align-items: center; gap: 12px; }
.vc-icon { font-size: 28px; color: #f59e0b; }
.vc-header-left h2 { margin: 0; font-size: 22px; font-weight: 700; color: #1e293b; }
.vc-btn { display: flex; align-items: center; gap: 8px; padding: 10px 18px; border: none; border-radius: 8px; font-weight: 600; cursor: pointer; transition: 0.2s; }
.vc-btn-add { background: #f59e0b; color: white; }
.vc-btn-add:hover { background: #d97706; }
.vc-header-actions { display: flex; }

.vc-toolbar { display: flex; justify-content: space-between; align-items: center; background: #f8fafc; padding: 12px 16px; border-radius: 12px; margin-bottom: 16px; border: 1px solid #e2e8f0; }
.vc-toolbar-left { display: flex; gap: 20px; }
.vc-select-group { display: flex; align-items: center; gap: 8px; font-size: 14px; }
.vc-select-group select { padding: 6px 12px; border-radius: 6px; border: 1px solid #cbd5e1; outline: none; }
.vc-search-box { display: flex; align-items: center; gap: 8px; background: white; border: 1px solid #cbd5e1; padding: 8px 16px; border-radius: 8px; width: 300px; }
.vc-search-box input { border: none; outline: none; flex: 1; font-size: 14px; }

.vc-table-wrapper { border-radius: 12px; overflow: hidden; border: 1px solid #e2e8f0; box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1); }
.vc-table { width: 100%; border-collapse: collapse; background: white; }
.vc-table thead { background: #f1f5f9; }
.vc-table th { padding: 14px 16px; text-align: left; font-size: 13px; font-weight: 600; color: #475569; text-transform: uppercase; }
.vc-table td { padding: 14px 16px; border-top: 1px solid #f1f5f9; color: #1e293b; font-size: 14px; }
.vc-row:hover { background: #f8fafc; }
.vc-badge { background: #fef3c7; color: #92400e; padding: 4px 10px; border-radius: 6px; font-weight: 700; font-size: 13px; letter-spacing: 0.5px; }
.vc-discount-badge { background: #dcfce7; color: #166534; padding: 4px 10px; border-radius: 6px; font-weight: 600; font-size: 13px; }

.vc-actions { display: flex; gap: 8px; justify-content: center; }
.vc-action-btn { width: 32px; height: 32px; border: none; border-radius: 6px; cursor: pointer; transition: 0.2s; display: flex; align-items: center; justify-content: center; font-size: 14px; }
.vc-edit { background: #fef3c7; color: #92400e; }
.vc-delete { background: #fee2e2; color: #991b1b; }
.vc-edit:hover { background: #fde68a; }
.vc-delete:hover { background: #fecaca; }

.vc-pagination-bar { display: flex; justify-content: space-between; align-items: center; margin-top: 20px; font-size: 14px; color: #64748b; }
.vc-pagination { display: flex; gap: 6px; }
.vc-page-btn { padding: 6px 12px; border: 1px solid #e2e8f0; border-radius: 6px; background: white; cursor: pointer; }
.vc-page-btn:disabled { opacity: 0.5; cursor: not-allowed; }
.vc-page-active { background: #f59e0b; color: white; border-color: #f59e0b; }
.text-center { text-align: center; }
.text-muted { color: #94a3b8; }
.text-danger { color: #ef4444; }
.py-5 { padding: 20px 0; }

.vc-form { display: flex; flex-direction: column; gap: 16px; padding: 10px 0; }
.vc-field { display: flex; flex-direction: column; gap: 6px; }
.vc-field label { font-size: 14px; font-weight: 600; color: #475569; }
.vc-input { width: 100%; border-radius: 8px; }
.vc-dialog-footer { display: flex; justify-content: flex-end; gap: 10px; }
.vc-save-btn { border-radius: 8px; padding: 8px 20px; }
</style>
