package dev.sweetme.controller;

import dev.sweetme.util.SessionHelper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public abstract class BaseApiController {

    protected String getSessionUsername(HttpServletRequest request) {
        return SessionHelper.getUsername(request);
    }

    protected boolean isAdmin(HttpServletRequest request) {
        return SessionHelper.isAdmin(request);
    }

    protected <T> ResponseEntity<T> unauthorized() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}
