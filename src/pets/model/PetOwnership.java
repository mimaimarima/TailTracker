package pets.model;

import java.sql.Timestamp;
import java.time.LocalDate;


public class PetOwnership {
	private String physicalId;
	private String petId;
	private LocalDate dateFrom;
	private LocalDate dateTo;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private int version;
    private String createdBy;
	public PetOwnership()
	{
		
	}
	public PetOwnership(String physicalId, String petId, LocalDate dateFrom, LocalDate dateTo) {
		this.physicalId = physicalId;
		this.petId = petId;
		this.dateFrom = dateFrom;
		this.dateTo = dateTo;
	}
	public String getPhysicalId() {
		return physicalId;
	}
	public void setPhysicalId(String physicalId) {
		this.physicalId = physicalId;
	}
	public String getPetId() {
		return petId;
	}
	public void setPetId(String petId) {
		this.petId = petId;
	}
	public LocalDate getDateFrom() {
		return dateFrom;
	}
	public void setDateFrom(LocalDate dateFrom) {
		this.dateFrom = dateFrom;
	}
	public LocalDate getDateTo() {
		return dateTo;
	}
	public void setDateTo(LocalDate dateTo) {
		if (dateTo != null && dateTo.isBefore(dateFrom)) {
            throw new IllegalArgumentException("Date of ending ownership cannot end before the date of beginning ownership.");
        }

		this.dateTo = dateTo;
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
