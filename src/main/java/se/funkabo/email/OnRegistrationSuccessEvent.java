package se.funkabo.email;

import org.springframework.context.ApplicationEvent;

import se.funkabo.entity.Account;

public class OnRegistrationSuccessEvent extends ApplicationEvent {

	private static final long serialVersionUID = 1L;
	
	private Account account;
	
	private String url;

	public OnRegistrationSuccessEvent(Account account, String url) {
		super(account);
		this.account = account;
		this.url = url;
	}

	public Account getAccount() {
		return account;
	}

	public void setAccount(Account account) {
		this.account = account;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

}
