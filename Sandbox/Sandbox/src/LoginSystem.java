import nl.saxion.app.SaxionApp;

import java.sql.SQLException;

public class LoginSystem {

    private static String mail = "";

    public static boolean signIn() throws SQLException {
        SaxionApp.print("Your mail: ");
        mail = SaxionApp.readString();
        boolean correctMail = SQL_Queries.checkMail(mail);
        SaxionApp.print("Your password: ");
        boolean correctPassword = SQL_Queries.checkPassword(SaxionApp.readString(), mail);

        if (correctMail && correctPassword) {
            return true;
        } else {
            SaxionApp.printLine("Wrong password or mail!");
            return false;
        }
    }

    public static void createAccount() throws SQLException {
        SaxionApp.print("First name: ");
        String firstName = getValue(0);
        SaxionApp.print("Last name (optional): ");
        String lastName = SaxionApp.readString();
        SaxionApp.clear();

        SaxionApp.print("Mail: ");
        String mail = getValue(1);
        boolean mailExist = SQL_Queries.checkMail(mail);
        while (mailExist) {
            SaxionApp.print("This mail is already used! \nTry another one: ");
            mail = getValue(1);
            mailExist = SQL_Queries.checkMail(mail);
        }

        SaxionApp.print("Password: ");
        String password = getValue(2);

        SQL_Queries.addUser(firstName, lastName, mail, password);
        SaxionApp.printLine("Account created successfully!");
        SaxionApp.printLine("Press ENTER to continue...");
    }

    private static String getValue(int type) {   //0 for name, 1 for mail, 2 for password
        String value = SaxionApp.readString();
        while (value.isEmpty()) {
            switch (type) {
                case 0:
                    SaxionApp.print("Missing name! \nEnter your name:");
                    break;
                case 1:
                    SaxionApp.print("Missing mail! \nEnter your mail:");
                    break;
                case 2:
                    SaxionApp.print("Missing password! \nEnter password:");
                    break;
            }
            value = SaxionApp.readString();

        }
        return value;
    }

    public static String getMail(){
        return mail;
    }
}
