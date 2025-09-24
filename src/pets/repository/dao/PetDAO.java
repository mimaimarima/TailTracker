package pets.repository.dao;

import pets.model.Pet;

import java.sql.SQLException;
import java.util.List;

public interface PetDAO {
    void insert(Pet pet) throws SQLException;
	public Pet getPetById(String petId) throws SQLException;
	public List<Pet> viewRegisteredPets() throws SQLException;
	public List<Pet> getRegisteredPetsWithoutValidPassports() throws SQLException;
	public List<Pet> getPetsByCC(String careCenterId) throws SQLException;
	public void updatePet(Pet pet) throws SQLException;
	public boolean deletePet(String petId) throws SQLException;
}