package pets.repository.daoimpl;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import pets.model.Passport;
import pets.model.Pet;
import pets.model.enums.Breed;
import pets.model.enums.PetType;
import pets.repository.dao.PassportDAO;

public class PassportDAOI implements PassportDAO {

	private final Connection connection;

    public PassportDAOI(Connection connection) {
        this.connection = connection;
    }
	@Override
	public void insertPassport(Passport passport) throws SQLException {
	    String sql = "INSERT INTO passport (" +
	                 "id, pet_id, number, date_issue, date_end, valid, " +
	                 "created_at, updated_at, version, created_by) " +
	                 "VALUES (?::uuid, ?::uuid, ?, ?, ?, ?, now(), TIMESTAMP '9999-12-31 23:59:59', 1, ?::uuid) " +
	                 "RETURNING id, created_at";

	    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
	        String newId = UUID.randomUUID().toString();
	        passport.setId(newId);
	        
	        stmt.setString(1, newId);
	        stmt.setString(2, passport.getPetId());
	        stmt.setString(3, passport.getNumber());
	        stmt.setDate(4, Date.valueOf(passport.getDateIssue()));
	        stmt.setDate(5, Date.valueOf(passport.getDateEnd()));
	        stmt.setBoolean(6, passport.isValid());
	        stmt.setString(7, passport.getCreatedBy());
	        
	        ResultSet rs = stmt.executeQuery();
	        if (rs.next()) {
	            passport.setId(rs.getString("id"));
	            passport.setCreatedAt(rs.getTimestamp("created_at"));
	            passport.setUpdatedAt(Timestamp.valueOf("9999-12-31 23:59:59"));
	            passport.setVersion(1);
	        }
	    }
	}
	public void updatePassport(Passport passport) throws SQLException {
	    int currentVersion = 1;

	    
	    String versionSql = "SELECT version FROM passport WHERE id = ?::uuid AND updated_at = TIMESTAMP '9999-12-31 23:59:59' ORDER BY version DESC LIMIT 1";
	    try (PreparedStatement stmt = connection.prepareStatement(versionSql)) {
	        stmt.setString(1, passport.getId());
	        try (ResultSet rs = stmt.executeQuery()) {
	            if (rs.next()) {
	                currentVersion = rs.getInt("version");
	            }
	        }
	    }

	 
	    String closeOldVersionSql = "UPDATE passport SET updated_at = NOW(), valid = false WHERE id = ?::uuid AND version = ?";
	    try (PreparedStatement stmt = connection.prepareStatement(closeOldVersionSql)) {
	        stmt.setString(1, passport.getId());
	        stmt.setInt(2, currentVersion);
	        stmt.executeUpdate();
	    }

	   
	    String insertSql = "INSERT INTO passport (id, number, date_issue, date_end, version, created_at, updated_at, created_by, valid, pet_id) " +
	                       "VALUES (?::uuid, ?, ?, ?, ?, NOW(), TIMESTAMP '9999-12-31 23:59:59', ?::uuid, true, ?::uuid)";
	    try (PreparedStatement stmt = connection.prepareStatement(insertSql)) {
	        stmt.setString(1, passport.getId());
	        stmt.setString(2, passport.getNumber());
	        stmt.setDate(3, Date.valueOf(passport.getDateIssue()));
	        stmt.setDate(4, Date.valueOf(passport.getDateEnd()));
	        stmt.setInt(5, currentVersion + 1);
	        stmt.setString(6, passport.getCreatedBy());
	        stmt.setString(7, passport.getPetId());
	        stmt.executeUpdate();
	    }
	}
	public Passport getPassport(String petId) throws SQLException {
		String sql = "SELECT p.* FROM passport p WHERE p.pet_id = ?::uuid AND p.valid = true AND p.updated_at = TIMESTAMP '9999-12-31 23:59:59'";
		try (PreparedStatement stmt = connection.prepareStatement(sql))
		{
			stmt.setString(1, petId);
	        try (ResultSet rs = stmt.executeQuery()){
	        	if (rs.next()) {
	        		Passport pass = new Passport();
					pass.setNumber(rs.getString("number"));
					pass.setDateIssue(rs.getDate("date_issue").toLocalDate());
					pass.setDateEnd(rs.getDate("date_end").toLocalDate());
					return pass;
	        	}
			}
		}
		return null;
	}
	public Passport getPassportByPassportNumber(String pn) throws SQLException {
		String sql = "SELECT p.* FROM passport p WHERE p.number = ? AND p.valid = true AND p.updated_at = TIMESTAMP '9999-12-31 23:59:59'";
		try (PreparedStatement stmt = connection.prepareStatement(sql))
		{
			stmt.setString(1, pn);
	        try (ResultSet rs = stmt.executeQuery()){
	        	if (rs.next()) {
	        		Passport pass = new Passport();
					pass.setNumber(rs.getString("number"));
					pass.setDateIssue(rs.getDate("date_issue").toLocalDate());
					pass.setDateEnd(rs.getDate("date_end").toLocalDate());
					return pass;
	        	}
			}
		}
		return null;
	}
	public Map<Pet, Passport> getValidPassports() throws SQLException {
	    String sql = "SELECT pa.id AS passport_id, pa.number, pa.date_issue, pa.date_end, " +
                "p.id AS pet_id, p.name, p.pet_type, p.breed, p.date_of_birth " +
                "FROM passport pa JOIN pet p ON pa.pet_id = p.id " +
                "WHERE pa.valid = true AND pa.updated_at = TIMESTAMP '9999-12-31 23:59:59'";
		try (PreparedStatement stmt = connection.prepareStatement(sql)) {
			ResultSet rs = stmt.executeQuery();
			
			Map<Pet, Passport> petToPassport = new LinkedHashMap<>();
			while (rs.next())
			{

	        	Pet pet = new Pet();
	            pet.setId(rs.getString("pet_id"));
	            pet.setName(rs.getString("name"));
	            pet.setPetType(PetType.valueOf(rs.getString("pet_type")));
	            pet.setBreed(Breed.valueOf(rs.getString("breed")));
	            pet.setDateOfBirth(rs.getDate("date_of_birth").toLocalDate());
	        	Passport pass = new Passport();
	        	pass.setPetId(rs.getString("pet_id"));
	        	pass.setId(rs.getString("passport_id"));
				pass.setNumber(rs.getString("number"));
				pass.setDateIssue(rs.getDate("date_issue").toLocalDate());
				pass.setDateEnd(rs.getDate("date_end").toLocalDate());
				
				petToPassport.put(pet, pass);
			}
			return petToPassport;
		}
	}
	
	public boolean deletePassport(String passportId) throws SQLException {
		String sql = "DELETE FROM passport WHERE id = ?::uuid";
		
		try (PreparedStatement stmt = connection.prepareStatement(sql)){
			stmt.setString(1, passportId);
			int rowsAffected = stmt.executeUpdate();
			return rowsAffected > 0;
		}
	}
	public boolean deletePassportByPetId(String petId) throws SQLException {
		String sql = "DELETE FROM passport WHERE pet_id = ?::uuid";
		
		try (PreparedStatement stmt = connection.prepareStatement(sql)){
			stmt.setString(1, petId);
			int rowsAffected = stmt.executeUpdate();
			return rowsAffected > 0;
		}
	}
	
}
