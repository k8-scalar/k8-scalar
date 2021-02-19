const logger = require("./logger");
const bcrypt = require("bcrypt");
const {getUserByName_NamePassword, createUser} = require("../database/controllers/UserController");
const {linkUserToTenant} = require("../database/controllers/TenantController");

const tokenKey = "authentication-token";

const authenticate = function (name, password) {
    return new Promise(function (resolve, reject) {
        getUserByName_NamePassword(name).then(function (user) {
            verifyPassword(password, user.password).then(function (result) {
                resolve(result, user);
            }).catch(e=>{logger.error(e);reject("Could not verify credentials.")})
        }).catch(e=>{logger.error(e);reject("User not found: " + name)});
    });
};


const hashPassword = function (password) {
    const salt = bcrypt.genSaltSync();
    return bcrypt.hash(password, salt);

};

const verifyPassword = function (password, actual) {
    return bcrypt.compare(password, actual);
};

const register = function (name, password, tenant_id) {
    return new Promise(function (resolve, reject) {
        hashPassword(password).then(function (password_hash) {
            createUser(name, password_hash).then(function (user) {
                linkUserToTenant(tenant_id, user).then(() => resolve(user)).catch(reject)
            }).catch(reject)
        }).catch(reject);
    });
};


module.exports = {authenticate, tokenKey, register};
