/**
 * Created by mark on 04.12.13.
 */
var host = "localhost:8080/heckenlights/";
function preparePlaylistLoad() {
    var uri = "http://" + host + "?playStatus=ENQUEUED";
    $.ajax({
               dataType: 'json',
               accepts: {
                   json: 'application/json'
               },
               url: uri

           }
    ).done(function (data) {

               var playCommands = data.playCommands.playCommand;
               var first = true;

               $("#playlist").empty();
               for (var playCommandId in playCommands) {

                   var playCommand = playCommands[playCommandId];
                   var trackName = "No name";
                   if (playCommand.trackName && playCommand.trackName != '') {
                       trackName = playCommand.trackName;
                   }

                   if (first) {

                       var html = "<a href=\"#\" class=\"list-group-item active\">" +
                                      "<h4 class=\"list-group-item-heading\">" + trackName + "</h4>" +
                                      "<p class=\"list-group-item-text\">Duration: " + playCommand.duration + " Seconds</p>" +
                                      "<p class=\"list-group-item-text\">Remaining: " + playCommand.duration + " Seconds</p>" +
                                      "</a>"

                       $("#playlist").append(html);
                       first = false;
                   } else {
                       var html = "<a href=\"#\" class=\"list-group-item\">" +
                                      "<h4 class=\"list-group-item-heading\">" + trackName + "</h4>" +
                                      "<p class=\"list-group-item-text\">Duration: " + playCommand.duration + " Seconds</p></a>"

                       $("#playlist").append(html);
                   }
               }
           })
}