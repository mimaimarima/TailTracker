package pets.repository.dao;

import java.sql.SQLException;
import java.util.List;

import pets.model.Event;
import pets.model.EventParticipation;

public interface EventDAO {
	public void insert(Event event) throws SQLException;
	public void update(Event event) throws SQLException;
	public Event getEvent(String eventId) throws SQLException;
	public List<Event> getAllEvents() throws SQLException;
	public List<Event> getEventsWithoutPet(String petId) throws SQLException;
	public void insertEventParticipation(EventParticipation ep) throws SQLException;
	public boolean updateEventAddress(String eventId, String newAddressId) throws SQLException;
	public boolean deleteEvent(String eventId) throws SQLException;
}
