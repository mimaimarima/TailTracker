package pets.controllers;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

import pets.model.Person;
import pets.model.Physical;
import pets.model.enums.Gender;
import pets.repository.daoimpl.PersonDAOI;
import pets.repository.daoimpl.PetOwnershipDAOI;
import pets.repository.daoimpl.PhysicalDAOI;
import pets.repository.daoimpl.UserDAOI;
import pets.repository.daoimpl.UserRoleDAOI;

public class PhysicalController {
	public static Physical addPhysical(Scanner scanner, Connection con) throws SQLException {
		PhysicalDAOI phTest = new PhysicalDAOI(con);
		Physical phys = new Physical();

		while (true) {
			System.out.print("Enter SSN: ");
			String ssn = scanner.nextLine();
			Physical existingPhysical = phTest.getPhysicalBySSN(ssn);
			if (existingPhysical!=null)
			{
				System.out.println("Person exists with entered SSN. Please enter again.");
				continue;
			}
			try {
				phys.setSSN(ssn);
				break;
			} catch (IllegalArgumentException e) {
				System.out.println(e.getMessage());
			}
		}
		while (true) {
			System.out.println("Enter gender:");
			int i = 1;
			for (Gender g : Gender.values()) {
				System.out.println(i + ": " + g.name());
				i++;
			}

			if (!scanner.hasNextInt()) {
				System.out.println("Invalid entry: please enter a number.");
				scanner.nextLine();
				continue;
			}

			int input = scanner.nextInt();
			if (input < 1 || input > Gender.values().length) {
				System.out.println("Invalid entry: number must be between 1 and " + Gender.values().length + ".");
				continue;
			}

			try {
				Gender gender = Gender.values()[input - 1];
				phys.setGender(gender);
				scanner.nextLine();
				break;
			} catch (ArrayIndexOutOfBoundsException e) {
				System.out.println("Invalid gender. Please try again.");
				scanner.nextLine();
			}
		}
		return phys;
	}
	public static List<Physical> getAllPhysical(Connection con) throws SQLException {
		List<Physical> physical = null;
		PhysicalDAOI phDao = new PhysicalDAOI(con);
		physical = phDao.getAllPhysical();
		if (physical.size() >= 1) {
		int i = 1;
		for (Physical p : physical) {
			System.out.println(i + " " + p.getName() + " " + p.getSurname() + " " + p.getSsn() + " " + p.getNumber());
			i++;
		}}
		else
		{
			System.out.println("No physicals found.");
		}
	
		return physical;

	}
	public static Physical selectPhysical(Scanner scanner, List<Physical> list)
	{
		Physical ph;
		while (true) {
			
			try {
				String inputStr = scanner.nextLine();
				int input = Integer.parseInt(inputStr);
				input--;
				if (input < 1 || input > list.size() + 1) {
					System.out.println("Invalid entry. Please enter a valid number from the list");
					continue;
				}
				try {
					ph = list.get(input);
					break;
				}catch (IndexOutOfBoundsException e) {
					System.out.println("Invalid entry. Please enter a valid number from the list.");
					}
			} catch (IllegalArgumentException e) {
				System.out.println("Invalid entry. Please enter a valid number from the list");
			}
		}
		return ph;
	}
	public static void deletePhysical(Scanner scanner, Connection con) throws SQLException {

	    boolean originalAutoCommit = con.getAutoCommit();
	    con.setAutoCommit(false); 

	    try {
	
	        List<Physical> physicals = getAllPhysical(con);
	        if (physicals.isEmpty()) {
	            System.out.println("No physical persons found.");
	            return; 
	        }

	        Physical selected = selectPhysical(scanner, physicals);
	        String physicalId = selected.getId();


	        Person person = new PersonDAOI(con).getPerson(physicalId);
	        if (person == null) {
	            System.out.println("Warning: No linked person record found");

	        }

	        String userId = person != null ? person.getUserId() : null;

	   
	        System.out.printf("Delete %s (ID: %s) and ALL related data? (yes/no): ",
	                        selected.getName(), physicalId);
	        if (!scanner.nextLine().trim().equalsIgnoreCase("yes")) {
	            System.out.println("Cancelled.");
	            con.rollback();
	            return;
	        }

	        new PetOwnershipDAOI(con).deleteOwnershipByPhysicalId(physicalId);
	        

	        new PhysicalDAOI(con).deletePhysical(physicalId);
	        

	        if (person != null) {
	            new PersonDAOI(con).deletePerson(physicalId);
	            

	            if (userId != null) {
	                new UserRoleDAOI(con).deleteByUserId(userId);
	                

	                new UserDAOI(con).deleteUser(userId);
	            }
	        }

	        con.commit();
	        System.out.println("Physical person and ALL related data deleted successfully!");

	    } catch (Exception e) {
	        try {
	            con.rollback();
	            System.err.println("Transaction rolled back due to error: " + e.getMessage());
	        } catch (SQLException rbEx) {
	            e.addSuppressed(rbEx);
	            System.err.println("CRITICAL: Rollback failed! Data may be inconsistent: " + rbEx.getMessage());
	        }
	        throw e instanceof SQLException ? (SQLException)e : new SQLException(e);
	    } finally {
	        try {
	            con.setAutoCommit(originalAutoCommit);
	        } catch (SQLException e) {
	            System.err.println("Warning: Failed to restore auto-commit: " + e.getMessage());
	        }
	    }
	}
	public static void getPhysical(Connection con, String personId) throws SQLException {
		PhysicalDAOI phDao = new PhysicalDAOI(con);
		Physical p = phDao.getPhysicalById(personId);
		System.out.println(p.getSsn());		
	}
}
