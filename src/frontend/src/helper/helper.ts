export default class Helper{
    static ToMoney(amount: number)
    {
        if(typeof amount == 'undefined')
            amount = -1;
        return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount);
    }
    static GetImageUrl(path: string) {
        if (!path) return '';
        if (path.startsWith('http')) return path;
        return 'http://localhost:8081' + path;
    }
}