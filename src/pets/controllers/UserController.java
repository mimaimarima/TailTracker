package pets.controllers;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;


import pets.model.CareCenter;
import pets.model.Legal;
import pets.model.Person;
import pets.model.Physical;
import pets.model.Sesion;
import pets.model.User;
import pets.model.UserRole;
import pets.model.VetStation;
import pets.model.Role;
import pets.model.enums.LegalType;
import pets.model.enums.PersonType;
import pets.repository.dao.RoleDAO;
import pets.repository.dao.UserRoleDAO;
import pets.repository.daoimpl.CareCenterDAOI;
import pets.repository.daoimpl.LegalDAOI;
import pets.repository.daoimpl.PersonDAOI;
import pets.repository.daoimpl.PhysicalDAOI;
import pets.repository.daoimpl.RoleDAOI;
import pets.repository.daoimpl.UserDAOI;
import pets.repository.daoimpl.UserRoleDAOI;
import pets.repository.daoimpl.VetStationDAOI;
import pets.services.PasswordHash;

public class UserController {
	public static User collectUserData(Scanner scanner, Connection con) throws SQLException {

		User user = new User();
		UserDAOI check = new UserDAOI(con);
		while (true) {

			System.out.print("Enter username: ");
			String username = scanner.nextLine();
			User existingUser = check.getUserByUsername(username);

			if (existingUser == null) {
				try {
					user.setUsername(username);
					break;
				} catch (IllegalArgumentException e) {
					System.out.println(e.getMessage());
				}
			} else {
				System.out.println("Username '" + username + "' is not available.");
			}
		}

		while (true) {

			System.out.print("Enter password: ");
			String password = scanner.nextLine();

			if (!password.matches("^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[#?!@$%^&*-]).{8,}$")) {
				System.out.println("Password must contain at least eight characters, "
						+ "at least one number and both lower and uppercase letters and special characters");
				continue;
			}
			String hashedPassword = PasswordHash.hashPassword(password);
			user.setPassword(hashedPassword);
			break;
		}

		while (true) {
			System.out.print("Enter name: ");
			String name = scanner.nextLine();
			if (name.matches(".*\\d.*")) {
		        System.out.println("No digits allowed in name.");
		        continue;
		    }
			try {
				user.setName(name);
				break;
			} catch (IllegalArgumentException e) {
				System.out.println(e.getMessage());
			}
		}

		while (true) {
			System.out.print("Enter surname: ");
			String surname = scanner.nextLine();
			try {
				user.setSurname(surname);
				break;
			} catch (IllegalArgumentException e) {
				System.out.println(e.getMessage());
			}
		}
		while (true) {
			System.out.print("Enter email: ");
			String email = scanner.nextLine();
	//		User existingUser = check.getUserByEmail(email);

				try {
					user.setEmail(email);
					break;
				} catch (IllegalArgumentException e) {
					System.out.println(e.getMessage());
				}
			}

		return user;
	}

	public static User addUser(Scanner scanner, Connection con, boolean isAdmin, int ownerType, Sesion ses) throws SQLException {

	    String roleName = "";
	    Person person;
	    try {
	        con.setAutoCommit(false);
	        
	        User user = collectUserData(scanner, con);

	        UserDAOI userDao = new UserDAOI(con);
	        userDao.insertUser(user);

	        if (ses.getUserId() == null)
	        {
	        	ses.setUserId(user.getId());
	        }
	        person = PersonController.createPerson(scanner, con, ownerType, isAdmin, ses, user);
	       
	        if (person.getPersonType().equals(PersonType.PHYSICAL)) {
	            roleName = determineUserRoleName(person, scanner, con, ownerType);
	        } else {
	            roleName = determineUserRoleName(person, scanner, con, 0); 
	        }
	        addUserRole(con, user.getId(), roleName);

	        con.commit();
	        System.out.println("User registered successfully with all information!");
	        return user;
	    } catch (Exception e) {
	        if (con != null) {
	            con.rollback();
	        }
	        System.out.println("Registration failed. All changes have been rolled back: " + e.getMessage());
	        throw e;
	    } finally {
	        con.setAutoCommit(true);
	    }
	}
	private static String determineUserRoleName(Person person, Scanner scanner, Connection con, int ownerType) throws SQLException {
	    if (person != null && person.getPersonType().equals(PersonType.LEGAL)) {
	        LegalDAOI legDao = new LegalDAOI(con);
	        Legal leg = legDao.getLegal(person.getId());
	        if (leg.getType().equals(LegalType.VET_STATION)) {
	            return "vet";
	        }
	        if (leg.getType().equals(LegalType.CARE_CENTER)) {
	            return "carer";
	        }
	    } 
	    else {
	        if (ownerType == 0) {
	            while (true) {
	                System.out.println("Type 1 or 2, according to your need:\n1: Pet Owner\n2: Pet Adopter");
	                String choice = scanner.nextLine();
	                if (choice.equals("1")) {
	                    return "petowner";
	                } else if (choice.equals("2")) {
	                    return "petadopter";
	                } else {
	                    System.out.println("Invalid input.");
	                }
	            }
	        } else if (ownerType == 1) {
	            return "petowner";
	        } else if (ownerType == 2) {
	            return "petadopter";
	        }
	    }
	    throw new IllegalStateException("Could not determine role name");
	}

	private static void addUserRole(Connection con, String userId, String roleName) throws SQLException {

		RoleDAO roleDAO = new RoleDAOI(con);
		String roleId = roleDAO.getIdByName(roleName);

		UserRole userRole = new UserRole(userId, roleId, LocalDate.now(), null);

		UserRoleDAO userRoleDAO = new UserRoleDAOI(con);
		userRoleDAO.insertUserRole(userRole);
	}
	public static Map<User,String> getAllUsersWithRoleName(Connection con, String rolename) throws SQLException {
		UserDAOI userDao = new UserDAOI(con);
		Map<User,String> usersWithRoleNames = userDao.getAllUsersWithRoles(rolename);
		int i = 1;
		if (!usersWithRoleNames.isEmpty()) {
			for (Map.Entry<User, String> entry : usersWithRoleNames.entrySet()) {
			    User user = entry.getKey();
			    System.out.println(i +": User: " + user.getUsername() + " " +
                        " | Role: " + entry.getValue() + " " + user.getName() + " " + user.getSurname() + " " + user.getEmail());
			    i++;
		}
			return usersWithRoleNames;
		}
		else {
		    System.out.println("No users found.");
		 return null;   
		}
}
	public static void updateUser(Scanner scanner, Connection con, Sesion ses) throws SQLException {
		UserDAOI userDao = new UserDAOI(con);
		User user = new User();
		String rolename = ses.getRole().getName();
		String userId = ses.getUserId();
		if (rolename.equals("admin"))
		{
			user = selectUser(scanner, con, null);
		} else {
			user = userDao.getUserById(userId);
		}
		while (true) {
		    System.out.print("Enter new username or \\ to skip: ");
		    String username = scanner.nextLine();
		    if (username.equals("\\")) {
		        break; 
		    }
		    User existingUser = userDao.getUserByUsername(username);
		    if (existingUser == null) {
		        try {
		            user.setUsername(username); 
		            break;
		        } catch (IllegalArgumentException e) {
		            System.out.println(e.getMessage());
		        }
		    } else {
		        System.out.println("Username '" + username + "' is not available.");
		    }
		}

		while (true) {

			System.out.print("Enter password or \\ to skip: ");
			String password = scanner.nextLine();
			if (password.equals("\\")) {
				break;
			}
			if (!password.matches("^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[#?!@$%^&*-]).{8,}$")) {
				System.out.println("Password must contain at least eight characters, "
						+ "at least one number and both lower and uppercase letters and special characters");
				continue;
			}
			String hashedPassword = PasswordHash.hashPassword(password);
			user.setPassword(hashedPassword);
			break;
		}

		while (true) {
			System.out.print("Enter name or \\ to skip: ");
			String name = scanner.nextLine();
			if (name.equals("\\")) {
				break;
			}
			if (name.matches(".*\\d.*")) {
		        System.out.println("No digits allowed in name.");
		        continue;
		    }
			try {
				user.setName(name);
				break;
			} catch (IllegalArgumentException e) {
				System.out.println(e.getMessage());
			}
		}

		while (true) {
			System.out.print("Enter surname or to skip \\: ");
			String surname = scanner.nextLine();
			if (surname.equals("\\")) {
				break;
			}
			try {
				user.setSurname(surname);
				break;
			} catch (IllegalArgumentException e) {
				System.out.println(e.getMessage());
			}
		}
		while (true) {
			System.out.print("Enter email or \\ to skip: ");
			String email = scanner.nextLine();
			if (email.equals("\\")) {
				break;
			}
			User existingUser = userDao.getUserByEmail(email);
			if (existingUser == null) {
				try {
					user.setEmail(email);
					break;
				} catch (IllegalArgumentException e) {
					System.out.println(e.getMessage());
				}
			} else {
				System.out.println("Email '" + email + "' is taken.");
			}
		}
		
		user = userDao.updateUser(user);
		
		System.out.println("User updated.");
		return;		
		
	}
	
	public static void deleteUserAdmin(Scanner scanner, Connection con) throws SQLException {
		
		User user = selectUser(scanner, con, null);
		deleteUser(con, user.getId());
		System.out.println("User deleted.");
		return;
	}
	
	public static boolean deleteUser(Connection con, String userId) throws SQLException {
		
	    boolean originalAutoCommit = con.getAutoCommit();
	    con.setAutoCommit(false); 

	    try {
	      
	        Person person = new PersonDAOI(con).findPersonByUserId(userId);
	        if (person != null) {
	            
	            PersonController.deletePersonData(con, person);
	            
	            
	            new PersonDAOI(con).deleteByUserId(userId);
	        }

	       
	        new UserRoleDAOI(con).deleteByUserId(userId);

	       
	        new UserDAOI(con).deleteUser(userId);

	        con.commit(); 
	        return true;

	    } catch (SQLException e) {
	    	con.rollback(); 
	        throw e; 
	    } finally {
	    	con.setAutoCommit(originalAutoCommit); 
	    	System.out.println("Account deleted.");
	    	 ConsoleApp.main(null);
	    }
	   
	}
	
	public static User selectUser(Scanner scanner, Connection con, String rolename) throws SQLException
	{
		UserController.getAllUsersWithRoleName(con, rolename);
		UserDAOI userDao = new UserDAOI(con);
		User user = new User();
		while (true) {
			try {
			String usernameOwner= scanner.nextLine().trim();
		    user = userDao.getUserByUsername(usernameOwner);
		    if (user == null) {
				System.out.print("Not found. Enter username you wish to edit or \\ to return");
		    	continue;
		    } else {
		    	break;
		    }
			}catch (IllegalArgumentException e) {
	            System.out.println(e.getMessage());
	        }
		    
		}
		return user;
	}
	public static void viewUser(Scanner scanner, Connection con, Sesion ses) throws SQLException
	{
		UserDAOI uDao = new UserDAOI(con);
		User user = uDao.getUserById(ses.getUserId());
		System.out.println("User: " + user.getName() + " " + user.getSurname() + " " + user.getUsername());
		viewAllRoles(scanner,con,ses);
	}
	public static void viewAllRoles(Scanner scanner, Connection con, Sesion ses) throws SQLException
	{

		String userId = ses.getUserId();
		
		UserRoleDAOI urDao = new UserRoleDAOI(con);
		RoleDAOI rDao = new RoleDAOI(con);
		List<String> list = urDao.getAllUserRoles(userId);
		for (String ur : list)
		{
			Role r = rDao.getById(ur);
			System.out.println(r.getName());
		}
	}

	public static void addNewRole(Scanner scanner, Connection con, Sesion ses) throws SQLException {
	    String currentUserRole = ses.getRole().getName();
	    String userId;
	    boolean isAdmin = currentUserRole.equals("admin");
	    
	    if (isAdmin) {
	        User user = selectUser(scanner, con, null);
	        if (user == null) {
	            System.out.println("No user selected. Operation cancelled.");
	            return;
	        }
	        userId = user.getId();
	    } else {
	        userId = ses.getUserId();
	    }

	    
	    RoleDAO rDao = new RoleDAOI(con);
	    UserRoleDAO urDao = new UserRoleDAOI(con);
	    
	    List<Role> allRoles = rDao.getAllRoles();
	    List<String> userRoles = urDao.getAllUserRoles(userId);
	    
	    List<Role> availableRoles = new ArrayList<>();
	    for (Role r : allRoles) {
	        if (userRoles.contains(r.getId())) {
	            continue;
	        }
	        
	        if (isAdmin) {
	            availableRoles.add(r);
	        } 
	        else if (r.getName().equals("petowner") || r.getName().equals("petadopter")) {
	            availableRoles.add(r);
	        }
	    }

	    if (availableRoles.isEmpty()) {
	        System.out.println("No available roles to add for this user.");
	        return;
	    }

	    System.out.println("\nAvailable roles to add:");
	    for (int i = 0; i < availableRoles.size(); i++) {
	        System.out.printf("%d. %s\n", i + 1, availableRoles.get(i).getName());
	    }
	    System.out.println("0. Cancel");

	    while (true) {
	        System.out.print("Select a role to add (or 0 to cancel): ");
	        String inputStr = scanner.nextLine().trim();
	        
	        try {
	            int input = Integer.parseInt(inputStr);
	            
	            if (input == 0) {
	                System.out.println("Operation cancelled.");
	                return;
	            }
	            
	            if (input < 1 || input > availableRoles.size()) {
	                System.out.println("Please enter a number between 1 and " + availableRoles.size());
	                continue;
	            }
	            
	            Role selectedRole = availableRoles.get(input - 1);
	            
	            String roleName = selectedRole.getName();
	            con.setAutoCommit(false);
	            try {
	                addUserRole(con, userId, selectedRole.getName());
	                
	                if (roleName.equals("vet") || roleName.equals("carer")) {
	                	
	                    
	                    Person person = PersonController.collectPersonData(scanner, con, PersonType.LEGAL, 0, userId, userId);
	                    person.setUserId(userId);
	                    
	                    PersonDAOI personDao = new PersonDAOI(con);
	                    personDao.insert(person);
	                    Legal leg = LegalController.addLegal(scanner, con);
	                    leg.setPerson_id(person.getId());
	                    leg.setType(roleName.equals("vet") ? LegalType.VET_STATION : LegalType.CARE_CENTER);
	                    leg.setCreatedBy(userId);
	                    
	                    LegalDAOI legDao = new LegalDAOI(con);
	                    legDao.insert(leg);
	                    
	                    if (roleName.equals("vet")) {
	                        VetStation vet = VetStationController.addVetStation(scanner);
	                        vet.setPerson_id(leg.getPerson_id());
	                        vet.setCreatedBy(userId);
	                        
	                        VetStationDAOI vsDao = new VetStationDAOI(con);
	                        vsDao.insertVetStation(vet);
	                        vsDao.insertVetStationSpecialties(vet);
	                        vsDao.insertVetStationSpecies(vet);
	                    } else {
	                        CareCenter car = CareCenterController.addCareCenter(scanner);
	                        car.setPerson_id(leg.getPerson_id());
	                        car.setCreatedBy(userId);
	                        
	                        CareCenterDAOI ccDao = new CareCenterDAOI(con);
	                        ccDao.insertCareCenterFacilities(car);
	                    }

	                } 
	                else if (roleName.equals("petowner") || roleName.equals("petadopter")) {
	                    int ownerType = roleName.equals("petowner") ? 1 : 2;
	                    Person person = PersonController.collectPersonData(scanner, con, PersonType.PHYSICAL, ownerType, userId, userId);
	                    person.setUserId(userId);
	                    
	                    PersonDAOI personDao = new PersonDAOI(con);
	                    personDao.insert(person);
	                    
	                    Physical phys = PhysicalController.addPhysical(scanner, con);
	                    phys.setPerson_id(person.getId());
	                    phys.setCreatedBy(userId);
	                    phys.setNameAndSurname(person.getName());
	                    PhysicalDAOI physDao = new PhysicalDAOI(con);
	                    physDao.insert(phys);
	                }
	                
	                con.commit();
	                System.out.println("Successfully added role: " + selectedRole.getName() + " with all associated data!");
	            } catch (Exception e) {
	                con.rollback();
	                System.out.println("Error adding role and associated data: " + e.getMessage());
	                throw e;
	            } finally {
	                con.setAutoCommit(true);
	            }
	            return;
	            
	        } catch (NumberFormatException e) {
	            System.out.println("Please enter a valid number.");
	        } catch (Exception e) {
	            System.out.println("Error adding role: " + e.getMessage());
	            return;
	        }
	    }
	}

	
	public static void deleteUserRole(Scanner scanner, Connection con, Sesion ses) throws SQLException {
	    Map<User, String> all = getAllUsersWithRoleName(con, null);
	    if (all.isEmpty()) {
	        System.out.println("No user roles found.");
	        return;
	    }

	    UserRoleDAOI urDao = new UserRoleDAOI(con);
	    
	    while (true) {
	        System.out.print("Enter user role number to delete or \\ to return: ");
	        String inputStr = scanner.nextLine().trim();
	        
	        if (inputStr.equals("\\")) {
	            return; 
	        }
	        
	        try {
	            int input = Integer.parseInt(inputStr);
	            if (input < 1 || input > all.size()) {
	                System.out.println("Invalid selection. Please enter a number between 1 and " + all.size());
	                continue;
	            }
	            
	            int currentIndex = 1;
	            User selectedUser = null;
	            String roleName = null;
	            for (Map.Entry<User, String> entry : all.entrySet()) {
	                if (currentIndex == input) {
	                    selectedUser = entry.getKey();
	                    roleName = entry.getValue();
	                    break;
	                }
	                currentIndex++;
	            }
	            
	            String roleId = new RoleDAOI(con).getIdByName(roleName);
	            if (roleId == null) {
	                System.out.println("Error: Role not found.");
	                return;
	            }
	            
	            UserRole ur = new UserRole();
	            ur.setUserId(selectedUser.getId());
	            ur.setRoleId(roleId);
	            
	            urDao.deleteUserRole(ur);
	                System.out.println("User role deleted successfully.");
	            
	            return;
	            
	        } catch (NumberFormatException e) {
	            System.out.println("Please enter a valid number or \\ to return.");
	        }
	    }
	}
}