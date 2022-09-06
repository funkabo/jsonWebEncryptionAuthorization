package se.funkabo.entity;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.Calendar;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "verificationToken")
public class VerificationToken {


	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	@Column(name="token")
	private String token;

	@OneToOne(targetEntity = Account.class, fetch = FetchType.EAGER)
	@JoinColumn(name = "account_id", nullable = false)
	private Account account;

	@Column(name="created")
	private Date created;
	
	@Column(name="expires")
	private Date expires;

	public VerificationToken() {}
	
	public VerificationToken(String token) {
		this.token = token;
		this.expires = calculateExpiration(60 * 24);
	}
	
	public VerificationToken(String token, Account account) {
		Calendar calendar = Calendar.getInstance();
		this.token = token;
		this.account = account;
		this.created = new Date(calendar.getTime().getTime());
		this.expires = calculateExpiration(60 * 24);
	}
	
	private Date calculateExpiration(int minutes) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Timestamp(calendar.getTime().getTime()));
		calendar.add(Calendar.MINUTE, minutes);
		return new Date(calendar.getTime().getTime());
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public Account getAccount() {
		return account;
	}

	public void setAccount(Account account) {
		this.account = account;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public Date getExpirationDate() {
		return expires;
	}

	public void setExpirationDate(Date expires) {
		this.expires = expires;
	}
}
