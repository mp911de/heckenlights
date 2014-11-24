/**
 * Created by mark on 04.12.13.
 */


var heckenlights = (function () {

        var instance = {};
        var visiblePlaylistEntries = 5;
        var loadPlaylistInterval;
        var mockData = true;
        var siteIsOpen = false;

        var config = {
            'fileupload': 'api/v1/playlist/queue',
            'authentication': 'api/v1/authentication',
            'playlist': 'api/v1/playlist'
        }


        instance.initialize = function () {


            if (mockData) {
                config.playlist = 'js/demo-playlist.json';
                config.fileupload = 'js/demo-upload.json';
            }

            $('#reload').click(function () {
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

            loadPlaylistInterval = window.setInterval(function () {
                loadPlaylist()
            }, 5000);


            $('#fileupload').fileupload({
                url: config.fileupload,
                dataType: 'json',
                done: function (e, data) {
                    if (data.jqXHR.responseJSON) {
                        var enqueued = data.jqXHR.responseJSON;
                        var key = "enqueue_success_plural";
                        if(enqueued.durationToPlay && enqueued.durationToPlay == 1){
                            key = "enqueue_success"
                        }

                        $('#uploadsuccesstext').html(i18n.t(key, {track: enqueued.trackName, count: enqueued.durationToPlay}));
                        var tweet = i18n.t("enqueue_tweet", {track: enqueued.trackName, count: enqueued.durationToPlay});
                        $("#sharetwitter").attr('href', 'http://twitter.com/home?status=' + escape(tweet));
                        $('#uploadsuccess').fadeIn();
                        setTimeout(function () {
                            $("#uploadsuccess").fadeOut()
                        }, 30000);
                    }
                },
                fail: function (e, data) {

                    $('#progress .progress-bar').removeClass('progress-bar-success');
                    $('#progress .progress-bar').addClass('progress-bar-warning');

                    var message = null;
                    if (data.jqXHR.status == 401 && data.jqXHR.responseJSON) {
                        var enqueued = data.jqXHR.responseJSON;
                        if (enqueued.submitStatus == 'UNAUTHENTICATED') {
                            message = i18n.t("enqueue_unauthenticated");
                            checkOrCreateRecaptcha();
                        }
                    }

                    if (data.jqXHR.status == 423 && data.jqXHR.responseJSON) {
                        var enqueued = data.jqXHR.responseJSON;
                        if (enqueued.submitStatus == 'OFFLINE') {
                            message = i18n.t("enqueue_offline");
                        }
                    }

                    if (data.jqXHR.status == 429 && data.jqXHR.responseJSON) {
                        var enqueued = data.jqXHR.responseJSON;
                        if (enqueued.submitStatus == 'QUOTA') {
                            message = i18n.t("enqueue_queue_error");
                        }
                    }

                    if (message == null && data.jqXHR.status >= 400 && data.jqXHR.status <= 499) {
                        message = i18n.t("enqueue_bad_request");
                    }

                    if (message == null && data.jqXHR.status >= 500 && data.jqXHR.status <= 599) {
                        message = i18n.t("enqueue_internal_server_error");
                    }

                    $('#uploadwarning').html(message);
                    $('#uploadprogress').hide();


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

            if (mockData) {
                config.authentication = 'js/demo-authentication.json';
            }

            var uri = config.authentication;


            $.ajax({
                    dataType: 'json',
                    accepts: {
                        json: 'application/json'
                    },
                    url: uri

                }
            ).done(function (data) {

                    if (!data || !data.humanOrMachine || data.humanOrMachine == 'machine') {
                        //$('#uploadcontainer').hide();
                        Recaptcha.create(data.recaptchaPublicKey,
                            "recaptcha_div",
                            {
                                theme: "white",
                                callback: Recaptcha.focus_response_field
                            }
                        );

                        $('#uploadgroup').hide();
                        $("#submitcaptcha").click(function () {
                            submitCaptcha();
                        });
                    }

                    if (data.humanOrMachine && data.humanOrMachine == 'human') {
                        $('#captchacontainer').hide();
                        $('#uploadgroup').show();
                    }
                }).fail(function (data) {
                    $('#uploadgroup').hide();
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
                        $('#captchawarning').html(i18n.t("captcha_validation_error"));

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
                    $('#captchawarning').html(i18n.t("internal_server_error"));
                    $('#captchawarning').fadeIn();

                    setTimeout(function () {
                        $("#captchawarning").fadeOut();
                    }, 5000);
                });
        }


        function siteClosed() {
            siteIsOpen = false;
            $("#playlist").empty();
            $("#closed").show();
            $("#closedsign").show();
            $("#captchacontainer").hide();
            $("#uploadgroup").hide();
        }

        function queueClosed() {
            $("#captchacontainer").hide();
            $("#uploadgroup").hide();
        }

        function siteOpen() {
            siteIsOpen = true;
            $("#closed").hide();
            $("#closedsign").hide();
            checkOrCreateRecaptcha();
        }

        function loadPlaylist() {
            var uri = config.playlist;
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

                    if (!data) {
                        return;
                    }

                    if (data.online) {
                        if (!siteIsOpen) {
                            siteOpen();
                        }
                    }
                    else {
                        if (siteIsOpen) {
                            siteClosed();
                        }
                    }

                    if (!data.queueOpen) {
                        queueClosed();
                    }

                    if (data.entries && data.entries.length) {

                        var entries = data.entries;
                        for (var index in entries) {

                            var playCommand = entries[index];
                            var trackName = "No name";
                            if (playCommand.trackName && playCommand.trackName != '') {
                                trackName = playCommand.trackName;
                            }

                            if (playCommand.playing) {

                                var html = "<a href=\"#\" class=\"list-group-item active\">" +
                                    "<h4 class=\"list-group-item-heading\"><span class=\"glyphicon glyphicon-play\"></span> " + trackName + "</h4>" +
                                    "<p class=\"list-group-item-text\">" + i18n.t("remaining_seconds", {count: playCommand.remaining}) + "</p>" +
                                    "</a>"

                                $("#playlist").append(html);
                                first = false;
                            } else {
                                var html = "<a href=\"#\" class=\"list-group-item\">" +
                                    "<h4 class=\"list-group-item-heading\">" + trackName + "</h4>" +
                                    "<p class=\"list-group-item-text\">" + i18n.t("duration_seconds", {count: playCommand.duration}) + "</p></a>"

                                $("#playlist").append(html);
                            }

                            if (index >= visiblePlaylistEntries) {
                                var html = "<a href=\"#\" class=\"list-group-item\">" +
                                    "<h4 class=\"list-group-item-heading\">" + i18n.t("and_more", {count: (entries.length - index + 1)}) + "</h4>" +
                                    "</a>"

                                $("#playlist").append(html);
                                break;
                            }
                        }
                    }
                    else {
                        var html = "<a href=\"#\" class=\"list-group-item\">" +
                            "<h4 class=\"list-group-item-heading\">" + i18n.t("index_playlist_empty") + "</h4>" +
                            "<p class=\"list-group-item-text\">-</p></a>"

                        $("#playlist").append(html);
                    }
                }
            ).
                fail(function () {
                    siteClosed();
                });
        }

        return instance;
    })
    ;
