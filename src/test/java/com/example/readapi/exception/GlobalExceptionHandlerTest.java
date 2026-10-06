package com.example.readapi.exception;

import org.junit.jupiter.api.Test;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void mapsMissingResourceToNotFoundResponse() {
        ResponseEntity<Map<String, Object>> response =
                handler.notFound(new ResourceNotFoundException("Author missing"));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(404, response.getBody().get("status"));
        assertEquals("Not Found", response.getBody().get("error"));
        assertEquals("Author missing", response.getBody().get("message"));
        assertNotNull(response.getBody().get("timestamp"));
    }

    @Test
    void mapsInvalidSortToBadRequestWithAllowedFields() {
        ResponseEntity<Map<String, Object>> response =
                handler.badSort(new InvalidDataAccessApiUsageException("invalid"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(((String) response.getBody().get("message")).contains("createdAt"));
    }
}
