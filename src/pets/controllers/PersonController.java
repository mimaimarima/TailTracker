package pets.controllers;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

import pets.model.Address;
import pets.model.CareCenter;
import pets.model.Legal;
import pets.model.Person;
import pets.model.Physical;
import pets.model.Sesion;
import pets.model.User;
import pets.model.VetStation;
import pets.model.enums.LegalType;
import pets.model.enums.PersonType;
import pets.repository.daoimpl.CareCenterDAOI;
import pets.repository.daoimpl.LegalDAOI;
import pets.repository.daoimpl.PersonDAOI;
import pets.repository.daoimpl.PetOwnershipDAOI;
import pets.repository.daoimpl.PhysicalDAOI;
import pets.repository.daoimpl.VetStationDAOI;

public class PersonController {
	private static void collectPersonFields(Scanner scanner, Person person, boolean allowSkip, PersonType personType, int send) {
	    if (personType.equals(PersonType.LEGAL)) {
	        while (true) {
	            System.out.print("Enter name" + (allowSkip ? " or \\ to skip" : "") + ": ");
	            String name = scanner.nextLine().trim();
	            if (allowSkip && name.equals("\\")) break;
	            try {
	                person.setName(name);
	                break;
	            } catch (IllegalArgumentException e) {
	                System.out.println(e.getMessage());
	            }
	        }
	    } else if (personType.equals(PersonType.PHYSICAL) && (!allowSkip || send == 0)) {
	        while (true) {
	            String origName = null, origSurname = null;

	            if (allowSkip && person.getName() != null && person.getName().contains(" ")) {
	                String[] parts = person.getName().split("\\s+", 2);
	                origName = parts[0];
	                origSurname = parts.length > 1 ? parts[1] : "";
	            }

	            System.out.print("Enter name" + (allowSkip ? " or \\ to skip" : "") + ": ");
	            String name = scanner.nextLine().trim();
	            if (allowSkip && name.equals("\\")) name = origName;

	            System.out.print("Enter surname" + (allowSkip ? " or \\ to skip" : "") + ": ");
	            String surname = scanner.nextLine().trim();
	            if (allowSkip && surname.equals("\\")) surname = origSurname;

	            try {
	                person.setName(name + " " + surname);
	                break;
	            } catch (IllegalArgumentException e) {
	                System.out.println(e.getMessage());
	            }
	        }
	    }

	  
	    while (true) {
	        System.out.print("Enter phone number" + (allowSkip ? " or \\ to skip" : "") + ": ");
	        String number = scanner.nextLine().trim();
	        if (allowSkip && number.equals("\\")) break;
	        try {
	            person.setNumber(number);
	            break;
	        } catch (IllegalArgumentException e) {
	            System.out.println(e.getMessage());
	        }
	    }

	  
	    while (true) {
	        System.out.print("Enter contact email" + (allowSkip ? " or \\ to skip" : "") + ": ");
	        String email = scanner.nextLine().trim();
	        if (allowSkip && email.equals("\\")) break;
	        try {
	            person.setEmail(email);
	            break;
	        } catch (IllegalArgumentException e) {
	            System.out.println(e.getMessage());
	        }
	    }
	}
	public static Person collectPersonData(Scanner scanner, Connection con, PersonType person_type, int send, String userId, String creatorId) throws SQLException {
	    Person person = new Person();

	    person.setUserId(userId);
	    person.setCreatedBy(creatorId);
	    person.setPersonType(person_type);
	    person.setStatus(true);

	    collectPersonFields(scanner, person, false, person_type, send);

	    Address adr = AddressController.addAddress(scanner, con, userId);
	    person.setAddressId(adr.getId());

	    return person;
	}
	public static Person createPerson(Scanner scanner, Connection con, int ownerType,
             boolean isAdmin, Sesion ses, User user) throws SQLException {
		
		boolean isPhysical = false, isLegal = false;
		if (isAdmin)
		{
		 while (true) {
             System.out.println("Are you registering a physical person? Y/N");
             String input = scanner.nextLine().trim().toUpperCase();
             if (input.equals("Y")) {
                 isPhysical = true;
                 break;
             } else if (input.equals("N")) {
                 isLegal = true;
                 break;
             } else {
                 System.out.println("Invalid input. Please enter Y or N.");
             }
             
         }
		 }else
		 {
			 isPhysical = true;
		 }
		Person person = new Person();
		PersonDAOI personDao = new PersonDAOI(con);
		String creatorId, userId;
		creatorId = ses.getUserId();
		if (!isAdmin && ownerType == 0)
		{
			userId = ses.getUserId();
		}else 
		{
		   // System.out.println(user.getId());

			userId = user.getId();
		}

		if (isPhysical) {
			person = PersonController.collectPersonData(scanner, con, PersonType.PHYSICAL, ownerType > 0 ? 1 : 0, userId, creatorId);
			person.setPersonType(PersonType.PHYSICAL);
			personDao.insert(person);
			
			Physical phys = PhysicalController.addPhysical(scanner, con);
			phys.setNameAndSurname(person.getName());
			phys.setPerson_id(person.getId());
			phys.setCreatedBy(creatorId);			
			PhysicalDAOI physDao = new PhysicalDAOI(con);
			physDao.insert(phys);
		} 
		if (isLegal){
			person = PersonController.collectPersonData(scanner, con, PersonType.LEGAL, 0, userId, creatorId);
			person.setPersonType(PersonType.LEGAL);
			personDao.insert(person);
			
			Legal leg = LegalController.addLegal(scanner, con);
			leg.setPerson_id(person.getId());
			leg.setVersion(1);
			leg.setCreatedBy(creatorId);
			boolean isVet = false, isCarer = false;
			while (true) {
	                System.out.println("Is it a vet station or care center? (VS/CC)");
	                String input = scanner.nextLine().trim().toUpperCase();
	                if (input.equals("VS")) {
	                    isVet = true;
	                    break;
	                } else if (input.equals("CC")) {
	                    isCarer = true;
	                    break;
	                } else {
	                    System.out.println("Invalid input.");
	                }
	            }
			if (isVet) {
				leg.setType(LegalType.VET_STATION);
				LegalDAOI legDao = new LegalDAOI(con);
				legDao.insert(leg);
				VetStation vet = VetStationController.addVetStation(scanner);
				vet.setPerson_id(person.getId());
				vet.setCreatedBy(creatorId);				
				VetStationDAOI vsDao = new VetStationDAOI(con);
				vsDao.insertVetStation(vet);
				vsDao.insertVetStationSpecialties(vet);
				vsDao.insertVetStationSpecies(vet);
			} 
			if (isCarer){
				leg.setType(LegalType.CARE_CENTER);
				LegalDAOI legDao = new LegalDAOI(con);
				legDao.insert(leg);
				CareCenter car = CareCenterController.addCareCenter(scanner);
				car.setId(person.getId());
				car.setCreatedBy(creatorId);
				CareCenterDAOI ccDao = new CareCenterDAOI(con);
				ccDao.insertCareCenterFacilities(car);
			}
		}
		return person;
}
	public static void updatePerson(Scanner scanner, Connection con, Sesion ses) throws SQLException
	{
		String rolename = ses.getRole().getName();
		String userId = ses.getUserId();
		Person person;
		    if (rolename.equals("admin")) {
		        List<Person> allPersons = getAllPersons(con);
		        if (allPersons.isEmpty()) {
		            System.out.println("No persons found.");
		            return;
		        }
		        System.out.println("Please select the person you wish to update or \\ to return.");
		        person = selectPerson(scanner, allPersons);
		        person.setCreatedBy(userId);
		    } else {
		        String personId = ses.getPersonId();
		         person = new PersonDAOI(con).getPerson(personId);
		    }

		    collectPersonFields(scanner, person, true, person.getPersonType(), 1);

		    if (person.getPersonType().equals(PersonType.LEGAL) && rolename.equals("admin")) {
		        LegalController.updateLegal(scanner, con, person);
		    }

		    Address adr = AddressController.updateAddress(scanner, con, person.getAddressId(), userId);
		    person.setAddressId(adr.getId());
		    person.setUserId(person.getUserId());
		    person.setCreatedBy(userId);

		    new PersonDAOI(con).updatePersonWithAddress(person);

		    if (person.getPersonType().equals(PersonType.PHYSICAL)) {
		        PhysicalDAOI phDao = new PhysicalDAOI(con);
		        Physical physical = phDao.getPhysicalById(person.getId());
		        String[] parts = person.getName().split("\\s+", 2);
		        physical.setName(parts[0]);
		        physical.setSurname(parts.length > 1 ? parts[1] : "");
		        physical.setCreatedBy(userId);
		        physical.setUserId(person.getUserId());
		        phDao.updatePhysical(physical);
		    }
	}
	public static List<Person> getAllPersons(Connection con) throws SQLException {
		PersonDAOI perDao = new PersonDAOI(con);
		List<Person> persons = perDao.getAllPersons();
		int i = 1;
		for (Person p : persons) {
			System.out.println(i + " " + p.getPersonType() + " " + p.getName() + " " + p.getEmail() + " " + p.getNumber());
			i++;
		}
		return persons;
	}
	public static void getPerson(Connection con, Sesion ses) throws SQLException
	{
		PersonDAOI perDao = new PersonDAOI(con);
		String personId = ses.getPersonId();
		Person p = perDao.getPerson(personId);
		System.out.println(p.getPersonType() + " " + p.getName() + " " + p.getEmail() + " " + p.getNumber());
		if (p.getPersonType().equals(PersonType.PHYSICAL))
		{
			PhysicalController.getPhysical(con, personId);
		} 
		if (p.getPersonType().equals(PersonType.LEGAL))
		{
			LegalController.getLegal(con, personId);
		}
		AddressController.viewAddress(con, p.getAddressId());
	}
	public static Person selectPerson(Scanner scanner, List<Person> list)
	{
		Person person;
		while (true) {
			
			try {
				String inputStr = scanner.nextLine();
				if (inputStr.equals("\\"))
				{
					return null;
				}
				int input = Integer.parseInt(inputStr);
				if (input < 1 || input > list.size()) {
					System.out.println("Invalid entry. Please enter a valid number from the list");
					continue;
				}
				try {
				person = list.get(input-1);
			
				break;
				}catch (IndexOutOfBoundsException e) {
					System.out.println("Invalid entry. Please enter a valid number from the list.");
					}
			} catch (IllegalArgumentException e) {
				System.out.println("Invalid entry. Please enter a valid number from the list");
			}
		}
		return person;
	}
	public static void deletePerson(Scanner scanner, Connection con) throws SQLException
	{
		System.out.println("Do you want to delete a physical (P), legal (L) person or \\ to return?");
		while (true) {
			try {
			String inputStr = scanner.nextLine();
			if (inputStr.equals("\\")) {
				return;
			}
			inputStr.toUpperCase();
			if (inputStr.equals("P")) {
				PhysicalController.deletePhysical(scanner, con);
				break;
			} else if (inputStr.equals("L"))
			{
				LegalController.deleteLegal(scanner, con);
				break;
			} else {
				System.out.println("Wrong input! Try again.");
				continue;
			}
		}catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }}
		return;
	} 
	public static void deletePersonData(Connection con, Person person) throws SQLException {
	    if (person.getPersonType() == PersonType.PHYSICAL) {
	        new PhysicalDAOI(con).deletePhysical(person.getId());
	        new PetOwnershipDAOI(con).deleteOwnershipByPhysicalId(person.getId());
	    } 
	    else if (person.getPersonType() == PersonType.LEGAL) {
	        LegalController.deleteLegalData(con, person.getId());
	    }
	}
}
