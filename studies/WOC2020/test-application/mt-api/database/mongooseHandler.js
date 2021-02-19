const mongoose = require('mongoose');
const logger = require('../utils/logger');

mongoose.Promise = global.Promise;

let db = {};

const connect = function() {
    logger.info("Connecting to database.");
    const connString = process.env.DB_CONNECTION_STRING || 'mongodb://localhost:27017/mt-api';
    const options = {
        useFindAndModify: false,
        autoIndex: false,
        poolSize: 10,
        bufferMaxEntries: 0,
        useNewUrlParser: true,
        useUnifiedTopology: true
    };
    logger.info("Connecting to: "+ connString);

    mongoose.connection.on('error', function (err) {
        if(err){
            logger.error("Could not connect to db.");
            logger.error(err);
        }
    });

    if(mongoose.connection.readyState !== 1){
        db = mongoose.connect(connString, options);
    } else {
        db = mongoose;
    }
};

const connection = function(){
    return mongoose.connection;
};

const disconnect = function(){
    logger.info("Disconnecting from database.");

    return mongoose.disconnect();
};
module.exports = {connection, connect, disconnect};
