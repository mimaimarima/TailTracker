package pets.repository.dao;

import java.sql.SQLException;

import pets.model.Address;

public interface AddressDAO {
	void insert(Address address) throws SQLException;
	public void updateAddress(Address address) throws SQLException;
}
