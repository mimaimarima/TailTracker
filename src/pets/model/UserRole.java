package pets.model;

import java.time.LocalDate;

public class UserRole {
	private String id;
    private String userId;
    private String roleId;
    private LocalDate dateHasFrom;
    private LocalDate dateHasTo;
	public UserRole(String userId, String roleId, LocalDate dateHasFrom, LocalDate dateHasTo) {
		setUserId(userId);
		setRoleId(roleId);
		setDateHasFrom(dateHasFrom);
		setDateHasTo(dateHasTo);
	}
	public UserRole() {
		// TODO Auto-generated constructor stub
	}

	public String getId() {
		return id;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getRoleId() {
		return roleId;
	}
	public void setRoleId(String roleId) {
		this.roleId = roleId;
	}
	public LocalDate getDateHasFrom() {
		return dateHasFrom;
	}
	public void setDateHasFrom(LocalDate dateHasFrom) {
		this.dateHasFrom = dateHasFrom;
	}
	public LocalDate getDateHasTo() {
		return dateHasTo;
	}
	public void setDateHasTo(LocalDate dateHasTo) {
		this.dateHasTo = dateHasTo;
	}
    
}
