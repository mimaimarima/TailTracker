package pets.repository.dao;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import pets.model.HealthEvent;
import pets.model.Pet;

public interface HealthEventDAO {

	public void insert(HealthEvent healthEvent) throws SQLException;
	public List<HealthEvent> getHealthEventsByPet(String petId) throws SQLException;
	public Map<Pet, List<HealthEvent>> getHealthEventsByVetStation(String vetStationId) throws SQLException;
	public Map<Pet, List<HealthEvent>> getUnverifiedHealthEvents(String vetStationId) throws SQLException ;
	public void verifyHealthEvent(String healthEventId) throws SQLException;
	public void updateHealthEvent(HealthEvent healthEvent) throws SQLException;
	public boolean deleteHealthEvent(String healthEventId) throws SQLException;
}
