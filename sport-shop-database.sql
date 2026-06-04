-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: db:3306
-- Generation Time: Apr 07, 2024 at 01:41 AM
-- Server version: 8.1.0
-- PHP Version: 8.2.10

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `pthtw2`
--

-- --------------------------------------------------------

--
-- Table structure for table `bill`
--

CREATE TABLE `bill` (
  `ID_Bill` int NOT NULL,
  `CreateDate` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `TotalMoney` int DEFAULT NULL,
  `VAT_rate` int DEFAULT '8',
  `VAT_amount` int NOT NULL,
  `TotalMoneyCheckout` float NOT NULL,
  `ID_BS` int DEFAULT '1',
  `ID_Order` int DEFAULT NULL,
  `TotalMoneyAfterSaleOff` int DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_vietnamese_ci;

--
-- Dumping data for table `bill`
--

INSERT INTO `bill` (`ID_Bill`, `CreateDate`, `TotalMoney`, `VAT_rate`, `VAT_amount`, `TotalMoneyCheckout`, `ID_BS`, `ID_Order`, `TotalMoneyAfterSaleOff`) VALUES
(52, '2024-01-26 08:06:36', 2016000, 8, 161280, 2177280, 3, 60, 2016000),
(53, '2024-03-06 04:15:27', 8000000, 8, 640000, 8640000, 4, 61, 8000000),
(54, '2024-03-28 05:00:20', 4868882, 8, 389511, 5258390, 2, 62, 4868882);

-- --------------------------------------------------------

--
-- Table structure for table `bill_status`
--

CREATE TABLE `bill_status` (
  `ID_BS` int NOT NULL,
  `Name_BS` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_vietnamese_ci DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_vietnamese_ci;

--
-- Dumping data for table `bill_status`
--

INSERT INTO `bill_status` (`ID_BS`, `Name_BS`) VALUES
(1, 'Đang xử lý'),
(2, 'Đã thanh toán'),
(3, 'Đang vận chuyển'),
(4, 'Đã hủy đơn');

-- --------------------------------------------------------

--
-- Table structure for table `bill_status_history`
--

CREATE TABLE `bill_status_history` (
  `ID_BILL` int NOT NULL,
  `ID_BS` int NOT NULL,
  `Date_BSH` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `ID_User` int NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_vietnamese_ci;

--
-- Dumping data for table `bill_status_history`
--

INSERT INTO `bill_status_history` (`ID_BILL`, `ID_BS`, `Date_BSH`, `ID_User`) VALUES
(52, 2, '2024-01-26 08:07:08', 5),
(53, 2, '2024-03-06 04:16:10', 5),
(53, 4, '2024-03-06 04:16:23', 5),
(53, 2, '2024-03-06 04:16:30', 5),
(54, 3, '2024-03-28 05:03:47', 5),
(54, 2, '2024-03-28 05:03:54', 5),
(54, 1, '2024-03-28 05:33:45', 5),
(54, 2, '2024-03-28 05:35:55', 5),
(52, 4, '2024-03-28 05:38:36', 5),
(52, 3, '2024-03-28 05:38:51', 5),
(53, 4, '2024-03-28 05:39:57', 5);

-- --------------------------------------------------------

--
-- Table structure for table `cart_detail`
--

CREATE TABLE `cart_detail` (
  `ID_SC` int NOT NULL,
  `ID_Product` int NOT NULL,
  `Amount_CD` int NOT NULL,
  `created_at` timestamp NULL DEFAULT NULL,
  `updated_at` timestamp NULL DEFAULT NULL,
  `ID_Color` int DEFAULT NULL,
  `ID_Material` int DEFAULT NULL,
  `ID_D` int DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `cart_detail`
--

INSERT INTO `cart_detail` (`ID_SC`, `ID_Product`, `Amount_CD`, `created_at`, `updated_at`, `ID_Color`, `ID_Material`, `ID_D`) VALUES
(136, 159, 2, '2024-01-26 15:05:48', '2024-01-26 15:06:08', NULL, NULL, NULL),
(137, 137, 2, '2024-03-06 11:14:53', '2024-03-06 11:15:00', NULL, NULL, NULL),
(138, 156, 2, '2024-03-28 11:59:39', '2024-03-28 11:59:58', NULL, NULL, NULL);

-- --------------------------------------------------------

--
-- Table structure for table `cart_status`
--

CREATE TABLE `cart_status` (
  `ID_CS` int NOT NULL,
  `Name_CS` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `created_at` timestamp NULL DEFAULT NULL,
  `updated_at` timestamp NULL DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `cart_status`
--

INSERT INTO `cart_status` (`ID_CS`, `Name_CS`, `created_at`, `updated_at`) VALUES
(1, 'Chưa thanh toán', '2023-08-30 13:51:22', '2023-08-30 13:51:22'),
(2, 'Đã thanh toán', '2023-08-30 13:51:42', '2023-08-30 13:51:42'),
(3, 'Đang xử lý', '2023-09-01 01:25:11', '2023-09-01 01:25:11');

-- --------------------------------------------------------

--
-- Table structure for table `category`
--

CREATE TABLE `category` (
  `ID_Category` int NOT NULL,
  `Name_Category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_vietnamese_ci DEFAULT NULL,
  `Icon` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_vietnamese_ci DEFAULT NULL,
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_vietnamese_ci;

--
-- Dumping data for table `category`
--

INSERT INTO `category` (`ID_Category`, `Name_Category`, `Icon`) VALUES
(28, 'Tạ', '/storage/images/categories/1705649489.jpg'),
(29, 'Xe', '/storage/images/categories/1705650066.jpg'),
(30, 'Găng tay', '/storage/images/categories/1705650735.jpg'),
(31, 'Dây nhảy', '/storage/images/categories/1705651385.jpg'),
(32, 'Bóng tập luyện', '/storage/images/categories/1705651979.jpg'),
(33, 'Thảm yoga', '/storage/images/categories/1705652502.jpg');

-- --------------------------------------------------------

--
-- Table structure for table `color`
--

CREATE TABLE `color` (
  `ID_Color` int NOT NULL,
  `Name_Color` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_vietnamese_ci DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_vietnamese_ci;

--
-- Dumping data for table `color`
--

INSERT INTO `color` (`ID_Color`, `Name_Color`) VALUES
(0, 'Không xác định'),
(1, 'Đỏ thẫm'),
(2, 'Đỏ tươi'),
(3, 'Đỏ cam'),
(4, 'Đỏ đun'),
(5, 'Vàng chanh'),
(6, 'Vàng nhạt'),
(7, 'Vàng cam'),
(8, 'Xanh lá cây'),
(9, 'Xanh lam'),
(10, 'Xanh dương'),
(11, 'Xanh lục'),
(12, 'Trắng tinh'),
(13, 'Trắng sữa'),
(14, 'Kem'),
(15, 'Đen nhám'),
(16, 'Đen sâu'),
(17, 'Đen mờ'),
(18, 'Xám bạc'),
(19, 'Xám đen'),
(20, 'Xám nâu'),
(21, 'Nâu đậm'),
(22, 'Nâu nhạt'),
(23, 'Nâu cam'),
(24, 'Nâu đỏ'),
(25, 'Hồng nhạt'),
(26, 'Hồng phấn'),
(27, 'Hồng đậm'),
(28, 'Tím nhạt'),
(29, 'Tím đậm'),
(30, 'Tím cam'),
(31, 'Cam đất'),
(32, 'Cam đỏ'),
(33, 'Cam nhạt'),
(34, 'Cam san hô');

-- --------------------------------------------------------

--
-- Table structure for table `comments`
--

CREATE TABLE `comments` (
  `id` bigint UNSIGNED NOT NULL,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `reply_to` bigint UNSIGNED DEFAULT NULL,
  `ID_Product` int NOT NULL,
  `ID_User` int NOT NULL,
  `created_at` timestamp NULL DEFAULT NULL,
  `updated_at` timestamp NULL DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `detail_product_color`
--

CREATE TABLE `detail_product_color` (
  `ID_Color` int NOT NULL,
  `ID_Product` int NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_vietnamese_ci;

--
-- Dumping data for table `detail_product_color`
--

INSERT INTO `detail_product_color` (`ID_Color`, `ID_Product`) VALUES
(29, 193),
(18, 193),
(25, 193),
(1, 193),
(11, 193),
(10, 195),
(33, 195),
(1, 195);

-- --------------------------------------------------------

--
-- Table structure for table `detail_product_image`
--

CREATE TABLE `detail_product_image` (
  `ID_DPI` int NOT NULL,
  `ID_Product` int NOT NULL,
  `Image` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_vietnamese_ci
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_vietnamese_ci;

-- --------------------------------------------------------

--
-- Table structure for table `aaterial`
--

CREATE TABLE `detail_product_material` (
  `ID_Material` int NOT NULL,
  `ID_Product` int NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_vietnamese_ci;

-- --------------------------------------------------------

--
-- Table structure for table `Detail_SaleOf_Product`
--

CREATE TABLE `Detail_SaleOf_Product` (
  `ID_SO` int NOT NULL,
  `ID_Product` int NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_vietnamese_ci;

--
-- Dumping data for table `Detail_SaleOf_Product`
--

INSERT INTO `Detail_SaleOf_Product` (`ID_SO`, `ID_Product`) VALUES
(2, 159),
(3, 159),
(5, 159);

-- --------------------------------------------------------

--
-- Table structure for table `dimensions`
--

CREATE TABLE `dimensions` (
  `ID_D` int NOT NULL,
  `Name_D` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `ID_Product` int NOT NULL,
  `created_at` timestamp NULL DEFAULT NULL,
  `updated_at` timestamp NULL DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `dimensions`
--

INSERT INTO `dimensions` (`ID_D`, `Name_D`, `ID_Product`, `created_at`, `updated_at`) VALUES
(9, '03 x 01 x 02', 195, NULL, NULL),
(10, '04 x 01 x 02', 195, NULL, NULL);

-- --------------------------------------------------------

--
-- Table structure for table `import_history`
--

CREATE TABLE `import_history` (
  `ID_IH` int NOT NULL,
  `TotalMoney_IH` int DEFAULT NULL,
  `DATE_IH` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_vietnamese_ci;

--
-- Dumping data for table `import_history`
--

INSERT INTO `import_history` (`ID_IH`, `TotalMoney_IH`, `DATE_IH`) VALUES
(30, 100000000, '2024-01-26 08:03:58'),
(31, 20500000, '2024-03-06 04:14:13'),
(32, 24000000, '2024-03-28 04:57:46');

-- --------------------------------------------------------

--
-- Table structure for table `import_history_details`
--

CREATE TABLE `import_history_details` (
  `id` bigint UNSIGNED NOT NULL,
  `Price_IDH` double(20,2) NOT NULL,
  `Amount_IDH` int NOT NULL,
  `ID_Color` int DEFAULT NULL,
  `ID_Material` int DEFAULT NULL,
  `ID_D` int DEFAULT NULL,
  `ID_Product` int NOT NULL,
  `ID_IH` int NOT NULL,
  `created_at` timestamp NULL DEFAULT NULL,
  `updated_at` timestamp NULL DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `import_history_details`
--

INSERT INTO `import_history_details` (`id`, `Price_IDH`, `Amount_IDH`, `ID_Color`, `ID_Material`, `ID_D`, `ID_Product`, `ID_IH`, `created_at`, `updated_at`) VALUES
(1, 1000000.00, 100, NULL, NULL, NULL, 159, 30, '2024-01-26 15:03:58', '2024-01-26 15:03:58'),
(2, 4100000.00, 5, NULL, NULL, NULL, 137, 31, '2024-03-06 11:14:13', '2024-03-06 11:14:13'),
(3, 240000.00, 100, NULL, NULL, NULL, 156, 32, '2024-03-28 11:57:46', '2024-03-28 11:57:46');

-- --------------------------------------------------------

--
-- Table structure for table `material`
--

CREATE TABLE `material` (
  `ID_Material` int NOT NULL,
  `Name_Material` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_vietnamese_ci NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_vietnamese_ci;

--
-- Dumping data for table `material`
--

INSERT INTO `material` (`ID_Material`, `Name_Material`) VALUES
(0, 'Không xác định'),
(1, 'Gỗ'),
(2, 'Thủy tinh');

-- --------------------------------------------------------

--
-- Table structure for table `methodofpayment`
--

CREATE TABLE `methodofpayment` (
  `ID_MOP` int NOT NULL,
  `Name_MOP` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_vietnamese_ci DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_vietnamese_ci;

--
-- Dumping data for table `methodofpayment`
--

INSERT INTO `methodofpayment` (`ID_MOP`, `Name_MOP`) VALUES
(1, 'Thanh toán khi nhận hàng'),
(2, 'Thanh toán bằng chuyển khoản');

-- --------------------------------------------------------

--
-- Table structure for table `migrations`
--

CREATE TABLE `migrations` (
  `id` int UNSIGNED NOT NULL,
  `migration` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `batch` int NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `migrations`
--

INSERT INTO `migrations` (`id`, `migration`, `batch`) VALUES
(9, '2023_08_30_132727_create_cart_status_table', 2),
(22, '2019_12_14_000001_create_personal_access_tokens_table', 3),
(23, '2023_09_04_115830_create_dimensions_table', 4),
(24, '2023_09_04_085147_add_id_material_columns_to_cart_detail_table', 5),
(30, '2023_09_06_055109_create_import_history_details_table', 6),
(32, '2023_09_06_152047_create_comments_table', 7),
(33, '2023_09_06_085417_create_product_price_histories_table', 8);

-- --------------------------------------------------------

--
-- Table structure for table `options`
--

CREATE TABLE `options` (
  `Name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_vietnamese_ci NOT NULL,
  `Status` tinyint NOT NULL,
  `Description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_vietnamese_ci
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_vietnamese_ci;

--
-- Dumping data for table `options`
--

INSERT INTO `options` (`Name`, `Status`, `Description`) VALUES
('password_strength', 0, ''),
('search_suggestion', 1, '');

-- --------------------------------------------------------

--
-- Table structure for table `order`
--

CREATE TABLE `order` (
  `ID_Order` int NOT NULL,
  `ID_SM` int NOT NULL,
  `ID_MOP` int NOT NULL,
  `Customer_Name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_vietnamese_ci NOT NULL,
  `Note_O` text CHARACTER SET utf8mb4 COLLATE utf8mb4_vietnamese_ci,
  `Phone_O` char(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_vietnamese_ci NOT NULL,
  `Address_O` text CHARACTER SET utf8mb4 COLLATE utf8mb4_vietnamese_ci NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_vietnamese_ci;

--
-- Dumping data for table `order`
--

INSERT INTO `order` (`ID_Order`, `ID_SM`, `ID_MOP`, `Customer_Name`, `Note_O`, `Phone_O`, `Address_O`) VALUES
(60, 1, 1, 'Nguyễn Ba Lan', 'abc xyz', '0372555412', 'Ninh Kiều Cần Thơ'),
(61, 1, 1, 'Nguyễn Ba Lan', 'aa', '0372555412', 'Ninh Kiều Cần Thơ'),
(62, 1, 1, 'Nguyễn Ba Lan', NULL, '0372555412', 'Ninh Kiều Cần Thơ');

-- --------------------------------------------------------

--
-- Table structure for table `order_detail`
--

CREATE TABLE `order_detail` (
  `ID_Order` int NOT NULL,
  `ID_SC` int NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_vietnamese_ci;

--
-- Dumping data for table `order_detail`
--

INSERT INTO `order_detail` (`ID_Order`, `ID_SC`) VALUES
(60, 136),
(61, 137),
(62, 138);

-- --------------------------------------------------------

--
-- Table structure for table `personal_access_tokens`
--

CREATE TABLE `personal_access_tokens` (
  `id` bigint UNSIGNED NOT NULL,
  `tokenable_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `tokenable_id` bigint UNSIGNED NOT NULL,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `token` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `abilities` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
  `last_used_at` timestamp NULL DEFAULT NULL,
  `expires_at` timestamp NULL DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT NULL,
  `updated_at` timestamp NULL DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `product`
--

CREATE TABLE `product` (
  `ID_Product` int NOT NULL,
  `ID_Category` int NOT NULL,
  `Name_Product` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_vietnamese_ci DEFAULT NULL,
  `Description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_vietnamese_ci,
  `Price` double DEFAULT NULL,
  `Avatar` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_vietnamese_ci DEFAULT NULL,
  `Amount_Product` int DEFAULT '0',
  `ID_S` int NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_vietnamese_ci;

--
-- Dumping data for table `product`
--

INSERT INTO `product` (`ID_Product`, `ID_Category`, `Name_Product`, `Description`, `Price`, `Avatar`, `Amount_Product`, `ID_S`) VALUES
(133, 28, 'GHẾ TẠ ĐA NĂNG KINGSPORT BK-599', 'abc', 9000000, '/storage/images/products/product_1705649905/1705649905_main.jpg', 0, 1),
(134, 29, 'XE ĐẠP TẬP ELIP JUPITER', 'bvn', 1000000, '/storage/images/products/product_1705650541/1705650541_main.jpg', 0, 2),
(135, 28, 'GIÀN TẠ ĐA NĂNG KINGSPORT BK-1999 NEW', '.,.,', 3000000, '/storage/images/products/product_1705650552/1705650552_main.jpg', 0, 2),
(137, 29, 'TRỢ GIÁ - MÁY CHẠY BỘ KINGSPORT KUNGFU ĐƠN NĂNG', ';lklkj', 4000000, '/storage/images/products/product_1705650333/1705650333_main.jpg', 3, 2),
(138, 28, 'GIÀN TẠ ĐA NĂNG KINGSPORT BK-1998 NEW', 'sjhfksjhfj', 120000000, '/storage/images/products/product_1705650565/1705650565_main.jpg', 0, 2),
(139, 29, 'XE ĐẠP TẬP ĐA NĂNG ELIP ZALO', 'babc', 43000000, '/storage/images/products/product_1705650577/1705650577_main.jpg', 0, 2),
(140, 30, 'Găng tay tập thể thao chống trầy xước chuyên dụng', 'jhkjgtkj', 200000, '/storage/images/products/product_1705650870/1705650870_main.jpg', 0, 2),
(141, 30, 'Găng Tay Tập Gym Harbinger Training Grip', 'xcxc', 10384000, '/storage/images/products/product_1705650971/1705650971_main.jpg', 0, 2),
(143, 30, 'Găng Tay Tập Gym Harbinger Men’s Flexfit', 'jhhg', 1989000, '/storage/images/products/product_1705651025/1705651025_main.jpg', 0, 2),
(145, 30, 'Harbinger Training Grip Wristwrap', 'lkjdflkjdsf', 10034000, '/storage/images/products/product_1705651199/1705651199_main.jpg', 0, 2),
(146, 30, 'Găng Tay Tập Gym Harbinger Men’s Pro Wristwrap', 'kjkjsd', 108345000, '/storage/images/products/product_1705651209/1705651209_main.jpg', 0, 1),
(147, 31, 'Dây Nhảy Giảm Cân Ptp Power - Đen', 'sff', 2300009, '/storage/images/products/product_1705651488/1705651488_main.jpg', 0, 2),
(148, 31, 'Dây Nhảy Tập Thể Dục JUMP ROPE SKLZ', 'iuiu', 23004000, '/storage/images/products/product_1705651553/1705651553_main.jpg', 0, 1),
(149, 31, 'Dây nhảy Speed Pro - Đen', 'jkk', 34000032, '/storage/images/products/product_1705651728/1705651728_main.jpg', 0, 2),
(150, 31, 'Dây Nhảy Tập Luyện Thể Thao Tại Nhà', 'kjklhj', 1200000, '/storage/images/products/product_1705651838/1705651838_main.jpg', 0, 2),
(151, 32, 'Bóng thể dục cỡ L - Xanh dương', 'jkjh', 500000, '/storage/images/products/product_1705652059/1705652059_main.jpg', 0, 1),
(153, 32, 'Bóng tạ y tế Gym Pilates 900g - Xanh Dương', 'sjhdjsh', 12000000, '/storage/images/products/product_1705652124/1705652124_main.jpg', 0, 2),
(155, 32, 'BÓNG ADIDAS 55 CM ADBL-11245GR', 'sds', 10056799, '/storage/images/products/product_1705652177/1705652177_main.jpg', 0, 2),
(156, 32, 'BÓNG ADIDAS 65CM ADBL-14246PL', 'ksjdjks', 2434441, '/storage/images/products/product_1711601841/1711601841_main.jpg', 98, 1),
(157, 32, 'BÓNG ADIDAS 65CM ADBL-14246GR', 'dfdf', 13984849, '/storage/images/products/product_1705652255/1705652255_main.jpg', 0, 2),
(158, 32, 'Bóng Tạ Sklz Trainer Med Ball (8-Lb) - Đen', 'kjk', 400000, '/storage/images/products/product_1705652350/1705652350_main.jpg', 0, 1),
(159, 32, 'Bóng Tập Yoga Sklz Trainer Sport Performance', 'Bóng Tập Yoga SKLZ Trainer Sport Performance là một công cụ linh hoạt được thiết kế để nâng cao sự linh hoạt của bạn trong thực hành Yoga và cả chế độ tập luyện sức khỏe tổng thể. Được làm từ các vật liệu chất lượng cao, bóng yoga này mang lại độ bền và ổn định, làm cho nó lý tưởng cho các bài tập khác nhau, bao gồm củng cố cơ bụng, tập trung vào cân bằng và tăng cường sự linh hoạt. Bề mặt có độ sần tăng cường cung cấp độ bám, đảm bảo việc sử dụng an toàn và thoải mái trong suốt quá trình tập luyện. Dù bạn là người mới bắt đầu hay là một người yêu thích Yoga có kinh nghiệm, Bóng Tập Yoga SKLZ Trainer Sport Performance đều phục vụ cho mọi cấp độ sức khỏe, giúp bạn đạt được mục tiêu sức khỏe một cách hiệu quả. Hãy tích hợp nó vào chế độ tập luyện của bạn để trải nghiệm sự ổn định, sức mạnh và sự linh hoạt được cải thiện, nâng cao các buổi tập Yoga của bạn lên một tầm cao mới.', 1008000, '/storage/images/products/product_1711603377/1711603377_main.jpg', 98, 2),
(172, 30, 'Găng tay bi-a Peri PR-Gloves - Hàng Chính Hãng', 'Thông tin sản phẩm đang được cập nhật', 405000, '/storage/images/products/product_1711770173/1711770173_main.jpg', 0, 0),
(173, 30, 'Găng tay bi-a 3 Seconds \"Xám\" PR-3GLOVE-04 - Hàng Chính Hãng', 'Thông tin sản phẩm đang được cập nhật', 675000, '/storage/images/products/product_1711771629/1711771629_main.jpg', 0, 0),
(174, 30, 'Găng tay bi-a 3 Seconds \"Hồng\" PR-3GLOVE-07 - Hàng Chính Hãng', 'Thông tin sản phẩm đang được cập nhật', 799000, '/storage/images/products/product_1711771852/1711771852_main.jpg', 0, 0),
(182, 30, 'Găng tay bi-a 3 Seconds \"Xanh Navy\" PR-3GLOVE-06 - Hàng Chính Hãng', 'Thông tin sản phẩm đang được cập nhật', 600000, '/storage/images/products/product_1711772577/1711772577_main.jpg', 0, 0),
(183, 30, 'Găng tay bi-a 3 Seconds \"Xanh Lá\" PR-3GLOVE-05 - Hàng Chính Hãng', 'Thông tin sản phẩm đang được cập nhật', 200000, '/storage/images/products/product_1711772851/1711772851_main.jpg', 0, 2),
(184, 30, 'Găng tay bi-a 3 Seconds \"Xanh\" PR-3GLOVE-02 - Hàng Chính Hãng', 'Thông tin sản phẩm đang được cập nhật', 675000, '/storage/images/products/product_1711772974/1711772974_main.jpg', 0, 1),
(185, 30, 'Găng tay bi-a 3 Seconds \"Đỏ\" PR-3GLOVE-01 - Hàng Chính Hãng', 'Thông tin sản phẩm đang được cập nhật', 500000, '/storage/images/products/product_1711773030/1711773030_main.jpg', 0, 2),
(186, 30, 'Găng tay bi-a 3 Seconds Limited Edition \"Vàng Đỏ\" PR-3GLOVE-VN01 - Hàng Chính Hãng', 'Thông tin sản phẩm đang được cập nhật', 600000, '/storage/images/products/product_1711773067/1711773067_main.jpg', 0, 1),
(187, 32, 'Bóng tập Yoga Bóng Yoga Tròn Cỡ Đại 65cm Cao Cấp BG (hàng nhập khẩu)', 'Thông tin sản phẩm đang được cập nhật', 175000, '/storage/images/products/product_1711773779/1711773779_main.jpg', 0, 1),
(188, 32, 'Bóng Tập Yoga - Bóng Tập Thể Hình Glofit GFY001 - Hàng Cao Cấp Chính Hãng (Tặng Kèm Bơm Và Khóa Van Dự Phòng) | Yoga Ball - Gym Ball', 'Thông tin sản phẩm đang được cập nhật', 385000, '/storage/images/products/product_1711773818/1711773818_main.jpg', 0, 1),
(189, 32, 'BÓNG TẬP GYM YOGA GLOFIT-GFY001 MÀU XANH LÁ', 'Thông tin sản phẩm đang được cập nhật', 250000, '/storage/images/products/product_1711773859/1711773859_main.jpg', 0, 2),
(190, 32, 'BG Bóng Tập Yoga, Bóng Yoga Tròn  45cm - 55cm - 65cm - 75cm Cỡ Đại Cao Cấp tặng bơm Mini - Chính Hãng (Hàng nhập khẩu)', 'Thông tin sản phẩm đang được cập nhật', 300000, '/storage/images/products/product_1711773901/1711773901_main.jpg', 0, 2),
(191, 32, 'Bóng tập Yoga, Gym 45cm Trơn Cao Cấp dày 2mm thăng bằng chống nổ có tặng kèm bơm YO32', 'Thông tin sản phẩm đang được cập nhật', 86000, '/storage/images/products/product_1711773953/1711773953_main.jpg', 0, 2),
(192, 32, 'Quả Bóng Tập Thể Dục, Tập Gym, Tập Yoga Có Gai 75cm Độ Bền Cao', 'Thông tin sản phẩm đang được cập nhật', 230000, '/storage/images/products/product_1711774000/1711774000_main.jpg', 0, 2),
(193, 32, 'Bóng tập Yoga, Gym có gai 75cm', 'Thông tin sản phẩm đang được cập nhật', 1, '/storage/images/products/product_1711774185/1711774185_main.jpg', 0, 2),
(194, 29, 'Xe đạp thể dục toàn thân Airbike Sport - Hàng chính hãng', 'Thông tin sản phẩm đang được cập nhật', 1610000, '/storage/images/products/product_1711775267/1711775267_main.jpg', 0, 2),
(195, 29, 'BG Xe đạp tập tại nhà thể dục kết hợp tay chân - giúp rèn luyện sức khỏe hiệu quả', 'Thông tin sản phẩm đang được cập nhật', 1659000, '/storage/images/products/product_1711775561/1711775561_main.jpg', 0, 2);

-- --------------------------------------------------------

--
-- Table structure for table `product_price_histories`
--

CREATE TABLE `product_price_histories` (
  `id` bigint UNSIGNED NOT NULL,
  `price` double UNSIGNED NOT NULL,
  `ID_Product` int NOT NULL,
  `date_effect` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `created_at` timestamp NULL DEFAULT NULL,
  `updated_at` timestamp NULL DEFAULT NULL,
  `ID_User` int NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `product_price_histories`
--

INSERT INTO `product_price_histories` (`id`, `price`, `ID_Product`, `date_effect`, `created_at`, `updated_at`, `ID_User`) VALUES
(1, 2434441, 156, '2024-03-28 04:57:21', '2024-03-28 11:57:21', '2024-03-28 11:57:21', 5),
(2, 405000, 172, '2024-03-30 03:42:53', '2024-03-30 10:42:53', '2024-03-30 10:42:53', 5),
(3, 675000, 173, '2024-03-30 04:07:09', '2024-03-30 11:07:09', '2024-03-30 11:07:09', 5),
(4, 799000, 174, '2024-03-30 04:10:52', '2024-03-30 11:10:52', '2024-03-30 11:10:52', 5),
(5, 600000, 182, '2024-03-30 04:22:57', '2024-03-30 11:22:57', '2024-03-30 11:22:57', 5),
(6, 200000, 183, '2024-03-30 04:27:31', '2024-03-30 11:27:31', '2024-03-30 11:27:31', 5),
(7, 675000, 184, '2024-03-30 04:29:34', '2024-03-30 11:29:34', '2024-03-30 11:29:34', 5),
(8, 500000, 185, '2024-03-30 04:30:30', '2024-03-30 11:30:30', '2024-03-30 11:30:30', 5),
(9, 600000, 186, '2024-03-30 04:31:07', '2024-03-30 11:31:07', '2024-03-30 11:31:07', 5),
(10, 175000, 187, '2024-03-30 04:42:59', '2024-03-30 11:42:59', '2024-03-30 11:42:59', 5),
(11, 385000, 188, '2024-03-30 04:43:38', '2024-03-30 11:43:38', '2024-03-30 11:43:38', 5),
(12, 250000, 189, '2024-03-30 04:44:19', '2024-03-30 11:44:19', '2024-03-30 11:44:19', 5),
(13, 300000, 190, '2024-03-30 04:45:01', '2024-03-30 11:45:01', '2024-03-30 11:45:01', 5),
(14, 86000, 191, '2024-03-30 04:45:53', '2024-03-30 11:45:53', '2024-03-30 11:45:53', 5),
(15, 230000, 192, '2024-03-30 04:46:40', '2024-03-30 11:46:40', '2024-03-30 11:46:40', 5),
(16, 1, 193, '2024-03-30 04:49:45', '2024-03-30 11:49:45', '2024-03-30 11:49:45', 5),
(17, 1610000, 194, '2024-03-30 05:07:47', '2024-03-30 12:07:47', '2024-03-30 12:07:47', 5),
(18, 1659000, 195, '2024-03-30 05:12:41', '2024-03-30 12:12:41', '2024-03-30 12:12:41', 5);

-- --------------------------------------------------------

--
-- Table structure for table `Rate`
--

CREATE TABLE `Rate` (
  `ID_Rate` int NOT NULL,
  `Value_Rate` int NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_vietnamese_ci;

--
-- Dumping data for table `Rate`
--

INSERT INTO `Rate` (`ID_Rate`, `Value_Rate`) VALUES
(1, 1),
(2, 2),
(3, 3),
(4, 4),
(5, 5);

-- --------------------------------------------------------

--
-- Table structure for table `Review_Rating`
--

CREATE TABLE `Review_Rating` (
  `Content_RR` text CHARACTER SET utf8mb4 COLLATE utf8mb4_vietnamese_ci,
  `ID_Product` int NOT NULL,
  `ID_User` int NOT NULL,
  `ID_Rate` int NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_vietnamese_ci;

--
-- Dumping data for table `Review_Rating`
--

INSERT INTO `Review_Rating` (`Content_RR`, `ID_Product`, `ID_User`, `ID_Rate`, `created_at`) VALUES
('Sản phẩm rất tốt', 159, 5, 5, '2024-01-26 08:08:35'),
('Tệ', 156, 5, 5, '2024-03-28 05:07:18');

-- --------------------------------------------------------

--
-- Table structure for table `Sale_Off`
--

CREATE TABLE `Sale_Off` (
  `ID_SO` int NOT NULL,
  `Name_SO` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_vietnamese_ci NOT NULL,
  `Description_SO` text CHARACTER SET utf8mb4 COLLATE utf8mb4_vietnamese_ci,
  `Discount_Percent_SO` double NOT NULL,
  `Start_Date_SO` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `End_Date_SO` timestamp NULL DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_vietnamese_ci;

--
-- Dumping data for table `Sale_Off`
--

INSERT INTO `Sale_Off` (`ID_SO`, `Name_SO`, `Description_SO`, `Discount_Percent_SO`, `Start_Date_SO`, `End_Date_SO`) VALUES
(2, 'noel', NULL, 10, '2024-11-21 17:00:00', '2024-12-11 17:00:00'),
(3, 'Ngày quốc tế phụ nữ', NULL, 15, '2024-02-29 00:00:00', '2024-09-10 00:00:00'),
(5, 'noel 2022', NULL, 10, '2022-11-01 00:00:00', '2022-12-31 00:00:00');

-- --------------------------------------------------------

--
-- Table structure for table `ship_method`
--

CREATE TABLE `ship_method` (
  `ID_SM` int NOT NULL,
  `Name_SM` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_vietnamese_ci NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_vietnamese_ci;

--
-- Dumping data for table `ship_method`
--

INSERT INTO `ship_method` (`ID_SM`, `Name_SM`) VALUES
(1, 'Giao hàng nhanh');

-- --------------------------------------------------------

--
-- Table structure for table `shoppingcart`
--

CREATE TABLE `shoppingcart` (
  `ID_User` int NOT NULL,
  `ID_SC` int NOT NULL,
  `ID_CS` int NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_vietnamese_ci;

--
-- Dumping data for table `shoppingcart`
--

INSERT INTO `shoppingcart` (`ID_User`, `ID_SC`, `ID_CS`) VALUES
(5, 136, 3),
(5, 137, 3),
(5, 138, 3);

-- --------------------------------------------------------

--
-- Table structure for table `supplier`
--

CREATE TABLE `supplier` (
  `ID_S` int NOT NULL,
  `Name_S` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_vietnamese_ci NOT NULL,
  `Address_S` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_vietnamese_ci NOT NULL,
  `Phone_S` char(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_vietnamese_ci NOT NULL,
  `Email_S` char(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_vietnamese_ci DEFAULT NULL,
  `Website_S` char(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_vietnamese_ci DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_vietnamese_ci;

--
-- Dumping data for table `supplier`
--

INSERT INTO `supplier` (`ID_S`, `Name_S`, `Address_S`, `Phone_S`, `Email_S`, `Website_S`) VALUES
(0, 'Không rõ', '', '', NULL, NULL),
(1, 'Công ty Nguyễn Văn A', 'Cần Thơ - Ninh Kiều', '0123', 'nva@gmail.com', 'cty.a.com'),
(2, 'Công ty Nguyễn Thị F', 'Hồ Chí Minh', '0123567342', 'nvf@gmail.com', 'f.website.com');

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `ID_User` int NOT NULL,
  `Name_User` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_vietnamese_ci DEFAULT NULL,
  `Phone` char(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_vietnamese_ci DEFAULT NULL,
  `Email` char(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_vietnamese_ci NOT NULL,
  `Address` text CHARACTER SET utf8mb4 COLLATE utf8mb4_vietnamese_ci,
  `password` varchar(65) CHARACTER SET utf8mb4 COLLATE utf8mb4_vietnamese_ci NOT NULL,
  `verify` char(0) CHARACTER SET utf8mb4 COLLATE utf8mb4_vietnamese_ci DEFAULT NULL,
  `regtime` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `salt` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_vietnamese_ci DEFAULT NULL,
  `ID_UT` int NOT NULL DEFAULT '1',
  `ID_UStatus` int DEFAULT '1'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_vietnamese_ci;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`ID_User`, `Name_User`, `Phone`, `Email`, `Address`, `password`, `verify`, `regtime`, `salt`, `ID_UT`, `ID_UStatus`) VALUES
(5, 'Nguyễn Văn Test', '0123456788', 'nguyenvantest@gmail.com', 'Tân Châu - An Giang', '$2y$10$TlP0noYlq4xDuwhLVanI0OgifnXEXeacmwmgumDU4vWHHUjvVRS0y', NULL, '2023-08-29 13:51:11', NULL, 2, 1),
(6, 'Nguyễn Văn Huy', NULL, 'nguyenvantest2@gmail.com', NULL, '$2y$10$TlP0noYlq4xDuwhLVanI0OgifnXEXeacmwmgumDU4vWHHUjvVRS0y', NULL, '2023-08-29 14:31:27', NULL, 3, 1),
(7, 'Lê Khách Hàng', NULL, 'khachhang@gmail.com', NULL, '$2y$10$DFSfHaLDmeIuokDVub1H9eTk.dvDRpnPZi5sVQ8vfE0VUJRWYQ2w2', NULL, '2023-11-13 04:19:50', NULL, 1, 1),
(8, NULL, NULL, 'asdsad@yahoo.com', NULL, '$2y$10$hmnk62zvwvjsuWG9tWgL5OmlXsvouQ70uKBaljY7d8umnq6te1r2S', NULL, '2023-11-23 06:11:19', NULL, 1, 1),
(9, NULL, NULL, 'nguyentantaitcag2000@yahoo.com', NULL, '$2y$10$LrShWws6dlQ1mg3R90DEhegdhdMAF8Zu8bsPAJaFMcqOQ4gxGPcta', NULL, '2023-11-23 06:11:36', NULL, 1, 1),
(10, NULL, NULL, 'xxxxzzzzz@yahoo.com', NULL, '$2y$10$dadhkGKGU9bjK67WWbYR1uENf9PxC6/iNRXxUECIlVlk.eT53Nzi2', NULL, '2023-11-23 06:12:12', NULL, 1, 1),
(11, NULL, NULL, 'jjjjj@yahoo.com', NULL, '$2y$10$EWCL1p5sNPxFjf4ZAB2aFuwuXMomquKo5qv4Nm6mFyxA4SQ7jtYhy', NULL, '2023-11-23 06:16:42', NULL, 1, 1);

-- --------------------------------------------------------

--
-- Table structure for table `user_type`
--

CREATE TABLE `user_type` (
  `ID_UT` int NOT NULL,
  `Name_UT` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_vietnamese_ci NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_vietnamese_ci;

--
-- Dumping data for table `user_type`
--

INSERT INTO `user_type` (`ID_UT`, `Name_UT`) VALUES
(1, 'user'),
(2, 'admin'),
(3, 'seller');

-- --------------------------------------------------------

--
-- Table structure for table `User_UStatus`
--

CREATE TABLE `User_UStatus` (
  `ID_UStatus` int NOT NULL,
  `Name_UStatus` char(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_vietnamese_ci NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_vietnamese_ci;

--
-- Dumping data for table `User_UStatus`
--

INSERT INTO `User_UStatus` (`ID_UStatus`, `Name_UStatus`) VALUES
(1, 'Đang hoạt động'),
(2, 'Đã bị khoá');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `bill`
--
ALTER TABLE `bill`
  ADD PRIMARY KEY (`ID_Bill`),
  ADD KEY `fk_ksdajf` (`ID_BS`),
  ADD KEY `fk_sldjsdj` (`ID_Order`);

--
-- Indexes for table `bill_status`
--
ALTER TABLE `bill_status`
  ADD PRIMARY KEY (`ID_BS`);

--
-- Indexes for table `bill_status_history`
--
ALTER TABLE `bill_status_history`
  ADD KEY `ID_BILL` (`ID_BILL`),
  ADD KEY `ID_BS` (`ID_BS`),
  ADD KEY `fk_id_user_bill_status_history` (`ID_User`);

--
-- Indexes for table `cart_detail`
--
ALTER TABLE `cart_detail`
  ADD KEY `cart_detail_id_material_foreign` (`ID_Material`),
  ADD KEY `cart_detail_id_sc_foreign` (`ID_SC`),
  ADD KEY `cart_detail_id_product_foreign` (`ID_Product`),
  ADD KEY `cart_detail_id_d_foreign` (`ID_D`);

--
-- Indexes for table `cart_status`
--
ALTER TABLE `cart_status`
  ADD PRIMARY KEY (`ID_CS`);

--
-- Indexes for table `category`
--
ALTER TABLE `category`
  ADD PRIMARY KEY (`ID_Category`);

--
-- Indexes for table `color`
--
ALTER TABLE `color`
  ADD PRIMARY KEY (`ID_Color`);

--
-- Indexes for table `comments`
--
ALTER TABLE `comments`
  ADD PRIMARY KEY (`id`),
  ADD KEY `comments_id_product_foreign` (`ID_Product`),
  ADD KEY `comments_id_user_foreign` (`ID_User`),
  ADD KEY `comments_reply_to_foreign` (`reply_to`);

--
-- Indexes for table `detail_product_color`
--
ALTER TABLE `detail_product_color`
  ADD KEY `FK_DSACUA` (`ID_Color`),
  ADD KEY `FK_CUADA` (`ID_Product`);

--
-- Indexes for table `detail_product_image`
--
ALTER TABLE `detail_product_image`
  ADD PRIMARY KEY (`ID_DPI`),
  ADD KEY `fk_sdkjaskdjadkad` (`ID_Product`);

--
-- Indexes for table `detail_product_material`
--
ALTER TABLE `detail_product_material`
  ADD KEY `ID_Material` (`ID_Material`),
  ADD KEY `ID_Product` (`ID_Product`);

--
-- Indexes for table `Detail_SaleOf_Product`
--
ALTER TABLE `Detail_SaleOf_Product`
  ADD KEY `fk_asdkjasd` (`ID_SO`),
  ADD KEY `fk_asdk123d` (`ID_Product`);

--
-- Indexes for table `dimensions`
--
ALTER TABLE `dimensions`
  ADD PRIMARY KEY (`ID_D`),
  ADD KEY `dimensions_id_product_foreign` (`ID_Product`);

--
-- Indexes for table `import_history`
--
ALTER TABLE `import_history`
  ADD PRIMARY KEY (`ID_IH`);

--
-- Indexes for table `import_history_details`
--
ALTER TABLE `import_history_details`
  ADD PRIMARY KEY (`id`),
  ADD KEY `import_history_details_id_color_foreign` (`ID_Color`),
  ADD KEY `import_history_details_id_material_foreign` (`ID_Material`),
  ADD KEY `import_history_details_id_product_foreign` (`ID_Product`),
  ADD KEY `import_history_details_id_d_foreign` (`ID_D`),
  ADD KEY `import_history_details_id_ih_foreign` (`ID_IH`);

--
-- Indexes for table `material`
--
ALTER TABLE `material`
  ADD PRIMARY KEY (`ID_Material`);

--
-- Indexes for table `methodofpayment`
--
ALTER TABLE `methodofpayment`
  ADD PRIMARY KEY (`ID_MOP`);

--
-- Indexes for table `migrations`
--
ALTER TABLE `migrations`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `options`
--
ALTER TABLE `options`
  ADD PRIMARY KEY (`Name`);

--
-- Indexes for table `order`
--
ALTER TABLE `order`
  ADD PRIMARY KEY (`ID_Order`),
  ADD KEY `ID_MOB` (`ID_MOP`),
  ADD KEY `order_ibfk_2` (`ID_SM`);

--
-- Indexes for table `order_detail`
--
ALTER TABLE `order_detail`
  ADD KEY `ID_Order` (`ID_Order`),
  ADD KEY `ID_SC` (`ID_SC`);

--
-- Indexes for table `personal_access_tokens`
--
ALTER TABLE `personal_access_tokens`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `personal_access_tokens_token_unique` (`token`),
  ADD KEY `personal_access_tokens_tokenable_type_tokenable_id_index` (`tokenable_type`,`tokenable_id`);

--
-- Indexes for table `product`
--
ALTER TABLE `product`
  ADD PRIMARY KEY (`ID_Product`),
  ADD KEY `fk_sdjadkj` (`ID_Category`),
  ADD KEY `fk_cuaNCC` (`ID_S`);

--
-- Indexes for table `product_price_histories`
--
ALTER TABLE `product_price_histories`
  ADD PRIMARY KEY (`id`),
  ADD KEY `product_price_histories_id_product_foreign` (`ID_Product`),
  ADD KEY `fk_id_user_product_price_histories` (`ID_User`);

--
-- Indexes for table `Rate`
--
ALTER TABLE `Rate`
  ADD PRIMARY KEY (`ID_Rate`);

--
-- Indexes for table `Review_Rating`
--
ALTER TABLE `Review_Rating`
  ADD KEY `fk_sdccx` (`ID_Product`),
  ADD KEY `fk_sdsdw` (`ID_User`),
  ADD KEY `fk_cxcxcvvcv` (`ID_Rate`);

--
-- Indexes for table `Sale_Off`
--
ALTER TABLE `Sale_Off`
  ADD PRIMARY KEY (`ID_SO`);

--
-- Indexes for table `ship_method`
--
ALTER TABLE `ship_method`
  ADD PRIMARY KEY (`ID_SM`);

--
-- Indexes for table `shoppingcart`
--
ALTER TABLE `shoppingcart`
  ADD PRIMARY KEY (`ID_SC`),
  ADD KEY `FK_CUA` (`ID_User`),
  ADD KEY `fk_sdkkjkj` (`ID_CS`);

--
-- Indexes for table `supplier`
--
ALTER TABLE `supplier`
  ADD PRIMARY KEY (`ID_S`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`ID_User`),
  ADD KEY `fk_ssfdsddkj` (`ID_UT`),
  ADD KEY `fk_fdksj` (`ID_UStatus`);

--
-- Indexes for table `user_type`
--
ALTER TABLE `user_type`
  ADD PRIMARY KEY (`ID_UT`);

--
-- Indexes for table `User_UStatus`
--
ALTER TABLE `User_UStatus`
  ADD PRIMARY KEY (`ID_UStatus`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `bill`
--
ALTER TABLE `bill`
  MODIFY `ID_Bill` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=55;

--
-- AUTO_INCREMENT for table `bill_status`
--
ALTER TABLE `bill_status`
  MODIFY `ID_BS` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT for table `cart_status`
--
ALTER TABLE `cart_status`
  MODIFY `ID_CS` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT for table `category`
--
ALTER TABLE `category`
  MODIFY `ID_Category` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=34;

--
-- AUTO_INCREMENT for table `color`
--
ALTER TABLE `color`
  MODIFY `ID_Color` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=35;

--
-- AUTO_INCREMENT for table `comments`
--
ALTER TABLE `comments`
  MODIFY `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;

--
-- AUTO_INCREMENT for table `detail_product_image`
--
ALTER TABLE `detail_product_image`
  MODIFY `ID_DPI` int NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `dimensions`
--
ALTER TABLE `dimensions`
  MODIFY `ID_D` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;

--
-- AUTO_INCREMENT for table `import_history`
--
ALTER TABLE `import_history`
  MODIFY `ID_IH` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=33;

--
-- AUTO_INCREMENT for table `import_history_details`
--
ALTER TABLE `import_history_details`
  MODIFY `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT for table `material`
--
ALTER TABLE `material`
  MODIFY `ID_Material` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT for table `methodofpayment`
--
ALTER TABLE `methodofpayment`
  MODIFY `ID_MOP` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT for table `migrations`
--
ALTER TABLE `migrations`
  MODIFY `id` int UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=34;

--
-- AUTO_INCREMENT for table `order`
--
ALTER TABLE `order`
  MODIFY `ID_Order` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=63;

--
-- AUTO_INCREMENT for table `personal_access_tokens`
--
ALTER TABLE `personal_access_tokens`
  MODIFY `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `product`
--
ALTER TABLE `product`
  MODIFY `ID_Product` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=196;

--
-- AUTO_INCREMENT for table `product_price_histories`
--
ALTER TABLE `product_price_histories`
  MODIFY `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=19;

--
-- AUTO_INCREMENT for table `Rate`
--
ALTER TABLE `Rate`
  MODIFY `ID_Rate` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT for table `Sale_Off`
--
ALTER TABLE `Sale_Off`
  MODIFY `ID_SO` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT for table `ship_method`
--
ALTER TABLE `ship_method`
  MODIFY `ID_SM` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT for table `shoppingcart`
--
ALTER TABLE `shoppingcart`
  MODIFY `ID_SC` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=139;

--
-- AUTO_INCREMENT for table `supplier`
--
ALTER TABLE `supplier`
  MODIFY `ID_S` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `ID_User` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=12;

--
-- AUTO_INCREMENT for table `user_type`
--
ALTER TABLE `user_type`
  MODIFY `ID_UT` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT for table `User_UStatus`
--
ALTER TABLE `User_UStatus`
  MODIFY `ID_UStatus` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `bill`
--
ALTER TABLE `bill`
  ADD CONSTRAINT `fk_sldjsdj` FOREIGN KEY (`ID_Order`) REFERENCES `order` (`ID_Order`) ON DELETE CASCADE;

--
-- Constraints for table `bill_status_history`
--
ALTER TABLE `bill_status_history`
  ADD CONSTRAINT `bill_status_history_ibfk_1` FOREIGN KEY (`ID_BILL`) REFERENCES `bill` (`ID_Bill`),
  ADD CONSTRAINT `bill_status_history_ibfk_2` FOREIGN KEY (`ID_BS`) REFERENCES `bill_status` (`ID_BS`),
  ADD CONSTRAINT `fk_id_user_bill_status_history` FOREIGN KEY (`ID_User`) REFERENCES `users` (`ID_User`);

--
-- Constraints for table `cart_detail`
--
ALTER TABLE `cart_detail`
  ADD CONSTRAINT `cart_detail_id_d_foreign` FOREIGN KEY (`ID_D`) REFERENCES `dimensions` (`ID_D`) ON DELETE CASCADE,
  ADD CONSTRAINT `cart_detail_id_material_foreign` FOREIGN KEY (`ID_Material`) REFERENCES `material` (`ID_Material`) ON DELETE CASCADE,
  ADD CONSTRAINT `cart_detail_id_product_foreign` FOREIGN KEY (`ID_Product`) REFERENCES `product` (`ID_Product`) ON DELETE CASCADE,
  ADD CONSTRAINT `cart_detail_id_sc_foreign` FOREIGN KEY (`ID_SC`) REFERENCES `shoppingcart` (`ID_SC`) ON DELETE CASCADE;

--
-- Constraints for table `comments`
--
ALTER TABLE `comments`
  ADD CONSTRAINT `comments_id_product_foreign` FOREIGN KEY (`ID_Product`) REFERENCES `product` (`ID_Product`) ON DELETE CASCADE,
  ADD CONSTRAINT `comments_id_user_foreign` FOREIGN KEY (`ID_User`) REFERENCES `users` (`ID_User`) ON DELETE CASCADE,
  ADD CONSTRAINT `comments_reply_to_foreign` FOREIGN KEY (`reply_to`) REFERENCES `comments` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `detail_product_color`
--
ALTER TABLE `detail_product_color`
  ADD CONSTRAINT `FK_CUADA` FOREIGN KEY (`ID_Product`) REFERENCES `product` (`ID_Product`) ON DELETE CASCADE,
  ADD CONSTRAINT `FK_DSACUA` FOREIGN KEY (`ID_Color`) REFERENCES `color` (`ID_Color`);

--
-- Constraints for table `detail_product_image`
--
ALTER TABLE `detail_product_image`
  ADD CONSTRAINT `fk_sdkjaskdjadkad` FOREIGN KEY (`ID_Product`) REFERENCES `product` (`ID_Product`) ON DELETE CASCADE;

--
-- Constraints for table `detail_product_material`
--
ALTER TABLE `detail_product_material`
  ADD CONSTRAINT `detail_product_material_ibfk_1` FOREIGN KEY (`ID_Material`) REFERENCES `material` (`ID_Material`) ON DELETE CASCADE,
  ADD CONSTRAINT `detail_product_material_ibfk_2` FOREIGN KEY (`ID_Product`) REFERENCES `product` (`ID_Product`) ON DELETE CASCADE;

--
-- Constraints for table `Detail_SaleOf_Product`
--
ALTER TABLE `Detail_SaleOf_Product`
  ADD CONSTRAINT `fk_asdk123d` FOREIGN KEY (`ID_Product`) REFERENCES `Product` (`ID_Product`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_asdkjasd` FOREIGN KEY (`ID_SO`) REFERENCES `Sale_Off` (`ID_SO`) ON DELETE CASCADE;

--
-- Constraints for table `dimensions`
--
ALTER TABLE `dimensions`
  ADD CONSTRAINT `dimensions_id_product_foreign` FOREIGN KEY (`ID_Product`) REFERENCES `product` (`ID_Product`) ON DELETE CASCADE;

--
-- Constraints for table `import_history_details`
--
ALTER TABLE `import_history_details`
  ADD CONSTRAINT `import_history_details_id_color_foreign` FOREIGN KEY (`ID_Color`) REFERENCES `color` (`ID_Color`) ON DELETE CASCADE,
  ADD CONSTRAINT `import_history_details_id_d_foreign` FOREIGN KEY (`ID_D`) REFERENCES `dimensions` (`ID_D`) ON DELETE CASCADE,
  ADD CONSTRAINT `import_history_details_id_ih_foreign` FOREIGN KEY (`ID_IH`) REFERENCES `import_history` (`ID_IH`) ON DELETE CASCADE,
  ADD CONSTRAINT `import_history_details_id_material_foreign` FOREIGN KEY (`ID_Material`) REFERENCES `material` (`ID_Material`) ON DELETE CASCADE,
  ADD CONSTRAINT `import_history_details_id_product_foreign` FOREIGN KEY (`ID_Product`) REFERENCES `product` (`ID_Product`) ON DELETE CASCADE;

--
-- Constraints for table `order`
--
ALTER TABLE `order`
  ADD CONSTRAINT `order_ibfk_2` FOREIGN KEY (`ID_SM`) REFERENCES `ship_method` (`ID_SM`),
  ADD CONSTRAINT `order_ibfk_3` FOREIGN KEY (`ID_MOP`) REFERENCES `methodofpayment` (`ID_MOP`) ON DELETE CASCADE;

--
-- Constraints for table `order_detail`
--
ALTER TABLE `order_detail`
  ADD CONSTRAINT `order_detail_ibfk_2` FOREIGN KEY (`ID_SC`) REFERENCES `shoppingcart` (`ID_SC`) ON DELETE CASCADE;

--
-- Constraints for table `product`
--
ALTER TABLE `product`
  ADD CONSTRAINT `fk_cuaNCC` FOREIGN KEY (`ID_S`) REFERENCES `supplier` (`ID_S`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_sdjadkj` FOREIGN KEY (`ID_Category`) REFERENCES `category` (`ID_Category`);

--
-- Constraints for table `product_price_histories`
--
ALTER TABLE `product_price_histories`
  ADD CONSTRAINT `fk_id_user_product_price_histories` FOREIGN KEY (`ID_User`) REFERENCES `users` (`ID_User`),
  ADD CONSTRAINT `product_price_histories_id_product_foreign` FOREIGN KEY (`ID_Product`) REFERENCES `product` (`ID_Product`) ON DELETE CASCADE;

--
-- Constraints for table `Review_Rating`
--
ALTER TABLE `Review_Rating`
  ADD CONSTRAINT `fk_cxcxcvvcv` FOREIGN KEY (`ID_Rate`) REFERENCES `Rate` (`ID_Rate`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_sdccx` FOREIGN KEY (`ID_Product`) REFERENCES `Product` (`ID_Product`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_sdsdw` FOREIGN KEY (`ID_User`) REFERENCES `Users` (`ID_User`) ON DELETE CASCADE;

--
-- Constraints for table `shoppingcart`
--
ALTER TABLE `shoppingcart`
  ADD CONSTRAINT `FK_CUA` FOREIGN KEY (`ID_User`) REFERENCES `users` (`ID_User`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_sdkkjkj` FOREIGN KEY (`ID_CS`) REFERENCES `cart_status` (`ID_CS`);

--
-- Constraints for table `users`
--
ALTER TABLE `users`
  ADD CONSTRAINT `fk_fdksj` FOREIGN KEY (`ID_UStatus`) REFERENCES `User_UStatus` (`ID_UStatus`),
  ADD CONSTRAINT `fk_ssfdsddkj` FOREIGN KEY (`ID_UT`) REFERENCES `user_type` (`ID_UT`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
