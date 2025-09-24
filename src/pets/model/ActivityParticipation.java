package pets.model;

public class ActivityParticipation {
	private String petId;
    private String activityId;
    
	public ActivityParticipation(String petId, String activityId) {
		setPetId(petId);
		setActivityId(activityId);
	}
	
	public ActivityParticipation() {
		// TODO Auto-generated constructor stub
	}

	public String getPetId() {
		return petId;
	}
	public void setPetId(String petId) {
		this.petId = petId;
	}
	public String getActivityId() {
		return activityId;
	}
	public void setActivityId(String activityId) {
		this.activityId = activityId;
	}
    
}
