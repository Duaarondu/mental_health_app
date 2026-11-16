package com.mentalhealth.mentalhealth_backend.service;

import java.security.SecureRandom;
import java.util.regex.Pattern;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mentalhealth.mentalhealth_backend.dto.AuthRequests.RegisterRequest;
import com.mentalhealth.mentalhealth_backend.model.ClientProfile;
import com.mentalhealth.mentalhealth_backend.model.Role;
import com.mentalhealth.mentalhealth_backend.model.TherapistProfile;
import com.mentalhealth.mentalhealth_backend.model.User;
import com.mentalhealth.mentalhealth_backend.repository.ClientProfileRepository;
import com.mentalhealth.mentalhealth_backend.repository.TherapistProfileRepository;
import com.mentalhealth.mentalhealth_backend.repository.UserRepository;

/** Creating accounts: patients, therapists and anonymous guests. */
@Service
@Transactional
public class AuthService {

    private static final Pattern USERNAME = Pattern.compile("^[A-Za-z0-9_.-]{3,50}$");
    private static final String CODE_CHARS = "abcdefghjkmnpqrstuvwxyz23456789"; // no look-alikes (l/1, o/0)
    private static final String[] ADJECTIVES = {"Quiet", "Gentle", "Calm", "Bright", "Steady", "Kind", "Brave", "Soft"};
    private static final String[] NOUNS = {"Fox", "River", "Pine", "Sparrow", "Harbor", "Willow", "Otter", "Meadow"};

    private final UserRepository userRepository;
    private final ClientProfileRepository clientProfileRepository;
    private final TherapistProfileRepository therapistProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom random = new SecureRandom();

    public AuthService(UserRepository userRepository, ClientProfileRepository clientProfileRepository,
            TherapistProfileRepository therapistProfileRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.clientProfileRepository = clientProfileRepository;
        this.therapistProfileRepository = therapistProfileRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /** Result of making an anonymous account: the recovery code is shown to the user once. */
    public record AnonymousAccount(User user, String recoveryCode) {
    }

    /**
     * Patients can use the app straight away. New therapists start unverified and are not
     * offered for booking until someone sets USER.isVerified = TRUE for them.
     */
    public User register(RegisterRequest r) {
        String username = trim(r.username());
        String firstName = trim(r.firstName());
        String lastName = trim(r.lastName());
        String password = r.password() == null ? "" : r.password();
        boolean therapist = "therapist".equalsIgnoreCase(trim(r.accountType()));

        if (!USERNAME.matcher(username).matches()) {
            throw ApiException.badRequest("Username must be 3 to 50 letters, numbers, dots, dashes or underscores.");
        }
        if (username.toLowerCase().startsWith("anon-")) {
            throw ApiException.badRequest("Usernames starting with \"anon-\" are reserved.");
        }
        if (password.length() < 8 || password.length() > 72) {
            throw ApiException.badRequest("Password must be 8 to 72 characters.");
        }
        if (firstName.isEmpty() || lastName.isEmpty() || firstName.length() > 50 || lastName.length() > 50) {
            throw ApiException.badRequest("Please enter your first and last name (up to 50 characters each).");
        }
        if (userRepository.existsByUsername(username)) {
            throw ApiException.conflict("That username is taken. Please choose another.");
        }

        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setRole(therapist ? Role.THERAPIST : Role.CLIENT);
        user.setIsVerified(false);
        user = userRepository.save(user);

        if (therapist) {
            String specs = trim(r.specializations());
            therapistProfileRepository.save(new TherapistProfile(user.getUserId(),
                    specs.length() > 255 ? specs.substring(0, 255) : specs));
        } else {
            clientProfileRepository.save(new ClientProfile(user.getUserId()));
        }
        return user;
    }

    /**
     * An anonymous guest: no real name, just a nickname. They get a generated username and
     * recovery code so they can come back to their bookings later.
     */
    public AnonymousAccount createAnonymous(String nickname) {
        String nick = trim(nickname);
        if (nick.isEmpty()) {
            nick = ADJECTIVES[random.nextInt(ADJECTIVES.length)] + " " + NOUNS[random.nextInt(NOUNS.length)];
        }
        if (nick.length() > 30) {
            throw ApiException.badRequest("Nickname must be 30 characters or fewer.");
        }

        String username;
        do {
            username = "anon-" + randomChars(8);
        } while (userRepository.existsByUsername(username));
        String recoveryCode = randomChars(4) + "-" + randomChars(4) + "-" + randomChars(4);

        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(recoveryCode));
        user.setFirstName(nick);
        user.setLastName("Guest");
        user.setRole(Role.ANONYMOUS);
        user.setIsVerified(false);
        user = userRepository.save(user);
        clientProfileRepository.save(new ClientProfile(user.getUserId()));
        return new AnonymousAccount(user, recoveryCode);
    }

    private String randomChars(int n) {
        StringBuilder sb = new StringBuilder(n);
        for (int i = 0; i < n; i++) {
            sb.append(CODE_CHARS.charAt(random.nextInt(CODE_CHARS.length())));
        }
        return sb.toString();
    }

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }
}
