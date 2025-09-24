package pets.repository.daoimpl;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import pets.model.Pet;
import pets.model.enums.Breed;
import pets.model.enums.Gender;
import pets.model.enums.PetType;
import pets.repository.dao.PetDAO;

public class PetDAOI implements PetDAO {
	
	private Connection connection;

    public PetDAOI(Connection connection) {
        this.connection = connection;
    }


    public void insert(Pet pet) throws SQLException {
        String sql = "INSERT INTO pet (" +
                     "id, name, pet_type, breed, gender, date_of_birth, date_of_registry, is_stray, registered_by_id, " +
                     "created_at, updated_at, version, date_of_death" +
                     ") VALUES (?::uuid, ?, ?::pet_type, ?::breed, ?::gender, ?, ?, ?, ?::uuid, now(), TIMESTAMP '9999-12-31 23:59:59', 1, TIMESTAMP '9999-12-31 23:59:59')";

        String generatedId = UUID.randomUUID().toString();
        pet.setId(generatedId);

        LocalDate birthDate = pet.getDateOfBirth();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, pet.getId());
            stmt.setString(2, pet.getName());
            stmt.setString(3, pet.getPetType().toString());
            stmt.setString(4, pet.getBreed().toString());
            stmt.setString(5, pet.getGender().toString());

            if (birthDate != null) {
                stmt.setDate(6, Date.valueOf(birthDate));
            } else {
                stmt.setNull(6, java.sql.Types.DATE);
            }

            stmt.setDate(7, Date.valueOf(pet.getDateOfRegistry()));
            stmt.setBoolean(8, pet.getStray());
            stmt.setString(9, pet.getRegisteredById());

            stmt.executeUpdate();
        }
    }


	public Pet getPetById(String petId) throws SQLException {
		String sql = "SELECT p.* FROM pet p " +
					 "WHERE p.id = ?::uuid AND p.updated_at =  TIMESTAMP '9999-12-31 23:59:59'";
		 try (PreparedStatement stmt = connection.prepareStatement(sql)) {
		        stmt.setString(1, petId);
		        try (ResultSet rs = stmt.executeQuery()) {
		            if (rs.next()) {
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
						return pet;
		            }
		        }
		    }
		return null;
	}
	
	public List<Pet> viewRegisteredPets() throws SQLException {
		String sql = "SELECT p.* FROM pet p WHERE p.updated_at = TIMESTAMP '9999-12-31 23:59:59'";
		try (PreparedStatement stmt = connection.prepareStatement(sql);
				ResultSet rs = stmt.executeQuery()) {
					List<Pet> pets = new ArrayList<>();
					while (rs.next())
					{
						Pet pet = new Pet();
						pet.setName(rs.getString("name"));
						pet.setId(rs.getString("id"));
						pet.setPetType(PetType.valueOf(rs.getString("pet_type")));
			            pet.setBreed(Breed.valueOf(rs.getString("breed")));
			            pet.setGender(Gender.valueOf(rs.getString("gender")));
						pet.setStray(rs.getBoolean("is_stray"));
						
						java.sql.Date dob = rs.getDate("date_of_birth");
			            pet.setDateOfBirth(dob != null ? dob.toLocalDate() : null);
			            
			            java.sql.Date dor = rs.getDate("date_of_registry");
			            pet.setDateOfRegistry(dor != null ? dor.toLocalDate() : null);
			            
			            java.sql.Date dod = rs.getDate("date_of_death");
			            pet.setDateOfDeath(dod != null ? dod.toLocalDate() : null);
			            
						pet.setRegisteredById(rs.getString("registered_by_id"));
						pets.add(pet);
					}
					return pets;
				}
	}
	public List<Pet> getRegisteredPetsWithoutValidPassports() throws SQLException {
	    String sql = "SELECT p.* FROM pet p " +
                "WHERE p.updated_at = TIMESTAMP '9999-12-31 23:59:59' " +
                "AND p.date_of_death = TIMESTAMP '9999-12-31'  " +
                "AND p.id NOT IN ( " +
                "    SELECT pa.pet_id FROM passport pa " +
                "    WHERE pa.valid = TRUE " +
                "    AND pa.updated_at = TIMESTAMP '9999-12-31 23:59:59' " +
                ")";
		try (PreparedStatement stmt = connection.prepareStatement(sql);
				ResultSet rs = stmt.executeQuery()) {
					List<Pet> pets = new ArrayList<>();
					while (rs.next())
					{
						Pet pet = new Pet();
						pet.setName(rs.getString("name"));
						pet.setId(rs.getString("id"));
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
	
	public List<Pet> getPetsByCC(String careCenterId) throws SQLException {
	    if (careCenterId == null || careCenterId.isEmpty()) {
	        throw new IllegalArgumentException("Care Center ID cannot be null or empty");
	    }

	    String sql = 
	            "SELECT p.* FROM pet p " +
	            "WHERE p.updated_at = TIMESTAMP '9999-12-31 23:59:59' " +
	            "AND p.date_of_death = TIMESTAMP '9999-12-31 23:59:59' " +
	            "AND p.registered_by_id = ?::uuid " +
	            "UNION " +
	            "SELECT DISTINCT p.* FROM pet p " +
	            "JOIN activity a ON p.id = a.pet_id " +
	            "WHERE p.updated_at = TIMESTAMP '9999-12-31 23:59:59' " +
	            "AND p.date_of_death = TIMESTAMP '9999-12-31' " +
	            "AND a.updated_at = TIMESTAMP '9999-12-31 23:59:59' " +
	            "AND a.care_center_id = ?::uuid";
	    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
	        stmt.setString(1, careCenterId);
	        stmt.setString(2, careCenterId);
	        
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
	                
	                pet.setDateOfBirth(rs.getDate("date_of_birth") != null ? 
	                    rs.getDate("date_of_birth").toLocalDate() : null);
	                
	                pet.setDateOfRegistry(rs.getDate("date_of_registry") != null ? 
	                    rs.getDate("date_of_registry").toLocalDate() : null);
	                
	                pet.setRegisteredById(rs.getString("registered_by_id"));
	                pets.add(pet);
	            }
	            return pets;
	        }
	    }
	}
	public void updatePet(Pet updatedPet) throws SQLException {
	    String selectSql = "SELECT * FROM pet WHERE id = ?::uuid AND updated_at = TIMESTAMP '9999-12-31 23:59:59'";
	    Pet existingPet = null;

	    try (PreparedStatement stmt = connection.prepareStatement(selectSql)) {
	        stmt.setString(1, updatedPet.getId());
	        ResultSet rs = stmt.executeQuery();
	        if (rs.next()) {
	            existingPet = new Pet();
	            existingPet.setId(rs.getString("id"));
	            existingPet.setName(rs.getString("name"));
	            existingPet.setPetType(PetType.valueOf(rs.getString("pet_type")));
	            existingPet.setBreed(Breed.valueOf(rs.getString("breed")));
	            existingPet.setGender(Gender.valueOf(rs.getString("gender")));
	            existingPet.setDateOfBirth(rs.getDate("date_of_birth") != null ? rs.getDate("date_of_birth").toLocalDate() : null);
	            existingPet.setDateOfRegistry(rs.getDate("date_of_registry").toLocalDate());
	            existingPet.setDateOfDeath(rs.getDate("date_of_death") != null ? rs.getDate("date_of_death").toLocalDate() : null);
	            existingPet.setStray(rs.getBoolean("is_stray"));
	            existingPet.setRegisteredById(rs.getString("registered_by_id"));
	            existingPet.setVersion(rs.getInt("version"));
	        } else {
	            throw new SQLException("Active pet version not found.");
	        }
	    }

	    String closeSql = "UPDATE pet SET updated_at = now() WHERE id = ?::uuid AND updated_at = TIMESTAMP '9999-12-31 23:59:59'";
	    try (PreparedStatement stmt = connection.prepareStatement(closeSql)) {
	        stmt.setString(1, existingPet.getId());
	        stmt.executeUpdate();
	    }

	    String insertSql = "INSERT INTO pet (" +
	                       "id, name, pet_type, breed, gender, date_of_birth, date_of_registry, date_of_death, is_stray, registered_by_id, " +
	                       "created_at, updated_at, version) " +
	                       "VALUES (?::uuid, ?, ?::pet_type, ?::breed, ?::gender, ?, ?, ?, ?, ?::uuid, now(), TIMESTAMP '9999-12-31 23:59:59', ?)";

	    try (PreparedStatement stmt = connection.prepareStatement(insertSql)) {

	    	stmt.setString(1, existingPet.getId());
	        stmt.setString(2, updatedPet.getName() != null ? updatedPet.getName() : existingPet.getName());
	        stmt.setString(3, existingPet.getPetType().toString());
	        stmt.setString(4, existingPet.getBreed().toString());
	        stmt.setString(5, existingPet.getGender().toString());

	        if (existingPet.getDateOfBirth() != null) {
	            stmt.setDate(6, Date.valueOf(existingPet.getDateOfBirth()));
	        } else {
	            stmt.setNull(6, java.sql.Types.DATE);
	        }

	        stmt.setDate(7, Date.valueOf(existingPet.getDateOfRegistry()));
	        if (updatedPet.getDateOfDeath() != null) {
	            stmt.setDate(8, Date.valueOf(updatedPet.getDateOfDeath()));
	        } else if (existingPet.getDateOfDeath() != null) {
	            stmt.setDate(8, Date.valueOf(existingPet.getDateOfDeath()));
	        } else {
	            stmt.setNull(8, java.sql.Types.DATE);
	        }

	        stmt.setBoolean(9, existingPet.getStray());
	        stmt.setString(10, existingPet.getRegisteredById());
	        stmt.setInt(11, existingPet.getVersion() + 1);
	        stmt.executeUpdate();
	    }
	}


	public boolean deletePet(String petId) throws SQLException {
		String sql = "DELETE FROM pet WHERE id = ?::uuid";
		try (PreparedStatement stmt = connection.prepareStatement(sql)){
			stmt.setString(1, petId);
			int rowsAffected = stmt.executeUpdate();
			return rowsAffected > 0;
		}

	}

}