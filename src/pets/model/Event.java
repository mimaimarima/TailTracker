package pets.model;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import pets.model.enums.EventType;

public class Event {
    private String addressId;
    private String name;
    private LocalDateTime dateTime;
    private String id;
    private EventType eventType;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private int version;
    private String createdBy;

    public Event(String addressId, String name, LocalDateTime dateTime, String id, EventType eventType) {
        setAddressId(addressId);
        setName(name);
        setDateTime(dateTime);
        setEventType(eventType);
    }

    public Event() {
		// TODO Auto-generated constructor stub
	}

	public String getAddressId() {
        return addressId;
    }

    public void setAddressId(String addressId) {
        this.addressId = addressId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.length() > 40)
            throw new IllegalArgumentException("Name cannot be null or longer than 40 characters.");
        if (!name.matches("^(?=.{1,40}$)(?=.*[A-Za-z])(?!.*[\\s'-]{2,})[A-Za-z]+([\\s'-][A-Za-z]+)*$"))
            throw new IllegalArgumentException("Name must contain only letters, spaces, apostrophes, or hyphens and cannot start or end with symbols.");
        this.name = name;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
    	
    	if (dateTime == null) {
            throw new IllegalArgumentException("Date and time must not be null.");
        }
        if (dateTime.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("Date and time cannot be in the future.");
        }
        this.dateTime = dateTime;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
    	if (eventType != EventType.COMPETITION && eventType != EventType.EXHIBITION && eventType != EventType.RACE)
            throw new IllegalArgumentException("Event Type can only be COMPETITION, EXHIBITION or Race.");

        this.eventType = eventType;
    }

	public Timestamp getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Timestamp createdAt) {
		this.createdAt = createdAt;
	}

	public Timestamp getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Timestamp updatedAt) {
		this.updatedAt = updatedAt;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}
    
}
