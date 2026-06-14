-- Tạo bảng voucher
CREATE TABLE IF NOT EXISTS voucher (
    ID_Voucher   BIGSERIAL    PRIMARY KEY,
    Ma_Voucher   VARCHAR(100) NOT NULL UNIQUE,
    Discount_Percent INTEGER   DEFAULT 0,
    created_at   TIMESTAMP    DEFAULT NOW(),
    updated_at   TIMESTAMP
);

-- Dữ liệu mẫu
INSERT INTO voucher (Ma_Voucher, Discount_Percent) VALUES
('SALE10', 10),
('GIAM20', 20),
('HCTECH50', 50)
ON CONFLICT (Ma_Voucher) DO NOTHING;
