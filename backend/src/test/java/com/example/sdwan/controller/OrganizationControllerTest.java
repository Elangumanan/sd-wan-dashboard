package com.example.sdwan.controller;

import com.example.sdwan.dto.OrganizationSummaryDto;
import com.example.sdwan.config.JacksonConfig;
import com.example.sdwan.exception.GlobalExceptionHandler;
import com.example.sdwan.exception.ResourceNotFoundException;
import com.example.sdwan.service.OrganizationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrganizationController.class)
@Import({JacksonConfig.class, GlobalExceptionHandler.class})
class OrganizationControllerTest {

    @Autowired MockMvc mockMvc;
    @MockBean  OrganizationService organizationService;

    private static final OrganizationSummaryDto DTO =
            new OrganizationSummaryDto("org-001", "Acme", "desc", "NA", 4, 2, 1, 1);

    @Test
    void listOrganizations_returns200WithWrappedList() throws Exception {
        when(organizationService.getAllOrganizations()).thenReturn(List.of(DTO));

        mockMvc.perform(get("/api/organizations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.value[0].id").value("org-001"))
                .andExpect(jsonPath("$.value[0].totalSites").value(4))
                .andExpect(jsonPath("$.value[0].healthySites").value(2));
    }

    @Test
    void getOrganization_returns200WithWrappedDto() throws Exception {
        when(organizationService.getOrganizationById("org-001")).thenReturn(DTO);

        mockMvc.perform(get("/api/organizations/org-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.value.name").value("Acme"))
                .andExpect(jsonPath("$.value.id").value("org-001"));
    }

    @Test
    void getOrganization_returns404WithErrorCode() throws Exception {
        when(organizationService.getOrganizationById("bad"))
                .thenThrow(new ResourceNotFoundException("Organization", "bad"));

        mockMvc.perform(get("/api/organizations/bad"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Organization not found: bad"));
    }
}
