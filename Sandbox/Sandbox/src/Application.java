import nl.saxion.app.SaxionApp;

import java.awt.*;
import java.sql.SQLException;

public class Application implements Runnable {

    public static void main(String[] args) {
        SaxionApp.start(new Application(), 800, 800);
    }

    public void run() {
        boolean exit = false;

        try {
            accessSystem();
            while (!exit) {
                SaxionApp.printLine("---Library Menu---" + "\n");
                SaxionApp.printLine("1.Show available books");
                SaxionApp.printLine("2.Borrow a book");
                SaxionApp.printLine("3.Show my loans");
                SaxionApp.printLine("4.Return a book");
                SaxionApp.printLine("5.Show my points");
                SaxionApp.printLine("0.Exit");
                SaxionApp.print("Choose function: ");
                int userChoice = SaxionApp.readInt();
                SaxionApp.printLine();

                switch (userChoice) {
                    case 1:
                        SaxionApp.clear();
                        SQL_Queries.showBooks();
                        break;

                    case 2:
                        SaxionApp.printLine("Available books: ");
                        SQL_Queries.showBooks();
                        if (!SQL_Queries.isBooksEmpty()) {
                            SaxionApp.print("Select one: ");
                            SQL_Queries.borrowBook(SaxionApp.readString());
                        }
                        break;

                    case 3:
                        SaxionApp.printLine("You didn't return these books:");
                        SQL_Queries.showLoans();
                        break;

                    case 4:
                        SaxionApp.print("Which book do you want to return? ");
                        SQL_Queries.removeLoan(SaxionApp.readString());
                        break;

                    case 5:
                        SaxionApp.printLine("You have " + SQL_Queries.getUserPoints() + " points" );
                        break;

                    case 0:
                        exit = true;
                        break;

                    default:
                        SaxionApp.printLine("Wrong input!", Color.RED);
                }
                SaxionApp.pause();
                SaxionApp.clear();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void accessSystem() throws SQLException {
        boolean loggedIn = false;
        boolean newUser = false;

        SaxionApp.printLine("Do you want to login or register? (l/r)");
        while (!loggedIn) {
            char entranceChoice = SaxionApp.readChar();

            if (entranceChoice == 'l' || entranceChoice == 'L' || newUser) {
                SaxionApp.clear();
                SaxionApp.printLine("Sign in");

                while (!LoginSystem.signIn()) {
                    SaxionApp.pause();
                    SaxionApp.clear();
                    SaxionApp.printLine("Login");
                }

                SaxionApp.pause();
                SaxionApp.clear();
                loggedIn = true;

            } else if (entranceChoice == 'r' || entranceChoice == 'R') {
                LoginSystem.createAccount();
                newUser = true;

            } else {
                SaxionApp.printLine("Wrong input!");
                SaxionApp.pause();
                SaxionApp.clear();
                SaxionApp.printLine("Try again! (l/r)");
            }
        }

    }
}
