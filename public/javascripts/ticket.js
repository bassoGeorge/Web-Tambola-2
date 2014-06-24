(function($) {

  var counter = 0

  $.fn.addNewTicket = function(ticket) {
    counter += 1

    var ticketId = counter
    init(this, ticketId)
    var tId = "#ticket" + ticketId
    var tbl = $(tId).find("table")[0]
    $.each(ticket, function(index, row) {
      var trow = tbl.insertRow(-1)
      $.each(row, function(index, data) {
        if (data == null)
          trow.insertCell(-1)
        else {
          cell = trow.insertCell(-1)
          cell.innerHTML = data.number
          $(cell).click(function(){$(this).toggleClass("success")})
          $(cell).hover(function(){$(this).addClass("active")}, function(){$(this).removeClass("active")})
        }
      })
    })
    $(tId + " button").click(function(){
      var claim = {
        kind: "Claim",
        data: {
          claimType: $(this).attr("name"),
          ticketId: ticketId,
          ticket: ticket
        }
      }
      //TODO: send the stuff and do the hiding
      //alert("Claiming : "+JSON.stringify(claim))
    })
  }

  function init(elem, ticketId) {
    elem.append("\
      <div id='ticket"+ticketId+"' class='panel panel-default'>\
        <div class='panel-heading'>\
          <h2 class='panel-title'>Ticket #"+ ticketId + "</h2>\
        </div>\
        <div class='panel-body'>\
          <div class='row'>\
            <div class='col-md-8'>\
              <table class='ticket table table-bordered'>\
              </table>\
            </div>\
            <div class='col-md-4'>\
              <div class='row no-gutter'>\
                <div class='col-sm-4'><button class='btn btn-default' name='Line1'>Line 1</button></div>\
                <div class='col-sm-4'><button class='btn btn-default' name='Line2'>Line 2</button></div>\
                <div class='col-sm-4'><button class='btn btn-default' name='Line3'>Line 3</button></div>\
              </div>\
              <div class='row no-gutter'>\
                <div class='col-sm-6'><button class='btn btn-warning' name='Corners'>Corners</button></div>\
                <div class='col-sm-6'><button class='btn btn-warning' name='BullsEye'>Bulls Eye</button></div>\
              </div>\
              <button class='btn btn-success' name='FullHouse'>Full House</button>\
            </div>\
          </div>\
        </div>\
      </div> ")
  }
})(jQuery)