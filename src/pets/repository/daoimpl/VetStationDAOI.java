package pets.repository.daoimpl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import pets.model.Pet;
import pets.model.VetStation;
import pets.model.enums.Breed;
import pets.model.enums.Gender;
import pets.model.enums.LegalSize;
import pets.model.enums.MedicalSpecialties;
import pets.model.enums.PetType;
import pets.repository.dao.VetStationDAO;

public class VetStationDAOI implements VetStationDAO {
	
	private final Connection connection;
	
	public VetStationDAOI(Connection connection)
	{
		this.connection = connection;
	}
	
	@Override
	public void insertVetStation(VetStation vs) throws SQLException {
	    String sql = "INSERT INTO vet_station (" +
	        "id, emergency_availability, version, created_at, created_by, updated_at" +
	        ") VALUES (?::uuid, ?, 1, now(), ?::uuid, TIMESTAMP '9999-12-31 23:59:59')";
	    
	    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
	        stmt.setString(1, vs.getPerson_id());
	        stmt.setBoolean(2, vs.getEmergencyAvailability());
	        stmt.setString(3, vs.getCreatedBy());
	        
	        stmt.executeUpdate();
	    }
	}

	public VetStation getVetStation(String vetStationId) throws SQLException
	{
		String sql = "SELECT p.id AS person_id, p.name AS person_name, p.number AS person_number, p.email, " +
	            "p.status AS person_status, p.address_id AS person_address_id, " +
	            "l.tin AS legal_tin, l.licence_id AS legal_licence_id, " +
	            "l.daily_work_hours AS legal_daily_work_hours, " +
	            "l.size AS legal_size, " +
	            "v.emergency_availability AS vet_emergency " +
	            "FROM vet_station v " +
	            "JOIN legal l ON v.id = l.legal_id " + 
	            "JOIN person p ON l.legal_id = p.id " +
	            "WHERE v.id = ?::uuid AND v.updated_at = TIMESTAMP '9999-12-31 23:59:59'"; 
		try (PreparedStatement stmt = connection.prepareStatement(sql)) {
		    stmt.setString(1, vetStationId);

		    try (ResultSet rs = stmt.executeQuery()) {
		        if (rs.next()) {
		            VetStation vs = new VetStation();

		            vs.setName(rs.getString("person_name"));
		            vs.setNumber(rs.getString("person_number"));
		            vs.setStatus(rs.getBoolean("person_status"));
		            vs.setAddressId(rs.getString("person_address_id"));
		            vs.setUserId(null); 
		            vs.setEmail(rs.getString("email"));
		            vs.setTin(rs.getString("legal_tin"));
		            vs.setLicenceId(rs.getString("legal_licence_id"));
		            vs.setDailyWorkHours(rs.getString("legal_daily_work_hours"));


		            String sizeValue = rs.getString("legal_size");
		            if (sizeValue != null) {
		                vs.setSize(LegalSize.valueOf(sizeValue));
		            } else {
		                vs.setSize(null);
		            }

		            vs.setId(rs.getString("person_id"));
		 //           vs.setSpecialties(null); 
		   //         vs.setAllowedSpecies(null); 
		            vs.setEmergencyAvailability(rs.getBoolean("vet_emergency"));

		            return vs;
		        }
		    }
		}
	return null;
	}
	public void insertVetStationSpecies(VetStation vs) throws SQLException {
	    String getMaxVersionSql = """
	        SELECT species, MAX(version) as max_version 
	        FROM vet_station_species 
	        WHERE vet_station_id = ?::uuid AND species = ?::pet_type
	        GROUP BY species
	    """;

	    String insertSql = """
	        INSERT INTO vet_station_species (
	            vet_station_id, species, version, created_at, updated_at, created_by
	        ) VALUES (
	            ?::uuid, ?::pet_type, ?, NOW(), TIMESTAMP '9999-12-31 23:59:59', ?::uuid
	        )
	        ON CONFLICT (vet_station_id, species, updated_at) DO NOTHING
	    """;

	    try (PreparedStatement getVersionStmt = connection.prepareStatement(getMaxVersionSql);
	         PreparedStatement insertStmt = connection.prepareStatement(insertSql)) {

	        for (PetType species : vs.getAllowedSpecies()) {
	            int currentVersion = 0;

	            getVersionStmt.setString(1, vs.getPerson_id());
	            getVersionStmt.setString(2, species.name());

	            try (ResultSet rs = getVersionStmt.executeQuery()) {
	                if (rs.next()) {
	                    currentVersion = rs.getInt("max_version");
	                }
	            }

	            int newVersion = currentVersion + 1;

	            insertStmt.setString(1, vs.getPerson_id());
	            insertStmt.setString(2, species.name());
	            insertStmt.setInt(3, newVersion);
	            insertStmt.setString(4, vs.getCreatedBy());
	            insertStmt.addBatch();

	            getVersionStmt.clearParameters();
	        }

	        insertStmt.executeBatch();
	    }
	}

	public void insertVetStationSpecialties(VetStation vs) throws SQLException {
	    String getMaxVersionSql = """
	        SELECT specialty, MAX(version) as max_version 
	        FROM vet_station_specialties 
	        WHERE vet_station_id = ?::uuid AND specialty = ?::medical_specialty
	        GROUP BY specialty
	    """;

	    String insertSql = """
	        INSERT INTO vet_station_specialties (
	            vet_station_id, specialty, version, created_at, updated_at, created_by
	        ) VALUES (
	            ?::uuid, ?::medical_specialty, ?, NOW(), TIMESTAMP '9999-12-31 23:59:59', ?::uuid
	        )
	        ON CONFLICT (vet_station_id, specialty, updated_at) DO NOTHING
	    """;

	    try (PreparedStatement getVersionStmt = connection.prepareStatement(getMaxVersionSql);
	         PreparedStatement insertStmt = connection.prepareStatement(insertSql)) {
	        
	        for (MedicalSpecialties specialty : vs.getSpecialties()) {
	            int currentVersion = 0;
	            getVersionStmt.setString(1, vs.getPerson_id());
	            getVersionStmt.setString(2, specialty.name());

	            try (ResultSet rs = getVersionStmt.executeQuery()) {
	                if (rs.next()) {
	                    currentVersion = rs.getInt("max_version");
	                }
	            }

	            int newVersion = currentVersion + 1;
	            insertStmt.setString(1, vs.getPerson_id());
	            insertStmt.setString(2, specialty.name());
	            insertStmt.setInt(3, newVersion);
	            insertStmt.setString(4, vs.getCreatedBy());
	            insertStmt.addBatch();

	            getVersionStmt.clearParameters();
	        }
	        insertStmt.executeBatch();
	    }
	}

	public List<VetStation> getVetStationsByPetType(String petType) throws SQLException {
	    String sql = "SELECT p.id AS legal_id, p.name AS person_name, p.number AS person_number, p.email, " +
	                "p.status AS person_status, p.address_id AS person_address_id, " +
	                "l.tin AS legal_tin, l.licence_id AS legal_licence_id, " +
	                "l.daily_work_hours AS legal_daily_work_hours, " +
	                "l.size AS legal_size, " +
	                "v.emergency_availability AS vet_emergency " +
	                "FROM vet_station v " +
	                "JOIN legal l ON v.id = l.legal_id " +
	                "JOIN person p ON l.legal_id = p.id " +
	                "JOIN vet_station_species vss ON v.id = vss.vet_station_id " +
	                "WHERE vss.species = ?::pet_type";

	    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
	        stmt.setString(1, petType);
	        ResultSet rs = stmt.executeQuery();
	        
	        List<VetStation> vetStations = new ArrayList<>();
	        
	        while (rs.next()) {
	            VetStation vs = new VetStation();
	            vs.setPerson_id(rs.getString("legal_id"));
	            vs.setName(rs.getString("person_name"));
	            vs.setNumber(rs.getString("person_number"));
	            vs.setStatus(rs.getBoolean("person_status"));
	            vs.setAddressId(rs.getString("person_address_id"));
	            vs.setUserId(null); 
	            vs.setEmail(rs.getString("email"));
	            vs.setTin(rs.getString("legal_tin"));
	            vs.setLicenceId(rs.getString("legal_licence_id"));
	            vs.setDailyWorkHours(rs.getString("legal_daily_work_hours"));


	            String sizeValue = rs.getString("legal_size");
	            if (sizeValue != null) {
	                vs.setSize(LegalSize.valueOf(sizeValue));
	            } else {
	                vs.setSize(null);
	            }

	            vs.setId(rs.getString("legal_id"));
	            vs.setEmergencyAvailability(rs.getBoolean("vet_emergency"));

	
	            vetStations.add(vs);
	        }
	        return vetStations;
	    }
	}
	public List<Pet> viewRegisteredPets(String userId, String personId) throws SQLException{
	    if (personId == null || personId.isEmpty()) {
	        throw new IllegalArgumentException("Person ID cannot be null or empty");
	    }


	    String sql = "SELECT DISTINCT p.* FROM pet p " +
                "LEFT JOIN vet_station vs ON p.registered_by_id = vs.id " +
                "LEFT JOIN health_event he ON p.id = he.pet_id " +
                "WHERE (p.registered_by_id = ?::uuid " +
                "   OR he.vet_station_id = ?::uuid) " +
                "AND p.updated_at = TIMESTAMP '9999-12-31 23:59:59' " + 
                "AND (vs.updated_at = TIMESTAMP '9999-12-31 23:59:59' OR vs.id IS NULL) " +  
                "AND (he.updated_at = TIMESTAMP '9999-12-31 23:59:59' OR he.id IS NULL)"; 

	    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
	        
	        stmt.setString(1, userId);
	        stmt.setString(2, personId);

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
	public List<MedicalSpecialties> getVetStationSpecialties(String vetStationId) throws SQLException {
	    String sql = """
	        SELECT specialty FROM vet_station_specialties 
	        WHERE vet_station_id = ?::uuid 
	        AND updated_at = TIMESTAMP '9999-12-31 23:59:59'
	    """;

	    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
	        stmt.setString(1, vetStationId);
	        try (ResultSet rs = stmt.executeQuery()) {
	            List<MedicalSpecialties> specialties = new ArrayList<>();
	            while (rs.next()) {
	                try {
	                    specialties.add(MedicalSpecialties.valueOf(rs.getString("specialty")));
	                } catch (IllegalArgumentException e) {
	                    System.err.println("Warning: Unknown specialty in database: " + rs.getString("specialty"));
	                }
	            }
	            return specialties;
	        }
	    }
	}

	public boolean deleteVetStationSpecialties(String vetStationId, Set<MedicalSpecialties> specialtiesToDelete, String userId) 
	        throws SQLException {

	    if (specialtiesToDelete == null || specialtiesToDelete.isEmpty()) {
	        return false;
	    }

	    String updateOldVersionSql = """
	        UPDATE vet_station_specialties 
	        SET updated_at = NOW(), created_by = ?::uuid
	        WHERE vet_station_id = ?::uuid AND specialty = ?::medical_specialty 
	        AND updated_at = TIMESTAMP '9999-12-31 23:59:59'
	    """;

	    try (PreparedStatement stmt = connection.prepareStatement(updateOldVersionSql)) {
	        for (MedicalSpecialties specialty : specialtiesToDelete) {
	            stmt.setString(1, userId);
	            stmt.setString(2, vetStationId);
	            stmt.setString(3, specialty.name());
	            stmt.addBatch();
	        }
	        stmt.executeBatch();
	        return true;
	    }
	}

	public List<PetType> getVetStationSpecies(String vetStationId) throws SQLException {
	    String sql = """
	        SELECT species FROM vet_station_species 
	        WHERE vet_station_id = ?::uuid 
	        AND updated_at = TIMESTAMP '9999-12-31 23:59:59'
	    """;

	    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
	        stmt.setString(1, vetStationId);

	        try (ResultSet rs = stmt.executeQuery()) {
	            List<PetType> pets = new ArrayList<>();
	            while (rs.next()) {
	                try {
	                    pets.add(PetType.valueOf(rs.getString("species")));
	                } catch (IllegalArgumentException e) {
	                    System.err.println("Unknown species in DB: " + rs.getString("species"));
	                }
	            }
	            return pets;
	        }
	    }
	}

	public boolean deleteVetStationSpecies(String vetStationId, Set<PetType> speciesToDelete, String userId) 
	        throws SQLException {

	    if (speciesToDelete == null || speciesToDelete.isEmpty()) {
	        return false;
	    }

	    String updateSql = """
	        UPDATE vet_station_species 
	        SET updated_at = NOW(), created_by = ?::uuid 
	        WHERE vet_station_id = ?::uuid 
	        AND species = ?::pet_type 
	        AND updated_at = TIMESTAMP '9999-12-31 23:59:59'
	    """;

	    try (PreparedStatement stmt = connection.prepareStatement(updateSql)) {
	        for (PetType species : speciesToDelete) {
	            stmt.setString(1, userId);
	            stmt.setString(2, vetStationId);
	            stmt.setString(3, species.name());
	            stmt.addBatch();
	        }

	        stmt.executeBatch();
	        return true;
	    }
	}
	public boolean updateEmergencyAvailability(String vetStationId, boolean bool, String userId) throws SQLException {

		String selectSql = "SELECT version FROM vet_station WHERE id = ?::uuid ORDER BY version DESC LIMIT 1";
	    int latestVersion = 0;

	    try (PreparedStatement stmt = connection.prepareStatement(selectSql)) {
	        stmt.setString(1, vetStationId);
	        ResultSet rs = stmt.executeQuery();
	        if (rs.next()) {
	            latestVersion = rs.getInt("version");
	        } else {
	            throw new SQLException("Vet station not found for ID: " + vetStationId);
	        }
	    }

	    String updateSql = "UPDATE vet_station SET updated_at = now() WHERE id = ?::uuid AND version = ?";
	    try (PreparedStatement stmt = connection.prepareStatement(updateSql)) {
	        stmt.setString(1, vetStationId);
	        stmt.setInt(2, latestVersion);
	        stmt.executeUpdate();
	    }

	    String insertSql = "INSERT INTO vet_station (id, version, emergency_availability, created_at, updated_at, created_by) " +
	                       "VALUES (?::uuid, ?, ?, now(), now(), ?::uuid)";
	    
	    try (PreparedStatement stmt = connection.prepareStatement(insertSql)) {
	        stmt.setString(1, vetStationId);
	        stmt.setInt(2, latestVersion + 1);
	        stmt.setBoolean(3, bool);
	        stmt.setString(4, userId);
	        int rowsInserted = stmt.executeUpdate();
	        return rowsInserted > 0;
	    }
	}
	public boolean deleteVetStation(String vetStationId) throws SQLException {
	    String specialtiesSql = "DELETE FROM vet_station_specialties WHERE vet_station_id = ?::uuid";
	    String stationSql = "DELETE FROM vet_station WHERE id = ?::uuid";
	    
	    try (PreparedStatement specialtiesStmt = connection.prepareStatement(specialtiesSql)) {
	        specialtiesStmt.setString(1, vetStationId);
	        specialtiesStmt.executeUpdate();
	    }
	    
	    try (PreparedStatement stationStmt = connection.prepareStatement(stationSql)) {
	        stationStmt.setString(1, vetStationId);
	        int rowsAffected = stationStmt.executeUpdate();
	        return rowsAffected > 0;
	    }
	}
	
}
