package pets.model;

import java.sql.Timestamp;

import pets.model.enums.PersonType;

public class Person {
    private String id;
    private String name;
    private String number;
    private boolean status;
    private String addressId;
    private String userId;
    private String email;
    private PersonType personType;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private int version;
    private String createdBy;
    
	public Person(String name, String number, boolean status, String addressId, String userId, String email, PersonType personType)
    {
    	setName(name);
    	setNumber(number);
    	setStatus(status);
    	setAddressId(addressId);
    	setUserId(userId);
    	setEmail(email);
    	setPersonType(personType);
    }
    
    public Person() {
			}

	public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getNumber() {
        return number;
    }

    public void setName(String name) {
    	name.trim();

    	if (name.isEmpty() || name.length() > 100) {
    	    throw new IllegalArgumentException("Name must be 1-100 characters long.");
    	}
    	if (!name.matches("^(?!.*[\\s-]{2})[A-Za-z0-9]+(?:[\\s-][A-Za-z0-9]+)*$")) {
    	    throw new IllegalArgumentException(
    	        "Invalid name format."
    	    );
    	}
        this.name = name;
    }

    public void setNumber(String number) {
    	
        if (number == null)
            throw new IllegalArgumentException("Phone number cannot be null.");
     
        number.trim();
        if (number.length()>16) {
        	throw new IllegalArgumentException("Entered phone number is too long.");
        }
     
        if (!number.matches("^[+]?\\d{1,3}?[-/\\s]?\\d{1,4}[-/\\s]?\\d{3}[-/\\s]?\\d{3}$")) {
            throw new IllegalArgumentException("Invalid phone number format. ");
        }

        this.number = number;
    }


    public boolean getStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }
    
    public void setAddressId(String addressId) {

        this.addressId = addressId;
    }

    public String getAddressId() {
        return addressId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be blank.");
        }
        if (email.length() > 254) {
            throw new IllegalArgumentException("Email too long.");
        }
        if (!email.matches("^((?:[A-Za-z0-9!#$%&'*+\\-\\/=?^_`{|}~]|(?<=^|\\.)\"|\"(?=$|\\.|@)|(?<=\".*)[ .](?=.*\")|(?<!\\.)\\.){1,64})(@)((?:[A-Za-z0-9.\\-])*(?:[A-Za-z0-9])\\.(?:[A-Za-z0-9]){2,})$")) {
            throw new IllegalArgumentException(
                "Invalid email format. Must follow standard email conventions."
            );
        }
        this.email = email;
    }

	public void setId(String id) {
		this.id = id;
		
		
	}

	public PersonType getPersonType() {
		return personType;
	}

	public void setPersonType(PersonType personType) {
		this.personType = personType;
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
