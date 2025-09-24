package pets.repository.dao;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import pets.model.Pet;
import pets.model.PetOwnership;

public interface PetOwnershipDAO {
	public void insertOwner(PetOwnership petOwnership) throws SQLException;
	public PetOwnership getOwnership(String petId) throws SQLException;
	public List<PetOwnership> getAllOwnerships() throws SQLException;
	public List<Pet> viewPetsWithoutOwner() throws SQLException;
	public List<Pet> viewPetsByOwner(String personId) throws SQLException;
	public void updateOwnershipDateTo(String petId, LocalDate dateTo) throws SQLException;
	public boolean deleteOwnership(String petId) throws SQLException;
}
