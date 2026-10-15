package com.kloudspot.helpdesk.config;

import com.kloudspot.helpdesk.domain.*;
import com.kloudspot.helpdesk.repository.TicketRepository;
import com.kloudspot.helpdesk.repository.UserRepository;
import com.kloudspot.helpdesk.repository.OrganizationRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Component
@ConditionalOnProperty(name = "app.seed-demo-data", havingValue = "true", matchIfMissing = true)
public class DemoDataInitializer implements ApplicationRunner {
    private final UserRepository users;
    private final TicketRepository tickets;
    private final OrganizationRepository organizations;
    private final PasswordEncoder passwords;

    public DemoDataInitializer(UserRepository users, TicketRepository tickets,
                               OrganizationRepository organizations, PasswordEncoder passwords) {
        this.users = users; this.tickets = tickets; this.organizations = organizations; this.passwords = passwords;
    }

    @Override
    public void run(ApplicationArguments args) {
        String hash = passwords.encode("Password123!");
        organizations.findById("acme").orElseGet(() -> organizations.save(
                new Organization("acme", "Acme Industries", Instant.now(), "seed")));
        organizations.findById("globex").orElseGet(() -> organizations.save(
                new Organization("globex", "Globex Corporation", Instant.now(), "seed")));
        users.findByEmailIgnoreCase("superadmin@tandem.test").orElseGet(() -> users.save(
                new AppUser("platform", "Tandem Platform", "Jordan Lee", "superadmin@tandem.test", hash, Role.SUPER_ADMIN)));
        if (users.count() > 1) return;
        AppUser acmeAdmin = new AppUser("acme", "Acme Industries", "Maya Patel", "admin@acme.test", hash, Role.ORGANIZATION_ADMIN);
        AppUser acmeAgent = new AppUser("acme", "Acme Industries", "Ethan Cole", "agent@acme.test", hash, Role.SUPPORT_AGENT);
        AppUser acmeCustomer = new AppUser("acme", "Acme Industries", "Nora Singh", "customer@acme.test", hash, Role.CUSTOMER);
        AppUser globexAdmin = new AppUser("globex", "Globex Corporation", "Alex Morgan", "admin@globex.test", hash, Role.ORGANIZATION_ADMIN);
        AppUser globexAgent = new AppUser("globex", "Globex Corporation", "Priya Shah", "agent@globex.test", hash, Role.SUPPORT_AGENT);
        AppUser globexCustomer = new AppUser("globex", "Globex Corporation", "Sam Rivera", "customer@globex.test", hash, Role.CUSTOMER);
        users.saveAll(List.of(acmeAdmin, acmeAgent, acmeCustomer, globexAdmin, globexAgent, globexCustomer));

        tickets.save(seedTicket(acmeCustomer, acmeAgent, "Unable to export monthly report",
                "The CSV export stops at 80% and then shows a generic error.", Priority.HIGH, TicketStatus.IN_PROGRESS, 2));
        tickets.save(seedTicket(acmeCustomer, null, "Update billing contact",
                "Please change our invoice contact for the next billing cycle.", Priority.LOW, TicketStatus.OPEN, 1));
        tickets.save(seedTicket(globexCustomer, globexAgent, "Dashboard metrics are delayed",
                "Live occupancy metrics appear to be around twenty minutes behind.", Priority.URGENT, TicketStatus.WAITING_ON_CUSTOMER, 3));
    }

    private Ticket seedTicket(AppUser requester, AppUser agent, String subject, String description,
                              Priority priority, TicketStatus status, int ageDays) {
        Instant created = Instant.now().minus(ageDays, ChronoUnit.DAYS);
        Ticket ticket = new Ticket();
        ticket.setTenantId(requester.getTenantId()); ticket.setSubject(subject); ticket.setDescription(description);
        ticket.setPriority(priority); ticket.setStatus(status); ticket.setRequesterId(requester.getId());
        ticket.setRequesterName(requester.getName()); ticket.setCreatedAt(created); ticket.setUpdatedAt(created.plus(3, ChronoUnit.HOURS));
        if (agent != null) { ticket.setAssignedAgentId(agent.getId()); ticket.setAssignedAgentName(agent.getName()); }
        ticket.getHistory().add(new TicketHistory(UUID.randomUUID().toString(), requester.getId(), requester.getName(),
                "status", null, TicketStatus.OPEN.name(), created));
        if (status != TicketStatus.OPEN) ticket.getHistory().add(new TicketHistory(UUID.randomUUID().toString(),
                agent.getId(), agent.getName(), "status", TicketStatus.OPEN.name(), status.name(), created.plus(2, ChronoUnit.HOURS)));
        ticket.getComments().add(new TicketComment(UUID.randomUUID().toString(), requester.getId(), requester.getName(),
                requester.getRole(), "Adding this here so the support team has the full context.", false, created.plus(10, ChronoUnit.MINUTES)));
        if (agent != null) ticket.getComments().add(new TicketComment(UUID.randomUUID().toString(), agent.getId(), agent.getName(),
                agent.getRole(), "Reproduced in the staging environment; investigating the service logs.", true, created.plus(2, ChronoUnit.HOURS)));
        return ticket;
    }
}

