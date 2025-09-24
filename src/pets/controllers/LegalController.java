package pets.controllers;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import pets.model.Legal;
import pets.model.Person;
import pets.model.enums.LegalSize;
import pets.model.enums.LegalType;
import pets.repository.daoimpl.LegalDAOI;
import pets.repository.daoimpl.PersonDAOI;

public class LegalController {
	public static Legal collectLegalData(Scanner scanner, Connection con, Legal leg, boolean allowSkipping) throws SQLException {
		LegalDAOI legDao = new LegalDAOI(con);
		
		
	    while (true) {
	        System.out.print("Enter Tax Number" + (allowSkipping ? " or \\ to skip" : "") + ": ");
	        String tin = scanner.nextLine();
	        Legal existingLeg = legDao.getLegalByTin(tin);
            if (existingLeg!=null)
            {
            	System.out.println("Legal Person with that TIN already exists. Please enter again.");
            	continue;
            }
	        if (allowSkipping && tin.equals("\\")) break;
	        try {
	            leg.setTin(tin);
	            break;
	        } catch (IllegalArgumentException e) {
	            System.out.println(e.getMessage());
	        }
	    }

	    while (true) {
	        System.out.print("Enter Licence ID" + (allowSkipping ? " or \\ to skip" : "") + ": ");
	        String licenceId = scanner.nextLine();
	        if (allowSkipping && licenceId.equals("\\")) break;
	        try {
	            leg.setLicenceId(licenceId);
	            break;
	        } catch (IllegalArgumentException e) {
	            System.out.println(e.getMessage());
	        }
	    }

	    while (true) {
	        System.out.print("Enter Daily Work Hours (e.g. 9-17, Mon-Thur)" + (allowSkipping ? " or \\ to skip" : "") + ": ");
	        String dailyWorkHours = scanner.nextLine();
	        if (allowSkipping && dailyWorkHours.equals("\\")) break;
	        try {
	            leg.setDailyWorkHours(dailyWorkHours);
	            break;
	        } catch (IllegalArgumentException e) {
	            System.out.println(e.getMessage());
	        }
	    }

	    List<LegalSize> sizes = Arrays.asList(LegalSize.values());
	    for (int i = 0; i < sizes.size(); i++) {
	        System.out.println((i + 1) + ": " + sizes.get(i).name());
	    }

	    while (true) {
	        System.out.print("Enter Size" + (allowSkipping ? " or \\ to skip" : "") + ": ");
	        String inputStr = scanner.nextLine();
	        if (allowSkipping && inputStr.equals("\\")) break;

	        try {
	            int input = Integer.parseInt(inputStr);
	            if (input < 1 || input > sizes.size()) {
	                System.out.println("Invalid entry. Please enter a number between 1 and " + sizes.size());
	                continue;
	            }
	            leg.setSize(sizes.get(input - 1));
	            break;
	        } catch (NumberFormatException e) {
	            System.out.println("Please enter a valid number.");
	        } catch (IllegalArgumentException e) {
	            System.out.println(e.getMessage());
	        }
	    }

	    return leg;
	}

	public static Legal addLegal(Scanner scanner, Connection con) throws SQLException {

		Legal leg = new Legal();
	    return collectLegalData(scanner, con, leg, false);
	}

	public static void updateLegal(Scanner scanner, Connection con, Person person) throws SQLException {
	    LegalDAOI legDao = new LegalDAOI(con);
	    Legal leg = legDao.getLegal(person.getId());
	    if (leg == null) {
	        System.out.println("No legal data found for selected person.");
	        return;
	    }

	    collectLegalData(scanner, con, leg, true);
	    leg.setCreatedBy(person.getCreatedBy());
	    legDao.updateLegal(leg);
	    return;
	}
	public static List<Legal> listAllLegal(Connection con, LegalType filter) throws SQLException{
		LegalDAOI legDao = new LegalDAOI(con);
		List<Legal> all = legDao.getAllLegals(filter);
		
		int i = 1;
		if (all.size() >= 1) {
	
		for (Legal leg : all) {
			System.out.println(i + " " + leg.getName() + " " + leg.getTin() + " " + leg.getType() + " " + leg.getSize());
			i++;
		}}
		else 
		{
			System.out.println("No legal persons found.");
			return null;
		}
		
		return all; 
	}
	private static LegalType selectLegalType(Scanner scanner) {
	    while (true) {
	        System.out.println("Select legal person type to delete:");
	        System.out.println("1: Vet Station");
	        System.out.println("2: Care Center");
	        System.out.println("\\: Cancel");
	        
	        String input = scanner.nextLine().trim();
	        
	        if (input.equals("\\")) return null;
	        
	        try {
	            int choice = Integer.parseInt(input);
	            switch (choice) {
	                case 1: return LegalType.VET_STATION;
	                case 2: return LegalType.CARE_CENTER;
	                default: 
	                    System.out.println("Please enter 1 or 2");
	            }
	        } catch (NumberFormatException e) {
	            System.out.println("Please enter a valid number");
	        }
	    }
	}
	private static Legal selectLegalEntity(Scanner scanner, List<Legal> legalEntities) {
	    System.out.println("\nAvailable Legal Entities:");
	    for (int i = 0; i < legalEntities.size(); i++) {
	        Legal leg = legalEntities.get(i);
	        System.out.printf("%d: %s (ID: %s)%n", i+1, leg.getName(), leg.getId());
	    }
	    
	    while (true) {
	        System.out.println("\nEnter number to select or \\ to cancel:");
	        String input = scanner.nextLine().trim();
	        
	        if (input.equals("\\")) return null;
	        
	        try {
	            int choice = Integer.parseInt(input);
	            if (choice >= 1 && choice <= legalEntities.size()) {
	                return legalEntities.get(choice - 1);
	            }
	            System.out.println("Please enter a number between 1 and " + legalEntities.size());
	        } catch (NumberFormatException e) {
	            System.out.println("Please enter a valid number");
	        }
	    }
	}public static void deleteLegal(Scanner scanner, Connection con) throws SQLException {

	    boolean originalAutoCommit = con.getAutoCommit();
	    con.setAutoCommit(false); 

	    try {

	        LegalType type = selectLegalType(scanner);
	        if (type == null) {
	            con.rollback();
	            return;
	        }

	   
	        Legal legal = selectLegalEntity(scanner, listAllLegal(con, type));
	        if (legal == null) {
	            con.rollback();
	            return;
	        }


	        if (type == LegalType.CARE_CENTER) {
	            CareCenterController.deleteCareCenter(con, legal.getId());
	        } else if (type == LegalType.VET_STATION) {
	            VetStationController.deleteVetStation(con, legal.getId());
	        }

	        new LegalDAOI(con).deleteLegal(legal.getId());
	        new PersonDAOI(con).deletePerson(legal.getId());

	        con.commit();
	        System.out.println("Successfully deleted legal entity!");

	    } catch (SQLException e) {
	        con.rollback(); 
	        System.err.println("Failed to delete: " + e.getMessage());
	        throw e;
	    } finally {
	        con.setAutoCommit(originalAutoCommit); 
	    }
	}
	public static void deleteLegalData(Connection con, String legalId) throws SQLException {
	    LegalType type = new LegalDAOI(con).getLegal(legalId).getType();
	    if (type == LegalType.VET_STATION) {
	        VetStationController.deleteVetStation(con, legalId); 
	    } 
	    else if (type == LegalType.CARE_CENTER) {
	        CareCenterController.deleteCareCenter(con, legalId); 
	    }
	}

	public static void getLegal(Connection con, String personId) throws SQLException {
		LegalDAOI legDao = new LegalDAOI(con);
		Legal leg = legDao.getLegal(personId);
	
			System.out.println(leg.getTin() + " " + leg.getType() + " " + leg.getSize());
	}
	
}
