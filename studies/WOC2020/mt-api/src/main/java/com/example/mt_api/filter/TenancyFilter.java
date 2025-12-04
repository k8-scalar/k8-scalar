package com.example.mt_api.filter;

import java.io.IOException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.mt_api.entity.Tenant;
import com.example.mt_api.service.TenantService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class TenancyFilter extends OncePerRequestFilter {

    @Autowired
    private TenantService tenantService;

    // Constants for tenant header key and admin tenant ID
    private static final String TENANT_ID_HEADER_KEY = "tenant-id";
    private static final String ADMIN_TENANT_ID = "admin";
    @Value("${EXPECTED_VERSION:v1}")
    private String EXPECTED_VERSION;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            // Get tenant ID from query parameter or header
            String tenantId = Optional.ofNullable(request.getParameter("tenant"))
                    .orElse(request.getHeader(TENANT_ID_HEADER_KEY));

            if (tenantId == null || tenantId.isEmpty()) {
                invalidateRequest(response);
                return;
            }

            // If tenant ID is "admin", allow the request to proceed
            if (ADMIN_TENANT_ID.equals(tenantId)) {
                System.out.println("Tenant id admin provided");
                filterChain.doFilter(request, response);
                return;
            }

            // Fetch tenant information based on tenantId
            Tenant tenant = tenantService.getTenant(tenantId);

            // Check tenant version
            if (EXPECTED_VERSION.equals(tenant.getVersion())) {
                // Attach tenant information to request for further use
                request.setAttribute("_tenant", tenant);
                filterChain.doFilter(request, response);
            } else {
                switchVersion(response);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            errorHandler(response);
        }
    }

    private void invalidateRequest(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.setContentType("application/json");
        response.getWriter().write("{\"reason\": \"Missing header: tenant-id\"}");
    }

    private void errorHandler(HttpServletResponse response)
            throws IOException {
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        response.setContentType("application/json");
        response.getWriter().write("{\"reason\": \"Could not retrieve tenant.\"}");
    }

    private void switchVersion(HttpServletResponse response)
            throws IOException {
        response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
        response.setContentType("application/json");
        response.getWriter().write("{\"reason\": \"Switch version\"}");
    }

}
