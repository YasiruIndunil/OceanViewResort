-- ============================================================
-- Ocean View Resort - Database Schema
-- MySQL with Stored Procedures
-- ============================================================

CREATE DATABASE IF NOT EXISTS ocean_view_resort;
USE ocean_view_resort;

-- ============================================================
-- TABLE: users (Staff Authentication)
-- ============================================================
CREATE TABLE IF NOT EXISTS users (
    user_id     INT AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(50)  NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,  -- SHA-256 hashed
    full_name   VARCHAR(100) NOT NULL,
    role        ENUM('ADMIN','STAFF') DEFAULT 'STAFF',
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- TABLE: rooms
-- ============================================================
CREATE TABLE IF NOT EXISTS rooms (
    room_id     INT AUTO_INCREMENT PRIMARY KEY,
    room_number VARCHAR(10)  NOT NULL UNIQUE,
    room_type   ENUM('STANDARD','DELUXE','SUITE','OCEAN_VIEW') NOT NULL,
    rate_per_night DECIMAL(10,2) NOT NULL,
    max_guests  INT DEFAULT 2,
    is_available TINYINT(1) DEFAULT 1
);

-- ============================================================
-- TABLE: guests
-- ============================================================
CREATE TABLE IF NOT EXISTS guests (
    guest_id      INT AUTO_INCREMENT PRIMARY KEY,
    guest_name    VARCHAR(100) NOT NULL,
    address       TEXT,
    contact_number VARCHAR(20) NOT NULL,
    email         VARCHAR(100),
    nic_number    VARCHAR(20),
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- TABLE: reservations
-- ============================================================
CREATE TABLE IF NOT EXISTS reservations (
    reservation_id     INT AUTO_INCREMENT PRIMARY KEY,
    reservation_number VARCHAR(20) NOT NULL UNIQUE,
    guest_id           INT NOT NULL,
    room_id            INT NOT NULL,
    check_in_date      DATE NOT NULL,
    check_out_date     DATE NOT NULL,
    num_guests         INT DEFAULT 1,
    total_amount       DECIMAL(10,2),
    status             ENUM('CONFIRMED','CHECKED_IN','CHECKED_OUT','CANCELLED') DEFAULT 'CONFIRMED',
    special_requests   TEXT,
    created_by         INT,
    created_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (guest_id)   REFERENCES guests(guest_id),
    FOREIGN KEY (room_id)    REFERENCES rooms(room_id),
    FOREIGN KEY (created_by) REFERENCES users(user_id)
);

-- ============================================================
-- TABLE: bills
-- ============================================================
CREATE TABLE IF NOT EXISTS bills (
    bill_id        INT AUTO_INCREMENT PRIMARY KEY,
    reservation_id INT NOT NULL UNIQUE,
    num_nights     INT NOT NULL,
    room_charge    DECIMAL(10,2) NOT NULL,
    tax_amount     DECIMAL(10,2) NOT NULL,
    total_amount   DECIMAL(10,2) NOT NULL,
    is_paid        TINYINT(1) DEFAULT 0,
    generated_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (reservation_id) REFERENCES reservations(reservation_id)
);

-- ============================================================
-- STORED PROCEDURE: AddReservation
-- ============================================================
DELIMITER $$

CREATE PROCEDURE AddReservation(
    IN  p_guest_name    VARCHAR(100),
    IN  p_address       TEXT,
    IN  p_contact       VARCHAR(20),
    IN  p_email         VARCHAR(100),
    IN  p_room_id       INT,
    IN  p_check_in      DATE,
    IN  p_check_out     DATE,
    IN  p_num_guests    INT,
    IN  p_special_req   TEXT,
    IN  p_created_by    INT,
    OUT p_res_number    VARCHAR(20),
    OUT p_total_amount  DECIMAL(10,2),
    OUT p_result_msg    VARCHAR(200)
)
BEGIN
    DECLARE v_guest_id      INT;
    DECLARE v_rate          DECIMAL(10,2);
    DECLARE v_num_nights    INT;
    DECLARE v_is_available  TINYINT(1);

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        SET p_result_msg = 'ERROR: Database error occurred during reservation.';
        SET p_res_number = NULL;
        SET p_total_amount = 0;
    END;

    START TRANSACTION;

    -- Check room availability
    SELECT is_available, rate_per_night
    INTO   v_is_available, v_rate
    FROM   rooms
    WHERE  room_id = p_room_id
    FOR UPDATE;

    IF v_is_available = 0 THEN
        ROLLBACK;
        SET p_result_msg  = 'ERROR: Room is not available.';
        SET p_res_number  = NULL;
        SET p_total_amount = 0;
    ELSE
        -- Insert or find guest
        INSERT INTO guests (guest_name, address, contact_number, email)
        VALUES (p_guest_name, p_address, p_contact, p_email);
        SET v_guest_id = LAST_INSERT_ID();

        -- Calculate nights & amount
        SET v_num_nights   = DATEDIFF(p_check_out, p_check_in);
        SET p_total_amount = v_num_nights * v_rate;

        -- Generate reservation number: OVR-YYYYMMDD-XXXXXX
        SET p_res_number = CONCAT('OVR-', DATE_FORMAT(NOW(), '%Y%m%d'), '-',
                                  LPAD(FLOOR(RAND() * 999999), 6, '0'));

        -- Insert reservation
        INSERT INTO reservations
            (reservation_number, guest_id, room_id, check_in_date,
             check_out_date, num_guests, total_amount, special_requests, created_by)
        VALUES
            (p_res_number, v_guest_id, p_room_id, p_check_in,
             p_check_out, p_num_guests, p_total_amount, p_special_req, p_created_by);

        -- Mark room unavailable
        UPDATE rooms SET is_available = 0 WHERE room_id = p_room_id;

        COMMIT;
        SET p_result_msg = 'SUCCESS: Reservation created successfully.';
    END IF;
END$$

-- ============================================================
-- STORED PROCEDURE: GetReservationDetails
-- ============================================================
CREATE PROCEDURE GetReservationDetails(
    IN p_reservation_number VARCHAR(20)
)
BEGIN
    SELECT
        r.reservation_number,
        g.guest_name,
        g.address,
        g.contact_number,
        g.email,
        rm.room_number,
        rm.room_type,
        rm.rate_per_night,
        r.check_in_date,
        r.check_out_date,
        DATEDIFF(r.check_out_date, r.check_in_date) AS num_nights,
        r.total_amount,
        r.num_guests,
        r.status,
        r.special_requests,
        r.created_at
    FROM reservations r
    JOIN guests  g  ON r.guest_id = g.guest_id
    JOIN rooms   rm ON r.room_id  = rm.room_id
    WHERE r.reservation_number = p_reservation_number;
END$$

-- ============================================================
-- STORED PROCEDURE: GenerateBill
-- ============================================================
CREATE PROCEDURE GenerateBill(
    IN  p_reservation_number VARCHAR(20),
    OUT p_result_msg         VARCHAR(200)
)
BEGIN
    DECLARE v_res_id       INT;
    DECLARE v_num_nights   INT;
    DECLARE v_rate         DECIMAL(10,2);
    DECLARE v_room_charge  DECIMAL(10,2);
    DECLARE v_tax          DECIMAL(10,2);
    DECLARE v_total        DECIMAL(10,2);

    SELECT r.reservation_id,
           DATEDIFF(r.check_out_date, r.check_in_date),
           rm.rate_per_night
    INTO   v_res_id, v_num_nights, v_rate
    FROM   reservations r
    JOIN   rooms rm ON r.room_id = rm.room_id
    WHERE  r.reservation_number = p_reservation_number;

    IF v_res_id IS NULL THEN
        SET p_result_msg = 'ERROR: Reservation not found.';
    ELSE
        SET v_room_charge = v_num_nights * v_rate;
        SET v_tax         = v_room_charge * 0.10;  -- 10% tax
        SET v_total       = v_room_charge + v_tax;

        INSERT INTO bills (reservation_id, num_nights, room_charge, tax_amount, total_amount)
        VALUES (v_res_id, v_num_nights, v_room_charge, v_tax, v_total)
        ON DUPLICATE KEY UPDATE
            num_nights   = v_num_nights,
            room_charge  = v_room_charge,
            tax_amount   = v_tax,
            total_amount = v_total;

        SET p_result_msg = 'SUCCESS: Bill generated successfully.';
    END IF;
END$$

-- ============================================================
-- STORED PROCEDURE: GetAllReservations
-- ============================================================
CREATE PROCEDURE GetAllReservations()
BEGIN
    SELECT
        r.reservation_number,
        g.guest_name,
        g.contact_number,
        rm.room_number,
        rm.room_type,
        r.check_in_date,
        r.check_out_date,
        r.total_amount,
        r.status
    FROM reservations r
    JOIN guests g  ON r.guest_id = g.guest_id
    JOIN rooms  rm ON r.room_id  = rm.room_id
    ORDER BY r.created_at DESC;
END$$

-- ============================================================
-- STORED PROCEDURE: CancelReservation
-- ============================================================
CREATE PROCEDURE CancelReservation(
    IN  p_reservation_number VARCHAR(20),
    OUT p_result_msg         VARCHAR(200)
)
BEGIN
    DECLARE v_room_id INT;

    SELECT room_id INTO v_room_id
    FROM   reservations
    WHERE  reservation_number = p_reservation_number
      AND  status = 'CONFIRMED';

    IF v_room_id IS NULL THEN
        SET p_result_msg = 'ERROR: Reservation not found or cannot be cancelled.';
    ELSE
        UPDATE reservations
        SET    status = 'CANCELLED'
        WHERE  reservation_number = p_reservation_number;

        UPDATE rooms SET is_available = 1 WHERE room_id = v_room_id;
        SET p_result_msg = 'SUCCESS: Reservation cancelled successfully.';
    END IF;
END$$

-- ============================================================
-- STORED PROCEDURE: AuthenticateUser
-- ============================================================
CREATE PROCEDURE AuthenticateUser(
    IN  p_username VARCHAR(50),
    IN  p_password VARCHAR(255),
    OUT p_user_id  INT,
    OUT p_role     VARCHAR(20),
    OUT p_name     VARCHAR(100)
)
BEGIN
    SELECT user_id, role, full_name
    INTO   p_user_id, p_role, p_name
    FROM   users
    WHERE  username = p_username
      AND  password = SHA2(p_password, 256);
END$$

DELIMITER ;

-- ============================================================
-- SEED DATA
-- ============================================================
-- Default admin user (password: Admin@123)
INSERT INTO users (username, password, full_name, role) VALUES
('admin', SHA2('Admin@123', 256), 'System Administrator', 'ADMIN'),
('staff1', SHA2('Staff@123', 256), 'Reception Staff', 'STAFF');

-- Rooms
INSERT INTO rooms (room_number, room_type, rate_per_night, max_guests) VALUES
('101', 'STANDARD',    5000.00, 2),
('102', 'STANDARD',    5000.00, 2),
('201', 'DELUXE',      8500.00, 3),
('202', 'DELUXE',      8500.00, 3),
('301', 'OCEAN_VIEW', 12000.00, 2),
('302', 'OCEAN_VIEW', 12000.00, 4),
('401', 'SUITE',      18000.00, 4),
('402', 'SUITE',      20000.00, 6);
