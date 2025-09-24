package pets.repository.dao;

import java.sql.SQLException;

import pets.model.Sesion;

public interface SesionDAO {
	void insert (Sesion ses) throws SQLException;
}
