package se.funkabo.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import se.funkabo.entity.Account;
import se.funkabo.repository.AccountRepository;


@Service
public class AccountDetailsService implements UserDetailsService {
	
	@Autowired
	private AccountRepository accountRepository;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException{
			Account account = null;
			if((account =  accountRepository.findByUsername(username)) != null) {
				List<GrantedAuthority> authorities = account.getAuthorities().stream().map(authority ->
				new SimpleGrantedAuthority(authority.getPermission().name())).collect(Collectors.toList());
				return new User(account.getUsername(), 
							    account.getPassword(), 
							    account.isEnabled(), 
							    account.isAccountNonExpired(), 
							    account.isAccountNonLocked(), 
							    account.isCredentialsNonExpired(), 
							    authorities);
		    } else {
		    	throw new UsernameNotFoundException("No user found with username : " + username);
		    }	
	}

	

}
