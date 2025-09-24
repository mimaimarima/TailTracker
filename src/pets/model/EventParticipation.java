package pets.model;

import java.sql.Timestamp;

public class EventParticipation {
	
    private String petId;
    private String eventId;
    private String reward;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private int version;
    private String createdBy;

	public EventParticipation(String petId, String eventId, String reward) {
		setPetId(petId);
		setEventId(eventId);
		setReward(reward);
	}
	
	public EventParticipation() {
		// TODO Auto-generated constructor stub
	}

	public String getPetId() {
		return petId;
	}
	public void setPetId(String petId) {
		this.petId = petId;
	}
	public String getEventId() {
		return eventId;
	}
	public void setEventId(String eventId) {
		this.eventId = eventId;
	}
	public String getReward() {
		return reward;
	}
	public void setReward(String reward) {
        if (reward!=null && reward.length() > 40)
            throw new IllegalArgumentException("Reward cannot be longer than 40 characters.");
        if (reward != null && !reward.matches("^[\\p{L}\\p{N}\\s.,\\-/\\\\]+$")) {
            throw new IllegalArgumentException("Invalid information.");
        }
		this.reward = reward;
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
