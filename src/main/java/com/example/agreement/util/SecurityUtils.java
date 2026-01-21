package com.example.agreement.util;

import com.example.agreement.entity.User;
import com.example.agreement.entity.CustomUserDetails;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {

    private SecurityUtils() {}

    public static CustomUserDetails currentUserDetails() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            throw new RuntimeException("Unauthorized");
        }

        Object principal = auth.getPrincipal();

        if (principal instanceof CustomUserDetails cud) {
            return cud;
        }

        assert principal != null;
        throw new RuntimeException("Invalid principal type: " + principal.getClass().getName());
    }

    public static User currentUser() {
        return currentUserDetails().getUser();
    }

    public static Long currentUserId() {
        return currentUser().getId();
    }

    public static Long currentUserIdOrNull() {
        try {
            return currentUserId();
        } catch (Exception e) {
            return null;
        }
    }

}
