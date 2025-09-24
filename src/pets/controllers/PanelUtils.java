package pets.controllers;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Scanner;

import pets.model.Sesion;
import pets.repository.daoimpl.SesionDAOI;

public class PanelUtils {

	public static void showPanel(Scanner scanner, Connection con, Sesion ses, String title, Map<String, PanelAction> actions) throws SQLException {
	    while (true) {
	
	        System.out.println(title);
	        actions.keySet().forEach(code -> {
	        	String prefix = (code.length() == 2) ? "  " : " ";
	        	System.out.println(prefix + code + " | " + getDescription(code));
	        });
	        System.out.println("  LO | Log Out");
	        String input = scanner.nextLine().trim().toUpperCase();
	        if ("LO".equals(input)) {
	        	ses.setEnd(LocalDateTime.now());
	        	SesionDAOI sesDao = new SesionDAOI(con);
	        	sesDao.insert(ses);
	        	ConsoleApp.main(null);
	        	return;
	        }
	
	        PanelAction action = actions.get(input);
	        if (action != null) {
	            action.run(scanner, con, ses.getUserId());
	        } else {
	            System.out.println("Invalid input. Please try again.");
	        }
	    }
	}
	private static String getDescription(String code) {
		    switch (code) {
		        case "RP": return "Register Pet";
		        case "UP": return "Update Pet";
		        case "DP": return "Delete Pet";
		        case "VP": return "View Pets";
		        case "RA": return "Register Activity";
		        case "UA": return "Update Activity";
		        case "DA": return "Delete Activity";
		        case "RE": return "Register Event";
		        case "UE": return "Update Event";
		        case "DE": return "Delete Event";
		        case "CA": return "List Activities By Care Center";
		        case "PA": return "List Activities By Pet";
		        case "AP": return "Create Person";
		        case "VPI": return "View Persons";
		        case "UPI": return "Update Personal Information";
		        case "VMP": return "View Personal Information";
		        case "UMP": return "Update My Personal Information";
		        case "DPI": return "Delete Person";
		        case "ELI": return "Update User Account";
		        case "RUA": return "Register User";
		        case "VUA": return "View Users";
		        case "VUR": return "View User";
		        case "UUA": return "Update User";
		        case "DUA": return "Delete User";
		        case "UU": return "Update My User Account";
		        case "DU": return "Delete My User Account";
		        case "UCF": return "Update Care Center Facilities";
		        case "DCF": return "Delete Care Center Facilities";
		        case "UEA": return "Update Vet Station's Emergency Availability";
		        case "UVS": return "Update Vet Station Species";
		        case "UVL": return "Update Vet Station Specialties";
		        case "DVS": return "Delete Vet Station Species";
		        case "DVL": return "Delete Vet Station Specialties";
		        case "RHE": return "Register Health Event";
		        case "UHE": return "Update Health Event";
		        case "DHE": return "Delete Health Event";
		        case "VEH": return "Verify Health Events";
		        case "VHE": return "View Health Events";
		        case "VHP": return "View Health Events By Pet";
		        case "REP": return "Register Event Participation";
		        case "VEP": return "View Event Participation";
		        case "UEP": return "Update Event Participation";
		        case "DEP": return "Delete Event Participation";
		        case "RPP": return "Register Pet Passport";
		        case "UPP": return "Update Pet Passport";
		        case "DPP": return "Delete Pet Passport";
		        case "PPP": return "Print Pet Passport";
		        case "REV": return "Register Event";
		        case "UEV": return "Update Event";
		        case "DEV": return "Delete Event";
		        case "VEV": return "View Events";
		        case "AUR": return "Add User Role";
		        case "DUR": return "Delete User Role";
		        case "SUR": return "Switch User Role";
		        case "LO": return "Log Out";
		        default:   return "Unknown";
		    }
		}

}
