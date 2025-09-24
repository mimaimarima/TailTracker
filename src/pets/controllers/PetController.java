package pets.controllers;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import pets.model.Pet;
import pets.model.PetOwnership;
import pets.model.Physical;
import pets.model.Sesion;
import pets.model.User;
import pets.model.enums.Breed;
import pets.model.enums.Gender;
import pets.model.enums.PetType;
import pets.repository.daoimpl.ActivityDAOI;
import pets.repository.daoimpl.EventDAOI;
import pets.repository.daoimpl.HealthEventDAOI;
import pets.repository.daoimpl.PassportDAOI;
import pets.repository.daoimpl.PersonDAOI;
import pets.repository.daoimpl.PetDAOI;
import pets.repository.daoimpl.PetOwnershipDAOI;
import pets.repository.daoimpl.PhysicalDAOI;
import pets.repository.daoimpl.VetStationDAOI;

public class PetController {
	public static List<Breed> listBreedsForPetType(PetType pt) {
		System.out.println("Available breedds for " + pt + ":");
		int i = 1;
		List<Breed> list = new ArrayList<>();
		for (Breed breed : Breed.values()) {
			if (breed.getPetType() == pt) {
				list.add(breed);
				System.out.println(i + ": " + breed.name());
				i++;
			}
		}
		return list;
	}
	public static void registerPet(Scanner scanner, Connection con, Sesion ses) throws SQLException {
		
		
		Pet pet = new Pet();
		String ownerId = "";
		String personId = ses.getPersonId();
		String rolename = ses.getRole().getName();
		String userId = ses.getUserId();
		if (rolename.equals("petowner")) {
			pet.setStray(false);
			pet.setRegisteredById(null);
			ownerId = personId;
		}
		if (rolename.equals("petadopter")) {
			ownerId = personId;
			pet.setStray(true);
		}
		
		while (true) {
			System.out.print("Enter name: ");
			String name = scanner.nextLine().trim();
			try {
				pet.setName(name);
				break;
			} catch (IllegalArgumentException e) {
				System.out.println(e.getMessage());
			}
		}
		if (rolename.equals("vet") || rolename.equals("carer") || rolename.equals("admin")) {
			while (true) {
				System.out.print("Is it a stray pet (Y/N)?");
				String ans = scanner.nextLine().trim().toUpperCase();
				if (ans.equals("Y")) {
					pet.setStray(true);
					break;
				} else if (ans.equals("N")) {
					pet.setStray(false);
					break;
				} else {
					System.out.println("Invalid input. Please enter 'Y' or 'N'.");
				}
			}}
		if (!pet.getStray()) {
			while (true) {
				System.out.print("Enter Date of Birth (YYYY-MM-DD).");
				String input = scanner.nextLine().trim();
	
				try {
					LocalDate dob = LocalDate.parse(input);
					pet.setDateOfBirth(dob);
					break;
				} catch (DateTimeParseException e) {
					System.out.println("Invalid format. Use YYYY-MM-DD.");
				} catch (IllegalArgumentException e) {
					System.out.println("Error: " + e.getMessage());
				}
			}
		}
		else 
		{
			while (true) {
				System.out.println("Enter age (X.0 or X.5 where X is years)");
				
				try {
					String in = scanner.nextLine().trim();
					float input = Float.parseFloat(in);
					
					if (input < 0)
					{
						System.out.println("Invalid input.");
						continue;
					}
					if (input > 30)
					{
						System.out.println("Age is too big.");
						continue;
					}
					
					float dec = input % 1;
					if (dec != 0.0f && dec != 0.5f)
					{
						System.out.println("Only full or half values are allowed.");
						continue;
					}
					int years = (int) input;
					int months = (int) ((input - years) * 12);
					
					LocalDate dob = LocalDate.now().minusYears(years).minusMonths(months);
					pet.setDateOfBirth(dob);
					break;
	
				} catch (NumberFormatException e) {
			        System.out.println("Please enter a valid number (e.g., 1.5).");
			    } catch (IllegalArgumentException e) {
			        System.out.println("Please enter a valid number (e.g., 1.5).");
			    }
			}
		}
		
		int i = 1;
		List<PetType> list = new ArrayList<>();
		for (PetType pt : PetType.values()) {
			list.add(pt);
			System.out.println(i + ": " + pt.name());
			i++;
	
		}
		while (true) {
		    System.out.print("Enter pet type number (1-6): ");
		    
		    try {
		        String inputStr = scanner.nextLine().trim();
		        int input = Integer.parseInt(inputStr);
		        
		        if (input < 1 || input > 6) {
		            System.out.println("Invalid entry: number must be between 1 and 6.");
		            continue;
		        }
		        
		        try {
		            PetType pt = list.get(input - 1); 
		            pet.setPetType(pt);
		            break;
		        } catch (IndexOutOfBoundsException e) {
		            System.out.println("Invalid selection: No pet type at that position.");
		        }
		        
		    } catch (NumberFormatException e) {
		        System.out.println("Invalid input: Please enter a number between 1 and 6.");
		    } catch (IllegalArgumentException e) {
		        System.out.println("Invalid pet type: " + e.getMessage());
		    }
		}
	
		PetType pt = pet.getPetType();
		List<Breed> lB = listBreedsForPetType(pt);
	
		while (true) {
	
			System.out.print("Enter breed number: ");
			try {
				String inputStr = scanner.nextLine().trim();
				int input = Integer.parseInt(inputStr);
	
				if (input < 1 || input > 6) {
					System.out.println("Invalid entry: number must be between 1 and 10.");
					continue;
				}
				try {
					Breed breed = lB.get(input - 1);
					pet.setBreed(breed);
					break;
				} catch (IndexOutOfBoundsException e) {
					System.out.println("Invalid selection: No breed type at that position.");
				}
	
			} catch (NumberFormatException e) {
				System.out.println("Invalid input: Please enter a number between 1 and 10.");
			} catch (IllegalArgumentException e) {
				System.out.println("Invalid breed: " + e.getMessage());
			}
		}
	
		while (true) {
			System.out.println("Enter gender:");
			i = 1;
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
				pet.setGender(gender);
				scanner.nextLine();
				break;
			} catch (ArrayIndexOutOfBoundsException e) {
				System.out.println("Invalid gender. Please try again.");
				scanner.nextLine();
			}
		}
	
		boolean flag = false;
		PetOwnership po = new PetOwnership();

		if (rolename.equals("vet") || rolename.equals("carer") || rolename.equals("admin")) {
			while (true) {
				System.out.print("Do you want to add information about the owner (Y/N)?");
				String ans = scanner.nextLine().trim().toUpperCase();
				if (ans.equals("Y")) {
	
					int owner = pet.getStray() ? 2 : 1;
					User ownerUser = UserController.addUser(scanner, con, false, owner,ses);
					PersonDAOI personDao = new PersonDAOI(con);
					ownerId= personDao.findPersonIdByUserId(ownerUser.getId());
		         //   System.out.println("Created owner with ID: " + ownerId);
				    po.setPhysicalId(ownerId);
					po.setCreatedBy(userId);
					flag = true;
					break;
				} else if (ans.equals("N")) {
					break;
				} else {
					System.out.println("Invalid input. Please enter 'Y' or 'N'.");
				}
			}
			pet.setRegisteredById(personId);
	
		}
		System.out.println("is stray: " + pet.getStray());
		pet.setDateOfRegistry(LocalDate.now());
		PetDAOI pDao = new PetDAOI(con);
		pet.setRegisteredById(userId);
		pDao.insert(pet);
		
		if (rolename.equals("petowner") || rolename.equals("petadopter") || flag) {
	
			po.setPetId(pet.getId());
			po.setPhysicalId(ownerId);
			po.setDateFrom(LocalDate.now());
			po.setCreatedBy(userId);
			PetOwnershipDAOI poDao = new PetOwnershipDAOI(con);
			poDao.insertOwner(po);
			System.out.println("Added owner!");
		}
		System.out.println("Pet registered successfully!");
	}
	public static List<Pet> listPetsByOwner(Scanner scanner, Connection con, Sesion ses) throws SQLException {
		List<Pet> pets;
		String personId = ses.getPersonId();
		String rolename = ses.getRole().getName();
		if (rolename.equals("admin"))
		{
			List<Physical> owners = new PetOwnershipDAOI(con).getAllOwners();
			personId = PhysicalController.selectPhysical(scanner, owners).getPerson_id();
		} else {
			personId = ses.getPersonId();
		}
		
		PetOwnershipDAOI petODao = new PetOwnershipDAOI(con);
		pets = petODao.viewPetsByOwner(personId);
		if (pets.size() >= 1) {
		int i = 1;
		for (Pet pet : pets) {
			System.out.println(i + " " + pet.getName() + " born on " + pet.getDateOfBirth() + " " + pet.getPetType() + " " + pet.getBreed());
			i++;
		}}
		else
		{
			System.out.println("No pets found.");}
		return pets;
		}
	public static List<Pet> listPetsWithoutOwner(Connection con) throws SQLException {
		List<Pet> pets;
		PetOwnershipDAOI test = new PetOwnershipDAOI(con);
		pets = test.viewPetsWithoutOwner();
		if (pets.size() >= 1) {
		int i = 1;
		for (Pet pet : pets) {
			System.out.println(i + " " + pet.getName());
			i++;
		}}
		else
		{
			System.out.println("No pets found.");}
	
		return pets;}
	public static List<Pet> listPetsByVS(Connection con, Sesion ses) throws SQLException {
		List<Pet> pets;
		String personId = ses.getPersonId();
		String userId = ses.getUserId();
		VetStationDAOI test = new VetStationDAOI(con);
		pets = test.viewRegisteredPets(userId, personId);
		if (pets.size() >= 1) {
	
		int i = 1;
		for (Pet pet : pets) {
			System.out.println(i + " " + pet.getName());
			i++;
		}}
		else
		{
			System.out.println("No pets found.");
	
		}
		return pets;}
	public static List<Pet> listPets(Connection con, boolean isAdmin) throws SQLException
	{
		
		List<Pet> pets;
		PetDAOI test = new PetDAOI(con);
		pets = test.viewRegisteredPets();
		int i = 1;
		if (pets.size() >= 1) {
	
			for (Pet pet : pets) {
			    LocalDate deathDate = pet.getDateOfDeath();
			    
			    if (!isAdmin && deathDate != null && !deathDate.equals(LocalDate.of(9999, 12, 31))) {
			        continue;
			    }

			    String output = String.format("%d: %s (%s - %s) born on %s",
			            i++, pet.getName(), pet.getPetType(), pet.getBreed(), pet.getDateOfBirth());

			    if (deathDate != null && !deathDate.equals(LocalDate.of(9999, 12, 31))) {
			        output += " - died on " + deathDate;
			    }

			    System.out.println(output);
			}

		}
		else 
		{
			System.out.println("No pets found.");
	
		}
		
		return pets; 
	}
	public static List<Pet> listPetsWithoutPassport(Connection con) throws SQLException
	{
		List<Pet> pets;
		PetDAOI petDao = new PetDAOI(con);
		pets = petDao.getRegisteredPetsWithoutValidPassports();
		int i = 1;
		if (pets.size()>=1) {
		for (Pet pet : pets) {
			System.out.println(i + " " + pet.getName());
			i++;
		}} else
		{
			System.out.println("No pets found.");
	
		}
		return pets;
	}
	public static List<Pet> listPetsByCC(Connection con, String careCenterId) throws SQLException
	{
		List<Pet> pets;
		PetDAOI petDao = new PetDAOI(con);
		pets = petDao.getPetsByCC(careCenterId);
		int i = 1;
		if (pets.size()>=1) {
		for (Pet pet : pets) {
			System.out.println(i + " " + pet.getName());
			i++;
		}} else
		{
			System.out.println("No pets found.");
	
		}
		return pets;
	}
	public static Pet selectPet(Scanner scanner, List<Pet> pets)
	{
		Pet pet;
		while (true) {
			
			try {
				String inputStr = scanner.nextLine();
				int input = Integer.parseInt(inputStr);
				
				if (input < 1 || input > pets.size()) {
					System.out.println("Invalid entry. Please enter a valid number from the list.");
					continue;
				}
			try {
				pet = pets.get(input-1);

				break;
			} catch (IndexOutOfBoundsException e) {
				System.out.println("Invalid entry. Please enter a valid number from the list.");
			}}
				catch (IllegalArgumentException e) {
			        System.out.println("Invalid entry. Please enter a valid number from the list.");
		    }
		}
		return pet;
	}
	public static void updateOwnershipDateTo(Scanner scanner, Connection con, Sesion ses) throws SQLException
	{
		List<Pet> lis = null;
		PetOwnership po;
		String personId = ses.getPersonId();
		String rolename = ses.getRole().getName();
		if (rolename.equals("petowner")) {

			lis = listPetsByOwner(scanner, con, ses);
		}
		if (rolename.equals("admin"))
		{
			List<PetOwnership> allOwnerships = listAllOwnerships(con);
			if (allOwnerships.size() > 0) {
				System.out.println("Please select the ownership you wish to update.");
				po = selectOwnership(scanner, allOwnerships);
			}
			else {
				System.out.println("No ownerships found.");
				return;
			}
		}
		Pet pet;
		
		System.out.println("Select the pet. Enter a number from the list.");

		PetOwnershipDAOI poDao = new PetOwnershipDAOI(con);
		pet = selectPet(scanner, lis);
		String petId = pet.getId();		
		po = poDao.getOwnership(petId);

		System.out.println("Enter the date of ending ownership.");

		while (true) {
			String input = scanner.nextLine().trim();

			try {
				LocalDate doe = LocalDate.parse(input);
				po.setDateTo(doe);
				poDao.updateOwnershipDateTo(petId, doe);

				System.out.println("Updated ownership date of end.");

				if (doe.isAfter(LocalDate.now()))
				{
					return;
				}
				break;
			} catch (DateTimeParseException e) {
				System.out.println("Invalid format. Use YYYY-MM-DD.");
			} catch (IllegalArgumentException e) {
				System.out.println("Error: " + e.getMessage());
			}
		}

		String ownerId;
		
		while (true) {
			System.out.print("Do you want to add information about the new owner (Y/N)?");
			String ans = scanner.nextLine().trim().toUpperCase();
			if (ans.equals("Y")) {

				int owner = pet.getStray() ? 2 : 1;
				User ownerUser = UserController.addUser(scanner, con, false, owner,ses);
				PersonDAOI personDao = new PersonDAOI(con);
				ownerId= personDao.findPersonIdByUserId(ownerUser.getId());
				break;
			} else if (ans.equals("N")) {
				ownerId = "";
				return;
			} else {
				System.out.println("Invalid input. Please enter 'Y' or 'N'.");
			}
		}
		po = new PetOwnership();
		po.setPetId(pet.getId());
		po.setPhysicalId(ownerId);
		po.setDateFrom(LocalDate.now());
		poDao.insertOwner(po);
		System.out.println("Added owner!");
		return;
		
	}

	public static List<PetOwnership> listAllOwnerships(Connection con) throws SQLException
	{
		PetDAOI petDao = new PetDAOI(con);
		PetOwnershipDAOI poDao = new PetOwnershipDAOI(con);
		List<PetOwnership> ownerships = poDao.getAllOwnerships();
		PhysicalDAOI physDao = new PhysicalDAOI(con);
		int i = 1;
		for (PetOwnership o : ownerships) {
		    Physical owner = physDao.getPhysicalById(o.getPhysicalId());
		    Pet pet = petDao.getPetById(o.getPetId());

		    System.out.printf("%d: %s %s owns %s (%s, %s) from %s to %s%n", i++,
		        owner.getName(),
		        owner.getSurname(),
		        pet.getName(),
		        pet.getPetType(),
		        pet.getBreed(),
		        o.getDateFrom(),
		        o.getDateTo() != null ? o.getDateTo() : "present"
		    );
		}
		
		return ownerships;
	}
	public static PetOwnership selectOwnership(Scanner scanner, List<PetOwnership> list)
	{
		PetOwnership po;
		while (true) {
			
			try {
				String inputStr = scanner.nextLine();
				int input = Integer.parseInt(inputStr);
				if (input < 1 || input > list.size()) {
					System.out.println("Invalid entry. Please enter a valid number from the list");
					continue;
				}
				try {
				po = list.get(input-1);
			
				break;
				}catch (IndexOutOfBoundsException e) {
					System.out.println("Invalid entry. Please enter a valid number from the list.");
					}
			} catch (IllegalArgumentException e) {
				System.out.println("Invalid entry. Please enter a valid number from the list");
			}
		}
		return po;
	}
	public static void addPetOwner(Scanner scanner, Connection con, Sesion ses) throws SQLException
	{
		List<Pet> allPetsWithoutOwner = listPetsWithoutOwner(con);
		
		System.out.println("Select the pet. Enter a number from the list.");
		Pet pet = selectPet(scanner, allPetsWithoutOwner);
		String ownerId = "";
		String personId = ses.getPersonId();
		String rolename = ses.getRole().getName();
		String userId = ses.getUserId();
		if (rolename.equals("admin")) {
			System.out.println("Do you want to search existing users (1) or register a new one? (2)");
			int input;
			while (true) {
				
				try {
					String inputStr = scanner.nextLine();
					input = Integer.parseInt(inputStr);
					
					if (input != 1 && input != 2) {
						System.out.println("Invalid entry. Please enter a valid number (1/2).");
						continue;
					}
					break;
				} catch (IndexOutOfBoundsException e) {
					System.out.println("Invalid entry. Please enter a valid number (1/2).");
				}
					catch (IllegalArgumentException e) {
				        System.out.println("Invalid entry. Please enter a valid number (1/2).");
			    }
			} 
			PersonDAOI personDao = new PersonDAOI(con);
			if (input == 1) {
				rolename = pet.getStray() ? "petadopter" : "petowner";
				User user = UserController.selectUser(scanner, con, rolename);
				ownerId = personDao.findPersonIdByUserId(user.getId());
				
			} else if (input == 2)
			{
				int owner = pet.getStray() ? 2 : 1;
				User ownerUser = UserController.addUser(scanner, con, false, owner,ses);
				ownerId = personDao.findPersonIdByUserId(ownerUser.getId());
			}
		} else if (rolename.equals("petadopter"))
		{
			ownerId = new PersonDAOI(con).findPersonIdByUserId(userId);
		}
		PetOwnership po = new PetOwnership();
		PetOwnershipDAOI poDao = new PetOwnershipDAOI(con);
		po.setPetId(pet.getId());
		po.setPhysicalId(ownerId);
		po.setDateFrom(LocalDate.now());
		po.setCreatedBy(userId);
		poDao.insertOwner(po);
		System.out.println("Added new owner!");
		return;		
	}
	public static void deleteOwnership(Scanner scanner, Connection con) throws SQLException
	{
		List<PetOwnership> allOwnerships = listAllOwnerships(con);
		PetOwnership po = new PetOwnership();
		if (allOwnerships.size() > 0) {
			po = selectOwnership(scanner, allOwnerships);
	}else {
		System.out.println("No ownerships found.");
		return;
	}
		PetOwnershipDAOI poDao = new PetOwnershipDAOI(con);
		poDao.deleteOwnership(po.getPetId());

	}
	public static void updatePet(Scanner scanner, Connection con, Sesion ses) throws SQLException {
		
		List<Pet> allPets;
		Pet pet;
		
		String personId = ses.getPersonId();
		String rolename = ses.getRole().getName();
		String userId = ses.getUserId();
		if (rolename.equals("carer"))
		{
			allPets = listPetsByCC(con, personId);
		} else if (rolename.equals("vet"))
		{
			allPets = listPetsByVS(con, ses);
		} else if (rolename.equals("petadopter") || rolename.equals("petowner"))
		{
			allPets = listPetsByOwner(scanner, con, ses);
		} else {
			allPets = listPets(con, true);
		}
		
		System.out.println("Please select the pet.");
	
		if (allPets.size() > 0) {
			pet = selectPet(scanner, allPets);
	
		}	
		else {
			System.out.println("No pets found.");
			return;
		}
		
		while (true) {
			System.out.print("Enter name or \\ to keep the same: ");
			String name = scanner.nextLine().trim();
			if (name.equals("\\"))
			{
				System.out.println("Name is the same.");
				break;
			}
			try {
				pet.setName(name);
				break;
			} catch (IllegalArgumentException e) {
				System.out.println(e.getMessage());
			}
		}

		while (true) {
			System.out.print("Update Date of Death or \\ to skip (YYYY-MM-DD).");
			String input = scanner.nextLine().trim();
			if (input.equals("\\"))
			{
				break;
			}
			try {
				LocalDate dod = LocalDate.parse(input);
				pet.setDateOfDeath(dod);
				break;
			} catch (DateTimeParseException e) {
				System.out.println("Invalid format. Use YYYY-MM-DD.");
			} catch (IllegalArgumentException e) {
				System.out.println("Error: " + e.getMessage());
			}
		}
		pet.setRegisteredById(userId);
		PetDAOI pDao = new PetDAOI(con);
		pDao.updatePet(pet);
		System.out.println("Pet updated successfully!");
		return;
	}
	public static void deletePet(Scanner scanner, Connection con) throws SQLException {
		List<Pet> allPets = listPets(con, true);
		Pet pet;
		String petId = "";
		if (allPets.size() > 0) {
			pet = selectPet(scanner, allPets);
			petId = pet.getId();
		}
		else {
			System.out.println("No pets found.");
			return;
		}
		 try {
		        con.setAutoCommit(false);

		        new HealthEventDAOI(con).deleteHealthEventByPetId(petId);
		        new PassportDAOI(con).deletePassportByPetId(petId);
		        new PetOwnershipDAOI(con).deleteOwnership(petId);
		        new ActivityDAOI(con).deleteActivityByPetId(petId);
		        new EventDAOI(con).deleteEventParticipationByPetId(petId);

		        new PetDAOI(con).deletePet(petId);

		        con.commit();
		        System.out.println("Pet and related records deleted.");
		    } catch (SQLException e) {
		    	con.rollback();
		        System.out.println("Failed to delete pet. Rolled back.");
		        throw e;
		    } finally {
		    	con.setAutoCommit(true);
		    }
		System.out.println("Pet deleted successfully!");
		return;
}
}
