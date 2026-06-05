<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue';
import axios from 'axios';
import Helper from '../../helper/helper';
import Toast from 'primevue/toast';
import { useToast } from 'primevue/usetoast';
import Dialog from 'primevue/dialog';
import Button from 'primevue/button';
import InputText from 'primevue/inputtext';

const toast = useToast();
const API = 'http://localhost:8081/order/admin';

const list = ref<any[]>([]);
const totalItems = ref(0);
const totalPages = ref(0);
const loading = ref(false);

const selectedOrder = ref<any>(null);
const editOrder = ref<any>(null); // Dùng cho chế độ edit
const isEditing = ref(false);

const showDetail = ref(false);
const orderItems = ref<any[]>([]);
const itemsLoading = ref(false);

// Master Data
const masterProducts = ref<any[]>([]);
const masterColors = ref<any[]>([]);
const masterMaterials = ref<any[]>([]);
const masterDimensions = ref<any[]>([]);

const search = ref('');
const pageSize = ref(10);
const currentPage = ref(0);
const sortOption = ref('newest');
const pageSizeOptions = [10, 20, 50, 100];

const loadMasterData = async () => {
    try {
        const [p, c, m, d] = await Promise.all([
            axios.get('http://localhost:8081/order/products'),
            axios.get('http://localhost:8081/order/colors'),
            axios.get('http://localhost:8081/order/materials'),
            axios.get('http://localhost:8081/order/dimensions')
        ]);
        masterProducts.value = p.data;
        masterColors.value = c.data;
        masterMaterials.value = m.data;
        masterDimensions.value = d.data;
    } catch (err) {
        console.error("Lỗi load master data", err);
    }
};

const loadData = async () => {
    loading.value = true;
    try {
        const params: Record<string, string | number> = {
            page: currentPage.value,
            size: pageSize.value,
            sort: sortOption.value
        };
        if (search.value.trim()) params.search = search.value.trim();
        
        const res = await axios.get(`${API}/all`, { params });
        list.value = res.data.content || [];
        totalItems.value = res.data.totalElements || 0;
        totalPages.value = res.data.totalPages || 0;
    } catch {
        toast.add({ severity: 'error', summary: 'Lỗi', detail: 'Không thể tải danh sách đơn hàng', life: 2000 });
    } finally {
        loading.value = false;
    }
};

const viewDetail = async (order: any) => {
    selectedOrder.value = { ...order };
    editOrder.value = JSON.parse(JSON.stringify(order)); // Deep copy
    isEditing.value = false;
    showDetail.value = true;
    itemsLoading.value = true;
    try {
        const res = await axios.get(`${API}/detail/${order.id}`);
        orderItems.value = res.data;
    } catch {
        toast.add({ severity: 'error', summary: 'Lỗi', detail: 'Không thể tải chi tiết đơn hàng', life: 2000 });
    } finally {
        itemsLoading.value = false;
    }
};

const startEdit = () => {
    isEditing.value = true;
};

const cancelEdit = () => {
    editOrder.value = JSON.parse(JSON.stringify(selectedOrder.value));
    isEditing.value = false;
};

const saveOrder = async () => {
    try {
        // 1. Lưu thông tin chung đơn hàng
        await axios.post(`${API}/update/${editOrder.value.id}`, editOrder.value);
        
        // 2. Lưu thông tin chi tiết từng sản phẩm
        for(const item of orderItems.value) {
            await axios.post(`${API}/update-item/${item.id}`, {
                product: item.product,
                amount: item.amount,
                price: item.price,
                color: item.color,
                material: item.material,
                dimensions: item.dimensions
            });
        }

        toast.add({ severity: 'success', summary: 'Thành công', detail: 'Đã cập nhật toàn bộ đơn hàng', life: 1500 });
        
        selectedOrder.value = JSON.parse(JSON.stringify(editOrder.value));
        isEditing.value = false;
        loadData();
    } catch (err: any) {
        toast.add({ severity: 'error', summary: 'Lỗi', detail: err.response?.data || 'Không thể cập nhật đơn hàng', life: 2000 });
    }
};

const deleteOrder = async (id: number) => {
    if (!confirm('Bạn có chắc muốn xóa đơn hàng này? Lưu ý: Mọi hóa đơn và chi tiết liên quan sẽ bị xóa sạch.')) return;
    try {
        const res = await axios.delete(`${API}/delete/${id}`);
        toast.add({ severity: 'success', summary: 'Thành công', detail: res.data, life: 1500 });
        loadData();
    } catch (err: any) {
        toast.add({ severity: 'error', summary: 'Lỗi', detail: err.response?.data || 'Xóa đơn hàng thất bại', life: 3000 });
    }
};

const pages = computed(() => {
    const arr: number[] = [];
    const max = 5;
    let start = Math.max(0, currentPage.value - 2);
    let end = Math.min(totalPages.value, start + max);
    if (end - start < max) start = Math.max(0, end - max);
    for (let i = start; i < end; i++) arr.push(i);
    return arr;
});

const goToPage = (p: number) => {
    if (p >= 0 && p < totalPages.value) currentPage.value = p;
};

let timeout: ReturnType<typeof setTimeout> | null = null;
watch(search, () => {
    if (timeout) clearTimeout(timeout);
    timeout = setTimeout(() => { currentPage.value = 0; loadData(); }, 400);
});
watch([pageSize, sortOption], () => { currentPage.value = 0; loadData(); });
watch(currentPage, () => loadData());

onMounted(() => {
    loadData();
    loadMasterData();
});
</script>

<template>
    <div class="vc-page">
        <Toast />

        <div class="vc-header">
            <div class="vc-header-left">
                <i class="fas fa-shopping-bag vc-icon"></i>
                <h2>Quản lý đơn hàng</h2>
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
                    </select>
                </div>
            </div>
            <div class="vc-toolbar-right">
                <div class="vc-search-box">
                    <i class="fas fa-search"></i>
                    <input type="text" v-model="search" placeholder="Tìm theo ID, Tên, SĐT...">
                </div>
            </div>
        </div>

        <div class="vc-table-wrapper">
            <table class="vc-table">
                <thead>
                    <tr>
                        <th style="width:60px">STT</th>
                        <th style="width:80px">ID</th>
                        <th>Khách hàng</th>
                        <th>Địa chỉ giao hàng</th>
                        <th style="width:160px">Thanh toán</th>
                        <th style="width:150px">Vận chuyển</th>
                        <th style="width:120px">Thao tác</th>
                    </tr>
                </thead>
                <tbody>
                    <tr v-if="loading">
                        <td colspan="7" class="text-center py-5"><i class="fas fa-spinner fa-spin"></i> Đang tải...</td>
                    </tr>
                    <tr v-else-if="list.length === 0">
                        <td colspan="7" class="text-center py-5">Không tìm thấy đơn hàng nào</td>
                    </tr>
                    <tr v-for="(item, idx) in list" :key="item.id" class="vc-row">
                        <td class="text-center">{{ currentPage * pageSize + idx + 1 }}</td>
                        <td class="text-center">#{{ item.id }}</td>
                        <td>
                            <div class="vc-cust-info">
                                <span class="vc-cust-name">{{ item.customerName }}</span>
                                <small>{{ item.phone }}</small>
                                <small class="vc-cust-email">{{ item.email }}</small>
                            </div>
                        </td>
                        <td>
                            <div class="vc-address truncate-2" :title="item.addressO">
                                {{ item.addressO }}
                            </div>
                        </td>
                        <td class="text-center">
                            <span class="vc-badge-pay">{{ item.methodofPayment?.name_mop || 'N/A' }}</span>
                        </td>
                        <td class="text-center">
                            <span class="vc-status-badge">{{ item.shipMethod?.nameSM || 'Giao tận nơi' }}</span>
                        </td>
                        <td class="text-center">
                            <div class="vc-actions">
                                <button class="vc-action-btn vc-view" title="Xem chi tiết" @click="viewDetail(item)">
                                    <i class="fas fa-eye"></i>
                                </button>
                                <button class="vc-action-btn vc-delete" title="Xóa đơn hàng" @click="deleteOrder(item.id)">
                                    <i class="fas fa-trash"></i>
                                </button>
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

        <Dialog v-model:visible="showDetail" header="📦 Chi tiết đơn hàng" modal :style="{ width: '1000px' }" class="vc-dialog">
            <div v-if="selectedOrder" class="vc-order-detail">
                <div class="vc-detail-header-bar">
                    <h3 class="m-0 text-xl font-bold text-gray-700">#{{ selectedOrder.id }} - Thông tin đơn hàng</h3>
                    <div>
                        <button v-if="!isEditing" @click="startEdit" class="vc-btn-edit-inline">
                            <i class="fas fa-edit"></i> Chỉnh sửa đơn hàng
                        </button>
                        <div v-else class="flex gap-2">
                            <button @click="saveOrder" class="vc-btn-save-inline">
                                <i class="fas fa-check"></i> Lưu thay đổi
                            </button>
                            <button @click="cancelEdit" class="vc-btn-cancel-inline">
                                <i class="fas fa-times"></i> Hủy
                            </button>
                        </div>
                    </div>
                </div>

                <div class="vc-detail-info-card">
                    <div class="vc-info-grid">
                        <div class="vc-info-item">
                            <label>Khách hàng:</label>
                            <InputText v-if="isEditing" v-model="editOrder.customerName" class="vc-itx" />
                            <span v-else>{{ selectedOrder.customerName }}</span>
                        </div>
                        <div class="vc-info-item">
                            <label>Số điện thoại:</label>
                            <InputText v-if="isEditing" v-model="editOrder.phone" class="vc-itx" />
                            <span v-else>{{ selectedOrder.phone }}</span>
                        </div>
                        <div class="vc-info-item">
                            <label>Địa chỉ:</label>
                            <InputText v-if="isEditing" v-model="editOrder.addressO" class="vc-itx" />
                            <span v-else>{{ selectedOrder.addressO }}</span>
                        </div>
                        <div class="vc-info-item">
                            <label>Người nhận khác:</label>
                            <InputText v-if="isEditing" v-model="editOrder.receiverName" class="vc-itx" />
                            <span v-else>{{ selectedOrder.receiverName || '—' }}</span>
                        </div>
                    </div>
                </div>

                <div class="vc-items-table mt-4">
                    <table>
                        <thead>
                            <tr>
                                <th>Sản phẩm & Thuộc tính</th>
                                <th style="width:120px" class="text-center">Số lượng</th>
                                <th style="width:130px" class="text-right">Đơn giá</th>
                                <th style="width:130px" class="text-right">Thành tiền</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr v-for="it in orderItems" :key="it.id">
                                <td>
                                    <div class="vc-prod">
                                        <img :src="Helper.GetImageUrl(it.product?.avatar)" class="vc-prod-img" />
                                        <div class="vc-prod-info">
                                            <div v-if="isEditing" class="mb-2">
                                                <select v-model="it.product" class="vc-it-sel prod-sel">
                                                    <option v-for="p in masterProducts" :key="p.id" :value="p">{{ p.name }}</option>
                                                </select>
                                            </div>
                                            <span v-else class="vc-prod-name">{{ it.product?.name }}</span>
                                            
                                            <div v-if="isEditing" class="vc-item-selectors mt-1">
                                                <select v-model="it.color" class="vc-it-sel color-sel">
                                                    <option :value="null">-- Màu sắc --</option>
                                                    <option v-for="c in masterColors" :key="c.id" :value="c">{{ c.nameColor }}</option>
                                                </select>
                                                <select v-model="it.material" class="vc-it-sel mat-sel">
                                                    <option :value="null">-- Chất liệu --</option>
                                                    <option v-for="m in masterMaterials" :key="m.id" :value="m">{{ m.nameMaterial }}</option>
                                                </select>
                                                <select v-model="it.dimensions" class="vc-it-sel dim-sel">
                                                    <option :value="null">-- Kích cỡ --</option>
                                                    <option v-for="d in masterDimensions" :key="d.id" :value="d">{{ d.nameD }}</option>
                                                </select>
                                            </div>
                                            <div v-else class="vc-prod-attrs">
                                                <span v-if="it.color?.nameColor" class="vattr vat-color">Màu: {{ it.color.nameColor }}</span>
                                                <span v-if="it.material?.nameMaterial" class="vattr vat-mat">Chất liệu: {{ it.material.nameMaterial }}</span>
                                                <span v-if="it.dimensions?.nameD" class="vattr vat-dim">Kích cỡ: {{ it.dimensions.nameD }}</span>
                                            </div>
                                        </div>
                                    </div>
                                </td>
                                <td class="text-center">
                                    <InputText v-if="isEditing" v-model="it.amount" type="number" class="vc-itx-small" />
                                    <span v-else>{{ it.amount }}</span>
                                </td>
                                <td class="text-right">
                                    <InputText v-if="isEditing" v-model="it.price" class="vc-itx-small" />
                                    <span v-else>{{ Helper.ToMoney(it.price) }}</span>
                                </td>
                                <td class="text-right font-bold text-orange-600">
                                    {{ Helper.ToMoney(it.price * it.amount) }}
                                </td>
                            </tr>
                        </tbody>
                    </table>
                </div>
            </div>
            <template #footer>
                <div class="vc-dialog-footer">
                    <Button label="Đóng cửa sổ" icon="pi pi-times" class="p-button-secondary" @click="showDetail = false" />
                </div>
            </template>
        </Dialog>
    </div>
</template>

<style scoped>
/* Mix existing with new editing UI */
.vc-page { padding: 24px; font-family: 'Inter', sans-serif; }
.vc-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; border-bottom: 2px solid #f1f5f9; padding-bottom: 16px; }
.vc-header-left { display: flex; align-items: center; gap: 12px; }
.vc-icon { font-size: 28px; color: #3b82f6; }
.vc-header-left h2 { margin: 0; font-size: 22px; font-weight: 700; color: #1e293b; }

.vc-toolbar { display: flex; justify-content: space-between; align-items: center; background: #f8fafc; padding: 12px 16px; border-radius: 12px; margin-bottom: 16px; border: 1px solid #e2e8f0; }
.vc-toolbar-left { display: flex; gap: 20px; }
.vc-select-group { display: flex; align-items: center; gap: 8px; font-size: 14px; }
.vc-select-group select { padding: 6px 12px; border-radius: 6px; border: 1px solid #cbd5e1; outline: none; }
.vc-search-box { display: flex; align-items: center; gap: 8px; background: white; border: 1px solid #cbd5e1; padding: 8px 16px; border-radius: 8px; width: 300px; }
.vc-search-box input { border: none; outline: none; font-size: 14px; width: 100%; }

.vc-table-wrapper { border-radius: 12px; overflow: hidden; border: 1px solid #e2e8f0; box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1); }
.vc-table { width: 100%; border-collapse: collapse; background: white; }
.vc-table thead { background: #f1f5f9; }
.vc-table th { padding: 14px 16px; text-align: left; font-size: 13px; font-weight: 600; color: #475569; text-transform: uppercase; }
.vc-table td { padding: 14px 16px; border-top: 1px solid #f1f5f9; color: #1e293b; font-size: 14px; }
.vc-row:hover { background: #f8fafc; }

.vc-cust-info { display: flex; flex-direction: column; gap: 2px; }
.vc-cust-name { font-weight: 700; color: #3b82f6; }
.vc-cust-email { color: #94a3b8; font-style: italic; }
.vc-address { color: #475569; font-size: 13px; line-height: 1.4; }
.truncate-2 { display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }

/* FIX COLUMN WRAP PAYMENT */
.vc-badge-pay { 
    background: #3b82f6; color: white; padding: 4px 12px; border-radius: 20px; 
    font-size: 11px; font-weight: 700; text-transform: uppercase;
    display: inline-block; white-space: nowrap; max-width: 180px; overflow: hidden; text-overflow: ellipsis;
}
.vc-status-badge { background: #f1f5f9; color: #475569; padding: 4px 12px; border-radius: 20px; font-size: 11px; font-weight: 700; border: 1px solid #e2e8f0; white-space: nowrap; }

.vc-actions { display: flex; gap: 8px; justify-content: center; }
.vc-action-btn { width: 34px; height: 34px; border: none; border-radius: 8px; cursor: pointer; transition: 0.2s; display: flex; align-items: center; justify-content: center; }
.vc-view { background: #e0f2fe; color: #0369a1; }
.vc-view:hover { background: #bae6fd; }
.vc-delete { background: #fee2e2; color: #991b1b; }
.vc-delete:hover { background: #fecaca; }

.vc-pagination-bar { display: flex; justify-content: space-between; align-items: center; margin-top: 20px; font-size: 14px; color: #64748b; }
.vc-pagination { display: flex; gap: 6px; }
.vc-page-btn { padding: 8px 14px; border: 1px solid #e2e8f0; border-radius: 8px; background: white; cursor: pointer; }
.vc-page-active { background: #3b82f6; color: white; border-color: #3b82f6; }

/* ORDER DETAIL STYLES */
.vc-detail-header-bar { display: flex; justify-content: space-between; align-items: center; padding: 0 0 16px 0; margin-bottom: 16px; border-bottom: 2px solid #eff6ff; }
.vc-btn-edit-inline { background: #f0f9ff; color: #0369a1; border: 1px solid #bae6fd; padding: 8px 16px; border-radius: 8px; font-weight: 600; cursor: pointer; transition: 0.2s; }
.vc-btn-edit-inline:hover { background: #bae6fd; }
.vc-btn-save-inline { background: #22c55e; color: white; border: none; padding: 8px 16px; border-radius: 8px; font-weight: 600; cursor: pointer; }
.vc-btn-cancel-inline { background: #94a3b8; color: white; border: none; padding: 8px 16px; border-radius: 8px; font-weight: 600; cursor: pointer; }

.vc-detail-info-card { background: #f8fafc; padding: 24px; border-radius: 12px; border: 1px solid #e2e8f0; }
.vc-info-grid { display: grid; grid-template-cols: repeat(4, 1fr); gap: 15px; }
.vc-info-item { display: flex; flex-direction: column; gap: 6px; }
.vc-info-item label { font-size: 11px; color: #64748b; font-weight: 700; text-transform: uppercase; }
.vc-info-item span { font-weight: 600; color: #1e293b; font-size: 14px; }
.vc-itx { width: 100%; padding: 6px 10px; border: 1px solid #cbd5e1; border-radius: 6px; }
.vc-itx-small { width: 100%; padding: 4px 8px; font-size: 13px; text-align: center; border: 1px solid #cbd5e1; border-radius: 4px; }

/* EDITORS */
.vc-item-selectors { display: flex; gap: 5px; flex-direction: column; }
.vc-it-sel { padding: 4px; border-radius: 4px; border: 1px solid #cbd5e1; font-size: 11px; outline: none; }
.color-sel { background: #fffbeb; }
.mat-sel { background: #f0fdf4; }
.dim-sel { background: #f8fafc; }

.vc-prod { display: flex; align-items: center; gap: 14px; }
.vc-prod-img { width: 56px; height: 56px; object-fit: cover; border-radius: 10px; border: 1px solid #e2e8f0; }
.vc-prod-info { display: flex; flex-direction: column; gap: 4px; flex: 1; }
.vc-prod-name { font-weight: 700; color: #1e293b; font-size: 14px; }
.vc-prod-attrs { display: flex; gap: 8px; flex-wrap: wrap; }
.vattr { font-size: 10px; padding: 1px 6px; border-radius: 4px; font-weight: 600; }
.vat-color { background: #fef3c7; color: #92400e; }
.vat-mat { background: #dcfce7; color: #166534; }
.vat-dim { background: #f1f5f9; color: #475569; }

.vc-items-table table { width: 100%; border-collapse: collapse; }
.vc-items-table th { background: #f1f5f9; padding: 12px; text-align: left; font-size: 12px; color: #475569; border-bottom: 2px solid #e2e8f0; text-transform: uppercase; }
.vc-items-table td { padding: 12px; border-bottom: 1px solid #f1f5f9; vertical-align: middle; }
.text-center { text-align: center; }
.text-right { text-align: right; }
.font-bold { font-weight: 700; }
.text-orange-600 { color: #ea580c; }
</style>
