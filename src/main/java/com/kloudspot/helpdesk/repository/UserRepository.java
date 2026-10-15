package com.kloudspot.helpdesk.repository;

import com.kloudspot.helpdesk.domain.AppUser;
import com.kloudspot.helpdesk.domain.Role;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends MongoRepository<AppUser, String> {
    Optional<AppUser> findByEmailIgnoreCase(String email);
    List<AppUser> findByTenantIdAndRoleInOrderByName(String tenantId, List<Role> roles);
    Optional<AppUser> findFirstByTenantIdAndRole(String tenantId, Role role);
    long countByTenantId(String tenantId);
}

