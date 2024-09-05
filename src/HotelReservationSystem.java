import java.sql.*;
import java.util.Random;
import java.math.BigDecimal;

public class HotelReservationSystem {
    private static final String URL = "jdbc:postgresql://localhost:5432/ReservationHotel";
    private static final String USER = "postgres";
    private static final String PASSWORD = "123";

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            insertHotels(conn);
            insertRoomTypes(conn);
            insertRooms(conn);
            insertCustomers(conn);
            insertReservations(conn);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        long endTime = System.currentTimeMillis();
        System.out.println("Tempo total de execução: " + (endTime - startTime) + "ms");
    }

    private static void insertHotels(Connection conn) throws SQLException {
        String insertHotelSQL = "INSERT INTO Hotels (hotel_name, address, rating) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(insertHotelSQL)) {
            for (int i = 1; i <= 10000; i++) {
                stmt.setString(1, "Hotel " + i);
                stmt.setString(2, "Endereço " + i);
                stmt.setInt(3, new Random().nextInt(5) + 1);
                stmt.addBatch();
                if (i % 1000 == 0) {
                    stmt.executeBatch();
                }
            }
            stmt.executeBatch();
        }
    }

    private static void insertRoomTypes(Connection conn) throws SQLException {
        String[] roomTypes = {"Padrão", "Deluxe", "Suíte"};
        String insertRoomTypeSQL = "INSERT INTO RoomTypes (type_name) VALUES (?)";
        try (PreparedStatement stmt = conn.prepareStatement(insertRoomTypeSQL)) {
            for (String type : roomTypes) {
                stmt.setString(1, type);
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    private static void insertRooms(Connection conn) throws SQLException {
        String insertRoomSQL = "INSERT INTO Rooms (hotel_id, room_type_id, room_number, price) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(insertRoomSQL)) {
            Random random = new Random();
            for (int hotelId = 1; hotelId <= 10000; hotelId++) {
                for (int roomNum = 1; roomNum <= random.nextInt(100) + 1; roomNum++) { // Máximo de 100 quartos por hotel
                    stmt.setInt(1, hotelId);
                    stmt.setInt(2, random.nextInt(3) + 1);  // Tipo de quarto
                    stmt.setString(3, "Room " + roomNum);
                    stmt.setBigDecimal(4, new BigDecimal(50 + random.nextInt(200)));
                    stmt.addBatch();
                }
                if (hotelId % 100 == 0) {
                    stmt.executeBatch();
                }
            }
            stmt.executeBatch();
        }
    }

    private static void insertCustomers(Connection conn) throws SQLException {
        String insertCustomerSQL = "INSERT INTO Customers (first_name, last_name, phone_number) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(insertCustomerSQL)) {
            Random random = new Random();
            for (int i = 1; i <= 500000; i++) {
                stmt.setString(1, "FirstName" + i);
                stmt.setString(2, "LastName" + i);
                stmt.setString(3, "1234567890");
                stmt.addBatch();
                if (i % 1000 == 0) {
                    try {
                        stmt.executeBatch();
                    } catch (BatchUpdateException e) {
                        e.printStackTrace();
                        // Lida com exceções de lote se necessário
                    }
                }
            }
            try {
                stmt.executeBatch();
            } catch (BatchUpdateException e) {
                e.printStackTrace();
                // Lida com exceções de lote se necessário
            }
        }
    }

    private static void insertReservations(Connection conn) throws SQLException {
        String selectRoomSQL = "SELECT hotel_id FROM Rooms WHERE room_id = ?";
        String insertReservationSQL = "INSERT INTO Reservations (customer_id, room_id, check_in_date, check_out_date) VALUES (?, ?, ?, ?)";

        try (PreparedStatement selectRoomStmt = conn.prepareStatement(selectRoomSQL);
             PreparedStatement insertReservationStmt = conn.prepareStatement(insertReservationSQL)) {

            Random random = new Random();

            for (int i = 1; i <= 2000000; i++) {
                int roomId = random.nextInt(1000000) + 1; // Quarto aleatório
                selectRoomStmt.setInt(1, roomId);
                ResultSet rs = selectRoomStmt.executeQuery();

                if (rs.next()) {
                    int hotelId = rs.getInt("hotel_id");

                    insertReservationStmt.setInt(1, random.nextInt(500000) + 1); // Cliente aleatório
                    insertReservationStmt.setInt(2, roomId); // Quarto aleatório
                    insertReservationStmt.setDate(3, Date.valueOf("2024-01-01"));
                    insertReservationStmt.setDate(4, Date.valueOf("2024-01-05"));
                    insertReservationStmt.addBatch();
                }

                if (i % 1000 == 0) {
                    try {
                        insertReservationStmt.executeBatch();
                    } catch (BatchUpdateException e) {
                        e.printStackTrace();
                        // Lida com exceções de lote se necessário
                    }
                }
            }
            try {
                insertReservationStmt.executeBatch();
            } catch (BatchUpdateException e) {
                e.printStackTrace();
                // Lida com exceções de lote se necessário
            }
        }
    }
}
