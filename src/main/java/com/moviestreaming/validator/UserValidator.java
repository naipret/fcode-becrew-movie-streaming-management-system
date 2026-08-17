package com.moviestreaming.validator;

import com.moviestreaming.exception.ValidationException;
import com.moviestreaming.model.User;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Domain validator for User account entities.
 */
public class UserValidator implements Validator<User> {

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,30}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    @Override
    public void validate(User user) throws ValidationException {
        List<String> errors = new ArrayList<>();

        if (user == null) {
            throw new ValidationException("User cannot be null");
        }

        if (user.getUsername() == null || !USERNAME_PATTERN.matcher(user.getUsername().trim()).matches()) {
            errors.add("Username must be 3-30 alphanumeric characters or underscores");
        }

        if (user.getPassword() == null || user.getPassword().length() < 6) {
            errors.add("Password must be at least 6 characters long");
        }

        if (user.getFullName() == null || user.getFullName().trim().isEmpty()) {
            errors.add("Full name cannot be empty");
        }

        if (user.getEmail() == null || !EMAIL_PATTERN.matcher(user.getEmail().trim()).matches()) {
            errors.add("Invalid email address format");
        }

        if (user.getRole() == null) {
            errors.add("User role must be specified");
        }

        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }
}
