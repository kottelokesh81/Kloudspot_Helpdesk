package com.kloudspot.helpdesk.repository;

import com.kloudspot.helpdesk.domain.Ticket;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface TicketRepository extends MongoRepository<Ticket, String> {
    Optional<Ticket> findByIdAndTenantId(String id, String tenantId);
    long countByTenantId(String tenantId);
}

