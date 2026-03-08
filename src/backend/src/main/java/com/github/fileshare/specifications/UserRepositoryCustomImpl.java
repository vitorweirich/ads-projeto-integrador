package com.github.fileshare.specifications;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.github.fileshare.config.entities.UploadedVideoEntity;
import com.github.fileshare.config.entities.UserEntity;
import com.github.fileshare.config.entities.UserSettingsEntity;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

@Repository
public class UserRepositoryCustomImpl implements UserRepositoryCustom {

    @PersistenceContext
    private EntityManager em;
    
    public Page<UserWithStorageProjection> findAllWithFiltersAndStorageSum(String name, String email, Pageable pageable) {

        CriteriaBuilder cb = em.getCriteriaBuilder();

        // Query principal
        CriteriaQuery<UserWithStorageProjection> cq = cb.createQuery(UserWithStorageProjection.class);
        Root<UserEntity> user = cq.from(UserEntity.class);

        // Subquery para soma do tamanho dos vídeos por usuário
        Subquery<Long> subquery = cq.subquery(Long.class);
        Root<UploadedVideoEntity> video = subquery.from(UploadedVideoEntity.class);
        subquery.select(cb.coalesce(cb.sum(video.get("size")), 0L));
        subquery.where(cb.equal(video.get("user").get("id"), user.get("id")));
        
        Join<UserEntity, UserSettingsEntity> settingsJoin = user.join("settings", JoinType.LEFT);

        // Filtros dinâmicos
        Predicate filters = cb.conjunction();
        if (name != null && !name.isBlank()) {
            filters = cb.and(filters, cb.like(cb.lower(user.get("name")), "%" + name.toLowerCase() + "%"));
        }
        if (email != null && !email.isBlank()) {
            filters = cb.and(filters, cb.like(cb.lower(user.get("email")), "%" + email.toLowerCase() + "%"));
        }

        cq.multiselect(
			user.get("id").alias("id"),
		    user.get("name").alias("name"),
		    user.get("email").alias("email"),
		    user.get("mfaSecret").alias("mfaSecret"),
		    user.get("mfaVerified").alias("mfaVerified"),
		    user.get("role").alias("role"),
		    user.get("createdAt").alias("createdAt"),
		    subquery.getSelection().alias("totalSize"),
		    settingsJoin.get("storageLimitBytes").alias("settingStorageLimitBytes"),
		    settingsJoin.get("maxVideoRetentionDays").alias("settingMaxVideoRetentionDays"),
		    settingsJoin.get("modifiedAt").alias("settingModifiedAt")
		);
        
        cq.where(filters);

        // Ordenação
        pageable.getSort().forEach(order -> {
            Path<?> orderProperty = user.get(order.getProperty());
            cq.orderBy(order.isAscending() ? cb.asc(orderProperty) : cb.desc(orderProperty));
        });

        // Executa query paginada
        List<UserWithStorageProjection> content = em.createQuery(cq)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();

        // Count
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<UserEntity> userCount = countQuery.from(UserEntity.class);

        Predicate countFilters = cb.conjunction();
        if (name != null && !name.isBlank()) {
            countFilters = cb.and(countFilters, cb.like(cb.lower(userCount.get("name")), "%" + name.toLowerCase() + "%"));
        }
        if (email != null && !email.isBlank()) {
            countFilters = cb.and(countFilters, cb.like(cb.lower(userCount.get("email")), "%" + email.toLowerCase() + "%"));
        }

        countQuery.select(cb.count(userCount));
        countQuery.where(countFilters);

        Long total = em.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(content, pageable, total);
    }

    
}

