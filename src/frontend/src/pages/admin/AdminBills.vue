<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue';
import axios from 'axios';
import Toast from 'primevue/toast';
import { useToast } from 'primevue/usetoast';
import Dialog from 'primevue/dialog';
import Button from 'primevue/button';
import html2pdf from 'html2pdf.js';
import Helper from '../../helper/helper';
import { state } from '../../app/MyApp';

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

const showPrintPreview = ref(false);
const printBillData = ref<any>(null);
const printOrderItems = ref<any[]>([]);
const printingItemsLoading = ref(false);
const isPrinting = ref(false);
const invoiceRef = ref<HTMLElement | null>(null);

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

const openPrintPreview = async (bill: any) => {
    printBillData.value = { ...bill };
    printOrderItems.value = [];
    showPrintPreview.value = true;
    const orderId = bill.orderbill?.id;
    if (!orderId) return;

    printingItemsLoading.value = true;
    try {
        const res = await axios.get(`${ORDER_API}/detail/${orderId}`);
        printOrderItems.value = res.data || [];
    } catch {
        toast.add({ severity: 'error', summary: 'Lỗi', detail: 'Không thể tải chi tiết hóa đơn', life: 3000 });
    } finally {
        printingItemsLoading.value = false;
    }
};

const downloadPDF = async () => {
    if (!invoiceRef.value) return;
    isPrinting.value = true;
    const opt = {
        margin: 10,
        filename: `HoaDon_${printBillData.value?.id || 'HD'}.pdf`,
        image: { type: 'jpeg' as const, quality: 0.98 },
        html2canvas: { scale: 2, useCORS: true },
        jsPDF: { unit: 'mm', format: 'a4', orientation: 'portrait' as const }
    };
    try {
        await html2pdf().from(invoiceRef.value).set(opt).save();
    } finally {
        isPrinting.value = false;
    }
};

const printInvoice = () => {
    let printContents = invoiceRef.value?.innerHTML;
    if(!printContents) return;
    
    let stylesHtml = '';
    for (const node of [...document.querySelectorAll('link[rel="stylesheet"], style')]) {
        stylesHtml += node.outerHTML;
    }
    
    const printWindow = window.open('', '', 'width=900,height=600');
    if(!printWindow) return;
    printWindow.document.write(`
        <html>
        <head>
            <title>In Hóa Đơn</title>
            ${stylesHtml}
            <style>
                body { padding: 20px; font-family: 'Inter', sans-serif; background: #fff; margin: 0; }
                .invoice-wrapper { background: white !important; }
                .invoice-container { box-shadow: none !important; margin: 0 !important; width: 100% !important; padding: 0 !important; }
            </style>
        </head>
        <body>
            <div class="invoice-wrapper">
                ${printContents}
            </div>
        </body>
        </html>
    `);
    printWindow.document.close();
    printWindow.focus();
    setTimeout(() => {
        printWindow.print();
        printWindow.close();
    }, 250);
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
                            <div style="display: flex; gap: 8px; justify-content: center;">
                                <button class="vc-action-btn vc-view" title="Xem chi tiết" @click="viewDetail(item)">
                                    <i class="fas fa-eye"></i>
                                </button>
                                <button class="vc-action-btn vc-print" title="In hóa đơn" @click="openPrintPreview(item)">
                                    <i class="fas fa-print"></i>
                                </button>
                            </div>
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
                                <th class="text-center">Bảo hành</th>
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
                                <td class="text-center">
                                    <span class="vattr vat-dim">{{ it.product?.warranty || '—' }}</span>
                                </td>
                            </tr>
                            <tr v-if="orderItems.length === 0 && !itemsLoading">
                                <td colspan="5" class="text-center text-slate-400 py-4">Không có sản phẩm</td>
                            </tr>
                        </tbody>
                        <tfoot>
                            <tr>
                                <td colspan="4" class="text-end font-semibold">Tổng tiền hàng:</td>
                                <td class="text-end font-bold">{{ Helper.ToMoney(selectedBill.totalMoney) }}</td>
                            </tr>
                            <tr>
                                <td colspan="4" class="text-end font-semibold text-red-500">Giảm giá:</td>
                                <td class="text-end font-bold text-red-500">- {{ Helper.ToMoney(selectedBill.discount || 0) }}</td>
                            </tr>
                            <tr v-if="selectedBill.vatAmount">
                                <td colspan="4" class="text-end font-semibold">VAT ({{ selectedBill.vatRate || 0 }}%):</td>
                                <td class="text-end font-bold">{{ Helper.ToMoney(selectedBill.vatAmount) }}</td>
                            </tr>
                            <tr class="vc-total-row">
                                <td colspan="4" class="text-end font-bold">Tổng thanh toán:</td>
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

        <Dialog v-model:visible="showPrintPreview" modal :style="{ width: '850px', maxWidth: '100vw' }" class="vc-dialog" :closable="false" :showHeader="false">
             <div class="print-dialog-header">
                 <h3 class="m-0 text-lg font-bold" style="color: #1e293b; border-right: 2px solid #e2e8f0; line-height: 1.2; padding-right: 1rem; margin-right: 1rem;">Xem trước hóa đơn bán hàng</h3>
                 <div class="print-actions">
                     <Button label="In hóa đơn" class="btn-print" @click="printInvoice" />
                     <Button label="Tải PDF" class="btn-download" @click="downloadPDF" :loading="isPrinting" />
                     <Button label="Đóng" class="btn-close" @click="showPrintPreview = false" />
                 </div>
             </div>

             <div class="invoice-wrapper">
                 <div v-if="printingItemsLoading" class="vc-loading">
                     <i class="fas fa-spinner fa-spin"></i> Đang tải dữ liệu in...
                 </div>
                 <div class="invoice-container" ref="invoiceRef" v-else>
                    <div class="inv-header">
                        <div class="inv-header-left">
                            <div class="inv-logo">
                                <img :src="state.generalImages?.['Logo'] ? Helper.GetImageUrl(state.generalImages['Logo']) : '/images/logotech.png'" alt="Logo" style="height: 40px; margin-bottom: 8px; object-fit: contain; max-width: 150px;">
                            </div>
                            <div class="inv-slogan">Chuyên thiết bị và phụ kiện thể thao chính hãng</div>
                            <div class="inv-contact-info">
                                <div><i class="fas fa-map-marker-alt" style="width: 16px;"></i> Địa chỉ: 67/7 Trương Định, KV Vĩnh Phú, P An Nhơn Bác, Gia Lai</div>
                                <div><i class="fas fa-phone-alt" style="width: 16px;"></i> Hotline: 19008089</div>
                                <div><i class="fas fa-envelope" style="width: 16px;"></i> Email: support@hcshop.com</div>
                                <div><i class="fas fa-globe" style="width: 16px;"></i> Website: www.hcshop.com</div>
                            </div>
                        </div>
                        <div class="inv-header-right">
                            <h2 class="inv-title">HÓA ĐƠN BÁN HÀNG</h2>
                            <div class="inv-subtitle">SALES INVOICE</div>
                            <table class="inv-meta">
                                <tr>
                                    <td class="text-left whitespace-nowrap">Mã đơn hàng:</td>
                                    <td class="text-green-600 font-bold uppercase text-left pl-2">{{ printBillData?.orderbill?.id || '---' }}</td>
                                </tr>
                                <tr>
                                    <td class="text-left whitespace-nowrap">Ngày đặt:</td>
                                    <td class="text-left pl-2">{{ printBillData?.createDate ? Helper.DateFormat(printBillData?.createDate) : '---' }}</td>
                                </tr>
                                <tr>
                                    <td class="text-left whitespace-nowrap">Trạng thái:</td>
                                    <td class="text-green-600 font-bold text-left pl-2">Đã thanh toán</td>
                                </tr>
                            </table>
                        </div>
                    </div>

                    <div class="inv-divider"></div>

                    <div class="inv-info-section">
                        <div class="inv-info-box">
                            <div class="inv-info-title">THÔNG TIN KHÁCH HÀNG</div>
                            <table class="inv-info-table">
                                <tr><td class="lbl">Khách hàng:</td><td class="val font-bold">{{ printBillData?.orderbill?.customerName }}</td></tr>
                                <tr><td class="lbl">Điện thoại:</td><td class="val">{{ printBillData?.orderbill?.phone }}</td></tr>
                                <tr><td class="lbl">Email:</td><td class="val">{{ printBillData?.orderbill?.email || '---' }}</td></tr>
                                <tr><td class="lbl">Địa chỉ:</td><td class="val">{{ printBillData?.orderbill?.addressO }}</td></tr>
                                <tr><td class="lbl">Tên người nhận:</td><td class="val">{{ printBillData?.orderbill?.receiverName || '---' }}</td></tr>
                                <tr><td class="lbl">SĐT người nhận:</td><td class="val">{{ printBillData?.orderbill?.receiverPhone || '---' }}</td></tr>
                            </table>
                        </div>
                        <div class="inv-info-box">
                            <div class="inv-info-title">THÔNG TIN GIAO NHẬN</div>
                            <table class="inv-info-table">
                                <tr><td class="lbl">Phương thức:</td><td class="val">{{ printBillData?.orderbill?.shipMethod?.nameSM || 'Giao tận nơi' }}</td></tr>
                                <tr><td class="lbl">DV vận chuyển:</td><td class="val">Standard Delivery</td></tr>
                                <tr><td class="lbl">Thanh toán:</td><td class="val">{{ printBillData?.orderbill?.methodofPayment?.name_mop || 'VNPAY' }}</td></tr>
                                <tr><td class="lbl">Ghi chú:</td><td class="val italic">Không có ghi chú.</td></tr>
                            </table>
                        </div>
                    </div>

                    <table class="inv-items-table">
                        <thead>
                            <tr>
                                <th style="width: 50px; text-align: center;">STT</th>
                                <th>TÊN SẢN PHẨM / THUỘC TÍNH</th>
                                <th style="width: 50px; text-align: center;">SL</th>
                                <th style="text-align: right;">ĐƠN GIÁ</th>
                                <th style="text-align: right;">THÀNH TIỀN</th>
                                <th style="text-align: center;">BẢO HÀNH</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr v-for="(it, idx) in printOrderItems" :key="idx">
                                <td style="text-align: center;">{{ idx + 1 }}</td>
                                <td>
                                    <div class="inv-prod-name">{{ it.product?.name }}</div>
                                    <div class="inv-prod-attrs">
                                        <span v-if="it.color" class="inv-attr">Màu: {{ it.color.nameColor }}</span>
                                        <span v-if="it.dimensions" class="inv-attr">Kích cỡ: {{ it.dimensions.nameD }}</span>
                                        <span v-if="it.material" class="inv-attr">Chất liệu: {{ it.material.nameMaterial }}</span>
                                    </div>
                                </td>
                                <td style="text-align: center;">{{ it.amount }}</td>
                                <td style="text-align: right;">{{ Helper.ToMoney(it.price) }}</td>
                                <td style="text-align: right;">{{ Helper.ToMoney(it.amount * it.price) }}</td>
                                <td style="text-align: center; font-size: 11px;">{{ it.product?.warranty || 'Không bảo hành' }}</td>
                            </tr>
                            <tr v-if="printOrderItems.length === 0">
                                <td colspan="6" class="text-center text-slate-400 py-4">Không có sản phẩm</td>
                            </tr>
                        </tbody>
                    </table>

                    <div class="inv-summary-section">
                        <div class="inv-policy">
                            <div class="inv-policy-title">Cam kết & Chính sách đổi trả:</div>
                            <ul>
                                <li>Được kiểm tra hàng trước khi nhận và thanh toán.</li>
                                <li>Hỗ trợ đổi trả sản phẩm trong vòng 7 ngày nếu lỗi sản xuất.</li>
                                <li>Giữ lại hóa đơn để được bảo hành sản phẩm chính hãng.</li>
                            </ul>
                        </div>
                        <div class="inv-totals">
                            <table class="inv-totals-table">
                                <tr>
                                    <td>Tạm tính:</td>
                                    <td class="font-bold">{{ Helper.ToMoney(printBillData?.totalMoney) }}</td>
                                </tr>
                                <tr>
                                    <td>Giảm giá voucher:</td>
                                    <td class="font-bold">-{{ Helper.ToMoney(printBillData?.discount || 0) }}</td>
                                </tr>
                                <tr class="inv-total-row">
                                    <td class="uppercase font-bold">TỔNG THANH TOÁN:</td>
                                    <td class="text-green-600 font-bold text-xl">{{ Helper.ToMoney(printBillData?.totalMoneyaftersaleoff) }}</td>
                                </tr>
                            </table>
                        </div>
                    </div>

                    <div class="inv-signatures">
                        <div class="inv-sig-box">
                            <div class="font-bold" style="color: #1e293b;">Người mua hàng</div>
                            <div class="italic text-slate-500" style="font-size: 11px; margin-bottom: 60px;">(Ký, ghi rõ họ tên)</div>
                            <div class="inv-sig-name">{{ printBillData?.orderbill?.customerName }}</div>
                        </div>
                        <div class="inv-sig-box">
                            <div class="font-bold" style="color: #1e293b;">Người lập hóa đơn</div>
                            <div class="italic text-slate-500" style="font-size: 11px; margin-bottom: 60px;">(Ký, đóng dấu đại diện)</div>
                            <div class="inv-sig-name">Bộ phận bán hàng</div>
                        </div>
                    </div>

                    <div class="inv-footer">
                        <div class="inv-thanks">CẢM ƠN QUÝ KHÁCH ĐÃ MUA SẮM TẠI HCSHOP!</div>
                        <div class="inv-seeyou">Hẹn gặp lại quý khách lần mua sắm tiếp theo.</div>
                    </div>
                 </div>
             </div>
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

/* In hóa đơn CSS */
.vc-print { background: #f97316; color: white; }
.vc-print:hover { background: #ea580c; }

.print-dialog-header { display: flex; justify-content: space-between; align-items: center; padding: 12px 16px; background: white; border-bottom: 1px solid #e2e8f0; }
.print-actions { display: flex; gap: 8px; }
.btn-print { background: #10b981 !important; color: white !important; border: none !important; width: 140px !important; height: 40px !important; border-radius: 6px !important; font-weight: 600 !important; cursor: pointer; transition: 0.2s; display: inline-flex; align-items: center; justify-content: center; padding: 0 !important; }
.btn-print:hover { background: #059669 !important; transform: translateY(-1px); }
.btn-download { background: #3b82f6 !important; color: white !important; border: none !important; width: 140px !important; height: 40px !important; border-radius: 6px !important; font-weight: 600 !important; cursor: pointer; transition: 0.2s; display: inline-flex; align-items: center; justify-content: center; padding: 0 !important; }
.btn-download:hover { background: #2563eb !important; transform: translateY(-1px); }
.btn-close { background: #94a3b8 !important; color: white !important; border: none !important; width: 140px !important; height: 40px !important; border-radius: 6px !important; font-weight: 600 !important; cursor: pointer; transition: 0.2s; display: inline-flex; align-items: center; justify-content: center; padding: 0 !important; }
.btn-close:hover { background: #64748b !important; transform: translateY(-1px); }

.invoice-wrapper { padding: 20px; background: #f8fafc; display: flex; justify-content: center; }
.invoice-container { background: white; padding: 30px 40px; border-radius: 8px; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.1); width: 100%; max-width: 780px; font-family: 'Inter', sans-serif; color: #334155; }

.inv-header { display: flex; justify-content: space-between; margin-bottom: 30px; }
.inv-logo { display:flex; align-items:center; }
.inv-slogan { font-style: italic; font-size: 13px; color: #64748b; margin-bottom: 10px; }
.inv-contact-info { font-size: 12px; line-height: 1.6; color: #475569; }
.inv-title { margin: 0 0 4px; font-size: 22px; font-weight: 800; color: #1e293b; text-align: right; letter-spacing: 0.5px; white-space: nowrap; }
.inv-subtitle { font-size: 13px; text-align: right; color: #94a3b8; letter-spacing: 1.5px; margin-bottom: 15px; }
.inv-meta { margin-left: auto; font-size: 12px; border-collapse: separate; border-spacing: 0 4px; color: #475569; }
.inv-meta td:first-child { width: 105px; text-align: left; }
.inv-meta td:last-child { text-align: left; }
.pl-2 { padding-left: 8px; }
.text-green-600 { color: #16a34a !important; }
.uppercase { text-transform: uppercase; }

.inv-divider { height: 1px; background: #e2e8f0; margin: 15px 0; }

.inv-info-section { display: flex; gap: 24px; margin-bottom: 24px; }
.inv-info-box { flex: 1; }
.inv-info-title { background: #f1f5f9; padding: 8px 12px; font-weight: 700; font-size: 13px; color: #334155; margin-bottom: 12px; border-left: 4px solid #cbd5e1; }
.inv-info-table { width: 100%; font-size: 13px; border-collapse: separate; border-spacing: 0 6px; }
.inv-info-table .lbl { width: 120px; color: #64748b; vertical-align: top; }
.inv-info-table .val { color: #1e293b; vertical-align: top; }

.inv-items-table { width: 100%; border-collapse: collapse; margin-bottom: 24px; border: 1px solid #e2e8f0; }
.inv-items-table th { border: 1px solid #e2e8f0; padding: 8px; font-size: 11px; font-weight: 700; background: #f8fafc; color: #1e293b; }
.inv-items-table td { border: 1px solid #e2e8f0; padding: 8px 10px; font-size: 12px; vertical-align: middle; }
.inv-prod-name { font-weight: 600; color: #1e293b; margin-bottom: 2px; }
.inv-prod-attrs { display: flex; gap: 6px; font-size: 10px; color: #64748b; }
.inv-attr { background: #f1f5f9; padding: 1px 4px; border-radius: 4px; border: 1px solid #e2e8f0; }

.inv-summary-section { display: flex; justify-content: space-between; margin-bottom: 25px; gap: 20px; }
.inv-policy { flex: 1; background: #f8fafc; padding: 16px; border-radius: 8px; border: 1px dashed #cbd5e1; }
.inv-policy-title { font-weight: 700; color: #16a34a; font-size: 13px; margin-bottom: 8px; }
.inv-policy ul { margin: 0; padding-left: 20px; font-size: 12px; color: #475569; line-height: 1.6; }

.inv-totals { width: 300px; }
.inv-totals-table { width: 100%; font-size: 14px; border-collapse: collapse; }
.inv-totals-table td { padding: 6px 0; text-align: right; }
.inv-totals-table td:first-child { text-align: left; color: #64748b; }
.inv-total-row td { padding-top: 8px; border-top: 1px dotted #cbd5e1; }
.text-xl { font-size: 18px; }

.inv-signatures { display: flex; justify-content: space-around; margin-bottom: 25px; text-align: center; }
.inv-sig-box { display: flex; flex-direction: column; align-items: center; }
.inv-sig-name { font-weight: 700; color: #1e293b; border-bottom: 1px solid #e2e8f0; padding-bottom: 4px; min-width: 140px; }

.inv-footer { text-align: center; color: #16a34a; }
.inv-thanks { font-weight: 800; font-size: 15px; margin-bottom: 4px; }
.inv-seeyou { font-size: 12px; color: #64748b; font-style: italic; }
</style>
