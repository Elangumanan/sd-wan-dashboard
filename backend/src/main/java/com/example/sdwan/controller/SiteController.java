package com.example.sdwan.controller;

import com.example.sdwan.dto.SiteDetailDto;
import com.example.sdwan.dto.SiteListItemDto;
import com.example.sdwan.response.SuccessResponse;
import com.example.sdwan.service.SiteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/organizations/{orgId}/sites")
@Tag(name = "Sites", description = "Site-level endpoints scoped to an organization")
public class SiteController {

    private final SiteService siteService;

    public SiteController(SiteService siteService) {
        this.siteService = siteService;
    }

    @GetMapping
    @Operation(summary = "List all sites for an organization")
    public ResponseEntity<SuccessResponse<List<SiteListItemDto>>> listSites(@PathVariable String orgId) {
        return ResponseEntity.ok(SuccessResponse.of(siteService.getSitesByOrgId(orgId)));
    }

    @GetMapping("/{siteId}")
    @Operation(summary = "Get site detail including device summaries")
    public ResponseEntity<SuccessResponse<SiteDetailDto>> getSite(@PathVariable String orgId,
                                                                    @PathVariable String siteId) {
        return ResponseEntity.ok(SuccessResponse.of(siteService.getSiteById(orgId, siteId)));
    }
}
