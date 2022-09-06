package se.funkabo.email;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Component;

import se.funkabo.entity.Account;
import se.funkabo.entity.VerificationToken;
import se.funkabo.repository.VerificationRepository;


@Component
public class RegistrationSuccessListener implements ApplicationListener<OnRegistrationSuccessEvent>{

	@Autowired
	VerificationRepository verificationRepository;
	
	@Autowired
	MailSender mailSender;
	
	public RegistrationSuccessListener(VerificationRepository verificationRepository, MailSender mailSender) {
		this.verificationRepository = verificationRepository;
		this.mailSender = mailSender;
	}
		
	@Override
	public void onApplicationEvent(OnRegistrationSuccessEvent event) {
		this.confirmRegistration(event);
	}

	private void confirmRegistration(OnRegistrationSuccessEvent event) {
		Account account = event.getAccount();
		String token = UUID.randomUUID().toString();
		VerificationToken verificationToken = new VerificationToken(token, account);
		verificationRepository.save(verificationToken);
		
		String to = account.getEmail();
		String subject = "Confirm Registration";
		String url = event.getUrl() + "/confirmRegistration?token=" + token;
		String message = "Almost done, " + account.getFirstname() + "!. To complete the registration you need to confirm this email by " +
		                 "clicking on the below to activate your account.\n\n";
		
		SimpleMailMessage email = new SimpleMailMessage();
		email.setTo(to);
		email.setSubject(subject);
		email.setText(message + "\t\thttp://localhost:8080" + url);
		mailSender.send(email);
	}

}
