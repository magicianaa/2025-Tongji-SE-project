-- Migration for Task 1.3: PMS Business Logic Fix

USE esports_hotel_db;

-- Add occupancy columns to tb_room
ALTER TABLE `tb_room` 
ADD COLUMN `max_occupancy` TINYINT NOT NULL DEFAULT 1 COMMENT 'Max occupancy' AFTER `price_per_hour`,
ADD COLUMN `current_occupancy` TINYINT NOT NULL DEFAULT 0 COMMENT 'Current occupancy' AFTER `max_occupancy`;

-- Update max_occupancy based on room_type
UPDATE `tb_room` SET `max_occupancy` = 1 WHERE `room_type` = 'SINGLE';
UPDATE `tb_room` SET `max_occupancy` = 2 WHERE `room_type` = 'DOUBLE';
UPDATE `tb_room` SET `max_occupancy` = 5 WHERE `room_type` = 'FIVE_PLAYER';
UPDATE `tb_room` SET `max_occupancy` = 2 WHERE `room_type` = 'VIP';

-- Verify migration
SELECT room_no, room_type, max_occupancy, current_occupancy, status 
FROM tb_room 
ORDER BY room_no;
