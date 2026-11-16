package com.mentalhealth.mentalhealth_backend.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.mentalhealth.mentalhealth_backend.model.ClientProfile;
import com.mentalhealth.mentalhealth_backend.model.Role;
import com.mentalhealth.mentalhealth_backend.model.TherapistProfile;
import com.mentalhealth.mentalhealth_backend.model.User;
import com.mentalhealth.mentalhealth_backend.repository.ClientProfileRepository;
import com.mentalhealth.mentalhealth_backend.repository.TherapistProfileRepository;
import com.mentalhealth.mentalhealth_backend.repository.UserRepository;

/**
 * Creates demo accounts for presentations (only if they don't exist yet).
 * Controlled by SEED_DEMO_DATA (default true) and DEMO_PASSWORD.
 * On a public website, set SEED_DEMO_DATA=false or use a private DEMO_PASSWORD.
 */
@Component
public class DemoDataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataSeeder.class);

    private final UserRepository userRepository;
    private final ClientProfileRepository clientProfileRepository;
    private final TherapistProfileRepository therapistProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final boolean enabled;
    private final String demoPassword;

    public DemoDataSeeder(UserRepository userRepository, ClientProfileRepository clientProfileRepository,
            TherapistProfileRepository therapistProfileRepository, PasswordEncoder passwordEncoder,
            @Value("${app.seed-demo-data:false}") boolean enabled,
            @Value("${app.demo-password:Demo1234!}") String demoPassword) {
        this.userRepository = userRepository;
        this.clientProfileRepository = clientProfileRepository;
        this.therapistProfileRepository = therapistProfileRepository;
        this.passwordEncoder = passwordEncoder;
        this.enabled = enabled;
        this.demoPassword = demoPassword;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (!enabled) {
            return;
        }
        therapist("dr.rivera", "Alex", "Rivera", "Anxiety, stress");
        therapist("dr.okafor", "Ngozi", "Okafor", "Depression, grief");
        therapist("dr.chen", "Wei", "Chen", "Students, life changes");
        client("jordan", "Jordan", "Kim");
        log.info("Demo accounts ready (dr.rivera, dr.okafor, dr.chen, jordan).");
    }

    private void therapist(String username, String first, String last, String specializations) {
        if (userRepository.existsByUsername(username)) {
            return;
        }
        User u = save(username, first, last, Role.THERAPIST, true);
        therapistProfileRepository.save(new TherapistProfile(u.getUserId(), specializations));
    }

    private void client(String username, String first, String last) {
        if (userRepository.existsByUsername(username)) {
            return;
        }
        User u = save(username, first, last, Role.CLIENT, true);
        clientProfileRepository.save(new ClientProfile(u.getUserId()));
    }

    private User save(String username, String first, String last, String role, boolean verified) {
        User u = new User();
        u.setUsername(username);
        u.setPasswordHash(passwordEncoder.encode(demoPassword));
        u.setFirstName(first);
        u.setLastName(last);
        u.setRole(role);
        u.setIsVerified(verified);
        return userRepository.save(u);
    }
}
