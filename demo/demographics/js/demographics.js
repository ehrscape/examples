/**
 * Created by LeandroG on 16.9.2014.
 */
(function ($, window, document, undefined) {

    var pluginName = "demographicsEhrscape",
        defaults = {
            baseUrl: "https://rest.ehrscape.com/rest/v1",
            username: "guidemo",
            password: "gui?!demo123",
            inputElDem: $("#demoInput")
        };

    function Plugin(element, options) {
        this.element = element;
        this.$element = $(this.element);

        this.options = $.extend({}, defaults, options);

        this._defaults = defaults;
        this._name = pluginName;

        this.init();
    }

    Plugin.prototype = {

        init: function () {

            this.cache = {};
            this.cache.demographics = {};

            this.enableAutocomplete(this.options.inputElDem);

            this.initEvents();

        },

        enableAutocomplete: function (demEl) {

            var self = this;

            demEl.catcomplete({
                source: function (request, response) {

                    var term = request.term;

                    // Set up local caching

                    if (term in self.cache.demographics) {
                        response(self.cache.demographics[ term ]);
                        return;
                    }

                    var sessionId = self._getSessionId();

                    var data = [];

                    $.ajax({
                        url: self.options.baseUrl + "/demographics/party/query",
                        type: 'GET',
                        data: { search: "*" + term + "*" },
                        contentType: 'application/json',
                        headers: {
                            "Ehr-Session": sessionId
                        },
                        success: function (res) {

                            if (res) {
                                for (var i = 0; i < res.parties.length; i++) {

                                    var rawData = res.parties[i];
                                    data.push({ category: "Demographics", value: rawData.firstNames + " " + rawData.lastNames, label: rawData.firstNames + " " + rawData.lastNames + " (" + rawData.gender.toLowerCase() + ", " + self._getAge(self._formatDateUS(rawData.dateOfBirth)) + ")", rawData: rawData})

                                }
                            }

                            self.cache.demographics[ term ] = data;

                            self._closeSession(sessionId);

                            response(data);

                        }
                    });

                },
                select: function (event, ui) {

                    $(event.target).val(ui.item.value);

                    var data = ui.item.rawData;

                    $("#demo-id").html(data.id);
                    $("#demo-name").find("span").html(data.firstNames + " " + data.lastNames);
                    $("#demo-sex").html(self._capitaliseFirstLetter(data.gender.toLowerCase()));
                    $("#demo-age").html(self._getAge(self._formatDateUS(data.dateOfBirth)));
                    $("#demo-dob").html(self._formatDate(data.dateOfBirth));


                    return false;
                }
            });

        },

        initEvents: function () {

            var self = this, input = this.options.inputElDem.hide();

            //Typeahead behaviour

            $("#demo-name").bind('click touchstart', function () {

                var $element = $(this).hide();

                input.show();

                input.focus().blur(function () {
                    $(this).hide();
                    $element.show();
                });

            });

        },

        //Helper Functions

        _capitaliseFirstLetter: function (string) {
            return string.charAt(0).toUpperCase() + string.slice(1);
        },

        _formatDate: function (date) {
            var d = new Date(date);

            var monthNames = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];

            var curr_date = d.getDate();

            var curr_month = d.getMonth();

            var curr_year = d.getFullYear();

            return monthNames[curr_month] + ". " + curr_date + ", " + curr_year;

        },

        _formatDateUS: function (date) {
            var d = new Date(date);

            var curr_date = d.getDate();
            curr_date = this._normalizeDate(curr_date);

            var curr_month = d.getMonth();
            curr_month++;
            curr_month = this._normalizeDate(curr_month);

            var curr_year = d.getFullYear();

            return curr_month + "-" + curr_date + "-" + curr_year;

        },

        _getAge: function (dateString) {
            var now = new Date();
            var today = new Date(now.getYear(), now.getMonth(), now.getDate());

            var yearNow = now.getYear();
            var monthNow = now.getMonth();
            var dateNow = now.getDate();

            var dob = new Date(dateString.substring(6, 10),
                    dateString.substring(0, 2) - 1,
                dateString.substring(3, 5)
            );

            var yearDob = dob.getYear();
            var monthDob = dob.getMonth();
            var dateDob = dob.getDate();
            var age = {};
            var ageString = "";
            var yearString = "";
            var monthString = "";
            var dayString = "";


            var yearAge = yearNow - yearDob;

            if (monthNow >= monthDob)
                var monthAge = monthNow - monthDob;
            else {
                yearAge--;
                var monthAge = 12 + monthNow - monthDob;
            }

            if (dateNow >= dateDob)
                var dateAge = dateNow - dateDob;
            else {
                monthAge--;
                var dateAge = 31 + dateNow - dateDob;

                if (monthAge < 0) {
                    monthAge = 11;
                    yearAge--;
                }
            }

            age = {
                years: yearAge,
                months: monthAge,
                days: dateAge
            };

            if (age.years > 1) yearString = "y";
            else yearString = "y";
            if (age.months > 1) monthString = "m";
            else monthString = "m";
            if (age.days > 1) dayString = " days";
            else dayString = " day";


            if ((age.years > 0) && (age.months > 0) && (age.days > 0))
                ageString = age.years + yearString + " " + age.months + monthString;// + ", and " + age.days + dayString + " old";
            else if ((age.years == 0) && (age.months == 0) && (age.days > 0))
                ageString = age.days + dayString + " old";
            else if ((age.years > 0) && (age.months == 0) && (age.days == 0))
                ageString = age.years + yearString;// + " old. Happy Birthday!";
            else if ((age.years > 0) && (age.months > 0) && (age.days == 0))
                ageString = age.years + yearString + " and " + age.months + monthString;// + " old";
            else if ((age.years == 0) && (age.months > 0) && (age.days > 0))
                ageString = age.months + monthString; // + " and " + age.days + dayString + " old";
            else if ((age.years > 0) && (age.months == 0) && (age.days > 0))
                ageString = age.years + yearString;// + " and " + age.days + dayString + " old";
            else if ((age.years == 0) && (age.months > 0) && (age.days == 0))
                ageString = age.months + monthString;// + " old";
            else ageString = "Oops! Could not calculate age!";

            return ageString;
        },

        _normalizeDate: function (el) {

            el = el + "";

            if (el.length == 1) {
                el = "0" + el;
            }

            return el;
        },

        _getSessionId: function () {
            var response = $.ajax({
                type: "POST",
                url: this.options.baseUrl + "/session?username=" + encodeURIComponent(this.options.username) +
                    "&password=" + encodeURIComponent(this.options.password),
                async: false
            });
            return response.responseJSON.sessionId;
        },

        _closeSession: function (sessionId) {
            $.ajax({
                type: "DELETE",
                url: this.options.baseUrl + "/session",
                headers: {
                    "Ehr-Session": sessionId
                }
            });
        }

    };

    $.fn[pluginName] = function (options) {
        return this.each(function () {
            if (!$.data(this, "plugin_" + pluginName)) {
                $.data(this, "plugin_" + pluginName,
                    new Plugin(this, options));
            }
        });
    };

})(jQuery, window, document);

