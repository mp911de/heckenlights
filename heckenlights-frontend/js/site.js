/**
 * Created by mark on 04.12.13.
 */


var heckenlights = (function () {

    var instance = {};
    var visiblePlaylistEntries = 5;


    instance.initialize = function () {

        $("#reload").click(function () {
            preparePlaylistLoad()
        });

        preparePlaylistLoad();

        $('#warning').click(function () {
            $("#warning").fadeOut();
        });

        $('#success').click(function () {
            $("#success").fadeOut();
        });


        $('#fileupload').fileupload({
            url: "app/submit.php",
            dataType: 'json',
            done: function (e, data) {
                if (data.jqXHR.responseJSON && data.jqXHR.responseJSON.enqueued) {
                    var enqueued = data.jqXHR.responseJSON.enqueued;

                    $('#success').html("<strong>Enqeued!</strong> It will take some " + enqueued.durationToPlay + " seconds until the file is played.");
                    $('#success').fadeIn();
                    setTimeout(function () {
                        $("#success").fadeOut()
                    }, 5000);
                }
            },
            fail: function (e, data) {
                if (data.jqXHR.responseJSON && data.jqXHR.responseJSON.enqueued) {
                    var enqueued = data.jqXHR.responseJSON.enqueued;
                    $('#progress .progress-bar').removeClass('progress-bar-success');
                    $('#progress .progress-bar').addClass('progress-bar-warning');
                    $('#warning').html("<strong>Aw, snap!</strong> Could not submit your file to the queue. Check if whether your file is a GM Midi-File.");
                    $('#warning').fadeIn();
                    setTimeout(function () {
                        $("#warning").fadeOut();
                    }, 5000);
                }
            },
            start: function () {
                $('#progress .progress-bar').addClass('progress-bar-success');
                $('#progress .progress-bar').removeClass('progress-bar-warning');
                $('#warning').hide();
                $('#success').hide();
            },
            progressall: function (e, data) {
                var progress = parseInt(data.loaded / data.total * 100, 10);
                $('#progress .progress-bar').css(
                    'width',
                    progress + '%'
                );
            }
        }).prop('disabled', !$.support.fileInput)
            .parent().addClass($.support.fileInput ? undefined : 'disabled');

    }


    function preparePlaylistLoad() {
        var uri = "app/playlist.php";
        $.ajax({
                dataType: 'json',
                accepts: {
                    json: 'application/json'
                },
                url: uri

            }
        ).done(function (data) {


                var first = true;

                $("#playlist").empty();


                if (data && data.length) {
                    for (var index in data) {

                        var playCommand = data[index];
                        var trackName = "No name";
                        if (playCommand.trackName && playCommand.trackName != '') {
                            trackName = playCommand.trackName;
                        }

                        if (playCommand.playing) {

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

                        if (index >= visiblePlaylistEntries) {
                            var html = "<a href=\"#\" class=\"list-group-item\">" +
                                "<h4 class=\"list-group-item-heading\">and " + (data.length - index + 1) + " more...</h4>" +
                                "</a>"

                            $("#playlist").append(html);
                            break;
                        }
                    }
                }
                else {
                    var html = "<a href=\"#\" class=\"list-group-item\">" +
                        "<h4 class=\"list-group-item-heading\">Empty Playlist</h4>" +
                        "<p class=\"list-group-item-text\">-</p></a>"

                    $("#playlist").append(html);
                }
            })
    }

    return instance;
});
