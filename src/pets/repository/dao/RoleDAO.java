package pets.repository.dao;

import java.sql.SQLException;
import java.util.List;

import pets.model.Role;

public interface RoleDAO {
    String getIdByName(String name) throws SQLException;
    String getNameById(String name) throws SQLException;
    public Role getById(String roleId) throws SQLException;
    List<Role> getAllRoles() throws SQLException;
}
