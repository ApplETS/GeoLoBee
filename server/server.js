app = require('http').createServer()
  , io = require('socket.io').listen(app)
  , fs = require('fs')


app.listen(8080);

var Point = require("./dal/Point.class");
var Unit = require("./dal/Unit.class");
var Extractor = require("./dal/Extractor.class");

var OrionWorld = require("./dal/OrionWorld.class");
var Lobby = require("./dal/Lobby.class");
var GameClient = require("./dal/GameClient.class");

/*
var world = new OrionWorld(3000,2000);
var lobby = new Lobby();
*/



function getActiveLobbies() {
	var array = {};
	for(i in Lobby._instances) {
		var lobby = Lobby._instances[i];
        //if the lobby isn't started yet
        if(lobby.state == 0) {
		    array[i] = {id:lobby.id, name:lobby.name, maxPlayers:lobby.maxPlayers, nPlayers:lobby.nClients()};
        }
	}
    return array;
}
/*
function getAllPlayers() {
	var array = {};
	for(i in GameClient._instances) {
		var client = GameClient._instances[i];
		array[i] = {uid:client.id, units:{}};
		for(j in client.units) {
			array[i]['units'][j] = {id:client.units[j].id, x:client.units[j].position.x, y:client.units[j].position.y};
		}
	}
	return array;
}
*/
/* Socket.IO Handling */
io.sockets.on('connection', function (socket) {
	socket.on('client-connect', function (data) {
		var CLIENT;
		if(data && data['uid']){
			CLIENT = GameClient.get(data['uid']);
		}
		if(!CLIENT){
			CLIENT = new GameClient(socket);
		}
		else {
			CLIENT.socket = socket;
			socket.client = CLIENT; //Needed when answering calls after the initial connection
		}
		CLIENT.socket.emit("handshake", {'type':'connect', uid:CLIENT.id});

		socket.on('auth', function(data) {
			CLIENT.name = data['username'];
			CLIENT.socket.emit("auth", {username:data['username']});
		});

        socket.on('get-lobbies', function(data) {
            socket.emit('get-lobbies', {lobbies:getActiveLobbies()});
        });

        socket.on('create-lobby', function(data) {
            var lobby = new Lobby(CLIENT, data['game_name'], data['maxPlayers']);
            Lobby._instances[''+lobby.id] = lobby;
            socket.emit('lobby-created', {id:data['id'], lobby:lobby.id, lobby_name:lobby.name, maxPlayers:lobby.maxPlayers});
        });

        socket.on('lobby-back', function(data) {
            socket.emit('lobby-back', {});
        });

        socket.on('join-lobby', function(data) {
            var lobby = Lobby.get(data['lobby_id']);
            if(lobby != undefined) {
                if(lobby.nClients() < lobby.maxPlayers) {
                    var alreadyIn = false;
                    for(var i in lobby.clients) {
                        if(CLIENT === lobby.clients[i]) {
                            alreadyIn = true;
                        }
                    }
                    if(!alreadyIn) {
                        lobby.addClient(CLIENT);
                        CLIENT.currentLobby = lobby.id;
                        lobby.broadcast('lobby-refresh',{lobby:lobby.toJSON()});
                        socket.emit('lobby-joined', {lobby_id:lobby.id});
                    }
                }
            }
        });

        socket.on('get-lobby', function(data) {
            var lobby = Lobby.get(data['lobby_id']);
            if(lobby != undefined) {
                socket.emit('get-lobby', {lobby:lobby.toJSON()});
            }
        });

        socket.on('lobby-ready', function(data) {
            var lobby = Lobby.get(data['lobby_id']);
            lobby.broadcast('lobby-ready', {readyId:data['uid']});
        });

        socket.on('disconnect-lobby', function(data) {
            var id = CLIENT.id;
            lobby = Lobby._instances[data['lobby_id']]
            var wasOwner = false;
            var newId = -1;
            if(CLIENT === lobby.owner)
                wasOwner = true;

            delete lobby.clients[""+id];
            CLIENT.currentLobby = -1;
            if(wasOwner)
                newId = lobby.chooseNewOwner();
            lobby.broadcast('lobby-refresh',{lobby:lobby.toJSON()});
            socket.emit('disconnect-lobby', {});
         });

        socket.on('start-game', function(data) {
            var lobby = Lobby.get(data['lobby_id']);
            if(lobby != undefined) {
                if(lobby.state == 0) {
                    var newgame = lobby.startGame();
                    lobby.broadcast('game-started', {game:newgame.toJSON()});

                }
            }
        });

		// Map all controls
		socket.on('gameMessage', function (data) {
            if(CLIENT.currentLobby != -1) {
                var lobby = Lobby.get(CLIENT.currentLobby);
                if(data['type'] == 'move' || data['type'] == 'attack') {
                    for(var unitId in data['unitIds']){
                        lobby.game.players[CLIENT.id+""].units[''+data['unitIds'][unitId]].position.x = data['x'];
                        lobby.game.players[CLIENT.id+""].units[''+data['unitIds'][unitId]].position.y = data['y'];
                    }
                } else if(data['type'] == 'create-cargo' || data['type'] == 'create-builder' || data['type'] == 'create-attack') {
                    lobby.game.players[CLIENT.id+""].units[''+data['id']] = new Unit(data['id'], new Point(data['x'], data['y']));
                } else if(data['type'] == 'build-extractor') {
                    lobby.game.players[CLIENT.id+""].buildings[''+data['id']] = new Extractor(data['id'], new Point(data['x'], data['y']));
                } else if(data['type'] == 'frameCheck') {
                    if(lobby.isTooLow(data['uid'], data['currentFrame'])) {
                        lobby.game.wait = true;
                        lobby.broadcast('wait', {serverFrame:lobby.game.frame, lowestUid:data['uid']});
                    }
                } else if(data['type'] == 'done-waiting') {
                    lobby.game.wait = false;
                }
                lobby.broadcast('gameMessage', data);
            }
		});
		
		socket.on('disconnect', function () {
			var id = CLIENT.id;
            for(i in Lobby._instances){
                lobby = Lobby._instances[i]
                for(j in lobby.clients) {
                    if(CLIENT === lobby.clients[j]) {
                        var wasOwner = false;
                        var newId = -1;
                        if(CLIENT === lobby.owner)
                            wasOwner = true;

                        delete lobby.clients[""+id];
                        if(wasOwner)
                            newId = lobby.chooseNewOwner();

                        lobby.broadcast('disconnect-lobby', {lobby:lobby.toJSON()});
                    }
                }
            }
			delete GameClient._instances[""+id];
		});
	});
});
