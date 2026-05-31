package com.example.sdwan.controller;

import com.example.sdwan.domain.DeviceStatus;
import com.example.sdwan.dto.DeviceDetailDto;
import com.example.sdwan.dto.WanHistoryDto;
import com.example.sdwan.exception.GlobalExceptionHandler;
import com.example.sdwan.exception.ResourceNotFoundException;
import com.example.sdwan.service.DeviceService;
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

@WebMvcTest(DeviceController.class)
@Import(GlobalExceptionHandler.class)
class DeviceControllerTest {

    @Autowired MockMvc mockMvc;
    @MockBean  DeviceService deviceService;

    private static final String BASE = "/api/organizations/org-001/sites/site-001/devices/dev-001";

    @Test
    void getDevice_returns200WithWrappedDto() throws Exception {
        var dto = new DeviceDetailDto("dev-001", "site-001", "NYC-EDGE-01", "Cisco ISR 4331",
                DeviceStatus.ONLINE, "10.1.1.1", "17.12.3", "2026-05-31T00:00:00Z", List.of());
        when(deviceService.getDeviceById("org-001", "site-001", "dev-001")).thenReturn(dto);

        mockMvc.perform(get(BASE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.value.name").value("NYC-EDGE-01"))
                .andExpect(jsonPath("$.value.status").value("ONLINE"));
    }

    @Test
    void getDevice_returns404WithErrorCode() throws Exception {
        when(deviceService.getDeviceById(any(), any(), any()))
                .thenThrow(new ResourceNotFoundException("Device", "dev-001"));

        mockMvc.perform(get(BASE))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    void getWanHistory_returns200WithWrappedDto() throws Exception {
        var dto = new WanHistoryDto("dev-001", "NYC-EDGE-01", "24h", List.of());
        when(deviceService.getWanHistory("org-001", "site-001", "dev-001", "24h")).thenReturn(dto);

        mockMvc.perform(get(BASE + "/wan-history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.value.range").value("24h"))
                .andExpect(jsonPath("$.value.deviceId").value("dev-001"));
    }

    @Test
    void getWanHistory_returns400ForInvalidRange() throws Exception {
        mockMvc.perform(get(BASE + "/wan-history").param("range", "48h"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("CONSTRAINT_VIOLATION"));
    }
}
