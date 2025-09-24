package pets.repository.dao;

import java.sql.SQLException;
import java.util.Map;

import pets.model.Passport;
import pets.model.Pet;

public interface PassportDAO {
	
	public void insertPassport(Passport passport) throws SQLException;
	public Map<Pet, Passport> getValidPassports() throws SQLException;
	public void updatePassport(Passport passport) throws SQLException;
	public boolean deletePassport(String id) throws SQLException;
}
