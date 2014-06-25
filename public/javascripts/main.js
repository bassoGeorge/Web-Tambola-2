/*global require, requirejs */

'use strict';

requirejs.config({
  paths: {
    'angular': ['../lib/angularjs/angular'],
  },
  shim: {
    'angular': {
      exports : 'angular'
    },
  }
});

require(['angular', './controllers', './directives', 'jquery'],
  function(angular, controllers, directives) {

    // Declare app level module which depends on filters, and services

    angular.module('tambolaApp', [])
    .controller('mainCtrl', controllers.mainCtrl)    // TODO: I think i need to add on the controllers here
    .controller('testCtrl', controllers.testCtrl)
    .directive('claimButton', directives.claimButton)
    .directive('alertPop', ['$timeout', directives.alertPop])
    .directive('gameTimer', directives.gameTimer)
    .directive('numberPop', ['$timeout', directives.numberPop])
    angular.bootstrap(document, ['tambolaApp']);

});