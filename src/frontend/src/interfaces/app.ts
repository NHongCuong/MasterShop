export interface UserStatus{
    id: number;
    name: string;
}
export interface UserType{
    id: number;
    name: string;
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

export interface Category{
    "id": number,
    "name": string,
    "icon": string,
}
export interface Supplier{
    "id": number,
    "name": string,
    "address": string,
    "phone": string,
    "email": string,
    "website": string,

}
export interface Product{
    "id": number,
    "name": string,
    "description": string,
    "price": number,
    "avatar": string,
    "amount": number,
    "supplier": Supplier,
    "category": Category,

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
export interface ProductItem {
    "id": number,
    "name": string,
    "description": string,
    "price": number,
    "avatar": string,
    "amount": number,
    "category": Category
}