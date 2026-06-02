package com.example.sdwan.controller;

import com.example.sdwan.dto.OrganizationSummaryDto;
import com.example.sdwan.response.SuccessResponse;
import com.example.sdwan.service.OrganizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import java.util.List;

@RestController
@RequestMapping("/api/organizations")
@Tag(name = "Organizations", description = "Organization-level summary endpoints")
public class OrganizationController {

    private final OrganizationService organizationService;

    public OrganizationController(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    @GetMapping
    @Operation(summary = "List all organizations")
    public ResponseEntity<SuccessResponse<List<OrganizationSummaryDto>>> listOrganizations() {
        return ResponseEntity.ok(SuccessResponse.of(organizationService.getAllOrganizations()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get organization by ID")
    public ResponseEntity<SuccessResponse<OrganizationSummaryDto>> getOrganization(@PathVariable String id) {
        return ResponseEntity.ok(SuccessResponse.of(organizationService.getOrganizationById(id)));
    }

    @PostMapping
    @Operation(summary = "Not supported — organization data is read-only in this mock API")
    public ResponseEntity<Void> createOrganization(@RequestBody Map<String, Object> body) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }
}
