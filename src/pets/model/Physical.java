package pets.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import pets.model.enums.Gender;

public class Physical extends Person {
    private String ssn;
    private Gender gender;
    private LocalDate dateOfBirth;
    private String person_id;
    private String name, surname;
    private int version;

    public Physical(String personId, String fullName) {
        this.setPerson_id(personId);
        this.setNameAndSurname(fullName);
    }
    
    public Physical(String ssn, Gender gender, LocalDate dOB, String person_id, String fullName)
    {
    	setSSN(ssn);
    	setGender(gender);
    	setDateOfBirth(dOB);
    	setPerson_id(person_id);
    	setNameAndSurname(fullName);
    }
    public void setNameAndSurname(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("Full name cannot be null or empty.");
        }

        String[] nameParts = fullName.trim().split("\\s+", 2); 

        this.name = nameParts[0]; 

        if (nameParts.length > 1) {
            this.surname = nameParts[1];
        } else {
            this.surname = ""; 
        }
		
	}
	public Physical() {
	}
	public String getSsn() {
        return ssn;
    }

	public void setSSN(String ssn) {
		ssn.trim();
	    if (ssn == null || ssn.length() != 13) {
	        throw new IllegalArgumentException("SSN must be exactly 13 digits long.");
	    }
	    if (!ssn.matches("\\d{13}")) {
	        throw new IllegalArgumentException("SSN must only contain digits.");
	    }

	    String day = ssn.substring(0, 2);
	    String month = ssn.substring(2, 4);
	    String yearFragment = ssn.substring(4, 7);

	    int yearNum = Integer.parseInt(yearFragment);
	    int year;

	    if (yearNum >= 900) {
	        year = 1000+ yearNum;
	    }else if (0<=yearNum && yearNum<=99) {
	    	year = 2000 + yearNum;
	    }else {
	        year = 1000 + yearNum;
	    }

	    String birthDateString = String.format("%02d-%02d-%04d", Integer.parseInt(day), Integer.parseInt(month), year);

	    LocalDate dateOfBirth;
	    try {
	        dateOfBirth = LocalDate.parse(birthDateString, DateTimeFormatter.ofPattern("dd-MM-yyyy"));
	    } catch (DateTimeParseException e) {
	        throw new IllegalArgumentException("Invalid SSN: The date of birth is not valid.");
	    }

	    LocalDate today = LocalDate.now();
	    if (dateOfBirth.isBefore(today.minusYears(100))) {
	        throw new IllegalArgumentException("Invalid SSN: The person cannot be older than 100 years.");
	    }
	    if (dateOfBirth.isAfter(today)) {
	        throw new IllegalArgumentException("Invalid SSN: The date of birth cannot be in the future.");
	    }
	    if (dateOfBirth.isAfter(today.minusYears(14))) {
	        throw new IllegalArgumentException("Invalid SSN: The person must be at least 14 years old.");
	    }
	    setDateOfBirth(dateOfBirth);
	    this.ssn = ssn;
	}



    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        if (gender == null) {
            throw new IllegalArgumentException("Gender cannot be null.");
        }
        if (gender!= Gender.FEMALE && gender != Gender.MALE)
        	throw new IllegalArgumentException("Gender can only be MALE or FEMALE.");
        this.gender = gender;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        if (dateOfBirth == null) {
            throw new IllegalArgumentException("Date of Birth cannot be null.");
        }
        if (dateOfBirth.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Date of Birth cannot be in the future.");
        }
        
        this.dateOfBirth = dateOfBirth;
    }

    public String getPerson_id() {
        return person_id;
    }

    public void setPerson_id(String person_id) {
        if (person_id == null || person_id.isEmpty()) {
            throw new IllegalArgumentException("Person ID cannot be null or empty.");
        }
        this.person_id = person_id;
    }

	public void setSsn(String ssn) {
		this.ssn = ssn;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getSurname() {
		return surname;
	}
	public void setSurname(String surname) {
		this.surname = surname;
	}


	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}
	

}
