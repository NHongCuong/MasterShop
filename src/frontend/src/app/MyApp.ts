import axios from "axios";
import { reactive } from "vue";
import { User } from "../interfaces/app";

interface GlobalState {
    isAuthenticated: boolean;
    user: User | null;
    cartCount: number;
    generalImages: Record<string, string>;
}

export const state = reactive<GlobalState>({
    isAuthenticated: false,
    user: null,
    cartCount: 0,
    generalImages: {}
});

export class MyApp {
    private static instance: MyApp;
    private constructor() {}

    async authenticate() {
        const token = localStorage.getItem("token");
        if (!token) {
            state.isAuthenticated = false;
            state.user = null;
            return;
        }

        try {
            const res = await axios.post("http://localhost:8081/authenticate", undefined, {
                headers: {
                    "Authorization": `Bearer ${token}`
                }
            });
            if (res.status === 200) {
                state.isAuthenticated = true;
                state.user = res.data.user;
                await this.updateCartCount(); // Đồng bộ giỏ hàng ngay khi xác thực thành công
            } else {
                this.clearState();
            }
        } catch (err) {
            this.clearState();
        }
    }

    async updateCartCount() {
        if (!state.isAuthenticated || !state.user) {
            state.cartCount = 0;
            return;
        }
        try {
            const res = await axios.get(`http://localhost:8081/cartdetail/user/${state.user.id}`);
            state.cartCount = res.data.length;
        } catch (err) {
            console.error("Error updating cart count:", err);
            state.cartCount = 0;
        }
    }

    async loadGeneralImages() {
        try {
            const res = await axios.get("http://localhost:8081/api/general-images");
            const imagesMap: Record<string, string> = {};
            res.data.forEach((img: any) => {
                imagesMap[img.imageName] = img.imageUrl;
            });
            state.generalImages = imagesMap;
        } catch (err) {
            console.error("Error loading general images:", err);
        }
    }

    clearState() {
        state.isAuthenticated = false;
        state.user = null;
        state.cartCount = 0;
        localStorage.removeItem("token");
    }

    public static getInstance() {
        if (!MyApp.instance)
            MyApp.instance = new MyApp();
        return MyApp.instance;
    }
}