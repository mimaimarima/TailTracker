package pets.repository.daoimpl;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import pets.model.Legal;
import pets.model.enums.LegalSize;
import pets.model.enums.LegalType;
import pets.repository.dao.LegalDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LegalDAOI implements LegalDAO {

    private final Connection connection;

    public LegalDAOI(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void insert(Legal legal) throws SQLException {
        String sql = """
            INSERT INTO legal (
                legal_id, tin, licence_id, daily_work_hours, size, legal_type, 
                version, created_at, updated_at, created_by
            ) VALUES (?::uuid, ?, ?, ?, ?::legal_size, ?::legal_type, 1, NOW(), TIMESTAMP '9999-12-31 23:59:59', ?::uuid)
            """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, legal.getPerson_id());
            stmt.setString(2, legal.getTin());
            stmt.setString(3, legal.getLicenceId());
            stmt.setString(4, legal.getDailyWorkHours());
            stmt.setString(5, legal.getSize().toString());
            stmt.setString(6, legal.getType().toString());
            stmt.setString(7, legal.getCreatedBy());
            stmt.executeUpdate();
            
       
            legal.setVersion(1);
        }
    }
    public void updateLegal(Legal legal) throws SQLException {
        String checkSql = """
            SELECT tin, licence_id, daily_work_hours, size, legal_type, version
            FROM legal
            WHERE legal_id = ?::uuid AND updated_at = TIMESTAMP '9999-12-31 23:59:59'
        """;

        try (PreparedStatement stmt = connection.prepareStatement(checkSql)) {
            stmt.setString(1, legal.getPerson_id());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    boolean valuesChanged =
                        !rs.getString("tin").equals(legal.getTin()) ||
                        !rs.getString("licence_id").equals(legal.getLicenceId()) ||
                        !rs.getString("daily_work_hours").equals(legal.getDailyWorkHours()) ||
                        !rs.getString("size").equals(legal.getSize().toString()) ||
                        !rs.getString("legal_type").equals(legal.getType().toString());

                    if (!valuesChanged) {
            //            System.out.println("Legal values unchanged - no update needed");
                        return;
                    }

                    int currentVersion = rs.getInt("version");

                    String closeSql = "UPDATE legal SET updated_at = NOW() WHERE legal_id = ?::uuid AND version = ?";
                    try (PreparedStatement closeStmt = connection.prepareStatement(closeSql)) {
                        closeStmt.setString(1, legal.getPerson_id());
                        closeStmt.setInt(2, currentVersion);
                        closeStmt.executeUpdate();
                    }

                    String insertSql = """
                        INSERT INTO legal (
                            legal_id, version, tin, licence_id, daily_work_hours, size, legal_type,
                            created_at, updated_at, created_by
                        ) VALUES (?::uuid, ?, ?, ?, ?, ?::legal_size, ?::legal_type,
                                  NOW(), TIMESTAMP '9999-12-31 23:59:59', ?::uuid)
                    """;

                    try (PreparedStatement insertStmt = connection.prepareStatement(insertSql)) {
                        insertStmt.setString(1, legal.getPerson_id());
                        insertStmt.setInt(2, currentVersion + 1);
                        insertStmt.setString(3, legal.getTin());
                        insertStmt.setString(4, legal.getLicenceId());
                        insertStmt.setString(5, legal.getDailyWorkHours());
                        insertStmt.setString(6, legal.getSize().toString());
                        insertStmt.setString(7, legal.getType().toString());
                        insertStmt.setString(8, legal.getCreatedBy());
                        insertStmt.executeUpdate();
                    }

                    legal.setVersion(currentVersion + 1);
                } else {
                    throw new SQLException("No current version found for legal with ID: " + legal.getPerson_id());
                }
            }
        }
    }

    public Legal getLegal(String personId) throws SQLException {
        String sql = """
            SELECT * FROM legal 
            WHERE legal_id = ?::uuid 
            ORDER BY version DESC 
            LIMIT 1
            """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, personId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Legal legal = new Legal();
                    legal.setPerson_id(rs.getString("legal_id"));
                    legal.setVersion(rs.getInt("version"));
                    legal.setTin(rs.getString("tin"));
                    legal.setLicenceId(rs.getString("licence_id"));
                    legal.setDailyWorkHours(rs.getString("daily_work_hours"));
                    legal.setSize(LegalSize.valueOf(rs.getString("size")));
                    legal.setType(LegalType.valueOf(rs.getString("legal_type")));
                    return legal;
                }
            }
        }
        return null;
    }
    public Legal getLegalByTin(String tin) throws SQLException {
        String sql = """
            SELECT * FROM legal 
            WHERE tin = ?
            AND updated_at = TIMESTAMP '9999-12-31 23:59:59'
            """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, tin);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Legal legal = new Legal();
                    legal.setPerson_id(rs.getString("legal_id"));
                    legal.setVersion(rs.getInt("version"));
                    legal.setTin(rs.getString("tin"));
                    legal.setLicenceId(rs.getString("licence_id"));
                    legal.setDailyWorkHours(rs.getString("daily_work_hours"));
                    legal.setSize(LegalSize.valueOf(rs.getString("size")));
                    legal.setType(LegalType.valueOf(rs.getString("legal_type")));
                    return legal;
                }
            }
        }
        return null;
    }
    public boolean deleteLegal(String personId) throws SQLException {
        String typeSql = "SELECT legal_type FROM legal WHERE legal_id = ?::uuid";
        try (PreparedStatement typeStmt = connection.prepareStatement(typeSql)) {
            typeStmt.setString(1, personId);
            try (ResultSet rs = typeStmt.executeQuery()) {
                if (rs.next()) {
                    String legalType = rs.getString("legal_type");
                    if ("CARE_CENTER".equalsIgnoreCase(legalType)) {
                    
                        String deleteFacilitiesSql = "DELETE FROM care_center_facilities WHERE care_center_id = ?::uuid";
                        try (PreparedStatement deleteFacilitiesStmt = connection.prepareStatement(deleteFacilitiesSql)) {
                            deleteFacilitiesStmt.setString(1, personId);
                            deleteFacilitiesStmt.executeUpdate();
                        }
                    } else if ("VET_STATION".equalsIgnoreCase(legalType)) {  
                       
                        String deleteSpeciesSql = "DELETE FROM vet_station_species WHERE vet_station_id = ?::uuid";
                        try (PreparedStatement deleteSpeciesStmt = connection.prepareStatement(deleteSpeciesSql)) {
                            deleteSpeciesStmt.setString(1, personId);
                            deleteSpeciesStmt.executeUpdate();
                        }
                        
                        String deleteSpecialtiesSql = "DELETE FROM vet_station_specialties WHERE vet_station_id = ?::uuid";
                        try (PreparedStatement deleteSpecialtiesStmt = connection.prepareStatement(deleteSpecialtiesSql)) {
                            deleteSpecialtiesStmt.setString(1, personId);
                            deleteSpecialtiesStmt.executeUpdate();
                        }
                        
                        String deleteVetStationSql = "DELETE FROM vet_station WHERE id = ?::uuid";
                        try (PreparedStatement deleteVetStationStmt = connection.prepareStatement(deleteVetStationSql)) {
                            deleteVetStationStmt.setString(1, personId);
                            deleteVetStationStmt.executeUpdate();
                        }
                    }
                } else {
                    return false;  
                }
            }
        }

     
        String deleteLegalSql = "DELETE FROM legal WHERE legal_id = ?::uuid";
        try (PreparedStatement deleteLegalStmt = connection.prepareStatement(deleteLegalSql)) {
            deleteLegalStmt.setString(1, personId);
            int rowsAffected = deleteLegalStmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    public List<Legal> getAllLegals(LegalType filterType) throws SQLException {
        String sql = "SELECT l.*, p.* FROM legal l " +
                     "JOIN person p ON l.legal_id = p.id " +
                     "WHERE l.updated_at = TIMESTAMP '9999-12-31 23:59:59' ";

        if (filterType != null) {
            sql += "AND l.legal_type = ?::legal_type";
        }

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            if (filterType != null) {
                stmt.setString(1, filterType.name());
            }

            try (ResultSet rs = stmt.executeQuery()) {
                List<Legal> legals = new ArrayList<>();

                while (rs.next()) {
                    Legal legal = new Legal();
                    legal.setPerson_id(rs.getString("legal_id")); 
                    legal.setName(rs.getString("name"));
                    legal.setNumber(rs.getString("number"));
                    legal.setStatus(rs.getBoolean("status"));
                    legal.setAddressId(rs.getString("address_id"));
                    legal.setUserId(rs.getString("user_id"));
                    legal.setEmail(rs.getString("email"));

                    legal.setTin(rs.getString("tin"));
                    legal.setLicenceId(rs.getString("licence_id"));
                    legal.setDailyWorkHours(rs.getString("daily_work_hours"));
                    legal.setSize(LegalSize.valueOf(rs.getString("size")));
                    String legalTypeStr = rs.getString("legal_type");
                    if (legalTypeStr != null) {
                        legal.setType(LegalType.valueOf(legalTypeStr));
                    } else {
                        legal.setType(null); 
                    }

                    legals.add(legal);
                }
                return legals;
            }
        }
    }

}