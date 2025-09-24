package pets.controllers;
import java.io.Console;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import pets.config.DatabaseConnection;
import pets.model.Person;
import pets.model.Role;
import pets.model.Sesion;
import pets.model.User;
import pets.model.enums.PersonType;
import pets.repository.dao.RoleDAO;
import pets.repository.dao.UserDAO;
import pets.repository.dao.UserRoleDAO;
import pets.repository.daoimpl.PersonDAOI;
import pets.repository.daoimpl.RoleDAOI;
import pets.repository.daoimpl.UserDAOI;
import pets.repository.daoimpl.UserRoleDAOI;
import pets.services.PasswordHash;

public class PanelController {
	public static void loginUser(Scanner scanner) throws SQLException {
		Connection con = DatabaseConnection.getConnection();

		System.out.print("Enter username: ");
		String username = scanner.nextLine();

		String password;
		Console console = System.console();
		if (console != null) {
			char[] passwordChars = console.readPassword("Enter password: ");
			password = new String(passwordChars);
		} else {
			System.out.print("Enter password: ");
			password = scanner.nextLine();
		}

		UserDAO u = new UserDAOI(con);
		User user = u.getUserByUsername(username);

		try {

			if (user == null) {
				System.out.println("Invalid credentials.");
				return;
			}

			boolean passwordMatch = PasswordHash.checkPassword(password, user.getPassword());

			if (passwordMatch) {

				System.out.println("Login successful! Welcome, " + user.getName() + " " + user.getSurname());
				Sesion ses = new Sesion();
				ses.setUserId(user.getId());
				ses.setStart(LocalDateTime.now());
				switchUserRole(scanner, con, ses);

			} else {
				System.out.println("Invalid credentials.");
			}
		} finally {
		}
	}
	private static void switchUserRole(Scanner scanner, Connection con, Sesion ses) throws SQLException {
	    UserRoleDAO urd = new UserRoleDAOI(con);
	    RoleDAO rod = new RoleDAOI(con);
	    PersonDAOI pd = new PersonDAOI(con);
	    String userId = ses.getUserId();

	    List<String> roleIds = urd.getRoleIdForUserId(userId);
	    
	    if (roleIds.isEmpty()) {
	        System.out.println("No roles found for this user.");
	        return;
	    }

	    Map<Role, List<Person>> rolePersonsMap = new LinkedHashMap<>();
	    for (String roleId : roleIds) {
	        Role role = rod.getById(roleId);
	        List<Person> persons = pd.findByUserIdAndRole(userId, roleId);


	        List<Person> filteredPersons = new ArrayList<>();
	        for (Person person : persons) {
	            if ((role.isPhysical() && person.getPersonType() == PersonType.PHYSICAL) || 
	                (role.isLegal() && person.getPersonType() == PersonType.LEGAL)) {
	                filteredPersons.add(person);
	            }
	            if ((role.isAdmin()))
	            {
	            	filteredPersons.add(person);
	            }
	        }
	        if (!filteredPersons.isEmpty()) {
	            rolePersonsMap.put(role, filteredPersons);
	        }
	    }

	    if (rolePersonsMap.isEmpty()) {
	        System.out.println("No valid person records found for any of the user's roles.");
	        return;
	    }

	    if (rolePersonsMap.size() == 1 && rolePersonsMap.values().iterator().next().size() == 1) {
	        Map.Entry<Role, List<Person>> entry = rolePersonsMap.entrySet().iterator().next();
	        ses.setRole(entry.getKey());
	        ses.setPersonId(entry.getValue().get(0).getId());
	        redirectToPanel(scanner, con, ses);
	        return;
	    }

	    while (true) {
	        try {
	            System.out.println("Available roles:");
	            List<Role> roles = new ArrayList<>(rolePersonsMap.keySet());
	            for (int i = 0; i < roles.size(); i++) {
	
	                System.out.printf("%d. %s %n",  i, roles.get(i).getName());
	            }

	            System.out.print("Enter number for role you want: ");
	            int roleChoice = Integer.parseInt(scanner.nextLine());

	            if (roleChoice < 0 || roleChoice >= roles.size()) {
	                System.out.println("Invalid role choice. Please try again.");
	                continue;
	            }

	            Role selectedRole = roles.get(roleChoice);
	            List<Person> persons = rolePersonsMap.get(selectedRole);
	            
	            if (persons.size() == 1) {
	                ses.setRole(selectedRole);
	                ses.setPersonId(persons.get(0).getId());
	                redirectToPanel(scanner, con, ses);
	                return;
	            }
	            
	            while (true) {
	                System.out.println("Available persons for " + selectedRole.getName() + ":");
	                for (int i = 0; i < persons.size(); i++) {
	                    System.out.printf("%d. %s%n", i, persons.get(i).getName());
	                }
	                
	                System.out.print("Enter number for person you want: ");
	                try {
	                    int personChoice = Integer.parseInt(scanner.nextLine());
	                    
	                    if (personChoice >= 0 && personChoice < persons.size()) {
	                        ses.setRole(selectedRole);
	                        ses.setPersonId(persons.get(personChoice).getId());
	                        redirectToPanel(scanner, con, ses);
	                        return;
	                    } else {
	                        System.out.println("Invalid person choice. Please try again.");
	                    }
	                } catch (NumberFormatException e) {
	                    System.out.println("Invalid input. Please enter a number.");
	                }
	            }
	        } catch (NumberFormatException e) {
	            System.out.println("Invalid input. Please enter a number.");
	        }
	    }
	    
	}
	public static void redirectToPanel(Scanner scanner, Connection con, Sesion ses) throws SQLException
	{
		switch ((ses.getRole().getName()).toLowerCase()) {
		case "admin" -> adminPanel(scanner, con, ses);
		case "petowner" -> petOwnerPanel(scanner, con, ses);
		case "petadopter" -> petAdopterPanel(scanner, con, ses);
		case "vet" -> vetStationPanel(scanner, con, ses);
		case "carer" -> carerPanel(scanner, con, ses);
		default -> System.out.println("Unknown role: " + ses);
		}
	}
	public static void carerPanel(Scanner scanner, Connection con, Sesion ses) throws SQLException {
	    Map<String, PanelAction> carerActions = new LinkedHashMap<>();
	    carerActions.put("RP", (s,c,u) -> PetController.registerPet(s,c,ses));
	    carerActions.put("UP", (s,c,u) -> PetController.updatePet(s,c,ses));
	    carerActions.put("RA", (s,c,u) -> ActivityController.registerActivity(s,c,ses));
	    carerActions.put("UA", (s,c,u) -> ActivityController.updateActivity(s,c,ses));
	    carerActions.put("DA", (s,c,u) -> ActivityController.deleteActivity(s,c,ses));
	    carerActions.put("CA",  (s,c,u) -> ActivityController.listActivitiesByCareCenter(s,c,ses));
	    carerActions.put("PA", (s,c,u) -> ActivityController.viewActivitiesByPet(s,c,ses));
	    carerActions.put("VMP", (s,c,u) -> PersonController.getPerson(c, ses));
	    carerActions.put("UPI", (s,c,u) -> PersonController.updatePerson(s,c,ses));
	    carerActions.put("UCF", (s,c,u) -> CareCenterController.updateCareCenterFacilities(s,c,ses));
	    carerActions.put("DCF", (s,c,u) -> CareCenterController.deleteCareCenterFacilities(s,c,ses));
	    carerActions.put("ELI",(s,c,u) -> UserController.updateUser(s,c,ses));
	    carerActions.put("DU", (s,c,u) -> UserController.deleteUser(c, ses.getUserId()));
	    carerActions.put("AUR",  (s,c,u)-> UserController.addNewRole(scanner, con, ses));
	    carerActions.put("SUR", (s,c,u) -> switchUserRole(scanner, con, ses));

	    PanelUtils.showPanel(scanner, con, ses, "Welcome Carer!\nYour options are:", carerActions);
	}
	private static void vetStationPanel(Scanner scanner, Connection con, Sesion ses) throws SQLException {
		Map<String, PanelAction> vetActions = new LinkedHashMap<>();
		vetActions.put("RP", (s,c,u) -> PetController.registerPet(s,c,ses));
		vetActions.put("UP", (s,c,u) -> PetController.updatePet(s,c,ses));
		vetActions.put("VP", (s,c,u) -> PetController.listPetsByVS(con, ses));
		vetActions.put("RHE", (s,c,u) -> HealthEventController.registerHealthEvent(s,c,ses));
		vetActions.put("UHE", (s,c,u) -> HealthEventController.updateHealthEvent(s,c,ses));
		vetActions.put("DHE", (s,c,u) -> HealthEventController.deleteHealthEvent(s,c,ses));
		vetActions.put("VHE", (s,c,u) -> HealthEventController.viewHealthEventsByVS(s,c,ses));
	//	vetActions.put("VEH", (s,c,u) -> HealthEventController.verifyHealthEventsVS(s, c, ses.getPersonId()));
		vetActions.put("UEA", (s,c,u) -> VetStationController.updateEmergencyAvailability(s,c,ses));
		vetActions.put("UVS", (s,c,u) -> VetStationController.updateVetStationSpecies(s,c,ses));
		vetActions.put("UVL", (s,c,u) -> VetStationController.updateVetStationSpecialties(s,c,ses));
		vetActions.put("DVS", (s,c,u) -> VetStationController.deleteVetStationSpecies(s,c,ses));
		vetActions.put("DVL", (s,c,u) -> VetStationController.deleteVetStationSpecialties(s,c,ses));
		vetActions.put("VMP", (s,c,u) -> PersonController.getPerson(c, ses));
		vetActions.put("UPI", (s,c,u) -> PersonController.updatePerson(s,c,ses));
		vetActions.put("ELI", (s,c,u) -> UserController.updateUser(s,c,ses));
		vetActions.put("DU",  (s,c,u) -> UserController.deleteUser(c,ses.getUserId()));
		vetActions.put("AUR",  (s,c,u)-> UserController.addNewRole(scanner, con, ses));
		vetActions.put("SUR", (s,c,u) -> switchUserRole(scanner, con, ses));

		PanelUtils.showPanel(scanner, con, ses, "Welcome Vet!\nYour options are:", vetActions);
	}
	private static void petAdopterPanel(Scanner scanner, Connection con, Sesion ses) throws SQLException {

		Map<String, PanelAction> adopterActions = new LinkedHashMap<>();
		adopterActions.put("RP", (s,c,u) -> PetController.registerPet(s,c,ses));
		adopterActions.put("UP", (s,c,u) -> PetController.updatePet(s,c,ses));
		adopterActions.put("VP", (s,c,u) -> PetController.listPetsByOwner(s, c, ses));
		adopterActions.put("RHE", (s,c,u) -> HealthEventController.registerHealthEvent(s,c,ses));
		adopterActions.put("VHP", (s,c,u) -> HealthEventController.viewHEPET(s,c,ses));
		adopterActions.put("UHE", (s,c,u) -> HealthEventController.updateHealthEvent(s,c,ses));
		adopterActions.put("VMP", (s,c,u) -> PersonController.getPerson(c, ses));
		adopterActions.put("UPI", (s,c,u) -> PersonController.updatePerson(s,c,ses));
		adopterActions.put("ELI", (s,c,u) -> UserController.updateUser(s,c,ses));
		adopterActions.put("DU",  (s,c,u) -> UserController.deleteUser(c,ses.getUserId()));
		adopterActions.put("AUR",  (s,c,u)-> UserController.addNewRole(scanner, con, ses));
		adopterActions.put("SUR", (s,c,u) -> switchUserRole(scanner, con, ses));

		PanelUtils.showPanel(scanner, con, ses, "Welcome Pet Adopter!\nYour options are:", adopterActions);

	}
	private static void petOwnerPanel(Scanner scanner, Connection con, Sesion ses) throws SQLException {
		Map<String, PanelAction> ownerActions = new LinkedHashMap<>();
		ownerActions.put("RP", (s,c,u) -> PetController.registerPet(s,c,ses));
		ownerActions.put("UP", (s,c,u) -> PetController.updatePet(s,c,ses));
		ownerActions.put("VP", (s,c,u) -> PetController.listPetsByOwner(s, c, ses));
		ownerActions.put("RHE", (s,c,u) -> HealthEventController.registerHealthEvent(s,c,ses));
		ownerActions.put("VHP", (s,c,u) -> HealthEventController.viewHEPET(s,c,ses));
		ownerActions.put("UHE", (s,c,u) -> HealthEventController.updateHealthEvent(s,c,ses));
		ownerActions.put("REP",  (s,c,u) -> EventController.registerEventParticipation(s, c, ses));
		ownerActions.put("UEP",  (s,c,u) -> EventController.updateEventParticipation(s, c, ses));
		ownerActions.put("VEP",  (s,c,u) -> EventController.viewEventsWithPet(s, c, ses));
		ownerActions.put("DEP",  (s,c,u) -> EventController.deleteEventParticipation(s, c, ses));
		ownerActions.put("PPP",  (s,c,u) -> PassportController.printPassport(s, c, ses));
		ownerActions.put("VMP", (s,c,u) -> PersonController.getPerson(c, ses));
		ownerActions.put("UPI", (s,c,u) -> PersonController.updatePerson(s,c,ses));
		ownerActions.put("ELI", (s,c,u) -> UserController.updateUser(s,c,ses));
		ownerActions.put("DU",  (s,c,u) -> UserController.deleteUser(c,ses.getUserId()));
		ownerActions.put("VUR",(s,c,u) -> UserController.viewUser(scanner, con, ses));
		ownerActions.put("AUR",  (s,c,u)-> UserController.addNewRole(scanner, con, ses));
		ownerActions.put("SUR", (s,c,u) -> switchUserRole(scanner, con, ses));
		PanelUtils.showPanel(scanner, con, ses, "Welcome Pet Owner!\nYour options are:", ownerActions);
	}
	private static void adminPanel(Scanner scanner, Connection con, Sesion ses) throws SQLException {
		
		Map<String, PanelAction> adminActions = new LinkedHashMap<>();
/*		adminActions.put("UU", (s,c,u) -> UserController.updateUser(s,c,ses));
		adminActions.put("DU",  (s,c,u) -> UserController.deleteUser(c,ses.getUserId()));
		adminActions.put("VMP", (s,c,u) -> PersonController.getPerson(c, ses));
		adminActions.put("UMP", (s,c,u) -> PersonController.updatePerson(s,c,ses)); */
		adminActions.put("RUA",(s,c,u) -> UserController.addUser(scanner, con, true, 0, ses));
		adminActions.put("VUA", (s,c,u) -> UserController.getAllUsersWithRoleName(con, null));
		adminActions.put("ELI", (s,c,u) -> UserController.updateUser(s,c,ses));
		adminActions.put("DUA", (s,c,u) -> UserController.deleteUserAdmin(s,c));
		adminActions.put("AP", (s,c,u) -> PersonController.createPerson(s,c,0,true,ses,null));
		adminActions.put("VPI", (s,c,u) -> PersonController.getAllPersons(c));
		adminActions.put("UPI", (s,c,u) -> PersonController.updatePerson(s,c,ses));
		adminActions.put("DPI", (s,c,u) -> PersonController.deletePerson(s,c));
		adminActions.put("RP", (s,c,u) -> PetController.registerPet(s,c,ses));
		adminActions.put("VP", (s,c,u) -> PetController.listPets(con, true));
		adminActions.put("UP", (s,c,u) -> PetController.updatePet(s,c,ses));
		adminActions.put("DP", (s,c,u) -> PetController.deletePet(s,c));
		adminActions.put("RHE", (s,c,u) -> HealthEventController.registerHealthEvent(s,c,ses));
		adminActions.put("VHE", (s,c,u) -> HealthEventController.viewHealthEventsByVS(s,c,ses));
		adminActions.put("UHE", (s,c,u) -> HealthEventController.updateHealthEvent(s,c,ses));
		adminActions.put("DHE", (s,c,u) -> HealthEventController.deleteHealthEvent(s,c,ses));
		adminActions.put("UEA", (s,c,u) -> VetStationController.updateEmergencyAvailability(s,c,ses));
		adminActions.put("UVS", (s,c,u) -> VetStationController.updateVetStationSpecies(s,c,ses));
		adminActions.put("DVS", (s,c,u) -> VetStationController.deleteVetStationSpecies(s,c,ses));
		adminActions.put("UVL", (s,c,u) -> VetStationController.updateVetStationSpecialties(s,c,ses));
		adminActions.put("DVL", (s,c,u) -> VetStationController.deleteVetStationSpecialties(s,c,ses));
		adminActions.put("UCF", (s,c,u) -> CareCenterController.updateCareCenterFacilities(s,c,ses));
		adminActions.put("DCF", (s,c,u) -> CareCenterController.deleteCareCenterFacilities(s,c,ses));
		adminActions.put("RA", (s,c,u) -> ActivityController.registerActivity(s,c,ses));
		adminActions.put("UA", (s,c,u) -> ActivityController.updateActivity(s,c,ses));
		adminActions.put("DA", (s,c,u) -> ActivityController.deleteActivity(s,c,ses));
		adminActions.put("CA",  (s,c,u) -> ActivityController.listActivitiesByCareCenter(s,c,ses));
		adminActions.put("PA", (s,c,u) -> ActivityController.viewActivitiesByPet(s,c,ses));
		adminActions.put("RPP",  (s,c,u) -> PassportController.registerPassport(s,c,ses));
		adminActions.put("PPP", (s,c,u) -> PassportController.printPassport(s, c, ses));
		adminActions.put("UPP",  (s,c,u) -> PassportController.updatePassport(s,c,ses));
		adminActions.put("DPP",  (s,c,u) -> PassportController.deletePassport(s,c));
		adminActions.put("REV",  (s,c,u) -> EventController.registerEvent(s,c,ses));
		adminActions.put("VEV",  (s,c,u) -> EventController.listEvents(c));
		adminActions.put("UEV",  (s,c,u) -> EventController.updateEvent(s,c,u));
		adminActions.put("DEV",  (s,c,u) -> EventController.deleteEvent(s,c));		
		adminActions.put("REP",  (s,c,u) -> EventController.registerEventParticipation(s, c, ses));
		adminActions.put("UEP",  (s,c,u) -> EventController.updateEventParticipation(s, c, ses));
		adminActions.put("VEP",  (s,c,u) -> EventController.viewEventsWithPet(s, c, ses));
		adminActions.put("DEP",  (s,c,u) -> EventController.deleteEventParticipation(s, c, ses));
		adminActions.put("AUR",  (s,c,u)-> UserController.addNewRole(s, c, ses));
		adminActions.put("DUR", (s,c,u)-> UserController.deleteUserRole(s, c, ses));
		
		PanelUtils.showPanel(scanner, con, ses, "Welcome Admin!\nYour options are:", adminActions);

	}
}
