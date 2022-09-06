package se.funkabo.api;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import se.funkabo.email.OnRegistrationSuccessEvent;
import se.funkabo.entity.Account;
import se.funkabo.entity.Authority;
import se.funkabo.entity.Permission;
import se.funkabo.entity.VerificationToken;
import se.funkabo.entity.request.Login;
import se.funkabo.entity.request.Register;
import se.funkabo.exception.IncorrectPasswordException;
import se.funkabo.exception.UserNotFoundException;
import se.funkabo.repository.AccountRepository;
import se.funkabo.repository.AuthorityRepository;
import se.funkabo.repository.VerificationRepository;
import se.funkabo.service.AccountDetailsService;
import se.funkabo.utils.authorization.AuthorizationUtils;




@RestController
@RequestMapping("/")
@CrossOrigin(origins = "http://localhost:8080", maxAge = 3600)
public class Facade {
	
	@Autowired
	AuthenticationManager authenticationManager;
	
	@Autowired
	private ApplicationEventPublisher applicationEventPublisher;
	
	@Autowired
	AccountRepository accountRepository;
	
	@Autowired
	AuthorityRepository authorityRepository;
	
	@Autowired
	VerificationRepository verificationRepository;
	
	@Autowired
    PasswordEncoder bCryptEncoder;
	
	@Autowired
	AccountDetailsService accountDetailsService;
	
	@Autowired
	AuthorizationUtils authorizationUtils;
	
	public Facade(ApplicationEventPublisher applicationEventPublisher) {
		this.applicationEventPublisher = applicationEventPublisher;
	}

	@PostMapping( value = "/login", consumes = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
								    produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
	public @ResponseBody ResponseEntity<Object> login( @RequestBody Login login, HttpServletRequest request) throws Exception, UsernameNotFoundException, BadCredentialsException {
		if( Objects.requireNonNull(login.getUsername() != null && Objects.requireNonNull(login.getPassword()) != null )) {
			Account account = null;
			if((account = accountRepository.findByUsername(login.getUsername())) == null) {
				throw new UserNotFoundException("No user with username : " + login.getUsername() + " exists");
			} else if(!BCrypt.checkpw(login.getPassword(), account.getPassword())){
				throw new IncorrectPasswordException("Incorrect Password");
			}

			Authentication authentication = authenticationManager.authenticate( new UsernamePasswordAuthenticationToken(
																  login.getUsername(), 
																  login.getPassword())); 

			SecurityContextHolder.getContext().setAuthentication(authentication);

			String token = authorizationUtils.encrypt(account);

			HttpHeaders responseHeaders = new HttpHeaders();
			responseHeaders.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
			responseHeaders.set(HttpHeaders.AUTHORIZATION, "Bearer " + token);
			return new ResponseEntity<Object>(null,responseHeaders, HttpStatus.OK);
		}
	   return new ResponseEntity<Object>(null,null,HttpStatus.NOT_FOUND);
	}
	
	
	@PostMapping( value = "/register", consumes = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
			   						   produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
	public @ResponseBody ResponseEntity<?> register( HttpServletRequest request, @RequestBody Register register ) throws Exception  {
		Objects.requireNonNull(register.getUsername());
		Objects.requireNonNull(register.getFirstname());
		Objects.requireNonNull(register.getLastname());
		Objects.requireNonNull(register.getEmail());
		Objects.requireNonNull(register.getPassword());
		Objects.requireNonNull(register.getPasswordConfirm());
		
		if(accountRepository.existsByUsername(register.getUsername())) {
			return new ResponseEntity<String>("Fail -> Username is already taken!", HttpStatus.BAD_REQUEST);
		}
		
		if(!StringUtils.equals(register.getPassword(), register.getPasswordConfirm())) {
			return new ResponseEntity<String>("Fail -> Username is already taken!", HttpStatus.BAD_REQUEST);
		}

		Account account = new Account();
		account.setUsername(register.getUsername());
		account.setFirstname(register.getFirstname());
		account.setLastname(register.getLastname());
		account.setEmail(register.getEmail());
		account.setPassword(bCryptEncoder.encode(register.getPassword()));

		Authority permission = authorityRepository.findByPermission(Permission.ROLE_USER).orElseThrow(
							   () -> new Exception("User Authority not set."));

		List<Authority> authorities = new ArrayList<Authority>();
		authorities.add(permission);

		account.setAuthorities(authorities);
		account.setCreated(Date.from(ZonedDateTime.now(ZoneId.of("UTC")).toInstant()));

		accountRepository.save(account);

		try { String url = request.getContextPath();
		      applicationEventPublisher.publishEvent(new OnRegistrationSuccessEvent(account, url));
		} catch(Exception ex) {
			throw new Exception("Error while sending confirmation email.");
		}

		return ResponseEntity.ok("Account Created");
	}
	
	
	@GetMapping( value = "/confirmRegistration", consumes = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
            					                 produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
	public @ResponseBody ResponseEntity<?> confirmRegistration( HttpServletRequest request, @RequestParam("token") String token ) throws Exception {
		if(Objects.requireNonNull(token) != null) {
			VerificationToken verificationToken = null;
			if((verificationToken = verificationRepository.findByToken(token)) != null) {
				Account account = verificationToken.getAccount();
				Calendar calendar = Calendar.getInstance();
				if((verificationToken.getExpirationDate().getTime() - calendar.getTime().getTime()) <=0) {
					return new ResponseEntity<String>("Fail -> Token has expired. Please register again!", HttpStatus.BAD_REQUEST);
				}
				
				account.setEnabled(true);
				account.setNonExpired(true);
				account.setNotLocked(true);
				account.setCredentialsNonExpired(true);
				accountRepository.save(account);
				
				return ResponseEntity.ok("Registration Confirmed");
			}
		}
		return new ResponseEntity<String>("Fail -> Invalid token!", HttpStatus.BAD_REQUEST);
	}
	
	
	
	
	@GetMapping( value = "/index", consumes = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
			                       produces = { MediaType.TEXT_PLAIN_VALUE, MediaType.TEXT_PLAIN_VALUE})
    public @ResponseBody ResponseEntity<?> index( HttpServletRequest request ) throws Exception  {
		return ResponseEntity.ok("Index");
	}
	
	@GetMapping( value = "/about", consumes = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
                                   produces = { MediaType.TEXT_PLAIN_VALUE, MediaType.TEXT_PLAIN_VALUE})
	public @ResponseBody ResponseEntity<?> about( HttpServletRequest request ) throws Exception  {
		return ResponseEntity.ok("About");
	}
}
