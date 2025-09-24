package pets.model;

import java.sql.Timestamp;
import java.time.LocalDate;

public class Passport {
    private String id;
    private String number;
    private LocalDate dateIssue;
    private LocalDate dateEnd;
    private boolean valid;
    private String petId;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private int version;
    private String createdBy;

	public Passport(String number, LocalDate dateIssue, LocalDate dateEnd, String petId, Boolean valid) {
		setNumber(number);
		setDateIssue(dateIssue);
		setDateEnd(dateEnd);
		setPetId(petId);
		setValid(valid);
	}
	public Passport() {
		// TODO Auto-generated constructor stub
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getNumber() {
		return number;
	}
    public void setNumber(String number) {
        if (number == null || number.trim().isEmpty()) {
            throw new IllegalArgumentException("Passport number cannot be null or empty");
        }
        if (!number.matches("^[A-Z]{2}[A-Z0-9]{6,8}$")) {
            throw new IllegalArgumentException("Passport number must follow format of 2-letter country code and 6-8 alphanumeric characters (eg. DE123456, FRABC1234).");
        }
        this.number = number.toUpperCase(); 
    }
	public LocalDate getDateIssue() {
		return dateIssue;
	}
    public void setDateIssue(LocalDate dateIssue) {
        if (dateIssue == null) {
            throw new IllegalArgumentException("Date of issue cannot be null.");
        }
        if (dateIssue.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Date of issue cannot be in the future.");
        }
        if (dateIssue.isBefore(LocalDate.now().minusYears(30))) {
            throw new IllegalArgumentException("Date of issue cannot be more than 30 years in the past.");
        }
        this.dateIssue = dateIssue;
    }

	public LocalDate getDateEnd() {
		return dateEnd;
	}

    public void setDateEnd(LocalDate dateEnd) {
        if (dateEnd == null) {
            throw new IllegalArgumentException("Date of end cannot be null.");
        }
        if (dateIssue != null && dateEnd.isBefore(dateIssue)) {
            throw new IllegalArgumentException("End date cannot be before issue date.");
        }
        if (dateIssue != null && !dateEnd.isAfter(dateIssue)) {
            throw new IllegalArgumentException("End date must be after issue date.");
        }
        if (dateIssue != null && dateEnd.isAfter(dateIssue.plusYears(5))) {
            throw new IllegalArgumentException("Passport cannot be valid for more than 5 years.");
        }
		if (dateEnd.isAfter(LocalDate.now()))
		{
			setValid(true);
		}
        this.dateEnd = dateEnd;
    }
	public String getPetId() {
		return petId;
	}
	public void setPetId(String petId) {
		this.petId = petId;
	}
	public boolean isValid() {
		return valid;
	}
	public void setValid(boolean valid) {

		this.valid = valid;
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
