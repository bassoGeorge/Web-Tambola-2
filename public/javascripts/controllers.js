/*global define */

'use strict';

define(function() {


/* ---------------- Auxiliary functions ------------- */
var createPack = function(newTicket) {
  var btnPack2 = function() {
    return {
      Line1: true,
      Line2: true,
      Line3: true,
      Corners: true,
      BullsEye: true,
      FullHouse: true
    }
  }
  return {
    ticket: newTicket,
    cButtons: btnPack2()
  }
}

function Claim(id, claimType, ticket) {
  this.kind = "Claim"
  this.data = {
    claimType: claimType,
    ticketId: id,
    ticket: ticket
  }
}

/* Controllers */
var controllers = {};
controllers.testCtrl = function($scope) {

  var sTicket = [[{number:10}, {number:30}, {number:20}, {}, {number:50}, {}, {}, {}, {number:90}],
            [{number:10}, {}, {number:20}, {}, {},{number:10}, {number:35}, {number:30}, {}],
            [{}, {number:35}, {}, {}, {number:32}, {number:60}, {}, {number:53}, {number:86}]]

  $scope.ticketPacks = [createPack(sTicket), createPack(sTicket)]   // 1
  $scope.leaderboard = [                                            // 2
    {
      prize: "Full house",
      username: "Anish George",
      winning: 3000
    },
    {
      prize: "Bull's eye",
      username: "Jibin",
      winning: 1000
    }
  ]

  $scope.gameDetails = {                                             // 3
    "Line 1": { prize: 100, count: 3 },
    "Line 2": { prize: 100, count: 3 },
    "Line 3": { prize: 100, count: 3 },
    "Corners": { prize: 300, count: 2},
    "Bull's Eye": { prize: 300, count: 2},
    "Full house": { prize: 500, count: 1}
  }

  $scope.submitFn = function(id, claimType, ticket) {alert(JSON.stringify(new Claim(id, claimType, ticket)))}   // 4

  $scope.messages = {                                   // 5
    success: "You have it right !!",
    error: "You got it wrong man",
    info: "Hey, look at this"
  }
}
controllers.testCtrl.$inject = ['$scope']


controllers.mainCtrl = function($scope) {
  /* ---- my code to go here ---- */
  /* -------------- Actuals --------------- */
  $scope.con = {}     // Connection item
  $scope.ticketPacks = []   // Array of ticketPacks         // 1
  $scope.leaderboard = []   // Leaderboard array            // 2
  $scope.gameDetails = {}                                   // 3
  $scope.submitFn = function(id, claimType, ticket) {con.send(new Claim(id, claimType, ticket))}    // 4
  $scope.message = {                                        // 5
    success: "",
    info: "",
    error: ""
  }

  var receive = function(msg) {
    switch(msg.kind) {
      case "TicketIssue":
        ticketPacks.push(createPack(msg.data))
        break;
      case "GameStartInfo": break;
      case "GameStart": break;
      case "GameEnd": break;
      case "ErrorMessage": break;
      case "TimeLeftForNewNumber": break;
      case "NewNumberPick": break;
      case "ClaimSuccess": break;
      case "ClaimFailure": break;
      case "PrizeDepleted":
        for (pack in $scope.ticketPacks)
          pack.cButtons[msg.data] = false
        break;
      case "Leaderboard":
        $scope.leaderboard = msg.data
        break;
    }
  }
}
controllers.mainCtrl.$inject = ['$scope'];

return controllers;

});