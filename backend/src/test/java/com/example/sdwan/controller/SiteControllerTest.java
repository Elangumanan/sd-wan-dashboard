package com.example.sdwan.controller;

import com.example.sdwan.domain.SiteHealth;
import com.example.sdwan.dto.SiteDetailDto;
import com.example.sdwan.dto.SiteListItemDto;
import com.example.sdwan.config.JacksonConfig;
import com.example.sdwan.exception.GlobalExceptionHandler;
import com.example.sdwan.exception.ResourceNotFoundException;
import com.example.sdwan.service.SiteService;
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

@WebMvcTest(SiteController.class)
@Import({JacksonConfig.class, GlobalExceptionHandler.class})
class SiteControllerTest {

    @Autowired MockMvc mockMvc;
    @MockBean  SiteService siteService;

    @Test
    void listSites_returns200WithWrappedList() throws Exception {
        var item = new SiteListItemDto("site-001", "org-001", "HQ", "NY", SiteHealth.HEALTHY, 3, 3, 0);
        when(siteService.getSitesByOrgId("org-001")).thenReturn(List.of(item));

        mockMvc.perform(get("/api/organizations/org-001/sites"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.value[0].id").value("site-001"))
                .andExpect(jsonPath("$.value[0].health").value("HEALTHY"));
    }

    @Test
    void getSite_returns404WithErrorCode() throws Exception {
        when(siteService.getSiteById("org-001", "bad"))
                .thenThrow(new ResourceNotFoundException("Site", "bad"));

        mockMvc.perform(get("/api/organizations/org-001/sites/bad"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    void getSite_returns200WithWrappedDetail() throws Exception {
        var detail = new SiteDetailDto("site-001", "org-001", "HQ", "NY",
                SiteHealth.DEGRADED, 2, 1, 1, List.of());
        when(siteService.getSiteById("org-001", "site-001")).thenReturn(detail);

        mockMvc.perform(get("/api/organizations/org-001/sites/site-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.value.health").value("DEGRADED"))
                .andExpect(jsonPath("$.value.onlineDeviceCount").value(1));
    }
}
