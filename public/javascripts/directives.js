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
      template: "<button class='btn btn-block'>test</button>",
      link: function(scope, elem, attr) {
        elem.addClass(attr.class)
        elem.click(function(){
          scope.submitFn(scope.$index, attr.name, scope.ticketPack.ticket)
          scope.$apply('ticketPack.cButtons.'+attr.name+' = false')
        })
        elem.text(attr.text)
        scope.$watch('ticketPack.cButtons.'+attr.name, function(value){
          value ? elem.removeAttr("disabled") : elem.attr("disabled", "disabled");
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

  directives.gameTimer = function() {
    return {
      restrict: 'AE',
      replace: true,
      scope: {
        timer: '='
      },
      template: '<div class="stopwatch"><div></div><div class="message">{{timer.message}}</div></div>',
      link: function(scope, elem, attr) {
        var clock = new FlipClock(elem.children("div")[0], {
          countdown: true,
          autoStart: false,
          clockFace: "MinuteCounter",
          callbacks: {
            stop: function(){scope.$apply(function(){scope.timer = null})}
          }
        })

        scope.$watch(function(){return scope.timer}, function(value){
          if(value != null) {
            elem.show()
            clock.setTime(value.time)
            clock.start()
          } else elem.hide()
        })
      }
    }
  }

  directives.numberPop = function($timeout) {
    return {
      restrict: 'AE',
      replace: true,
      scope: {number: '='},
      template: '<div class="numberPop">{{ number }}</div>',
      link: function(scope, elem, attr) {
        scope.$watch(function(){return scope.number}, function(value){
          if(value != null) {
            elem.show()
            $timeout(function(){scope.number = null}, parseInt(attr.delay))
          } else elem.hide()
        })
      }
    }
  }

  return directives
});