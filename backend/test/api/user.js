'use strict';
/**
 * Created by gehhi on 02.05.2017.
 */

const mongoose = require('mongoose');
const chai = require('chai');
const chaiHttp = require('chai-http');
const server = require('../../server');
const util = require('../../lib/util');
chai.should();
const expect = chai.expect;
chai.use(chaiHttp);

const jwt = require('jsonwebtoken');
const config = require('config');

require('../../app/models/user');
const User = mongoose.model('User');

describe('User', () => {
    beforeEach((done) => {
        util.connectDatabase(mongoose);
        return done();
    });

    describe('/POST user', () => {

        it('should register a new user with valid data', (done) => {
            const registrationData = {
                name: 'test',
                email: 'mail@online.de',
                password: 'secret'
            };
            chai.request(server)
                .post('/user')
                .send(registrationData)
                .end((err, res) => {
                    res.should.have.status(200);
                    return done();
                });
        });

        it('registration without name should lead to error', (done) => {
            const registrationData = {
                email: 'mail@online.de',
                password: 'secret'
            };
            chai.request(server)
                .post('/user')
                .send(registrationData)
                .end((err, res) => {
                    res.should.have.status(400);
                    return done();
                });
        });

        it('registration without email should lead to error', (done) => {
            const registrationData = {
                name: 'test',
                password: 'secret'
            };
            chai.request(server)
                .post('/user')
                .send(registrationData)
                .end((err, res) => {
                    res.should.have.status(400);
                    return done();
                });
        });

        it('registration without pw should lead to error', (done) => {
            const registrationData = {
                email: 'mail@online.de',
                name: 'test'
            };
            chai.request(server)
                .post('/user')
                .send(registrationData)
                .end((err, res) => {
                    res.should.have.status(400);
                    return done();
                });
        });


    });
});