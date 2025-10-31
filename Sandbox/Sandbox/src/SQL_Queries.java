import nl.saxion.app.SaxionApp;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;

public class SQL_Queries {

    private static Connection accessDB() {    //task can be read(0) and write(1)
        try {
            //download driver MySQL
            String url = "jdbc:mysql://localhost:3306/sql_library";
            String username = "root";
            String password = "natahaserg";


            return DriverManager.getConnection(url, username, password); //creates connection with database
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    public static void showBooks() throws SQLException {
        String query = "SELECT name, book_genre FROM books WHERE available = true;";
        try (Connection con = accessDB();
             Statement statement = con.createStatement();
             ResultSet rs = statement.executeQuery(query)) {

            boolean any = false;

            while (rs.next()) {
                any = true;
                SaxionApp.printLine("Book name is " + rs.getString("name") + "     Genre is " + rs.getString("book_genre"));
            }
            if (!any) {
                SaxionApp.printLine("No books available now!");
            }
        }
    }

    public static void borrowBook(String bookName) throws SQLException {
        String checkIfAvailable = "SELECT name, book_genre FROM books WHERE available = true AND name = ?;";
        String query = "UPDATE books SET available = false WHERE name = ?;";

        try (Connection con = accessDB();
             PreparedStatement psCheck = con.prepareStatement(checkIfAvailable)) {

            psCheck.setString(1, bookName);
            try (ResultSet rs = psCheck.executeQuery()) {

                if (rs.next()) {
                    try (PreparedStatement preparedStatement = con.prepareStatement(query)) {
                        preparedStatement.setString(1, bookName);
                        preparedStatement.executeUpdate();
                    }

                    addLoan(bookName);
                    addPoints();
                    SaxionApp.printLine("Successfully!");
                    SaxionApp.printLine("+100 points gained!");
                } else {
                    SaxionApp.printLine("No such book!");
                }
            }
        }
    }

    public static boolean checkMail(String mail) throws SQLException {
        String query = "SELECT * FROM customers where mail = ?;";
        try (Connection con = accessDB();
             PreparedStatement preparedStatement = con.prepareStatement(query)) {

            preparedStatement.setString(1, mail);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                return rs.next();
            }
        }
    }


    public static boolean checkPassword(String password, String mail) throws SQLException {
        String query = "SELECT first_name FROM customers WHERE mail = ? AND password = ?;";

        try (Connection con = accessDB();
             PreparedStatement preparedStatement = con.prepareStatement(query)) {

            preparedStatement.setString(1, mail);
            preparedStatement.setString(2, password);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                if (rs.next()) {
                    SaxionApp.printLine("Welcome " + rs.getString("first_name") + "!");
                    return true;
                } else {
                    return false;
                }
            }
        }
    }

    public static void addUser(String firstName, String lastName, String mail, String password) throws SQLException {
        String query = "INSERT INTO customers (first_Name, last_Name, points, password, mail) values(?, ?, default, ?, ?);";

        Connection con = accessDB();
        PreparedStatement preparedStatement = null;

        if (lastName.isEmpty()) {
            query = "INSERT INTO customers (first_Name, last_Name, points, password, mail) values(?, default, default, ?, ?);";

            preparedStatement = con.prepareStatement(query);
            preparedStatement.setString(1, firstName);
            preparedStatement.setString(2, password);
            preparedStatement.setString(3, mail);

        } else {
            preparedStatement = con.prepareStatement(query);
            preparedStatement.setString(1, firstName);
            preparedStatement.setString(2, lastName);
            preparedStatement.setString(3, password);
            preparedStatement.setString(4, mail);
        }

        preparedStatement.executeUpdate();

        preparedStatement.close();
        con.close();
    }

    private static void addLoan(String bookName) throws SQLException {
        String query = "INSERT INTO borrowed_books (customer_id, first_name, last_name, borrowed_book) " +
                "SELECT c.id, c.first_name, c.last_name, b.name " +
                "FROM customers c, books b " +
                "WHERE c.mail = ? AND b.name = ?;";

        try (Connection con = accessDB();
             PreparedStatement preparedStatement = con.prepareStatement(query)) {

            preparedStatement.setString(1, LoginSystem.getMail());
            preparedStatement.setString(2, bookName);
            preparedStatement.executeUpdate();
        }
    }

    private static void addPoints() throws SQLException {
        String query = "UPDATE customers SET points = points + 100 " +
                "WHERE mail = ?;";

        try (Connection con = accessDB();
             PreparedStatement preparedStatement = con.prepareStatement(query)) {

            preparedStatement.setString(1, LoginSystem.getMail());
            preparedStatement.executeUpdate();
        }
    }

    public static void removeLoan(String bookToReturn) throws SQLException {
        if (checkLoan(bookToReturn)) {
            String query = "DELETE bb FROM borrowed_books bb " +
                    "JOIN customers c ON bb.customer_id = c.id " +
                    "WHERE bb.borrowed_book = ? AND c.mail = ?;";
            String availabilityQuery = "UPDATE books SET available = true WHERE name = ?";

            try (Connection con = accessDB();
                 PreparedStatement preparedStatement = con.prepareStatement(query);
                 PreparedStatement preparedStatement2 = con.prepareStatement(availabilityQuery)) {

                preparedStatement.setString(1, bookToReturn);
                preparedStatement.setString(2, LoginSystem.getMail());
                preparedStatement.executeUpdate();


                preparedStatement2.setString(1, bookToReturn);
                preparedStatement2.executeUpdate();
            }

            SaxionApp.printLine("Successfully!");
        } else {
            SaxionApp.printLine("You didn't borrow this book!");
        }
    }

    public static void showLoans() throws SQLException {
        String query = "SELECT borrowed_book FROM borrowed_books bb " +
                "JOIN customers c ON bb.customer_id = c.id WHERE c.mail = ?";

        try (Connection con = accessDB();
             PreparedStatement preparedStatement = con.prepareStatement(query)) {

            preparedStatement.setString(1, LoginSystem.getMail());
            try (ResultSet rs = preparedStatement.executeQuery()) {

                while (rs.next()) {
                    SaxionApp.printLine(rs.getString("borrowed_book"));
                }
            }
        }
    }

    private static boolean checkLoan(String bookToReturn) throws SQLException {
        String query = "SELECT borrowed_book FROM borrowed_books bb " +
                "JOIN customers c ON bb.customer_id = c.id WHERE c.mail = ? and bb.borrowed_book = ?";

        try (Connection con = accessDB();
             PreparedStatement preparedStatement = con.prepareStatement(query)) {

            preparedStatement.setString(1, LoginSystem.getMail());
            preparedStatement.setString(2, bookToReturn);
            try (ResultSet rs = preparedStatement.executeQuery()) {

                return rs.next();
            }
        }
    }

    public static boolean isBooksEmpty() throws SQLException {
        String query = "SELECT 1 FROM books WHERE available = true LIMIT 1;";

        try (Connection con = accessDB();
             Statement statement = con.createStatement();
             ResultSet rs = statement.executeQuery(query)) {

            return !rs.next();
        }
    }

    public static int getUserPoints() throws SQLException {
        String query = "SELECT points FROM customers WHERE mail = ?";

        try (Connection con = accessDB();
             PreparedStatement preparedStatement = con.prepareStatement(query)) {

            preparedStatement.setString(1, LoginSystem.getMail());
            ResultSet rs = preparedStatement.executeQuery();


            if (rs.next()) {
                return rs.getInt("points");
            } else {
                return 0;
            }
        }
    }
}
