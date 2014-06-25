/*global define */

'use strict';

define(function() {


var createPack = function(newTicket) {
  var btnPack = function() {
    var res = {}
    for (c in ["Line1", "Line2", "Line3", "Corners", "BullsEye", "FullHouse"])
      res[c] = true
    return res
  }
  return {
    ticket: newTicket,
    cButtons: btnPack()
  }
}

/* Controllers */
var controllers = {};
controllers.mainCtrl = function($scope) {
  /* ---- my code to go here ---- */

  /* ---------------- Samples ------------------------------------- */
  $scope.SampleTicketPacks = [
    {
      ticket: [[{number:10}, {number:30}, {number:20}, {}, {number:50}, {}, {}, {}, {number:90}],
            [{number:10}, {}, {number:20}, {}, {},{number:10}, {number:35}, {number:30}, {}],
            [{}, {number:35}, {}, {}, {number:32}, {number:60}, {}, {number:53}, {number:86}]],
      cButtons: {Line1: true, Line2: true, Line3: true, Corners: true, BullsEye: true, FullHouse: true}
    },
    {
      ticket: [[{number:10}, {number:30}, {number:20}, {}, {number:50}, {}, {}, {}, {number:90}],
            [{number:10}, {}, {number:20}, {}, {},{number:10}, {number:35}, {number:30}, {}],
            [{}, {number:35}, {}, {}, {number:32}, {number:60}, {}, {number:53}, {number:86}]],
      cButtons: {Line1: true, Line2: true, Line3: true, Corners: true, BullsEye: true, FullHouse: true}
    }
  ]

  $scope.sampleLeaderboard = [
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

  $scope.sampleGameDetails = {
    "Line 1": { prize: 100, count: 3 },
    "Line 2": { prize: 100, count: 3 },
    "Line 3": { prize: 100, count: 3 },
    "Corners": { prize: 300, count: 2},
    "Bull's Eye": { prize: 300, count: 2},
    "Full house": { prize: 500, count: 1}
  }

  /* -------------- Actuals --------------- */
  $scope.con = {}     // Connection item
  $scope.ticketPacks = []   // Array of ticketPacks
  $scope.leaderboard = []   // Leaderboard array
  $scope.submitFn = function(id, claimType, ticket) { alert(JSON.stringify({    // TODO: replace by con.send()
    kind: "Claim",
    data: {
      claimType: claimType,
      ticketId: id,
      ticket: ticket
    }
  }))}

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