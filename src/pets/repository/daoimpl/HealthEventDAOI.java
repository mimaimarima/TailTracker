package pets.repository.daoimpl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import pets.model.HealthEvent;
import pets.model.Pet;
import pets.model.enums.HealthEventType;
import pets.model.enums.PetType;
import pets.repository.dao.HealthEventDAO;

public class HealthEventDAOI implements HealthEventDAO {
	
	private Connection connection;

    public HealthEventDAOI(Connection connection) {
        this.connection = connection;
    }
	
    public void insert(HealthEvent healthEvent) throws SQLException {
        String sql = "INSERT INTO health_event (id, type, datetime, description, recommendation, price, vet_station_id, pet_id, verified, created_by, version, created_at, updated_at) " +
                     "VALUES (?::uuid, ?::health_event_type, ?, ?, ?, ?, ?::uuid, ?::uuid, ?, ?::uuid, 1, NOW(), TIMESTAMP '9999-12-31 23:59:59')";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            String newId = UUID.randomUUID().toString();
            healthEvent.setId(newId);

            stmt.setString(1, newId);
            stmt.setString(2, healthEvent.getTypeHE().toString());
            stmt.setTimestamp(3, Timestamp.valueOf(healthEvent.getDateTime()));
            stmt.setString(4, healthEvent.getDescription());
            stmt.setString(5, healthEvent.getRecommendation());
            stmt.setBigDecimal(6, healthEvent.getPrice());
            stmt.setString(7, healthEvent.getVetStationId());
            stmt.setString(8, healthEvent.getPetId());
            stmt.setBoolean(9, healthEvent.isVerified());
            stmt.setString(10, healthEvent.getCreatedBy());

            stmt.executeUpdate();
        }
    }

	
	public List<HealthEvent> getHealthEventsByPet(String petId) throws SQLException
	{
		if (petId == null || petId.isEmpty()) {
	        throw new IllegalArgumentException("Pet ID cannot be null or empty");
	    }

	    String sql = "SELECT he.* FROM health_event he " +
	                 "JOIN pet p ON p.id = he.pet_id " +
	                 "WHERE he.pet_id = ?::uuid AND he.updated_at = TIMESTAMP '9999-12-31 23:59:59' AND p.updated_at = TIMESTAMP '9999-12-31 23:59:59'"; 

	    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
	        
	        stmt.setString(1, petId);
	        
	        try (ResultSet rs = stmt.executeQuery()) {
	            List<HealthEvent> heS = new ArrayList<>();
	            while (rs.next()) {
	                HealthEvent he = new HealthEvent();
	                he.setId(rs.getString("id"));
	                he.setTypeHE(HealthEventType.valueOf(rs.getString("type")));
	                he.setDescription(rs.getString("description"));
	                he.setRecommendation(rs.getString("recommendation"));
	                he.setPrice(rs.getBigDecimal("price"));
		            he.setVetStationId(rs.getString("vet_station_id"));
	                java.sql.Timestamp d = rs.getTimestamp("datetime");
	                he.setDateTime(d.toLocalDateTime());
	                heS.add(he);
	            }
	            return heS;
	        }
	    }
}
	public Map<Pet, List<HealthEvent>> getHealthEventsByVetStation(String vetStationId) throws SQLException {
	    if (vetStationId == null || vetStationId.isEmpty()) {
	        throw new IllegalArgumentException("Vet Station ID cannot be null or empty");
	    }

	    String sql = "SELECT he.*, p.id AS pet_id, p.name AS pet_name, p.pet_type " +
	                "FROM health_event he " +
	                "JOIN pet p ON he.pet_id = p.id " +
	                "WHERE he.vet_station_id = ?::uuid AND he.updated_at = TIMESTAMP '9999-12-31 23:59:59' AND p.updated_at = TIMESTAMP '9999-12-31 23:59:59'" + 
	                "ORDER BY p.name, he.datetime DESC";

	    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
	        stmt.setObject(1, vetStationId);
	        ResultSet rs = stmt.executeQuery();

	        Map<Pet, List<HealthEvent>> petToEvents = new LinkedHashMap<>();

	        while (rs.next()) {

	        	Pet pet = new Pet();
	            pet.setId(rs.getString("pet_id"));
	            pet.setName(rs.getString("pet_name"));
	            pet.setPetType(PetType.valueOf(rs.getString("pet_type")));

	            HealthEvent event = new HealthEvent();
	            event.setId(rs.getString("id")); 
	            event.setTypeHE(HealthEventType.valueOf(rs.getString("type")));
	            event.setDescription(rs.getString("description"));
	            event.setVerified(rs.getBoolean("verified"));
	            event.setVetStationId(rs.getString("vet_station_id"));
	            Timestamp timestamp = rs.getTimestamp("datetime");
	            if (timestamp != null) {
	                event.setDateTime(timestamp.toLocalDateTime());
	            }


	            petToEvents.computeIfAbsent(pet, k -> new ArrayList<>()).add(event);
	        }
	        return petToEvents;
	    }
	}
	
	public Map<Pet, List<HealthEvent>> getUnverifiedHealthEvents(String vetStationId) throws SQLException {
	    if (vetStationId == null || vetStationId.isEmpty()) {
	        throw new IllegalArgumentException("Vet Station ID cannot be null or empty");
	    }

	    String sql = "SELECT he.*, " +
	                 "p.id AS pet_id, p.name AS pet_name, p.pet_type " +
	                 "FROM health_event he " +
	                 "JOIN pet p ON he.pet_id = p.id " +
	                 "WHERE he.vet_station_id = ?::uuid " +
	                 "AND he.verified = false " +
	                 "AND he.updated_at = TIMESTAMP '9999-12-31 23:59:59' " +
	                 "AND p.updated_at = TIMESTAMP '9999-12-31 23:59:59' " +
	                 "ORDER BY p.name, he.datetime DESC";

	    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
	        stmt.setObject(1, vetStationId);
	        try (ResultSet rs = stmt.executeQuery()) {

	            Map<Pet, List<HealthEvent>> petToEvents = new LinkedHashMap<>();

	            while (rs.next()) {

	            	Pet pet = new Pet();
	                pet.setId(rs.getString("pet_id"));
	                pet.setName(rs.getString("pet_name"));
	                pet.setPetType(PetType.valueOf(rs.getString("pet_type")));

	                HealthEvent event = new HealthEvent();
	                event.setId(rs.getString("id"));
	                event.setTypeHE(HealthEventType.valueOf(rs.getString("type")));
	                event.setDescription(rs.getString("description"));
	                event.setRecommendation(rs.getString("recommendation"));
	                event.setPrice(rs.getBigDecimal("price"));
	                event.setVerified(rs.getBoolean("verified"));
	                event.setPetId(rs.getString("pet_id"));
	                event.setVetStationId(rs.getString("vet_station_id"));

	                Timestamp timestamp = rs.getTimestamp("datetime");
	                if (timestamp != null) {
	                    event.setDateTime(timestamp.toLocalDateTime());
	                }

	                petToEvents.computeIfAbsent(pet, k -> new ArrayList<>()).add(event);
	            }

	            return petToEvents;
	        }
	    }
	}

	public void verifyHealthEvent(String healthEventId) throws SQLException {
	    if (healthEventId == null || healthEventId.isEmpty()) {
	        throw new IllegalArgumentException("Health Event ID cannot be null or empty");
	    }

	    String sql = "UPDATE health_event SET verified = true WHERE id = ?::uuid AND updated_at = TIMESTAMP '9999-12-31 23:59:59'";
	    
	    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
	        stmt.setObject(1, healthEventId);
	        int affectedRows = stmt.executeUpdate();
	        
	        if (affectedRows == 0) {
	            throw new SQLException("No health event found with ID: " + healthEventId);
	        }
	    }
	}
	public void updateHealthEvent(HealthEvent healthEvent) throws SQLException {
	    int currentVersion = 1;

	    String versionSql = "SELECT version FROM health_event WHERE id = ?::uuid AND updated_at = TIMESTAMP '9999-12-31 23:59:59'";
	    try (PreparedStatement stmt = connection.prepareStatement(versionSql)) {
	        stmt.setString(1, healthEvent.getId());
	        try (ResultSet rs = stmt.executeQuery()) {
	            if (rs.next()) {
	                currentVersion = rs.getInt("version");
	            } else {
	                throw new SQLException("No current version found for health_event with ID: " + healthEvent.getId());
	            }
	        }
	    }

	    String closeOldSql = "UPDATE health_event SET updated_at = NOW() " +
	                         "WHERE id = ?::uuid AND version = ? AND updated_at = TIMESTAMP '9999-12-31 23:59:59'";
	    try (PreparedStatement stmt = connection.prepareStatement(closeOldSql)) {
	        stmt.setString(1, healthEvent.getId());
	        stmt.setInt(2, currentVersion);
	        stmt.executeUpdate();
	    }

	    String insertSql = "INSERT INTO health_event (id, type, datetime, description, recommendation, price, vet_station_id, pet_id, verified, version, created_at, updated_at, created_by) " +
	                       "VALUES (?::uuid, ?::health_event_type, ?, ?, ?, ?, ?::uuid, ?::uuid, ?, ?, NOW(), TIMESTAMP '9999-12-31 23:59:59', ?::uuid)";

	    try (PreparedStatement stmt = connection.prepareStatement(insertSql)) {
	        stmt.setString(1, healthEvent.getId());
	        stmt.setString(2, healthEvent.getTypeHE().toString());
	        stmt.setTimestamp(3, Timestamp.valueOf(healthEvent.getDateTime()));
	        stmt.setString(4, healthEvent.getDescription());
	        stmt.setString(5, healthEvent.getRecommendation());
	        stmt.setBigDecimal(6, healthEvent.getPrice());
	        stmt.setString(7, healthEvent.getVetStationId());
	        stmt.setString(8, healthEvent.getPetId());
	        stmt.setBoolean(9, healthEvent.isVerified());
	        stmt.setInt(10, currentVersion + 1);
	        stmt.setString(11, healthEvent.getCreatedBy());
	        stmt.executeUpdate();
	    }
	}

	
	public boolean deleteHealthEvent(String healthEventId) throws SQLException
	{
		String sql = "DELETE FROM health_event WHERE id = ?::uuid";
			try (PreparedStatement stmt = connection.prepareStatement(sql)){
				stmt.setString(1, healthEventId);
				int rowsAffected = stmt.executeUpdate();
				return rowsAffected > 0;
			}
		}
	public boolean deleteHealthEventByPetId(String petId) throws SQLException
	{
		String sql = "DELETE FROM health_event WHERE id = ?::uuid";
			try (PreparedStatement stmt = connection.prepareStatement(sql)){
				stmt.setString(1, petId);
				int rowsAffected = stmt.executeUpdate();
				return rowsAffected > 0;
			}
		}
	public boolean deleteHealthEventsByVs(String vetStationId) throws SQLException
	{
		String sql = "DELETE FROM health_event WHERE vet_station_id = ?::uuid";
			try (PreparedStatement stmt = connection.prepareStatement(sql)){
				stmt.setString(1, vetStationId);
				int rowsAffected = stmt.executeUpdate();
				return rowsAffected > 0;
			}
		}
}
