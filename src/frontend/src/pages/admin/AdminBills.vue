<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue';
import axios from 'axios';
import Toast from 'primevue/toast';
import { useToast } from 'primevue/usetoast';
import Dialog from 'primevue/dialog';
import Button from 'primevue/button';
import Helper from '../../helper/helper';

const API = 'http://localhost:8081/bill';
const ORDER_API = 'http://localhost:8081/order/admin';
const toast = useToast();

const list = ref<any[]>([]);
const loading = ref(false);
const totalItems = ref(0);
const totalPages = ref(0);
const currentPage = ref(0);
const pageSize = ref(10);
const pageSizeOptions = [5, 10, 20, 50];
const search = ref('');
const sortOption = ref('id,desc');

const showDetail = ref(false);
const selectedBill = ref<any>(null);
const orderItems = ref<any[]>([]);
const itemsLoading = ref(false);

const loadData = async () => {
    loading.value = true;
    try {
        const res = await axios.get(`${API}/list`, {
            params: {
                page: currentPage.value,
                size: pageSize.value,
                search: search.value,
                sort: sortOption.value
            }
        });
        list.value = res.data.content;
        totalItems.value = res.data.totalElements;
        totalPages.value = res.data.totalPages;
    } catch (err) {
        toast.add({ severity: 'error', summary: 'Lỗi', detail: 'Không thể tải danh sách hóa đơn', life: 3000 });
    } finally {
        loading.value = false;
    }
};

const exportExcel = () => {
    window.location.href = `${API}/export-excel`;
};

const viewDetail = async (bill: any) => {
    selectedBill.value = { ...bill };
    showDetail.value = true;
    orderItems.value = [];
    const orderId = bill.orderbill?.id;
    if (!orderId) return;

    itemsLoading.value = true;
    try {
        const res = await axios.get(`${ORDER_API}/detail/${orderId}`);
        orderItems.value = res.data || [];
    } catch {
        toast.add({ severity: 'error', summary: 'Lỗi', detail: 'Không thể tải chi tiết hóa đơn', life: 3000 });
    } finally {
        itemsLoading.value = false;
    }
};

const pages = computed(() => {
    const arr: number[] = [];
    for (let i = 0; i < totalPages.value; i++) arr.push(i);
    return arr;
});

const goToPage = (p: number) => {
    currentPage.value = p;
};

watch([pageSize, sortOption, currentPage], () => loadData());
let timeout: any = null;
watch(search, () => {
    if (timeout) clearTimeout(timeout);
    timeout = setTimeout(() => {
        currentPage.value = 0;
        loadData();
    }, 400);
});

onMounted(() => loadData());
</script>

<template>
    <div class="vc-page">
        <Toast />

        <div class="vc-header">
            <div class="vc-header-left">
                <i class="fas fa-file-invoice-dollar vc-icon"></i>
                <h2>Quản lý Hóa đơn</h2>
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
                        <option value="id,desc">Mới nhất</option>
                        <option value="id,asc">Cũ nhất</option>
                        <option value="totalMoneyaftersaleoff,desc">Số tiền cao nhất</option>
                    </select>
                </div>
            </div>
            <div class="vc-toolbar-right">
                <div class="vc-search-box">
                    <i class="fas fa-search"></i>
                    <input type="text" v-model="search" placeholder="Số HD, Số ĐH, Tên KH...">
                </div>
            </div>
        </div>

        <div class="vc-table-container">
            <table class="vc-table">
                <thead>
                    <tr>
                        <th style="width:80px">ID HD</th>
                        <th style="width:100px">ID Đơn</th>
                        <th>Khách hàng</th>
                        <th>Ngày tạo</th>
                        <th class="text-end">Tổng tiền hóa đơn</th>
                        <th class="text-end">Giảm giá</th>
                        <th class="text-end">Tổng thanh toán</th>
                        <th style="width:100px">Thao tác</th>
                    </tr>
                </thead>
                <tbody>
                    <tr v-for="item in list" :key="item.id">
                        <td class="text-center font-bold text-slate-500">#{{ item.id }}</td>
                        <td class="text-center">
                            <span class="vc-order-id">#{{ item.orderbill?.id || '?' }}</span>
                        </td>
                        <td>
                            <div class="font-bold text-slate-700">{{ item.orderbill?.customerName }}</div>
                            <small class="text-slate-400">{{ item.orderbill?.phone }}</small>
                        </td>
                        <td>{{ item.createDate ? Helper.DateFormat(item.createDate) : '—' }}</td>
                        <td class="text-end text-slate-600 font-semibold">{{ Helper.ToMoney(item.totalMoney) }}</td>
                        <td class="text-end text-red-500">- {{ Helper.ToMoney(item.discount) }}</td>
                        <td class="text-end text-blue-600 font-bold">{{ Helper.ToMoney(item.totalMoneyaftersaleoff) }}</td>
                        <td class="text-center">
                            <button class="vc-action-btn vc-view" title="Xem chi tiết" @click="viewDetail(item)">
                                <i class="fas fa-eye"></i>
                            </button>
                        </td>
                    </tr>
                    <tr v-if="list.length === 0 && !loading">
                        <td colspan="8" class="text-center py-5 text-slate-400 italic">Không tìm thấy hóa đơn nào</td>
                    </tr>
                </tbody>
            </table>
        </div>

        <div class="vc-pagination-bar" v-if="totalPages > 0">
            <div class="vc-page-info">
                Hiển thị {{ currentPage * pageSize + 1 }} - {{ Math.min((currentPage + 1) * pageSize, totalItems) }} / {{ totalItems }} hóa đơn
            </div>
            <div class="vc-pagination">
                <button class="vc-page-btn" :disabled="currentPage === 0" @click="goToPage(currentPage - 1)">
                    <i class="fas fa-chevron-left"></i>
                </button>
                <button v-for="pg in pages" :key="pg" class="vc-page-btn" :class="{ 'vc-page-active': pg === currentPage }" @click="goToPage(pg)">
                    {{ pg + 1 }}
                </button>
                <button class="vc-page-btn" :disabled="currentPage >= totalPages - 1" @click="goToPage(currentPage + 1)">
                    <i class="fas fa-chevron-right"></i>
                </button>
            </div>
        </div>

        <Dialog v-model:visible="showDetail" header="Chi tiết hóa đơn" modal :style="{ width: '900px' }" class="vc-dialog">
            <div v-if="selectedBill" class="vc-bill-detail">
                <div class="vc-detail-header">
                    <h3 class="vc-detail-title">Hóa đơn #{{ selectedBill.id }}</h3>
                    <span class="vc-order-badge">Đơn hàng #{{ selectedBill.orderbill?.id || '?' }}</span>
                </div>

                <div class="vc-info-grid">
                    <div class="vc-info-item">
                        <label>Khách hàng</label>
                        <span>{{ selectedBill.orderbill?.customerName || '—' }}</span>
                    </div>
                    <div class="vc-info-item">
                        <label>Số điện thoại</label>
                        <span>{{ selectedBill.orderbill?.phone || '—' }}</span>
                    </div>
                    <div class="vc-info-item">
                        <label>Email</label>
                        <span>{{ selectedBill.orderbill?.email || '—' }}</span>
                    </div>
                    <div class="vc-info-item">
                        <label>Địa chỉ giao hàng</label>
                        <span>{{ selectedBill.orderbill?.addressO || '—' }}</span>
                    </div>
                    <div class="vc-info-item">
                        <label>Ngày tạo hóa đơn</label>
                        <span>{{ selectedBill.createDate ? Helper.DateFormat(selectedBill.createDate) : '—' }}</span>
                    </div>
                    <div class="vc-info-item">
                        <label>Phương thức thanh toán</label>
                        <span>{{ selectedBill.orderbill?.methodofPayment?.name_mop || '—' }}</span>
                    </div>
                    <div class="vc-info-item">
                        <label>Vận chuyển</label>
                        <span>{{ selectedBill.orderbill?.shipMethod?.nameSM || '—' }}</span>
                    </div>
                    <div class="vc-info-item">
                        <label>Người nhận khác</label>
                        <span>{{ selectedBill.orderbill?.receiverName || '—' }}</span>
                    </div>
                    <div class="vc-info-item">
                        <label>SĐT Người nhận</label>
                        <span>{{ selectedBill.orderbill?.receiverPhone || '—' }}</span>
                    </div>
                </div>

                <div class="vc-items-section">
                    <h4 class="vc-items-title">Danh sách sản phẩm</h4>
                    <div v-if="itemsLoading" class="vc-loading">
                        <i class="fas fa-spinner fa-spin"></i> Đang tải...
                    </div>
                    <table v-else class="vc-items-table">
                        <thead>
                            <tr>
                                <th>Sản phẩm & Thuộc tính</th>
                                <th class="text-center">SL</th>
                                <th class="text-end">Đơn giá</th>
                                <th class="text-end">Thành tiền</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr v-for="it in orderItems" :key="it.id">
                                <td>
                                    <div class="vc-prod-name">{{ it.product?.name }}</div>
                                    <div class="vc-prod-attrs">
                                        <span v-if="it.color" class="vattr vat-color">Màu: {{ it.color.nameColor }}</span>
                                        <span v-if="it.material" class="vattr vat-mat">Chất liệu: {{ it.material.nameMaterial }}</span>
                                        <span v-if="it.dimensions" class="vattr vat-dim">Kích cỡ: {{ it.dimensions.nameD }}</span>
                                    </div>
                                </td>
                                <td class="text-center">{{ it.amount }}</td>
                                <td class="text-end">{{ Helper.ToMoney(it.price) }}</td>
                                <td class="text-end font-bold">{{ Helper.ToMoney(it.amount * it.price) }}</td>
                            </tr>
                            <tr v-if="orderItems.length === 0 && !itemsLoading">
                                <td colspan="4" class="text-center text-slate-400 py-4">Không có sản phẩm</td>
                            </tr>
                        </tbody>
                        <tfoot>
                            <tr>
                                <td colspan="3" class="text-end font-semibold">Tổng tiền hàng:</td>
                                <td class="text-end font-bold">{{ Helper.ToMoney(selectedBill.totalMoney) }}</td>
                            </tr>
                            <tr>
                                <td colspan="3" class="text-end font-semibold text-red-500">Giảm giá:</td>
                                <td class="text-end font-bold text-red-500">- {{ Helper.ToMoney(selectedBill.discount || 0) }}</td>
                            </tr>
                            <tr v-if="selectedBill.vatAmount">
                                <td colspan="3" class="text-end font-semibold">VAT ({{ selectedBill.vatRate || 0 }}%):</td>
                                <td class="text-end font-bold">{{ Helper.ToMoney(selectedBill.vatAmount) }}</td>
                            </tr>
                            <tr class="vc-total-row">
                                <td colspan="3" class="text-end font-bold">Tổng thanh toán:</td>
                                <td class="text-end font-bold text-blue-600">{{ Helper.ToMoney(selectedBill.totalMoneyaftersaleoff) }}</td>
                            </tr>
                        </tfoot>
                    </table>
                </div>
            </div>
            <template #footer>
                <Button label="Đóng" icon="pi pi-times" class="p-button-secondary" @click="showDetail = false" />
            </template>
        </Dialog>
    </div>
</template>

<style scoped>
.vc-page { padding: 24px; font-family: 'Inter', sans-serif; }
.vc-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; border-bottom: 2px solid #f1f5f9; padding-bottom: 16px; }
.vc-header-left { display: flex; align-items: center; gap: 12px; }
.vc-header-actions { display: flex; gap: 12px; }
.vc-icon { font-size: 28px; color: #3b82f6; }
.vc-header-left h2 { margin: 0; font-size: 22px; font-weight: 700; color: #1e293b; }

.vc-btn-export { background: #16a34a; color: white; border: none; padding: 10px 20px; border-radius: 8px; font-weight: 600; cursor: pointer; transition: 0.2s; }
.vc-btn-export:hover { background: #15803d; transform: translateY(-1px); }

.vc-toolbar { display: flex; justify-content: space-between; align-items: center; background: white; padding: 16px; border-radius: 12px; box-shadow: 0 1px 3px rgba(0,0,0,0.1); margin-bottom: 20px; }
.vc-toolbar-left, .vc-toolbar-right { display: flex; gap: 20px; align-items: center; }

.vc-select-group { display: flex; align-items: center; gap: 8px; font-size: 14px; color: #64748b; }
.vc-select-group select { padding: 6px 12px; border: 1px solid #e2e8f0; border-radius: 6px; outline: none; cursor: pointer; }

.vc-search-box { position: relative; width: 300px; }
.vc-search-box i { position: absolute; left: 12px; top: 50%; transform: translateY(-50%); color: #94a3b8; }
.vc-search-box input { width: 100%; padding: 8px 12px 8px 36px; border: 1px solid #e2e8f0; border-radius: 8px; outline: none; transition: 0.2s; }
.vc-search-box input:focus { border-color: #3b82f6; box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1); }

.vc-table-container { background: white; border-radius: 12px; box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1); overflow: hidden; }
.vc-table { width: 100%; border-collapse: collapse; }
.vc-table th { background: #f8fafc; padding: 14px; text-align: left; font-size: 13px; font-weight: 600; color: #475569; border-bottom: 1px solid #e2e8f0; }
.vc-table td { padding: 14px; border-bottom: 1px solid #f1f5f9; font-size: 14px; color: #334155; }
.vc-table tr:hover { background: #f8fafc; }

.vc-order-id { background: #e0f2fe; color: #0369a1; padding: 4px 8px; border-radius: 4px; font-size: 12px; font-weight: 700; }

.vc-action-btn { width: 32px; height: 32px; border-radius: 6px; border: none; display: flex; align-items: center; justify-content: center; cursor: pointer; transition: 0.2s; }
.vc-view { background: #e0f2fe; color: #3b82f6; }
.vc-view:hover { background: #bae6fd; }

.vc-pagination-bar { display: flex; justify-content: space-between; align-items: center; margin-top: 20px; }
.vc-page-info { font-size: 14px; color: #64748b; }
.vc-pagination { display: flex; gap: 6px; }
.vc-page-btn { padding: 8px 14px; border: 1px solid #e2e8f0; border-radius: 8px; background: white; cursor: pointer; }
.vc-page-active { background: #3b82f6; color: white; border-color: #3b82f6; }

.vc-bill-detail { font-size: 14px; }
.vc-detail-header { display: flex; align-items: center; gap: 12px; margin-bottom: 20px; padding-bottom: 16px; border-bottom: 2px solid #eff6ff; }
.vc-detail-title { margin: 0; font-size: 18px; font-weight: 700; color: #1e293b; }
.vc-order-badge { background: #e0f2fe; color: #0369a1; padding: 4px 12px; border-radius: 6px; font-size: 13px; font-weight: 700; }

.vc-info-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 12px 24px; background: #f8fafc; padding: 16px; border-radius: 10px; margin-bottom: 20px; }
.vc-info-item { display: flex; flex-direction: column; gap: 4px; }
.vc-info-item label { font-size: 12px; font-weight: 600; color: #64748b; text-transform: uppercase; }
.vc-info-item span { font-weight: 600; color: #1e293b; }

.vc-items-section { margin-top: 8px; }
.vc-items-title { margin: 0 0 12px; font-size: 15px; font-weight: 700; color: #334155; }
.vc-loading { text-align: center; padding: 24px; color: #64748b; }
.vc-items-table { width: 100%; border-collapse: collapse; }
.vc-items-table th { background: #f1f5f9; padding: 10px 12px; font-size: 12px; font-weight: 600; color: #475569; border-bottom: 2px solid #e2e8f0; }
.vc-items-table td { padding: 10px 12px; border-bottom: 1px solid #f1f5f9; vertical-align: middle; }
.vc-items-table tfoot td { padding: 8px 12px; }
.vc-total-row { background: #eff6ff; }
.vc-total-row td { padding: 12px !important; font-size: 16px; }

.vc-prod-name { font-weight: 700; color: #1e293b; margin-bottom: 4px; }
.vc-prod-attrs { display: flex; flex-wrap: wrap; gap: 6px; }
.vattr { font-size: 11px; padding: 2px 8px; border-radius: 4px; font-weight: 600; }
.vat-color { background: #fef3c7; color: #92400e; }
.vat-mat { background: #dcfce7; color: #166534; }
.vat-dim { background: #f1f5f9; color: #475569; }
.text-end { text-align: right; }
.text-center { text-align: center; }
.font-bold { font-weight: 700; }
.font-semibold { font-weight: 600; }
.text-red-500 { color: #ef4444; }
.text-blue-600 { color: #2563eb; }
.text-slate-400 { color: #94a3b8; }
</style>
