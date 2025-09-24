package pets.model;

import java.util.Set;

import pets.model.enums.LegalSize;
import pets.model.enums.LegalType;
import pets.model.enums.MedicalSpecialties;
import pets.model.enums.PersonType;
import pets.model.enums.PetType;

public class VetStation extends Legal{
	private Set<MedicalSpecialties> specialties;
    private Set<PetType> allowedSpecies;
    private boolean emergencyAvailability;
    private String person_id;

    
	public VetStation(String name, String number, boolean status, String addressId, String userId, String email,
			PersonType personType, String tin, String licenceId, String dailyWorkHours,
			LegalSize size, LegalType type, Set<MedicalSpecialties> specialties, Set<PetType> allowedSpecies,
			boolean emergencyAvailability, String person_id) {
	
		this.specialties = specialties;
		this.allowedSpecies = allowedSpecies;
		this.emergencyAvailability = emergencyAvailability;
		this.person_id = person_id;
	}

	public VetStation() {
		
	}

	public Set<MedicalSpecialties> getSpecialties() {
		return specialties;
	}
	public void setSpecialties(Set<MedicalSpecialties> specialties) {
		if (specialties == null || specialties.isEmpty()) {
	        throw new IllegalArgumentException("Please enter numbers corresponding to medical specialties.");
	    }
		for (MedicalSpecialties specialty : specialties) {
	        if (specialty != MedicalSpecialties.SURGERY && 
        		specialty != MedicalSpecialties.DENTISTRY && 
				specialty != MedicalSpecialties.DERMATOLOGY && 
				specialty != MedicalSpecialties.OPHTHALMOLOGY && 
				specialty != MedicalSpecialties.ORTHOPEDICS && 
				specialty != MedicalSpecialties.CARDIOLOGY
				) {
	            throw new IllegalArgumentException("Invalid specialty: " + specialty);
	        }
	    }
		this.specialties = specialties;
	}
	public Set<PetType> getAllowedSpecies() {
		return allowedSpecies;
	}
	public void setAllowedSpecies(Set<PetType> allowedSpecies) {
		if (allowedSpecies == null || allowedSpecies.isEmpty()) {
	        throw new IllegalArgumentException("Please enter numbers corresponding to pet types.");
	    }
		for (PetType pet : allowedSpecies) {
	        if (pet != PetType.CAT && 
	        	pet != PetType.BIRD && 
	        	pet != PetType.DOG && 
	        	pet != PetType.FISH && 
	        	pet != PetType.HAMSTER && 
	       		pet != PetType.RABBIT
				) {
	            throw new IllegalArgumentException("Invalid pet type: " + pet);
	        }
	    }
		this.allowedSpecies = allowedSpecies;
	}
	public boolean getEmergencyAvailability() {
		return emergencyAvailability;
	}
	public void setEmergencyAvailability(boolean emergencyAvailability) {
		this.emergencyAvailability = emergencyAvailability;
	}
	public String getPerson_id() {
		return person_id;
	}
	public void setPerson_id(String person_id) {
		this.person_id = person_id;
	}
	
    
    
}
