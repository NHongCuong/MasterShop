package com.sportshop.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.sportshop.entity.CategoryEntity;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<CategoryEntity, Long> {

	@Query("SELECT c FROM CategoryEntity c " +
			"WHERE (:search IS NULL OR c.name LIKE %:search% OR CAST(c.id AS string) LIKE %:search%)")
	Page<CategoryEntity> findWithSearch(@Param("search") String search, Pageable pageable);

	boolean existsByName(String name);
	Optional<CategoryEntity> findByName(String name);
}
