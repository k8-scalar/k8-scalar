package com.example.mt_api.service;

import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.mt_api.entity.Tenant;
import com.example.mt_api.entity.User;
import com.example.mt_api.repository.TenantRepository;
import com.example.mt_api.repository.UserRepository;

@Service
public class TenantService {

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private UserRepository userRepository;

    public void addUser(String tenantName, User user) {
        Tenant tenant = tenantRepository.findByName(tenantName).orElseThrow();

        ArrayList<User> users = tenant.getUsers();
        users.add(user);
        tenant.setUsers(users);
        tenantRepository.save(tenant);

        user.setTenant(tenant);
        userRepository.save(user);
    }

    public Tenant createTenant(Tenant tenant) throws Exception {
        Optional<Tenant> t = tenantRepository.findByName(tenant.getName());
        if (t.isPresent()) {
            throw new Exception("Tenant already exists: " + tenant.getName());
        }

        return tenantRepository.save(tenant);
    }

    public Tenant getTenant(String tenantName) throws Exception {
        Optional<Tenant> t = tenantRepository.findByName(tenantName);
        if (t.isEmpty()) {
            throw new Exception("Tenant does not exists: " + tenantName);
        }

        Tenant tenant = t.get();
        if (tenant.getUsers() != null && !tenant.getUsers().isEmpty()) {
            tenant.setUsers(tenant.getUsers().stream().peek(u -> u.setPassword("HIDDEN")).collect(Collectors.toCollection(ArrayList::new)));
        }

        return tenant;
    }

    public void clearAll() {
        tenantRepository.deleteAll();
    }

    public Tenant updateTenant(Tenant t) {
        return tenantRepository.save(t);
    }

}
