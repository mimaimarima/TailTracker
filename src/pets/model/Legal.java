package pets.model;

import java.sql.Timestamp;

import pets.model.enums.LegalSize;
import pets.model.enums.LegalType;
import pets.model.enums.PersonType;

public class Legal extends Person {
    private String tin;
    private String licenceId;
    private String dailyWorkHours;
    private LegalSize size;
    private LegalType type;
    private String person_id;
    private int version;
    private String createdBy;
    private Timestamp createdAt;
    private Timestamp updatedAt;
	public Legal(String name, String number, boolean status, String addressId, String userId, String email,
			PersonType personType, String tin, String licenceId, String dailyWorkHours,
			LegalSize size, LegalType type, String person_id) {
		super(person_id, name, status, addressId, userId, email, personType);
		this.tin = tin;
		this.licenceId = licenceId;
		this.dailyWorkHours = dailyWorkHours;
		this.size = size;
		this.type = type;
	}

	public Legal() {
	}

	public String getTin() {
		return tin;
	}
	public void setTin(String tin) {
		tin.trim();
		if (tin == null  || tin.length() != 13)
			throw new IllegalArgumentException("Tax number must be 13 digits long.");
		if (!tin.matches("\\d+")) {
		        throw new IllegalArgumentException("Tax number must contain only digits.");
		    }
		this.tin = tin;
	}
	public String getLicenceId() {
		return licenceId;
	}
	public void setLicenceId(String licenceId) {
		licenceId.trim();
		if (!licenceId.matches("^(?=.*?[0-9])(?=.*?[0-9\\-\\/]).{4,13}$")) {
	        throw new IllegalArgumentException("Invalid Licence ID.");
	    }
		this.licenceId = licenceId;
	}
	public String getDailyWorkHours() {
		return dailyWorkHours;
	}
	public void setDailyWorkHours(String dailyWorkHours) {
		this.dailyWorkHours = dailyWorkHours;
	}

	public LegalSize getSize() {
		return size;
	}
	public void setSize(LegalSize size) {
		if (size == null || (size != LegalSize.LARGE && size != LegalSize.MEDIUM && size!= LegalSize.SMALL))
            throw new IllegalArgumentException("Wrong entry");
		this.size = size;
	}
	public LegalType getType() {
		return type;
	}
	public void setType(LegalType type) {
		this.type = type;
	}

	public String getPerson_id() {
		return person_id;
	}

	public void setPerson_id(String person_id) {
		this.person_id = person_id;
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