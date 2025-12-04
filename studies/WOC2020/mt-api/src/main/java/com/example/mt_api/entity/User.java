package com.example.mt_api.entity;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Document(collection = "users")
public class User {
    @Id
    @JsonSerialize(using = ObjectIdSerializer.class)
    private ObjectId id;
    private String name;
    private String password;
    @DBRef
    @JsonBackReference
    private Tenant tenant;

    public User(String name, String password) {
        this.id = ObjectId.get();
        this.name = name;
        this.password = password;
    }

    public User(ObjectId id, String name, String password, Tenant tenant) {
        this.id = id;
        this.name = name;
        this.password = password;
        this.tenant = tenant;
    }

    public User() {
    }

    public ObjectId getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Tenant getTenant() {
        return tenant;
    }

    public void setTenant(Tenant tenant) {
        this.tenant = tenant;
    }

}
