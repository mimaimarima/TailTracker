package pets.model;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import pets.model.enums.HealthEventType;

public class HealthEvent {
    private String id;
    private HealthEventType typeHE; // Vaccine, Chip, Surgery, Checkup
    private LocalDateTime dateTime;
    private String description;
    private String recommendation;
    private BigDecimal price; 
    private String vetStationId;
    private String petId;
    private boolean verified;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private int version;
    private String createdBy;
    
    public HealthEvent(HealthEventType typeHE, LocalDateTime dateTime, String description, String recommendation, BigDecimal price, String vetStationId, String petId, boolean verified) {
       setTypeHE(typeHE);
       setDateTime(dateTime);
       setDescription(description);
       setRecommendation(recommendation);
       setPrice(price);
       setVetStationId(vetStationId);
       setPetId(petId);
       setVerified(verified);
    }

    public HealthEvent() {
		// TODO Auto-generated constructor stub
	}

	public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public HealthEventType getTypeHE() {
        return typeHE;
    }
    public void setTypeHE(HealthEventType typeHE) {
    	if (typeHE != HealthEventType.CHECKUP && typeHE != HealthEventType.CHIP && typeHE != HealthEventType.SURGERY && typeHE != HealthEventType.VACCINE)
    		throw new IllegalArgumentException("Health event can only be CHECKUP, CHIP, SURGERY or VACCINE.");
        this.typeHE = typeHE;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }
    public void setDateTime(LocalDateTime dateTime) {
    	if (dateTime == null) {
            throw new IllegalArgumentException("Date cannot be null.");
        }
        if (dateTime.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("Date cannot be in the future.");
        }
        this.dateTime = dateTime;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
		if (description == null || description.length() > 254 || description.isBlank())
            throw new IllegalArgumentException("Description cannot be empty or longer than 254 characters.");
		 this.description = description; 
	}
	public String getRecommendation() {
		return recommendation;
	}
	public void setRecommendation(String recommendation) {
		if (recommendation == null || recommendation.length() > 254 || recommendation.isBlank())
            throw new IllegalArgumentException("Recommendation cannot be empty or longer than 254 characters.");
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

	public String getVetStationId() {
		return vetStationId;
	}

	public void setVetStationId(String vetStationId) {
		this.vetStationId = vetStationId;
	}

	public String getPetId() {
		return petId;
	}

	public void setPetId(String petId) {
		this.petId = petId;
	}

	public boolean isVerified() {
		return verified;
	}

	public void setVerified(boolean verified) {
		this.verified = verified;
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