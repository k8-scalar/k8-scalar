package com.example.mt_api.entity;

import java.util.ArrayList;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Document(collection = "tenants")
public class Tenant {
    @Id
    @JsonSerialize(using = ObjectIdSerializer.class)
    private ObjectId id;
    private String name;
    @DBRef
    @JsonManagedReference
    private ArrayList<User> users;
    private String version;

    public Tenant(String name, String version) {
        this.id = ObjectId.get();
        this.name = name;
        this.version = version;
        this.users = new ArrayList<>();
    }

    public Tenant() {
        this.users = new ArrayList<>();
    }

    public Tenant(ObjectId id, String name, ArrayList<User> users, String version) {
        this.id = id;
        this.name = name;
        this.users = users;
        this.version = version;
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

    @SuppressWarnings("unchecked")
    public ArrayList<User> getUsers() {
        return (ArrayList<User>) users.clone();
    }

    public void setUsers(ArrayList<User> users) {
        this.users = users;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

}
