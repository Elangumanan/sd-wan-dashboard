package com.example.sdwan.controller;

import com.example.sdwan.dto.DeviceDetailDto;
import com.example.sdwan.dto.WanHistoryDto;
import com.example.sdwan.response.SuccessResponse;
import com.example.sdwan.service.DeviceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/organizations/{orgId}/sites/{siteId}/devices/{deviceId}")
@Validated
@Tag(name = "Devices", description = "Device-level detail and WAN telemetry endpoints")
public class DeviceController {

    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @GetMapping
    @Operation(summary = "Get device detail including all interfaces")
    public ResponseEntity<SuccessResponse<DeviceDetailDto>> getDevice(
            @PathVariable String orgId,
            @PathVariable String siteId,
            @PathVariable String deviceId) {
        return ResponseEntity.ok(SuccessResponse.of(deviceService.getDeviceById(orgId, siteId, deviceId)));
    }

    @GetMapping("/wan-history")
    @Operation(summary = "Get WAN bandwidth history for a device")
    public ResponseEntity<SuccessResponse<WanHistoryDto>> getWanHistory(
            @PathVariable String orgId,
            @PathVariable String siteId,
            @PathVariable String deviceId,
            @Parameter(description = "Time range: 1h, 6h, or 24h")
            @RequestParam(defaultValue = "24h")
            @Pattern(regexp = "1h|6h|24h", message = "range must be one of: 1h, 6h, 24h")
            String range) {
        return ResponseEntity.ok(SuccessResponse.of(deviceService.getWanHistory(orgId, siteId, deviceId, range)));
    }
}
