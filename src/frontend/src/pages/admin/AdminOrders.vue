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
const currentBill = ref<any>(null);
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
        
        // Lấy thông tin Bill để lấy discount
        const billRes = await axios.get(`http://localhost:8081/bill/order/${order.id}`);
        if(billRes.data && billRes.data.length > 0) {
            currentBill.value = billRes.data[0];
        } else {
            currentBill.value = null;
        }
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
        
        // NẠP LẠI DỮ LIỆU để cập nhật TotalMoney, Discount trong Bill
        await viewDetail(editOrder.value);
        
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

const exportExcel = () => {
    window.location.href = 'http://localhost:8081/order/export-excel';
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
            <div class="vc-header-actions">
                <button class="vc-btn-export" @click="exportExcel">
                    <i class="fas fa-file-excel me-2"></i>Xuất Excel
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
                        <th style="width:130px">Ngày đặt</th>
                        <th style="width:130px">Cập nhật</th>
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
                        <td class="text-center text-slate-700">
                             {{ item.createdDate ? Helper.DateFormat(item.createdDate) : '—' }}
                        </td>
                        <td class="text-center text-slate-500 italic">
                             {{ item.updatedDate ? Helper.DateFormat(item.updatedDate) : '—' }}
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
                    <div class="vc-detail-header-left">
                        <h3 class="font-bold text-slate-700 m-0">#{{ selectedOrder.id }} - Thông tin đơn hàng</h3>
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
                        <div class="vc-info-item">
                            <label>SĐT người nhận:</label>
                            <InputText v-if="isEditing" v-model="editOrder.receiverPhone" class="vc-itx" />
                            <span v-else>{{ selectedOrder.receiverPhone || '—' }}</span>
                        </div>
                    </div>
                </div>

                <div class="vc-items-table mt-4">
                    <table>
                        <thead>
                            <tr>
                                <th>Sản phẩm & Thuộc tính</th>
                                <th>Số lượng</th>
                                <th>Đơn giá</th>
                                <th>Thành tiền</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr v-for="it in orderItems" :key="it.id">
                                <td class="vc-it-prod">
                                    <div class="vc-it-flex">
                                        <div>
                                            <div class="vc-it-name" v-if="!isEditing">{{ it.product?.name }}</div>
                                            <select v-else v-model="it.product" class="vc-it-sel-prod" @change="it.price = it.product.price">
                                                <option v-for="p in masterProducts" :key="p.id" :value="p">{{ p.name }}</option>
                                            </select>
                                            
                                            <div class="vc-it-opts">
                                                <!-- Màu sắc -->
                                                <span class="vc-it-opt-badge clr" v-if="!isEditing && it.color">Màu: {{ it.color?.nameColor }}</span>
                                                <select v-else-if="isEditing" v-model="it.color" class="vc-it-sel clr-sel">
                                                    <option :value="null">-- Màu --</option>
                                                    <option v-for="c in masterColors" :key="c.id" :value="c">{{ c.nameColor }}</option>
                                                </select>
                                                
                                                <!-- Chất liệu -->
                                                <span class="vc-it-opt-badge mat" v-if="!isEditing && it.material">Chất liệu: {{ it.material?.nameMaterial }}</span>
                                                <select v-else-if="isEditing" v-model="it.material" class="vc-it-sel mat-sel">
                                                    <option :value="null">-- Chất liệu --</option>
                                                    <option v-for="m in masterMaterials" :key="m.id" :value="m">{{ m.nameMaterial }}</option>
                                                </select>

                                                <!-- Kích cỡ -->
                                                <span class="vc-it-opt-badge dim" v-if="!isEditing && it.dimensions">Kích cỡ: {{ it.dimensions?.nameD }}</span>
                                                <select v-else-if="isEditing" v-model="it.dimensions" class="vc-it-sel dim-sel">
                                                    <option :value="null">-- Kích cỡ --</option>
                                                    <option v-for="d in masterDimensions" :key="d.id" :value="d">{{ d.nameD }}</option>
                                                </select>
                                            </div>
                                        </div>
                                    </div>
                                </td>
                                <td class="text-center">
                                    <span v-if="!isEditing" class="vc-it-qty">{{ it.amount }}</span>
                                    <input v-else type="number" v-model="it.amount" class="vc-it-input qty-input" />
                                </td>
                                <td class="text-right">
                                    <span v-if="!isEditing" class="vc-it-price">{{ Helper.ToMoney(it.price) }}</span>
                                    <input v-else type="number" v-model="it.price" class="vc-it-input price-input" />
                                </td>
                                <td class="text-right font-bold text-blue-600">
                                    {{ Helper.ToMoney(it.amount * it.price) }}
                                </td>
                            </tr>
                        </tbody>
                        <tfoot v-if="currentBill">
                            <tr class="bg-gray-50">
                                <td colspan="3" class="text-right font-semibold py-2">Tổng tiền hàng:</td>
                                <td class="text-right font-bold py-2">{{ Helper.ToMoney(currentBill.totalMoney) }}</td>
                            </tr>
                            <tr class="bg-gray-50">
                                <td colspan="3" class="text-right font-semibold py-2 text-green-600">Giảm giá:</td>
                                <td class="text-right font-bold py-2 text-green-600">-{{ Helper.ToMoney(currentBill.discount || 0) }}</td>
                            </tr>
                            <tr class="bg-blue-50">
                                <td colspan="3" class="text-right font-bold py-3 text-lg text-blue-700">Tổng thanh toán:</td>
                                <td class="text-right font-bold py-3 text-lg text-blue-700">{{ Helper.ToMoney(currentBill.totalMoneyaftersaleoff) }}</td>
                            </tr>
                        </tfoot>
                    </table>
                </div>
            </div>
            <template #footer>
                <div class="vc-dialog-footer">
                    <template v-if="!isEditing">
                        <Button label="Chỉnh sửa" icon="pi pi-user-edit" class="p-button-success" @click="startEdit" />
                        <Button label="Đóng" icon="pi pi-times" class="p-button-secondary" @click="showDetail = false" />
                    </template>
                    <template v-else>
                        <Button label="Lưu thay đổi" icon="pi pi-check" class="p-button-success" @click="saveOrder" />
                        <Button label="Hủy bỏ" icon="pi pi-times" class="p-button-danger" @click="cancelEdit" />
                    </template>
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
.vc-header-actions { display: flex; gap: 12px; }
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
.truncate-2 { display: -webkit-box; -webkit-line-clamp: 2; line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }

/* FIX COLUMN WRAP PAYMENT */
.vc-detail-header-bar { display: flex; justify-content: space-between; align-items: center; padding: 0 0 16px 0; margin-bottom: 16px; border-bottom: 2px solid #eff6ff; }
.vc-btn-edit-inline { background: #16a34a; color: white; border: none; padding: 8px 16px; border-radius: 8px; font-weight: 600; cursor: pointer; transition: 0.2s; }
.vc-btn-edit-inline:hover { background: #15803d; }
.vc-btn-save-inline { background: #16a34a; color: white; border: none; padding: 8px 20px; border-radius: 8px; font-weight: 600; cursor: pointer; }
.vc-btn-save-inline:hover { background: #15803d; }
.vc-btn-cancel-inline { background: #ef4444; color: white; border: none; padding: 8px 20px; border-radius: 8px; font-weight: 600; cursor: pointer; }
.vc-btn-cancel-inline:hover { background: #dc2626; }

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
.vc-date-info { display: flex; flex-direction: column; gap: 2px; font-size: 13px; }

/* ORDER DETAIL STYLES */
.vc-it-prod { padding: 12px !important; }
.vc-it-name { font-weight: 700; color: #1e293b; margin-bottom: 6px; font-size: 15px; }
.vc-it-opts { display: flex; flex-wrap: wrap; gap: 8px; }
.vc-it-opt-badge { padding: 4px 10px; border-radius: 6px; font-size: 11px; font-weight: 600; display: inline-flex; align-items: center; }
.clr { background: #fee2e2; color: #991b1b; }
.mat { background: #f0fdf4; color: #166534; }
.dim { background: #eff6ff; color: #1e40af; }
.vc-it-sel { padding: 4px 8px; border-radius: 6px; border: 1px solid #cbd5e1; font-size: 12px; max-width: 130px; }
.vc-it-sel-prod { width: 100%; padding: 6px; border-radius: 6px; border: 1px solid #3b82f6; margin-bottom: 8px; }
.vc-it-qty { font-weight: 700; color: #475569; }
.vc-it-price { font-weight: 600; color: #1e293b; }
.qty-input, .price-input { width: 80px; padding: 6px; border-radius: 6px; border: 1px solid #cbd5e1; text-align: center; }
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
.vc-btn-export { background: #16a34a; color: white; border: none; padding: 10px 20px; border-radius: 8px; font-weight: 600; cursor: pointer; transition: 0.2s; display: flex; align-items: center; }
.vc-btn-export:hover { background: #15803d; transform: translateY(-1px); box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1); }

.vc-items-table table { width: 100%; border-collapse: collapse; }
.vc-items-table th { background: #f1f5f9; padding: 12px; text-align: left; font-size: 12px; color: #475569; border-bottom: 2px solid #e2e8f0; text-transform: uppercase; }
.vc-items-table td { padding: 12px; border-bottom: 1px solid #f1f5f9; vertical-align: middle; }
.text-center { text-align: center; }
.text-right { text-align: right; }
.font-bold { font-weight: 700; }
.text-orange-600 { color: #ea580c; }
</style>
