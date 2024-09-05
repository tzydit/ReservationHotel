import java.sql.*;
import java.util.Random;
import java.math.BigDecimal;

public class HotelReservationSystem {
    private static final String URL = "jdbc:postgresql://localhost:5432/ReservationHotel";
    private static final String USER = "postgres";
    private static final String PASSWORD = "123";
    private static final int TOTAL_QUARTOS = 2000000;
    private static final int TOTAL_HOTEIS = 10000;
    private static final int TOTAL_CLIENTES = 500000;
    private static final int TOTAL_RESERVAS = 2000000;

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            int hotelsInserted = insertHotels(conn);
            int roomTypesInserted = insertRoomTypes(conn);
            int roomsInserted = insertRooms(conn);
            int customersInserted = insertCustomers(conn);
            int reservationsInserted = insertReservations(conn);

            System.out.println("Total de hotéis inseridos: " + hotelsInserted);
            System.out.println("Total de tipos de quarto inseridos: " + roomTypesInserted);
            System.out.println("Total de quartos inseridos: " + roomsInserted);
            System.out.println("Total de clientes inseridos: " + customersInserted);
            System.out.println("Total de reservas inseridas: " + reservationsInserted);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        long endTime = System.currentTimeMillis();
        System.out.println("Tempo total de execução: " + (endTime - startTime) + "ms");
    }

    private static int insertHotels(Connection conn) throws SQLException {
        String insertHotelSQL = "INSERT INTO Hotels (hotel_name, address, rating) VALUES (?, ?, ?)";
        int count = 0;
        try (PreparedStatement stmt = conn.prepareStatement(insertHotelSQL)) {
            for (int i = 1; i <= TOTAL_HOTEIS; i++) {
                stmt.setString(1, "Hotel " + i);
                stmt.setString(2, "Endereço " + i);
                stmt.setInt(3, new Random().nextInt(5) + 1);
                stmt.addBatch();
                if (i % 1000 == 0) {
                    stmt.executeBatch();
                }
                count++;

                if (i % 1000 == 0 || i == TOTAL_HOTEIS) {
                    double percentage = (double) count / TOTAL_HOTEIS * 100;
                    System.out.printf("Progresso da inserção de hotéis: %.2f%%\n", percentage);
                }
            }
            stmt.executeBatch();
        }
        return count;
    }

    private static int insertRoomTypes(Connection conn) throws SQLException {
        String[] roomTypes = {"Padrão", "Deluxe", "Suíte"};
        String insertRoomTypeSQL = "INSERT INTO RoomTypes (type_name) VALUES (?)";
        int count = 0;
        try (PreparedStatement stmt = conn.prepareStatement(insertRoomTypeSQL)) {
            for (String type : roomTypes) {
                stmt.setString(1, type);
                stmt.addBatch();
                count++;
            }
            stmt.executeBatch();

            double percentage = (double) count / roomTypes.length * 100;
            System.out.printf("Progresso da inserção de tipos de quarto: %.2f%%\n", percentage);
        }
        return count;
    }

    private static int insertRooms(Connection conn) throws SQLException {
        String insertRoomSQL = "INSERT INTO Rooms (hotel_id, room_type_id, room_number, price) VALUES (?, ?, ?, ?)";
        int count = 0;
        int roomsPerHotel = TOTAL_QUARTOS / TOTAL_HOTEIS;
        int extraRooms = TOTAL_QUARTOS % TOTAL_HOTEIS;
        Random random = new Random();

        try (PreparedStatement stmt = conn.prepareStatement(insertRoomSQL)) {
            for (int hotelId = 1; hotelId <= TOTAL_HOTEIS; hotelId++) {
                int numRooms = roomsPerHotel + (hotelId <= extraRooms ? 1 : 0);
                for (int roomNum = 1; roomNum <= numRooms; roomNum++) {
                    stmt.setInt(1, hotelId);
                    stmt.setInt(2, random.nextInt(3) + 1);
                    stmt.setString(3, "Room " + roomNum);
                    stmt.setBigDecimal(4, new BigDecimal(50 + random.nextInt(200)));
                    stmt.addBatch();
                    count++;

                    // Exibir progresso
                    if (count % 1000 == 0 || count == TOTAL_QUARTOS) {
                        double percentage = (double) count / TOTAL_QUARTOS * 100;
                        System.out.printf("Progresso da inserção de quartos: %.2f%%\n", percentage);
                    }
                }
                if (hotelId % 100 == 0) {
                    stmt.executeBatch();
                }
            }
            stmt.executeBatch();
        }
        return count;
    }

    private static int insertCustomers(Connection conn) throws SQLException {
        String insertCustomerSQL = "INSERT INTO Customers (first_name, last_name, phone_number) VALUES (?, ?, ?)";
        int count = 0;
        try (PreparedStatement stmt = conn.prepareStatement(insertCustomerSQL)) {
            for (int i = 1; i <= TOTAL_CLIENTES; i++) {
                stmt.setString(1, "FirstName" + i);
                stmt.setString(2, "LastName" + i);
                stmt.setString(3, "1234567890");
                stmt.addBatch();
                count++;

                if (i % 1000 == 0 || i == TOTAL_CLIENTES) {
                    double percentage = (double) count / TOTAL_CLIENTES * 100;
                    System.out.printf("Progresso da inserção de clientes: %.2f%%\n", percentage);
                }

                if (i % 1000 == 0) {
                    stmt.executeBatch();
                }
            }
            stmt.executeBatch();
        }
        return count;
    }

    private static int insertReservations(Connection conn) throws SQLException {
        String selectRoomSQL = "SELECT hotel_id FROM Rooms WHERE room_id = ?";
        String insertReservationSQL = "INSERT INTO Reservations (customer_id, room_id, check_in_date, check_out_date) VALUES (?, ?, ?, ?)";
        int count = 0;

        try (PreparedStatement selectRoomStmt = conn.prepareStatement(selectRoomSQL);
             PreparedStatement insertReservationStmt = conn.prepareStatement(insertReservationSQL)) {

            Random random = new Random();

            for (int i = 1; i <= TOTAL_RESERVAS; i++) {
                int roomId = random.nextInt(TOTAL_QUARTOS) + 1; // Quarto aleatório
                selectRoomStmt.setInt(1, roomId);
                ResultSet rs = selectRoomStmt.executeQuery();

                if (rs.next()) {
                    int hotelId = rs.getInt("hotel_id");

                    insertReservationStmt.setInt(1, random.nextInt(TOTAL_CLIENTES) + 1); // Cliente aleatório
                    insertReservationStmt.setInt(2, roomId); // Quarto aleatório
                    insertReservationStmt.setDate(3, Date.valueOf("2024-01-01"));
                    insertReservationStmt.setDate(4, Date.valueOf("2024-01-05"));
                    insertReservationStmt.addBatch();
                    count++;
                }

                if (i % 1000 == 0 || i == TOTAL_RESERVAS) {
                    double percentage = (double) count / TOTAL_RESERVAS * 100;
                    System.out.printf("Progresso da inserção de reservas: %.2f%%\n", percentage);
                }

                if (i % 1000 == 0) {
                    insertReservationStmt.executeBatch();
                }
            }
            insertReservationStmt.executeBatch();
        }
        return count;
    }
}
