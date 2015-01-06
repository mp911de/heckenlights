/**
 * Created by mark on 04.12.13.
 */


var heckenlights = (function () {

        var instance = {};
        var visiblePlaylistEntries = 5;
        var loadPlaylistInterval;
        var countdownInterval;
        var loadPresetInterval;
        var mockData = false;
        var siteIsOpen = false;
        var queueIsOpen = false;
        var fadeoutAfter = 15000;
        var playlistRefresh = 5000;
        var captchaInitialized = false;
        var captchaRendered = null;
        var suspended = true;

        var config = {
            'fileupload': 'api/v1/playlist/queue',
            'submitpreset': 'api/v1/playlist/preset',
            'authentication': 'api/v1/authentication',
            'playlist': 'api/v1/playlist',
            'presets': 'api/v1/presets'
        }


        instance.initialize = function () {

            if (mockData) {
                config.playlist = 'js/demo-playlist.json';
                config.fileupload = 'js/demo-upload.json';
                config.submitpreset = 'js/demo-upload.json';
                config.presets = 'js/demo-preset.json';
                config.authentication = 'js/demo-authentication.json';
            }

            if (suspended) {
                $("#playlist").empty();
                emptyPlaylist();
                siteClosed();
            }else{
                $('#reload').click(function () {
                    loadPlaylist()
                });

                loadPlaylist();
                loadPresets();

                $("#submitcaptcha").click(function () {
                    submitCaptcha();
                });

                $('#uploadwarning').click(function () {
                    $("#uploadwarning").fadeOut();
                });

                $('#captchawarning').click(function () {
                    $("#captchawarning").fadeOut();
                });

                loadPlaylistInterval = window.setInterval(function () {
                    loadPlaylist();
                }, playlistRefresh);

                countdownInterval = window.setInterval(function () {
                    refreshCountdown();
                }, 50);

                loadPresetInterval = window.setInterval(function () {
                    loadPresets();
                }, 60 * 1000);
            }

            $('#fileupload').fileupload({
                url: config.fileupload,
                dataType: 'json',
                done: function (e, data) {
                    if (data.jqXHR.responseJSON) {
                        var enqueued = data.jqXHR.responseJSON;
                        enqueueSuccess(enqueued);
                    }
                    $('#uploadprogress').hide();
                },
                fail: function (e, data) {
                    var enqueued = null;
                    var status = data.jqXHR.status;
                    if (data.jqXHR.responseJSON) {
                        enqueued = data.jqXHR.responseJSON;
                    }

                    enqueueFailed(enqueued, status);
                    $('#uploadprogress').hide();
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

        instance.captchaInitialized = function () {
            captchaInitialized = true;
        }

        function refreshCountdown() {

            $('.countdown').each(function (index, value) {
                var key = $(value).data("i18n");
                var end = $(value).data("countdown-end");
                var diff = end - Date.now();


                if (diff > 0) {
                    diff = Math.round(diff / 1000);
                    var text = i18n.t(key, {count: diff});
                    if ($(value).html() != text) {
                        $(value).html(text);
                    }
                }
            });
        }

        function enqueueSuccess(data) {
            var key = "enqueue_success";
            if (data.durationToPlay && data.durationToPlay >= 20) {
                key = "enqueue_success_plural"
            }

            $('#uploadsuccesstext').html(i18n.t(key, {
                track: data.trackName,
                count: data.durationToPlay
            }));

            var tweetKey = "enqueue_tweet_now";
            if (data.durationToPlay >= 20) {
                tweetKey = "enqueue_tweet";
            }

            var tweet = i18n.t(tweetKey, {
                track: data.trackName,
                count: data.durationToPlay
            });

            $("#tweetsuccess").attr('href', 'http://twitter.com/home?status=' + escape(tweet));
            $('#uploadsuccess').fadeIn();
            setTimeout(function () {
                $("#uploadsuccess").fadeOut()
            }, 30000);
        }

        function enqueueFailed(enqueued, status) {
            $('#progress .progress-bar').removeClass('progress-bar-success');
            $('#progress .progress-bar').addClass('progress-bar-warning');


            var message = null;
            if (status == 401 && enqueued) {
                if (enqueued.submitStatus == 'UNAUTHENTICATED') {
                    message = i18n.t("enqueue_unauthenticated");
                    checkOrCreateRecaptcha();
                }
            }

            if (status == 423 && enqueued) {
                if (enqueued.submitStatus == 'OFFLINE') {
                    message = i18n.t("enqueue_offline");
                }
            }

            if (status == 429 && enqueued) {
                if (enqueued.submitStatus == 'QUOTA') {
                    message = i18n.t("enqueue_queue_error");
                }
            }

            if (message == null && status >= 400 && status <= 499) {
                message = i18n.t("enqueue_bad_request");
            }

            if (message == null && status >= 500 && status <= 599) {
                message = i18n.t("enqueue_internal_server_error");
            }

            $('#uploadwarning').html(message);
            $('#uploadprogress').hide();


            $('#uploadwarning').fadeIn();
            setTimeout(function () {
                $("#uploadwarning").fadeOut();
            }, fadeoutAfter);
        }

        function enqueuePreset(presetfile) {
            var uri = config.submitpreset;

            $.ajax({
                    dataType: 'json',
                    accepts: {
                        json: 'application/json'
                    },
                    url: uri,
                    type: 'POST',
                    data: JSON.stringify({
                        presetfile: presetfile
                    })
                }
            ).done(function (data) {
                    enqueueSuccess(data);
                    checkOrCreateRecaptcha();
                }).fail(function (jqXHR) {
                    var enqueued = null;
                    var status = jqXHR.status;
                    if (jqXHR.responseJSON) {
                        enqueued = jqXHR.responseJSON;
                    }

                    enqueueFailed(enqueued, status);
                });
        }


        function loadPresets() {

            var uri = config.presets;

            $.ajax({
                    dataType: 'json',
                    accepts: {
                        json: 'application/json'
                    },
                    url: uri
                }
            ).done(function (data) {

                    $("#presets").empty();
                    if (!data) {
                        return;
                    }

                    for (var key in data) {


                        if (data.hasOwnProperty(key)) {

                            var title = data[key];

                            var a = $(document.createElement("a"));
                            a.addClass('btn btn-default preset-btn');
                            a.attr('data-preset', key);

                            a.mouseover(function () {
                                $(this).addClass('btn-success').removeClass('btn-default');
                            });

                            a.mouseout(function () {
                                $(this).addClass('btn-default').removeClass('btn-success');
                            });

                            a.click(function () {
                                var presetfile = $(this).data('preset');
                                if (presetfile && presetfile.length != 0) {
                                    enqueuePreset($(this).data('preset'));
                                }
                            });

                            a.append("<i class='fa fa-music'></i> " + $('<div/>').text(title).html())

                            $("#presets").append(a, '<br/>');
                        }
                    }
                });
        }


        function checkOrCreateRecaptcha() {

            if (!siteIsOpen) {
                return;
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
                        if (siteIsOpen && captchaInitialized) {
                            $("#captchacontainer").show();

                            if (captchaRendered != null) {
                                grecaptcha.reset(captchaRendered);
                            } else {
                                captchaRendered = grecaptcha.render(
                                    "recaptcha_div",
                                    {
                                        'sitekey': data.recaptchaPublicKey,
                                        'theme': 'light'
                                    }
                                );
                            }

                        }
                        hideAllUploads();
                    }

                    if (data.humanOrMachine && data.humanOrMachine == 'human') {
                        captchaConfirmsHooooman(data);
                    }
                }).fail(function (data) {
                    hideAllUploads();
                });
        }

        function captchaConfirmsHooooman(data) {
            $('#captchacontainer').hide();

            if (data.presetSubmitted) {
                $('#presetcontainer').hide();
            }
            else {
                $('#presetcontainer').show();
            }

            $('#uploadgroup').show();
            $('#uploadcontainer').show();
            $('#lastinupload').show();
        }

        function hideAllUploads() {
            $('#uploadgroup').hide();
            $('#uploadcontainer').hide();
            $('#lastinupload').hide();
            $('#presetcontainer').hide();
        }

        instance.checkOrCreateRecaptcha = checkOrCreateRecaptcha;

        function submitCaptcha() {

            var captcha = grecaptcha.getResponse(captchaRendered);
            var uri = "api/v1/authentication";
            $.ajax({
                dataType: 'json',
                accepts: {
                    json: 'application/json'
                },
                url: uri,
                type: 'POST',
                data: {
                    recaptcha_response_field: captcha
                }
            }).done(
                function (data) {
                    if (!data.humanOrMachine || data.humanOrMachine == 'machine') {
                        hideAllUploads();

                        $('#captchawarning').html(i18n.t("captcha_validation_error"));
                        $('#captchawarning').fadeIn();
                        setTimeout(function () {
                            $("#captchawarning").fadeOut();
                        }, fadeoutAfter);

                        checkOrCreateRecaptcha();
                        return;
                    }

                    if (data.humanOrMachine && data.humanOrMachine == 'human') {
                        captchaConfirmsHooooman(data);
                    }
                }
            ).fail(function () {
                    hideAllUploads();
                    $('#captchawarning').html(i18n.t("internal_server_error"));
                    $('#captchawarning').fadeIn();

                    setTimeout(function () {
                        $("#captchawarning").fadeOut();
                    }, fadeoutAfter);
                });
        }


        function siteClosed() {
            siteIsOpen = false;
            $("#closed").show();
            $("#closedsign").show();
            $("#offlinenote").show();
            $("#offlinenote2").show();
            $("#captchacontainer").hide();
            hideAllUploads();
        }

        function queueClosed() {
            queueIsOpen = false;
            $("#captchacontainer").hide();
            hideAllUploads();
        }

        function queueOpen() {
            queueIsOpen = true;
            checkOrCreateRecaptcha();
        }

        function siteOpen() {
            siteIsOpen = true;
            $("#closed").hide();
            $("#closedsign").hide();
            $("#offlinenote").hide();
            $("#offlinenote2").hide();
            checkOrCreateRecaptcha();
        }

        function emptyPlaylist() {
            var html = "<a href=\"#\" class=\"list-group-item\">" +
                "<h4 class=\"list-group-item-heading\">" + i18n.t("index_playlist_empty") + "</h4>" +
                "<p class=\"list-group-item-text\">&nbsp;</p></a>"

            $("#playlist").append(html);
        }

        function updateSiteState(data) {

            if (data && data.online) {


                if (!siteIsOpen) {
                    siteOpen();
                }

                if (data.queueOpen) {
                    if (!queueIsOpen) {
                        queueOpen();
                    }
                } else {
                    if (queueIsOpen) {
                        queueClosed();
                    }
                }
            }
            else {
                siteClosed();
            }
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

                    updateSiteState(data);

                    if (data && data.entries && data.entries.length && data.entries.length != 0) {

                        var entries = data.entries;
                        for (var index in entries) {

                            var playCommand = entries[index];
                            var trackName = "No name";
                            var foundTrackName = false;
                            if (playCommand.trackName && playCommand.trackName != '') {
                                trackName = playCommand.trackName;
                                foundTrackName = true;
                            }

                            if (playCommand.fileName && playCommand.fileName != '') {
                                if (foundTrackName) {
                                    trackName += " (" + playCommand.fileName + ")";

                                } else {
                                    trackName = playCommand.fileName;
                                }
                            }

                            var remaining = "<p class=\"list-group-item-text countdown\">&nbsp;</p>";

                            if (playCommand.playing) {

                                var coundownend = Date.now() + (playCommand.remaining * 1000);

                                if (playCommand.remaining && playCommand.remaining > 0) {
                                    remaining = "<p class=\"list-group-item-text countdown\" data-i18n=\"remaining_seconds\" " +
                                    "data-countdown-end=\"" + coundownend + "\">" +
                                    i18n.t("remaining_seconds", {count: playCommand.remaining}) + "</p>";
                                }

                                var html = "<a href=\"#\" class=\"list-group-item active\">" +
                                    "<h4 class=\"list-group-item-heading\"><span class=\"glyphicon glyphicon-play\"></span> " + trackName + "</h4>" +
                                    remaining +
                                    "</a>"

                                $("#playlist").append(html);
                                first = false;
                            } else {
                                var coundownend = Date.now() + (playCommand.timeToStart * 1000);

                                if (playCommand.timeToStart && playCommand.timeToStart > 0) {
                                    remaining =
                                        "<p class=\"list-group-item-text countdown\" data-i18n=\"duration_seconds\" " +
                                        "data-countdown-end=\"" + coundownend + "\">" +
                                        i18n.t("duration_seconds", {count: playCommand.timeToStart}) + "</p>";
                                }

                                var html = "<a href=\"#\" class=\"list-group-item\">" +
                                    "<h4 class=\"list-group-item-heading\">" + trackName + "</h4>" +
                                    remaining +
                                    "</a>"

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
                        emptyPlaylist();
                    }
                }
            ).
                fail(function () {
                    $("#playlist").empty();
                    emptyPlaylist();
                    siteClosed();
                });
        }

        return instance;
    })
    ;
