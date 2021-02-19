
const UserModel = require('../schemes/UserModel');
const logger = require("../../utils/logger");

function getUserByName_NamePassword(name){
    return UserModel.findOne({ name }).select('name password').exec()
}

function createUser(name, password_hash){
    return new Promise(function (resolve, reject) {
        getUserByName_NamePassword(name).then(function (user) {
            if(!user) {
                const user = new UserModel({
                    name: name,
                    password: password_hash
                });
                user.save(function (err) {
                    if(err) reject(err);
                    else{
                        logger.info("[REGISTER] User created: "+ name);
                        resolve(user);}
                })
            }
            else {logger.info("[REGISTER] Duplicate user: "+ name);reject("Name already used: "+ name);}
        }).catch(reject)
    });
}

function getAllUsers_Full(){
    return UserModel.find().populate('tenant', 'name').exec()
}

module.exports = {getUserByName_NamePassword, createUser, getAllUsers_Full};
