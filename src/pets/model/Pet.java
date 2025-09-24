package pets.model;
import java.sql.Timestamp;
import java.time.LocalDate;

import pets.model.enums.Breed;
import pets.model.enums.Gender;
import pets.model.enums.PetType;

public class Pet {
	private String id;
    private String name;
    private PetType petType; 
    private Breed breed;
    private Gender gender;
    private boolean isStray;
    private LocalDate dateOfBirth;
    private LocalDate dateOfRegistry;
    private LocalDate dateOfDeath;
    private String registeredById;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private int version;

	public Pet(String id, String name, PetType petType, Breed breed, Gender gender, boolean isStray,
			LocalDate dateOfBirth, LocalDate dateOfRegistry, LocalDate dateOfDeath, String registeredById) {
		setId(id);
		setName(name);
		setPetType(petType);
		setBreed(breed);
		setGender(gender);
		setStray(isStray);
		setDateOfBirth(dateOfBirth);
		setDateOfRegistry(dateOfRegistry);
		setDateOfDeath(dateOfDeath);
		setRegisteredById(registeredById);
	}
	public Pet() {
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	
	public String getName() {
		return name;
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
	
	public PetType getPetType() {
		return petType;
	}
	public void setPetType(PetType petType) {
		if (petType == null) throw new IllegalArgumentException ("Pet type can't be null.");
		if (petType != PetType.BIRD && petType != PetType.CAT && petType != PetType.DOG && petType != PetType.HAMSTER &&petType != PetType.FISH && petType != PetType.RABBIT)
			throw new IllegalArgumentException ("Pet type can only be CAT, DOG, HAMSTER, FISH or RABBIT.");
		this.petType = petType;
	}
	public Breed getBreed() {
		return breed;
	}
	public void setBreed(Breed breed) {
		if (breed == null) throw new IllegalArgumentException ("Breed can't be null.");
		
		if (this.petType != breed.getPetType()) throw new IllegalArgumentException("Pet type and breed mismatch.");
		this.breed = breed;
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
	public boolean getStray() {
		return isStray;
	}
	public void setStray(boolean isStray) { 
		this.isStray = isStray;
	}
	public LocalDate getDateOfBirth() {
		return dateOfBirth;
	}
	public void setDateOfBirth(LocalDate dateOfBirth) {
		
        if (dateOfBirth != null && dateOfBirth.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Date of Birth cannot be in the future.");
        }
        if (dateOfBirth != null && dateOfBirth.isBefore(LocalDate.now().minusYears(30)))
        {
            throw new IllegalArgumentException("Pet cannot be over 30 years old.");
        }
        
        this.dateOfBirth = dateOfBirth;
	}
	public LocalDate getDateOfRegistry() {
		return dateOfRegistry;
	}
	public void setDateOfRegistry(LocalDate dateOfRegistry) {
		if (dateOfRegistry == null) {
            throw new IllegalArgumentException("Date of Registry cannot be null.");
        }
		if (dateOfBirth != null && dateOfRegistry.isBefore(dateOfBirth)) {
            throw new IllegalArgumentException("Date of Registry cannot be before Date of Birth.");
        }
        if (dateOfRegistry.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Date of Registry cannot be in the future.");
        }
        
        this.dateOfRegistry = dateOfRegistry;
	}
	public LocalDate getDateOfDeath() {
		return dateOfDeath;
	}
	public void setDateOfDeath(LocalDate dateOfDeath) {
	    if (dateOfDeath == null) {
	        this.dateOfDeath = null;
	        return;
	    }

	    if (dateOfDeath.equals(LocalDate.of(9999, 12, 31))) {
	        this.dateOfDeath = dateOfDeath;
	        return;
	    }

	    if (dateOfBirth != null && dateOfDeath.isBefore(dateOfBirth)) {
	        throw new IllegalArgumentException("Date of Death cannot be before Date of Birth.");
	    }

	    if (dateOfDeath.isAfter(LocalDate.now())) {
	        throw new IllegalArgumentException("Date of Death cannot be in the future.");
	    }

	    this.dateOfDeath = dateOfDeath;
	}

	
	public String getRegisteredById() {
		return registeredById;
	}
	public void setRegisteredById(String registeredById) {
		this.registeredById = registeredById;
	}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pet pet = (Pet) o;
        return id != null && id.equals(pet.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
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
