export default class Helper {
    static ToMoney(amount: number) {
        if (typeof amount == 'undefined')
            amount = -1;
        return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount);
    }
    static GetImageUrl(path: string) {
        if (!path) return '';
        if (path.startsWith('http')) return path;
        return 'http://localhost:8081' + path;
    }
    static DateFormat(date: string | Date | null) {
        if (!date) return '—';
        return new Date(date).toLocaleString('vi-VN', {
            year: 'numeric',
            month: '2-digit',
            day: '2-digit',
            hour: '2-digit',
            minute: '2-digit',
            second: '2-digit'
        });
    }
}