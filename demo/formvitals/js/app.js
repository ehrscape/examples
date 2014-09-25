/**
 * Copyright (c) 2014 Marand Lab
 */

(function ($, window, document, undefined) {

    var pluginName = "formVitalsEhrscape",
        defaults = {
            baseUrl: "https://rest.ehrscape.com/rest/v1",
            templateId: "Vital Signs",
            username: "username",
            password: "****",
            ehrId: "0737c232-0e4b-4f8b-a42e-ee3fa78a5dfa",
            language: "en",
            $resultsTable: $("#historyForm"),
            $languageSelect: $("#language"),
            formEl: [
                {id: "weight", path: "Body weight|Body weight", type: "input", auxLabel: false},
                {id: "height", path: "Height/Length|Body Height/Length", type: "input", auxLabel: false},
                {id: "bmi", path: "Body mass index|Body Mass Index", type: "input", auxLabel: false},
                {id: "systolic", path: "Blood Pressure|Systolic", type: "input", auxLabel: true},
                {id: "diastolic", path: "Blood Pressure|Diastolic", type: "input", auxLabel: false},
                {id: "position", path: "Blood Pressure|Position", type: "select", auxLabel: false},
                {id: "rate", path: "Pulse|Rate", type: "input", auxLabel: true},
                {id: "rhythm", path: "Respirations|Rhythm", type: "select", auxLabel: true},
                {id: "depth", path: "Respirations|Depth", type: "select", auxLabel: false}
            ]
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

            var self = this;

            this.getTemplate();

            this.$element.on('touchstart click', function () {

                self.getFormData();

            });

        },

        buildHistoryTable: function (data) {

            this.updateTableHeader();

            var self = this, html = '';

            for (var i = 0; i < data.length; i++) {

                html += "<tr>";

                html += "<td>" + this.concatData(data[i].weight) + "</td><td>" + this.concatData(data[i].height) + "</td><td>" + this.concatData(data[i].bmi) + "</td><td>" + this.concatData(data[i].systolic) + "</td><td>" + this.concatData(data[i].diastolic) + "</td><td>" + this.decode("position", data[i].position) + "</td><td>" + this.concatData(data[i].rate) + "</td><td>" + this.decode("rhythm", data[i].rhythm) + "</td><td>" + this.decode("depth", data[i].depth) + "</td><td>" + self._formatDate(data[i].date) + "</td>";

                html += "</tr>";

            }

            this.options.$resultsTable.find("tbody").html(html);

        },

        concatData: function (data) {

            return (data.magnitude + " " + data.units);

        },

        decode: function (name, code) {

            var term = "", language = this.options.language;

            for (var i = 0; i < this.options.formEl.length; i++) {

                if (this.options.formEl[i].id == name) {

                    var list = this.options.formEl[i].data.inputs[0].list;

                    for (var k = 0; k < list.length; k++) {
                        if (list[k].value == code) term = list[k].localizedLabels[language];
                    }
                }

            }

            return term;

        },

        initOtherEvents: function () {

            //BMI automatic calculation
            $(".bmi-element").on('keyup', function () {

                var weight = $("#weight").val(),
                    height = $("#height").val(),
                    bmi = 0;

                if (weight && height) {

                    bmi = ( weight / Math.pow(( height * 0.01 ), 2) );

                    bmi = Math.round(bmi * 10) / 10;
                }

                $("#bmi").val(bmi);
            });

        },

        getChildrenByName: function (data, name) {

            for (var i = 0; i < data.length; i++) {

                if (data[i].name) {

                    if (data[i].name == name) {

                        return (data[i].children);

                    }
                }
            }

        },

        getCompositionsHistory: function () {

            var self = this, sessionId = this._getSessionId();

            this.startLoading(this.options.$resultsTable.parent());

            $.ajaxSetup({
                headers: {
                    "Ehr-Session": sessionId
                }
            });

            var aql = "select " +
                "a/context/start_time/value as date, " +
                "a_a/data[at0002]/events[at0003]/data[at0001]/items[at0004, 'Body weight']/value as weight, " +
                "a_b/data[at0001]/events[at0002]/data[at0003]/items[at0004, 'Body Height/Length']/value as height, " +
                "a_c/data[at0001]/events[at0002]/data[at0003]/items[at0004]/value as bmi, " +
                "a_d/data[at0001]/events[at0006]/data[at0003]/items[at0004]/value as systolic, " +
                "a_d/data[at0001]/events[at0006]/data[at0003]/items[at0005]/value as diastolic, " +
                "a_d/data[at0001]/events[at0006]/state[at0007]/items[at0008]/value/defining_code/code_string as position, " +
                "a_f/data[at0002]/events[at0003]/data[at0001]/items[at0004]/value as rate, " +
                "a_g/data[at0001]/events[at0002]/data[at0003]/items[at0005]/value/defining_code/code_string as rhythm, " +
                "a_g/data[at0001]/events[at0002]/data[at0003]/items[at0016]/value/defining_code/code_string as depth " +
                "from EHR e " +
                "contains COMPOSITION a " +
                "contains ( " +
                "OBSERVATION a_a[openEHR-EHR-OBSERVATION.body_weight.v1] and " +
                "OBSERVATION a_b[openEHR-EHR-OBSERVATION.height.v1] and " +
                "OBSERVATION a_c[openEHR-EHR-OBSERVATION.body_mass_index.v1] and " +
                "OBSERVATION a_d[openEHR-EHR-OBSERVATION.blood_pressure.v1] and " +
                "OBSERVATION a_f[openEHR-EHR-OBSERVATION.heart_rate-pulse.v1] and " +
                "OBSERVATION a_g[openEHR-EHR-OBSERVATION.respiration.v1]) " +
                "order by date DESC offset 0 limit 100";

            $.ajax({
                url: this.options.baseUrl + "/query?" + $.param({"aql": aql}),
                type: 'GET',
                success: function (res) {

                    self.compositionsData = res.resultSet;

                    self.buildHistoryTable(self.compositionsData);

                    self.stopLoading(self.options.$resultsTable.parent());

                    self._closeSession(sessionId);
                }
            });
        },

        getDataByName: function (data, name) {
            for (var i = 0; i < data.length; i++) {

                if (data[i].name) {

                    if (data[i].name == name) {

                        return (data[i]);

                    }
                }
            }
        },

        getFieldDetails: function (response) {

            this.templateData = response.webTemplate.tree.children;

            for (var i = 0; i < this.options.formEl.length; i++) {  //store data for each element

                var field = this.options.formEl[i].path.split("|");

                var dataAux = this.getChildrenByName(this.templateData, field[0]);

                this.options.formEl[i].data = this.getDataByName(dataAux[0].children, field[1]); //[0] = any event

            }

            this.setLabels();

            this.getCompositionsHistory();

            this.initOtherEvents();

        },

        getFormData: function () {

            var weight = $("#weight").val(),
                height = $("#height").val(),
                bmi = $("#bmi").val(),
                systolic = $("#systolic").val(),
                diastolic = $("#diastolic").val(),
                position = $("#position").parent(".selecter").data("selecter").$select.val(),
                depth = $("#depth").parent(".selecter").data("selecter").$select.val(),
                rhythm = $("#rhythm").parent(".selecter").data("selecter").$select.val(),
                rate = $("#rate").val();

            if (weight && height && bmi && systolic && diastolic && rate && position && depth && rhythm) {

                var compositionData = {
                    "ctx/language": "en",
                    "ctx/territory": "SI",
                    "vital_signs/body_weight/any_event/body_weight": parseFloat(weight),
                    "vital_signs/height_length/any_event/body_height_length": parseFloat(height),
                    "vital_signs/body_mass_index/any_event/body_mass_index": parseFloat(bmi),
                    "vital_signs/blood_pressure/any_event/systolic": parseFloat(systolic),
                    "vital_signs/blood_pressure/any_event/diastolic": parseFloat(diastolic),
                    "vital_signs/blood_pressure/any_event/position": position,
                    "vital_signs/pulse/any_event/rate": parseFloat(rate),
                    "vital_signs/respirations/any_event/depth": depth,
                    "vital_signs/respirations/any_event/rhythm": rhythm
                };

                this.saveComposition(compositionData);

            }
            else {

                $.growl({
                        icon: "fa fa-warning",
                        title: "<strong> Error!</strong><br>",
                        message: "Please fill all the obligatory fields."
                    },
                    {
                        type: "danger",
                        animate: {
                            enter: 'animated flipInY',
                            exit: 'animated flipOutX'
                        },
                        delay: 3000,
                        onShow: function () {
                            if (!weight) $('#weight').addClass("obligatory-field");
                            if (!height) $('#height').addClass("obligatory-field");
                            if (!bmi) $('#bmi').addClass("obligatory-field");
                            if (!systolic) $('#systolic').addClass("obligatory-field");
                            if (!diastolic) $('#diastolic').addClass("obligatory-field");
                            if (!rate) $('#rate').addClass("obligatory-field");
                        },
                        onHide: function () {
                            if (!weight) $('#weight').removeClass("obligatory-field");
                            if (!height) $('#height').removeClass("obligatory-field");
                            if (!bmi) $('#bmi').removeClass("obligatory-field");
                            if (!systolic) $('#systolic').removeClass("obligatory-field");
                            if (!diastolic) $('#diastolic').removeClass("obligatory-field");
                            if (!rate) $('#rate').removeClass("obligatory-field");
                        }
                    });

            }

        },

        getTemplate: function () {

            var self = this, sessionId = this._getSessionId();

            $.ajax({
                url: this.options.baseUrl + "/template/" + this.options.templateId,
                type: 'GET',
                headers: {
                    "Ehr-Session": sessionId
                },
                success: function (res) {

                    $("#templateId").html(res.webTemplate.templateId); //set Title

                    self.setLanguages(res.webTemplate.languages);

                    self.getFieldDetails(res);

                    self._closeSession(sessionId);

                }
            });
        },

        resetForm: function () {

            for (var i = 0; i < this.options.formEl.length; i++) {

                var el = $("#" + this.options.formEl[i].id);

                if (this.options.formEl[i].type == "select") {

                    var firstValue = $(el.children()[0]).val();

                    el.val(firstValue).trigger("change");

                }
                else {

                    el.val("");
                }

            }
        },

        saveComposition: function (compositionData) {

            var self = this, sessionId = this._getSessionId();

            $.ajaxSetup({
                headers: {
                    "Ehr-Session": sessionId
                }
            });

            var queryParams = {
                "ehrId": this.options.ehrId,
                templateId: 'Vital Signs',
                format: 'FLAT',
                committer: 'guidemo'
            };

            $.ajax({
                url: this.options.baseUrl + "/composition?" + $.param(queryParams),
                type: 'POST',
                contentType: 'application/json',
                data: JSON.stringify(compositionData),
                success: function () {

                    $.growl({
                            icon: "fa fa-check",
                            title: "<strong> Success!</strong><br>",
                            message: "Composition has been stored."
                        },
                        {
                            type: "success",
                            animate: {
                                enter: 'animated flipInY',
                                exit: 'animated flipOutX'
                            },
                            delay: 3000
                        });

                    self.resetForm(); //clear form

                    self.getCompositionsHistory(); //table update

                    self._closeSession(sessionId);

                }
            });
        },

        setAuxiliaryLabel: function (el, exp, language) {

            var label, data = this.templateData;

            for (var i = 0; i < data.length; i++) {

                if (data[i].name) {

                    if (data[i].name == exp) {

                        label = data[i].localizedNames[language];

                    }
                }
            }

            $("#" + el).closest('.list-group-item').prev().html(label);

        },

        setLabels: function () {

            var lang = this.options.language;

            for (var i = 0; i < this.options.formEl.length; i++) {

                var field = this.options.formEl[i].path.split("|");

                //"One level up" label (blue headers)
                if (this.options.formEl[i].auxLabel) this.setAuxiliaryLabel(this.options.formEl[i].id, field[0], lang);

                var el = $("#" + this.options.formEl[i].id),
                    elData = this.options.formEl[i].data,
                    elInputData = this.options.formEl[i].data.inputs;

                var label = elData.localizedNames[lang];

                if (this.options.formEl[i].type == "select") {

                    this.setSelectOptions(el, elInputData[0].list, lang); //build select

                    el.parent().siblings(".select-label").html(label); // label

                }
                else {

                    el.prev().html(label); // label

                    var units = elInputData[1].list[0].value;

                    el.next().html(units); //units

                }

                this.options.formEl[i].label = label;

            }


        },

        setLanguages: function (languages) {

            var langSelect = this.options.$languageSelect;

            // build select element
            for (var i = 0; i < languages.length; i++) {
                langSelect.append('<option value="' + languages[i] + '">' + languages[i] + '</option>');
            }

            var self = this;

            langSelect.selecter({
                callback: function (value) {

                    self.options.language = value;

                    self.setLabels();

                    self.buildHistoryTable(self.compositionsData);

                }
            });

        },

        setSelectOptions: function (id, list, language) {

            var select = id;

            select.html('');

            for (var i = 0; i < list.length; i++) {

                select.append('<option value="' + list[i].value + '">' + list[i].localizedLabels[language] + '</option>');

            }

            if (select.closest(".selecter").length > 0) {
                //check if selecter plugin is already initialized
                select.selecter("destroy");
                select.selecter();

            }
            else {
                select.selecter();
            }
        },

        updateTableHeader: function () {

            var html = '<tr>';

            for (var i = 0; i < this.options.formEl.length; i++) {

                html += '<th>' + this.options.formEl[i].label + '</th>';

            }

            switch (this.options.language) {
                case 'en':
                    html += '<th>Date</th></tr>';
                    break;
                case 'sl':
                    html += '<th>Datum</th></tr>';
                    break;
                default:
                    html += '<th>Date</th></tr>';
                    break;
            }


            this.options.$resultsTable.find("thead").html(html);

        },

        /* Spinner (loading) */

        startLoading: function (el) {

            $(el.children()[0]).css('opacity', '0.3');

            var opts = {
                color: '#3BAFDA' // #rgb or #rrggbb or array of colors
            };

            $(el).spin(opts);

        },

        stopLoading: function (el) {

            $(el).spin(false);

            $(el.children()[0]).css('opacity', '1');

        },

        /* Internal functions */

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
        },

        _formatDate: function (date) {

            var d = new Date(date);
            var dd = d.getDate();
            var mm = d.getMonth() + 1; //January is 0
            var yyyy = d.getFullYear();
            var mins = d.getMinutes();
            var hours = d.getHours();

            if (dd < 10) {
                dd = '0' + dd;
            }

            if (mm < 10) {
                mm = '0' + mm;
            }

            if (hours < 10) {
                hours = '0' + hours;
            }

            if (mins < 10) {
                mins = '0' + mins;
            }

            return (dd + '-' + mm + '-' + yyyy + " " + hours + ":" + mins);

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

