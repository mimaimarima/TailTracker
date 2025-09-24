package pets.controllers;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Scanner;

@FunctionalInterface
public interface PanelAction {
    void run(Scanner scanner, Connection con, String userId) throws SQLException;
}
