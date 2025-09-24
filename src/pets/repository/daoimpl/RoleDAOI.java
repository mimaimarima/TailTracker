package pets.repository.daoimpl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import pets.model.Role;
import pets.repository.dao.RoleDAO;

public class RoleDAOI implements RoleDAO {

    private final Connection connection;

    public RoleDAOI(Connection connection) {
        this.connection = connection;
    }

    @Override
    public String getIdByName(String roleName) throws SQLException {
        String query = "SELECT id FROM role WHERE name = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, roleName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getObject("id", UUID.class).toString();
            }
            return null;
        }
    }
    public String getNameById(String id) throws SQLException {
    	String sql = "SELECT name FROM role WHERE id = ?::uuid";
    	try (PreparedStatement stmt = connection.prepareStatement(sql))
    	{
    		stmt.setString(1,  id);
    		ResultSet rs = stmt.executeQuery();
    		if (rs.next()) {
    			return rs.getString("name");
    		} else {
    			return null;
    		}
    	}
    }
    public Role getById(String roleId) throws SQLException {
    	String sql = "SELECT * FROM role WHERE id = ?::uuid";
    	try (PreparedStatement stmt = connection.prepareStatement(sql))
    	{
    		stmt.setString(1, roleId);
    		ResultSet rs = stmt.executeQuery();
    		if (rs.next()) {
    			Role role = new Role();
    			role.setId(rs.getString("id"));
    			role.setDateFrom(rs.getDate("date_from").toLocalDate());		
    			role.setName(rs.getString("name"));
    			role.setDateTo(rs.getDate("date_to").toLocalDate());
    			return role;
    		} else {
    			return null;
    		}
    	}
    }
    public List<Role> getAllRoles() throws SQLException
    {
    	String sql = "SELECT * FROM ROLE";
    	try (PreparedStatement stmt = connection.prepareStatement(sql))
    	{
    		ResultSet rs = stmt.executeQuery();
    		List<Role> list = new ArrayList<>();
    		while (rs.next())
    		{
    			Role role = new Role();
    			role.setName(rs.getString("name"));
    			role.setId(rs.getString("id"));
    			role.setDateFrom(rs.getDate("date_from").toLocalDate());
    			role.setDateTo(rs.getDate("date_to").toLocalDate());
    			list.add(role);
    		}
    		return list;
    	}
    }

}
