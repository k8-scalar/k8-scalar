const passport = require('passport');
const jwt = require('jsonwebtoken');
const UserModel = require('../database/schemes/UserModel');


const JWTStrategy = require('passport-jwt').Strategy;
const ExtractJWT = require('passport-jwt').ExtractJwt;

const secret = process.env.JWT_SECRET || 'supersecret';

const signToken = function (user) {
    return jwt.sign({data:user}, secret, { expiresIn: 604800});
};


passport.serializeUser(function (user, done) {
    done(null, user._id);
});

passport.deserializeUser(function(id, done) {
    UserModel.findById(id).exec().then(user => done(null, user)).catch(err => done(err,null));
});

const extractJWT = function (req) {
    const token =  req.query.token || req.headers['authentication-token'] || null;
    req._token = token;
    return token;

};

const strategyOptions = {
    jwtFromRequest: extractJWT,
    secretOrKey: process.env.JWT_SECRET || 'supersecret',
    passReqToCallback: true
};

passport.use(new JWTStrategy(strategyOptions, function(req, jwt_payload, done) {
    UserModel.findOne({id: jwt_payload.sub}, function(err, user) {
        if (err) {
            return done(err, false);
        }
        if (user) {
            req._user = user;
            return done(null, user);
        } else {
            return done(null, false);
            // or you could create a new account
        }
    });
}));


const login = function(req, user){
    return new Promise((resolve, reject) => {
        req.login(user, { session: false }, err => {
            if (err) {
                return reject(err);
            } else {
                return resolve(signToken(user))
            }

        });
    })
};

module.exports = {login, signToken};
