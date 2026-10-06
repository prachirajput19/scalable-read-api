package com.example.readapi.config;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;

class ResponseTimeFilterTest {

    @Test
    void addsResponseTimeHeaderAndPreservesBodyForApiRequests() throws Exception {
        ResponseTimeFilter filter = new ResponseTimeFilter();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/posts");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (req, res) -> res.getWriter().write("payload"));

        assertEquals("payload", response.getContentAsString());
        assertTrue(response.getHeader("X-Response-Time-Ms").matches("\\d+"));
    }

    @Test
    void skipsRequestsOutsideApiPath() {
        class ExposedFilter extends ResponseTimeFilter {
            boolean shouldSkip(HttpServletRequest request) {
                return shouldNotFilter(request);
            }
        }
        ExposedFilter filter = new ExposedFilter();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/dashboard");

        assertTrue(filter.shouldSkip(request));
        request.setRequestURI("/api/posts");
        assertFalse(filter.shouldSkip(request));
    }
}
