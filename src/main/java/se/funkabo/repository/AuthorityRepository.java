package se.funkabo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import se.funkabo.entity.Authority;
import se.funkabo.entity.Permission;

@Repository
public interface AuthorityRepository extends JpaRepository<Authority, Long>{
	Optional<Authority> findByPermission(Permission permission);
}
