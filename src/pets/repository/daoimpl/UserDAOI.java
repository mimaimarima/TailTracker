package pets.repository.daoimpl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import pets.model.User;
import pets.repository.dao.UserDAO;

public class UserDAOI implements UserDAO {

	Connection connection;
	public UserDAOI(Connection connection)
	{
		this.connection = connection;
	}
	
	 public void insertUser(User user) throws SQLException {
	    
	        if (user.getId() == null) {
	            user.setId(UUID.randomUUID().toString());
	        }

	        String sql = "INSERT INTO app_user (user_id, version, email, password, name, surname, username, created_at, updated_at) " +
	                "VALUES (?::uuid, 1, ?, ?, ?, ?, ?, now(), TIMESTAMP '9999-12-31 23:59:59') RETURNING user_id";

	        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
	            stmt.setString(1, user.getId());
	            stmt.setString(2, user.getEmail());
	            stmt.setString(3, user.getPassword());
	            stmt.setString(4, user.getName());
	            stmt.setString(5, user.getSurname());
	            stmt.setString(6, user.getUsername());

	            ResultSet rs = stmt.executeQuery();
	            if (rs.next()) {
	                user.setId(rs.getString("user_id"));
	                user.setVersion(1);
	            }
	        }
	    }

	    public String getIdFromUsername(String username) throws SQLException {
	        String sql = "SELECT user_id FROM app_user WHERE username = ? AND updated_at = TIMESTAMP '9999-12-31 23:59:59'";
	        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
	            stmt.setString(1, username);
	            ResultSet rs = stmt.executeQuery();
	            if (rs.next()) {
	                return rs.getString("user_id");
	            } else {
	                return null;
	            }
	        }
	    }

	    public User getUserByUsername(String username) throws SQLException {
	        String sql = "SELECT * FROM app_user WHERE username = ? AND updated_at = TIMESTAMP '9999-12-31 23:59:59'";
	        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
	            stmt.setString(1, username);
	            try (ResultSet rs = stmt.executeQuery()) {
	                if (rs.next()) {
	                    User user = new User();
	                    user.setId(rs.getString("user_id"));
	                    user.setEmail(rs.getString("email"));
	                    user.setPassword(rs.getString("password"));
	                    user.setName(rs.getString("name"));
	                    user.setSurname(rs.getString("surname"));
	                    user.setUsername(rs.getString("username"));
	                    user.setCreatedAt(rs.getTimestamp("created_at"));
	                    user.setUpdatedAt(rs.getTimestamp("updated_at"));
	                    user.setVersion(rs.getInt("version"));
	                    return user;
	                }
	            }
	        }
	        return null;
	    }

	    public User getUserById(String userId) throws SQLException {
	        String sql = "SELECT * FROM app_user WHERE user_id = ?::uuid AND updated_at = TIMESTAMP '9999-12-31 23:59:59'";
	        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
	            stmt.setString(1, userId);
	            try (ResultSet rs = stmt.executeQuery()) {
	                if (rs.next()) {
	                    User user = new User();
	                    user.setId(rs.getString("user_id"));
	                    user.setEmail(rs.getString("email"));
	                    user.setPassword(rs.getString("password"));
	                    user.setName(rs.getString("name"));
	                    user.setSurname(rs.getString("surname"));
	                    user.setUsername(rs.getString("username"));
	                    user.setCreatedAt(rs.getTimestamp("created_at"));
	                    user.setUpdatedAt(rs.getTimestamp("updated_at"));
	                    user.setVersion(rs.getInt("version"));
	                    return user;
	                }
	            }
	        }
	        return null;
	    }

	    public User getUserByEmail(String email) throws SQLException {
	        String sql = "SELECT user_id, username, password, name, surname, email " +
	                "FROM app_user WHERE email = ? AND updated_at = TIMESTAMP '9999-12-31 23:59:59'";

	        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
	            stmt.setString(1, email);
	            try (ResultSet rs = stmt.executeQuery()) {
	                if (rs.next()) {
	                    User user = new User();
	                    user.setId(rs.getString("user_id"));
	                    user.setUsername(rs.getString("username"));
	                    user.setPassword(rs.getString("password"));
	                    user.setName(rs.getString("name"));
	                    user.setSurname(rs.getString("surname"));
	                    user.setEmail(rs.getString("email"));
	                    return user;
	                }
	            }
	        }
	        return null;
	    }

	    public Map<User, String> getAllUsersWithRoles(String roleNameFilter) throws SQLException {
	        String sql = "SELECT u.*, r.name as role_name " +
	                "FROM app_user u " +
	                "JOIN user_role ur ON u.user_id = ur.user_id " +
	                "JOIN role r on r.id = ur.role_id " +
	                "WHERE u.updated_at = TIMESTAMP '9999-12-31 23:59:59' AND r.name != 'admin'";

	        if (roleNameFilter != null) {
	            sql += " AND r.name = ?";
	        }

	        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
	            if (roleNameFilter != null) {
	                stmt.setString(1, roleNameFilter);
	            }

	            try (ResultSet rs = stmt.executeQuery()) {
	                Map<User, String> userToRoleName = new LinkedHashMap<>();
	                while (rs.next()) {
	                    User user = new User();
	                    user.setId(rs.getString("user_id"));
	                    user.setName(rs.getString("name"));
	                    user.setSurname(rs.getString("surname"));
	                    user.setEmail(rs.getString("email"));
	                    user.setUsername(rs.getString("username"));
	                    userToRoleName.put(user, rs.getString("role_name"));
	                }
	                return userToRoleName;
	            }
	        }
	    }

	    public User updateUser(User user) throws SQLException {
	        connection.setAutoCommit(false); 

	        try {
	        
	            String versionSql = "SELECT version FROM app_user WHERE user_id = ?::uuid AND updated_at = TIMESTAMP '9999-12-31 23:59:59'";
	            int latestVersion = 0;

	            try (PreparedStatement stmt = connection.prepareStatement(versionSql)) {
	                stmt.setString(1, user.getId());
	                ResultSet rs = stmt.executeQuery();
	                if (rs.next()) {
	                    latestVersion = rs.getInt("version");
	                } else {
	                    throw new SQLException("Latest version of user not found.");
	                }
	            }

	   
	            String updateOld = "UPDATE app_user SET updated_at = now() WHERE user_id = ?::uuid AND version = ?";
	            int rowsAffected;
	            try (PreparedStatement stmt = connection.prepareStatement(updateOld)) {
	                stmt.setString(1, user.getId());
	                stmt.setInt(2, latestVersion);
	                rowsAffected = stmt.executeUpdate();
	            }

	            if (rowsAffected == 0) {
	                connection.rollback();
	                throw new SQLException("Failed to close previous user version.");
	            }

	            
	            String insertNew = "INSERT INTO app_user (user_id, version, email, password, name, surname, username, created_at, updated_at) " +
	                    "VALUES (?::uuid, ?, ?, ?, ?, ?, ?, now(), TIMESTAMP '9999-12-31 23:59:59')";
	            try (PreparedStatement stmt = connection.prepareStatement(insertNew)) {
	                stmt.setString(1, user.getId());
	                stmt.setInt(2, latestVersion + 1);
	                stmt.setString(3, user.getEmail());
	                stmt.setString(4, user.getPassword());
	                stmt.setString(5, user.getName());
	                stmt.setString(6, user.getSurname());
	                stmt.setString(7, user.getUsername());
	                stmt.executeUpdate();
	            }

	            user.setVersion(latestVersion + 1);
	            connection.commit();
	            return user;

	        } catch (SQLException e) {
	            connection.rollback(); 
	            throw e;
	        } finally {
	            connection.setAutoCommit(true); 
	        }
	    }

	    public void deleteUser(String userId) throws SQLException {
	        String sql = "DELETE FROM app_user WHERE user_id = ?::uuid";
	        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
	            stmt.setString(1, userId);
	            stmt.executeUpdate();
	        }
	    }

}
   
