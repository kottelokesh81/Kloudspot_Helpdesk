package com.kloudspot.helpdesk.controller;

import com.kloudspot.helpdesk.domain.AppUser;
import com.kloudspot.helpdesk.domain.Role;
import com.kloudspot.helpdesk.dto.UserView;
import com.kloudspot.helpdesk.repository.UserRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserRepository users;
    public UserController(UserRepository users) { this.users = users; }

    @GetMapping("/agents")
    public List<UserView> agents(@AuthenticationPrincipal AppUser actor) {
        return users.findByTenantIdAndRoleInOrderByName(actor.getTenantId(), List.of(Role.SUPPORT_AGENT)).stream()
                .map(UserView::from).toList();
    }
}

