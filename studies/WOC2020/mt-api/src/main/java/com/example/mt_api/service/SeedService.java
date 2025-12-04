package com.example.mt_api.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.mt_api.entity.Tenant;
import com.example.mt_api.entity.User;
import com.example.mt_api.repository.TenantRepository;

@Service
public class SeedService {
    private final Logger logger = LoggerFactory.getLogger(SeedService.class); // Logger instance for logging.

    @Value("${FORCE_SEED:false}")
    private boolean FORCE_SEED;

    @Autowired
    private TenantService tenantService;

    @Autowired
    private UserInfoService userInfoService;

    @Autowired
    private TenantRepository tenantRepository;

    public void clearData() {
        tenantService.clearAll();
        userInfoService.clearAll();
    }

    private final String[] tenant_names = { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13" };

    private final int users_per_tenant = 1;

    public void seedData() throws Exception {
        var hasData = tenantRepository.count() > 1;
        logger.info("[SeedService.seedData()] - FORCE_SEED: {}; hasData: {}", FORCE_SEED, hasData);

        if (hasData && !FORCE_SEED) {
            return;
        }

        

        logger.info("[SeedService.seedData()] - Clearing data,");
        clearData();
        logger.info("[SeedService.seedData()] - Data cleared.");

        logger.info("[SeedService.seedData()] - Start seeding {} tenant(s) with {} user(s).", tenant_names.length, users_per_tenant);
        
        for (int i = 0; i < tenant_names.length; i++) {
            String t_name = tenant_names[i];
            tenantService.createTenant(new Tenant(t_name, "v1"));
            for (int j = 0; j < users_per_tenant; j++) {
                
                User u = new User("user-" + t_name + "-" + (j + 1), "password");
 
                logger.info("[SeedService.seedData()] - Seeding new user: {}.", u.getName());

                userInfoService.addUser(u);
                tenantService.addUser(t_name, u);
            }
        }

        logger.info("[SeedService.seedData()] - Seeding finished.");
    }
}
