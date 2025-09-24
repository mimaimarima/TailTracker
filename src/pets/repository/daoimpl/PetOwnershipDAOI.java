package pets.repository.daoimpl;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import pets.model.Pet;
import pets.model.PetOwnership;
import pets.model.Physical;
import pets.model.enums.Breed;
import pets.model.enums.Gender;
import pets.model.enums.PetType;
import pets.repository.dao.PetOwnershipDAO;

public class PetOwnershipDAOI implements PetOwnershipDAO  {

	private final Connection connection;

    public PetOwnershipDAOI(Connection connection) {
        this.connection = connection;
    }
   
	@Override
	public void insertOwner(PetOwnership petOwnership) throws SQLException {
	    String query = "INSERT INTO pet_ownership (physical_id, pet_id, date_from, date_to, created_at, updated_at, version, created_by) " +
	                   "VALUES (?::uuid, ?::uuid, ?, TIMESTAMP '9999-12-31 23:59:59', NOW(), TIMESTAMP '9999-12-31 23:59:59', 1, ?::uuid)";

	    try (PreparedStatement stmt = connection.prepareStatement(query)) {
	        stmt.setString(1, petOwnership.getPhysicalId());
	        stmt.setString(2, petOwnership.getPetId());
	        stmt.setDate(3, Date.valueOf(petOwnership.getDateFrom()));
	        stmt.setString(4, petOwnership.getCreatedBy());
	        stmt.executeUpdate();
	    }
	}

	public PetOwnership getOwnership(String petId) throws SQLException {
	    String sql = "SELECT po.* FROM pet_ownership po WHERE po.pet_id = ?::uuid " +
	                 "AND po.updated_at = TIMESTAMP '9999-12-31 23:59:59'";

	    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
	        stmt.setString(1, petId);
	        try (ResultSet rs = stmt.executeQuery()) {
	            if (rs.next()) {
	                PetOwnership po = new PetOwnership();
	                po.setPetId(petId);
	                po.setPhysicalId(rs.getString("physical_id"));
	                po.setDateFrom(rs.getDate("date_from").toLocalDate());
	                java.sql.Date dateTo = rs.getDate("date_to");
	                po.setDateTo(dateTo != null ? dateTo.toLocalDate() : null);
	                po.setCreatedAt(rs.getTimestamp("created_at"));
	                po.setUpdatedAt(rs.getTimestamp("updated_at"));
	                po.setVersion(rs.getInt("version"));
	                po.setCreatedBy(rs.getString("created_by"));
	                return po;
	            }
	        }
	    }
	    return null;
	}

	public List<PetOwnership> getAllOwnerships() throws SQLException {
	    String sql = "SELECT po.* FROM pet_ownership po WHERE po.updated_at = TIMESTAMP '9999-12-31 23:59:59'";
	    try (PreparedStatement stmt = connection.prepareStatement(sql);
				ResultSet rs = stmt.executeQuery()) {
					List<PetOwnership> pos = new ArrayList<>();
					while (rs.next())
					{
						PetOwnership po = new PetOwnership();
						po.setPetId(rs.getString("pet_id"));
						po.setPhysicalId(rs.getString("physical_id"));
						po.setDateFrom(rs.getDate("date_from").toLocalDate());
						java.sql.Date dt = rs.getDate("date_to");
						po.setDateTo(dt != null ? dt.toLocalDate() : null);
						pos.add(po);
					}
					return pos;
				}
	}
	public List<Physical> getAllOwners() throws SQLException {
	    String sql = "SELECT p.* FROM physical p JOIN pet_ownership po ON p.physical_id = po.physical_id WHERE po.updated_at = TIMESTAMP '9999-12-31 23:59:59' AND p.updated_at = TIMESTAMP '9999-12-31 23:59:59'";
	    try (PreparedStatement stmt = connection.prepareStatement(sql);
				ResultSet rs = stmt.executeQuery()) {
					List<Physical> ps = new ArrayList<>();
					while (rs.next())
					{
						Physical p = new Physical();
						p.setId(rs.getString("physical_id"));
						p.setName(rs.getString("name"));
						p.setSurname(rs.getString("surname"));
						ps.add(p);
					}
					return ps;
				}
	}
	public List<Pet> viewPetsWithoutOwner() throws SQLException {
		
	    String sql = "SELECT DISTINCT p.* FROM pet p"
	    		+ " WHERE p.date_of_death is NULL AND p.updated_at = TIMESTAMP '9999-12-31 23:59:59' AND p.id NOT IN ( SELECT po.pet_id"
	    		+ " FROM pet_ownership po"
	    		+ " WHERE po.date_to is NULL or po.date_to > CURRENT_DATE )";

	    try (PreparedStatement stmt = connection.prepareStatement(sql)) {

	        try (ResultSet rs = stmt.executeQuery()) {
	            List<Pet> pets = new ArrayList<>();
	            while (rs.next()) {
	                Pet pet = new Pet();
	                pet.setId(rs.getString("id"));
	                pet.setName(rs.getString("name"));
	                pet.setPetType(PetType.valueOf(rs.getString("pet_type")));
	                pet.setBreed(Breed.valueOf(rs.getString("breed")));
	                pet.setGender(Gender.valueOf(rs.getString("gender")));
	                pet.setStray(rs.getBoolean("is_stray"));
	                
	                java.sql.Date dob = rs.getDate("date_of_birth");
	                pet.setDateOfBirth(dob != null ? dob.toLocalDate() : null);
	                
	                java.sql.Date dor = rs.getDate("date_of_registry");
	                pet.setDateOfRegistry(dor != null ? dor.toLocalDate() : null);
	                
	                pet.setRegisteredById(rs.getString("registered_by_id"));
	                pets.add(pet);
	            }
	            return pets;
	        }
	    }
	}
	public List<Pet> viewPetsByOwner(String personId) throws SQLException {
	    if (personId == null || personId.isEmpty()) {
	        throw new IllegalArgumentException("Person ID cannot be null or empty");
	    }

	    String sql = "SELECT DISTINCT p.* FROM pet p " +
	                 "JOIN pet_ownership po ON p.id = po.pet_id " +
	                 "WHERE po.physical_id = ?::uuid AND (po.date_to = TIMESTAMP '9999-12-31 23:59:59' OR po.date_to > ?)"
	                 + " AND p.updated_at = TIMESTAMP '9999-12-31 23:59:59' AND po.updated_at = TIMESTAMP '9999-12-31 23:59:59'"; 

	    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
	        
	        stmt.setString(1, personId);
	        stmt.setDate(2, java.sql.Date.valueOf(LocalDate.now()));
	        try (ResultSet rs = stmt.executeQuery()) {
	            List<Pet> pets = new ArrayList<>();
	            while (rs.next()) {
	                Pet pet = new Pet();
	                pet.setId(rs.getString("id"));
	                pet.setName(rs.getString("name"));
	                pet.setPetType(PetType.valueOf(rs.getString("pet_type")));
	                pet.setBreed(Breed.valueOf(rs.getString("breed")));
	                pet.setGender(Gender.valueOf(rs.getString("gender")));
	                pet.setStray(rs.getBoolean("is_stray"));
	                
	                java.sql.Date dob = rs.getDate("date_of_birth");
	                pet.setDateOfBirth(dob != null ? dob.toLocalDate() : null);
	                
	                java.sql.Date dor = rs.getDate("date_of_registry");
	                pet.setDateOfRegistry(dor != null ? dor.toLocalDate() : null);
	                
	                pet.setRegisteredById(rs.getString("registered_by_id"));
	                pets.add(pet);
	            }
	            return pets;
	        }
	    }
	}
	public void updateOwnershipDateTo(String petId, LocalDate dateTo) throws SQLException {
	    int currentVersion = 1;
	    String versionSql = "SELECT version FROM pet_ownership WHERE pet_id = ?::uuid ORDER BY version DESC LIMIT 1";
	    try (PreparedStatement stmt = connection.prepareStatement(versionSql)) {
	        stmt.setString(1, petId);
	        try (ResultSet rs = stmt.executeQuery()) {
	            if (rs.next()) {
	                currentVersion = rs.getInt("version");
	                
	            }
	        }
	    }
	    
	    String closeOldVersionSql = "UPDATE pet_ownership SET updated_at = NOW() WHERE pet_id = ?::uuid AND version = ?";
	    try (PreparedStatement stmt = connection.prepareStatement(closeOldVersionSql)) {
	        stmt.setString(1, petId);
	        stmt.setInt(2, currentVersion);
	        stmt.executeUpdate();
	    }

	}
	public boolean deleteOwnership(String petId) throws SQLException
	{

		String sql = "DELETE FROM pet_ownership WHERE pet_id = ?::uuid";
		
		try (PreparedStatement stmt = connection.prepareStatement(sql)){
			stmt.setString(1, petId);
			int rowsAffected = stmt.executeUpdate();
			return rowsAffected > 0;
		}
	}

	public boolean deleteOwnershipByPhysicalId(String physicalId) throws SQLException {
		String sql = "DELETE FROM pet_ownership WHERE physical_id = ?::uuid";
		
		try (PreparedStatement stmt = connection.prepareStatement(sql)){
			stmt.setString(1, physicalId);
			int rowsAffected = stmt.executeUpdate();
			return rowsAffected > 0;
		}
	}
}
