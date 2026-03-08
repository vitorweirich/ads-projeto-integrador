package com.github.fileshare.respositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.ListPagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import com.github.fileshare.config.entities.UserSettingsEntity;

@Repository
public interface UserSettingsRepository extends JpaRepository<UserSettingsEntity, Long>,
										ListPagingAndSortingRepository<UserSettingsEntity, Long> {
	
}
