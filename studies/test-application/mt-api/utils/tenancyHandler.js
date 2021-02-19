const logger = require('../utils/logger');
const {getTenantById_IdVersion} = require("../database/controllers/TenantController");
const tenancyHandler = {};

tenancyHandler.version = false;

tenancyHandler.idHeaderKey = 'tenant-id';
tenancyHandler.infoHeaderKey = 'tenant-info';

tenancyHandler.invalidateRequest = function(res, reason){
    res.status(400).json({reason: reason});
};


tenancyHandler.errorHandler = function (err, req, res, next) {
    logger.error(err);
    res.status(500).json({reason: "Could not retrieve tenant."});
};

tenancyHandler.switchVersion = function ( req, res, next) {
    res.status(500).json({reason: "Switching version"});
};

tenancyHandler.check = function (req, res, next) {
    const tenantID = req.query.tenant || req.headers[tenancyHandler.idHeaderKey];
    if(tenantID !== undefined && tenantID != null){
        if(tenantID === "admin") next();
        else {
            getTenantById_IdVersion(tenantID).then(function (tenant) {
                if(tenant.version === tenancyHandler.version){
                    req._tenant = tenant;
                    next();
                }
                else tenancyHandler.switchVersion(req, res, next);
            }).catch(e => tenancyHandler.errorHandler(e, req,res, next));
        }
    } else {
        tenancyHandler.invalidateRequest(res, "Missing header: tenant-id");
    }
};




module.exports = tenancyHandler;
