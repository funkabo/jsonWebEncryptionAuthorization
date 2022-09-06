package se.funkabo.entity.request;

import java.util.Date;
import java.util.List;

import se.funkabo.entity.Authority;

public class Register {
	
	private String username;
	private String firstname;
	private String lastname;
	private String password;
	private String passwordConfirm;
	private String email;
	private Date created;
	private boolean enabled;
    private List<Authority> authorities;
    
    public Register() {}

    
    public Register(String username,
			   	    String firstname,
			   	    String lastname,
			   	    String email,
			   	    String password,
			   	    String passwordConfirm) {
    	this.setUsername(username);
    	this.setFirstname(firstname);
    	this.setLastname(lastname);
    	this.setEmail(email);
    	this.setPassword(password);
    	this.setPasswordConfirm(passwordConfirm);
}
    
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getFirstname() {
		return firstname;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	public String getLastname() {
		return lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPasswordConfirm() {
		return passwordConfirm;
	}

	public void setPasswordConfirm(String passwordConfirm) {
		this.passwordConfirm = passwordConfirm;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public List<Authority> getAuthorities() {
		return authorities;
	}

	public void setAuthorities(List<Authority> authorities) {
		this.authorities = authorities;
	}
      
}
