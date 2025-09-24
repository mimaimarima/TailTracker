package pets.controllers;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Scanner;

import pets.model.Address;
import pets.repository.dao.AddressDAO;
import pets.repository.daoimpl.AddressDAOI;

public class AddressController {
	private static void collectAddressData(Scanner scanner, Address adr, boolean allowSkip) {

	    while (true) {
	        System.out.print("Enter street and home number" + (allowSkip ? " or \\ to skip" : "") + ": ");
	        String input = scanner.nextLine().trim();
	        if (allowSkip && input.equals("\\")) {
	            System.out.println("Street is the same.");
	            break;
	        }
	        try {
	            adr.setStreet(input);
	            break;
	        } catch (IllegalArgumentException e) {
	            System.out.println("Error: " + e.getMessage());
	        }
	    }

	
	    while (true) {
	        System.out.print("Enter city" + (allowSkip ? " or \\ to skip" : "") + ": ");
	        String input = scanner.nextLine().trim();
	        if (allowSkip && input.equals("\\")) {
	            System.out.println("City is the same.");
	            break;
	        }
	        try {
	            adr.setCity(input);
	            break;
	        } catch (IllegalArgumentException e) {
	            System.out.println("Error: " + e.getMessage());
	        }
	    }


	    while (true) {
	        System.out.print("Enter post code" + (allowSkip ? " or \\ to skip" : "") + ": ");
	        String input = scanner.nextLine().trim();
	        if (allowSkip && input.equals("\\")) {
	            System.out.println("Post code is the same.");
	            break;
	        }
	        try {
	            adr.setPostCode(input);
	            break;
	        } catch (IllegalArgumentException e) {
	            System.out.println("Error: " + e.getMessage());
	        }
	    }


	    while (true) {
	        System.out.print("Enter country" + (allowSkip ? " or \\ to skip" : "") + ": ");
	        String input = scanner.nextLine().trim();
	        if (allowSkip && input.equals("\\")) {
	            System.out.println("Country is the same.");
	            break;
	        }
	        try {
	            adr.setCountry(input);
	            break;
	        } catch (IllegalArgumentException e) {
	            System.out.println("Error: " + e.getMessage());
	        }
	    }
	}
	public static Address addAddress(Scanner scanner, Connection con, String userId) throws SQLException {
	    Address adr = new Address();
	    collectAddressData(scanner, adr, false);  
	    adr.setCreatedBy(userId);

	    try {
	        AddressDAO addressDao = new AddressDAOI(con);
	        addressDao.insert(adr);
	        return adr;
	    } catch (SQLException e) {
	        System.out.println("Failed to insert address: " + e.getMessage());
	        throw e;
	    }
	}
	public static Address updateAddress(Scanner scanner, Connection con, String addressId, String userId) throws SQLException {
	    AddressDAOI addressDao = new AddressDAOI(con);
	    Address adr = addressDao.getAddress(addressId);

	    collectAddressData(scanner, adr, true); 
	    adr.setCreatedBy(userId);

	    addressDao.updateAddress(adr);
	    return adr;
	}
	public static void viewAddress(Connection con, String addressId) throws SQLException
	{
		AddressDAOI addressDao = new AddressDAOI(con);
		Address adr = addressDao.getAddress(addressId);
		System.out.println("Address: " + adr.getStreet() + " " + adr.getCity() + " " + adr.getPostCode() + " " + adr.getCountry());
	}
	
}
