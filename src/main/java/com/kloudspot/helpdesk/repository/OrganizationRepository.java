package com.kloudspot.helpdesk.repository;

import com.kloudspot.helpdesk.domain.Organization;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface OrganizationRepository extends MongoRepository<Organization, String> {
    boolean existsByNameIgnoreCase(String name);
    List<Organization> findAllByOrderByCreatedAtDesc();
}

