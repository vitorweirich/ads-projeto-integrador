package com.github.fileshare.specifications;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserRepositoryCustom {
    Page<UserWithStorageProjection> findAllWithFiltersAndStorageSum(String name, String email, Pageable pageable);
}

