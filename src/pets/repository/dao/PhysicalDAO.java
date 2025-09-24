package pets.repository.dao;

import java.sql.SQLException;
import java.util.List;

import pets.model.Physical;

public interface PhysicalDAO {
	public Physical getPhysicalById(String physicalId) throws SQLException;
	public List<Physical> getAllPhysical() throws SQLException;
	public void updatePhysical(Physical physical) throws SQLException;
	public boolean deletePhysical(String personId) throws SQLException;
	void insert(Physical physical) throws SQLException;
	
}