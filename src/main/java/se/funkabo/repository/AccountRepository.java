package se.funkabo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import se.funkabo.entity.Account;

public interface AccountRepository extends JpaRepository<Account, Long>{
	Account findByUsername(String username);
	Boolean existsByUsername(String username);
}
