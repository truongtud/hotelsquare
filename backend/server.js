'use strict';
const os = require('os');
const restify = require('restify');
const session = require('./app/routes/session');
const user = require('./app/routes/user');
const venue = require('./app/routes/venue');
const chat = require('./app/routes/chat');
const chatsocket = require('./app/routes/chatsocket');
const util = require('./lib/util');
const mongoose = require('mongoose');
const auth = require('./app/middleware/filter/authentication');
const fs = require('fs');

mongoose.Promise = global.Promise;

const server = restify.createServer();

const io = require('socket.io').listen(server);
chatsocket(io);

util.connectDatabase(mongoose).then(util.initDatabase);

server.use(restify.bodyParser({
    maxBodySize: 1024 * 1024,
    mapParams: true,
    mapFiles: true,
    overrideParams: false,
    keepExtensions: true,
    uploadDir: os.tmpdir(),
    multiples: true,
    hash: 'sha1'
}));

const bunyanLogger = util.initLogger(server);


// Session
server.post('sessions', session.postSession);

// User
server.get('users', auth, user.profile);
server.get('users/:name', user.profile);
server.get('users/id/:id', user.profileByID);

server.post('users', user.register);
server.post('users/:name/friend_requests', auth, user.sendFriendRequest);
server.put('users', auth, user.updateUser);
server.del('users', auth, user.deleteUser);

server.get('profile', auth, user.profile);

server.post('profile/avatar', auth, user.uploadAvatar);
server.del('profile/avatar', auth, user.deleteAvatar);

server.del('profile/friends/:name', auth, user.removeFriend);
server.put('profile/friend_requests/:name', auth, user.confirmFriendRequest);

// Chat
server.get('chats/:chatId', auth, chat.getConversation);
server.post('chats', auth, chat.newChat);
server.post('chats/:chatId/messages', auth, chat.replyMessage);
server.get('chats', auth, chat.getConversations);

// Venue
server.get('venues/:id', venue.getVenue);

server.post('venues/:id/comments', auth, venue.addComment);

server.put('venues/:id/checkin', auth, venue.checkin);

server.get('venues/comments', venue.getComments);
server.del('venues/comment', auth, venue.delComment);
server.post('venues/like', auth, venue.like);
server.post('venues/dislike', auth, venue.dislike);

// Search

// User
server.post('searches/users', auth, user.search);
// Venue
server.post('searches/venues', venue.queryVenue);
server.post('searches/venues/:page', venue.queryVenue);

// Delete downloads
server.on('after', (request) => {
    if (request.files) {
        const key = Object.keys(request.files);
        key.forEach((k) => {
            fs.unlink(request.files[k].path, () => {
            });
        });
    }
});

server.listen(8081, function () {
    bunyanLogger.info('%s listening at %s', server.name, server.url);
});

module.exports = server;