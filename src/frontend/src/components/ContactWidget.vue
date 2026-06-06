<script setup lang="ts">
import { ref } from 'vue';

const PHONE = '0776856666';
const PHONE_DISPLAY = '077.685.6666';

const showGreeting = ref(true);
const showContactMenu = ref(false);

const contactLinks = [
    {
        name: 'Messenger',
        iconClass: 'fab fa-facebook-messenger',
        iconType: 'messenger',
        url: 'https://m.me/'
    },
    {
        name: 'Facebook',
        iconClass: 'fab fa-facebook-f',
        iconType: 'facebook',
        url: 'https://www.facebook.com/'
    },
    {
        name: 'Zalo',
        iconType: 'zalo',
        url: `https://zalo.me/${PHONE}`
    },
    {
        name: PHONE_DISPLAY,
        iconClass: 'fas fa-phone-alt',
        iconType: 'phone',
        url: `tel:${PHONE}`,
        isPhone: true
    }
];

const closeGreeting = () => {
    showGreeting.value = false;
};

const toggleMenu = () => {
    showContactMenu.value = !showContactMenu.value;
};

const closeMenu = () => {
    showContactMenu.value = false;
};

const openMap = () => {
    window.open('https://maps.google.com/?q=HC+Shop', '_blank', 'noopener,noreferrer');
};
</script>

<template>
    <div class="cw-wrapper">
        <!-- Panel liên hệ -->
        <Transition name="cw-fade">
            <div v-if="showContactMenu" class="cw-contact-panel">
                <a
                    v-for="link in contactLinks"
                    :key="link.name"
                    class="cw-contact-item"
                    :href="link.url"
                    :target="link.isPhone ? '_self' : '_blank'"
                    rel="noopener noreferrer"
                >
                    <span class="cw-contact-icon" :class="`cw-icon-${link.iconType}`">
                        <i v-if="link.iconClass" :class="link.iconClass"></i>
                        <span v-else class="cw-zalo-text">Zalo</span>
                    </span>
                    <span class="cw-contact-label" :class="{ 'cw-phone': link.isPhone }">{{ link.name }}</span>
                </a>
            </div>
        </Transition>

        <!-- Lời chào -->
        <Transition name="cw-fade">
            <div v-if="showGreeting" class="cw-greeting" :class="{ 'cw-greeting--menu-open': showContactMenu }">
                <button class="cw-greeting-close" type="button" aria-label="Đóng lời chào" @click="closeGreeting">
                    <i class="fas fa-times"></i>
                </button>
                <div class="cw-greeting-body">
                    <img src="/images/logotech.png" alt="HC Shop" class="cw-greeting-logo" />
                    <div class="cw-greeting-text">
                        <strong>HC Shop</strong>
                        <p>HC shop hân hạnh phục vụ! Anh/chị có cần tư vấn sản phẩm hay hỗ trợ gì không ạ?</p>
                    </div>
                </div>
            </div>
        </Transition>

        <!-- Nút liên hệ: chỉ 1 icon xanh WhatsApp -->
        <div class="cw-actions">
            <Transition name="cw-slide">
                <button
                    v-if="showContactMenu"
                    type="button"
                    class="cw-btn cw-btn-map"
                    title="Vị trí cửa hàng"
                    @click="openMap"
                >
                    <i class="fas fa-map-marker-alt"></i>
                </button>
            </Transition>

            <Transition name="cw-slide">
                <button
                    v-if="showContactMenu"
                    type="button"
                    class="cw-btn cw-btn-close"
                    title="Đóng menu"
                    @click="closeMenu"
                >
                    <i class="fas fa-times"></i>
                </button>
            </Transition>

            <button
                type="button"
                class="cw-btn cw-btn-whatsapp"
                :class="{ 'cw-btn-whatsapp--active': showContactMenu }"
                :title="showContactMenu ? 'Đóng liên hệ' : 'Liên hệ'"
                @click="toggleMenu"
            >
                <i class="fab fa-whatsapp"></i>
            </button>
        </div>
    </div>
</template>

<style scoped>
.cw-wrapper {
    position: fixed;
    bottom: 28px;
    right: 28px;
    z-index: 1050;
    display: flex;
    flex-direction: column;
    align-items: flex-end;
    gap: 12px;
    font-family: 'Inter', sans-serif;
}

.cw-greeting {
    position: relative;
    width: 270px;
    background: #fff;
    border-radius: 12px;
    padding: 14px 16px;
    box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
    margin-bottom: 4px;
}

.cw-greeting::after {
    content: '';
    position: absolute;
    bottom: -8px;
    right: 22px;
    border-left: 8px solid transparent;
    border-right: 8px solid transparent;
    border-top: 8px solid #fff;
}

.cw-greeting--menu-open {
    margin-bottom: 0;
}

.cw-greeting-close {
    position: absolute;
    top: 8px;
    right: 10px;
    border: none;
    background: transparent;
    color: #94a3b8;
    font-size: 12px;
    cursor: pointer;
    padding: 2px 4px;
    line-height: 1;
    transition: color 0.2s;
}

.cw-greeting-close:hover {
    color: #475569;
}

.cw-greeting-body {
    display: flex;
    align-items: flex-start;
    gap: 12px;
    padding-right: 16px;
}

.cw-greeting-logo {
    width: 36px;
    height: 36px;
    border-radius: 50%;
    object-fit: cover;
    flex-shrink: 0;
    box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
}

.cw-greeting-text strong {
    display: block;
    font-size: 13px;
    color: #1e293b;
    margin-bottom: 4px;
}

.cw-greeting-text p {
    margin: 0;
    font-size: 12px;
    line-height: 1.45;
    color: #475569;
}

.cw-contact-panel {
    position: absolute;
    bottom: 0;
    right: 68px;
    width: 220px;
    background: #fff;
    border-radius: 12px;
    padding: 10px 0;
    box-shadow: 0 8px 30px rgba(0, 0, 0, 0.15);
}

.cw-contact-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 10px 18px;
    text-decoration: none;
    color: #334155;
    transition: background 0.2s;
}

.cw-contact-item:hover {
    background: #f8fafc;
}

.cw-contact-icon {
    width: 36px;
    height: 36px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 16px;
    flex-shrink: 0;
    color: #fff;
}

.cw-icon-messenger {
    background: linear-gradient(135deg, #00b2ff 0%, #006aff 100%);
}

.cw-icon-messenger i {
    font-size: 18px;
}

.cw-icon-facebook {
    background: #1877f2;
}

.cw-icon-facebook i {
    font-size: 17px;
}

.cw-icon-zalo {
    background: #0068ff;
}

.cw-zalo-text {
    font-size: 10px;
    font-weight: 800;
    letter-spacing: -0.3px;
    line-height: 1;
}

.cw-icon-phone {
    background: #25d366;
}

.cw-icon-phone i {
    font-size: 15px;
    transform: scaleX(-1);
}

.cw-contact-label {
    font-size: 14px;
    font-weight: 500;
}

.cw-contact-label.cw-phone {
    color: #16a34a;
    font-weight: 700;
}

.cw-actions {
    display: flex;
    flex-direction: column-reverse;
    align-items: center;
    gap: 10px;
}

.cw-btn {
    width: 52px;
    height: 52px;
    border-radius: 50%;
    border: none;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 22px;
    cursor: pointer;
    box-shadow: 0 4px 14px rgba(0, 0, 0, 0.18);
    transition: transform 0.2s, box-shadow 0.2s;
}

.cw-btn:hover {
    transform: scale(1.08);
    box-shadow: 0 6px 20px rgba(0, 0, 0, 0.22);
}

.cw-btn-whatsapp {
    background: #25d366;
    color: #fff;
}

.cw-btn-whatsapp i {
    font-size: 28px;
}

.cw-btn-whatsapp--active {
    background: #128c7e;
}

.cw-btn-map,
.cw-btn-close {
    background: #fff;
    color: #25d366;
    border: 2px solid #25d366;
    font-size: 18px;
    box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.cw-btn-close {
    color: #16a34a;
    border-color: #16a34a;
}

.cw-fade-enter-active,
.cw-fade-leave-active {
    transition: opacity 0.25s ease, transform 0.25s ease;
}

.cw-fade-enter-from,
.cw-fade-leave-to {
    opacity: 0;
    transform: translateY(8px);
}

.cw-slide-enter-active,
.cw-slide-leave-active {
    transition: opacity 0.2s ease, transform 0.2s ease;
}

.cw-slide-enter-from,
.cw-slide-leave-to {
    opacity: 0;
    transform: scale(0.6);
}

@media (max-width: 576px) {
    .cw-wrapper {
        bottom: 16px;
        right: 16px;
    }

    .cw-greeting {
        width: calc(100vw - 90px);
        max-width: 270px;
    }

    .cw-contact-panel {
        right: 60px;
        width: 200px;
    }

    .cw-btn {
        width: 46px;
        height: 46px;
        font-size: 20px;
    }
}
</style>
