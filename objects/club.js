function Club () {
    this.name = "";
    this.campaigns = [];
    this.netIds = [];
};

Club.prototype.setName = function( newName ) {
	this.name = newName;
};
