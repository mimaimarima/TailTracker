package pets.repository.dao;

import java.sql.SQLException;
import java.util.List;

import pets.model.UserRole;

public interface UserRoleDAO {
	void insertUserRole(UserRole userRole) throws SQLException;
	List<String> getAllUserRoles(String userId) throws SQLException;
	List<String> getRoleIdForUserId(String userId) throws SQLException;
	UserRole updateUserRole(UserRole userRole) throws SQLException;
}
