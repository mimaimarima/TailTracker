package pets.repository.dao;

import java.sql.SQLException;

import pets.model.CareCenter;

public interface CareCenterDAO {
	public void insertCareCenterFacilities(CareCenter cc) throws SQLException;

}
