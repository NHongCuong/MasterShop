export {}; // Biến tệp này thành một mô-đun

declare global {
    interface Window {
        data: any;
        hasRunInHome: boolean; // Vì trang chủ có hiệu viết, nên lưu lại giá trị này nếu là false thì không tạo hiệu ứng nữa
    }
}
