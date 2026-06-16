ALTER TABLE `cart_items` ADD COLUMN `variant_id` bigint DEFAULT NULL AFTER `product_id`;
ALTER TABLE `cart_items` ADD CONSTRAINT `fk_cart_items_variant` FOREIGN KEY (`variant_id`) REFERENCES `product_variants` (`variant_id`) ON DELETE SET NULL;

ALTER TABLE `order_items` ADD COLUMN `variant_id` bigint DEFAULT NULL AFTER `product_id`;
ALTER TABLE `order_items` ADD CONSTRAINT `fk_order_items_variant` FOREIGN KEY (`variant_id`) REFERENCES `product_variants` (`variant_id`) ON DELETE SET NULL;
