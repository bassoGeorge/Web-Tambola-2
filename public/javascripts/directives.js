/*global define */

'use strict';

define([], function() {

/* Directives */
  var directives = {}

  // DONE!!!
  directives.claimButton = function() {
    return {
      restrict: 'E',
      replace: true,
      template: "<button class='btn'>test</button>",
      link: function(scope, elem, attr) {
        elem.addClass(attr.class)
        elem.click(function(){
          scope.submitFn(scope.$index, attr.name, scope.ticketPack.ticket)
          scope.$apply('ticketPack.cButtons.'+attr.name+' = false')
        })
        elem.text(attr.text)
        scope.$watch('ticketPack.cButtons.'+attr.name, function(value){
          value ? elem.show() : elem.hide();
        })
      }
    }
  }
  return directives
});