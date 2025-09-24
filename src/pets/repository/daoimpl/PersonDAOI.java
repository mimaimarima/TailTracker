package pets.repository.daoimpl;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import pets.model.Person;
import pets.model.enums.PersonType;
import pets.repository.dao.PersonDAO;
public class PersonDAOI implements PersonDAO {
    private Connection connection;

    public PersonDAOI(Connection connection) {
        this.connection = connection;
    }


    private boolean isUserIdValid(String userId) throws SQLException {
        String sql = "SELECT 1 FROM app_user WHERE user_id = ?::uuid AND updated_at = TIMESTAMP '9999-12-31 23:59:59'";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    public Person insert(Person person) throws SQLException {

    	if (person.getId() == null) {
            person.setId(UUID.randomUUID().toString());
        }

   /*     if (!isUserIdValid(person.getUserId())) {
            throw new SQLException("Invalid user_id: does not exist or not active in app_user");
        }*/

        String sql = """
            INSERT INTO person 
                (id, name, number, status, address_id, user_id, email, person_type, version, created_at, updated_at, created_by) 
            VALUES 
                (?::uuid, ?, ?, ?, ?::uuid, ?::uuid, ?, ?::person_type, 1, NOW(), TIMESTAMP '9999-12-31 23:59:59', ?::uuid)
            RETURNING id
            """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, person.getId());
            stmt.setString(2, person.getName());
            stmt.setString(3, person.getNumber());
            stmt.setBoolean(4, person.getStatus());
            stmt.setString(5, person.getAddressId());
            stmt.setString(6, person.getUserId());
            stmt.setString(7, person.getEmail());
            stmt.setString(8, person.getPersonType().toString());
            stmt.setString(9, person.getCreatedBy());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    person.setId(rs.getString("id"));
                }
            }
        }
        return person;
    }

    public String findPersonIdByUserId(String userId) throws SQLException {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("User ID cannot be null or empty");
        }

        try {
            String sql = "SELECT id FROM person WHERE user_id = ? ORDER BY created_at DESC LIMIT 1";
            
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setObject(1, UUID.fromString(userId));
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        String id = rs.getString("id");
                        if (id == null) {
                            throw new SQLException("Person record exists but has null ID for user: " + userId);
                        }
                        return id;
                    }
                }
            }
            throw new SQLException("No person record found for user: " + userId);
        } catch (IllegalArgumentException e) {
            throw new SQLException("Invalid UUID format: " + userId, e);
        }
    }

    public Person findPersonByUserId(String userId) throws SQLException {
        String sql = """
            SELECT * FROM person 
            WHERE user_id = ?::uuid AND (updated_at IS NULL OR updated_at = TIMESTAMP '9999-12-31 23:59:59.000000')
            ORDER BY version DESC
            LIMIT 1
        """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                	Person person = new Person();
                    person.setId(rs.getString("id"));
                    person.setName(rs.getString("name"));
                    person.setNumber(rs.getString("number"));
                    person.setUserId(rs.getString("user_id"));
                    person.setEmail(rs.getString("email"));
                    person.setStatus(rs.getBoolean("status"));
                    person.setAddressId(rs.getString("address_id"));
                    person.setPersonType(PersonType.valueOf(rs.getString("person_type")));
                    person.setVersion(rs.getInt("version"));
                    person.setCreatedBy(rs.getString("created_by"));
                    return person;
                }
            }
        }
        return null;
    }
    public Person getPerson(String personId) throws SQLException {
        String sql = """
            SELECT p.*
            FROM person p
            WHERE id = ?::uuid AND p.updated_at >= TIMESTAMP '9999-12-31 23:59:59'
        """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, personId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Person person = new Person();
                    person.setId(rs.getString("id"));
                    person.setName(rs.getString("name"));
                    person.setNumber(rs.getString("number"));
                    person.setUserId(rs.getString("user_id"));
                    person.setEmail(rs.getString("email"));
                    person.setStatus(rs.getBoolean("status"));
                    person.setAddressId(rs.getString("address_id"));
                    person.setPersonType(PersonType.valueOf(rs.getString("person_type")));
                    person.setVersion(rs.getInt("version"));
                    person.setCreatedBy(rs.getString("created_by"));
                    return person;
                }
            }
        }
        return null;
    }

    public void updatePersonWithAddress(Person person) throws SQLException {
        String checkSql = "SELECT name, number, status, address_id, email, version " +
                         "FROM person WHERE id = ?::uuid AND updated_at = TIMESTAMP '9999-12-31 23:59:59'";
        
        try (PreparedStatement stmt = connection.prepareStatement(checkSql)) {
            stmt.setString(1, person.getId());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    boolean valuesChanged = !rs.getString("name").equals(person.getName()) ||
                                          !rs.getString("number").equals(person.getNumber()) ||
                                          rs.getBoolean("status") != person.getStatus() ||
                                          !rs.getString("address_id").equals(person.getAddressId()) ||
                                          !rs.getString("email").equals(person.getEmail());
                    
                    if (!valuesChanged) {
                        System.out.println("Person values unchanged - no update needed");
                        return;
                    }
                    
                    int currentVersion = rs.getInt("version");
                    
                    String closeOldVersionSql = "UPDATE person SET updated_at = NOW() " +
                                             "WHERE id = ?::uuid AND version = ?";
                    try (PreparedStatement closeStmt = connection.prepareStatement(closeOldVersionSql)) {
                        closeStmt.setString(1, person.getId());
                        closeStmt.setInt(2, currentVersion);
                        closeStmt.executeUpdate();
                    }
                    
                    String insertSql = """
                        INSERT INTO person (
                            id, name, number, status, address_id, user_id, email, 
                            person_type, version, created_at, updated_at, created_by
                        ) VALUES (
                            ?::uuid, ?, ?, ?, ?::uuid, ?::uuid, ?, ?::person_type, ?, 
                            NOW(), TIMESTAMP '9999-12-31 23:59:59', ?::uuid
                        )""";
                    
                    try (PreparedStatement insertStmt = connection.prepareStatement(insertSql)) {
                        insertStmt.setString(1, person.getId());
                        insertStmt.setString(2, person.getName());
                        insertStmt.setString(3, person.getNumber());
                        insertStmt.setBoolean(4, person.getStatus());
                        insertStmt.setString(5, person.getAddressId());
                        insertStmt.setString(6, person.getUserId());
                        insertStmt.setString(7, person.getEmail());
                        insertStmt.setString(8, person.getPersonType().toString());
                        insertStmt.setInt(9, currentVersion + 1);
                        insertStmt.setString(10, person.getCreatedBy());
                        insertStmt.executeUpdate();
                    }
                    
                    person.setVersion(currentVersion + 1);
                } else {
                    throw new SQLException("No current version found for person with ID: " + person.getId());
                }
            }
        }
    }

    public List<Person> getAllPersons() throws SQLException {
        String sql = "SELECT * FROM person WHERE updated_at = TIMESTAMP '9999-12-31 23:59:59'";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            try (ResultSet rs = stmt.executeQuery()) {
                List<Person> persons = new ArrayList<>();
                while (rs.next()) {
                    Person person = new Person();
                    person.setId(rs.getString("id"));
                    person.setUserId(rs.getString("user_id"));
                    person.setEmail(rs.getString("email"));
                    person.setName(rs.getString("name"));
                    person.setNumber(rs.getString("number"));
                    person.setStatus(rs.getBoolean("status"));
                    person.setAddressId(rs.getString("address_id"));
                    person.setPersonType(PersonType.valueOf(rs.getString("person_type")));
                    person.setVersion(rs.getInt("version"));
                    persons.add(person);
                }
                return persons;
            }
        }
    }

    public boolean updatePersonName(String name, String personId) throws SQLException {
        String sql = "UPDATE person SET name = ? WHERE id = ?::uuid";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setString(2, personId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    public boolean deletePerson(String personId) throws SQLException {
        String sql = """
            WITH deleted_person AS (DELETE FROM person WHERE id = ?::uuid RETURNING user_id)
            DELETE FROM app_user WHERE user_id IN (SELECT user_id FROM deleted_person)
        """;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, personId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        }
    }
    public void deleteByUserId(String userId) throws SQLException {
        String sql = "DELETE FROM person WHERE user_id = ?::uuid";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, userId);
            stmt.executeUpdate();
        }
    }


    public List<Person> findByUserIdAndRole(String userId, String roleId) throws SQLException {
        String sql = """
            SELECT p.* FROM person p
            JOIN user_role ur ON p.user_id = ur.user_id
            WHERE p.user_id = ?::uuid 
            AND ur.role_id = ?::uuid
            AND (p.updated_at IS NULL OR p.updated_at = TIMESTAMP '9999-12-31 23:59:59.000000')
            AND (ur.updated_at IS NULL OR ur.updated_at = TIMESTAMP '9999-12-31 23:59:59.000000')
            ORDER BY p.version DESC
        """;

        List<Person> persons = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, userId);
            stmt.setString(2, roleId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                	Person person = new Person();
		        	 person.setId(rs.getString("id"));
		             person.setName(rs.getString("name"));
		             person.setNumber(rs.getString("number"));
		             person.setUserId(rs.getString("user_id"));
		             person.setEmail(rs.getString("email"));
		             person.setStatus(rs.getBoolean("status"));
		             person.setAddressId(rs.getString("address_id"));
		             person.setPersonType(PersonType.valueOf(rs.getString("person_type")));
		             person.setVersion(rs.getInt("version"));
		             person.setCreatedBy(rs.getString("created_by"));
                	persons.add(person);
            }
        }
        return persons;
    }
    }
}