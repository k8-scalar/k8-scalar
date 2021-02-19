
const TenantModel = require('../schemes/TenantModel');
const UserModel = require('../schemes/UserModel');

function getAllTenants_IdVersion() {
    return TenantModel.find().select('name version').exec()
}
function getAllTenants_Full() {
    return TenantModel.find().populate('users.ref', 'name ').exec()
}
function getTenantByName_Full(name) {
    return TenantModel.findOne({name: name}).populate('users.ref', 'name').exec();
}

function getTenantById_IdVersion(id) {
    return TenantModel.findOne({name: id}).select('name version').exec()
}

function createTenant(name, version){
    return new Promise(function (resolve, reject) {
        TenantModel.findOne({name: name}).then(function (tenant) {
            if(!tenant){
                const nTenant = new TenantModel({name: name, version: version, users: []});
                nTenant.save(function (err) {
                    if(err) reject(err);
                    else resolve(nTenant);
                })
            } else reject("Tenant already exists: " + name);
        }).catch(reject)
    });
}

function linkUserToTenant(tenant_id, user_id){
    return new Promise(function (resolve, reject) {
        TenantModel.findById(tenant_id).exec().then(function (tenant) {
            const l = tenant.users.length || 0;
            for(let i = 0; i < l; i++){
                if(tenant.users[i].ref.toString() === user_id) return reject("Already linked.")
            }
            tenant.users.push({ref: user_id});
            tenant.save(function (err) {
                if(err) reject(err);
                else UserModel.updateOne({_id: user_id}, {$set: {tenant: tenant}}, function (err) {
                   if(err) reject(err);
                   else resolve();
                });
            });
        });
    });
}

function upgradeTenant(name, version){
    return new Promise(function (resolve, reject) {
        TenantModel.findOne({name: name}).select('name version').exec().then(function (tenant) {
            tenant.version = version;
            tenant.save(function (err) {
                if(err) reject(err);
                else resolve(tenant);
            });
        })
    });
}

module.exports = {getAllTenants_IdVersion, getAllTenants_Full,getTenantById_IdVersion, linkUserToTenant,createTenant, getTenantByName_Full, upgradeTenant};
