package com.example.mt_api.repository;

import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.example.mt_api.entity.User;

public interface UserRepository extends MongoRepository<User, ObjectId> {
    public Optional<User> findByName(String name);
}
