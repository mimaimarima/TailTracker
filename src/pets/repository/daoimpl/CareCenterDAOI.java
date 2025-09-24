package pets.repository.daoimpl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import pets.model.CareCenter;
import pets.model.enums.Facilities;
import pets.repository.dao.CareCenterDAO;

public class CareCenterDAOI implements CareCenterDAO {

	private final Connection connection;
	
	public CareCenterDAOI(Connection connection)
	{
		this.connection = connection;
	}
	

	public void insertCareCenterFacilities(CareCenter cc) throws SQLException {
		String getMaxVersionSql = """
	            SELECT facility, MAX(version) as max_version 
	            FROM care_center_facilities 
	            WHERE care_center_id = ?::uuid AND facility = ?::facility_type
	            GROUP BY facility
	            """;
	        
	        String insertSql = """
	            INSERT INTO care_center_facilities (
	                care_center_id, facility, version, created_at, updated_at, created_by
	            ) VALUES (
	                ?::uuid, ?::facility_type, ?, NOW(), TIMESTAMP '9999-12-31 23:59:59', ?::uuid
	            )
	            ON CONFLICT (care_center_id, facility, updated_at) DO NOTHING
	            """;

	        try (PreparedStatement getVersionStmt = connection.prepareStatement(getMaxVersionSql);
	             PreparedStatement insertStmt = connection.prepareStatement(insertSql)) {
	            
	            for (Facilities f : cc.getFacilities()) {
	                int currentVersion = 0;
	                getVersionStmt.setString(1, cc.getPerson_id());
	                getVersionStmt.setString(2, f.name());
	                try (ResultSet rs = getVersionStmt.executeQuery()) {
	                    if (rs.next()) {
	                        currentVersion = rs.getInt("max_version");
	                    }
	                }
	                
	                int newVersion = currentVersion + 1;
	                insertStmt.setString(1, cc.getPerson_id());
	                insertStmt.setString(2, f.name());
	                insertStmt.setInt(3, newVersion);
	                insertStmt.setString(4, cc.getCreatedBy());
	                insertStmt.addBatch();
	                
	                getVersionStmt.clearParameters();
	            }
	            insertStmt.executeBatch();
	        }
	}

	
	public List<Facilities> getCareCenterFacilities(String careCenterId) throws SQLException {
	    String sql = """
	        SELECT facility FROM care_center_facilities 
	        WHERE care_center_id = ?::uuid 
	        AND updated_at = TIMESTAMP '9999-12-31 23:59:59'
	        """;

	    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
	        stmt.setString(1, careCenterId);
	        try (ResultSet rs = stmt.executeQuery()) {
	            List<Facilities> facilities = new ArrayList<>();
	            while (rs.next()) {
	                try {
	                    facilities.add(Facilities.valueOf(rs.getString("facility")));
	                } catch (IllegalArgumentException e) {
	                    System.err.println("Warning: Unknown facility type in database: " + 
	                                     rs.getString("facility"));
	                }
	            }
	            return facilities;
	        }
	    }
	}
	public boolean deleteCareCenterFacilities(String careCenterId, Set<Facilities> facilitiesToDelete, String userId) 
	        throws SQLException {

	    if (facilitiesToDelete == null || facilitiesToDelete.isEmpty()) {
	        return false;
	    }

	    String updateOldVersionSql = """
	        UPDATE care_center_facilities 
	        SET updated_at = NOW(), created_by = ?::uuid
	        WHERE care_center_id = ?::uuid AND facility = ?::facility_type 
	        AND updated_at = TIMESTAMP '9999-12-31 23:59:59'
	        """;

	    try (PreparedStatement stmt = connection.prepareStatement(updateOldVersionSql)) {
	        for (Facilities facility : facilitiesToDelete) {
	        	stmt.setString(1, userId);
	            stmt.setString(2, careCenterId);
	            stmt.setString(3, facility.name());
	            stmt.addBatch();
	        }
	        stmt.executeBatch();
	        return true;
	    }
	}
	public boolean deleteCareCenter(String careCenterId) throws SQLException
	{
		String sql = "DELETE FROM care_center_facilities WHERE id = ?::uuid";
		
		try (PreparedStatement stmt = connection.prepareStatement(sql)){
			stmt.setString(1, careCenterId);
			int rowsAffected = stmt.executeUpdate();
			return rowsAffected > 0;
		}
	}
	
}
