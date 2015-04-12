var allCampaigns;

function Campaign( clubName ) {
    this.name = "";
    this.club = clubName;
    this.goalAmnt = 0;
    this.currentAmnt = 0;
    this.startAmnt = 0;
    this.startdate = "";
    this.enddate = "";
    this.picture = "";
    this.description = "";
    this.pledges = [];
    //if (allcampaigns == undefined) {
      //  var allcampaigns = [];
    //}
    //allcampaigns.append(this);
};

Campaign.prototype.sumPledges = function( ) {
	var sum = 0;
    for (var i = 0; i < pledges.length; i++) {
        sum = sum + pledges[i].amount;
    }
    return sum;
};

Campaign.prototype.postToPostGres = function () {

};


