package pets.repository.dao;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;

import pets.model.Pet;
import pets.model.VetStation;
import pets.model.enums.MedicalSpecialties;
import pets.model.enums.PetType;

public interface VetStationDAO {
	public void insertVetStation(VetStation vs) throws SQLException;
	public void insertVetStationSpecies(VetStation vs) throws SQLException;
	public void insertVetStationSpecialties(VetStation vs) throws SQLException;
	public VetStation getVetStation(String vetStationId) throws SQLException;
	public List<VetStation> getVetStationsByPetType(String petType) throws SQLException;
	public List<Pet> viewRegisteredPets(String userId, String personId) throws SQLException;
	public List<MedicalSpecialties> getVetStationSpecialties(String vetStationId) throws SQLException;
	public List<PetType> getVetStationSpecies(String vetStationId) throws SQLException;
	public boolean deleteVetStationSpecialties(String vetStationId, Set<MedicalSpecialties> specialtiesToDelete, String userId) throws SQLException;
	public boolean updateEmergencyAvailability(String vetStationId, boolean bool, String userId) throws SQLException;
}
