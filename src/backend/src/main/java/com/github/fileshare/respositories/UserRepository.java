package com.github.fileshare.respositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.ListPagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import com.github.fileshare.config.entities.UserEntity;
import com.github.fileshare.specifications.UserRepositoryCustom;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long>,
										ListPagingAndSortingRepository<UserEntity, Long>,
										JpaSpecificationExecutor<UserEntity>,
										UserRepositoryCustom {
	
    Optional<UserEntity> findByEmail(String email);
    
    Boolean existsByEmail(String email);
}
