<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import axios from 'axios'
import Helper from '../../helper/helper'
import Toast from 'primevue/toast'
import { useToast } from 'primevue/usetoast'
import Dialog from 'primevue/dialog'
import Button from 'primevue/button'
import InputText from 'primevue/inputtext'
import InputNumber from 'primevue/inputnumber'
import Textarea from 'primevue/textarea'
import Dropdown from 'primevue/dropdown'
import type { Category, Supplier } from '../../interfaces/app.ts'

const toast = useToast()
const API_URL = 'http://localhost:8081'
const PRODUCT_API = `${API_URL}/product`
const CATEGORY_API = `${API_URL}/category`
const SUPPLIER_API = `${API_URL}/supplier`
const VOUCHER_API = `${API_URL}/voucher`
const STORAGE_API = `${API_URL}/storage`

const productList = ref<any[]>([])
const categoryList = ref<Category[]>([])
const supplierList = ref<Supplier[]>([])
const voucherList = ref<any[]>([])

const totalItems = ref(0)
const totalPages = ref(0)
const loading = ref(false)

const selectedProduct = ref<any>(null)
const visibleDialog = ref(false)
const editMode = ref(false)

const showDetailDialog = ref(false)
const detailProduct = ref<any>(null)
const detailLoading = ref(false)
const detailColors = ref<string[]>([])
const detailMaterials = ref<string[]>([])
const detailDimensions = ref<string[]>([])

const search = ref('')
const pageSize = ref(10)
const currentPage = ref(0)
const sortOption = ref('newest')
const pageSizeOptions = [10, 20, 50, 100]

// ========== IMAGE UPLOAD STATE ==========
const selectedFiles = ref<File[]>([])
const previewImages = ref<string[]>([])
const uploadedImageUrls = ref<string[]>([])
const isUploading = ref(false)
const fileInputRef = ref<HTMLInputElement | null>(null)

// ========== IMPORT STATE ==========
const importFileRef = ref<HTMLInputElement | null>(null)
const importPreviewList = ref<any[]>([])
const visibleImportDialog = ref(false)
const isConfirmingImport = ref(false)

// ========== LOAD DATA ==========
const loadProducts = async () => {
    loading.value = true
    try {
        const params: any = {
            page: currentPage.value,
            size: pageSize.value,
            sort: sortOption.value
        }
        if (search.value.trim()) params.search = search.value.trim()
        
        const res = await axios.get(`${PRODUCT_API}/all-paged`, { params })
        productList.value = res.data.content || []
        totalItems.value = res.data.totalElements || 0
        totalPages.value = res.data.totalPages || 0
    } catch {
        toast.add({ severity: 'error', summary: 'Lỗi', detail: 'Không thể tải sản phẩm', life: 2000 })
    } finally {
        loading.value = false
    }
}

const loadMetaData = async () => {
    try {
        const catRes = await axios.get(`${CATEGORY_API}/list`)
        categoryList.value = Array.isArray(catRes.data) ? catRes.data : (catRes.data?.content || [])
    } catch {
        toast.add({ severity: 'warn', summary: 'Cảnh báo', detail: 'Không thể tải danh mục', life: 2000 })
    }
    try {
        const supRes = await axios.get(`${SUPPLIER_API}/list`, { params: { page: 0, size: 500, sort: 'name,asc' } })
        supplierList.value = supRes.data?.content || []
    } catch {
        toast.add({ severity: 'warn', summary: 'Cảnh báo', detail: 'Không thể tải nhà cung cấp', life: 2000 })
    }
    try {
        const vchRes = await axios.get(`${VOUCHER_API}/list`)
        voucherList.value = Array.isArray(vchRes.data) ? vchRes.data : (vchRes.data?.content || [])
    } catch {
        toast.add({ severity: 'warn', summary: 'Cảnh báo', detail: 'Không thể tải voucher', life: 2000 })
    }
}

// ========== FILE HANDLING ==========
const onFileSelect = (event: Event) => {
    const input = event.target as HTMLInputElement
    if (!input.files) return
    const files = Array.from(input.files)
    files.forEach(file => {
        if (!file.type.startsWith('image/')) return
        selectedFiles.value.push(file)
        const reader = new FileReader()
        reader.onload = (e) => previewImages.value.push(e.target?.result as string)
        reader.readAsDataURL(file)
    })
    input.value = ''
}

const removeImage = (index: number) => {
    selectedFiles.value.splice(index, 1)
    previewImages.value.splice(index, 1)
}

const removeUploadedImage = (index: number) => {
    uploadedImageUrls.value.splice(index, 1)
}

const triggerFileInput = () => fileInputRef.value?.click()

const uploadAllImages = async (): Promise<string[]> => {
    const urls: string[] = []
    for (const file of selectedFiles.value) {
        const formData = new FormData()
        formData.append('file', file)
        formData.append('folder', 'products')
        try {
            const res = await axios.post(`${STORAGE_API}/upload`, formData, {
                headers: { 'Content-Type': 'multipart/form-data' }
            })
            urls.push(res.data.url)
        } catch (err) {
            console.error('Upload error:', err)
        }
    }
    return urls
}

// ========== DIALOG ACTIONS ==========
const openAddDialog = async () => {
    await loadMetaData()
    selectedProduct.value = {
        id: null,
        name: '',
        description: '',
        price: 0,
        avatar: '',
        amount: 0,
        discountPercent: 0,
        warranty: '',
        category: null,
        supplier: null,
        voucher: null
    }
    selectedFiles.value = []
    previewImages.value = []
    uploadedImageUrls.value = []
    editMode.value = false
    visibleDialog.value = true
}

const openEditDialog = async (product: any) => {
    await loadMetaData()
    selectedProduct.value = { ...product }
    // Map objects to ensure dropdown selection
    if (product.category) selectedProduct.value.category = categoryList.value.find(c => c.id === product.category.id)
    if (product.supplier) selectedProduct.value.supplier = supplierList.value.find(s => s.id === product.supplier.id)
    if (product.voucher) selectedProduct.value.voucher = voucherList.value.find(v => v.id === product.voucher.id)

    uploadedImageUrls.value = []
    if (product.avatar) uploadedImageUrls.value.push(product.avatar)
    if (product.productImages) {
        product.productImages.forEach((img: any) => {
            if (img.imageUrl && img.imageUrl !== product.avatar) uploadedImageUrls.value.push(img.imageUrl)
        })
    }

    selectedFiles.value = []
    previewImages.value = []
    editMode.value = true
    visibleDialog.value = true
}

const saveProduct = async () => {
    try {
        isUploading.value = true
        let newUrls = await uploadAllImages()
        const allUrls = [...uploadedImageUrls.value, ...newUrls]
        const avatarUrl = allUrls.length > 0 ? allUrls[0] : ''
        const productImages = allUrls.map(url => ({ imageUrl: url }))

        const payload = {
            name: selectedProduct.value.name,
            description: selectedProduct.value.description,
            price: selectedProduct.value.price,
            avatar: avatarUrl,
            amount: selectedProduct.value.amount,
            discountPercent: selectedProduct.value.discountPercent,
            warranty: selectedProduct.value.warranty || '',
            category: selectedProduct.value.category ? { id: selectedProduct.value.category.id } : null,
            supplier: selectedProduct.value.supplier ? { id: selectedProduct.value.supplier.id } : null,
            voucher: selectedProduct.value.voucher ? { id: selectedProduct.value.voucher.id } : null,
            productImages: productImages
        }

        if (editMode.value) {
            await axios.put(`${PRODUCT_API}/update/${selectedProduct.value.id}`, payload)
            toast.add({ severity: 'success', summary: 'Cập nhật', detail: 'Sửa sản phẩm thành công', life: 1500 })
        } else {
            await axios.post(`${PRODUCT_API}/add`, payload)
            toast.add({ severity: 'success', summary: 'Thêm mới', detail: 'Thêm sản phẩm thành công', life: 1500 })
        }

        visibleDialog.value = false
        loadProducts()
    } catch (err) {
        toast.add({ severity: 'error', summary: 'Lỗi', detail: 'Không thể lưu sản phẩm', life: 2000 })
    } finally {
        isUploading.value = false
    }
}

const deleteProduct = async (id: number) => {
    if (!confirm('Bạn có chắc muốn xóa sản phẩm này?')) return
    try {
        await axios.delete(`${PRODUCT_API}/delete/${id}`)
        toast.add({ severity: 'success', summary: 'Thành công', detail: 'Đã xóa sản phẩm', life: 1500 })
        loadProducts()
    } catch {
        toast.add({ severity: 'error', summary: 'Lỗi', detail: 'Xóa thất bại', life: 2000 })
    }
}

const getSalePrice = (p: any) => {
    if (!p?.price) return 0
    if (p.discountPercent) return Math.round(p.price * (1 - p.discountPercent / 100))
    return p.price
}

const parseStock = (val: any) => {
    const n = Number(val)
    return Number.isNaN(n) ? 0 : n
}

const getStockRemaining = (p: any) => parseStock(p?.amount)

const viewProductDetail = async (p: any) => {
    showDetailDialog.value = true
    detailLoading.value = true
    detailProduct.value = null
    detailColors.value = []
    detailMaterials.value = []
    detailDimensions.value = []
    try {
        const [prodRes, colorRes, matRes, dimRes] = await Promise.all([
            axios.get(`${PRODUCT_API}/${p.id}`),
            axios.get(`${API_URL}/detailcolor/all/${p.id}`),
            axios.get(`${API_URL}/detailmaterial/all/${p.id}`),
            axios.get(`${API_URL}/dimensions/all/${p.id}`)
        ])
        detailProduct.value = prodRes.data
        detailColors.value = (colorRes.data || []).map((c: any) => c.nameColor || c.name).filter(Boolean)
        detailMaterials.value = (matRes.data || []).map((m: any) => m.nameMaterial || m.name).filter(Boolean)
        detailDimensions.value = (dimRes.data || []).map((d: any) => d.nameD).filter(Boolean)
    } catch {
        toast.add({ severity: 'error', summary: 'Lỗi', detail: 'Không thể tải chi tiết sản phẩm', life: 2000 })
        showDetailDialog.value = false
    } finally {
        detailLoading.value = false
    }
}

const exportExcel = async () => {
    try {
        const res = await axios.get(`${PRODUCT_API}/export-excel`, { responseType: 'blob' })
        const url = window.URL.createObjectURL(new Blob([res.data]))
        const link = document.createElement('a')
        link.href = url
        link.setAttribute('download', 'products.xlsx')
        document.body.appendChild(link)
        link.click()
        link.remove()
        window.URL.revokeObjectURL(url)
        toast.add({ severity: 'success', summary: 'Thành công', detail: 'Đã xuất file Excel', life: 2000 })
    } catch {
        toast.add({ severity: 'error', summary: 'Lỗi', detail: 'Xuất Excel thất bại', life: 2000 })
    }
}

// ========== IMPORT LOGIC ==========
const triggerImportFile = () => importFileRef.value?.click()
const onImportFileSelect = async (event: Event) => {
    const input = event.target as HTMLInputElement
    if (!input.files || input.files.length === 0) return
    const file = input.files[0]
    const formData = new FormData()
    formData.append('file', file)
    loading.value = true
    try {
        const res = await axios.post(`${PRODUCT_API}/preview-import`, formData, {
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
    // Chỉ nhập những sản phẩm CHƯA tồn tại
    const newItems = importPreviewList.value.filter(it => !it.isExisting)
    
    if (newItems.length === 0) {
        toast.add({ severity: 'warn', summary: 'Thông báo', detail: 'Tất cả sản phẩm đã tồn tại trong hệ thống. Không có gì để nhập mới.', life: 4000 })
        visibleImportDialog.value = false
        return
    }

    isConfirmingImport.value = true
    try {
        const res = await axios.post(`${PRODUCT_API}/confirm-import`, newItems)
        toast.add({ severity: 'success', summary: 'Thành công', detail: `Đã nhập mới ${newItems.length} sản phẩm.`, life: 3000 })
        visibleImportDialog.value = false
        loadProducts()
    } catch (err: any) {
        toast.add({ severity: 'error', summary: 'Lỗi', detail: err.response?.data || 'Import thất bại', life: 3000 })
    } finally {
        isConfirmingImport.value = false
    }
}

// ========== PAGINATION ==========
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
    if (timeout) clearTimeout(timeout)
    timeout = setTimeout(() => {
        currentPage.value = 0
        loadProducts()
    }, 400)
})
watch([pageSize, sortOption, currentPage], () => loadProducts())

onMounted(() => {
    loadProducts()
    loadMetaData()
})
</script>

<template>
    <div class="p-page">
        <Toast />

        <div class="p-header">
            <div class="p-header-left">
                <i class="fas fa-boxes p-icon"></i>
                <h2>Quản lý Sản phẩm</h2>
            </div>
            <div class="p-header-actions">
                <button class="p-btn p-btn-add" @click="openAddDialog">
                    <i class="fas fa-plus"></i> Thêm sản phẩm
                </button>
                <button class="p-btn p-btn-import" @click="triggerImportFile">
                    <i class="fas fa-file-import"></i> Nhập Excel
                </button>
                <input type="file" ref="importFileRef" hidden accept=".xlsx, .xls" @change="onImportFileSelect" />
                <button class="p-btn p-btn-export" @click="exportExcel">
                    <i class="fas fa-file-excel"></i> Xuất Excel
                </button>
            </div>
        </div>

        <div class="p-toolbar">
            <div class="p-toolbar-left">
                <div class="p-select-group">
                    <label>Hiển thị:</label>
                    <select v-model="pageSize">
                        <option v-for="o in pageSizeOptions" :key="o" :value="o">{{ o }}</option>
                    </select>
                </div>
                <div class="p-select-group">
                    <label>Sắp xếp:</label>
                    <select v-model="sortOption">
                        <option value="newest">Mới nhất</option>
                        <option value="price_asc">Giá tăng dần</option>
                        <option value="price_desc">Giá giảm dần</option>
                        <option value="az">A-Z</option>
                        <option value="za">Z-A</option>
                    </select>
                </div>
            </div>
            <div class="p-toolbar-right">
                <div class="p-search-box">
                    <i class="fas fa-search"></i>
                    <input type="text" v-model="search" placeholder="Tìm tên sản phẩm, mô tả...">
                </div>
            </div>
        </div>

        <div class="p-table-wrapper">
            <table class="p-table">
                <thead>
                    <tr>
                        <th style="width:60px">ID</th>
                        <th style="width:100px">Ảnh</th>
                        <th>Tên sản phẩm</th>
                        <th>Danh mục</th>
                        <th>Nhà cung cấp</th>
                        <th>Giá</th>
                        <th style="width:80px">Tồn kho</th>
                        <th style="width:90px">Đã bán</th>
                        <th>Voucher</th>
                        <th style="width:120px">Bảo hành</th>
                        <th style="width:150px">Ngày tạo</th>
                        <th style="width:150px">Ngày sửa</th>
                        <th style="width:120px">Thao tác</th>
                    </tr>
                </thead>
                <tbody>
                    <tr v-if="loading">
                        <td colspan="13" class="text-center py-5"><i class="fas fa-spinner fa-spin"></i> Đang tải...</td>
                    </tr>
                    <tr v-else-if="productList.length === 0">
                        <td colspan="13" class="text-center py-5">Không tìm thấy sản phẩm</td>
                    </tr>
                    <tr v-for="p in productList" :key="p.id" class="p-row p-row-clickable" @click="viewProductDetail(p)">
                        <td class="text-center">{{ p.id }}</td>
                        <td class="text-center">
                            <img :src="Helper.GetImageUrl(p.avatar)" class="p-table-img" />
                        </td>
                        <td><span class="p-name">{{ p.name }}</span></td>
                        <td><span class="p-badge-cat">{{ p.category?.name || '—' }}</span></td>
                        <td>{{ p.supplier?.name || '—' }}</td>
                        <td class="font-bold text-blue-600">
                            {{ Helper.ToMoney(p.price) }}
                            <div v-if="p.discountPercent" class="text-orange-500 text-xs">
                                <i class="fas fa-arrow-down mr-1"></i>{{ p.discountPercent }}%
                            </div>
                        </td>
                        <td class="text-center">
                            <span :class="getStockRemaining(p) > 0 ? 'text-green-600' : 'text-red-500'" class="font-bold">
                                {{ getStockRemaining(p) }}
                            </span>
                        </td>
                        <td class="text-center">
                            <span class="font-bold text-blue-600">{{ p.soldQuantity ?? 0 }}</span>
                        </td>
                        <td>
                            <span v-if="p.voucher" class="p-voucher-badge">
                                <i class="fas fa-ticket-alt mr-1"></i>{{ p.voucher.maVoucher }}
                            </span>
                            <span v-else class="text-gray-400 italic">Trống</span>
                        </td>
                        <td>
                            <span v-if="p.warranty" class="p-warranty-badge">
                                <i class="fas fa-shield-alt mr-1"></i>{{ p.warranty }}
                            </span>
                            <span v-else class="text-gray-400 italic">—</span>
                        </td>
                        <td class="p-date-cell">{{ Helper.DateFormat(p.createdAt) }}</td>
                        <td class="p-date-cell">{{ Helper.DateFormat(p.updatedAt) }}</td>
                        <td class="text-center" @click.stop>
                            <div class="p-actions">
                                <button class="p-action-btn p-edit" @click="openEditDialog(p)"><i class="fas fa-edit"></i></button>
                                <button class="p-action-btn p-delete" @click="deleteProduct(p.id)"><i class="fas fa-trash"></i></button>
                            </div>
                        </td>
                    </tr>
                </tbody>
            </table>
        </div>

        <div class="p-pagination-bar" v-if="totalPages > 0">
            <div>
                Hiển thị {{ currentPage * pageSize + 1 }} - {{ Math.min((currentPage + 1) * pageSize, totalItems) }} / {{ totalItems }} sản phẩm
            </div>
            <div class="p-pagination">
                <button class="p-page-btn" :disabled="currentPage === 0" @click="goToPage(currentPage - 1)">
                    <i class="fas fa-chevron-left"></i>
                </button>
                <button v-for="pg in pages" :key="pg" class="p-page-btn" :class="{ 'p-page-active': pg === currentPage }" @click="goToPage(pg)">
                    {{ pg + 1 }}
                </button>
                <button class="p-page-btn" :disabled="currentPage >= totalPages - 1" @click="goToPage(currentPage + 1)">
                    <i class="fas fa-chevron-right"></i>
                </button>
            </div>
        </div>

        <!-- DIALOG THÊM / SỬA -->
        <Dialog v-model:visible="visibleDialog" modal class="p-dialog-modern" :style="{ width: '900px' }" :header="editMode ? '✏️ Chỉnh sửa sản phẩm' : '🆕 Thêm sản phẩm mới'">
            <div class="p-form-container">
                <div class="p-form-main">
                    <div class="p-section-title">Thông tin cơ bản</div>
                    <div class="p-grid">
                        <div class="p-col-12 p-field">
                            <label><i class="fas fa-tag mr-2"></i>Tên sản phẩm</label>
                            <InputText v-model="selectedProduct.name" class="p-input-premium" placeholder="Nhập tên sản phẩm..." />
                        </div>
                        
                        <div class="p-col-6 p-field">
                            <label><i class="fas fa-layer-group mr-2"></i>Danh mục</label>
                            <Dropdown v-model="selectedProduct.category" :options="categoryList" optionLabel="name" placeholder="Chọn danh mục" class="p-dropdown-premium" />
                        </div>
                        <div class="p-col-6 p-field">
                            <label><i class="fas fa-truck mr-2"></i>Nhà cung cấp</label>
                            <Dropdown v-model="selectedProduct.supplier" :options="supplierList" optionLabel="name" placeholder="Chọn nhà cung cấp" class="p-dropdown-premium" />
                        </div>

                        <div class="p-col-6 p-field">
                            <label><i class="fas fa-coins mr-2"></i>Giá bán (VNĐ)</label>
                            <InputNumber v-model="selectedProduct.price" class="p-input-premium" inputClass="p-input-premium" mode="currency" currency="VND" locale="vi-VN" style="width: 100%" />
                        </div>
                        <div class="p-col-6 p-field">
                            <label><i class="fas fa-archive mr-2"></i>Số lượng nhập kho</label>
                            <InputNumber v-model="selectedProduct.amount" class="p-input-premium" inputClass="p-input-premium" style="width: 100%" />
                        </div>
                        <div class="p-col-6 p-field">
                            <label><i class="fas fa-percentage mr-2"></i>Giảm giá (%)</label>
                            <InputNumber v-model="selectedProduct.discountPercent" class="p-input-premium" inputClass="p-input-premium" :min="0" :max="100" style="width: 100%" />
                        </div>
                        <div class="p-col-6 p-field">
                            <label><i class="fas fa-shield-alt mr-2"></i>Bảo hành</label>
                            <InputText v-model="selectedProduct.warranty" class="p-input-premium" placeholder="VD: 12 tháng, 2 năm..." style="width: 100%" />
                        </div>

                        <div class="p-col-12 p-field">
                            <label><i class="fas fa-ticket-alt mr-2"></i>Mã Voucher áp dụng</label>
                            <Dropdown v-model="selectedProduct.voucher" :options="voucherList" optionLabel="maVoucher" placeholder="Chọn voucher ưu đãi cho sản phẩm" class="p-dropdown-premium" showClear />
                        </div>

                        <div class="p-col-12 p-field">
                            <label><i class="fas fa-align-left mr-2"></i>Mô tả chi tiết</label>
                            <Textarea v-model="selectedProduct.description" rows="4" class="p-input-premium p-textarea" placeholder="Nội dung mô tả sản phẩm..." />
                        </div>
                    </div>
                </div>

                <div class="p-form-sidebar">
                    <div class="p-section-title">Hình ảnh chuyên sâu</div>
                    <div class="p-image-management">
                        <div class="p-upload-zone" @click="triggerFileInput">
                            <i class="fas fa-cloud-upload-alt"></i>
                            <span>Tải ảnh lên</span>
                            <input ref="fileInputRef" type="file" accept="image/*" multiple hidden @change="onFileSelect" />
                        </div>
                        
                        <div class="p-image-preview-list">
                            <!-- Hiển thị ảnh đã có -->
                            <div v-for="(url, idx) in uploadedImageUrls" :key="'up-'+idx" class="p-preview-card">
                                <img :src="Helper.GetImageUrl(url)" alt="Product" />
                                <div class="p-preview-overlay">
                                    <button class="p-remove-icon" @click="removeUploadedImage(idx)"><i class="fas fa-trash"></i></button>
                                </div>
                                <div v-if="idx === 0" class="p-main-badge">Chính</div>
                            </div>
                            
                            <!-- Hiển thị ảnh mới chọn -->
                            <div v-for="(src, idx) in previewImages" :key="'pre-'+idx" class="p-preview-card p-preview-new">
                                <img :src="src" alt="New Preview" />
                                <div class="p-preview-overlay">
                                    <button class="p-remove-icon" @click="removeImage(idx)"><i class="fas fa-trash"></i></button>
                                </div>
                                <div v-if="uploadedImageUrls.length === 0 && idx === 0" class="p-main-badge">Chính</div>
                            </div>
                        </div>
                    </div>
                    <div class="p-image-hint">
                        💡 Ảnh đầu tiên sẽ tự động được chọn làm đại diện. Kéo thả hoặc chọn nhiều ảnh cùng lúc.
                    </div>
                </div>
            </div>
            
            <template #footer>
                <div class="p-modal-footer">
                    <button class="p-footer-btn p-btn-cancel" @click="visibleDialog = false" :disabled="isUploading">
                        <i class="fas fa-times mr-2"></i>Hủy bỏ
                    </button>
                    <button class="p-footer-btn p-btn-save" @click="saveProduct" :disabled="isUploading">
                        <template v-if="isUploading">
                            <i class="fas fa-spinner fa-spin mr-2"></i>Đang lưu...
                        </template>
                        <template v-else>
                            <i class="fas fa-check-circle mr-2"></i>{{ editMode ? 'Cập nhật' : 'Lưu sản phẩm' }}
                        </template>
                    </button>
                </div>
            </template>
        </Dialog>

        <!-- DIALOG REVIEW IMPORT -->
        <Dialog v-model:visible="visibleImportDialog" modal class="p-import-dialog" :style="{ width: '90vw' }" header="📥 Nhập sản phẩm từ Excel">
            <div class="p-import-review">
                <div class="p-info-bar">
                    <i class="fas fa-info-circle"></i>
                    <span>Xem trước <b>{{ importPreviewList.length }}</b> sản phẩm sẽ được nhập. Kiểm tra thông tin Danh mục/Nhà cung cấp/Voucher kỹ trước khi xác nhận.</span>
                </div>
                
                <div class="p-import-table-wrapper">
                    <table class="p-import-table">
                        <thead>
                            <tr>
                                <th style="width: 50px">STT</th>
                                <th style="width: 80px">Ảnh</th>
                                <th>Tên sản phẩm</th>
                                <th style="width: 150px">Giá</th>
                                <th style="width: 80px">Giảm (%)</th>
                                <th style="width: 80px">Tồn kho</th>
                                <th>Danh mục</th>
                                <th>Nhà cung cấp</th>
                                <th>Voucher</th>
                                <th>Bảo hành</th>
                                <th style="width: 120px">Trạng thái</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr v-for="(prod, idx) in importPreviewList" :key="idx">
                                <td class="text-center font-bold">{{ idx + 1 }}</td>
                                <td class="text-center">
                                    <img :src="Helper.GetImageUrl(prod.avatar)" alt="Preview" class="p-import-img-preview" />
                                </td>
                                <td class="p-import-name">{{ prod.name }}</td>
                                <td class="font-bold text-blue-600">{{ Helper.ToMoney(prod.price) }}</td>
                                <td class="text-center text-orange-500">{{ prod.discountPercent }}%</td>
                                <td class="text-center font-bold">{{ prod.amount }}</td>
                                <td>
                                    <span v-if="prod.category?.id" class="p-import-tag-success">{{ prod.category.name }}</span>
                                    <span v-else class="p-import-tag-error">{{ prod.category?.name || 'Mặc định' }}</span>
                                </td>
                                <td>
                                    <span v-if="prod.supplier?.id" class="p-import-tag-success">{{ prod.supplier.name }}</span>
                                    <span v-else class="p-import-tag-error">{{ prod.supplier?.name || 'Mặc định' }}</span>
                                </td>
                                <td>
                                    <span v-if="prod.voucher?.id" class="p-import-tag-success">{{ prod.voucher.name }}</span>
                                    <span v-else class="p-import-tag-error">{{ prod.voucher?.name || 'Trống' }}</span>
                                </td>
                                <td>{{ prod.warranty || '—' }}</td>
                                <td class="text-center">
                                    <span v-if="prod.isExisting" class="p-import-tag-error">
                                        <i class="fas fa-exclamation-triangle mr-1"></i> Đã tồn tại
                                    </span>
                                    <span v-else class="p-import-tag-success">
                                        <i class="fas fa-check-circle mr-1"></i> Có thể nhập
                                    </span>
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
                            <i class="fas fa-check-circle mr-2"></i>Xác nhận nhập {{ importPreviewList.length }} sản phẩm
                        </template>
                    </button>
                </div>
            </template>
        </Dialog>

        <!-- CHI TIẾT SẢN PHẨM -->
        <Dialog v-model:visible="showDetailDialog" modal class="p-detail-dialog" :style="{ width: '960px' }" header="Thông tin sản phẩm">
            <div v-if="detailLoading" class="text-center py-5">
                <i class="fas fa-spinner fa-spin fa-2x text-primary"></i>
                <p class="mt-3 text-muted">Đang tải chi tiết...</p>
            </div>
            <div v-else-if="detailProduct" class="p-detail-body">
                <div class="p-detail-top">
                    <div class="p-detail-image">
                        <img :src="Helper.GetImageUrl(detailProduct.avatar)" :alt="detailProduct.name" />
                    </div>
                    <div class="p-detail-info">
                        <div class="p-detail-row"><label>ID:</label><span>{{ detailProduct.id }}</span></div>
                        <div class="p-detail-row"><label>Tên sản phẩm:</label><span class="fw-bold">{{ detailProduct.name }}</span></div>
                        <div class="p-detail-row"><label>Giá:</label><span class="text-danger fw-bold fs-5">{{ Helper.ToMoney(getSalePrice(detailProduct)) }}</span></div>
                        <div class="p-detail-row" v-if="detailProduct.discountPercent">
                            <label>Giá gốc:</label>
                            <span class="text-muted text-decoration-line-through">{{ Helper.ToMoney(detailProduct.price) }}</span>
                        </div>
                        <div class="p-detail-row"><label>Số lượng trong kho:</label><span>{{ getStockRemaining(detailProduct) }}</span></div>
                        <div class="p-detail-row"><label>Đã bán:</label><span class="text-primary fw-bold">{{ detailProduct.soldQuantity ?? 0 }}</span></div>
                        <div class="p-detail-row"><label>Giảm giá:</label><span>{{ detailProduct.discountPercent ? detailProduct.discountPercent + '%' : '—' }}</span></div>
                        <div class="p-detail-row"><label>Danh mục:</label><span>{{ detailProduct.category?.name || '—' }}</span></div>
                        <div class="p-detail-row"><label>Nhà cung cấp:</label><span>{{ detailProduct.supplier?.name || '—' }}</span></div>
                        <div class="p-detail-row"><label>Màu sắc:</label><span>{{ detailColors.length ? detailColors.join(', ') : '—' }}</span></div>
                        <div class="p-detail-row"><label>Chất liệu:</label><span>{{ detailMaterials.length ? detailMaterials.join(', ') : '—' }}</span></div>
                        <div class="p-detail-row"><label>Kích cỡ:</label><span>{{ detailDimensions.length ? detailDimensions.join(', ') : '—' }}</span></div>
                        <div class="p-detail-row"><label>Mã voucher:</label><span>{{ detailProduct.voucher?.maVoucher || '—' }}</span></div>
                        <div class="p-detail-row"><label>Bảo hành:</label><span>{{ detailProduct.warranty || '—' }}</span></div>
                        <div class="p-detail-row"><label>Ngày tạo:</label><span>{{ Helper.DateFormat(detailProduct.createdAt) }}</span></div>
                        <div class="p-detail-row"><label>Ngày sửa:</label><span>{{ Helper.DateFormat(detailProduct.updatedAt) }}</span></div>
                    </div>
                </div>
                <div class="p-detail-desc" v-if="detailProduct.description">
                    <h5 class="p-detail-desc-title">Mô tả</h5>
                    <p>{{ detailProduct.description }}</p>
                </div>
            </div>
            <template #footer>
                <div class="p-modal-footer">
                    <button class="p-footer-btn p-btn-cancel" @click="showDetailDialog = false">
                        <i class="fas fa-times mr-2"></i>Đóng
                    </button>
                </div>
            </template>
        </Dialog>
    </div>
</template>

<style scoped>
.p-page { padding: 24px; font-family: 'Inter', sans-serif; background: #fdfdfd; }
.p-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; }
.p-header-left { display: flex; align-items: center; gap: 12px; }
.p-icon { font-size: 32px; color: #f59e0b; }
.p-header-left h2 { margin: 0; font-size: 24px; font-weight: 800; color: #1e293b; }

.p-btn { display: flex; align-items: center; gap: 8px; padding: 12px 20px; border: none; border-radius: 10px; font-weight: 700; cursor: pointer; transition: 0.3s cubic-bezier(0.4, 0, 0.2, 1); color: white; }
.p-btn-add { background: #f59e0b; box-shadow: 0 4px 6px -1px rgba(245, 158, 11, 0.3); }
.p-btn-import { background: #6366f1; box-shadow: 0 4px 6px -1px rgba(99, 102, 241, 0.3); margin-left: 12px; }
.p-btn-export { background: #10b981; box-shadow: 0 4px 6px -1px rgba(16, 185, 129, 0.3); margin-left: 12px; }
.p-btn:hover { transform: translateY(-2px); box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1); opacity: 0.95; }
.p-header-actions { display: flex; }

.p-toolbar { display: flex; justify-content: space-between; align-items: center; background: white; padding: 16px; border-radius: 16px; margin-bottom: 20px; border: 1px solid #f1f5f9; box-shadow: 0 1px 3px rgba(0,0,0,0.05); }
.p-toolbar-left { display: flex; gap: 24px; }
.p-select-group { display: flex; align-items: center; gap: 10px; font-size: 14px; font-weight: 500; color: #64748b; }
.p-select-group select { padding: 8px 16px; border-radius: 8px; border: 1px solid #e2e8f0; background: #f8fafc; outline: none; }

.p-search-box { display: flex; align-items: center; gap: 10px; background: #f8fafc; border: 1px solid #e2e8f0; padding: 10px 18px; border-radius: 10px; width: 350px; transition: 0.2s; }
.p-search-box:focus-within { border-color: #3b82f6; background: white; box-shadow: 0 0 0 4px rgba(59, 130, 246, 0.1); }
.p-search-box input { border: none; background: transparent; outline: none; flex: 1; font-size: 14px; }

.p-table-wrapper {
  border-radius: 16px;
  overflow-x: auto;
  overflow-y: hidden;
  border: 1px solid #f1f5f9;
  background: white;
  box-shadow: 0 4px 20px rgba(0,0,0,0.03);
}
.p-table { width: 100%; min-width: 1320px; border-collapse: collapse; }
.p-table thead { background: #f8fafc; border-bottom: 2px solid #f1f5f9; }
.p-table th { padding: 16px; text-align: left; font-size: 12px; font-weight: 700; color: #64748b; text-transform: uppercase; letter-spacing: 0.05em; white-space: nowrap; }
.p-table td { padding: 16px; border-top: 1px solid #f1f5f9; color: #334155; font-size: 14px; vertical-align: middle; white-space: nowrap; }
.p-row:hover { background: #fbfcfe; }
.p-row-clickable { cursor: pointer; }
.p-row-clickable:hover { background: #eff6ff !important; }

.p-table-img { width: 50px; height: 50px; border-radius: 10px; object-fit: cover; border: 1px solid #f1f5f9; }
.p-name { font-weight: 700; color: #1e293b; }
.p-badge-cat { background: #eff6ff; color: #1d4ed8; padding: 4px 10px; border-radius: 6px; font-weight: 600; font-size: 11px; }
.p-voucher-badge { background: #f0fdf4; color: #15803d; padding: 4px 10px; border-radius: 6px; font-weight: 600; font-size: 11px; border: 1px solid #bbf7d0; display: inline-flex; align-items: center; }

.p-actions { display: flex; gap: 10px; justify-content: center; }
.p-action-btn { width: 36px; height: 36px; border: none; border-radius: 10px; cursor: pointer; transition: 0.2s; display: flex; align-items: center; justify-content: center; font-size: 15px; }
.p-edit { background: #fef9c3; color: #854d0e; }
.p-delete { background: #fee2e2; color: #991b1b; }

.p-pagination-bar { display: flex; justify-content: space-between; align-items: center; margin-top: 24px; font-size: 14px; color: #64748b; }
.p-pagination { display: flex; gap: 8px; }
.p-page-btn { padding: 8px 16px; border: 1px solid #e2e8f0; border-radius: 10px; background: white; cursor: pointer; font-weight: 600; }
.p-page-active { background: #3b82f6; color: white; border-color: #3b82f6; }

/* IMPORT DIALOG STYLES */
.p-import-dialog { border-radius: 16px; overflow: hidden; }
.p-import-review { padding: 12px 24px 24px; }
.p-info-bar { background: #eff6ff; border: 1px solid #bfdbfe; color: #1e40af; padding: 14px 20px; border-radius: 12px; margin-bottom: 20px; display: flex; align-items: center; gap: 12px; font-size: 14px; }
.p-info-bar b { color: #1d4ed8; }
.p-import-table-wrapper { border: 1px solid #e2e8f0; border-radius: 12px; overflow-x: auto; max-height: 550px; }
.p-import-table { width: 100%; border-collapse: collapse; background: #fff; }
.p-import-table th { position: sticky; top: 0; background: #f8fafc; padding: 12px 16px; text-align: left; font-size: 12px; font-weight: 700; color: #64748b; text-transform: uppercase; border-bottom: 2px solid #e2e8f0; white-space: nowrap; z-index: 10; }
.p-import-table td { padding: 12px 16px; border-bottom: 1px solid #f1f5f9; font-size: 13px; color: #334155; }
.p-import-img-preview { width: 44px; height: 44px; border-radius: 8px; object-fit: cover; border: 1px solid #e2e8f0; }
.p-import-name { font-weight: 600; color: #1e293b; min-width: 250px; }
.p-import-tag-success { background: #f0fdf4; color: #15803d; padding: 2px 10px; border-radius: 6px; font-weight: 700; font-size: 11px; border: 1px solid #bbf7d0; }
.p-import-tag-error { background: #fff1f2; color: #be123c; padding: 2px 10px; border-radius: 6px; font-weight: 700; font-size: 11px; border: 1px solid #fecdd3; }

/* DIALOG MODAL FOOTER */
.p-modal-footer { display: flex; justify-content: flex-end; gap: 12px; padding: 16px 24px; background: #f8fafc; border-top: 1px solid #e2e8f0; }
.p-footer-btn { display: flex; align-items: center; gap: 8px; padding: 10px 24px; border: none; border-radius: 10px; font-weight: 700; cursor: pointer; transition: 0.2s; }
.p-btn-cancel { background: #fff; color: #64748b; border: 1px solid #e2e8f0; }
.p-btn-save { background: #f59e0b; color: #fff; }
.p-btn-confirm-import { background: linear-gradient(135deg, #6366f1, #4f46e5); color: #fff; box-shadow: 0 4px 6px -1px rgba(99, 102, 241, 0.3); }
.p-btn-confirm-import:hover { transform: translateY(-2px); box-shadow: 0 10px 15px -3px rgba(99, 102, 241, 0.4); }

.p-dialog-modern { border-radius: 20px; overflow: hidden; }
.p-form-container { display: grid; grid-template-columns: 1fr 320px; gap: 32px; padding: 20px; }
.p-section-title { font-size: 16px; font-weight: 700; color: #1e293b; margin-bottom: 20px; border-bottom: 2px solid #3b82f622; padding-bottom: 8px; }
.p-grid { display: grid; grid-template-columns: repeat(12, 1fr); gap: 16px; }
.p-col-6 { grid-column: span 6; }
.p-col-12 { grid-column: span 12; }
.p-field { display: flex; flex-direction: column; gap: 6px; }
.p-field label { font-size: 13px; font-weight: 700; color: #334155; margin-bottom: 4px; display: flex; align-items: center; }
.p-input-premium, .p-dropdown-premium, .p-textarea { 
    width: 100%; 
    border-radius: 10px !important; 
    border: 1px solid #cbd5e1 !important;
    transition: all 0.2s ease;
    background: #fff !important;
}
.p-input-premium:focus, .p-dropdown-premium:focus-within, .p-textarea:focus {
    border-color: #3b82f6 !important;
    box-shadow: 0 0 0 4px rgba(59, 130, 246, 0.1) !important;
    background: #fff !important;
}
:deep(.p-inputnumber), :deep(.p-inputnumber-input) { width: 100% !important; border-radius: 10px !important; }
:deep(.p-dropdown-label) { padding: 10px 12px !important; }
:deep(.p-inputtext) { padding: 10px 12px !important; }
.p-form-sidebar { background: #f8fafc; padding: 20px; border-radius: 16px; border: 1px solid #e2e8f0; }
.p-upload-zone { border: 2px dashed #cbd5e1; border-radius: 12px; padding: 20px; text-align: center; cursor: pointer; background: #fff; display: flex; flex-direction: column; gap: 8px; }
.p-image-preview-list { display: grid; grid-template-columns: repeat(3, 1fr); gap: 8px; margin-top: 16px; }
.p-preview-card { position: relative; aspect-ratio: 1; border-radius: 8px; overflow: hidden; border: 1px solid #e2e8f0; }
.p-preview-card img { width: 100%; height: 100%; object-fit: cover; }
.p-preview-overlay { position: absolute; inset: 0; background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center; opacity: 0; transition: 0.2s; }
.p-preview-card:hover .p-preview-overlay { opacity: 1; }
.p-main-badge { position: absolute; top: 4px; left: 4px; background: #3b82f6; color: #fff; font-size: 8px; padding: 2px 6px; border-radius: 4px; font-weight: 800; }

.p-detail-top { display: grid; grid-template-columns: 300px 1fr; gap: 24px; margin-bottom: 20px; }
.p-detail-image { background: #f8fafc; padding: 16px; border-radius: 12px; border: 1px solid #e2e8f0; }
.p-detail-image img { width: 100%; height: auto; border-radius: 8px; }
.p-detail-info { display: flex; flex-direction: column; gap: 8px; }
.p-detail-row { display: grid; grid-template-columns: 140px 1fr; gap: 8px; border-bottom: 1px solid #f1f5f9; padding: 4px 0; }
.p-detail-row label { font-weight: 600; color: #64748b; }

.p-warranty-badge { background: #ede9fe; color: #6d28d9; padding: 4px 10px; border-radius: 6px; font-weight: 600; font-size: 11px; border: 1px solid #c4b5fd; display: inline-flex; align-items: center; }
</style>