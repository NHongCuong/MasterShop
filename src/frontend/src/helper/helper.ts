export default class Helper{
    static ToMoney(amount: number)
    {
        if(typeof amount == 'undefined')
            amount = -1;
        return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount);
    }
}