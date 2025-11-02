export default class Helper{
    static ToMoney(amount: number)
    {
        if(typeof amount == 'undefined')
            amount = -1;
        return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount);
    }
    static formatMoney(value: number | string | undefined): string {
        if (!value) return "0 VNĐ";
        return Number(value).toLocaleString("vi-VN") + " VNĐ";
    }
}