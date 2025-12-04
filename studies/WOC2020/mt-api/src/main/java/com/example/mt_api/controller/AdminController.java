package com.example.mt_api.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.mt_api.entity.MultiUpgradeRequest;
import com.example.mt_api.entity.Tenant;
import com.example.mt_api.service.TenantService;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private TenantService tenantService;

    @PostMapping("/tenant")
    public ResponseEntity<Object> register(@RequestBody Tenant tenant) {
        try {
            Tenant t = tenantService.createTenant(tenant);
            return ResponseEntity.ok(t);
        } catch (Exception e) {
            return mapError(e);
        }
    }

    @GetMapping("/tenant/{tenantName}")
    public ResponseEntity<Object> getTenant(@PathVariable("tenantName") String tenantName) {
        try {
            return ResponseEntity.ok(tenantService.getTenant(tenantName));
        } catch (Exception e) {
            return mapError(e);
        }
    }

    @PostMapping("/tenant/version")
    public ResponseEntity<Object> upgrade(@RequestBody Tenant tenant, @RequestHeader("tenant-id") String tenantHeader) {
        String name = tenant.getName();
        String version = tenant.getVersion();

        if (!tenantHeader.equals("admin")) {
            if (!tenantHeader.equals(name)) {
                Map<String, Object> response = new HashMap<>();
                response.put("reason", "Switching version");
                return ResponseEntity.internalServerError().body(response);
            }
        }

        try {
            Tenant t = tenantService.getTenant(name);
            t.setVersion(version);
            return ResponseEntity.ok(tenantService.updateTenant(t));
        } catch (Exception e) {
            return mapError(e);
        }
    }

    @PostMapping("/tenants/version")
    public ResponseEntity<Object> upgradeMulti(@RequestBody MultiUpgradeRequest request,
            @RequestHeader("tenant-id") String tenantHeader) {

        ArrayList<Tenant> tenants = new ArrayList<>();
        for (String names : request.getNames()) {
            ResponseEntity<Object> res = upgrade(new Tenant(names, request.getVersion()), tenantHeader);
            if (!res.getStatusCode().is2xxSuccessful()) {
                return res;
            }

            tenants.add((Tenant) res.getBody());
        }

        return ResponseEntity.ok(tenants);
    }

    private ResponseEntity<Object> mapError(Exception e) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", e.getMessage());
        return ResponseEntity.internalServerError().body(response);
    }
}
