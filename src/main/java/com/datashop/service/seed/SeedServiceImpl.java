package com.datashop.service.seed;

import com.datashop.config.AppConfig;
import com.datashop.db.Jpa;
import com.datashop.domain.user.User;
import com.datashop.domain.user.UserRole;
import com.datashop.repository.UserRepository;
import com.datashop.security.PasswordEncoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class SeedServiceImpl implements SeedService {

    private final Jpa jpa;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AppConfig config;

    @Override
    public void seed() {
        if (!config.isSeedEnabled()) {
            log.info("Seeding disabled");
            return;
        }
        boolean created = jpa.write(entityManager -> {
            if (userRepository.findByEmail(entityManager, config.getSeedAdminEmail()).isPresent()) {
                return false;
            }
            User admin = new User();
            admin.setEmail(config.getSeedAdminEmail());
            admin.setPasswordHash(passwordEncoder.hash(config.getSeedAdminPassword()));
            admin.setRole(UserRole.ADMIN);
            userRepository.save(entityManager, admin);
            return true;
        });
        if (created) {
            log.info("Seeded admin account {}", config.getSeedAdminEmail());
        } else {
            log.info("Admin account already present");
        }
    }
}
