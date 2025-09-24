package pets.controllers;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Scanner;
import pets.config.DatabaseConnection;
import pets.model.Sesion;

public class ConsoleApp {
	public static void main(String[] args) throws SQLException {
		Scanner scanner = new Scanner(System.in);
		Connection con = DatabaseConnection.getConnection();
		while (true) {
			System.out.println("Welcome! To register, type 'R'. To login, type 'L'. To quit, type 'Q':");
			String input = scanner.nextLine().trim().toUpperCase();

			switch (input) {
			case "R":
				Sesion ses = new Sesion();
				ses.setStart(LocalDateTime.now());
				UserController.addUser(scanner, con, false, 0, ses);
				ses.setEnd(LocalDateTime.now());
				break;
			case "L":
				PanelController.loginUser(scanner);
				break;
			case "Q":
				System.exit(-1);
			default:
				System.out.println("Invalid input. Please try again.");
			}

		}
	}
}
