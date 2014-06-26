@(implicit r: RequestHeader)

/*
 * params:
 *  username: The username to connect
 *  fnConf: object with onMessage, onClose, onError functions which take direct json data
 *
 * return:
 *  if successful, the callBack() fn will be called with an object which contains the send method
 *  else the callBack() will be called with an object which contains the error field
 */


/**
 * Tested and debugged: 22-06-2014, 11:17PM.
 */
function connect(username, fnConf, callBack) {
  function confirmCon(t) {
    var WS = window['MozWebSocket'] ? MozWebSocket : WebSocket
    var socket = new WS("@routes.Application.confirmJoin.webSocketURL()")
    socket.onopen = function() {
      socket.send(JSON.stringify({token: t}))
    }

    var res = ""
    socket.onmessage = function(event) {
      var data = JSON.parse(event.data)
      if (data.connectionStatus) {
        if(fnConf.onMessage)
          socket.onmessage = function(event) {fnConf.onMessage(JSON.parse(event.data))}
        callBack({
          send: function(obj){socket.send(JSON.stringify(obj))}
        })
      } else callBack({error: "Couldn't get connectionStatus as true, socket handshake discontinued at last step"})
    }
    if(fnConf.onClose) {
      res += "found onClose(), "
      socket.onclose = fnConf.onClose
    }
    if(fnConf.onError) {
      res += "found onError() "
      socket.onerror = fnConf.onError
    }

  }

  if (username != "") {
    $.getJSON("@routes.Application.joinGame(None)", {user: username},
      function(data, status, xhr) {
        if(data.token) confirmCon(data.token)
        else callBack({ error: data })
      }
    )
  }
}

function getGameStatus(fn) {
  $.getJSON("@routes.Application.checkStatus()", fn)
}
