const {disconnect, connect} = require('../database/mongooseHandler');
const logger = require('../utils/logger');

connect();

let promises = [];


const TenantModel = require('../database/schemes/TenantModel');
const UserModel = require('../database/schemes/UserModel');

logger.warn("Clearing database.");
promises.push(new Promise(function (resolve, reject) {
    UserModel.deleteMany({}, function (err, d) {
        if(err){ logger.info(err); reject(err);}
        else {logger.info("UserModel: " + JSON.stringify(d));resolve(d); }
    });
}));

promises.push(new Promise(function (resolve, reject) {
    TenantModel.deleteMany({}, function (err, d) {
        if(err){ logger.info(err); reject(err);}
        else {logger.info("TenantModel: " + JSON.stringify(d));resolve(d); }
    });
}));

Promise.all(promises).then(_ => logger.warn("Database cleared."))
    .catch(_ => logger.error("Could not clear database.")).finally(()=>disconnect());
