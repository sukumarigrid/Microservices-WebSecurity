package org.example;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserAccountRepository userAccountRepository;
    private final PostRepository postRepository;
    private final NotificationRepository notificationRepository;

    public AdminController(UserAccountRepository userAccountRepository,
                           PostRepository postRepository,
                           NotificationRepository notificationRepository) {
        this.userAccountRepository = userAccountRepository;
        this.postRepository = postRepository;
        this.notificationRepository = notificationRepository;
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public AdminStatsResponse stats() {
        return new AdminStatsResponse(
                userAccountRepository.count(),
                postRepository.count(),
                notificationRepository.count(),
                userAccountRepository.findAll().stream().map(UserAccount::username).toList());
    }
}
