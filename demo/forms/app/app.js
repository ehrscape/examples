(function forms4DemoNamespace() {

    console.log("Here we are");
    var app = angular.module("formsApp", ["ngResource", "tmh.dynamicLocale", "thinkehrForms4", "ui.bootstrap", "ui.bootstrap.datetimepicker"])
        .config(['$httpProvider', "tmhDynamicLocaleProvider", function ($httpProvider, tmhDynamicLocaleProvider) {
            //Enable cross domain calls
            $httpProvider.defaults.useXDomain = true;
            tmhDynamicLocaleProvider.localeLocationPattern("angular/1.3.2/i18n/angular-locale_{{locale}}.js");
        }])
        .factory("AppConfig", function () {
            return {
                url: "https://rest.ehrscape.com/rest/v1",
                username: "guidemo",
                password: "gui?!demo123",
                ehrId: "851aa256-e6cf-4df8-95b6-b1fdfb43f692",
                locales: ["en-US", "sl-SI"]
            };
        })
        .factory("SessionResource", ["$resource", "AppConfig", function ($resource, AppConfig) {
            return $resource(
                AppConfig.url + "/session",
                {}, // Defaults
                // Actions
                {
                    login: {method: "POST", params: {username: AppConfig.username, password: AppConfig.password}},
                    ping: {
                        method: "PUT",
                        headers: {
                            "Ehr-Session": function () {
                                return AppConfig.sessionId
                            }
                        }
                    }
                }
            )
        }])
        .factory("FormResource", ["$resource", "AppConfig", function ($resource, AppConfig) {
            return $resource(
                AppConfig.url + "/form",
                {}, // Defaults,
                // Actions
                {
                    load: {
                        url: AppConfig.url + "/form/:name/:version",
                        method: "GET",
                        params: {resources: "form-description"},
                        headers: {
                            "Ehr-Session": function () {
                                return AppConfig.sessionId
                            }
                        }
                    },

                    list: {
                        method: "GET",
                        headers: {
                            "Ehr-Session": function () {
                                return AppConfig.sessionId
                            }
                        }
                    }
                }
            );
        }])
        .factory("CompositionResource", ["$resource", "AppConfig", function ($resource, AppConfig) {
            return $resource(
                AppConfig.url + "/composition",
                {}, // Defaults,
                // Actions
                {
                    add: {
                        method: "POST",
                        params: {ehrId: AppConfig.ehrId, format: "STRUCTURED"},
                        //params: { format: "STRUCTURED" },
                        headers: {
                            "Ehr-Session": function () {
                                return AppConfig.sessionId;
                            }
                        }
                    },
                    load: {
                        url: AppConfig.url + "/composition/:uid",
                        method: "GET",
                        params: {format: "STRUCTURED"},
                        headers: {
                            "Ehr-Session": function () {
                                return AppConfig.sessionId;
                            }
                        }
                    }
                }
            );
        }])
        .factory("QueryResource", ["$resource", "AppConfig", function ($resource, AppConfig) {
            return $resource(
                AppConfig.url + "/query",
                {}, // Defaults,
                // Actions
                {
                    querySaved: {
                        method: "GET",
                        params: {
                            aql: function () {
                                return "select " +
                                    "c/uid/value as uid, " +
                                    "c/context/start_time/value as startTime, " +
                                    "c/archetype_details/template_id/value as templateId" +
                                    " from EHR[ehr_id/value='" +
                                    AppConfig.ehrId +
                                    "'] " +
                                    " CONTAINS Composition c " +
                                    " where templateId = '" + AppConfig.templateId +
                                    "'" +
                                    " order by startTime DESC offset 0 limit 10";
                            }
                        },
                        headers: {
                            "Ehr-Session": function () {
                                return AppConfig.sessionId;
                            }
                        }
                    }
                }
            );
        }])
        .controller('AppCtrl', ["$scope", "$compile", "$interval", "$timeout", "AppConfig", "FormResource", "SessionResource", "tmhDynamicLocale", "EhrContext",
            "CompositionResource", "EhrSaveHelper", "QueryResource",
            function ($scope, $compile, $interval, $timeout, AppConfig, FormResource, SessionResource, tmhDynamicLocale, EhrContext, CompositionResource, EhrSaveHelper, QueryResource) {
                this.AppConfig = AppConfig;

                $scope.$on('selectPretty', function (scope, element, attrs) {
                    $(element).selecter();
                });

                EhrContext.language = "en";
                EhrContext.territory = "US";
                EhrContext.locale = "en-US";
                this.EhrContext = EhrContext;
                $scope.currentLocale = EhrContext.language + "-" + EhrContext.territory;
//                $scope.formsDataSource = new kendo.data.ObservableArray([]);
                $scope.currentFormIndex = "";
                this.showValueModel = true;
                this.valueModel = {};
                var ctrl = this;

                /*this.refreshValues = function () {
                    console.log("Refresh values");
                    this.valueModel = {};
                    thinkehr.f4.refreshValues(this.formModel, this.valueModel);

                };*/

                this.createGrowl = function(settings) {
                    $.growl({
                            icon: "fa fa-" + settings.icon,
                            title: "<strong> " + settings.title + "</strong><br>",
                            message: settings.message
                        },
                        {
                            type: settings.type,
                            animate: {
                                enter: 'animated fadeInDown',
                                exit: 'animated fadeOutUp'
                            },
                            delay: 1500
                        }
                    );
                };

                this.saveValues = function () {
                    var cr = new CompositionResource(EhrSaveHelper.prepareValueModel(this.valueModel));
                    cr.$add({templateId: this.templateId},
                        function (success) {
                            var h = success.meta.href;
                            var parsedHref = h.split("/");
                            ctrl.lastSaved = parsedHref[parsedHref.length - 1];

                            var opts = {
                                icon: "check",
                                title: "Success!",
                                message: "Composition has been saved.",
                                type: "success"
                            };

                            ctrl.createGrowl(opts);

                            if (!ctrl.compositions) {
                                ctrl.compositions = [];
                            }

                            var newComp = {
                                startTime: new Date(),
                                uid: ctrl.lastSaved,
                                templateId: ctrl.templateId
                            };

                            if (ctrl.compositions.length > 9) {
                                ctrl.compositions.pop();
                            }

                            ctrl.compositions.unshift(newComp);

                        },
                        function (error) {
                            ctrl.saveError = error;
                            console.error("err", error);

                            var opts = {
                                icon: "warning",
                                title: "Error!",
                                message: "Saving compositions is not possible in this demo.",
                                type: "danger"
                            };

                            ctrl.createGrowl(opts);
                        }
                    );
                };

                this.openComposition = function ($event) {
                    $event.preventDefault();
                    var compositionUid = $($event.target).attr("data-composition-uid");
                    if (compositionUid) {
                        CompositionResource.load({uid: compositionUid},
                            function (success) {
                                console.log("Loaded composition: ", success);
                                ctrl.composition = success.composition;
                                ctrl.valueModel = ctrl.composition;

                                thinkehr.f4.refreshValues(ctrl.formModel, ctrl.composition);  //loadFormData

                                $("body").animate({ scrollTop: 0 }, "slow",
                                    function () {

                                        var opts = {
                                            icon: "eye",
                                            title: "Composition view",
                                            message: "Form has been refreshed.",
                                            type: "info"
                                        };

                                        ctrl.createGrowl(opts);
                                    }
                                );
                            }
                        );
                    }
                };

                this.tableClass = function ($even) {
                    return $even ? "even" : "odd";
                };

                var login = SessionResource.login();
                login.$promise.then(function (success) {
                    console.log("Login", success);
                    AppConfig.sessionId = success.sessionId;

                    // Ping the session every 5 minutes
                    $scope.sessionPing = $interval(function () {
                        SessionResource.ping({},
                            function (success) {
                                console.info("Successfully pinged ehr session", AppConfig.sessionId);
                            },
                            function (failure) {
                                console.error("Failed to ping ehr session", AppConfig.sessionId, " please reload page to create a new session.", failure);
                            }
                        );
                    }, 300000, 0, false);

                    return FormResource.list().$promise;
                }).then(
                    function (success) {
                        var displayForms = [];
                        var done = {};
                        var id = 0;
                        angular.forEach(success.forms, function (form) {
                            if (!done[form.name]) {
                                form.xid = id++;
                                form.nameWithVersion = form.name + " " + form.version;
                                displayForms.push(form);
                                done[form.name] = 1;
                            }
                        });

                        $scope.formsDataSource = displayForms;
                        //angular.forEach(success.forms, function (form) {
                        //    form.xid = id++;
                        //    form.nameWithVersion = form.name + " " + form.version;
                        //});

                        $timeout(function () {
                            $("#form-select").selecter();
                        }, 1); //make sure to inject the $timeout service in your controller


                    },

                    function (fail) {
                        console.error("Fail", fail);
                    }
                );

                // This is kinda ugly, redraws the entire form, maybe there is a better way
                $scope.$on("$localeChangeSuccess", function (event) {
                    var scl = $scope.currentLocale;
                    EhrContext.language = scl.substr(0, 2);
                    EhrContext.territory = scl.substr(3, 2);
                    EhrContext.locale = scl;

                    var formAttach = $("#form-attach");
                    var ehrFormEl = $("ehr-form");

                    $(ehrFormEl).remove();

                    var el = $compile('<ehr-form model="appCtrl.formModel" ehr-context="appCtrl.EhrContext" form-id="{{appCtrl.AppConfig.formName}}_{{appCtrl.AppConfig.formVersion}}"></ehr-form>')($scope);
                    formAttach.append(el);

                });

                $scope.$watch("currentLocale", function (newVal, oldVal) {
                    if (newVal != oldVal) {
                        console.log("Locale change", newVal, oldVal, newVal.toLowerCase());
                        tmhDynamicLocale.set(newVal.toLowerCase());
                    }
                });

                $scope.$watch("currentFormIndex", function (newVal, oldVal) {
                    if (newVal != oldVal) {

                        var cfi = parseInt(newVal.xid);
                        var form = $scope.formsDataSource[cfi];

                        if (cfi >= 0) {
                            ctrl.currentForm = form;
                            FormResource.load({name: form.name, version: form.version}).$promise.then(
                                function (success) {

                                    var formDesc = function () {
                                        for (var i = 0; i < success.form.resources.length; i++) {
                                            var r = success.form.resources[i];
                                            if (r.name == "form-description") {
                                                return r.content;
                                            }
                                        }
                                    }();

                                    console.log("Form description", formDesc);

                                    ctrl.templateId = success.form.templateId;
                                    AppConfig.templateId = ctrl.templateId;

                                    if (!formDesc) {
                                        console.error("Could not find form-description for form " + AppConfig.formName + ":" + AppConfig.formVersion);
                                    } else {
                                        ctrl.valueModel = {};
                                        ctrl.formModel = thinkehr.f4.parseFormDescription(ctrl.ehrContext, formDesc, ctrl.valueModel);
                                    }

                                    return QueryResource.querySaved().$promise;
                                }
                            ).then(
                                function (success) {
                                    ctrl.compositions = success.resultSet;
                                },

                                function (fail) {
                                    console.error("Fail", fail);
                                })
                            ;
                        }
                    }
                });
            }])
        .controller("demographicsController", ["$scope", "$http", "$interval", "AppConfig", "FormResource", "SessionResource",
            function ($scope, $http, $interval, AppConfig, FormResource, SessionResource) {

                /* cache */

                $scope.cache = {};
                $scope.cache.demographics = {};

                /* patient */

                $scope.patient = {};
                $scope.patient.name = "-";
                $scope.patient.sex = "-";
                $scope.patient.age = "-";
                $scope.patient.dob = "-";

                $scope.$watch('searchString', function (tmpStr) {
                    if (!tmpStr || tmpStr.length == 0) {
                        $('.typehead').hide();
                        return 0;
                    }            // if searchStr is still the same..
                    // go ahead and retrieve the data
                    if (tmpStr === $scope.searchString) {
                        var term = $scope.searchString;

                        if (term in $scope.cache.demographics) {  //cache
                            $scope.party = $scope.cache.demographics[term];
                            $('.typehead').show();
                            return;
                        }

                        var login = SessionResource.login();
                        login.$promise.then(function (success) {
                            //console.log("Login", success);
                            AppConfig.sessionId = success.sessionId;

                            // Ping the session every 5 minutes
                            $scope.sessionPing = $interval(function () {
                                SessionResource.ping({},
                                    function (success) {
                                        //console.info("Successfully pinged ehr session", AppConfig.sessionId);
                                    },
                                    function (failure) {
                                        //console.error("Failed to ping ehr session", AppConfig.sessionId, " please reload page to create a new session.", failure);
                                    }
                                );
                            }, 300000, 0, false);

                            return FormResource.list().$promise;
                        }).then(
                            function () {
                                $http({
                                    url: AppConfig.url + "/demographics/party/query",
                                    method: "GET",
                                    params: {search: "*" + term + "*"},
                                    headers: {
                                        "Ehr-Session": AppConfig.sessionId
                                    }
                                }).success(function (data) {
                                    $scope.party = data.parties;
                                    $scope.cache.demographics[term] = $scope.party;
                                    $('.typehead').show();
                                });
                            }
                        );
                    }
                });

                $scope.selectParty = function (patient) {

                    $scope.patient = patient;

                    $scope.patient.name = patient.firstNames + " " + patient.lastNames;
                    $scope.patient.sex = ehrscapeDemo.capitaliseFirstLetter(patient.gender.toLowerCase());
                    $scope.patient.age = ehrscapeDemo.getAge(ehrscapeDemo.formatDateUS(patient.dateOfBirth));
                    $scope.patient.dob = ehrscapeDemo.formatDate(patient.dateOfBirth);

                    $http({
                        url: AppConfig.url + "/ehr",
                        method: "GET",
                        params: {
                            subjectId: patient.id,
                            subjectNamespace: 'ehscape'
                        },
                        headers: {
                            "Ehr-Session": AppConfig.sessionId
                        }
                    }).success(function (data) {
                        if (data) {
                            $scope.patient.ehrId = data.ehrId;
                            AppConfig.ehrId = $scope.patient.ehrId;
                        }
                        else {
                            console.log("ehrId not found!");
                        }

                    });

                    $('.search-query').val('');
                    $('.typehead').hide();

                };

                $scope.buildSuggestion = function (party) {
                    return party.firstNames + " " + party.lastNames + " (ID: " + party.id + ")";
                }
            }])
        .filter('highlight', function ($sce) {
            return function (text, phrase) {
                //if (phrase) text = text.replace(new RegExp('('+phrase+')', 'gi'),
                //  '<span class="highlighted">$1</span>');

                if (phrase) text = text.replace(new RegExp("(?![^&;]+;)(?!<[^<>]*)(" + phrase + ")(?![^<>]*>)(?![^&;]+;)", "gi"), '<span class="highlighted">$1</span>');

                return $sce.trustAsHtml(text)
            }
        })
        .directive('prettySelect', function () {
            return function (scope, element, attrs) {
                setTimeout(function () {
                    scope.$emit('selectPretty', element, attrs);
                }, 10);
            };
        })
        .directive('prettySelectForm', function ($timeout, $parse) {
            return {
                require: 'ngModel',
                link: function ($scope, element, $attrs, ngModel) {
                    return $timeout(function () {

                        $scope.$emit('selectPretty', element, $attrs);

                        $scope.$watch(function () {
                            return ngModel.$modelValue;
                        }, function(newValue) {
                            $(element).selecter("refresh");
                        });
                    });
                }
            };
        })
        .directive('iCheck', function ($timeout, $parse) {
            return {
                require: 'ngModel',
                link: function ($scope, element, $attrs, ngModel) {
                    return $timeout(function () {
                        var value;
                        value = $attrs['value'];

                        $scope.$watch($attrs['ngModel'], function (newValue) {
                            $(element).iCheck('update');
                        });

                        return $(element).iCheck({
                            checkboxClass: 'icheckbox_flat',
                            radioClass: 'iradio_flat',
                            increaseArea: '20%'
                        }).on('ifChanged', function (event) {
                            if ($(element).attr('type') === 'checkbox' && $attrs['ngModel']) {
                                $scope.$apply(function () {
                                    return ngModel.$setViewValue(event.target.checked);
                                });
                            }
                            if ($(element).attr('type') === 'radio' && $attrs['ngModel']) {
                                return $scope.$apply(function () {
                                    return ngModel.$setViewValue(value);
                                });
                            }
                        });
                    });
                }
            };
        });
})();