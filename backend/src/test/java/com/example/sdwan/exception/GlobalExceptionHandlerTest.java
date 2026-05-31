package com.example.sdwan.exception;

import com.example.sdwan.controller.OrganizationController;
import com.example.sdwan.service.OrganizationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests GlobalExceptionHandler end-to-end through OrganizationController as a host.
 * Verifies the ErrorResponse shape (code, message, details) for each exception type.
 */
@WebMvcTest(OrganizationController.class)
@Import(GlobalExceptionHandler.class)
class GlobalExceptionHandlerTest {

    @Autowired MockMvc mockMvc;
    @MockBean  OrganizationService organizationService;

    // ── AppException subclasses (handled by single AppException handler) ───────

    @Test
    void returns404_forResourceNotFoundException() throws Exception {
        when(organizationService.getOrganizationById("missing"))
                .thenThrow(ExceptionFactory.resourceNotFound("Organization", "missing"));

        mockMvc.perform(get("/api/organizations/missing"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Organization not found: missing"))
                .andExpect(jsonPath("$.details").doesNotExist());
    }

    @Test
    void returns400_forValidationException() throws Exception {
        when(organizationService.getOrganizationById("bad"))
                .thenThrow(ExceptionFactory.validationError("orgId", "must not be blank"));

        mockMvc.perform(get("/api/organizations/bad"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("'orgId': must not be blank"));
    }

    @Test
    void returns422_forBusinessException() throws Exception {
        when(organizationService.getOrganizationById("any"))
                .thenThrow(ExceptionFactory.businessError("Organization is suspended"));

        mockMvc.perform(get("/api/organizations/any"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("BUSINESS_ERROR"))
                .andExpect(jsonPath("$.message").value("Organization is suspended"));
    }

    @Test
    void returns401_forUnauthorizedException() throws Exception {
        when(organizationService.getOrganizationById("any"))
                .thenThrow(ExceptionFactory.unauthorized("Invalid API key"));

        mockMvc.perform(get("/api/organizations/any"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("Invalid API key"));
    }

    // ── Infrastructure exceptions (individual handlers) ────────────────────────

    @Test
    void returns400_forIllegalArgumentException() throws Exception {
        when(organizationService.getOrganizationById("bad"))
                .thenThrow(new IllegalArgumentException("Unsupported region"));

        mockMvc.perform(get("/api/organizations/bad"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("ILLEGAL_ARGUMENT"))
                .andExpect(jsonPath("$.message").value("Unsupported region"));
    }

    @Test
    void returns400_forMalformedRequestBody() throws Exception {
        mockMvc.perform(post("/api/organizations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ not valid json }"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("MALFORMED_REQUEST"))
                .andExpect(jsonPath("$.message").value("Request body is missing or malformed"));
    }

    @Test
    void returns405_forMethodNotAllowed() throws Exception {
        mockMvc.perform(delete("/api/organizations/org-001"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.code").value("METHOD_NOT_ALLOWED"));
    }

    @Test
    void returns500_forUnexpectedRuntimeException() throws Exception {
        when(organizationService.getAllOrganizations())
                .thenThrow(new RuntimeException("Disk full"));

        mockMvc.perform(get("/api/organizations"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"));
    }

    // ── Response structure ─────────────────────────────────────────────────────

    @Test
    void errorResponse_alwaysContainsCodeAndMessage() throws Exception {
        when(organizationService.getOrganizationById("x"))
                .thenThrow(ExceptionFactory.resourceNotFound("Organization", "x"));

        mockMvc.perform(get("/api/organizations/x"))
                .andExpect(jsonPath("$.code").isNotEmpty())
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void errorResponse_detailsAbsent_forSimpleErrors() throws Exception {
        when(organizationService.getOrganizationById("x"))
                .thenThrow(ExceptionFactory.resourceNotFound("Organization", "x"));

        mockMvc.perform(get("/api/organizations/x"))
                .andExpect(jsonPath("$.details").doesNotExist());
    }
}
