const mongoose = require('mongoose');

const UserSchema = new mongoose.Schema({
    name: {
        type: String,
        index: true,
        unique: true

    },
    password: String,
    tenant: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'Tenant'
    },
});

const UserModel = mongoose.model('User', UserSchema);
module.exports = UserModel;
