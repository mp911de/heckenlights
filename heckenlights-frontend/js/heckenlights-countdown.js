var countdown = (function () {

    var instance = {};
    var moment_string = '2014-12-01 17:00';
    var moment_tz = 'Europe/Berlin';
    var moment_locale_fmt_except = 'de-DE';

    function preciseHumanize(m, withSuffix) {
        var isFuture = m._milliseconds < 0,
            duration = m.abs()._data,
            locale = m.localeData(),
            addRelative = function (number, string, pluralize) {
                if (number === 0) {
                    return;
                }
                if (output.length > 0) {
                    output.push(' ')
                }
                output.push(locale.relativeTime(
                    number,
                    !withSuffix,
                    pluralize ? number > 1 ? (string + string) : string : string,
                    isFuture
                ));
            },
            output = [];

        addRelative(duration.years, 'y', true);
        addRelative(duration.months, 'M', true);
        addRelative(duration.days, 'd', true);
        addRelative(duration.hours, 'h', true);
        addRelative(duration.minutes, 'm', true);
        addRelative(duration.seconds, 's', false);

        output.push(output[output.length - 1]);
        output[output.length - 2] = ' ' + $.i18n.t('term_and') + ' ';

        output = output.join('');

        if (withSuffix) {
            output = m.localeData().pastFuture(isFuture ? -1 : 1, output);
        }

        return m.localeData().postformat(output);
    }


    function refreshCountdown(starting) {
        var duration = moment.duration(starting.diff(new Date()));

        if (duration.asSeconds() > 0) {

            var text = preciseHumanize(duration, false);
            if ($("#countdowntext").text() != text) {
                $("#countdowntext").text(text);
            }
        }
    }

    function initializeTimers() {
        var locale = $.i18n.t('moment_locale');
        moment.locale(locale);

        var starting = moment.tz(moment_string, moment_tz);
        var duration = moment.duration(starting.diff(new Date()));

        var tz = jstz.determine();
        if (duration.asSeconds() > 0) {
            if (tz && tz.name()) {
                var zoned = starting.clone().tz(tz.name());

                var text = zoned.format('lll');
                if (locale != moment_locale_fmt_except) {
                    text += ' (' + zoned.format('z') + ')';
                }
                $('#localizedstart').text(text);
            }

            window.setInterval(function () {
                refreshCountdown(starting);
            }, 100);
        } else {
            $("#countdown").hide();
        }

    }

    instance.initialize = function () {

        moment.tz.load({
            zones: [],
            links: [],
            version: '2014e'
        });

        initializeTimers();
    }

    return instance;
});
