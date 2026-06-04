package com.sportshop.repository;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.sportshop.entity.UserEntity;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
	Optional<UserEntity> findByEmail(String email);

	@Query("SELECT u FROM UserEntity u " +
			"WHERE (:search IS NULL OR u.nameUser LIKE %:search% OR u.phone LIKE %:search% " +
			"OR u.email LIKE %:search% OR u.address LIKE %:search% OR CAST(u.id AS string) LIKE %:search%)")
	Page<UserEntity> findWithSearch(@Param("search") String search, Pageable pageable);
}
