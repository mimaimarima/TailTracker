package pets.controllers;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import pets.model.Address;
import pets.model.Event;
import pets.model.EventParticipation;
import pets.model.Pet;
import pets.model.Sesion;
import pets.model.enums.EventType;
import pets.repository.daoimpl.EventDAOI;
import pets.repository.daoimpl.PetDAOI;

public class EventController {
	public static Event collectEventFields(Scanner scanner, Event event, boolean allowSkip, List<EventType> eventTypes) {

	    while (true) {
	        System.out.println("Event Types:");
	        for (int i = 0; i < eventTypes.size(); i++) {
	            System.out.println((i + 1) + ": " + eventTypes.get(i).name());
	        }
	        System.out.print("Enter Event Type" + (allowSkip ? " or \\ to keep current (" + event.getEventType() + ")" : "") + ": ");

	        String input = scanner.nextLine().trim();

	        if (allowSkip && input.equals("\\")) {
	            break;  
	        }

	        try {
	            int choice = Integer.parseInt(input);
	            if (choice < 1 || choice > eventTypes.size()) {
	                System.out.println("Invalid entry: number must be between 1 and " + eventTypes.size() + ".");
	                continue;
	            }
	            event.setEventType(eventTypes.get(choice - 1));
	            break;
	        } catch (NumberFormatException e) {
	            System.out.println("Invalid input. Please enter a number.");
	        }
	    }


	    LocalDate date = event.getDateTime() != null ? event.getDateTime().toLocalDate() : null;
	    while (true) {
	        System.out.print("Enter date (YYYY-MM-DD)" + (allowSkip && date != null ? " or \\ to keep current (" + date + ")" : "") + ": ");
	        String input = scanner.nextLine().trim();
	        if (allowSkip && input.equals("\\") && date != null) {
	            break;
	        }
	        try {
	            date = LocalDate.parse(input);

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


	    LocalTime time = event.getDateTime() != null ? event.getDateTime().toLocalTime() : null;
	    while (true) {
	        System.out.print("Enter time (HH:MM)" + (allowSkip && time != null ? " or \\ to keep current (" + time + ")" : "") + ": ");
	        String input = scanner.nextLine().trim();
	        if (allowSkip && input.equals("\\") && time != null) {
	            break;
	        }
	        try {
	            time = LocalTime.parse(input);
	            break;
	        } catch (DateTimeParseException e) {
	            System.out.println("Invalid time format. Please use HH:MM.");
	        }
	    }

	    event.setDateTime(LocalDateTime.of(date, time));


	    while (true) {
	        System.out.print("Enter event name" + (allowSkip && event.getName() != null ? " or \\ to keep current (" + event.getName() + ")" : "") + ": ");
	        String nameInput = scanner.nextLine().trim();
	        if (allowSkip && nameInput.equals("\\")) {
	            break;
	        }
	        try {
	            event.setName(nameInput);
	            break;
	        } catch (IllegalArgumentException e) {
	            System.out.println(e.getMessage());
	        }
	    }

	    return event;
	}

	public static void registerEvent(Scanner scanner, Connection con, Sesion ses) throws SQLException {
		
		Event ev = new Event();
	    List<EventType> eventTypes = Arrays.asList(EventType.values());
	    String userId = ses.getUserId();
	    collectEventFields(scanner, ev, false, eventTypes);

	    Address adr = AddressController.addAddress(scanner, con, userId);

	    ev.setCreatedBy(userId);
	    ev.setAddressId(adr.getId());

	    EventDAOI eventDao = new EventDAOI(con);
	    eventDao.insert(ev);
	    System.out.println("Event registered successfully!");
		
	}
	public static void registerEventParticipation(Scanner scanner, Connection con, Sesion ses) throws SQLException
	{
		List<Pet> list1;
		String personId;
		String rolename = ses.getRole().getName();
		String userId = ses.getUserId();
		if (rolename.equals("admin"))
		{
			list1 = PetController.listPets(con, true);
		}else {
			personId = ses.getPersonId();
			list1 = PetController.listPetsByOwner(scanner, con, ses);

		}
		EventDAOI eDao = new EventDAOI(con);
		EventParticipation ep = new EventParticipation();
		ep.setCreatedBy(userId);
		Pet pet;
		String petId = null;
		
		if (list1.size()>1)
		{	System.out.println("Please select the pet.");
			pet = PetController.selectPet(scanner, list1);
			petId = pet.getId();
			ep.setPetId(petId);
			
		}else if (list1.size() == 1)
		{
			pet = list1.get(0);
			petId = pet.getId();
			ep.setPetId(pet.getId());
			System.out.println ("Registering " + pet.getName() + " for event.");
		} else
		{
			System.out.println("No pets found.");
			return;
		}
		
		List<Event> list2 = listEventsWithoutPet(con, petId);

		if (list2.size() > 0) {
			System.out.println("Please select the event.");
			Event event = selectEvent(scanner, list2);
			String eventId = event.getId();
			ep.setEventId(eventId);

			while (true) {
				System.out.print("Enter reward or \\ if none: ");
				String reward = scanner.nextLine();
				if (reward.equalsIgnoreCase("\\") || reward.isEmpty()) {
					ep.setReward(null);				
					break;
				}       
				try {
					ep.setReward(reward);
					break;
				} catch (IllegalArgumentException e) {
					System.out.println(e.getMessage());
				}
			}
		
			eDao.insertEventParticipation(ep);
			System.out.println("Event participation registered successfully!");		
		} else
		{
			System.out.println("No events found!");		
		}			
	}
	private static Event selectEvent(Scanner scanner, List<Event> list) {
		Event event;
		while (true) {
			
			try {
				String inputStr = scanner.nextLine();
				int input = Integer.parseInt(inputStr);
				if (input < 1 || input > list.size()) {
					System.out.println("Invalid entry. Please enter a valid number from the list");
					continue;
				}
				try {
				event = list.get(input-1);
				break;
				}catch (IndexOutOfBoundsException e) {
					System.out.println("Invalid entry. Please enter a valid number from the list.");
					}
			} catch (IllegalArgumentException e) {
				System.out.println("Invalid entry. Please enter a valid number from the list");
			}
		}
		return event;
	}
	private static List<Event> listEventsWithoutPet(Connection con, String petId) throws SQLException {
		EventDAOI evDao = new EventDAOI(con);
		List<Event> list = evDao.getEventsWithoutPet(petId);
		int i = 1;
		for (Event e : list)
		{
			System.out.println(i++ + ": " + e.getName() + " " + e.getEventType() + " " + e.getDateTime());
		}		
		return list;
	}
	public static List<Event> listEventsWithPet(Connection con, String petId) throws SQLException {
		
		
		EventDAOI evDao = new EventDAOI(con);
		List<Event> list = evDao.getEventsWithPet(petId);
		int i = 1;
		if (list.size()>=1) {
		for (Event e : list)
		{
			System.out.println(i++ + ": " + e.getName() + " " + e.getEventType() + " " + e.getDateTime());
		}	}
		else {
			System.out.println("No events found with pet.");
		}
		return list;
	}
	public static void viewEventsWithPet(Scanner scanner, Connection con, Sesion ses) throws SQLException
	{
		List<Pet> pets = new ArrayList<>();
		if (ses.getRole().getName().equals("admin"))
		{
			pets = PetController.listPets(con, true);
		}
		else if (ses.getRole().getName().equals("petowner"))
		{
			pets = PetController.listPetsByOwner(scanner, con, ses);
		}
		Pet pet;
		String petId = "";
		if (pets.size()>1)
		{ System.out.println("Please select the pet");
			pet = PetController.selectPet(scanner, pets);
			petId = pet.getId();
		}else if (pets.size() == 1) {
			pet = pets.get(0);
			petId = pet.getId();
		}else {
			return;
		}
		listEventsWithPet(con, petId);
		return;
	}
	public static List<Event> listEvents(Connection con) throws SQLException
	{
		EventDAOI evDao = new EventDAOI(con);
		List<Event> list = evDao.getAllEvents();
		int i = 1;
		for (Event e : list)
		{
			System.out.println(i++ + ": " + e.getName() + " " + e.getEventType() + " " + e.getDateTime());
		}
		return list;
	}
	public static void deleteEvent(Scanner scanner, Connection con) throws SQLException {
		EventDAOI eDao = new EventDAOI(con);
		System.out.println("Registered events:");
		List<Event> list = listEvents(con);
		
		if (list.size() > 0) {
			System.out.println("Please select the event you wish to delete.");
			Event event = selectEvent(scanner, list);
			String eventId = event.getId();
			eDao.deleteEvent(eventId);
			eDao.deleteEventParticipationByEventId(eventId);
			System.out.println("Event deleted!");
			return;	
		}
		else {
			System.out.println("No events found.");
			return;
		}
	
	}
	public static void updateEvent(Scanner scanner, Connection con, String userId) throws SQLException {
	    EventDAOI eDao = new EventDAOI(con);
	    List<Event> list = eDao.getAllEvents();

	    if (list.isEmpty()) {
	        System.out.println("No events to update.");
	        return;
	    }

	    System.out.println("Registered events:");
	    for (int i = 0; i < list.size(); i++) {
	        Event e = list.get(i);
	        System.out.println((i + 1) + ": " + e.getName() + " " + e.getEventType() + " " + e.getDateTime());
	    }

	    System.out.println("Please select the event you wish to update.");
	    Event event = selectEvent(scanner, list);

	    List<EventType> eventTypes = Arrays.asList(EventType.values());

	    collectEventFields(scanner, event, true, eventTypes);

	    AddressController.updateAddress(scanner, con, event.getAddressId(), userId);

	    event.setCreatedBy(userId);
	    eDao.update(event);
	    System.out.println("Event updated successfully!");

		
		
	}
	public static void updateEventParticipation(Scanner scanner, Connection con, Sesion ses) throws SQLException {
	    List<Pet> list1;
	    String userId = ses.getUserId();
		String rolename = ses.getRole().getName();
	    if (rolename.equals("admin")) {
	        list1 = PetController.listPets(con, true);
	    } else {

			String personId = ses.getPersonId();
	        list1 = PetController.listPetsByOwner(scanner, con, ses);
	    }

	    if (list1.isEmpty()) {
	        System.out.println("No pets found.");
	        return;
	    }

	    Pet pet;
	    if (list1.size() == 1) {
	        pet = list1.get(0);
	        System.out.println("Updating " + pet.getName() + "'s event participations.");
	    } else {
	        System.out.println("Please select the pet.");
	        pet = PetController.selectPet(scanner, list1);
	    }

	    String petId = pet.getId();
	    List<Event> list2 = listEventsWithPet(con, petId);

	    if (list2.isEmpty()) {
	        System.out.println("No events found!");
	        return;
	    }

	    System.out.println("Please select the event.");
	    Event event = selectEvent(scanner, list2);
	    String eventId = event.getId();

	    EventDAOI eDao = new EventDAOI(con);

	    EventParticipation ep = eDao.getEventParticipation(petId, eventId);
	    if (ep == null) {
	        System.out.println("No current event participation found for this event.");
	        return;
	    }

	    System.out.println("Current reward: " + (ep.getReward() != null ? ep.getReward() : "none"));
	    while (true) {
	        System.out.print("Enter new reward: ");
	        String reward = scanner.nextLine().trim();
	        
	        if (reward.isEmpty() || reward.equals("\\")) {
	            ep.setReward("\\");
	            break;

	        }
	        
	        try {
	            ep.setReward(reward);
	            break;
	        } catch (IllegalArgumentException e) {
	            System.out.println("Invalid reward: " + e.getMessage());
	        }
	    }
	    ep.setCreatedBy(userId);
	    eDao.updateEventParticipation(ep);
	    System.out.println("Event participation updated successfully!");
	}

	public static void deleteEventParticipation(Scanner scanner, Connection con, Sesion ses) throws SQLException {
	    List<Pet> pets;
	    String rolename = ses.getRole().getName();
	    if (rolename.equals("admin")) {
	        pets = PetController.listPets(con, true);
	    } else {
	        pets = PetController.listPetsByOwner(scanner, con, ses);
	    }

	    if (pets.isEmpty()) {
	        System.out.println("No pets found.");
	        return;
	    }

	    Pet selectedPet;
	    if (pets.size() == 1) {
	        selectedPet = pets.get(0);
	        System.out.println("Using only pet: " + selectedPet.getName());
	    } else {
	        System.out.println("Select a pet:");
	        selectedPet = PetController.selectPet(scanner, pets);
	    }

	    String petId = selectedPet.getId();
	    List<Event> events = listEventsWithPet(con, petId);
	    if (events.isEmpty()) {
	        System.out.println("No events found for selected pet.");
	        return;
	    }

	    System.out.println("Select an event:");
	    Event selectedEvent = selectEvent(scanner, events);
	    String eventId = selectedEvent.getId();

	    EventDAOI eventDao = new EventDAOI(con);
	    EventParticipation ep = eventDao.getEventParticipation(petId, eventId);
	    if (ep == null) {
	        System.out.println("No active event participation found.");
	        return;
	    }

	    eventDao.deleteEventParticipation(ep);
	    System.out.println("Event participation deleted successfully!");
	}

	
	
}
