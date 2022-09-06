package se.funkabo.core;

import java.util.Properties;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.mail.MailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@Configuration
@EnableWebSecurity
@ComponentScan(basePackages = "se.funkabo")
@PropertySource("classpath:application.properties")
public class Email {
	
	@Bean(name = "mailSender")
	public MailSender javaMailService() {
	   JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
	   mailSender.setHost("smtp.gmail.com");
	   mailSender.setPort(587);
	   mailSender.setProtocol("smtp");
	   mailSender.setUsername("email@email.com");
	   mailSender.setPassword("emailpassword123");
	   Properties mailProperties = new Properties();
	   mailProperties.put("mail.smtp.auth", "true");
	   mailProperties.put("mail.smtp.starttls.enable", "true");
	   mailProperties.put("mail.smtp.debug", "true");
	   mailSender.setJavaMailProperties(mailProperties);
	   return mailSender;
	}
	
	@Bean
    public MessageSource messageSource() {
       ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
       messageSource.setBasename("classpath:messages");
       messageSource.setUseCodeAsDefaultMessage(true);
       messageSource.setDefaultEncoding("UTF-8");
       messageSource.setCacheSeconds(0);
       return messageSource;
    }

}
