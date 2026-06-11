<script setup lang="ts">
import { ref } from 'vue';
import axios from 'axios';
import Toast from 'primevue/toast';
import { useToast } from 'primevue/usetoast';

const toast = useToast();
const contactForm = ref({
    fullName: '',
    email: '',
    phoneNumber: '',
    subject: '',
    message: ''
});

const isSubmitting = ref(false);

const submitContact = async () => {
    // Validation
    if (!contactForm.value.fullName.trim()) {
        toast.add({ severity: 'warn', summary: 'Cảnh báo', detail: 'Vui lòng nhập họ và tên', life: 3000 });
        return;
    }
    if (!contactForm.value.email.trim()) {
        toast.add({ severity: 'warn', summary: 'Cảnh báo', detail: 'Vui lòng nhập email', life: 3000 });
        return;
    }
    if (!contactForm.value.message.trim()) {
        toast.add({ severity: 'warn', summary: 'Cảnh báo', detail: 'Vui lòng nhập nội dung tin nhắn', life: 3000 });
        return;
    }

    isSubmitting.value = true;
    try {
        await axios.post('http://localhost:8081/contact/add', contactForm.value);
        toast.add({ 
            severity: 'success', 
            summary: 'Gửi thành công', 
            detail: 'Cảm ơn bạn đã liên hệ. Chúng tôi sẽ phản hồi sớm nhất có thể!', 
            life: 5000 
        });
        // Reset form
        contactForm.value = {
            fullName: '',
            email: '',
            phoneNumber: '',
            subject: '',
            message: ''
        };
    } catch (error) {
        console.error('Lỗi khi gửi liên hệ:', error);
        toast.add({ 
            severity: 'error', 
            summary: 'Lỗi', 
            detail: 'Đã có lỗi xảy ra khi gửi tin nhắn. Vui lòng thử lại sau.', 
            life: 3000 
        });
    } finally {
        isSubmitting.value = false;
    }
};
</script>

<template>
    <div class="contact-page py-5" style="background-color: #f0f7ff; min-height: 80vh;">
        <Toast />
        <div class="container pb-5">
            <div class="page-header text-center mb-5">
                <p class="text-muted fs-5">Chúng tôi luôn sẵn sàng lắng nghe và hỗ trợ bạn 24/7</p>
            </div>

            <div class="row g-4 justify-content-center">
                <!-- Contact Info Cards -->
                <div class="col-lg-5">
                    <div class="row g-3">
                        <div class="col-12">
                            <div class="contact-info-card d-flex align-items-center p-4">
                                <div class="icon-box me-4">
                                    <i class="fas fa-phone-alt"></i>
                                </div>
                                <div>
                                    <h6 class="fw-bold mb-1 text-dark">Số Điện Thoại</h6>
                                    <p class="mb-0 text-muted">0557-843-408</p>
                                </div>
                            </div>
                        </div>

                        <div class="col-12">
                            <div class="contact-info-card d-flex align-items-center p-4">
                                <div class="icon-box me-4" style="background-color: #e3f2fd;">
                                    <i class="fas fa-envelope" style="color: #0396d8;"></i>
                                </div>
                                <div>
                                    <h6 class="fw-bold mb-1 text-dark">Email</h6>
                                    <p class="mb-0 text-muted">hcshop@gmail.com</p>
                                </div>
                            </div>
                        </div>

                        <div class="col-12">
                            <div class="contact-info-card d-flex align-items-center p-4">
                                <div class="icon-box me-4" style="background-color: #e3f2fd;">
                                    <i class="fas fa-map-marker-alt" style="color: #0396d8;"></i>
                                </div>
                                <div>
                                    <h6 class="fw-bold mb-1 text-dark">Địa Chỉ</h6>
                                    <p class="mb-0 text-muted">67/7 Trương Định, Khu vực Vĩnh Phước Phường An Nhơn Bắc, Tỉnh Gia Lai</p>
                                </div>
                            </div>
                        </div>

                        <div class="col-12">
                            <div class="contact-info-card d-flex align-items-center p-4">
                                <div class="icon-box me-4" style="background-color: #e3f2fd;">
                                    <i class="fas fa-clock" style="color: #0396d8;"></i>
                                </div>
                                <div>
                                    <h6 class="fw-bold mb-1 text-dark">Giờ Làm Việc</h6>
                                    <p class="mb-0 text-muted">Thứ 2 - Thứ 7: 8:00 - 21:00</p>
                                    <p class="mb-0 text-muted">Chủ Nhật: 9:00 - 18:00</p>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Contact Form -->
                <div class="col-lg-6 ms-lg-4">
                    <div class="contact-form-card p-5 h-100 shadow-sm border-0">
                        <h2 class="mb-4 fw-bold text-dark">Gửi Tin Nhắn</h2>
                        <form @submit.prevent="submitContact">
                            <div class="mb-3">
                                <input v-model="contactForm.fullName" type="text" class="form-control custom-input" placeholder="Họ và tên" required>
                            </div>
                            <div class="mb-3">
                                <input v-model="contactForm.email" type="email" class="form-control custom-input" placeholder="Email" required>
                            </div>
                            <div class="mb-3">
                                <input v-model="contactForm.phoneNumber" type="text" class="form-control custom-input" placeholder="Số điện thoại">
                            </div>
                            <div class="mb-3">
                                <input v-model="contactForm.subject" type="text" class="form-control custom-input" placeholder="Chủ đề">
                            </div>
                            <div class="mb-4">
                                <textarea v-model="contactForm.message" class="form-control custom-input" rows="5" placeholder="Nội dung tin nhắn" required></textarea>
                            </div>
                            <button type="submit" class="btn btn-send-contact w-100 py-3 fw-bold" :disabled="isSubmitting">
                                <span v-if="isSubmitting" class="spinner-border spinner-border-sm me-2"></span>
                                Gửi Liên Hệ
                            </button>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<style scoped>
.contact-page {
    font-family: 'Inter', sans-serif;
}

.contact-info-card {
    background: white;
    border-radius: 16px;
    box-shadow: 0 4px 12px rgba(0,0,0,0.03);
    transition: all 0.3s ease;
    border: 1px solid rgba(0,0,0,0.02);
}

.contact-info-card:hover {
    transform: translateY(-5px);
    box-shadow: 0 8px 24px rgba(0,0,0,0.06);
}

.icon-box {
    width: 56px;
    height: 56px;
    background-color: #e3f2fd;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #0396d8;
    font-size: 1.4rem;
    flex-shrink: 0;
}

.contact-form-card {
    background: white;
    border-radius: 20px;
}

.custom-input {
    border: 1px solid #e2e8f0;
    padding: 14px 18px;
    border-radius: 8px;
    font-size: 0.95rem;
    color: #334155;
    background-color: #fff;
    transition: all 0.2s ease;
}

.custom-input:focus {
    box-shadow: 0 0 0 4px rgba(3, 150, 216, 0.1);
    border-color: #0396d8;
    outline: none;
}

.btn-send-contact {
    background-color: #0396d8;
    border: none;
    border-radius: 10px;
    font-size: 1.1rem;
    color: white;
    transition: all 0.3s ease;
}

.btn-send-contact:hover {
    background-color: #0284c7;
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(3, 150, 216, 0.3);
}

.btn-send-contact:active {
    transform: translateY(0);
}

.btn-send-contact:disabled {
    background-color: #94a3b8;
    cursor: not-allowed;
    transform: none;
}

@media (max-width: 991.98px) {
    .contact-form-card {
        padding: 30px !important;
    }
}
</style>