package pets.repository.dao;

import java.sql.SQLException;
import java.util.List;

import pets.model.Person;

public interface PersonDAO {
    String findPersonIdByUserId(String userId) throws SQLException;
//	boolean updatePersonAddress(String personId, String newAddressId) throws SQLException;
	public Person getPerson(String id) throws SQLException;
	public List<Person> getAllPersons() throws SQLException;
	public boolean deletePerson(String personId) throws SQLException;
	void updatePersonWithAddress(Person person) throws SQLException;
	public Person insert(Person person) throws SQLException;
}
