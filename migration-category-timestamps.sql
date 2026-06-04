-- Thêm cột Ngày tạo / Ngày sửa cho bảng category (chạy thủ công nếu cần)
-- Lưu ý: Ứng dụng cũng tự chạy migration khi khởi động (CategorySchemaMigration.java)

ALTER TABLE `category`
  ADD COLUMN IF NOT EXISTS `created_at` DATETIME NULL DEFAULT NULL AFTER `Icon`;

ALTER TABLE `category`
  ADD COLUMN IF NOT EXISTS `updated_at` DATETIME NULL DEFAULT NULL AFTER `created_at`;

UPDATE `category` SET `created_at` = NOW() WHERE `created_at` IS NULL;
