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
    .directive('claimButton', directives.claimButton)
    angular.bootstrap(document, ['tambolaApp']);

});