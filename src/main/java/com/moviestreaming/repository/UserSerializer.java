package com.moviestreaming.repository;

import com.moviestreaming.model.User;
import com.moviestreaming.model.UserRole;
import com.moviestreaming.util.CsvSanitizer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

/**
 * CSV Serializer for User entity.
 */
public class UserSerializer implements CsvSerializer<User, String> {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Override
    public String getHeader() {
        return "id|username|password|fullName|email|role|createdAt";
    }

    @Override
    public String serialize(User user) {
        if (user == null) {
            return "";
        }
        String createdAtStr = user.getCreatedAt() != null
                ? user.getCreatedAt().format(DATE_FORMATTER)
                : LocalDateTime.now().format(DATE_FORMATTER);

        return CsvSanitizer.join(Arrays.asList(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                user.getFullName(),
                user.getEmail(),
                user.getRole() != null ? user.getRole().name() : UserRole.USER.name(),
                createdAtStr
        ));
    }

    @Override
    public User deserialize(String csvLine) {
        List<String> tokens = CsvSanitizer.split(csvLine);
        if (tokens.size() < 7) {
            throw new IllegalArgumentException("Invalid User CSV format, expected 7 columns but got: " + tokens.size());
        }

        String id = tokens.get(0);
        String username = tokens.get(1);
        String password = tokens.get(2);
        String fullName = tokens.get(3);
        String email = tokens.get(4);
        UserRole role = UserRole.valueOf(tokens.get(5).trim().toUpperCase());
        LocalDateTime createdAt = LocalDateTime.parse(tokens.get(6).trim(), DATE_FORMATTER);

        return new User(id, username, password, fullName, email, role, createdAt);
    }

    @Override
    public String extractId(User user) {
        return user != null ? user.getId() : null;
    }
}
