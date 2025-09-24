package pets.repository.daoimpl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import pets.model.Event;
import pets.model.EventParticipation;
import pets.model.enums.EventType;
import pets.repository.dao.EventDAO;

public class EventDAOI implements EventDAO {
	
	private Connection connection;


	public EventDAOI(Connection connection) {
		this.connection = connection;
	}

	public void insert(Event event) throws SQLException {

		if (event.getId() == null || event.getId().isEmpty()) {
	        event.setId(UUID.randomUUID().toString());
	    }

	    String sql = "INSERT INTO event (id, address_id, name, date_time, event_type, created_at, updated_at, created_by) " +
	                 "VALUES (?::uuid, ?::uuid, ?, ?, ?::event_type, NOW(), TIMESTAMP '9999-12-31 23:59:59', ?::uuid)";
	    
	    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
	        stmt.setString(1, event.getId());
	        stmt.setString(2, event.getAddressId());
	        stmt.setString(3, event.getName());
	        stmt.setTimestamp(4, Timestamp.valueOf(event.getDateTime()));
	        stmt.setString(5, event.getEventType().toString());
	        stmt.setString(6, event.getCreatedBy());

	        stmt.executeUpdate();
	    }
	}

	public void update(Event event) throws SQLException {
	    String checkSql = "SELECT address_id, name, date_time, event_type, version " +
	                     "FROM event WHERE id = ?::uuid AND updated_at = TIMESTAMP '9999-12-31 23:59:59'";
	    
	    try (PreparedStatement stmt = connection.prepareStatement(checkSql)) {
	        stmt.setString(1, event.getId());
	        try (ResultSet rs = stmt.executeQuery()) {
	            if (rs.next()) {
	                boolean valuesChanged = !rs.getString("address_id").equals(event.getAddressId()) ||
	                                      !rs.getString("name").equals(event.getName()) ||
	                                      !rs.getTimestamp("date_time").toLocalDateTime().equals(event.getDateTime()) ||
	                                      !rs.getString("event_type").equals(event.getEventType().toString());
	                
	                if (!valuesChanged) {
	             //       System.out.println("Event values unchanged - no update needed");
	                    return;
	                }
	                
	                int currentVersion = rs.getInt("version");
	                
	                String closeOldVersionSql = "UPDATE event SET updated_at = NOW() " +
	                                         "WHERE id = ?::uuid AND version = ?";
	                try (PreparedStatement closeStmt = connection.prepareStatement(closeOldVersionSql)) {
	                    closeStmt.setString(1, event.getId());
	                    closeStmt.setInt(2, currentVersion);
	                    closeStmt.executeUpdate();
	                }
	                
	                String insertSql = "INSERT INTO event (id, address_id, name, date_time, event_type, " +
	                                 "version, created_at, updated_at, created_by) " +
	                                 "VALUES (?::uuid, ?::uuid, ?, ?, ?::event_type, ?, " +
	                                 "NOW(), TIMESTAMP '9999-12-31 23:59:59', ?::uuid)";
	                try (PreparedStatement insertStmt = connection.prepareStatement(insertSql)) {
	                    insertStmt.setString(1, event.getId());
	                    insertStmt.setString(2, event.getAddressId());
	                    insertStmt.setString(3, event.getName());
	                    insertStmt.setTimestamp(4, Timestamp.valueOf(event.getDateTime()));
	                    insertStmt.setString(5, event.getEventType().toString());
	                    insertStmt.setInt(6, currentVersion + 1);
	                    insertStmt.setString(7, event.getCreatedBy());
	                    insertStmt.executeUpdate();
	                }
	            } else {
	                throw new SQLException("No current version found for event with ID: " + event.getId());
	            }
	        }
	    }
	}


	public Event getEvent(String eventId) throws SQLException {
		
		String sql = "SELECT e.* FROM event e WHERE e.id = ?::uuid AND e.updated_at = TIMESTAMP '9999-12-31 23:59:59'";
		try (PreparedStatement stmt = connection.prepareStatement(sql)) {
			stmt.setString(1, eventId);
			try (ResultSet rs = stmt.executeQuery()) {
	            if (rs.next()) {
	            	Event event = new Event();
	            	event.setId(rs.getString("id"));
	            	event.setName(rs.getString("name"));
					event.setEventType(EventType.valueOf(rs.getString("event_type")));
					event.setAddressId(rs.getString("address_id"));
		            java.sql.Timestamp dt = rs.getTimestamp("date_time");
		            event.setDateTime(dt != null ? dt.toLocalDateTime() : null);
		            
					return event;
	            }
		}
	}
		return null;
	}
	
	public List<Event> getAllEvents() throws SQLException
	{
		String sql = "SELECT e.* FROM event e WHERE e.updated_at = TIMESTAMP '9999-12-31 23:59:59' ORDER BY e.date_time DESC";

		   try (PreparedStatement stmt = connection.prepareStatement(sql)) {

		       try (ResultSet rs = stmt.executeQuery()) {
		           List<Event> events = new ArrayList<>();
		           while (rs.next()) {
		               Event event = new Event();
		               event.setAddressId(rs.getString("address_id"));
		               event.setEventType(EventType.valueOf(rs.getString("event_type")));
		               event.setName(rs.getString("name"));
		               
		               Timestamp timestamp = rs.getTimestamp("date_time");
		               event.setDateTime(timestamp != null ? timestamp.toLocalDateTime() : null);
		               
		               event.setId(rs.getString("id"));
		               events.add(event);
		           }
		           return events;
		         }
   }
}
	public List<Event> getEventsWithoutPet(String petId) throws SQLException // za da ne dade 2 pati da se vnese pet u ist event
	{
		   String sql = """
			        SELECT e.*
			        FROM event e
			        WHERE e.updated_at = TIMESTAMP '9999-12-31 23:59:59'
			          AND e.date_time > (SELECT date_of_birth FROM pet WHERE id = ?::uuid LIMIT 1)
			          AND NOT EXISTS (
			              SELECT 1 FROM event_participation ep
			              WHERE ep.pet_id = ?::uuid
			                AND ep.event_id = e.id
			                AND ep.updated_at = TIMESTAMP '9999-12-31 23:59:59'
			          )
			        ORDER BY e.date_time DESC
			    """;

		   try (PreparedStatement stmt = connection.prepareStatement(sql)) {
		       stmt.setString(1, petId);  
		       stmt.setString(2, petId);
		       try (ResultSet rs = stmt.executeQuery()) {
		           List<Event> events = new ArrayList<>();
		           while (rs.next()) {
		               Event event = new Event();
		               event.setAddressId(rs.getString("address_id"));
		               event.setEventType(EventType.valueOf(rs.getString("event_type")));
		               event.setName(rs.getString("name"));
		               
		               Timestamp timestamp = rs.getTimestamp("date_time");
		               event.setDateTime(timestamp != null ? timestamp.toLocalDateTime() : null);
		               
		               event.setId(rs.getString("id"));
		               events.add(event);
		           }
		           return events;
       }
   }
	}
	public List<Event> getEventsWithPet(String petId) throws SQLException {

	    String sql = """
	            SELECT e.*
	            FROM event e
	            JOIN (
	                SELECT ep.*
	                FROM event_participation ep
	                WHERE ep.updated_at = TIMESTAMP '9999-12-31 23:59:59'
	                  AND ep.pet_id = ?::uuid
	            ) latest_ep ON latest_ep.event_id = e.id
	            WHERE e.updated_at = TIMESTAMP '9999-12-31 23:59:59'
	            ORDER BY e.date_time DESC
	        """; 
	    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
	        stmt.setString(1, petId);
	        try (ResultSet rs = stmt.executeQuery()) {
	            List<Event> events = new ArrayList<>();
	            while (rs.next()) {
	                Event event = new Event();
	                event.setId(rs.getString("id"));
	                event.setName(rs.getString("name"));
	                event.setEventType(EventType.valueOf(rs.getString("event_type")));
	                event.setAddressId(rs.getString("address_id"));

	                Timestamp timestamp = rs.getTimestamp("date_time");
	                event.setDateTime(timestamp != null ? timestamp.toLocalDateTime() : null);

	                events.add(event);
	            }
	            return events;
	        }
	    }
	}

	public void insertEventParticipation(EventParticipation ep) throws SQLException {
	    String sql = "INSERT INTO event_participation (pet_id, event_id, reward, version, created_at, updated_at, created_by) " +
	                 "VALUES (?::uuid, ?::uuid, ?, 1, NOW(), TIMESTAMP '9999-12-31 23:59:59', ?::uuid)";
	    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
	        stmt.setString(1, ep.getPetId());
	        stmt.setString(2, ep.getEventId());
	        stmt.setString(3, ep.getReward());
	        stmt.setString(4, ep.getCreatedBy());
	        stmt.executeUpdate();
	    }
	}
	public EventParticipation getEventParticipation(String petId, String eventId) throws SQLException {
	    String sql = "SELECT * FROM event_participation " +
	                 "WHERE pet_id = ?::uuid AND event_id = ?::uuid AND updated_at = TIMESTAMP '9999-12-31 23:59:59'";
	    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
	        stmt.setString(1, petId);
	        stmt.setString(2, eventId);
	        try (ResultSet rs = stmt.executeQuery()) {
	            if (rs.next()) {
	                EventParticipation ep = new EventParticipation();
	                ep.setPetId(rs.getString("pet_id"));
	                ep.setEventId(rs.getString("event_id"));
	                ep.setReward(rs.getString("reward"));
	                ep.setVersion(rs.getInt("version"));
	                ep.setCreatedBy(rs.getString("created_by"));
	                return ep;
	            }
	        }
	    }
	    return null;
	}
	public List<EventParticipation> getAllEventParticipation() throws SQLException {
	    String sql = "SELECT * FROM event_participation WHERE updated_at = TIMESTAMP '9999-12-31 23:59:59'";
	    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
	        try (ResultSet rs = stmt.executeQuery()) {
	            List<EventParticipation> eventParticipations = new ArrayList<>();
	            while (rs.next()) {
	                EventParticipation ep = new EventParticipation();
	                ep.setEventId(rs.getString("event_id"));
	                ep.setPetId(rs.getString("pet_id"));
	                ep.setReward(rs.getString("reward"));
	                eventParticipations.add(ep);
	            }
	            return eventParticipations;
	        }
	    }
	}

	public void updateEventParticipation(EventParticipation ep) throws SQLException {
	    int currentVersion = 1;

	    String versionSql = "SELECT version FROM event_participation WHERE pet_id = ?::uuid AND event_id = ?::uuid " +
	                        "AND updated_at = TIMESTAMP '9999-12-31 23:59:59'";
	    try (PreparedStatement stmt = connection.prepareStatement(versionSql)) {
	        stmt.setString(1, ep.getPetId());
	        stmt.setString(2, ep.getEventId());
	        try (ResultSet rs = stmt.executeQuery()) {
	            if (rs.next()) {
	                currentVersion = rs.getInt("version");
	            } else {
	                throw new SQLException("No current version found for this participation.");
	            }
	        }
	    }

	    String closeSql = "UPDATE event_participation SET updated_at = NOW() " +
	                      "WHERE pet_id = ?::uuid AND event_id = ?::uuid AND version = ?";
	    try (PreparedStatement stmt = connection.prepareStatement(closeSql)) {
	        stmt.setString(1, ep.getPetId());
	        stmt.setString(2, ep.getEventId());
	        stmt.setInt(3, currentVersion);
	        stmt.executeUpdate();
	    }

	    String insertSql = "INSERT INTO event_participation (pet_id, event_id, reward, version, created_at, updated_at, created_by) " +
	                       "VALUES (?::uuid, ?::uuid, ?, ?, NOW(), TIMESTAMP '9999-12-31 23:59:59', ?::uuid)";
	    try (PreparedStatement stmt = connection.prepareStatement(insertSql)) {
	        stmt.setString(1, ep.getPetId());
	        stmt.setString(2, ep.getEventId());
	        stmt.setString(3, ep.getReward());
	        stmt.setInt(4, currentVersion + 1);
	        stmt.setString(5, ep.getCreatedBy());
	        stmt.executeUpdate();
	    }
	}

	public boolean deleteEventParticipation(EventParticipation ep) throws SQLException {
	    String closeOldSql = "UPDATE event_participation SET updated_at = NOW() " +
	                         "WHERE pet_id = ?::uuid AND event_id = ?::uuid AND updated_at = TIMESTAMP '9999-12-31 23:59:59'";
	    try (PreparedStatement stmt = connection.prepareStatement(closeOldSql)) {
	        stmt.setString(1, ep.getPetId());
	        stmt.setString(2, ep.getEventId());
	        int affected = stmt.executeUpdate();
	        return affected > 0;
	    }
	}

	public boolean updateEventAddress(String eventId, String newAddressId) throws SQLException {
	    String sql = "UPDATE event SET address_id = ?::uuid WHERE id = ?::uuid";
	    
	    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
	        stmt.setString(1, newAddressId);
	        stmt.setString(2, eventId);
	        
	        int rowsAffected = stmt.executeUpdate();
	        return rowsAffected > 0;
	    }
	}
	public boolean deleteEvent(String eventId) throws SQLException {
		String sql = "DELETE FROM event WHERE id = ?::uuid";
		
		try (PreparedStatement stmt = connection.prepareStatement(sql)){
			stmt.setString(1, eventId);
			int rowsAffected = stmt.executeUpdate();
			return rowsAffected > 0;
		}
	}
	public boolean deleteEventParticipationByPetId(String petId) throws SQLException {
		String sql = "DELETE FROM event_participation WHERE pet_id = ?::uuid";
		
		try (PreparedStatement stmt = connection.prepareStatement(sql)){
			stmt.setString(1, petId);
			int rowsAffected = stmt.executeUpdate();
			return rowsAffected > 0;
		}
	}
	public boolean deleteEventParticipationByEventId(String eventId) throws SQLException {
		String sql = "DELETE FROM event_participation WHERE event_id = ?::uuid";
		
		try (PreparedStatement stmt = connection.prepareStatement(sql)){
			stmt.setString(1, eventId);
			int rowsAffected = stmt.executeUpdate();
			return rowsAffected > 0;
		}
	}
}
