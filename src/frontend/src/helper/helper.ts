export default class Helper {
    static ToMoney(amount: number) {
        if (typeof amount == 'undefined')
            amount = -1;
        return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount);
    }
    static GetImageUrl(path: string) {
        if (!path) return '';
        const firstPath = path.split(',')[0].trim();
        if (firstPath.startsWith('http')) return firstPath;
        return 'http://localhost:8081' + firstPath;
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