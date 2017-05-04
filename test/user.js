const mock = require('mock-require');
const bcrypt = require('bcrypt');

let failBcrypt = false;

mock('bcrypt', {
    hash: function(password, salt_work) {
        if(failBcrypt)
            return Promise.reject('Cant hash password!');
        else
            return bcrypt.hash(password, salt_work);
    },
    compare: bcrypt.compare
});


require('../app/models/user');

const mongoose = require('mongoose');
const chai = require('chai');
const expect = chai.expect;
const chaiAsPromised = require('chai-as-promised');
chai.use(chaiAsPromised);

const Util = require('../lib/util');

const User = mongoose.model('User');

describe('user', function () {


    let validUser;

    beforeEach(function (done) {
        failBcrypt = false;
        mongoose.Promise = global.Promise;

        validUser = new User({
            name: 'Test',
            email: 'test@test.de',
            password: 'password'
        });

        if (mongoose.connection.db) return done();
        Util.connectDatabase(mongoose).then(function () {
            User.remove({}, done);
        });
    });

    it('can be saved', function (done) {
        const u = new User(validUser);
        u.save(done);
    });

    describe('#name', function () {
        it('cant be empty', function (done) {
            validUser.name = '';
            const u = new User(validUser);

            u.validate(function (err) {
                expect(err).to.not.be.null;
                expect(err.errors.name).to.exist;
                done();
            });
        });

        it('with 3 characters is invalid', function (done) {
            validUser.name = 'a'.repeat(3);
            const u = new User(validUser);

            u.validate(function (err) {
                expect(err).to.not.be.null;
                expect(err.errors.name).to.exist;
                return done();
            });
        });

        it('with 4 characters is valid', function (done) {
            validUser.name = 'a'.repeat(4);
            const u = new User(validUser);

            u.validate(function (err) {
                expect(err).to.be.null;
                return done();
            });
        });
    });

    describe('#email', function () {
        it('cant be empty', function (done) {
            validUser.email = '';
            const u = new User(validUser);
            u.validate(function (err) {
                expect(err).to.not.be.null;
                expect(err.errors.email).to.exist;
                return done();
            });
        });

        it('must be a valid email', function (done) {
            validUser.email = 'test.test.de';
            const u = new User(validUser);
            u.validate(function (err) {
                expect(err).to.not.be.null;
                expect(err.errors.email).to.exist;
                validUser.email = 'test@test.de';
                const u = new User(validUser);
                u.validate(function (err) {
                    expect(err).to.be.null;
                    return done();
                });
            });
        });
    });

    describe('#password', function () {

        it('should have a minimum length of 6 characters', function (done) {
            const u = new User(validUser);
            u.password = '12345';

            u.validate(function (err) {
                expect(err).to.not.be.null;
                expect(err.errors.password).to.exist;
                u.password = '123456';
                u.validate(function (err) {
                    expect(err).to.be.null;
                    return done();
                });
            });
        });

        it('should stay valid when not changed', function (done) {
            const u = new User(validUser);
            u.save().then((res) => {
                res.name = 'Blub';
                res.validate().then(done);
            });
        });

        it('should be hashed when saved', function (done) {
            const u = new User(validUser);
            u.save(function () {
                expect(u.password).to.be.not.equal(validUser.password);
                return done();
            });
        });

        it('should not change when already hashed and saved again', (done) => {
            const u = new User(validUser);
            u.save().then((res) => {
                const hashed_password = res.password;
                res.save().then((res) => {
                    expect(hashed_password).to.be.equal(res.password);
                    return done();
                });
            });
        });

        it('should not be converted into json', function () {
            const u = new User(validUser);
            expect(u.toJSON().password).to.be.undefined;
        });

        it('should not save object if password cant be hashed', () => {
            failBcrypt = true;
            const u = new User(validUser);
            return expect(u.save()).to.eventually.rejected;
        });
    });

    describe('comparePassword', () => {
        it('compare should yield a true result with correct plain text password', function (done) {
            const u = new User(validUser);
            u.save().then(function () {
                u.comparePassword(validUser.password).then(function (res) {
                    expect(res).to.be.true;
                    return done();
                });
            });
        });

        it('compare should yield a false result with wrong plain text password', function (done) {
            const u = new User(validUser);
            u.save().then(function () {
                u.comparePassword('haxor').then(function (res) {
                    expect(res).to.be.false;
                    return done();
                });
            });
        });
    });

    describe('login', function () {
        beforeEach(function (done) {

            validUser = new User({
                name: 'Test',
                email: 'test@test.de',
                password: 'password'
            });
            User.remove({}, function () {
                validUser.save(done);
            });
        });

        it('should return the user with valid name and password', function () {
            return expect(User.login(validUser.name, 'password').then(function (u) {
                return Promise.resolve(u.equals(validUser));
            })).to.eventually.equal(true);
        });

        it('should reject with invalid password', function () {
            return expect(User.login(validUser.name, 'wrong')).to.eventually.rejected;
        });

        it('should reject with invalid name', function () {
            return expect(User.login('test', 'password')).to.eventually.rejected;
        });

        it('should work with a json object as first parameter', () => {
            return expect(User.login({name: 'test', password: 'password'})).to.eventually.resolved;
        });
    });
});