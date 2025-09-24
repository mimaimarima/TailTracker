package pets.model;

import java.sql.Timestamp;

public class User {
	
    private String id; 
    private String username;
    private String email;
    private String password; 
    private String name;
    private String surname;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private int version;
    
    public User()
    {
    	
    }
    public User(String username, String email, String name, String surname, String password) {
    	setUsername(username);
        setEmail(email);
        setName(name);
        setSurname(surname);
        setPassword(password);
    }
    
    public void setUsername(String username) {
    	username.trim();
        if (!username.matches("^[a-zA-Z][a-zA-Z0-9_]{2,14}$"))
            throw new IllegalArgumentException("Username must start with a letter, be 3–15 characters, and contain only letters, numbers, or _.");
        this.username = username;
    }

    public void setPassword(String password) {
        if (password == null || password.isBlank())
            throw new IllegalArgumentException("Password cannot be empty.");
        this.password = password;
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

    public void setName(String name) {
    	name.trim();

    	if (name.isEmpty() || name.length() > 50) {
    	    throw new IllegalArgumentException("Name must be 1-50 characters long.");
    	}
    	if (!name.matches("^(?!.*[\\s-]{2})[A-Za-z0-9]+(?:[\\s-][A-Za-z0-9]+)*$")) {
    	    throw new IllegalArgumentException(
    	        "Invalid name format."
    	    );
    	}
        this.name = name;
    }


    public void setSurname(String surname) {
		surname.trim();

        if (surname == null || surname.isEmpty() || surname.length() > 50) {
            throw new IllegalArgumentException("Surname must be 1-50 characters long.");
        }
        if (!surname.matches("^(?!.*[\\s'-]{2})(?!.*[\\s'-]$)(?!^[\\s'-])([A-Za-z]+(?:[\\s'-][A-Za-z0-9]+)*)$")) {
            throw new IllegalArgumentException(
                "Surname can only contain letters, digits, single spaces, or single hyphens.\n" +
                "Cannot start/end with symbols, cannot have consecutive symbols.\n" +
                "Digits are only allowed after the first letter (for royal names)."
            );
        }
        this.surname = surname;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
		return username;
	}

	public void setId(String id) {
        this.id = id;
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
	
}