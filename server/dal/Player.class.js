function Player(id, name, color, isMultiplayer) {
    this.id = id;
    this.name = name;
    this.mothership;
    this.color = color;
    if(isMultiplayer) {
        if(color == 0)
            this.mothership = [-250, 0];
        else
            this.mothership = [250, 0];
    } else {
        this.mothership = [0, 0];
    }
    this.units = {};
    this.buildings = {};
}

Player.prototype.toJSON = function() {
    var json = {};

    json['id'] = this.id;
    json['name'] = this.name;
    json['color'] = this.color;
    json['mothership'] = {x:this.mothership[0], y:this.mothership[1]};

    json['units'] = {};
    for(var i in this.units) {
        json['units'][this.units[i].id+""] = {id:this.units[i].id, position:this.units[i].position};
    }

    json['buildings'] = {};
    for(var b in this.buildings) {
        json['buildings'][this.buildings[b].id+""] = {id:this.buildings[b].id, position:this.buildings[b].position};
    }
    return json;
}

module.exports = Player;