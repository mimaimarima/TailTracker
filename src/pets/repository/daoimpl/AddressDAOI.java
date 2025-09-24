package pets.repository.daoimpl;

import java.sql.*;
import java.util.UUID;

import pets.model.Address;
import pets.repository.dao.AddressDAO;

public class AddressDAOI implements AddressDAO {
    private final Connection connection;

    public AddressDAOI(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void insert(Address address) throws SQLException {

    	if (address.getId() == null || address.getId().isEmpty()) {
            address.setId(UUID.randomUUID().toString());
        }

        String sql = "INSERT INTO address (id, street, city, post_code, country, created_at, updated_at, created_by) " +
                     "VALUES (?::uuid, ?, ?, ?, ?, NOW(), TIMESTAMP '9999-12-31 23:59:59', ?::uuid) " +
                     "RETURNING id";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, address.getId());
            stmt.setString(2, address.getStreet());
            stmt.setString(3, address.getCity());
            stmt.setString(4, address.getPostCode());
            stmt.setString(5, address.getCountry());
            stmt.setString(6, address.getCreatedBy());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    address.setId(rs.getString("id")); 
                }
            }
        }
    }

    public Address getAddress(String addressId) throws SQLException {
	    	String sql = "SELECT a.* FROM address a " +
					 "WHERE a.id = ?::uuid";
		 try (PreparedStatement stmt = connection.prepareStatement(sql)) {
		        stmt.setString(1, addressId);
		        try (ResultSet rs = stmt.executeQuery()) {
		            if (rs.next()) {
		            	Address adr = new Address();
		            	adr.setCity(rs.getString("city"));
		            	adr.setCountry(rs.getString("country"));
		            	adr.setId(rs.getString("id"));
		            	adr.setPostCode(rs.getString("post_code"));
		            	adr.setStreet(rs.getString("street"));
		            	return adr;
		            }
		        }
		    }
		 return null;
    }
    public void updateAddress(Address address) throws SQLException {
        String checkSql = "SELECT street, city, post_code, country, version FROM address " +
                         "WHERE id = ?::uuid AND updated_at = TIMESTAMP '9999-12-31 23:59:59'";
        
        try (PreparedStatement stmt = connection.prepareStatement(checkSql)) {
            stmt.setString(1, address.getId());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    boolean valuesChanged = !rs.getString("street").equals(address.getStreet()) ||
                                          !rs.getString("city").equals(address.getCity()) ||
                                          !rs.getString("post_code").equals(address.getPostCode()) ||
                                          !rs.getString("country").equals(address.getCountry());
                    
                    if (!valuesChanged) {
                        System.out.println("Address values unchanged - no update needed");
                        return;
                    }
                    
                    int currentVersion = rs.getInt("version");
                    
                    String closeOldVersionSql = "UPDATE address SET updated_at = NOW() " +
                                             "WHERE id = ?::uuid AND version = ?";
                    try (PreparedStatement closeStmt = connection.prepareStatement(closeOldVersionSql)) {
                        closeStmt.setString(1, address.getId());
                        closeStmt.setInt(2, currentVersion);
                        closeStmt.executeUpdate();
                    }
                    
                    String insertSql = "INSERT INTO address (id, street, city, post_code, country, version, " +
                                     "created_at, updated_at, created_by) " +
                                     "VALUES (?::uuid, ?, ?, ?, ?, ?, NOW(), TIMESTAMP '9999-12-31 23:59:59', ?::uuid)";
                    try (PreparedStatement insertStmt = connection.prepareStatement(insertSql)) {
                        insertStmt.setString(1, address.getId());
                        insertStmt.setString(2, address.getStreet());
                        insertStmt.setString(3, address.getCity());
                        insertStmt.setString(4, address.getPostCode());
                        insertStmt.setString(5, address.getCountry());
                        insertStmt.setInt(6, currentVersion + 1);
                        insertStmt.setString(7, address.getCreatedBy());
                        insertStmt.executeUpdate();
                    }
                } else {
                    throw new SQLException("No current version found for address with ID: " + address.getId());
                }
            }
        }
    }

}