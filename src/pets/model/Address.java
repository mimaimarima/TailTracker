package pets.model;

import java.sql.Timestamp;

public class Address {
    private String id;
    private String street;     
    private String city;       
    private String postCode;   
    private String country;    
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private String createdBy;
    private int version;
    
    public Address(String street, String city, String postCode, String country) {
		setStreet(street);
		setCity(city);
		setPostCode(postCode);
		setCountry(country);
	}
    
    public Address() {
		// TODO Auto-generated constructor stub
	}

	public void setId(String string) {
		this.id = string;
		
	}
	public String getId() {
        return id;
    }

	public void setStreet(String street) {
		street = street.trim();
		if (!street.matches("^[\\p{L}\\p{N}\\s.,\\-/]+$"))
		{
	        throw new IllegalArgumentException("Invalid information.");
		}
	    this.street = street;
	}

	public void setCity(String city) {
	    if (city == null || city.isEmpty() || city.length() > 60) {
	        throw new IllegalArgumentException("City must be between 1 and 60 characters long.");
	    }
	    if (!city.matches("^[A-Za-z]+([\\s'-][A-Za-z]+)*$")) {
	        throw new IllegalArgumentException("City can only contain letters, hyphens, apostrophes, and spaces.");
	    }
	    this.city = city;
	}

	public void setCountry(String country) {
	    if (country == null || country.isEmpty() || country.length() > 50) {
	        throw new IllegalArgumentException("Country must be between 1 and 50 characters long.");
	    }
	    if (!country.matches("^[A-Za-z]+([\\s'-][A-Za-z]+)*$")) {
	        throw new IllegalArgumentException("Country can only contain letters, hyphens, apostrophes, and spaces.");
	    }
	    this.country = country;
	}

    public void setPostCode(String postCode) {
        if (postCode == null || postCode.isEmpty()) {
            throw new IllegalArgumentException("Post code is required.");
        }
        if (!postCode.matches("\\d{4,5}")) {
            throw new IllegalArgumentException("Post code must be exactly 4 or 5 digits.");
        }
        this.postCode = postCode;
    }

    public String getStreet() {
        return street;
    }

    public String getCity() {
        return city;
    }

    public String getPostCode() {
        return postCode;
    }

    public String getCountry() {
        return country;
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
