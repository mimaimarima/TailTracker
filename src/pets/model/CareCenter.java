package pets.model;

import java.sql.Timestamp;
import java.util.Set;

import pets.model.enums.Facilities;
import pets.model.enums.LegalSize;
import pets.model.enums.LegalType;
import pets.model.enums.PersonType;

public class CareCenter extends Legal{
    private Set<Facilities> facilities;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private String createdBy;
    private int version;
	public CareCenter(String name, String number, boolean status, String addressId, String userId, String email,
			PersonType personType, String tin, String licenceId, String dailyWorkHours, 
			LegalSize size, LegalType type, Set<Facilities> facilities) {

		setFacilities(facilities);
	}

	public CareCenter()
	{
		
	}

	public Set<Facilities> getFacilities() {
		return facilities;
	}

	public void setFacilities(Set<Facilities> facilities) {
        if (facilities == null || facilities.isEmpty()) {
            throw new IllegalArgumentException("Please enter numbers corresponding to the list.");
        }

        for (Facilities facility : facilities) {
            if (facility != Facilities.GROOMING_ROOM && facility != Facilities.PET_SHOP && facility != Facilities.DAILY_STAY && facility != Facilities.TRAINING) {
                throw new IllegalArgumentException("Invalid facility: " + facility);
            }
        }

        this.facilities = facilities;
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

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}    
	
	
}
