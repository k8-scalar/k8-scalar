const mongoose = require('mongoose');

const TenantSchema = new mongoose.Schema({
    name: {
        type: String,
        index: true,
        unique: true
    },
    users: [{
        ref: {
            type: mongoose.Schema.Types.ObjectId,
            ref: 'User'
        }
    }],
    version: String
});

const TenantModel = mongoose.model('Tenant', TenantSchema);
module.exports = TenantModel;
