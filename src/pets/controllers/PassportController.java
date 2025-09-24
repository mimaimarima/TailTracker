package pets.controllers;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import pets.model.Passport;
import pets.model.Pet;
import pets.model.Sesion;
import pets.repository.dao.PassportDAO;
import pets.repository.daoimpl.PassportDAOI;

public class PassportController {
	public static void collectPassportFields(Scanner scanner, Connection con, Pet pet, Passport passport, boolean isEdit) throws SQLException {
		PassportDAOI passDao = new PassportDAOI(con);
		
		
		while (true) {
			System.out.print("Enter " + (isEdit ? "new " : "") + "passport number" + (isEdit ? " or \\ to skip" : "") + ": ");
			String number = scanner.nextLine().trim();
			Passport existingPassport = passDao.getPassportByPassportNumber(number);
			if (existingPassport!=null)
			{
				System.out.println("Passport number not available.");
				continue;
			}
			if (isEdit && number.equals("\\")) break;

			try {
				passport.setNumber(number);
				break;
			} catch (IllegalArgumentException e) {
				System.out.println(e.getMessage());
			}
		}

		// Date of Issue
		while (true) {
			System.out.print("Enter Date of Issue (YYYY-MM-DD)" + (isEdit ? " or \\ to skip" : "") + ": ");
			String input = scanner.nextLine().trim();
			if (isEdit && input.equals("\\")) break;

			try {
				LocalDate date = LocalDate.parse(input);
				if (date.isBefore(LocalDate.now().minusYears(30))) {
					System.out.println("Passport start date cannot be that old.");
					continue;
				}
				if (date.isBefore(pet.getDateOfBirth())) {
					System.out.println("Passport start date cannot be before pet's birthday.");
					continue;
				}
				passport.setDateIssue(date);
				break;
			} catch (DateTimeParseException e) {
				System.out.println("Invalid format. Use YYYY-MM-DD.");
			} catch (IllegalArgumentException e) {
				System.out.println("Error: " + e.getMessage());
			}
		}

		// Date of End
		while (true) {
			System.out.print("Enter Date of End (YYYY-MM-DD)" + (isEdit ? " or \\ to skip" : "") + ": ");
			String input = scanner.nextLine().trim();
			if (isEdit && input.equals("\\")) break;

			try {
				LocalDate dateEnd = LocalDate.parse(input);
				passport.setDateEnd(dateEnd);
				break;
			} catch (DateTimeParseException e) {
				System.out.println("Invalid format. Use YYYY-MM-DD.");
			} catch (IllegalArgumentException e) {
				System.out.println("Error: " + e.getMessage());
			}
		}
	}

	public static void registerPassport(Scanner scanner, Connection con, Sesion ses) throws SQLException {

		List<Pet> pets;
		pets = PetController.listPetsWithoutPassport(con);
		Pet pet;
		String petId;
		String userId = ses.getUserId();
		if (pets.size() > 1) {
			System.out.println("Please select for which pet you want to add the passport.");
			pet = PetController.selectPet(scanner, pets);
			petId = pet.getId();
		} else if (pets.size()==1) {
			pet = pets.get(0);
			petId = pet.getId();
			System.out.println("Registering passport for " + pet.getName());
		} else 
		{
			System.out.println("No pets found.");
			return;
		}
		Passport pass = new Passport();
		pass.setPetId(petId);
		pass.setCreatedBy(userId);
		collectPassportFields(scanner, con, pet, pass, false);
	    PassportDAOI passDao = new PassportDAOI(con);
	    passDao.insertPassport(pass);
	    System.out.println("Passport added successfully!");
	 
	}
	public static void printPassport(Scanner scanner, Connection con, Sesion ses) throws SQLException {
		List<Pet> pets = null;
		String rolename = ses.getRole().getName();
		
		if (rolename.equals("petowner"))
		{
			pets = PetController.listPetsByOwner(scanner, con, ses);
	}	else if (rolename.equals("admin"))
	{
			pets = PetController.listPets(con, true);
	}
			Pet pet;
			String petId;
			if (pets.size() > 1) {
				System.out.println("Please select for which pet you want to print the passport.");
				pet = PetController.selectPet(scanner, pets);
				petId = pet.getId();
			} else if (pets.size() == 1) {
				pet = pets.get(0);
				petId = pet.getId();
				System.out.println("Passport for." + pet.getName() + " " + pet.getPetType() + " " + pet.getBreed());
	
			} else {
				return;
			}

		PassportDAOI passDao = new PassportDAOI(con);
		Passport pass = passDao.getPassport(petId);
		if (pass != null) {
			System.out.println("Name: " + pet.getName());
			System.out.println("Species: " + pet.getPetType());
			System.out.println("Breed: " + pet.getBreed());
			System.out.println("Gender: " + pet.getGender());
			System.out.println("Date of Birth: " + pet.getGender());
			System.out.println("Passport Number: " + pass.getNumber());
			System.out.println("Date of Issue: " + pass.getDateIssue());
			System.out.println("Date of End: " + pass.getDateEnd());
		} else {
			System.out.println("No valid passport found.");
		}
 	}
	public static void updatePassport(Scanner scanner, Connection con, Sesion ses) throws SQLException {
		String userId = ses.getUserId();
	    PassportDAOI passDao = new PassportDAOI(con);
	    Map<Pet, Passport> petPassport = passDao.getValidPassports();
	    List<Map.Entry<Pet, Passport>> passportList = new ArrayList<>(petPassport.entrySet());

	    if (passportList.isEmpty()) {
	        System.out.println("No valid passports found.");
	        return;
	    }

	    System.out.println("\nVALID PASSPORTS LIST:");
	    int index = 1;
	    for (Map.Entry<Pet, Passport> entry : passportList) {
	        Pet pet = entry.getKey();
	        Passport pass = entry.getValue();
	        System.out.println("[" + index + "] " + pet.getName() + " (" + pet.getPetType() + ")");
	        System.out.println("    Passport: " + pass.getNumber());
	        System.out.println("    Valid until: " + pass.getDateEnd());
	        System.out.println("----------------------------------");
	        index++;
	    }

	    Passport selectedPassport = null;
	    Pet selectedPet = null;

	    while (true) {
	        System.out.print("\nEnter passport number to edit (1-" + passportList.size() + ") or '\\' to cancel: ");
	        String input = scanner.nextLine().trim();

	        if (input.equals("\\")) {
	            System.out.println("Operation cancelled.");
	            return;
	        }

	        try {
	            int choice = Integer.parseInt(input);
	            if (choice >= 1 && choice <= passportList.size()) {
	                Map.Entry<Pet, Passport> selectedEntry = passportList.get(choice - 1);
	                selectedPet = selectedEntry.getKey();
	                selectedPassport = selectedEntry.getValue();
	                break;
	            } else {
	                System.out.println("Please enter a number between 1 and " + passportList.size());
	            }
	        } catch (NumberFormatException e) {
	            System.out.println("Invalid input. Please enter a number.");
	        }catch (IllegalArgumentException e) {
				System.out.println("Error: " + e.getMessage());
			}
	    }

	    Passport pass = new Passport();
	    pass.setId(selectedPassport.getId());
	    pass.setPetId(selectedPassport.getPetId());
	    pass.setCreatedBy(userId);
	    pass.setDateIssue(selectedPassport.getDateIssue());
	    pass.setDateEnd(selectedPassport.getDateEnd());
	    pass.setNumber(selectedPassport.getNumber());

	    collectPassportFields(scanner, con, selectedPet, pass, true);
	    passDao.updatePassport(pass);
	    System.out.println("Passport updated successfully!");
	    
		}

	public static void deletePassport(Scanner scanner, Connection con) throws SQLException 
	{
		PassportDAO paDao = new PassportDAOI(con);
		Passport pass = new Passport();
		Map<Pet, Passport> petPassport = paDao.getValidPassports();
		List<Map.Entry<Pet, Passport>> passportList = new ArrayList<>(petPassport.entrySet());

		if (passportList.isEmpty()) {
		    System.out.println("No valid passports found.");
		    return;
		}

		System.out.println("\nVALID PASSPORTS LIST:");
		int index = 1;
		for (Map.Entry<Pet, Passport> entry : passportList) {
		    Pet pet = entry.getKey();
		    pass = entry.getValue();
		    
		    System.out.println("[" + index + "] " + pet.getName() + " (" + pet.getPetType() + ")");
		    System.out.println("    Passport: " + pass.getNumber());
		    System.out.println("    Valid until: " + pass.getDateEnd());
		    System.out.println("----------------------------------");
		    index++;
		}

		Passport selectedPassport = null;
//		Pet selectedPet = null;
		while (true) {
		    System.out.print("\nEnter passport number to delete (1-" + passportList.size() + ") or '\\' to cancel: ");
		    String input = scanner.nextLine().trim();
		    
		    if (input.equals("\\")) {
		        System.out.println("Operation cancelled.");
		        return;
		    }
		    
		    try {
		        int choice = Integer.parseInt(input);
		        if (choice >= 1 && choice <= passportList.size()) {
		            Map.Entry<Pet, Passport> selectedEntry = passportList.get(choice - 1);
		      //      selectedPet = selectedEntry.getKey();
		            selectedPassport = selectedEntry.getValue();
		            break;
		        } else {
		            System.out.println("Please enter a number between 1 and " + passportList.size());
		        }
		    } catch (NumberFormatException e) {
		        System.out.println("Invalid input. Please enter a number.");
		    }
		}
		
		paDao.deletePassport(selectedPassport.getId());
	}
}
