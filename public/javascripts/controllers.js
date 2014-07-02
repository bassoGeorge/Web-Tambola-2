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
      BullsEye: true,
      Corners: true,
      Star: true,
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

  //getGameStatus(function(d){alert("Game status : "+JSON.stringify(d))})
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

  $scope.submitFn = function(id, claimType, ticket) {$scope.rec = JSON.stringify(new Claim(id, claimType, ticket))}   // 4

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
  $scope.perks = {
    orig: 100,
    cur: 500
  }    // 9
  $scope.gameState = 'Running'
  $scope.makeMessage = function() {
    if ($scope.perks.cur == $scope.perks.orig) return "Your perks have remained unchanged"
    else if ($scope.perks.cur > $scope.perks.orig) return "You have earned "+($scope.perks.cur - $scope.perks.orig)+" perks this game"
    else return "You have lost "+($scope.perks.orig - $scope.perks.cur)+" perks this game"
  }
  $scope.displayFinM = function() {$("#finishModal").modal('show')}
}
controllers.testCtrl.$inject = ['$scope']

controllers.mainCtrl = function($scope) {

  $scope.gameState = null         // String: Waiting, Running, Stopped
  $scope.con = {isConnected: false}      // {isConnected: boolean, send: function(JSON), close: function(), error: String}

  $scope.ticketPacks = []   // Array of ticketPacks         // 1
  $scope.leaderboard = []   // Leaderboard array            // 2
  $scope.gameDetails = {}                                   // 3
  $scope.submitFn = function(id, claimType, ticket) {
    if($scope.con.isConnected) $scope.con.send(new Claim(id, claimType, ticket))
  }    // 4

  $scope.messages = {                                        // 5
    success: "",
    info: "",
    error: ""
  }
  $scope.timer = null                                         // 6
  $scope.ticket = {}                                        // 7
  $scope.ticketReq = function(){
    if($scope.con.isConnected) $scope.con.send({kind: "TicketRequest"})
  }
  $scope.perks = {
    orig: 0,
    cur: 0
  }    // 9
  $scope.username = null
  /* ---------- Game setup ------------ */
  $scope.check = function () {
    getGameStatus(function(d){
      $scope.$apply(
        function(){$scope.gameState = d.status}
      )
  })}

  $scope.check()
  $scope.number = null

    // TODO: test the $scope.$apply usage thuroughly
  var receive = function(msg) { $scope.$apply(function(){
    switch(msg.kind) {
      case "UserData":      // Working
        $scope.perks.cur = msg.data
        break;
      case "TicketIssue":     // Working
        $scope.ticketPacks.push(createPack(msg.data))
        break;
      case "TicketsLeft":     // Working
        $scope.ticket.count = msg.data
        break;
      case "GameStartInfo":   // Working
        $scope.gameDetails = msg.data.details
        $scope.ticket.price = msg.data.ticketPrice
        $scope.timer = {time: msg.data.timeLeft, message: "for game to start"}
        break;
      case "GameStart":     // Working
        // apart from other things
        $scope.perks.orig = $scope.perks.cur
        $scope.leaderboard = []     // Either here .. 1
        $scope.ticket = {}
        $scope.gameState = 'Running'
        $scope.messages.info = "Game On !!"
        break;
      case "GameEnd":
        $scope.ticketPacks = []
        setTimeout(function(){$scope.leaderboard = []}, 4000)   // Or here .. 2
        $scope.gameState = 'Waiting'
        $("#finishModal").modal('show')
        $scope.messages.info = "The game has finished, check your score"
        alert('Game finished message received')
        break;
      case "ErrorMessage":    // working
        $scope.messages.error = msg.data
        break;
      case "TimeLeftForNewNumber":    // Working
        $scope.timer = { time: msg.data, message: "left for new number to be announced"}
        break;
      case "NewNumberPick":       // Working
        $scope.number = msg.data
        break;
      case "ClaimSuccess":
        $scope.messages.success = "Your claim was accepted"
        break;
      case "ClaimFailure":      // Working
        $scope.messages.error = "Your claim was rejected"
        $scope.ticketPacks[msg.data.ticketId].cButtons[msg.data.claimType] = true
        break;
      case "PrizeDepleted":
        for (var i = 0; i < $scope.ticketPacks.length; i++)
          $scope.ticketPacks[i].cButtons[msg.data] = false
        $scope.messages.info = "The "+msg.data+" prize has been depleted"
        break;
      case "Leaderboard":   // Working
        $scope.leaderboard = msg.data
        $scope.messages.info = "The leaderboard has changed, check it out"
        break;
    }
  })}

  var onclose = function() {$scope.$apply(function(){
    alert("Thankyou for playing tambola")
    $scope.con = {isConnected: false}
    setTimeout(function(){$scope.check()}, 2000)
  })}

  var onerror = function(d) {
    alert("Some error occured : "+JSON.stringify(d))
  }

  $scope.establishConnection = function(user) {
    connect(user, {
      onMessage : receive,
      onError : onerror,
      onClose : onclose
    }, function(value){$scope.$apply(function(){$scope.con = value})})
  }

  $scope.makeMessage = function() {
    if ($scope.perks.cur == $scope.perks.orig) return "Your perks have remained unchanged"
    else if ($scope.perks.cur > $scope.perks.orig) return "You have earned "+($scope.perks.cur - $scope.perks.orig)+" perks this game"
    else return "You have lost "+($scope.perks.orig - $scope.perks.cur)+" perks this game"
  }
}
controllers.mainCtrl.$inject = ['$scope'];

return controllers;

});