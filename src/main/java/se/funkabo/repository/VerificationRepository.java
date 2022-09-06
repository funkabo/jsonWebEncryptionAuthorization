package se.funkabo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import se.funkabo.entity.VerificationToken;

@Repository
public interface VerificationRepository extends JpaRepository<VerificationToken, Long>{
	VerificationToken findByToken(String verificationToken);
}
