export default class Helper {
    static ToMoney(amount: number) {
        if (typeof amount == 'undefined')
            amount = -1;
        return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount);
    }
    static GetImageUrl(path: string, options?: { w?: number, h?: number, c?: string, q?: string, f?: string, customParams?: string }) {
        if (!path) return '';
        const firstPath = path.split(',')[0].trim();
        
        if (firstPath.startsWith('http')) {
            if (firstPath.includes('cloudinary.com') && firstPath.includes('/upload/')) {
                // Ignore if it already has transformations (checking for f_auto / q_auto to avoid duplicates)
                if (firstPath.includes('q_auto') || firstPath.includes('f_auto')) return firstPath;

                const uploadIndex = firstPath.indexOf('/upload/');
                const beforeUpload = firstPath.substring(0, uploadIndex + 8);
                const afterUpload = firstPath.substring(uploadIndex + 8);
                
                let params = [];
                params.push(`q_${options?.q || 'auto'}`);
                params.push(`f_${options?.f || 'auto'}`);
                if (options?.c) params.push(`c_${options.c}`);
                else if (options?.w || options?.h) params.push('c_limit');
                if (options?.w) params.push(`w_${options.w}`);
                if (options?.h) params.push(`h_${options.h}`);
                if (options?.customParams) params.push(options.customParams);
                
                return `${beforeUpload}${params.join(',')}/${afterUpload}`;
            }
            return firstPath;
        }
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