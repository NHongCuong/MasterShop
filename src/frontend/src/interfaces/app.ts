export interface UserStatus {
    id: number;
    name: string;
}

export interface UserType {
    id: number;
    name: string;
}

export interface CartStatus {
    id: number;
    nameCS: string;
    created_at: Date;
    updated_at: Date;
}

export interface User {
    id: number;
    nameUser: string;
    phone: string;
    email: string;
    address: string;
    password?: string; // This property is marked as WRITE_ONLY
    verify: string;
    regtime: string;
    salt?: string; // This property is marked as WRITE_ONLY
    userStatus: UserStatus;
    userType: UserType;
}

export interface Category {
    "id": number,
    "name": string,
    "icon": string,
    "createdAt"?: string | Date,
    "updatedAt"?: string | Date,
}

export interface Supplier {
    "id": number,
    "name": string,
    "address": string,
    "phone": string,
    "email": string,
    "website": string,

}

export interface Voucher {
    id: number;
    maVoucher: string;
    discountPercent: number;
}

export interface Product {
    "id": number,
    "name": string,
    "description": string,
    "price": number,
    "avatar": string,
    "amount": number,
    "supplier": Supplier,
    "category": Category,
    "voucher"?: Voucher | null,
    "detailproductcolor"?: any[];
    "detailproductmaterial"?: any[];
    "productDemensions"?: any[];
    defaultColorId?: number | null;
    defaultMaterialId?: number | null;
    defaultDimensionId?: number | null;
}

export interface MyImage {
    itemImageSrc: string;
    thumbnailImageSrc: string;
    alt: string;
}

export interface bill {
    ID_Bill: string;
    UserID: number;
    Total: number;
    Status: string;
}

export interface ShopCart {
    ID_SC: number;
    ID_CS: number;
    ID_User: number;
}

export interface ProductItem {
    "id": number,
    "name": string,
    "description": string,
    "price": number,
    "avatar": string,
    "amount": number,
    "category": Category
}
export interface DetailProductColor{
    "idColor": number;
    "idProduct":number

}
export interface DetailProductMaterial{
    "idMaterial":number;
    "idProduct":number
}
export interface Dimensions{
    "idDimension":number;
    "name":String;
    "idProduct":number;
    "createdAt":Date;
    "updatedAt":Date
}
