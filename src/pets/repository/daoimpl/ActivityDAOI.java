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

import pets.model.Activity;
import pets.model.Pet;
import pets.model.enums.ActivityType;
import pets.model.enums.PetType;
import pets.repository.dao.ActivityDAO;

public class ActivityDAOI implements ActivityDAO {

	private Connection connection;

	public ActivityDAOI(Connection connection) {
		this.connection = connection;
	}

	public void insert(Activity activity) throws SQLException {
	    String sql = "INSERT INTO activity (activity_id, type, datetime_start, datetime_end, price, description, " +
	                 "recommendation, care_center_id, pet_id, version, created_at, updated_at, created_by) " +
	                 "VALUES (?::uuid, ?::activity_type, ?, ?, ?, ?, ?, ?::uuid, ?::uuid, 1, NOW(), " +
	                 "TIMESTAMP '9999-12-31 23:59:59', ?::uuid)";

	    String activityId = UUID.randomUUID().toString();
	    activity.setId(activityId);

	    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
	        stmt.setString(1, activityId);
	        stmt.setString(2, activity.getTypeA().toString());
	        stmt.setTimestamp(3, Timestamp.valueOf(activity.getDateTimeStart()));
	        stmt.setTimestamp(4, Timestamp.valueOf(activity.getDateTimeEnd()));
	        stmt.setBigDecimal(5, activity.getPrice());
	        stmt.setString(6, activity.getDescription());
	        stmt.setString(7, activity.getRecommendation());
	        stmt.setString(8, activity.getCareCenterId());
	        stmt.setString(9, activity.getPetId());
	        stmt.setString(10, activity.getCreatedBy());

	        stmt.executeUpdate();
	    }
	}
	public Map<Pet, List<Activity>> getActivitiesByCareCenter(String careCenterId) throws SQLException
	{
		if (careCenterId == null || careCenterId.isEmpty()) {
			throw new IllegalArgumentException("Care Center ID cannot be null or empty");
		}
		
		String sql = "SELECT a.*, p.id as pet_id, p.name as pet_name, p.pet_type, p.date_of_birth "
				+ "FROM activity a "
				+ "JOIN pet p ON p.id = a.pet_id "
				+ "WHERE a.updated_at = TIMESTAMP '9999-12-31 23:59:59'"
				+ " AND p.updated_at = TIMESTAMP '9999-12-31 23:59:59'"
				+ " AND a.care_center_id = ?::uuid "
				+ "ORDER BY p.name, a.datetime_end DESC";
		
		try (PreparedStatement stmt = connection.prepareStatement(sql))
		{
			stmt.setObject(1, careCenterId);
			ResultSet rs = stmt.executeQuery();
			
			Map<Pet, List<Activity>> petToActivities = new LinkedHashMap<>();
			
			while (rs.next())
			{
				Pet pet = new Pet();
				pet.setId(rs.getString("pet_id"));
				pet.setName(rs.getString("pet_name"));
				pet.setPetType(PetType.valueOf(rs.getString("pet_type")));
				pet.setDateOfBirth(rs.getDate("date_of_birth").toLocalDate());
				Activity activity = new Activity();
				activity.setId(rs.getString("activity_id"));
				activity.setTypeA(ActivityType.valueOf(rs.getString("type")));
				activity.setDescription(rs.getString("description"));
				activity.setRecommendation(rs.getString("recommendation"));
				activity.setPrice(rs.getBigDecimal("price"));
				Timestamp startTimestamp = rs.getTimestamp("datetime_start");
				activity.setDateTimeStart(startTimestamp.toLocalDateTime());
				Timestamp endTimestamp = rs.getTimestamp("datetime_end");
				activity.setDateTimeEnd(endTimestamp.toLocalDateTime());

				petToActivities.computeIfAbsent(pet, k-> new ArrayList<>()).add(activity);
			}
			return petToActivities;
		}
	}
	
	public List<Activity> getActivitiesByPet(String petId) throws SQLException {
		if (petId == null || petId.isEmpty()) {
			throw new IllegalArgumentException("Pet ID cannot be null or empty");
		}
		String sql = "SELECT a.* FROM activity a WHERE a.pet_id = ?::uuid AND a.updated_at = TIMESTAMP '9999-12-31 23:59:59' ORDER BY a.datetime_end DESC";

		try (PreparedStatement stmt = connection.prepareStatement(sql)) {

			stmt.setString(1, petId);

			try (ResultSet rs = stmt.executeQuery()) {
				List<Activity> aS = new ArrayList<>();
				while (rs.next()) {
					Activity a = new Activity();
					a.setId(rs.getString("activity_id"));
					a.setTypeA(ActivityType.valueOf(rs.getString("type")));
					a.setDescription(rs.getString("description"));
					a.setRecommendation(rs.getString("recommendation"));
					a.setPrice(rs.getBigDecimal("price"));
					Timestamp startTimestamp = rs.getTimestamp("datetime_start");
					a.setDateTimeStart(startTimestamp.toLocalDateTime());
					Timestamp endTimestamp = rs.getTimestamp("datetime_end");
					a.setDateTimeEnd(endTimestamp.toLocalDateTime());

					aS.add(a);
				}
				return aS;
			}
		}
	}
	
	public void update(Activity activity) throws SQLException {
	    int currentVersion = 1;

	    String versionSql = "SELECT version FROM activity WHERE activity_id = ?::uuid AND updated_at = TIMESTAMP '9999-12-31 23:59:59'";
	    try (PreparedStatement stmt = connection.prepareStatement(versionSql)) {
	        stmt.setString(1, activity.getId());
	        try (ResultSet rs = stmt.executeQuery()) {
	            if (rs.next()) {
	                currentVersion = rs.getInt("version");
	            } else {
	                throw new SQLException("No current version found for activity with ID: " + activity.getId());
	            }
	        }
	    }

	    String closeOldSql = "UPDATE activity SET updated_at = NOW() " +
	                         "WHERE activity_id = ?::uuid AND version = ? AND updated_at = TIMESTAMP '9999-12-31 23:59:59'";
	    try (PreparedStatement stmt = connection.prepareStatement(closeOldSql)) {
	        stmt.setString(1, activity.getId());
	        stmt.setInt(2, currentVersion);
	        stmt.executeUpdate();
	    }

	    String insertSql = "INSERT INTO activity (activity_id, type, datetime_start, datetime_end, price, description, recommendation, care_center_id, pet_id, version, created_at, updated_at, created_by) " +
	                       "VALUES (?::uuid, ?::activity_type, ?, ?, ?, ?, ?, ?::uuid, ?::uuid, ?, NOW(), TIMESTAMP '9999-12-31 23:59:59', ?::uuid)";
	    try (PreparedStatement stmt = connection.prepareStatement(insertSql)) {
	        stmt.setString(1, activity.getId());
	        stmt.setString(2, activity.getTypeA().toString());
	        stmt.setTimestamp(3, Timestamp.valueOf(activity.getDateTimeStart()));
	        stmt.setTimestamp(4, Timestamp.valueOf(activity.getDateTimeEnd()));
	        stmt.setBigDecimal(5, activity.getPrice());
	        stmt.setString(6, activity.getDescription());
	        stmt.setString(7, activity.getRecommendation());
	        stmt.setString(8, activity.getCareCenterId());
	        stmt.setString(9, activity.getPetId());
	        stmt.setInt(10, currentVersion + 1);
	        stmt.setString(11, activity.getCreatedBy());

	        stmt.executeUpdate();
	    }
	}

	public boolean delete(String activityId) throws SQLException {
		String sql = "DELETE FROM activity WHERE activity_id = ?::uuid";
		
		try(PreparedStatement stmt = connection.prepareStatement(sql)) {
			stmt.setString(1, activityId);
			int rowsAffected = stmt.executeUpdate();
			return rowsAffected > 0;
		}
	}

	public boolean deleteActivityByPetId(String petId) throws SQLException {
		String sql = "DELETE FROM activity WHERE pet_id = ?::uuid";
		
		try(PreparedStatement stmt = connection.prepareStatement(sql)) {
			stmt.setString(1, petId);
			int rowsAffected = stmt.executeUpdate();
			return rowsAffected > 0;
		}		
	}

	public boolean deleteActivitiesByCC(String careCenterId) throws SQLException {
		String sql = "DELETE FROM activity WHERE care_center_id = ?::uuid";
		
		try(PreparedStatement stmt = connection.prepareStatement(sql)) {
			stmt.setString(1, careCenterId);
			int rowsAffected = stmt.executeUpdate();
			return rowsAffected > 0;
		}	
	}
}
	
