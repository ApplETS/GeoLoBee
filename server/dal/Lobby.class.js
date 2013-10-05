var Game = require("./Game.class");

function Lobby(owner, name, maxPlayers) {
	this.id = ++Lobby.ID_SEQ;
	Lobby._instances[this.id+""] = this;
	
	this.clients = {};

	this.owner = owner;
    this.name = name;
    this.maxPlayers = maxPlayers;

    this.state = 0;

    this.game;
}

Lobby.prototype.nClients = function() {
    var c = 0;
    for(i in this.clients) {
        c++;
    }
    return c;
}

Lobby.prototype.addClient = function(client) {
	this.clients[client.id+""] = client;
}

Lobby.prototype.broadcast = function(command, data){
	for(i in this.clients){
		this.clients[i].socket.emit(command, data);
	}
}

Lobby.prototype.toJSON = function() {
    var json = {};

    json['id'] = this.id;
    json['name'] = this.name;

    json['clients'] = {};
    for(var i in this.clients) {
        var isOwner = false;
        if(this.clients[i] === this.owner)
            isOwner = true;
        json['clients'][this.clients[i].id+""] = {id:this.clients[i].id, name:this.clients[i].name, isOwner:isOwner};
    }
    return json;
}

Lobby.prototype.chooseNewOwner = function() {
    var newOwner = -1;
    for(i in this.clients) {
        if(newOwner == -1 && this.clients[i] !== this.owner) {
            this.owner = this.clients[i];
            newOwner = this.owner.id;
        }
    }
    if(newOwner == -1) {
        delete Lobby._instances[this.id+""];
    }
    return newOwner;
}

Lobby.prototype.startGame = function() {
    this.game = new Game(this.clients, this);
    this.state = 1;
    return this.game;
}

Lobby.prototype.isTooLow = function(clientFrame) {
    var needWait = false;
    if(clientFrame < this.game.frame - 500) {
        needWait = true;
    }
    return needWait;
}

Lobby.ID_SEQ = Math.floor(9001*Math.random());
Lobby._instances = {};

Lobby.get = function(uid){
	return Lobby._instances[uid+""];
}

module.exports = Lobby;