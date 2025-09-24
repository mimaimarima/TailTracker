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

import pets.model.Activity;
import pets.model.Pet;
import pets.model.Sesion;
import pets.model.enums.ActivityType;
import pets.model.enums.Facilities;
import pets.repository.daoimpl.ActivityDAOI;
import pets.repository.daoimpl.CareCenterDAOI;

public class ActivityController {
	public static void collectActivityFields(
		    Scanner scanner, 
		    Activity a, 
		    Pet selectedPet, 
		    boolean allowSkip
		) {
		    LocalDate dateStart = a.getDateTimeStart() != null ? a.getDateTimeStart().toLocalDate() : null;
		    LocalDate dateEnd = a.getDateTimeEnd() != null ? a.getDateTimeEnd().toLocalDate() : null;
		    LocalTime timeStart = a.getDateTimeStart() != null ? a.getDateTimeStart().toLocalTime() : null;
		    LocalTime timeEnd = a.getDateTimeEnd() != null ? a.getDateTimeEnd().toLocalTime() : null;
		    LocalDate petDOB = selectedPet.getDateOfBirth();

		    // Start Date
		    while (true) {
		        System.out.print("Enter start date (YYYY-MM-DD)" + (allowSkip ? " or \\ to skip" : "") + ": ");
		        String input = scanner.nextLine().trim();
		        if (allowSkip && input.equals("\\")) break;

		        try {
		            dateStart = LocalDate.parse(input);
		            if (dateStart.isBefore(petDOB)) {
		                System.out.println("Date cannot be before pet's birthday.");
		                continue;
		            }
		            if (dateStart.isBefore(LocalDate.now().minusYears(30))) {
		                System.out.println("Date cannot be more than 30 years in the past.");
		                continue;
		            }
		            if (dateStart.isAfter(LocalDate.now())) {
		                System.out.println("Date cannot be in the future.");
		                continue;
		            }
		            break;
		        } catch (DateTimeParseException e) {
		            System.out.println("Invalid date format.");
		        }
		    }

		    // Start Time
		    while (true) {
		        System.out.print("Enter start time (HH:MM)" + (allowSkip ? " or \\ to skip" : "") + ": ");
		        String input = scanner.nextLine().trim();
		        if (allowSkip && input.equals("\\")) break;

		        try {
		            timeStart = LocalTime.parse(input);
		            break;
		        } catch (DateTimeParseException e) {
		            System.out.println("Invalid time format.");
		        }
		    }

		    if (dateStart != null && timeStart != null)
		        a.setDateTimeStart(LocalDateTime.of(dateStart, timeStart));

		    // End Date
		    if (a.getTypeA() == ActivityType.DAILY_STAY) {
		        while (true) {
		            System.out.print("Enter end date (YYYY-MM-DD)" + (allowSkip ? " or \\ to skip" : "") + ": ");
		            String input = scanner.nextLine().trim();
		            if (allowSkip && input.equals("\\")) break;

		            try {
		                dateEnd = LocalDate.parse(input);
		                if (dateEnd.isBefore(dateStart)) {
		                    System.out.println("End date cannot be before start date.");
		                    continue;
		                }
		                if (dateEnd.isAfter(dateStart.plusDays(31))) {
		                    System.out.println("Daily Stay cannot exceed 31 days.");
		                    continue;
		                }
		                break;
		            } catch (DateTimeParseException e) {
		                System.out.println("Invalid date format.");
		            }
		        }
		    } else {
		        dateEnd = dateStart;
		    }

		    // End Time
		    while (true) {
		        System.out.print("Enter end time (HH:MM)" + (allowSkip ? " or \\ to skip" : "") + ": ");
		        String input = scanner.nextLine().trim();
		        if (allowSkip && input.equals("\\")) break;

		        try {
		            timeEnd = LocalTime.parse(input);
		            if (dateStart != null && dateEnd != null && dateStart.equals(dateEnd) &&
		                timeStart != null && timeEnd.isBefore(timeStart)) {
		                System.out.println("End time cannot be before start time.");
		                continue;
		            }
		            break;
		        } catch (DateTimeParseException e) {
		            System.out.println("Invalid time format.");
		        }
		    }

		    if (dateEnd != null && timeEnd != null)
		        a.setDateTimeEnd(LocalDateTime.of(dateEnd, timeEnd));

		    // Description
		    while (true) {
		        System.out.print("Enter description" + (allowSkip ? " or \\ to skip" : "") + ": ");
		        String input = scanner.nextLine();
		        if (allowSkip && input.equals("\\")) break;

		        try {
		            a.setDescription(input);
		            break;
		        } catch (IllegalArgumentException e) {
		            System.out.println(e.getMessage());
		        }
		    }

		    // Recommendation
		    while (true) {
		        System.out.print("Enter recommendation" + (allowSkip ? " or \\ to skip" : "") + ": ");
		        String input = scanner.nextLine();
		        if (allowSkip && input.equals("\\")) break;

		        try {
		            a.setRecommendation(input);
		            break;
		        } catch (IllegalArgumentException e) {
		            System.out.println(e.getMessage());
		        }
		    }

		    // Price
		    while (true) {
		        System.out.print("Enter price" + (allowSkip ? " or \\ to skip" : "") + ": ");
		        String input = scanner.nextLine().trim();
		        if (allowSkip && input.equals("\\")) break;

		        if (input.isEmpty() || input.equalsIgnoreCase("null")) {
		            System.out.println("Price cannot be empty.");
		            continue;
		        }

		        try {
		            BigDecimal price = new BigDecimal(input);
		            if (price.compareTo(BigDecimal.ZERO) < 0) {
		                System.out.println("Price cannot be negative.");
		                continue;
		            }
		            a.setPrice(price);
		            break;
		        } catch (NumberFormatException e) {
		            System.out.println("Invalid number format.");
		        } catch (IllegalArgumentException e) {
		            System.out.println(e.getMessage());
		        }
		    }
		}

	public static void registerActivity(Scanner scanner, Connection con, Sesion ses) throws SQLException {
		
		ActivityDAOI aDao = new ActivityDAOI(con);
		
		Activity a = new Activity();
	
		String personId;
		String rolename = ses.getRole().getName();
		String userId = ses.getUserId();
		if (rolename.equals("admin"))
		{
			personId = CareCenterController.selectCareCenter(scanner, con);
		} else {
			personId = ses.getPersonId();
		}
		
		List<ActivityType> list = listFacilitiesByCC(con, personId);
		
		if (list.size()>=1) {
		System.out.println("Enter Activity Type. Enter a number from the list.");
		}
		else {
			System.out.println("Please enter facilities first.");
			return;
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
				ActivityType ac = list.get(input-1);
				a.setTypeA(ac);
				break;
			} catch (IndexOutOfBoundsException e) {
				System.out.println("Invalid entry. Please enter a valid number from the list.");
			}}
				catch (IllegalArgumentException e) {
			        System.out.println("Invalid entry. Please enter a valid number from the list.");
		    }
		}
		
		
		List<Pet> list1 = PetController.listPets(con, false);
		if (list1.size()== 0)
		{
			System.out.println("No pets found.");
			return;
		}
	
		System.out.println("Select the pet. Enter a number from the list.");


		Pet selectedPet = PetController.selectPet(scanner, list1);
		a.setPetId(selectedPet.getId());
		collectActivityFields(scanner, a, selectedPet, false);
		a.setCreatedBy(userId);
		a.setCareCenterId(personId);
		aDao.insert(a);

		
		System.out.println("Activity registered successfully!");
		return;
	}
	private static List<ActivityType> listFacilitiesByCC(Connection con, String careCenterId) throws SQLException {
		CareCenterDAOI cDao = new CareCenterDAOI(con);
		List<Facilities> list = cDao.getCareCenterFacilities(careCenterId);
		
		List<ActivityType> ret = new ArrayList<>();
		
		for (Facilities f : list)
		{
			if (f.equals(Facilities.DAILY_STAY)) {
				ret.add(ActivityType.DAILY_STAY);
			}
			if (f.equals(Facilities.GROOMING_ROOM)) {
				ret.add(ActivityType.GROOMING);
			}
			if (f.equals(Facilities.TRAINING)) {
				ret.add(ActivityType.TRAINING);
			}
		}
		int i = 1;
		for (ActivityType at : ret)
		{
			System.out.println(i + ": " + at);
			i++;
		}
		return ret;
	}
	public static void viewActivitiesByPet(Scanner scanner, Connection con, Sesion ses) throws SQLException {
		String personId;
		String rolename = ses.getRole().getName();
		
		if (rolename.equals("admin"))
		{
			personId = CareCenterController.selectCareCenter(scanner,con);
		} else {
			personId = ses.getPersonId();
		}
		
		List<Pet> pets = PetController.listPetsByCC(con, personId);
		Pet pet;
		if (pets.size() > 1) {
			System.out.println("Select a pet:");
			pet = PetController.selectPet(scanner, pets);

		} else if (pets.size() == 1)
		{
			pet = pets.get(0);
		} else {
			System.out.println("No pets found.");
			return;
		}
		String petId = pet.getId();
		ActivityDAOI aDao = new ActivityDAOI(con);
		List<Activity> acts = aDao.getActivitiesByPet(petId);
		
		if (acts.size()>=1) {
			System.out.println("Activities in which " + pet.getName() + " " + pet.getPetType() + " " + pet.getBreed() + " has participated in:");
	
			for (Activity a : acts)
			{
				System.out.println(a.getTypeA() + " " + a.getDescription() + "\n" + a.getDateTimeStart() + " - " + a.getDateTimeEnd());
			}
		}else
		{
			System.out.println("No activities found for: " + pet.getName());
			}
	}
	public static Map<Pet,List<Activity>> listActivitiesByCareCenter(Scanner scanner, Connection con, Sesion ses) throws SQLException {
	String personId;

	String rolename = ses.getRole().getName();
		if (rolename.equals("admin"))
		{
			personId = CareCenterController.selectCareCenter(scanner, con);
		} else {
			personId = ses.getPersonId();
			
		}
		
		ActivityDAOI aDao = new ActivityDAOI(con);
		Map<Pet,List<Activity>> petActivities = aDao.getActivitiesByCareCenter(personId);
		if (!petActivities.isEmpty()) {
			for (Map.Entry<Pet, List<Activity>> entry : petActivities.entrySet()) {
			    Pet pet = entry.getKey();
			    System.out.println("\nPet: " + pet.getName() + " (" + pet.getPetType().toString().toLowerCase() + ")");
			    
			    for (Activity activity : entry.getValue()) {
			        System.out.println("-" + activity.getTypeA() + ": " + activity.getDescription() + " " + activity.getDateTimeStart() + " - " + activity.getDateTimeEnd() + " " + activity.getPrice());;
			    }
		}}
		else {
		    System.out.println("No activities found.");
		    }
	return petActivities;
	}
	public static void updateActivity(Scanner scanner, Connection con, Sesion ses) throws SQLException
	{			

		String rolename = ses.getRole().getName();
		String personId;
		String userId = ses.getUserId();
		if (rolename.equals("admin"))
		{
			System.out.println("Select the Care Center.");
			personId = CareCenterController.selectCareCenter(scanner, con);
		} else {
			personId = ses.getPersonId();
		}
		
		
		Map<Pet, List<Activity>> petActivities = listActivitiesByCareCenter(scanner, con, ses);
		
		List<Pet> pets = new ArrayList<>(petActivities.keySet());

		if (pets.isEmpty()) {
			System.out.println("No pets or activities found for this care center.");
			return;
		}

		System.out.println("Select a pet:");

		Pet selectedPet = PetController.selectPet(scanner, pets);

		List<Activity> activities = petActivities.get(selectedPet);
		if (activities == null || activities.isEmpty()) {
			System.out.println("No activities available for selected pet.");
			return;
		}

		System.out.println("Select an activity:");
		for (int i = 0; i < activities.size(); i++) {
			Activity act = activities.get(i);
			System.out.printf("%d: %s - %s to %s%n", i + 1,
				act.getTypeA(),
				act.getDateTimeStart(),
				act.getDateTimeEnd());
		}

		Activity a = null;
		while (true) {
			try {
				String inputStr = scanner.nextLine().trim();
				int input = Integer.parseInt(inputStr);
				if (input < 1 || input > activities.size()) {
					System.out.println("Invalid choice. Try again.");
					continue;
				}
				a = activities.get(input - 1);
				break;
			} catch (NumberFormatException e) {
				System.out.println("Enter a valid number.");
			}
		}


		collectActivityFields(scanner, a, selectedPet, true);
		ActivityDAOI actDao = new ActivityDAOI(con);
		a.setCareCenterId(personId);
		a.setPetId(selectedPet.getId());
		a.setCreatedBy(userId);
		actDao.update(a);
		System.out.println("Activity updated!");
		return;
	}
	public static void deleteActivity(Scanner scanner, Connection con, Sesion ses) throws SQLException
	{

		String personId;
		String rolename = ses.getRole().getName();
		if (rolename.equals("admin")) {	
			personId = CareCenterController.selectCareCenter(scanner, con);
		}else {
			personId = ses.getPersonId();
		}
		
		Map<Pet, List<Activity>> petActivities = listActivitiesByCareCenter(scanner, con, ses);
		
		List<Pet> pets = new ArrayList<>(petActivities.keySet());

		if (pets.isEmpty()) {
			System.out.println("No pets or activities found for this care center.");
			return;
		}

		System.out.println("Select a pet:");

		Pet selectedPet = PetController.selectPet(scanner, pets);

		List<Activity> activities = petActivities.get(selectedPet);
		if (activities == null || activities.isEmpty()) {
			System.out.println("No activities available for selected pet.");
			return;
		}

		System.out.println("Select an activity:");
		for (int i = 0; i < activities.size(); i++) {
			Activity act = activities.get(i);
			System.out.printf("%d: %s - %s to %s%n", i + 1,
				act.getTypeA(),
				act.getDateTimeStart(),
				act.getDateTimeEnd());
		}

		Activity a = null;
		while (true) {
			try {
				String inputStr = scanner.nextLine().trim();
				int input = Integer.parseInt(inputStr);
				if (input < 1 || input > activities.size()) {
					System.out.println("Invalid choice. Try again.");
					continue;
				}
				a = activities.get(input - 1);
				break;
			} catch (NumberFormatException e) {
				System.out.println("Enter a valid number.");
			}
		}
		
		ActivityDAOI actDao = new ActivityDAOI(con);
		actDao.delete(a.getId());
		System.out.println("Activity deleted!");
		return;
	}
	public static void deleteActivitiesByCC(Connection con, String careCenterId) throws SQLException
	{
		new ActivityDAOI(con).deleteActivitiesByCC(careCenterId);
	}
}
