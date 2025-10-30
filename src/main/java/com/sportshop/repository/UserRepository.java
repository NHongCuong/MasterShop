package com.sportshop.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.sportshop.entity.UserEntity;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
	Optional<UserEntity> findByEmail(String email);
}
