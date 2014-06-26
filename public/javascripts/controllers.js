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

  $scope.timer = null                                     // 6
  $scope.number = null
  $scope.ticket = {                                       // 7
    price: 100,
    count: 100
  }
  $scope.ticketReq = function(){alert(JSON.stringify({kind: "TicketRequest"}))} // 8
  $scope.perks = 0    // 9
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
  $scope.timer = null                                         // 6
  $scope.ticket = {}                                        // 7
  $scope.ticketReq = function(){/*con.send({kind: "TicketRequest"}))*/}   // 8
  $scope.perks = 0    // 9

  var receive = function(msg) {
    switch(msg.kind) {
      case "UserData":
        $scope.perks = msg.data
        break;
      case "TicketIssue":
        $scope.ticketPacks.push(createPack(msg.data))
        break;
      case "TicketsLeft":
        $scope.ticket.count = msg.data
        break;
      case "GameStartInfo":
        $scope.gameDetails = msg.data.details
        $scope.ticket.price = msg.data.ticketPrice
        $scope.timer = {time: msg.data.timeLeft, message: "for game to start"}
        break;
      case "GameStart":
        // apart from other things
        $scope.ticket = {}
        $scope.messages.info = "Game On !!"
        break;
      case "GameEnd":
        $scope.ticketPacks = []
        $scope.gameDetails = {}
        $scope.leaderboard = {}
        $scope.message.info = "The game has finished"
        break;
      case "ErrorMessage":
        $scope.messages.error = msg.data
        break;
      case "TimeLeftForNewNumber":
        $scope.timer = { time: msg.data, message: "left for new number to be announced"}
        break;
      case "NewNumberPick":
        $scope.number = msg.data
        break;
      case "ClaimSuccess":
        $scope.messages.success = "Your claim was accepted"
        break;
      case "ClaimFailure":
        $scope.ticketPacks[msg.data.ticketId].cButtons[msg.data.claimType] = true
        break;
      case "PrizeDepleted":
        for (pack in $scope.ticketPacks)
          pack.cButtons[msg.data] = false
        $scope.messages.info = "The "+msg.data+" prize has been depleted"
        break;
      case "Leaderboard":
        $scope.leaderboard = msg.data
        $scope.messages.info = "The leaderboard has changed, check it out"
        break;
    }
  }
}
controllers.mainCtrl.$inject = ['$scope'];

return controllers;

});