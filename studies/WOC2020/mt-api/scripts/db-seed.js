const {disconnect, connect, connection} = require('../database/mongooseHandler');
const logger = require('../utils/logger');

const {register} = require("../utils/authHandler");

const TenantController = require('../database/controllers/TenantController');
const authHandler = require('../utils/authHandler');

connect();
let versions = 3;
let tenant_names = ["abc", "def", "ghi"];
let tenant_promises=[];
let users_per_tenant = 1;
let user_promises=[];
let tenants = [];

let password = 'password';

function seed() {
    logger.warn("Seeding database.");
    seed_tenants()
        .then(t=>{
            logger.warn("Tenants seeded.");
            tenants = t;
            seed_users()
                .then(_ => logger.warn("Users seeded."))
                .catch(r=> {logger.error("Could not seed Users.");logger.error(r)})
                .finally(()=> disconnect())
        })
        .catch(r=> {logger.error("Could not seed tenants.");logger.error(r); disconnect()})
}

function seed_tenants() {
    for(let i = 0; i < versions; i++){
        tenant_promises.push(TenantController.createTenant(tenant_names[i], 'v'+(i+1)));
    }
    return Promise.all(tenant_promises)
}

function seed_users() {
    for(let i = 0; i < tenants.length; i++){
        let t_name = tenants[i].name;
        for(let j=0; j<users_per_tenant; j++){
            user_promises.push(authHandler.register('user-'+t_name+'-'+(j+1),password, tenants[i]));
        }
    }
    return Promise.all(user_promises)
}




TenantController.getAllTenants_IdVersion()
    .then(t=>{if(t == null || t.length <= 0) seed(); else {logger.warn("Database is not empty."); disconnect();}})
    .catch(r=> {disconnect(); logger.error(r)});
