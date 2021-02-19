const createError = require('http-errors');
const express = require('express');
const path = require('path');
const cookieParser = require('cookie-parser');
const httpLogger = require('morgan');
const logger = require('./utils/logger');
const cors = require('cors');
const passport = require("passport");


let app = express();

app.set('views', path.join(__dirname, 'views'));
app.set('view engine', 'ejs');

app.use(httpLogger('dev'));
app.use(express.json());
app.use(express.urlencoded({ extended: false }));
app.use(cookieParser());
app.use(express.static(path.join(__dirname, 'public')));


app.use('*', cors());
app.use(function(req, res, next) {
  res.header("Access-Control-Allow-Origin", "*"); // update to match the domain you will make the request from
  res.header("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept, authentication-token, tenant-id");
  res.header("Access-Control-Expose-Headers", "Origin, X-Requested-With, Content-Type, Accept, authentication-token, tenant-id");
  next();
});


const mongooseController = require('./database/mongooseHandler');
mongooseController.connect();

app.use(passport.initialize());
const passportHandler = require('./utils/passportHandler');


const errorHandler = function(err, req, res, next) {
  // set locals, only providing error in development
  res.locals.message = err.message;
  res.locals.error = req.app.get('env') === 'development' ? err : {};

  // render the error page
  res.status(err.status || 500);
  res.render('error');
};


let checker = require('./utils/tenancyHandler');
checker.errorHandler = errorHandler;
checker.version = require('./utils/common').version;
app.use(checker.check);

let adminRouter = require("./routers/admin-api");
app.use('/admin', adminRouter);
let apiRouter = require("./routers/api");
app.use('/api', apiRouter);
let indexRouter = require("./routers/index");
app.use('/', indexRouter);

// catch 404 and forward to error handler
app.use(function(req, res, next) {
  next(createError(404));
});

// error handler
app.use(errorHandler);

logger.info("Application initialized");

module.exports = app;
