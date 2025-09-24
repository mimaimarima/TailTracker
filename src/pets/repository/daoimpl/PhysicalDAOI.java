package pets.repository.daoimpl;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import pets.model.Physical;
import pets.model.enums.Gender;
import pets.repository.dao.PhysicalDAO;

public class PhysicalDAOI implements PhysicalDAO {
    private Connection connection;

    public PhysicalDAOI(Connection connection) {
        this.connection = connection;
    }

    public void insert(Physical physical) throws SQLException {
    
        String insertSql = """
            INSERT INTO physical (
                physical_id, version,
                ssn, date_of_birth, gender, name, surname, created_at, updated_at, created_by
            ) VALUES (?::uuid, 1, ?, ?, ?::gender, ?, ?, NOW(), TIMESTAMP '9999-12-31 23:59:59', ?::uuid)
        """;
        try (PreparedStatement stmt = connection.prepareStatement(insertSql)) {
            stmt.setString(1, physical.getPerson_id());
            stmt.setString(2, physical.getSsn());
            stmt.setDate(3, Date.valueOf(physical.getDateOfBirth()));
            stmt.setString(4, physical.getGender().name());
            stmt.setString(5, physical.getName());
            stmt.setString(6, physical.getSurname());
            stmt.setString(7, physical.getCreatedBy());
            stmt.executeUpdate();
        }

    }

    public Physical getPhysicalById(String physicalId) throws SQLException {
        String sql = """
                SELECT p.* FROM physical p
                WHERE p.physical_id = ?::uuid AND updated_at = TIMESTAMP '9999-12-31 23:59:59' 
                """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, physicalId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Physical physical = new Physical();
                    physical.setPerson_id(rs.getString("physical_id"));
                    physical.setVersion(rs.getInt("version"));
                    physical.setName(rs.getString("name"));
                    physical.setSurname(rs.getString("surname"));
                    physical.setDateOfBirth(rs.getDate("date_of_birth").toLocalDate());
                    physical.setSsn(rs.getString("ssn"));
                    physical.setGender(Gender.valueOf(rs.getString("gender"))); 
                    return physical;
                }
            }
        }
        return null;
    }

    public Physical getPhysicalBySSN(String ssn) throws SQLException {
        String sql = """
                SELECT p.* FROM physical p
                WHERE p.ssn = ? AND updated_at = TIMESTAMP '9999-12-31 23:59:59' 
                """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, ssn);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Physical physical = new Physical();
                    physical.setPerson_id(rs.getString("physical_id"));
                    physical.setVersion(rs.getInt("version"));
                    physical.setName(rs.getString("name"));
                    physical.setSurname(rs.getString("surname"));
                    physical.setDateOfBirth(rs.getDate("date_of_birth").toLocalDate());
                    physical.setSsn(rs.getString("ssn"));
                    physical.setGender(Gender.valueOf(rs.getString("gender"))); 
                    return physical;
                }
            }
        }
        return null;
    }


	public List<Physical> getAllPhysical() throws SQLException
	{
		   String sql = "SELECT p.*, pe.* FROM physical p JOIN person pe ON pe.id = p.person_id WHERE updated_at = TIMESTAMP '9999-12-31 23:59:59'";

	    try (PreparedStatement stmt = connection.prepareStatement(sql)) {

	        try (ResultSet rs = stmt.executeQuery()) {
	            List<Physical> physicals = new ArrayList<>();
	            while (rs.next()) {
	            	Physical physical = new Physical();
	            	physical.setPerson_id(rs.getString("person_id"));
	            	physical.setUserId(rs.getString("user_id"));
	            	physical.setEmail(rs.getString("email"));
	            	physical.setStatus(rs.getBoolean("status"));
	            	physical.setAddressId(rs.getString("address_id"));
	            	physical.setName(rs.getString("name"));
	            	physical.setSurname(rs.getString("surname"));
	            	physical.setDateOfBirth(rs.getDate("date_of_birth").toLocalDate());
	            	physical.setSsn(rs.getString("ssn"));
	            	physicals.add(physical);
	            }
	            return physicals;
	        }
	    }
	}
	
	public void updatePhysical(Physical physical) throws SQLException {
	    String checkSql = "SELECT ssn, gender, date_of_birth, name, surname, version " +
	                     "FROM physical WHERE physical_id = ?::uuid " +
	                     "AND updated_at = TIMESTAMP '9999-12-31 23:59:59'";
	    
	    try (PreparedStatement stmt = connection.prepareStatement(checkSql)) {
	        stmt.setString(1, physical.getPerson_id());
	        try (ResultSet rs = stmt.executeQuery()) {
	            if (rs.next()) {
	                boolean valuesChanged = !rs.getString("ssn").equals(physical.getSsn()) ||
	                                      !rs.getString("gender").equals(physical.getGender().toString()) ||
	                                      !rs.getDate("date_of_birth").toLocalDate().equals(physical.getDateOfBirth()) ||
	                                      !rs.getString("name").equals(physical.getName()) ||
	                                      !rs.getString("surname").equals(physical.getSurname());
	                
	                if (!valuesChanged) {
	        //            System.out.println("Physical values unchanged - no update needed");
	                    return;
	                }
	                
	                int currentVersion = rs.getInt("version");
	                
	                String closeOldVersionSql = "UPDATE physical SET updated_at = NOW() " +
	                                         "WHERE physical_id = ?::uuid AND version = ?";
	                try (PreparedStatement closeStmt = connection.prepareStatement(closeOldVersionSql)) {
	                    closeStmt.setString(1, physical.getPerson_id());
	                    closeStmt.setInt(2, currentVersion);
	                    closeStmt.executeUpdate();
	                }
	                
	                String insertSql = "INSERT INTO physical (physical_id, ssn, gender, date_of_birth, " +
	                                 "name, surname, version, created_at, updated_at, created_by) " +
	                                 "VALUES (?::uuid, ?, ?::gender, ?, ?, ?, ?, " +
	                                 "NOW(), TIMESTAMP '9999-12-31 23:59:59', ?::uuid)";
	                try (PreparedStatement insertStmt = connection.prepareStatement(insertSql)) {
	                    insertStmt.setString(1, physical.getPerson_id());
	                    insertStmt.setString(2, physical.getSsn());
	                    insertStmt.setString(3, physical.getGender().toString());
	                    insertStmt.setDate(4, Date.valueOf(physical.getDateOfBirth()));
	                    insertStmt.setString(5, physical.getName());
	                    insertStmt.setString(6, physical.getSurname());
	                    insertStmt.setInt(7, currentVersion + 1);
	                    insertStmt.setString(8, physical.getCreatedBy());
	                    insertStmt.executeUpdate();
	                }
	            } else {
	                throw new SQLException("No current version found for physical with ID: " + physical.getPerson_id());
	            }
	        }
	    }
	}
	public boolean deletePhysical(String personId) throws SQLException {
		String sql = "DELETE FROM physical WHERE physical_id = ?::uuid";
		try (PreparedStatement stmt = connection.prepareStatement(sql)){
			stmt.setString(1, personId);
			int rowsAffected = stmt.executeUpdate();
			return rowsAffected > 0;
		}
	}
}