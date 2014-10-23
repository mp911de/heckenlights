/**
 * Created by mark on 04.12.13.
 */


var heckenlights = (function () {

    var instance = {};
    var visiblePlaylistEntries = 5;


    instance.initialize = function () {

        $("#reload").click(function () {
            loadPlaylist()
        });

        loadPlaylist();
        checkOrCreateRecaptcha();


        $('#uploadwarning').click(function () {
            $("#uploadwarning").fadeOut();
        });

        $('#captchawarning').click(function () {
            $("#captchawarning").fadeOut();
        });

        $('#uploadsuccess').click(function () {
            $("#uploadsuccess").fadeOut();
        });

        window.setInterval(function () {
            loadPlaylist()
        }, 5000);


        $('#fileupload').fileupload({
            url: "api/v1/playlist/queue",
            dataType: 'json',
            done: function (e, data) {
                if (data.jqXHR.responseJSON) {
                    var enqueued = data.jqXHR.responseJSON;

                    $('#uploadsuccess').html("<strong>Enqeued!</strong> It will take some " + enqueued.durationToPlay + " seconds until the file is played.");
                    $('#uploadsuccess').fadeIn();
                    setTimeout(function () {
                        $("#uploadsuccess").fadeOut()
                    }, 5000);
                }
            },
            fail: function (e, data) {
                $('#progress .progress-bar').removeClass('progress-bar-success');
                $('#progress .progress-bar').addClass('progress-bar-warning');

                var message = null;
                if (data.jqXHR.status == 401 && data.jqXHR.responseJSON) {
                    var enqueued = data.jqXHR.responseJSON;
                    if (enqueued.submitStatus == 'UNAUTHENTICATED') {
                        message = "<strong>Unauthenticated</strong> Please use the captcha to confirm that you are a hooooman.";
                        checkOrCreateRecaptcha();
                    }
                }

                if (data.jqXHR.status == 429 && data.jqXHR.responseJSON) {
                    var enqueued = data.jqXHR.responseJSON;
                    if (enqueued.submitStatus == 'QUOTA') {
                        message = "<strong>Too many requests</strong> Please retry later.";
                    }
                }


                if (message == null && data.jqXHR.status >= 400 && data.jqXHR.status <= 499) {
                    message = "<strong>Aw, snap!</strong> Could not submit your file to the queue. Check if whether your file is a GM Midi-File.";
                }

                if (message == null && data.jqXHR.status >= 500 && data.jqXHR.status <= 599) {
                    message = "<strong>Aw, snap!</strong> There was an internal server error.";
                }

                $('#uploadwarning').html(message);


                $('#uploadwarning').fadeIn();
                setTimeout(function () {
                    $("#uploadwarning").fadeOut();
                }, 5000);
            },
            start: function () {
                $('#progress .progress-bar').addClass('progress-bar-success');
                $('#progress .progress-bar').removeClass('progress-bar-warning');
                $('#uploadwarning').hide();
                $('#uploadsuccess').hide();
                $('#uploadprogress').show();
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

    function checkOrCreateRecaptcha() {
        var uri = "api/v1/authentication";
        $.ajax({
                dataType: 'json',
                accepts: {
                    json: 'application/json'
                },
                url: uri

            }
        ).done(function (data) {

                if (!data.humanOrMachine || data.humanOrMachine == 'machine') {
                    //$('#uploadcontainer').hide();
                    Recaptcha.create(data.recaptchaPublicKey,
                        "recaptcha_div",
                        {
                            theme: "white",
                            callback: Recaptcha.focus_response_field
                        }
                    );

                    $("#submitcaptcha").click(function () {
                        submitCaptcha();
                    });
                }

                if (data.humanOrMachine && data.humanOrMachine == 'human') {
                    $('#captchacontainer').hide();
                    $('#uploadcontainer').show();
                }
            });
    }

    function submitCaptcha() {

        var captcha = $("#recaptcha_response_field").val();
        var challenge = $("#recaptcha_challenge_field").val();
        var uri = "api/v1/authentication";
        $.ajax({
            dataType: 'json',
            accepts: {
                json: 'application/json'
            },
            url: uri,
            type: 'POST',
            data: {
                recaptcha_response_field: captcha,
                recaptcha_challenge_field: challenge
            }
        }).done(
            function (data) {
                if (!data.humanOrMachine || data.humanOrMachine == 'machine') {
                    $('#captchawarning').html("Validation not successful. Please enter the displayed words/numbers. (" + data.challengeResponse + ")");

                    $('#captchawarning').fadeIn();
                    setTimeout(function () {
                        $("#captchawarning").fadeOut();
                    }, 5000);

                    checkOrCreateRecaptcha();
                    return;
                }


                if (data.humanOrMachine && data.humanOrMachine == 'human') {
                    $('#captchacontainer').hide();
                    $('#uploadcontainer').show();
                    return;
                }
            }
        ).fail(function () {
                $('#captchawarning').html("There was an internal server error, could not validate your request.");

                $('#captchawarning').fadeIn();
                setTimeout(function () {
                    $("#captchawarning").fadeOut();
                }, 5000);
            });
    }


    function loadPlaylist() {
        var uri = "api/v1/playlist";
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
                                "<h4 class=\"list-group-item-heading\"><span class=\"glyphicon glyphicon-play\"></span> " + trackName + "</h4>" +
                                "<p class=\"list-group-item-text\">Remaining: " + playCommand.remaining + " Seconds</p>" +
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
