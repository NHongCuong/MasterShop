<script setup lang="ts">
import { state, MyApp } from '../../app/MyApp';
import { useRouter } from 'vue-router';
import { onMounted, ref, watch, computed } from 'vue';
import axios from 'axios';
import Dialog from 'primevue/dialog';
import Button from 'primevue/button';
import html2pdf from 'html2pdf.js';
import Helper from '../../helper/helper';

const router = useRouter();
const activeTab = ref('account');

// Profile Editing
const isEditing = ref(false);
const editForm = ref({
  nameUser: '',
  phone: '',
  email: '',
  avatar: '',
  address: ''
});

const startEdit = () => {
  editForm.value = {
    nameUser: state.user?.nameUser || '',
    phone: state.user?.phone || '',
    email: state.user?.email || '',
    avatar: state.user?.avatar || '',
    address: state.user?.address || ''
  };
  isEditing.value = true;
};

const cancelEdit = () => {
  isEditing.value = false;
};

const handleAvatarUpload = async (event: any) => {
  const file = event.target.files[0];
  if (!file) return;
  
  const formData = new FormData();
  formData.append('file', file);
  formData.append('folder', 'avatars');
  
  try {
    const res = await axios.post('http://localhost:8081/storage/upload', formData);
    editForm.value.avatar = res.data.url;
  } catch (e) {
    alert('Lỗi upload ảnh: ' + e);
  }
};

const saveProfile = async () => {
  if (!state.user?.id) return;
  try {
    const payload = {
        ...state.user,
        nameUser: editForm.value.nameUser,
        phone: editForm.value.phone,
        avatar: editForm.value.avatar,
        address: editForm.value.address
    };
    
    const res = await axios.put(`http://localhost:8081/update/${state.user.id}`, payload);
    
    if (res.status === 200) {
      alert('Cập nhật hồ sơ thành công!');
      // Đồng bộ lại state toàn cục
      state.user.nameUser = editForm.value.nameUser;
      state.user.phone = editForm.value.phone;
      state.user.avatar = editForm.value.avatar;
      state.user.address = editForm.value.address;
      isEditing.value = false;
    }
  } catch (e) {
    alert('Lỗi khi cập nhật thông tin');
    console.error(e);
  }
};

// Orders data
const orders = ref<any[]>([]);
const totalItems = ref(0);
const totalPages = ref(0);
const currentPage = ref(0);
const pageSize = ref(10);
const sortOption = ref('createDate,desc');
const loadingOrders = ref(false);

const loadOrders = async () => {
  if (!state.isAuthenticated || !state.user?.email) return;
  loadingOrders.value = true;
  try {
    const res = await axios.get('http://localhost:8081/bill/my-bills', {
      params: {
        email: state.user.email,
        page: currentPage.value,
        size: pageSize.value,
        sort: sortOption.value
      }
    });

    const billList = res.data.content || [];
    orders.value = billList.map((b: any) => ({ ...b, primaryProductName: 'Đang tải...', totalQuantity: 0 }));
    totalItems.value = res.data.totalElements || 0;
    totalPages.value = res.data.totalPages || 0;
    
    billList.forEach(async (bill: any, index: number) => {
        if (bill.orderbill) {
            try {
                const detailRes = await axios.get(`http://localhost:8081/order/admin/detail/${bill.orderbill.id}`);
                const details = detailRes.data;
                let primaryName = 'Đơn hàng #' + bill.id;
                let totalQty = 0;
                if (details && details.length > 0) {
                    primaryName = details[0].product.name + (details.length > 1 ? ` (+ ${details.length - 1} sp)` : '');
                    totalQty = details.reduce((acc: number, item: any) => acc + (Number(item.amount) || 0), 0);
                }
                orders.value[index].primaryProductName = primaryName;
                orders.value[index].totalQuantity = totalQty;
            } catch (e) {
                orders.value[index].primaryProductName = 'Đơn hàng #' + bill.id;
            }
        }
    });
  } catch (err) {
    console.error('Lỗi khi tải đơn hàng:', err);
  } finally {
    loadingOrders.value = false;
  }
};

onMounted(async () => {
  // Chờ xác thực xong nếu có token (tránh bay màu khi F5)
  if (!state.isAuthenticated && localStorage.getItem('token')) {
    await MyApp.getInstance().authenticate();
  }

  if (!state.isAuthenticated) {
    router.push('/'); // Quay về trang chủ thay vì trang đăng nhập theo yêu cầu
  } else {
    loadOrders();
  }
});

watch(activeTab, () => {
    isEditing.value = false;
    passwordForm.value = { newPassword: '', confirmPassword: '' };
    if (activeTab.value === 'orders') {
        loadOrders();
    }
});

watch([currentPage, pageSize, sortOption], () => {
    if (activeTab.value === 'orders') {
        loadOrders();
    }
});

// Change Password Logic
const passwordForm = ref({
    newPassword: '',
    confirmPassword: ''
});
const changingPassword = ref(false);

const changePassword = async () => {
    if (!passwordForm.value.newPassword) {
        alert('Vui lòng nhập mật khẩu mới');
        return;
    }
    if (passwordForm.value.newPassword.length < 6) {
        alert('Mật khẩu phải có ít nhất 6 ký tự');
        return;
    }
    if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
        alert('Mật khẩu xác nhận không khớp');
        return;
    }
    
    changingPassword.value = true;
    try {
        const res = await axios.put(`http://localhost:8081/change-password/${state.user?.id}`, {
            newPassword: passwordForm.value.newPassword
        });
        
        if (res.status === 200) {
            alert('Đổi mật khẩu thành công!');
            passwordForm.value = { newPassword: '', confirmPassword: '' };
        }
    } catch (e: any) {
        alert('Lỗi: ' + (e.response?.data || 'Không thể đổi mật khẩu'));
    } finally {
        changingPassword.value = false;
    }
};

const logout = () => {
    MyApp.getInstance().clearState();
    router.push('/');
};

// Print Invoice Logic
const showPrintPreview = ref(false);
const printBillData = ref<any>(null);
const printOrderItems = ref<any[]>([]);
const printingItemsLoading = ref(false);
const isPrinting = ref(false);
const invoiceRef = ref<HTMLElement | null>(null);

const printBill = async (bill: any) => {
    printBillData.value = { ...bill };
    printOrderItems.value = [];
    showPrintPreview.value = true;
    const orderId = bill.orderbill?.id;
    if (!orderId) return;

    printingItemsLoading.value = true;
    try {
        const res = await axios.get(`http://localhost:8081/order/admin/detail/${orderId}`);
        printOrderItems.value = res.data || [];
    } catch {
        alert('Không thể tải chi tiết đơn hàng để in');
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
    const list = [];
    for (let i = 0; i < totalPages.value; i++) list.push(i);
    return list;
});
</script>

<template>
  <div class="profile-page">
    <!-- Top Banner -->
    <div class="breadcrumb-banner py-3 px-0 mb-4 shadow-sm">
      <div class="container d-flex align-items-center">
        <span class="fw-bold text-white fs-6">TRANG CHỦ > TRANG CÁ NHÂN</span>
      </div>
    </div>

    <div class="container mb-5">
      <div class="row g-0 bg-white shadow-sm rounded-4 border overflow-hidden" style="min-height: 600px;">
        <!-- Sidebar -->
        <div class="col-md-3 border-end bg-light-subtle">
          <div class="sidebar-menu py-4">
            <div class="menu-item" :class="{ active: activeTab === 'account' }" @click="activeTab = 'account'">
              <i class="fas fa-user"></i>
              <span>Thông tin tài khoản</span>
            </div>
            <div class="menu-item" :class="{ active: activeTab === 'orders' }" @click="activeTab = 'orders'">
              <i class="fas fa-shopping-bag"></i>
              <span>Đơn hàng đã đặt</span>
            </div>
            <div class="menu-item" :class="{ active: activeTab === 'change-password' }" @click="activeTab = 'change-password'">
              <i class="fas fa-lock"></i>
              <span>Đổi mật khẩu</span>
            </div>
            <div class="menu-item mt-2" @click="logout">
              <i class="fas fa-sign-out-alt"></i>
              <span>Đăng xuất</span>
            </div>
          </div>
        </div>

        <!-- Main Content -->
        <div class="col-md-9 p-4 p-md-5">
          <!-- Account Management -->
          <div v-if="activeTab === 'account'" class="tab-content animate__animated animate__fadeIn">
            <h4 class="fw-bold mb-5" style="color: #333; font-size: 1.5rem;">Thông tin tài khoản</h4>
            
            <div class="row align-items-start gy-4">
              <div class="col-md-7">
                <div v-if="!isEditing" class="info-grid mt-2">
                  <div class="info-row mb-4">
                    <label>Họ và tên:</label>
                    <span class="fw-medium">{{ state.user?.nameUser }}</span>
                  </div>
                  <div class="info-row mb-4">
                    <label>Email:</label>
                    <span class="text-secondary">{{ state.user?.email }}</span>
                  </div>
                  <div class="info-row mb-4">
                    <label>Số điện thoại:</label>
                    <span class="text-dark">{{ state.user?.phone || 'Chưa cập nhật' }}</span>
                  </div>
                  <div class="info-row mb-4">
                    <label>Địa chỉ:</label>
                    <span class="text-dark">{{ state.user?.address || 'Chưa cập nhật' }}</span>
                  </div>

                  <div class="mt-5 pt-2">
                    <button @click="startEdit" class="btn btn-yellow px-4 py-2 fw-bold d-flex align-items-center gap-2">
                        <i class="fas fa-edit small text-white"></i> Chỉnh sửa
                    </button>
                  </div>
                </div>

                <!-- Edit Form -->
                <div v-else class="edit-form-container mt-2">
                    <div class="mb-3">
                        <label class="form-label fw-bold small text-muted">HỌ VÀ TÊN</label>
                        <input v-model="editForm.nameUser" type="text" class="form-control border-secondary-subtle py-2 shadow-none">
                    </div>
                    <div class="mb-3">
                        <label class="form-label fw-bold small text-muted">EMAIL (Không thể thay đổi)</label>
                        <input v-model="editForm.email" type="email" class="form-control border-secondary-subtle py-2 bg-light shadow-none" readonly disabled>
                    </div>
                    <div class="mb-3">
                        <label class="form-label fw-bold small text-muted">SỐ ĐIỆN THOẠI</label>
                        <input v-model="editForm.phone" type="text" class="form-control border-secondary-subtle py-2 shadow-none">
                    </div>
                    <div class="mb-4">
                        <label class="form-label fw-bold small text-muted">ĐỊA CHỈ</label>
                        <textarea v-model="editForm.address" class="form-control border-secondary-subtle py-2 shadow-none" rows="2"></textarea>
                    </div>

                    <div class="d-flex gap-3 mt-4">
                        <button @click="saveProfile" class="btn btn-success px-4 py-2 fw-bold d-flex align-items-center gap-2">
                           <i class="fas fa-save"></i> Lưu
                        </button>
                        <button @click="cancelEdit" class="btn btn-outline-secondary px-4 py-2 fw-bold">
                           Hủy
                        </button>
                    </div>
                </div>
              </div>
              
              <div class="col-md-5 text-center mt-0">
                <div class="avatar-section">
                    <div class="avatar-wrapper mx-auto mb-3">
                        <div v-if="!isEditing" class="profile-avatar-placeholder mx-auto shadow-sm">
                            <img v-if="state.user?.avatar" :src="Helper.GetImageUrl(state.user.avatar)" class="img-fluid rounded-circle" style="width: 100%; height: 100%; object-fit: cover;">
                            <i v-else class="fas fa-user text-muted" style="font-size: 5rem;"></i>
                        </div>
                        <div v-else class="profile-avatar-placeholder mx-auto shadow-sm position-relative">
                            <img v-if="editForm.avatar" :src="Helper.GetImageUrl(editForm.avatar)" class="img-fluid rounded-circle" style="width: 100%; height: 100%; object-fit: cover; opacity: 0.7;">
                            <i v-else class="fas fa-user text-muted" style="font-size: 5rem; opacity: 0.5;"></i>
                            <div class="position-absolute top-50 start-50 translate-middle">
                                <i class="fas fa-camera fs-3 text-dark"></i>
                            </div>
                        </div>
                    </div>
                    
                    <div v-if="isEditing" class="mt-2">
                        <input type="file" @change="handleAvatarUpload" class="form-control form-control-sm border-secondary-subtle bg-white shadow-none" accept="image/*">
                    </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Orders Management -->
          <div v-if="activeTab === 'orders'" class="tab-content animate__animated animate__fadeIn">
            <h4 class="fw-bold mb-4" style="color: #333; font-size: 1.5rem;">Đơn hàng đã đặt</h4>

            <div class="d-flex justify-content-between align-items-center mb-4 gap-3 flex-wrap">
                <div class="d-flex align-items-center gap-2">
                    <span class="small text-muted fw-semibold">Hiển thị:</span>
                    <select v-model="pageSize" class="form-select form-select-sm border-secondary-subtle" style="width: 75px;">
                        <option :value="10">10</option>
                        <option :value="20">20</option>
                        <option :value="50">50</option>
                    </select>
                </div>
                <div class="d-flex align-items-center gap-2">
                    <span class="small text-muted fw-semibold">Sắp xếp theo:</span>
                    <select v-model="sortOption" class="form-select form-select-sm border-secondary-subtle" style="width: 180px;">
                        <option value="createDate,desc">Ngày đặt (Mới nhất)</option>
                        <option value="createDate,asc">Ngày đặt (Cũ nhất)</option>
                    </select>
                </div>
            </div>

            <div class="table-responsive">
              <table class="table align-middle mb-0">
                <thead>
                  <tr class="border-header">
                    <th class="py-3 text-center border-0">STT</th>
                    <th class="py-3 text-start border-0">Tên sản phẩm</th>
                    <th class="py-3 text-center border-0">Số lượng</th>
                    <th class="py-3 text-center border-0">Trạng thái</th>
                    <th class="py-3 text-center border-0">Ngày đặt</th>
                    <th class="py-3 text-center border-0">Tổng tiền</th>
                    <th class="py-3 text-center border-0">Hành động</th>
                  </tr>
                </thead>
                <tbody v-if="!loadingOrders">
                  <tr v-for="(bill, index) in orders" :key="bill.id" class="order-row">
                    <td class="text-center text-muted">{{ currentPage * pageSize + index + 1 }}</td>
                    <td class="text-start">
                      <div class="text-dark">
                        {{ bill.primaryProductName }}
                      </div>
                    </td>
                    <td class="text-center text-dark">{{ bill.totalQuantity || 0 }}</td>
                    <td class="text-center">
                      <span class="status-text">
                          {{ bill.bill?.nameBS || 'Đang xử lý' }}
                      </span>
                    </td>
                    <td class="text-center text-dark">
                        {{ Helper.DateFormat(bill.createDate) }}
                    </td>
                    <td class="text-center fw-bold text-dark">
                        {{ Helper.ToMoney(bill.totalMoneyaftersaleoff) }}
                    </td>
                    <td class="text-center">
                        <button class="btn btn-print-green btn-sm px-3" @click="printBill(bill)">
                             In Hóa Đơn
                        </button>
                    </td>
                  </tr>
                  <tr v-if="orders.length === 0">
                    <td colspan="7" class="text-center py-5 text-muted fst-italic">
                        <i class="fas fa-shopping-cart d-block mb-2 fs-2 opacity-25"></i>
                        Bạn chưa có đơn hàng nào
                    </td>
                  </tr>
                </tbody>
                <tbody v-else>
                    <tr><td colspan="7" class="text-center py-5"><div class="spinner-border spinner-border-sm text-warning"></div></td></tr>
                </tbody>
              </table>
            </div>

            <div v-if="totalPages > 1" class="mt-4 d-flex justify-content-end">
                <nav>
                    <ul class="pagination pagination-sm m-0 gap-1 border-0">
                        <li class="page-item" :class="{ disabled: currentPage === 0 }">
                            <button class="page-link border-0 rounded-circle" @click="currentPage--" style="width: 32px; height: 32px; display: flex; align-items: center; justify-content: center;">
                                <i class="fas fa-chevron-left small"></i>
                            </button>
                        </li>
                        <li v-for="p in pages" :key="p" class="page-item" :class="{ active: p === currentPage }">
                            <button class="page-link border-0 rounded-circle" @click="currentPage = p" style="width: 32px; height: 32px; display: flex; align-items: center; justify-content: center;">{{ p + 1 }}</button>
                        </li>
                        <li class="page-item" :class="{ disabled: currentPage === totalPages - 1 }">
                            <button class="page-link border-0 rounded-circle" @click="currentPage++" style="width: 32px; height: 32px; display: flex; align-items: center; justify-content: center;">
                                <i class="fas fa-chevron-right small"></i>
                            </button>
                        </li>
                    </ul>
                </nav>
            </div>
          </div>

          <!-- Change Password Management -->
          <div v-if="activeTab === 'change-password'" class="tab-content animate__animated animate__fadeIn">
              <h4 class="fw-bold mb-4" style="color: #333; font-size: 1.5rem;">Đặt Lại Mật Khẩu</h4>
              
              <div class="change-password-form mt-4" style="max-width: 100%;">
                  <div class="mb-4">
                      <input v-model="passwordForm.newPassword" type="password" class="form-control custom-password-input" placeholder="Mật khẩu mới">
                  </div>
                  <div class="mb-5">
                      <input v-model="passwordForm.confirmPassword" type="password" class="form-control custom-password-input" placeholder="Xác nhận mật khẩu">
                  </div>

                  <button @click="changePassword" class="btn btn-yellow w-100 py-3 fw-bold fs-5" :disabled="changingPassword">
                      {{ changingPassword ? 'Đang xử lý...' : 'Xác nhận' }}
                  </button>
              </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Print Preview Dialog -->
    <Dialog v-model:visible="showPrintPreview" modal :style="{ width: '850px', maxWidth: '100vw' }" class="vc-dialog" :closable="false" :showHeader="false">
             <div class="print-dialog-header">
                 <h3 class="m-0 text-lg font-bold" style="color: #1e293b; border-right: 2px solid #e2e8f0; line-height: 1.2; padding-right: 1rem; margin-right: 1rem;">Xem trước hóa đơn bán hàng</h3>
                 <div class="print-actions">
                     <Button label="In hóa đơn" class="btn-print" @click="printInvoice" />
                     <Button label="Tải PDF" class="btn-download" @click="downloadPDF" :loading="isPrinting" />
                     <Button label="Đóng" class="btn-close" @click="showPrintPreview = false" />
                 </div>
             </div>

             <div class="invoice-wrapper-print">
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
                                    <td class="text-green-600 font-bold uppercase text-left pl-2">#{{ printBillData?.orderbill?.id || '---' }}</td>
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
                                <td style="text-align: center; font-size: 11px;">Không bảo hành</td>
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
                                    <td class="text-success font-bold text-xl">{{ Helper.ToMoney(printBillData?.totalMoneyaftersaleoff) }}</td>
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

                    <div class="inv-footer-print">
                        <div class="inv-thanks">CẢM ƠN QUÝ KHÁCH ĐÃ MUA SẮM TẠI HCSHOP!</div>
                        <div class="inv-seeyou">Hẹn gặp lại quý khách lần mua sắm tiếp theo.</div>
                    </div>
                 </div>
             </div>
        </Dialog>
  </div>
</template>

<style scoped>
.profile-page {
  background-color: #fcfcfc;
  min-height: 100vh;
  font-family: 'Inter', system-ui, -apple-system, sans-serif;
}

.breadcrumb-banner {
  background-color: #f1c40f; 
}

.sidebar-menu .menu-item {
  padding: 1.15rem 2rem;
  display: flex;
  align-items: center;
  gap: 14px;
  cursor: pointer;
  transition: all 0.25s ease;
  color: #64748b;
  font-weight: 500;
  border-left: 4px solid transparent;
}

.sidebar-menu .menu-item i {
  font-size: 1.1rem;
  width: 24px;
  text-align: center;
}

.sidebar-menu .menu-item:hover:not(.active) {
  background-color: #f1f5f9;
  color: #334155;
}

.sidebar-menu .menu-item.active {
  background-color: #fefce8;
  color: #1e293b;
  border-left-color: #f1c40f;
  font-weight: 700;
}

.sidebar-menu .menu-item.text-danger:hover {
    background-color: #fef2f2;
}

.info-row {
    display: flex;
    align-items: center;
    font-size: 1.05rem;
}

.info-row label {
    font-weight: 700;
    width: 130px;
    color: #475569;
    font-size: 0.95rem;
}

.info-row span {
    color: #1e293b;
}

.btn-yellow {
    background-color: #f1c40f;
    border: none;
    color: white;
    border-radius: 8px;
    box-shadow: 0 4px 12px rgba(241, 196, 15, 0.2);
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.btn-yellow:hover {
    background-color: #d4ac0d;
    transform: translateY(-2px);
    box-shadow: 0 6px 15px rgba(241, 196, 15, 0.3);
}

.btn-print-green {
    background-color: #1abc9c;
    color: white;
    border: none;
    border-radius: 4px;
    font-weight: 500;
    font-size: 0.85rem;
    padding-top: 4px;
    padding-bottom: 4px;
    transition: all 0.2s ease;
}

.btn-print-green:hover {
    background-color: #16a085;
    color: white;
}

.profile-avatar-placeholder {
    width: 180px;
    height: 180px;
    background-color: #e2e8f0;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    border: 6px solid #f8fafc;
    overflow: hidden;
}

.border-header {
    border-bottom: 2px solid #f1c40f !important;
}

.custom-password-input {
    border: 2px solid #f1c40f !important;
    border-radius: 12px !important;
    padding: 15px 20px !important;
    font-size: 1rem !important;
    color: #444 !important;
    transition: all 0.3s ease;
}

.custom-password-input:focus {
    box-shadow: 0 0 0 4px rgba(241, 196, 15, 0.1) !important;
    border-color: #d4ac0d !important;
}

.custom-password-input::placeholder {
    color: #999;
    font-weight: 400;
}

.status-text {
    font-weight: 500;
    color: #334155;
}

.order-row td {
    border-bottom: 1px solid #f1f5f9;
    padding-top: 15px !important;
    padding-bottom: 15px !important;
}

.table thead th {
    font-size: 0.9rem;
    font-weight: 700;
    color: #000;
    letter-spacing: 0.2px;
    background: transparent;
}

.table tbody td {
    padding: 1.15rem 0.5rem;
}

.pagination .page-link {
    background-color: #f1f5f9;
    color: #64748b;
    font-weight: 600;
    border: none;
    font-size: 0.8rem;
}

.pagination .page-item.active .page-link {
    background-color: #f1c40f;
    color: white;
}

.tab-content {
    animation-duration: 0.4s;
}

.border-dashed {
    border-style: dashed !important;
}

/* Print Invoice Styles */
.print-dialog-header { display: flex; justify-content: space-between; align-items: center; padding: 12px 16px; background: white; border-bottom: 1px solid #e2e8f0; }
.print-actions { display: flex; gap: 8px; }
.btn-print { background: #10b981 !important; color: white !important; border: none !important; width: 140px !important; height: 36px !important; border-radius: 6px !important; font-weight: 600 !important; cursor: pointer; transition: 0.2s; display: inline-flex; align-items: center; justify-content: center; padding: 0 !important; }
.btn-print:hover { background: #059669 !important; transform: translateY(-1px); }
.btn-download { background: #3b82f6 !important; color: white !important; border: none !important; width: 140px !important; height: 36px !important; border-radius: 6px !important; font-weight: 600 !important; cursor: pointer; transition: 0.2s; display: inline-flex; align-items: center; justify-content: center; padding: 0 !important; }
.btn-download:hover { background: #2563eb !important; transform: translateY(-1px); }
.btn-close { background: #94a3b8 !important; color: white !important; border: none !important; width: 140px !important; height: 36px !important; border-radius: 6px !important; font-weight: 600 !important; cursor: pointer; transition: 0.2s; display: inline-flex; align-items: center; justify-content: center; padding: 0 !important; }
.btn-close:hover { background: #64748b !important; transform: translateY(-1px); }

.invoice-wrapper-print { padding: 20px; background: #f8fafc; display: flex; justify-content: center; }
.invoice-container { background: white; padding: 30px 40px; border-radius: 8px; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.1); width: 100%; max-width: 780px; font-family: 'Inter', sans-serif; color: #334155; }

.inv-header { display: flex; justify-content: space-between; margin-bottom: 30px; }
.inv-logo { display:flex; align-items:center; }
.inv-slogan { font-style: italic; font-size: 13px; color: #64748b; margin-bottom: 10px; text-align: left; }
.inv-contact-info { font-size: 12px; line-height: 1.6; color: #475569; text-align: left; }
.inv-title { margin: 0 0 4px; font-size: 22px; font-weight: 800; color: #1e293b; text-align: right; letter-spacing: 0.5px; white-space: nowrap; }
.inv-subtitle { font-size: 13px; text-align: right; color: #94a3b8; letter-spacing: 1.5px; margin-bottom: 15px; }
.inv-meta { margin-left: auto; font-size: 12px; border-collapse: separate; border-spacing: 0 4px; color: #475569; }
.inv-meta td { padding: 2px 0; }
.inv-meta td:first-child { width: 105px; text-align: left; }
.inv-meta td:last-child { text-align: left; }
.pl-2 { padding-left: 8px; }

.inv-divider { height: 1px; background: #e2e8f0; margin: 15px 0; }

.inv-info-section { display: flex; gap: 24px; margin-bottom: 24px; text-align: left; }
.inv-info-box { flex: 1; }
.inv-info-title { background: #f1f5f9; padding: 8px 12px; font-weight: 700; font-size: 13px; color: #334155; margin-bottom: 12px; border-left: 4px solid #f1c40f; }
.inv-info-table { width: 100%; font-size: 13px; border-collapse: separate; border-spacing: 0 6px; }
.inv-info-table .lbl { width: 120px; color: #64748b; vertical-align: top; }
.inv-info-table .val { color: #1e293b; vertical-align: top; }

.inv-items-table { width: 100%; border-collapse: collapse; margin-bottom: 24px; border: 1px solid #e2e8f0; }
.inv-items-table th { border: 1px solid #e2e8f0; padding: 8px; font-size: 11px; font-weight: 700; background: #f8fafc; color: #1e293b; }
.inv-items-table td { border: 1px solid #e2e8f0; padding: 8px 10px; font-size: 12px; vertical-align: middle; }
.inv-prod-name { font-weight: 600; color: #1e293b; margin-bottom: 2px; text-align: left; }
.inv-prod-attrs { display: flex; gap: 6px; font-size: 10px; color: #64748b; }
.inv-attr { background: #f1f5f9; padding: 1px 4px; border-radius: 4px; border: 1px solid #e2e8f0; }

.inv-summary-section { display: flex; justify-content: space-between; margin-bottom: 25px; gap: 20px; }
.inv-policy { flex: 1; background: #f8fafc; padding: 16px; border-radius: 8px; border: 1px dashed #cbd5e1; text-align: left; }
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

.inv-footer-print { text-align: center; color: #16a34a; }
.inv-thanks { font-weight: 800; font-size: 15px; margin-bottom: 4px; }
.inv-seeyou { font-size: 12px; color: #64748b; font-style: italic; }

.vc-loading { text-align: center; padding: 40px; color: #64748b; }

@media (max-width: 768px) {
    .info-row {
        flex-direction: column;
        align-items: flex-start;
        gap: 4px;
    }
    .info-row label {
        width: 100%;
    }
}
</style>
