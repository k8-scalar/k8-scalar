const express = require('express');
let router = express.Router();
const passport= require('passport');
const path = require('path');


router.get('/home', passport.authenticate('jwt', {session: false}), function (req, res, next)  {
    const tenant = req._tenant || {name: undefined};
    const domain = tenant.name;
    if(domain !== undefined){
        res.locals.tenant = req._tenant;
        res.locals.user = req._user;
        res.locals.user.password = "HIDDEN";
        res.status(200);
        res.render('home');
    } else res.status(400).json({error: "Could not retrieve tenant information."});
});

module.exports = router;
