package com.example.mt_api.repository;

import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.example.mt_api.entity.Tenant;

public interface TenantRepository extends MongoRepository<Tenant, ObjectId> {
    public Optional<Tenant> findByName(String name);
}
