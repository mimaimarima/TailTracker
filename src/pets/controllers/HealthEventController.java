package pets.controllers;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import pets.model.HealthEvent;
import pets.model.Pet;
import pets.model.Sesion;
import pets.model.VetStation;
import pets.model.enums.HealthEventType;
import pets.repository.daoimpl.HealthEventDAOI;

public class HealthEventController {
	private static void collectHealthEventDetails(Scanner scanner, HealthEvent hE, LocalDate dob, boolean isUpdate) {
		LocalDate date = dob;
		LocalTime time;
		LocalDateTime dateTime;

		while (true) {
			System.out.print("Enter date (YYYY-MM-DD)" + (isUpdate ? " or \\ to skip" : "") + ": ");
			String dateInput = scanner.nextLine().trim();

			if (isUpdate && dateInput.equals("\\")) break;

			try {
				date = LocalDate.parse(dateInput);

				if (dob != null && date.isBefore(dob)) {
					System.out.println("Date cannot be before pet's birthday.");
					continue;
				}
				if (date.isBefore(LocalDate.now().minusYears(30))) {
					System.out.println("Date cannot be more than 30 years in the past.");
					continue;
				}
				if (date.isAfter(LocalDate.now())) {
					System.out.println("Date cannot be in the future.");
					continue;
				}

				break;
			} catch (DateTimeParseException e) {
				System.out.println("Invalid date format. Please use YYYY-MM-DD.");
			}
		}

		while (true) {
			System.out.print("Enter time (HH:MM)" + (isUpdate ? " or \\ to skip" : "") + ": ");
			String timeInput = scanner.nextLine().trim();

			if (isUpdate && timeInput.equals("\\")) break;

			try {
				time = LocalTime.parse(timeInput);
				if (date != null) {
					dateTime = LocalDateTime.of(date, time);
					hE.setDateTime(dateTime);
				}
				break;
			} catch (DateTimeParseException e) {
				System.out.println("Invalid time format. Please use HH:MM.");
			}
		}


		while (true) {
			System.out.print("Enter description" + (isUpdate ? " or \\ to skip" : "") + ": ");
			String description = scanner.nextLine();

			if (isUpdate && description.equals("\\")) break;

			try {
				hE.setDescription(description);
				break;
			} catch (IllegalArgumentException e) {
				System.out.println(e.getMessage());
			}
		}


		while (true) {
			System.out.print("Enter recommendation" + (isUpdate ? " or \\ to skip" : "") + ": ");
			String recommendation = scanner.nextLine();

			if (isUpdate && recommendation.equals("\\")) break;

			try {
				hE.setRecommendation(recommendation);
				break;
			} catch (IllegalArgumentException e) {
				System.out.println(e.getMessage());
			}
		}


		while (true) {
			System.out.print("Enter price" + (isUpdate ? " or \\ to skip" : "") + ": ");
			String inputStr = scanner.nextLine().trim();

			if (isUpdate && inputStr.equals("\\")) break;

			if (inputStr.isEmpty() || inputStr.equalsIgnoreCase("null")) {
				System.out.println("Price cannot be empty.");
				continue;
			}

			try {
				BigDecimal price = new BigDecimal(inputStr);
				hE.setPrice(price);
				break;
			} catch ( IllegalArgumentException e) {
				System.out.println("Invalid price. Please enter a valid decimal number.");
			}
		}
	}

	public static void registerHealthEvent(Scanner scanner, Connection con, Sesion ses) throws SQLException {
		HealthEvent hE = new HealthEvent();
		HealthEventDAOI heDao = new HealthEventDAOI(con);
		List<Pet> lis = null;
		Pet pet = null;
		String personId = "";
		String rolename = ses.getRole().getName();
		String userId = ses.getUserId();
		if (rolename.equals("petowner") || rolename.equals("petadopter"))
		{
			personId = ses.getPersonId();
			lis = PetController.listPetsByOwner(scanner, con, ses);
		} else if (rolename.equals("vet"))
		{
			personId = ses.getPersonId();
			lis = PetController.listPetsByVS(con, ses);
			hE.setVetStationId(personId);
		} else if (rolename.equals("admin"))
		{
			lis = PetController.listPets(con, true);
		}
		if (lis.size() >=1) {
			System.out.println("Select the pet. Enter a number from the list.");
			pet = PetController.selectPet(scanner, lis);
		} else if (lis.size() == 1) 
		{
			pet = lis.get(0);
		} else {
			System.out.println("No pets found.");
			return;
		}
		String petId = pet.getId();
		hE.setPetId(petId);

		LocalDate dob = pet.getDateOfBirth();
		
		if (!rolename.equals("vet")) {
			List<VetStation> vss = VetStationController.listVetStationsByPetType(con, pet.getPetType());
			if (vss.size()>=1) {
				VetStation vs = VetStationController.selectVetStationPT(scanner, vss);
				hE.setVetStationId(vs.getPerson_id());
				hE.setVerified(false); }
			else {
				System.out.println("No vet station found.");
				return;
			}
		}
		int i = 1;
		hE.setCreatedBy(userId); 

		List<HealthEventType> list = new ArrayList<>();
		for (HealthEventType he : HealthEventType.values()) {
			list.add(he);
			System.out.println(i + ": " + he.name());
			i++;

		}
		while (true) {
			try {
				String inputStr = scanner.nextLine();
				int input = Integer.parseInt(inputStr);
				
				if (input < 1 || input > list.size()) {
					System.out.println("Invalid entry. Please enter a valid number from the list.");
					continue;
				}
			try {
				HealthEventType heT = list.get(input-1);
				hE.setTypeHE(heT);
				break;
			} catch (IndexOutOfBoundsException e) {
				System.out.println("Invalid entry. Please enter a valid number from the list.");
			}}
				catch (IllegalArgumentException e) {
			        System.out.println("Invalid entry. Please enter a valid number from the list.");
		    }
		}
		collectHealthEventDetails(scanner, hE, dob, false);
		if (rolename.equals("vet")) {
			hE.setVetStationId(personId);
			hE.setVerified(true);
		} 

		heDao.insert(hE);
		System.out.println("Health event registered successfully!");
		return;


	}
	public static void viewHEPET(Scanner scanner, Connection con, Sesion ses) throws SQLException
	{
		List<Pet> list = PetController.listPetsByOwner(scanner, con, ses);
		Pet pet = PetController.selectPet(scanner, list);
		viewHealthEventsByPet(scanner,con,pet.getId());
	}
	public static List<HealthEvent> viewHealthEventsByPet(Scanner scanner, Connection con, String petId) throws SQLException {

		HealthEventDAOI heDao = new HealthEventDAOI(con);

		List<HealthEvent> list = heDao.getHealthEventsByPet(petId);
	    System.out.println("\nTotal events found: " + list.size());
	    int i = 1;
		if (list.size()>=1) {
			for (HealthEvent he : list) {
				System.out.println(i + " Type:" + he.getTypeHE());
				System.out.println("Description:" + he.getDescription());
				System.out.println("Recommendation:" + he.getRecommendation());
				System.out.println("Date and time:" + he.getDateTime());
				System.out.println("Price:" + he.getPrice());
				i++;
			}
		}else {
			System.out.println("No health events found");
		}
		return list;
	}
	public static Map<Pet, List<HealthEvent>> viewHealthEventsByVS(Scanner scanner, Connection con, Sesion ses) throws SQLException
	{
		String personId;
		HealthEventDAOI heDao = new HealthEventDAOI(con);
		String rolename = ses.getRole().getName();
		if (rolename.equals("admin"))
		{
			personId = VetStationController.selectVetStation(scanner, con);
		} else 
		{
			personId = ses.getPersonId();
		}
		Map<Pet, List<HealthEvent>> petEvents = heDao.getHealthEventsByVetStation(personId);

		if (!petEvents.isEmpty()) {
			for (Map.Entry<Pet, List<HealthEvent>> entry : petEvents.entrySet()) {
			    Pet pet = entry.getKey();
			    System.out.println("\nPet: " + pet.getName() + " (" + pet.getPetType().toString().toLowerCase() + ")");
			    
			    for (HealthEvent event : entry.getValue()) {
			        System.out.println("-" + event.getTypeHE() + ": " + event.getDescription() + " " + event.getDateTime());
			        System.out.println(event.isVerified() ? "verified" : "unverified");
			    }
		}
			return petEvents;
		}
		else {
		    System.out.println("No health events found.");
		 return null;   
		}
}
	public static Map<Pet, List<HealthEvent>> listUnverifiedHealthEvents(Connection con, String personId) throws SQLException
	{
		HealthEventDAOI heDao = new HealthEventDAOI(con);

		Map<Pet, List<HealthEvent>> petEvents = heDao.getUnverifiedHealthEvents(personId);
		int i = 1;
		if (!petEvents.isEmpty()) {
			for (Map.Entry<Pet, List<HealthEvent>> entry : petEvents.entrySet()) {
			    Pet pet = entry.getKey();
			    System.out.println(i + " Pet: " + pet.getName() + " (" + pet.getPetType().toString().toLowerCase() + ")");
			    
			    for (HealthEvent event : entry.getValue()) {
			        System.out.println("-" + event.getTypeHE() + ": " + event.getDescription() + " " + event.getDateTime());
			        System.out.println(event.isVerified() ? "verified" : "unverified");
			    }
		}
			return petEvents;
		}
		else {
		    System.out.println("No health events found.");
		 return null;   
		}
	}
	public static void verifyHealthEventsVS(Scanner scanner, Connection con, String personId) throws SQLException
	{
		HealthEventDAOI heDao = new HealthEventDAOI(con);
		HealthEvent hE;
		Map<Pet, List<HealthEvent>> map = listUnverifiedHealthEvents(con, personId);
	   
		if (map!=null) {
			System.out.println("Please enter the health event you want to verify or \\ for none.");
			List<HealthEvent> allEvents = new ArrayList<>();
			for (List<HealthEvent> events : map.values()) {
				 allEvents.addAll(events);
			}
			hE = selectHealthEvent(scanner, allEvents);
			hE.setVerified(true);
			heDao.updateHealthEvent(hE);
			System.out.println("Verified health event!"); 
			}
		return;
	}
	public static void updateHealthEvent(Scanner scanner, Connection con, Sesion ses) throws SQLException
	{
		List<Pet> list = new ArrayList<>();
		String personId;
		String rolename = ses.getRole().getName();
		String userId = ses.getUserId();
		if (rolename.equals("vet")) {
	
			list = PetController.listPetsByVS(con, ses);
		} else if (rolename.equals("admin"))
		{
			list = PetController.listPets(con, true);
		} else if (rolename.equals("petowner") || rolename.equals("petadopter"))
		{
		
			personId = ses.getPersonId();
			list = PetController.listPetsByOwner(scanner, con, ses);;
		}
	
		Pet pet;
		HealthEvent hE = new HealthEvent();
		String petId;

		if (list.size() > 1) {
			System.out.println("Please select the pet.");
			pet = PetController.selectPet(scanner, list);
		}
		else if (list.size()==1)
		{
			pet = list.get(0);
			System.out.println("Updating health event for only pet: " + pet.getName());
		} else {
			System.out.println("No pets found.");
			return;
		}
		petId = pet.getId();
		List<HealthEvent> hes = viewHealthEventsByPet(scanner, con, petId);
		if (hes.size() >= 1) {
		    System.out.println("Please select the event or \\ to skip.");
		    
		    hE = selectHealthEvent(scanner, hes);
		    if (hE == null) {
		        return;
		    }
		    hE.setPetId(petId);
		    collectHealthEventDetails(scanner, hE, pet.getDateOfBirth(), true);
		} else {
		    System.out.println("No health events found.");
		    return;
		}
		hE.setCreatedBy(userId);
		HealthEventDAOI heDao = new HealthEventDAOI(con);
		heDao.updateHealthEvent(hE);
		System.out.println("Health event updated successfully!");
		return;
	}
	private static HealthEvent selectHealthEvent(Scanner scanner, List<HealthEvent> hes) {
		HealthEvent hE;
		while (true) {
			
			try {
				String inputStr = scanner.nextLine();
				if (inputStr.equals("\\"))
				{
					return null;
				}
				int input = Integer.parseInt(inputStr);
				
				if (input < 1 || input > hes.size()) {
					System.out.println("Invalid entry. Please enter a valid number from the list.");
					continue;
				}
			try {
				hE = hes.get(input-1);
				break;
			} catch (IndexOutOfBoundsException e) {
				System.out.println("Invalid entry. Please enter a valid number from the list.");
			}}
				catch (IllegalArgumentException e) {
			        System.out.println("Invalid entry. Please enter a valid number from the list.");
		    }
		}
		return hE;
	}
	public static void deleteHealthEvent(Scanner scanner, Connection con, Sesion ses) throws SQLException
	{
		List<Pet> list = new ArrayList<>();
		String rolename = ses.getRole().getName();
		if (rolename.equals("admin"))
		{
			list = PetController.listPets(con, true);
		} else if (rolename.equals("vet")) {
			list = PetController.listPetsByVS(con, ses);
		}
		Pet pet;
		HealthEvent hE = new HealthEvent();
		String petId, heId;
		
		if (list.size() > 1) {
			System.out.println("Please select the pet.");
			pet = PetController.selectPet(scanner, list);
			petId = pet.getId();
		}else if (list.size() == 1)
		{
			pet = list.get(0);
			System.out.println("Deleting health events for only pet: " + pet.getName());
			petId = pet.getId();
		}
		else {
			return;
		}
		
		List<HealthEvent> hes = viewHealthEventsByPet(scanner, con, petId);
		if (hes.size() >= 1) {
			System.out.println("Please select the event or \\ to skip.");
			hE = selectHealthEvent(scanner, hes);
			heId = hE.getId();
		} 
		else 
		{
			System.out.println("No health events found.");
			return; 
		}
			
		HealthEventDAOI heDao = new HealthEventDAOI(con);
		heDao.deleteHealthEvent(heId);
		System.out.println("Health event deleted successfully!");
		return;
	}
	public static void deleteHealthEventsByVs(Connection con, String vetStationId) throws SQLException
	{
		new HealthEventDAOI(con).deleteHealthEventsByVs(vetStationId);
	}
	
}
