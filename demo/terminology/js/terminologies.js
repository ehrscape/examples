/**
 * Created by LeandroG on 16.9.2014.
 */

(function ($, window, document, undefined) {

    var pluginName = "terminologiesEhrscape",
        defaults = {
            baseUrl: "https://rest.ehrscape.com/rest/v1",
            selectElTer: $("#entities"), //select entities
            inputElTer: $("#terminologies"), //input terminologies
            terminologiesUrl: "https://rest.ehrscape.com/terminology-adapter/rest",
            username: "guidemo",
            password: "gui?!demo123"
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
            this.cache.terminologies = {};

            this.codeSystem = "ICD10"; //variable to keep track of actual code system

            this.getCodeSystems();

            this.enableAutocomplete(this.options.inputElTer);

            this.initEvents();

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
        },

        createTreeObj: function (data) {

            var self = this;

            this.entities.push({ "id": data[0].code, "parent": "#", "text": data[0].code + ": " + data[0].description, "state": { "opened": false } });
            var code, parent = data[0].code;

            for (var i = 1; i < data.length; i++) {

                code = data[i].code;
                if (code.indexOf(parent) == -1) {
                    self.entities.push({ "id": data[i].code, "parent": "#", "text": data[i].code + ": " + data[i].description, "state": { "opened": false } });
                    parent = data[i].code;
                }
                else {
                    self.entities.push({ "id": data[i].code, "parent": parent, "text": data[i].code + ": " + data[i].description, "icon": "jstree-file", "state": { "opened": false } });
                }

            }

        },

        enableAutocomplete: function (terEl) {

            var self = this;

            terEl.catcomplete({
                source: function (request, response) {

                    var term = request.term;

                    //set up local caching

                    self.$element.jstree(true).search(term);

                    if (self.codeSystem in self.cache.terminologies) {
                        if (term in self.cache.terminologies [self.codeSystem]) {
                            response(self.cache.terminologies [self.codeSystem] [ term ]);
                            return;
                        }
                    }

                    var data = [];

                    $.ajax({
                        type: "GET",
                        url: self.options.terminologiesUrl + '/terminology/codesystem/' + self.codeSystem + '/entities',
                        data: { matchvalue: term },
                        contentType: 'application/json',
                        success: function (res) {

                            if (res) {
                                for (var i = 0; i < res.items.length; i++) {
                                    data.push({ category: "Entities", code: res.items[i].code, label: res.items[i].code + ": " + res.items[i].description})
                                }
                            }

                            if (!self.cache.terminologies [self.codeSystem]) {
                                self.cache.terminologies [self.codeSystem] = {};
                            }
                            self.cache.terminologies [self.codeSystem] [term] = data;

                            response(data);

                        }
                    });

                },
                select: function (event, ui) {

                    self.setInputTerminology(ui.item.label, ui.item.code);

                    return false;
                },
                open: function () {
                    self.$element.parent().css('overflow', 'hidden');
                },
                close: function () {
                    self.$element.parent().css('overflow', 'auto');
                }
            });
        },

        getCodeSystems: function () {

            var self = this;

            return $.ajax({
                type: "GET",
                url: this.options.terminologiesUrl + '/terminology/codesystems',
                contentType: 'application/json',
                success: function (data) {
                    self.setSelectOptions(data.items);
                }
            });
        },

        getData: function (page) {

            var self = this;

            return $.ajax({
                type: "GET",
                url: this.options.terminologiesUrl + '/terminology/codesystem/' + self.codeSystem + '/entities',
                contentType: 'application/json',
                data: {
                    page: page,
                    pagesize: 1000
                },
                success: function (data) {

                    $.merge(self.browsingData, data.items);

                    self.setTreeView(self.browsingData); //update tree

                    if (data.lastPage) {

                        $("#loadMore").hide();

                    }
                }
            });

        },

        getEntities: function () {

            $("#loadMore").show();

            this.browsingData = []; //entities data

            this.pageCount = 1;

            this.getData(this.pageCount);

        },

        initEvents: function () {

            var self = this;

            //manual "load more data" option

            $("#loadMore").bind('click touchstart', function () {

                self.pageCount++;

                self.getData(self.pageCount);

            });
        },

        setSelectOptions: function (data) {

            var self = this;
            var elSelect = self.options.selectElTer;

            for (var i = 0; i < data.length; i++) {
                elSelect.append('<option value="' + data[i].code + '">' + data[i].description + '</option>');
            }

            elSelect.selecter({
                callback: function (value) {
                    self.codeSystem = value;
                    self.getEntities();
                }
            });

            this.getEntities();

        },

        setInputTerminology: function (label, code) {

            this.options.inputElTer.val(label);
            this.options.inputElTer.attr("data-code", code);

            this.$element.jstree(true).search();

        },

        setTreeView: function (data) {

            var self = this;

            this.entities = [];

            //format data
            this.createTreeObj(data);

            if (this.$element.jstree(true) == false) {

                this.$element.jstree({
                    "core": {
                        "data": this.entities
                    },
                    "plugins": [ "search" ]
                });

                this.$element
                    // listen for event
                    .on('changed.jstree', function (e, data) {
                        var i, j, r = [];
                        for (i = 0, j = data.selected.length; i < j; i++) {
                            r.push(data.instance.get_node(data.selected[i]).text);
                        }

                        var entity = r.join(', '),
                            code = entity.substring(0, entity.indexOf(":"));

                        self.setInputTerminology(entity, code);

                    })
                    // create the instance
                    .jstree();

                // load more data on scroll

                $('#treeWrapper').bind('scroll', check_scroll);

                function check_scroll(e) {
                    var elem = $(e.currentTarget);
                    if (elem[0].scrollHeight - elem.scrollTop() == elem.outerHeight()) {

                        if ($("#loadMore").is(":visible")) {

                            self.pageCount++;

                            self.getData(self.pageCount);

                        }

                    }

                }

            }
            else {

                //reset tree

                this.$element.jstree(true).settings.core.data = this.entities;

                this.$element.jstree(true).refresh(function () {
                    return false;
                });
            }

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

