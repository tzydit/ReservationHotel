CREATE TABLE Hotels (
    hotel_id SERIAL PRIMARY KEY,
    hotel_name VARCHAR(255) NOT NULL,
    address TEXT NOT NULL,
    rating INT CHECK (rating >= 1 AND rating <= 5)
);

CREATE TABLE RoomTypes (
    room_type_id SERIAL PRIMARY KEY,
    type_name VARCHAR(50) NOT NULL
);

CREATE TABLE Rooms (
    room_id SERIAL PRIMARY KEY,
    hotel_id INT REFERENCES Hotels(hotel_id),
    room_type_id INT REFERENCES RoomTypes(room_type_id),
    room_number VARCHAR(10) NOT NULL,
    price DECIMAL(10,2) NOT NULL
);

CREATE TABLE Customers (
    customer_id SERIAL PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    phone_number VARCHAR(15)
);

CREATE TABLE Reservations (
    reservation_id SERIAL PRIMARY KEY,
    customer_id INT REFERENCES Customers(customer_id),
    room_id INT REFERENCES Rooms(room_id),
    check_in_date DATE NOT NULL,
    check_out_date DATE NOT NULL
	
);

CREATE OR REPLACE PROCEDURE CheckRoomAvailability(
    IN p_hotel_id INT,
    IN p_check_in_date DATE,
    IN p_check_out_date DATE
)
LANGUAGE plpgsql
AS $$
DECLARE
    v_start_time TIMESTAMP;
    v_end_time TIMESTAMP;
    rec RECORD;
    v_duration INTERVAL;
BEGIN
    v_start_time := clock_timestamp();

    IF EXISTS (SELECT 1 FROM pg_tables WHERE tablename = 'temp_room_availability') THEN
        DROP TABLE temp_room_availability;
    END IF;

    CREATE TEMP TABLE temp_room_availability AS
    SELECT r.room_id, r.room_number, r.price, r.hotel_id
    FROM Rooms r
    LEFT JOIN Reservations res ON r.room_id = res.room_id
        AND (res.check_in_date < p_check_out_date AND res.check_out_date > p_check_in_date)
    WHERE r.hotel_id = p_hotel_id
    AND res.room_id IS NULL;

    FOR rec IN
        SELECT * FROM temp_room_availability
    LOOP
        RAISE NOTICE 'Hotel ID: %, Room ID: %, Room Number: %, Price: %',
            rec.hotel_id, rec.room_id, rec.room_number, rec.price;
    END LOOP;

    v_end_time := clock_timestamp();
    v_duration := v_end_time - v_start_time;

    RAISE NOTICE 'Tempo de execução: %.3f ms', EXTRACT(EPOCH FROM v_duration) * 1000;
END;
$$;

CALL CheckRoomAvailability(2, '2024-01-01', '2024-01-05');

CREATE OR REPLACE PROCEDURE CreateReservation(
    IN p_customer_id INT,
    IN p_room_id INT,
    IN p_check_in_date DATE,
    IN p_check_out_date DATE
)
LANGUAGE plpgsql
AS $$
DECLARE
    v_start_time TIMESTAMP;
    v_end_time TIMESTAMP;
    v_duration INTERVAL;
BEGIN
    v_start_time := clock_timestamp();

    INSERT INTO Reservations (customer_id, room_id, check_in_date, check_out_date)
    VALUES (p_customer_id, p_room_id, p_check_in_date, p_check_out_date);

    v_end_time := clock_timestamp();
    v_duration := v_end_time - v_start_time;

    RAISE NOTICE 'Tempo de execução: %.3f ms', EXTRACT(EPOCH FROM v_duration) * 1000;
END;
$$;

CALL CreateReservation(2, 18, '2024-01-01', '2024-01-05');

CALL CheckRoomAvailability(2, '2024-01-01', '2024-01-05');


CREATE OR REPLACE PROCEDURE CancelReservation(
    IN p_hotel_id INT,
    IN p_room_id INT
)
LANGUAGE plpgsql
AS $$
DECLARE
    v_start_time TIMESTAMP;
    v_end_time TIMESTAMP;
    v_duration INTERVAL;
BEGIN
    v_start_time := clock_timestamp();

    DELETE FROM Reservations
    WHERE room_id = p_room_id
      AND room_id IN (SELECT room_id FROM Rooms WHERE hotel_id = p_hotel_id);

    v_end_time := clock_timestamp();
    v_duration := v_end_time - v_start_time;

    RAISE NOTICE 'Tempo de execução: %.3f ms', EXTRACT(EPOCH FROM v_duration) * 1000;
END;
$$;



CALL CancelReservation(2, 18);

CALL CheckRoomAvailability(2, '2024-01-01', '2024-01-05');



