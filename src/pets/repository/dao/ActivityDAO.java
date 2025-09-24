package pets.repository.dao;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import pets.model.Activity;
import pets.model.Pet;

public interface ActivityDAO {
	public void insert(Activity activity) throws SQLException;
	public Map<Pet, List<Activity>> getActivitiesByCareCenter(String careCenterId) throws SQLException;
	public List<Activity> getActivitiesByPet(String petId) throws SQLException;
	public void update(Activity activity) throws SQLException;
	public boolean delete(String activityId) throws SQLException; 
	public boolean deleteActivityByPetId(String petId) throws SQLException; 
}
