/*global define */

'use strict';

define(['jquery'], function($) {

/* Controllers */

var controllers = {};


controllers.mainCtrl = function($scope) {
  /* ---- my code to go here ---- */
  $scope.myName = "Anish George";
  $scope.ticketPacks = [
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

  //$scope.cButtons = {Line1: true, Line2: true, Line3: true, Corners: true, BullsEye: true, FullHouse: true}

  $scope.submitFn = function(id, claimType, ticket) { alert(JSON.stringify({
    kind: "Claim",
    data: {
      claimType: claimType,
      ticketId: id,
      ticket: ticket
    }
  }))}
  /*
  $scope.ticket = [[{number:10}, {number:30}, {number:20}, {}, {number:50}, {}, {}, {}, {number:90}],
  [{number:10}, {}, {number:20}, {}, {},{number:10}, {number:35}, {number:30}, {}],
  [{}, {number:35}, {}, {}, {number:32}, {number:60}, {}, {number:53}, {number:86}]]
  */
}
controllers.mainCtrl.$inject = ['$scope'];

return controllers;

});