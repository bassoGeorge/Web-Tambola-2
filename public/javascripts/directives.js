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
      template: '<div class="alert alert-dismissable"><button type="button" class="close" aria-hidden="true">&times</button>{{ msg }}</div>',
      link: function(scope, elem, attr) {
        elem.addClass(attr.class)
        elem.children("button").click(function(){
          scope.$apply(function(){scope.msg = ""})
        })

        var prev = null
        scope.$watch(function(){return scope.msg}, function(v){
          if(v != "") {
            elem.slideDown()
            $timeout.cancel(prev)
            prev = $timeout(function(){scope.msg = ""}, parseInt(attr.delay))
          } else elem.slideUp()
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
            start: function(){scope.$apply(function(){scope.timer = null})},
            stop: function(){elem.hide()}
          }
        })

        scope.$watch(function(){return scope.timer}, function(value){
          if(value != null) {
            elem.show()
            clock.setTime(value.time)
            clock.start()
          }
        })
      }
    }
  }

  directives.numberPop = function() {
    return {
      restrict: 'AE',
      replace: true,
      scope: {number: '='},
      template: '<div class="numberPop bg-info">{{ number }}</div>',
      link: function(scope, elem, attr) {
        var tmout = null
        scope.$watch(function(){return scope.number}, function(value){
          if(value != null) {
            elem.show()
            //$timeout(function(){scope.number = null}, parseInt(attr.delay))
            clearTimeout(tmout)
            tmout = setTimeout(function(){elem.hide()}, parseInt(attr.delay))
          }
        })
      }
    }
  }

  return directives
});