package pets.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import pets.model.enums.ActivityType;

public class Activity {
    private String id;
    private ActivityType typeA;
    private LocalDateTime dateTimeStart;
    private LocalDateTime dateTimeEnd;
    private String description;
    private String recommendation;
    private BigDecimal price;
    private String careCenterId;
    private String petId;
    private String registeredById;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private String createdBy;
    private int version;
    
	public Activity(ActivityType typeA, LocalDateTime dateTimeStart, LocalDateTime dateTimeEnd,
			String description, String recommendation, BigDecimal price, String careCenterId, String petId) {
		setTypeA(typeA);
		setDateTimeStart(dateTimeStart);
		setDateTimeEnd(dateTimeEnd);
		setDescription(description);
		setRecommendation(recommendation);
		setPrice(price);
		setCareCenterId(careCenterId);
		setPetId(petId);
	}
	
	public Activity() {
		// TODO Auto-generated constructor stub
	}

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public ActivityType getTypeA() {
		return typeA;
	}
	public void setTypeA(ActivityType typeA) {
		if (typeA != ActivityType.DAILY_STAY && typeA != ActivityType.TRAINING && typeA != ActivityType.GROOMING)
			throw new IllegalArgumentException("Available activities: DAILY_STAY, TRAINING, GROOMING.");
		this.typeA = typeA;
	}
	public LocalDateTime getDateTimeStart() {
		return dateTimeStart;
	}
	public void setDateTimeStart(LocalDateTime dateTimeStart) {
		
    	if (dateTimeStart == null) {
            throw new IllegalArgumentException("Date and time must not be null.");
        }
        
		this.dateTimeStart = dateTimeStart;
	}
	public LocalDateTime getDateTimeEnd() {
		return dateTimeEnd;
	}
	public void setDateTimeEnd(LocalDateTime dateTimeEnd) {
		if (dateTimeEnd == null) {
            throw new IllegalArgumentException("Date and time must not be null.");
        }
		if (dateTimeEnd.isBefore(dateTimeStart)) {
			throw new IllegalArgumentException("Activity cannot end before it starts.");
		}
		this.dateTimeEnd = dateTimeEnd;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		if (description == null || description.length() > 254)
            throw new IllegalArgumentException("Description cannot be null or longer than 254 characters.");
      
		this.description = description;
	}
	public String getRecommendation() {
		return recommendation;
	}
	public void setRecommendation(String recommendation) {
		if (recommendation == null || recommendation.length() > 254)
            throw new IllegalArgumentException("Recommendation cannot be null or longer than 254 characters.");
     
		this.recommendation = recommendation;
	}
	public BigDecimal getPrice() {
		return price;
	}
	public void setPrice(BigDecimal price) {
	    if (price == null) {
	        throw new IllegalArgumentException("Price cannot be null.");
	    }

	    if (price.compareTo(BigDecimal.ZERO) < 0) {
	        throw new IllegalArgumentException("Price cannot be negative.");
	    }

	    BigDecimal maxValue = new BigDecimal("99999999.99");
	    if (price.compareTo(maxValue) > 0) {
	        throw new IllegalArgumentException("Price cannot exceed 99,999,999.99");
	    }

	    this.price = price.setScale(2, RoundingMode.HALF_UP);
	}
	public String getCareCenterId() {
		return careCenterId;
	}
	public void setCareCenterId(String careCenterId) {
		this.careCenterId = careCenterId;
	}

	public String getPetId() {
		return petId;
	}

	public void setPetId(String petId) {
		this.petId = petId;
	}

	public String getRegisteredById() {
		return registeredById;
	}

	public void setRegisteredById(String registeredById) {
		this.registeredById = registeredById;
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
