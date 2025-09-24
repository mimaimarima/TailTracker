package pets.repository.dao;

import java.sql.SQLException;
import java.util.Map;

import pets.model.User;

public interface UserDAO {
	void insertUser(User user) throws SQLException;
	User getUserByUsername(String username) throws SQLException;	
	public String getIdFromUsername(String username) throws SQLException;
	public User getUserById(String userId) throws SQLException;
	public User getUserByEmail(String email) throws SQLException;
	public Map<User, String> getAllUsersWithRoles(String roleNameFilter) throws SQLException;
	public User updateUser(User user) throws SQLException;
	public void deleteUser(String userId) throws SQLException;
}