package pets.repository.daoimpl;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import pets.model.UserRole;
import pets.repository.dao.UserRoleDAO;

public class UserRoleDAOI implements UserRoleDAO {
	
	private final Connection connection;

    public UserRoleDAOI(Connection connection) {
        this.connection = connection;
    }
    
	@Override
	public void insertUserRole(UserRole userRole) throws SQLException {
	    String query = "INSERT INTO user_role (user_id, role_id, date_from, date_to, updated_at) " +
	                   "VALUES (?::uuid, ?::uuid, ?, ?, TIMESTAMP '9999-12-31 23:59:59.000000')";  
	    
	    try (PreparedStatement stmt = connection.prepareStatement(query)) {
	    	stmt.setString(1, userRole.getUserId());
	    	stmt.setString(2, userRole.getRoleId());
	        stmt.setDate(3, Date.valueOf(userRole.getDateHasFrom()));
	        stmt.setDate(4, Date.valueOf(LocalDate.of(9999, 12, 31)));

	        stmt.executeUpdate();
	    }
	}

	@Override
	public List<String> getRoleIdForUserId(String userId) throws SQLException {
	    List<String> list = new ArrayList<>();
	    String query = "SELECT role_id FROM user_role WHERE user_id = ?::uuid";

	    try (PreparedStatement stmt = connection.prepareStatement(query)) {
	        stmt.setString(1, userId);
	        ResultSet rs = stmt.executeQuery();
	        while (rs.next()) {
	            list.add(rs.getString("role_id"));
	        }
	    }
	    return list;
	}
	public void deleteByUserId(String userId) throws SQLException {
	    String sql = "DELETE FROM user_role WHERE user_id = ?::uuid";
	    try (PreparedStatement stmt = connection.prepareStatement(sql)) {
	        stmt.setString(1, userId);
	        stmt.executeUpdate();
	    }
	}

	public UserRole updateUserRole(UserRole userRole) throws SQLException {
		String sql = "UPDATE user_role SET date_to = NOW() where user_id = ?::uuid and role_id = ?:uuid";
		try (PreparedStatement stmt = connection.prepareStatement(sql)) {
			stmt.setString(1, userRole.getUserId());
			stmt.setString(2, userRole.getRoleId());
			stmt.executeUpdate();
		}
		return null;
	}
	public UserRole deleteUserRole(UserRole userRole) throws SQLException {
		String sql = "DELETE FROM user_role WHERE user_id = ?::uuid and role_id = ?::uuid";
		try (PreparedStatement stmt = connection.prepareStatement(sql)) {
			stmt.setString(1, userRole.getUserId());
			stmt.setString(2, userRole.getRoleId());
			stmt.executeUpdate();
		}
		return null;
	}

	@Override
	public List<String> getAllUserRoles(String userId) throws SQLException {
		String sql = "SELECT role_id FROM user_role WHERE user_id = ?::uuid";
		try (PreparedStatement stmt = connection.prepareStatement(sql))
		{
			stmt.setString(1, userId);
			List<String> list = new ArrayList<>();
	        ResultSet rs = stmt.executeQuery();

	        while (rs.next()) {
	        	list.add(rs.getString("role_id"));
	        	
	            
	        }
	        return list;
		}
	}

}
	
