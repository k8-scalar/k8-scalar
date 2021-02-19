let winston = require('winston');

let fs = require("fs");

let path = require("path");

let logger = {};
const name = "mt-api";

let errorLogFile = './logs/rocket-err.log';
let infoLogFile = './logs/rocket-info.log';

const { combine, timestamp, label, prettyPrint , printf, colorize} = winston.format;
const myFormat = printf(({ level, message, label, timestamp }) => {
    return `${timestamp} [${label}] ${level}: ${message}`;
});

let init = function () {


    logger = winston.createLogger({
        level: 'info',
        transports: [
            new winston.transports.File({ filename: path.normalize(errorLogFile), level: 'error' }),
            new winston.transports.File({ filename: path.normalize(infoLogFile) })
        ],
        format: combine(
            label({ label: name }),
            timestamp(),
            myFormat
        ),

    });
    logger.add(new winston.transports.Console({
        format: combine(
            label({ label: name }),
            timestamp(),
            colorize(),
            myFormat
        ),
        colorize: true
    }));

};

function createFiles (path, title) {
    let message = title || "MT-API\n";

    try{
        let infoFile = fs.statSync(path);
        if(!infoFile.isFile()){
            fs.writeFile(path, message);
        }
    } catch (err){
        if(err === 'ENOENT'){
            fs.writeFile(path, message);
        }
    }
}


createFiles(infoLogFile);
createFiles(errorLogFile);
init();

module.exports = logger;
