package pets.controllers;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import pets.model.Legal;
import pets.model.Sesion;
import pets.model.VetStation;
import pets.model.enums.LegalType;
import pets.model.enums.MedicalSpecialties;
import pets.model.enums.PetType;
import pets.repository.daoimpl.LegalDAOI;
import pets.repository.daoimpl.VetStationDAOI;

public class VetStationController {
	public static VetStation addVetStation(Scanner scanner) throws SQLException {
		VetStation vs = new VetStation();

		System.out.println("Possible medical specialties:");

		int i = 1;
		List<MedicalSpecialties> lis = new ArrayList<>();
		for (MedicalSpecialties m : MedicalSpecialties.values()) {
			lis.add(m);
			System.out.println(i + ": " + m.name());
			i++;

		}
		System.out.println("7: SELECT ALL");
		while (true) {
			System.out.print("Enter medical specialty numbers (1-6) or 7 for selecting all: ");
			String input = scanner.nextLine().trim();

			if (input.isEmpty()) {
				System.out.println("Error: Please enter at least one number.");
				continue;
			}
			Set<MedicalSpecialties> ms = new HashSet<>();

			if (input.equals("7")) {
				ms.addAll(lis);
				vs.setSpecialties(ms);
				System.out.println("Medical specialties set successfully!");
				break;
			}
			boolean hasInvalidInput = false;

			for (String part : input.split("[,\\-/\\s]+")) {
				part = part.trim();

				try {
					int p = Integer.parseInt(part);
					p--;

					if (p >= 0 && p < lis.size()) {
						MedicalSpecialties specialty = lis.get(p);
						if (ms.contains(specialty)) {
							System.out.println("Warning: Duplicate entry for " + (p + 1));
						} else {
							ms.add(specialty);
						}
					} else {
						System.out.println(
								"Error: Invalid number " + (p + 1) + ". Please enter correct numbers.");
						hasInvalidInput = true;
						break;
					}
				} catch (NumberFormatException e) {
					System.out.println("Error: '" + part + "' is not a valid number.");
					hasInvalidInput = true;
					break;
				}
			}

			if (hasInvalidInput) {
				continue;
			}

			try {
				vs.setSpecialties(ms);
				System.out.println("Medical specialties set successfully!");
				break;
			} catch (IllegalArgumentException e) {
				System.out.println("Error: " + e.getMessage());
			}
		}

		System.out.println("Possible pets:");
		i = 1;
		List<PetType> list = new ArrayList<>();
		for (PetType pt : PetType.values()) {
			list.add(pt);
			System.out.println(i + ": " + pt.name());
			i++;

		}

		System.out.println("7: SELECT ALL");

		while (true) {
			System.out.print("Enter pet type numbers (1-6) or 7 for selecting all: ");
			String input = scanner.nextLine().trim();

			if (input.isEmpty()) {
				System.out.println("Please enter at least one pet type number.");
				continue;
			}

			Set<PetType> pt = new HashSet<>();
			if (input.equals("7")) {
				pt.addAll(list);
				vs.setAllowedSpecies(pt);
				System.out.println("Allowed pets set successfully!");
				break;
			}

			boolean inputValid = true;

			for (String part : input.split("[,\\-/\\s]+")) {
				part = part.trim();

				try {
					int p = Integer.parseInt(part);
					p--;

					if (p >= 0 && p < list.size()) {
						pt.add(list.get(p));
					} else {
						System.out.println("Invalid number: " + (p + 1) + ". Please enter correct numbers. ");
						inputValid = false;
						break;
					}
				} catch (NumberFormatException e) {
					System.out.println("Invalid input: '" + part + "'. Please enter numbers only.");
					inputValid = false;
					break;
				}
			}

			if (!inputValid) {
				continue;
			}

			try {
				vs.setAllowedSpecies(pt);
				break;
			} catch (IllegalArgumentException e) {
				System.out.println(e.getMessage());
			}
		}

		while (true) {
			System.out.print("Does the vet station offer emergency availability? (Y/N): ");
			String input = scanner.nextLine().trim().toUpperCase();

			if (input.equals("Y") || input.equals("N")) {
				try {
					boolean eA = input.equals("Y");
					vs.setEmergencyAvailability(eA);
					break;
				} catch (IllegalArgumentException e) {
					System.out.println(e.getMessage());
				}
			} else {
				System.out.println("Invalid input. Please enter 'Y' or 'N'.");
			}
		}

		return vs;
	}
	public static void updateVetStationSpecialties(Scanner scanner, Connection con, Sesion ses) throws SQLException {
	    VetStation vs = new VetStation();
	    String personId;
	    String userId = ses.getUserId();
		String rolename = ses.getRole().getName();

	    if (rolename.equals("admin")) {
	        personId = selectVetStation(scanner, con);
	        if (personId == null) {
	            System.out.println("No vet station selected. Operation cancelled.");
	            return;
	        }
	    } else {
	        personId = ses.getPersonId();
	    }

	    vs.setPerson_id(personId);
	    VetStationDAOI vsDao = new VetStationDAOI(con);
	    List<MedicalSpecialties> currentSpecialties = vsDao.getVetStationSpecialties(personId);

	    Set<MedicalSpecialties> allSpecialties = new HashSet<>(Arrays.asList(MedicalSpecialties.values()));
	    Set<MedicalSpecialties> availableSpecialties = new HashSet<>(allSpecialties);
	    availableSpecialties.removeAll(currentSpecialties);

	    List<MedicalSpecialties> availableList = new ArrayList<>(availableSpecialties);

	    if (availableList.isEmpty()) {
	        System.out.println("All possible specialties already available in Vet Station. To delete, please select other function.");
	        return;
	    }

	    System.out.println("\nAvailable specialties to add:");
	    for (int i = 0; i < availableList.size(); i++) {
	        System.out.println((i + 1) + ": " + availableList.get(i).name());
	    }
	    int selectAllOption = availableList.size() + 1;
	    if (availableList.size() > 1) {
	        System.out.println(selectAllOption + ": SELECT ALL");
	    }

	    while (true) {
	        System.out.print("Enter specialty numbers: ");
	        String input = scanner.nextLine().trim();

	        if (input.isEmpty()) {
	            System.out.println("No input provided. Please try again.");
	            continue;
	        }

	        Set<MedicalSpecialties> selected = new HashSet<>();

	        if (input.equals(String.valueOf(selectAllOption))) {
	            selected.addAll(availableSpecialties);
	        } else {
	            boolean invalidInput = false;
	            for (String part : input.split("[,\\s]+")) {
	                try {
	                    int choice = Integer.parseInt(part.trim());
	                    if (choice > 0 && choice <= availableList.size()) {
	                        selected.add(availableList.get(choice - 1));
	                    } else {
	                        System.out.println("Invalid number: " + choice);
	                        invalidInput = true;
	                        break;
	                    }
	                } catch (NumberFormatException e) {
	                    System.out.println("Invalid input: " + part);
	                    invalidInput = true;
	                    break;
	                }
	            }
	            if (invalidInput || selected.isEmpty()) {
	                System.out.println("Invalid or empty selection. Try again.");
	                continue;
	            }
	        }

	        vs.setSpecialties(selected);
	        vs.setCreatedBy(userId);
	        vsDao.insertVetStationSpecialties(vs);
	        System.out.println("Selected specialties set successfully!");
	        return;
	    }
	}

	public static void deleteVetStationSpecialties(Scanner scanner, Connection con, Sesion ses) throws SQLException {
	    VetStationDAOI vsDao = new VetStationDAOI(con);
	    String personId;
		String rolename = ses.getRole().getName();
		String userId = ses.getUserId();
	    if (rolename.equals("admin")) {
	        personId = selectVetStation(scanner, con); // Implement this similarly to selectCareCenter()
	        if (personId == null) {
	            System.out.println("No vet station selected. Operation cancelled.");
	            return;
	        }
	    } else {
	        personId = ses.getPersonId();
	    }

	    List<MedicalSpecialties> currentSpecialties = vsDao.getVetStationSpecialties(personId);
	    if (currentSpecialties.isEmpty()) {
	        System.out.println("No specialties are currently assigned to this vet station.");
	        return;
	    }

	    System.out.println("Current specialties in vet station:");
	    for (int i = 0; i < currentSpecialties.size(); i++) {
	        System.out.println((i + 1) + ": " + currentSpecialties.get(i).name());
	    }
	    int selectAllOption = currentSpecialties.size() + 1;
	    System.out.println(selectAllOption + ": SELECT ALL");

	    System.out.print("Enter specialty numbers to delete: ");

	    while (true) {
	        String input = scanner.nextLine().trim();
	        Set<MedicalSpecialties> toDelete = new HashSet<>();

	        if (input.equals(String.valueOf(selectAllOption))) {
	            toDelete.addAll(currentSpecialties);
	        } else {
	            boolean validInput = true;
	            for (String part : input.split("[,\\-/\\s]+")) {
	                try {
	                    int choice = Integer.parseInt(part.trim());
	                    if (choice >= 1 && choice <= currentSpecialties.size()) {
	                        toDelete.add(currentSpecialties.get(choice - 1));
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
	            if (!validInput || toDelete.isEmpty()) {
	                System.out.print("Try again: ");
	                continue;
	            }
	        }

	        vsDao.deleteVetStationSpecialties(personId, toDelete, userId);
	        System.out.println("Selected specialties have been deleted.");
	        return;
	    }
	}

	public static void deleteVetStationSpecies(Scanner scanner, Connection con, Sesion ses) throws SQLException {
	    VetStationDAOI vsDao = new VetStationDAOI(con);
	    String personId;
		String rolename = ses.getRole().getName();
		String userId = ses.getUserId();
	    if (rolename.equals("admin")) {
	        personId = selectVetStation(scanner, con);
	        if (personId == null) {
	            System.out.println("No vet station selected. Operation cancelled.");
	            return;
	        }
	    } else {
	        personId = ses.getPersonId();
	    }

	    List<PetType> currentSpecies = vsDao.getVetStationSpecies(personId);
	    if (currentSpecies.isEmpty()) {
	        System.out.println("No species are currently assigned to this vet station.");
	        return;
	    }

	    System.out.println("Current pet types allowed in vet station:");
	    for (int i = 0; i < currentSpecies.size(); i++) {
	        System.out.println((i + 1) + ": " + currentSpecies.get(i).name());
	    }
	    int selectAllOption = currentSpecies.size() + 1;
	    System.out.println(selectAllOption + ": SELECT ALL");

	    System.out.print("Enter pet type numbers to delete: ");
	    while (true) {
	        String input = scanner.nextLine().trim();
	        Set<PetType> toDelete = new HashSet<>();

	        if (input.equals(String.valueOf(selectAllOption))) {
	            toDelete.addAll(currentSpecies);
	        } else {
	            boolean validInput = true;
	            for (String part : input.split("[,\\-/\\s]+")) {
	                try {
	                    int choice = Integer.parseInt(part.trim());
	                    if (choice >= 1 && choice <= currentSpecies.size()) {
	                        toDelete.add(currentSpecies.get(choice - 1));
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

	            if (!validInput || toDelete.isEmpty()) {
	                System.out.print("Try again: ");
	                continue;
	            }
	        }

	        vsDao.deleteVetStationSpecies(personId, toDelete, userId);
	        System.out.println("Selected species have been deleted.");
	        return;
	    }
	}

	public static void updateVetStationSpecies(Scanner scanner, Connection con, Sesion ses) throws SQLException {
	    VetStation vs = new VetStation();
	    String personId;
		String rolename = ses.getRole().getName();
		String userId = ses.getUserId();
	    if (rolename.equals("admin")) {
	        personId = selectVetStation(scanner, con);
	        if (personId == null) {
	            System.out.println("No vet station selected. Operation cancelled.");
	            return;
	        }
	    } else {
	        personId = ses.getPersonId();
	    }

	    vs.setPerson_id(personId);
	    VetStationDAOI vsDao = new VetStationDAOI(con);
	    List<PetType> currentSpecies = vsDao.getVetStationSpecies(personId);

	    Set<PetType> allSpecies = new HashSet<>(Arrays.asList(PetType.values()));
	    Set<PetType> availableSpecies = new HashSet<>(allSpecies);
	    availableSpecies.removeAll(currentSpecies);

	    List<PetType> availableList = new ArrayList<>(availableSpecies);
	    if (availableList.isEmpty()) {
	        System.out.println("All possible species already available in Vet Station. To delete, please select the other function.");
	        return;
	    }

	    System.out.println("\nAvailable species to add:");
	    for (int i = 0; i < availableList.size(); i++) {
	        System.out.println((i + 1) + ": " + availableList.get(i).name());
	    }
	    int selectAllOption = availableList.size() + 1;
	    if (availableList.size() > 1) {
	        System.out.println(selectAllOption + ": SELECT ALL");
	    }

	    while (true) {
	        System.out.print("Enter species numbers: ");
	        String input = scanner.nextLine().trim();
	        Set<PetType> selected = new HashSet<>();

	        if (input.isEmpty()) {
	            System.out.println("No input provided. Please try again.");
	            continue;
	        }

	        if (input.equals(String.valueOf(selectAllOption))) {
	            selected.addAll(availableSpecies);
	        } else {
	            boolean invalidInput = false;
	            for (String part : input.split("[,\\s]+")) {
	                try {
	                    int choice = Integer.parseInt(part.trim());
	                    if (choice >= 1 && choice <= availableList.size()) {
	                        selected.add(availableList.get(choice - 1));
	                    } else {
	                        System.out.println("Invalid number: " + choice);
	                        invalidInput = true;
	                        break;
	                    }
	                } catch (NumberFormatException e) {
	                    System.out.println("Invalid input: " + part);
	                    invalidInput = true;
	                    break;
	                }
	            }

	            if (invalidInput || selected.isEmpty()) {
	                System.out.println("Invalid or empty selection. Try again.");
	                continue;
	            }
	        }

	        vs.setAllowedSpecies(selected);
	        vs.setCreatedBy(userId);
	        vsDao.insertVetStationSpecies(vs);
	        System.out.println("Selected species set successfully!");
	        return;
	    }
	}

	public static void updateEmergencyAvailability(Scanner scanner, Connection con, Sesion ses) throws SQLException
	{
		VetStationDAOI vsDao = new VetStationDAOI(con);

	    VetStation vs = new VetStation();
	    String personId;
		String rolename = ses.getRole().getName();
		String userId = ses.getUserId();
	    if (rolename.equals("admin")) {
	        personId = selectVetStation(scanner, con);
	        if (personId == null) {
	            System.out.println("No vet station selected. Operation cancelled.");
	            return;
	        }
	    } else {
	        personId = ses.getPersonId();
	    }
		vs = vsDao.getVetStation(personId);
	    boolean currentStatus = vs.getEmergencyAvailability();
	    System.out.printf("\nCurrent emergency availability status: %s%n",
	                     currentStatus ? "OFFERS emergency services" : "DOES NOT offer emergency services");
		while (true) {
	        System.out.print("Change emergency availability? (Y/N): ");
	        String input = scanner.nextLine().trim().toUpperCase();

			if (input.equals("Y")) {
				try {
					boolean newStatus = !currentStatus;
					vs.setEmergencyAvailability(newStatus);
					vsDao.updateEmergencyAvailability(personId, newStatus, userId);
			        System.out.println("Changed emergency availability.");
					break;
				} catch (IllegalArgumentException e) {
					System.out.println(e.getMessage());
				}
			} else if (input.equals("N")) {
				 System.out.println("Emergency availability remains unchanged.");
				 break;
			}else {
				System.out.println("Invalid input. Please enter 'Y' or 'N'.");
			}
		}

	}
	public static List<VetStation> listVetStationsByPetType(Connection con, PetType pt) throws SQLException
	{
		VetStationDAOI vsDao = new VetStationDAOI(con);
		List<VetStation> vss = vsDao.getVetStationsByPetType(String.valueOf(pt));
		int i = 1;
		for (VetStation v : vss) {
			System.out.println(i++ + ":" + v.getName());
		}
		return vss;
	}
	public static VetStation selectVetStationPT(Scanner scanner, List<VetStation> vss) {
		VetStation ret = new VetStation();
		while (true) {
			System.out.println("Enter the vet station.");

			try {
				String inputStr = scanner.nextLine().trim();
				int input = Integer.parseInt(inputStr);
				
				if (input < 1 || input > vss.size())
				{
					System.out.println("Invalid entry. Please enter a valid number from the list.");
					continue;
				}
				try {
					ret = vss.get(input-1);
					break;
				} catch (IndexOutOfBoundsException e) {
					System.out.println("Invalid entry. Please enter a valid number from the list.");
				}
			}catch (IllegalArgumentException e) {
					System.out.println("Error: " + e.getMessage());}	
		}
		return ret;
	}
	public static List<Legal> viewAllVetStations(Connection con) throws SQLException
	{
		LegalDAOI legDao = new LegalDAOI(con);
		List<Legal> allVetStations = legDao.getAllLegals(LegalType.VET_STATION);
		int i = 1;
		for (Legal l : allVetStations)
		{
			System.out.println(i++ + " " + l.getName());
		}
		return allVetStations;
	}
	public static String selectVetStation(Scanner scanner, Connection con) throws SQLException {
		 List<Legal> allVetStations = viewAllVetStations(con);
		    
		    if (allVetStations.isEmpty()) {
		        System.out.println("No vet stations available.");
		        return null;
		    }

		    while (true) {
		        try {
		            String inputStr = scanner.nextLine();
		            int input = Integer.parseInt(inputStr);
		            
		            if (input < 1 || input > allVetStations.size()) {
		                System.out.println("Invalid entry. Please enter a valid number from the list.");
		                continue;
		            }
		            
		            Legal leg = allVetStations.get(input-1);
		            return leg.getPerson_id(); 
		        } 
		        catch (NumberFormatException e) {
		            System.out.println("Invalid entry. Please enter a valid number from the list.");
		        }
		    }
}
	public static void deleteVetStation(Connection con, String vetStationId) throws SQLException
	{
		HealthEventController.deleteHealthEventsByVs(con, vetStationId);
		new VetStationDAOI(con).deleteVetStation(vetStationId);
	}
}