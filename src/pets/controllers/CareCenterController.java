package pets.controllers;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import pets.model.CareCenter;
import pets.model.Legal;
import pets.model.Sesion;
import pets.model.enums.Facilities;
import pets.model.enums.LegalType;
import pets.repository.daoimpl.CareCenterDAOI;
import pets.repository.daoimpl.LegalDAOI;

public class CareCenterController {
	public static CareCenter addCareCenter(Scanner scanner) throws SQLException {
		CareCenter cc = new CareCenter();

		int i = 1;
		System.out.println("Possible facilities:");
		List<Facilities> list = new ArrayList<>();
		for (Facilities f : Facilities.values()) {
			list.add(f);
			System.out.println(i + ": " + f.name());
			i++;

		}
		System.out.println("5: SELECT ALL");
		System.out.print("Enter care center facilities or 5 for selecting all:");
		while (true) {
			String input = scanner.nextLine();
			Set<Facilities> f = new HashSet<>();
			boolean validInput = true;
			if (input.equals("5")) {
				f.addAll(list);
				cc.setFacilities(f);
				System.out.println("Care Center's facilities set successfully!");
				break;
			}
			for (String part : input.split("[,\\-/\\s]+")) {
				part = part.trim();
				try {
					int p = Integer.parseInt(part);
					p--;
					if (p >= 0 && p < list.size()) {
						f.add(list.get(p));
					} else {
						System.out.println("Invalid number: " + (p + 1)
								+ ". Please enter numbers corresponding to care center facilities, or 4 for selecting all.");
						validInput = false;
						break;
					}
				} catch (NumberFormatException e) {
					System.out.println("Invalid input: " + part
							+ ". Please enter numbers corresponding to care center facilities.");
					validInput = false;
					break;
				}
			}

			if (!validInput) {
				continue;
			}

			try {
				cc.setFacilities(f);
				System.out.println("Care Center's facilities set successfully!");
				break;
			} catch (IllegalArgumentException e) {
				System.out.println(e.getMessage());
			}
		}
		return cc;
	}
	public static void updateCareCenterFacilities(Scanner scanner, Connection con, Sesion ses) throws SQLException {
	    CareCenter cc = new CareCenter();
	    String personId;
		String rolename = ses.getRole().getName();
		String userId = ses.getUserId();
	    if (rolename.equals("admin"))
	    {
	    	personId = selectCareCenter(scanner, con);
	        if (personId == null) {
	            System.out.println("No care center selected. Operation cancelled.");
	            return; 
	        }
	    } 
	    else {
		    personId = ses.getPersonId();
	    }
	    
	    cc.setPerson_id(personId);
	    CareCenterDAOI carDao = new CareCenterDAOI(con);
	    List<Facilities> currentFacilities = carDao.getCareCenterFacilities(personId);

	    Set<Facilities> allFacilities = new HashSet<>(Arrays.asList(Facilities.values()));
	    Set<Facilities> currentSet = new HashSet<>(currentFacilities);
	    Set<Facilities> availableFacilities = new HashSet<>(allFacilities);
	    availableFacilities.removeAll(currentSet);

	
	    List<Facilities> availableFacilitiesList = new ArrayList<>(availableFacilities);
	    
	    if (availableFacilitiesList.size()>=1) {
	    System.out.println("\nAvailable facilities to add:");
	    for (int i = 0; i < availableFacilitiesList.size(); i++) {
	        System.out.println((i + 1) + ": " + availableFacilitiesList.get(i).name());
	    }
	    int selectAllOption = 0;
	    if (availableFacilitiesList.size()>=1) {
		    selectAllOption = availableFacilitiesList.size() + 1;
		    System.out.println(selectAllOption + ": SELECT ALL");
	    }
	    
	    while (true) {
	        System.out.print("Enter facility numbers.");
	        String input = scanner.nextLine().trim();
	        
	        if (input.isEmpty()) {
	            System.out.println("No input provided. Please try again.");
	            continue;
	        }

	        Set<Facilities> selectedFacilities = new HashSet<>();
	        

	        if (selectAllOption > 1) {
	        if (input.equals(String.valueOf(selectAllOption))) {
	            selectedFacilities.addAll(availableFacilities);
	            cc.setFacilities(selectedFacilities);
	            System.out.println("All available facilities selected successfully!");
	            break;
	        }}
	        

	        boolean invalidInput = false;
	        for (String part : input.split("[,\\s]+")) {
	            try {
	                int choice = Integer.parseInt(part.trim());
	                if (choice > 0 && choice <= availableFacilitiesList.size()) {
	                    selectedFacilities.add(availableFacilitiesList.get(choice - 1));
	                } else {
	                    System.out.println("Invalid number: " + choice + ". Please enter numbers between 1 and " + availableFacilitiesList.size());
	                    invalidInput = true;
	                    break;
	                }
	            } catch (NumberFormatException e) {
	                System.out.println("Invalid input: " + part + ". Please enter numbers only.");
	                invalidInput = true;
	                break;
	            }
	        }
	        
	        if (invalidInput) {
	            continue;
	        }
	        
	        if (selectedFacilities.isEmpty()) {
	            System.out.println("No valid facilities selected. Please try again.");
	            continue;
	        }
	        
	        try {
	            cc.setFacilities(selectedFacilities);
	            System.out.println("Selected facilities set successfully!");
	            break;
	        } catch (IllegalArgumentException e) {
	            System.out.println("Error: " + e.getMessage());
	        }
	    }}
	    else {
	    	System.out.println("All possible facilities already available in Care Center. To delete, please select other function.");
	    	return;
	    }
		CareCenterDAOI ccDao = new CareCenterDAOI(con);
		cc.setCreatedBy(userId);
		ccDao.insertCareCenterFacilities(cc);
		return;
	}
	public static void deleteCareCenterFacilities(Scanner scanner, Connection con, Sesion ses) throws SQLException {
	    CareCenterDAOI carDao = new CareCenterDAOI(con);

	    String personId;
		String rolename = ses.getRole().getName();
		String userId = ses.getUserId();
	    if (rolename.equals("admin"))
	    {
	    	personId = selectCareCenter(scanner, con);
	        if (personId == null) {
	            System.out.println("No care center selected. Operation cancelled.");
	            return;
	        }
	    } 
	    else {
		    personId = ses.getPersonId();
	    }
	    
	    List<Facilities> currentFacilities = carDao.getCareCenterFacilities(personId);
	    
	    if (currentFacilities.isEmpty()) {
	        System.out.println("No facilities are currently assigned to this care center.");
	        return;
	    }

	    System.out.println("Current facilities in care center:");
	    int i = 1;
	    for (Facilities f : currentFacilities) {
	        System.out.println(i + ": " + f.name());
	        i++;
	    }

	    System.out.println(i + ": SELECT ALL");

	    System.out.print("Enter care center facility numbers to delete (e.g., 1,2 or " + i + "' for all): ");

	    while (true) {
	        String input = scanner.nextLine().trim();
	        Set<Facilities> toDelete = new HashSet<>();
	        boolean validInput = true;

	        if (input.equals(String.valueOf(i))) {
	            toDelete.addAll(currentFacilities);
	        } else {
	            for (String part : input.split("[,\\-/\\s]+")) {
	                try {
	                    int choice = Integer.parseInt(part.trim());
	                    if (choice >= 1 && choice <= currentFacilities.size()) {
	                        toDelete.add(currentFacilities.get(choice - 1));
	                    } else {
	                        System.out.println("Invalid number: " + choice);
	                        validInput = false;
	                        break;
	                    }
	                } catch (NumberFormatException e) {
	                    System.out.println("Invalid input: " + part);
	                    validInput = false;
	                    break;
	                }
	            }
	        }

	        if (!validInput) {
	            System.out.print("Try again: ");
	            continue;
	        }
	        
	        carDao.deleteCareCenterFacilities(personId, toDelete, userId);
	        System.out.println("Selected facilities have been deleted.");
	        return;
	    }
	}
	public static List<Legal> viewAllCareCenters(Connection con) throws SQLException
	{
		LegalDAOI legDao = new LegalDAOI(con);
		List<Legal> allCareCenters = legDao.getAllLegals(LegalType.CARE_CENTER);
		int i = 1;
		for (Legal l : allCareCenters)
		{
			System.out.println(i++ + " " + l.getName());
		}
		return allCareCenters;
	}
	public static String selectCareCenter(Scanner scanner, Connection con) throws SQLException {
	    List<Legal> allCareCenters = viewAllCareCenters(con);
	    
	    if (allCareCenters.isEmpty()) {
	        System.out.println("No care centers available.");
	        return null;
	    }

	    while (true) {
	        try {
	            String inputStr = scanner.nextLine();
	            int input = Integer.parseInt(inputStr);
	            
	            if (input < 1 || input > allCareCenters.size()) {
	                System.out.println("Invalid entry. Please enter a valid number from the list.");
	                continue;
	            }
	            
	            Legal leg = allCareCenters.get(input-1);
	            return leg.getPerson_id(); 
	        } 
	        catch (NumberFormatException e) {
	            System.out.println("Invalid entry. Please enter a valid number from the list.");
	        }
	    }
	}
	public static void deleteCareCenter(Connection con, String careCenterId) throws SQLException
	{
	    ActivityController.deleteActivitiesByCC(con, careCenterId);
	    new CareCenterDAOI(con).deleteCareCenter(careCenterId);
	}
}
