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
  directives.alertPop = function($timeout) {
    return {
      restrict: 'E',
      replace: true,
      scope: {
        msg: '='
      },
      template: '<div class="alert alert-dismissable"><button type="button" class="close" data-dismiss="alert" aria-hidden="true">&times</button>{{ msg }}</div>',
      link: function(scope, elem, attr) {
        elem.addClass(attr.class)
        var prev = null
        scope.$watch(function(){return scope.msg}, function(v){
          if(v) {
            elem.show()
            $timeout.cancel(prev)
            prev = $timeout(function(){scope.msg = ""}, parseInt(attr.delay))
          } else elem.hide()
        })
      }
    }
  }
  return directives
});