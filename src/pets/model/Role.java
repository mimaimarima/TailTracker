package pets.model;

import java.time.LocalDate;

public class Role {
	private String id;
	private String name;
	private LocalDate dateFrom;
	private LocalDate dateTo;
	public Role(String name, LocalDate dateFrom, LocalDate dateTo) {
		setName(name);
		setDateFrom(dateFrom);
		setDateTo(dateTo);
	}
	public Role() {
		// TODO Auto-generated constructor stub
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
        if (name == null || name.length() > 20)
            throw new IllegalArgumentException("Name cannot be null or longer than 20 characters.");
        if (!name.matches("^[A-Za-z\\-\\s]+$"))
            throw new IllegalArgumentException("Only letters, ', and - allowed.");
        this.name = name;
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
		this.dateTo = dateTo;
	}
	public boolean isLegal() {
	    return ("vet".equals(this.getName()) || 
	            "carer".equals(this.getName()));
	}

	public boolean isPhysical() {
	    return ("petowner".equals(this.getName()) || 
	            "petadopter".equals(this.getName()));
	}
	public boolean isAdmin() {
	    return ("admin".equals(this.getName()));
	}
}
