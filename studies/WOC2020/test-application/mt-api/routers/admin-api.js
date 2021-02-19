const express = require('express');
let router = express.Router();
const passport = require('passport');

const {createTenant, getAllTenants_Full, getTenantByName_Full, upgradeTenant} = require("../database/controllers/TenantController");

const {getAllUsers_Full} = require("../database/controllers/UserController");

router.post('/tenant', function (req, res, next) {
    const {name, version} = req.body;
    createTenant(name, version)
        .then(t => res.status(200).json(t))
        .catch(r => res.status(500).json({error: r}));
});

router.get('/tenant/:name', function (req, res, next) {
    getTenantByName_Full(req.params.name)
        .then(t => res.status(200).json(t))
        .catch(r => res.status(500).json({error: r}));
});

router.post('/tenant/version', function (req, res, next) {
    const {name, version} = req.body;
    upgradeTenant(name, version)
        .then(t => res.status(200).json(t))
        .catch(r => res.status(500).json({error: r}));
});
router.post('/tenants/version', function (req, res, next) {
    const {names, version} = req.body;
    let promises = [];
    for(let i = 0; i < names.length; i++){
        promises.push(new Promise(function (resolve, reject) {
            upgradeTenant(names[i], version)
                .then(t => resolve(t))
                .catch(r => reject(r));
        }));
    }
    Promise.all(promises).then(t => res.status(200).json(t))
        .catch(r => res.status(500).json({error: r}));

});

router.get('/', function (req, res, next) {
    getAllTenants_Full().then(function (tenants) {
        getAllUsers_Full().then(function (users) {

            res.locals.tenant = req._tenant;
            res.locals.tenants = tenants;
            res.locals.users = users;
            res.locals.token = req._token || "none";
            res.status(200);
            res.render('admin');
        }).catch(r => res.status(500).json({error: r}))
    }).catch(r => res.status(500).json({error: r}));
});


module.exports = router;
