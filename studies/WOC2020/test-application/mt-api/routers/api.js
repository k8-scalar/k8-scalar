const express = require('express');
let router = express.Router();
let common = require("../utils/common");
const passport= require('passport');
const {authenticate, tokenKey, register} = require("../utils/authHandler");
const {login} = require("../utils/passportHandler");
const logger = require("../utils/logger");



router.get('/info', function (req, res, next) {
    res.status(200).json(common)
});

router.post('/login', function (req, res, next) {
    const {name, password} = req.body;
    authenticate(name,password).then(function (result, user) {
        if(result){
            login(req, user).then(function (token) {
                res.set(tokenKey, token);
                res.status(200).json({token: token});
            }).catch(function (err) {
                logger.error(err);
                res.status(400).json({error: "Login error"});
            });
        } else res.status(400).json({ error: "Credentials are incorrect."});
    }).catch(e => res.status(400).json({ error: e}));
});

router.post('/register', function (req,res, next) {
    const {name, password} = req.body;
    register(name, password, req._tenant)
        .then(()=>res.status(200).json({message: "User created"}))
        .catch(e=>{logger.error(e); res.status(500).json({error: e})});
});


router.get('/wait', passport.authenticate('jwt', { session : false }), function(req,res, next) {
    setTimeout(function () {
        res.status(200).json({message: "Waited for 1000ms."})
   }, 1000)
});


module.exports = router;
