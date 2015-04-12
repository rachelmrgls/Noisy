function Student( ) {
    this.firstname = "";
    this.lastname = "";
    this.netid = "";
    this.pledges = [];
};

Club.prototype.setName = function( newName ) {
	this.name = newName;
};
