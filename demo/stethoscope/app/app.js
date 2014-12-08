(function stethoscopeDemoNamespace() {

    var app = angular.module("digiscopeApp", ["ngResource", 'dropzone'])
        .factory("AppConfig", function () {
            return {
                url: "https://rest.ehrscape.com/rest/v1",
                username: "guidemo",
                password: "gui?!demo123",
                subjectNamespace: "ehscape",
                templateId: "Digiscope",
                storeURL: "https://rest.ehrscape.com/store/rest/store/form"
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
                        params: {format: "FLAT"},
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
                        params: {format: "FLAT"},
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
            return function(ehrId){
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
                                        ehrId +
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
            }
        }])
        .factory("TemplateResource", ["$resource", "AppConfig", function ($resource, AppConfig) {
            return $resource(
                AppConfig.url + "/template/" + AppConfig.templateId,
                {}, // Defaults,
                // Actions
                {
                    load: {
                        method: "GET",
                        headers: {
                            "Ehr-Session": function () {
                                return AppConfig.sessionId;
                            }
                        }
                    }
                }
            );
        }])
        .controller('medController', ["$scope", "$http", "$sce", "$compile", "$interval", "$timeout", "AppConfig", "FormResource", "SessionResource", "CompositionResource", "QueryResource", "TemplateResource",
            function ($scope, $http, $sce, $compile, $interval, $timeout, AppConfig, FormResource, SessionResource, CompositionResource, QueryResource, TemplateResource) {

                this.AppConfig = AppConfig;
                var ctrl = this;

                $timeout(function(){
                    $('.digiscope-content').addClass("durationAnim");
                }, 1000);

                $scope.$on('selectPretty', function (scope, element, attrs) {

                    $(element).selecter();

                });

                /* cache */

                $scope.cache = {};
                $scope.cache.demographics = {};

                /* Demographics */

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

                    ctrl.getEhrID(patient.id, AppConfig.subjectNamespace);

                    $('.search-query').val('');
                    $('.typehead').hide();

                };

                $scope.buildSuggestion = function (party) {
                    return party.firstNames + " " + party.lastNames + " (ID: " + party.id + ")";
                };

                /* Digiscope */

                this.buildMHForm = function(){

                    ctrl._auxHTML = '';
                    ctrl.MHFormHTML = [];

                    var list = ctrl.templateTree.children;

                    for(var i=0; i<list.length; i++){

                        if(list[i].localizedName){

                            ctrl._auxHTML = "<div class='form-wrapper'>";

                            if(list[i].id != "examination_findings"){

                                ctrl._auxHTML += "<div class='form-title'><i class='fa fa-angle-down'></i><span>" + list[i].localizedName + "</span></div>";

                                ctrl.goThroughTree(list[i]);

                                ctrl._auxHTML += "</div>";

                                ctrl.MHFormHTML.push(ctrl._auxHTML);

                            }
                            else{

                                ctrl._auxHTML += "<div class='form-title'><i class='fa fa-angle-down'></i><span>Chest Auscultation of the chest</span></div>";

                                ctrl.goThroughTree(list[i]);

                                ctrl._auxHTML += "</div>";

                                $scope.htmlFindings = ctrl._auxHTML;

                            }
                        }
                    }

                    var html = '';
                    for(var k=0; k<ctrl.MHFormHTML.length; k++){

                        if(k % 2 == 0) html += '<div class="row">';

                        html += '<div class="col-sm-6">' + ctrl.MHFormHTML[k] + '</div>';

                        if(k % 2 != 0) html += '</div>';

                    }

                    $scope.htmlMH = html;

                };

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

                this.getEhrID = function(patientID, namespace){

                    $http({
                        url: AppConfig.url + "/ehr",
                        method: "GET",
                        params: {
                            subjectId: patientID,
                            subjectNamespace: namespace
                        },
                        headers: {
                            "Ehr-Session": AppConfig.sessionId
                        }
                    }).success(function (data) {
                        if (data) {
                            $scope.patient.ehrId = data.ehrId;
                        }
                        else {
                            (namespace == "ehscape") ? ctrl.getEhrID(patientID, 'guidemo') : console.log("ehrId not found!");
                        }

                    });

                };

                this.goThroughTree = function(field){

                        if(field.children){

                            var children = field.children;

                            for(var i=0; i<children.length; i++){
                                if(children[i].localizedName){
                                    ctrl.goThroughTree(children[i]);
                                }
                            }
                        }
                        else{
                            if(field.localizedName){
                                ctrl._auxHTML += "<div class='row'><div class='col-sm-4 right-align'><div class='form-label'>" + field.localizedName + ":</div></div>";

                                ctrl._auxHTML += ctrl.setInputs(field);

                                ctrl._auxHTML += "</div>";
                            }
                        }
                };

                this.prepareComposition = function(){

                    var medicalHistory = {
                        "ctx/language": "en",
                        "ctx/territory": "SI",
                        "digiscope/body_weight:0/any_event:0/weight|magnitude": $scope.ehr_weight,
                        "digiscope/body_weight:0/any_event:0/weight|unit": $scope.ehr_weight_units,
                        "digiscope/height_length:0/any_event:0/height_length|magnitude": $scope.ehr_height_length,
                        "digiscope/height_length:0/any_event:0/height_length|unit": $scope.ehr_height_length_units,
                        "digiscope/blood_pressure:0/any_event:0/systolic|magnitude": $scope.ehr_systolic,
                        "digiscope/blood_pressure:0/any_event:0/systolic|unit": $scope.ehr_systolic_units,
                        "digiscope/blood_pressure:0/any_event:0/diastolic|magnitude": $scope.ehr_diastolic,
                        "digiscope/blood_pressure:0/any_event:0/diastolic|unit": $scope.ehr_diastolic_units,
                        "digiscope/indirect_oximetry:0/spo2|numerator": $scope.ehr_spo2_num,
                        "digiscope/indirect_oximetry:0/spo2|denominator": $scope.ehr_spo2_den
                    };

                    var i= 0, examinations = {};

                    for(var key in $scope.examinations) {
                        if($scope.examinations.hasOwnProperty(key)) {

                            if($scope.examinations[key]['isUsed']){

                                examinations["digiscope/examination_findings:" + i + "/auscultation_of_the_chest:0/normal_statements/normal_statement:0|code"] = $scope.examinations[key]['ehr_normal_statement'];

                                if($scope.examinations[key]['ehr_phonogram'] && Object.keys($scope.examinations[key]['ehr_phonogram']).length>0){

                                    examinations["digiscope/examination_findings:" + i + "/auscultation_of_the_chest:0/findings/heart_sounds_and_bruits/phonogram"] = $scope.examinations[key]['ehr_phonogram']['href'];
                                    examinations["digiscope/examination_findings:" + i + "/auscultation_of_the_chest:0/findings/heart_sounds_and_bruits/phonogram|mediatype"] = $scope.examinations[key]['ehr_phonogram']['mimeType'];
                                    examinations["digiscope/examination_findings:" + i + "/auscultation_of_the_chest:0/findings/heart_sounds_and_bruits/phonogram|alternatetext"] = $scope.examinations[key]['ehr_phonogram']['fileName'];
                                }

                                examinations["digiscope/examination_findings:" + i + "/auscultation_of_the_chest:0/findings/lung/area/region|code"] = $scope.examinations[key]['ehr_region'];
                                examinations["digiscope/examination_findings:" + i + "/auscultation_of_the_chest:0/findings/lung/area/phase_of_respiratory_cycle|code"] = $scope.examinations[key]['ehr_phase_of_respiratory_cycle'];
                                examinations["digiscope/examination_findings:" + i + "/auscultation_of_the_chest:0/findings/lung/area/findings"] = $scope.examinations[key]['ehr_findings'];
                                examinations["digiscope/examination_findings:" + i + "/auscultation_of_the_chest:0/findings/lung/clinical_description"] = $scope.examinations[key]['ehr_clinical_description'];
                                examinations["digiscope/examination_findings:" + i + "/interpretation:0"] = $scope.examinations[key]['ehr_interpretation'];

                                i++;
                            }

                        }
                    }

                    var compositionData = $.extend({}, medicalHistory, examinations);

                    for (var k in compositionData) {
                        if (compositionData[k] === null || compositionData[k] === undefined) {
                            delete compositionData[k];
                        }
                    }

                    return compositionData;

                };

                this.refreshValues = function(composition){

                    $scope.ehr_weight = composition["digiscope/body_weight:0/any_event:0/weight|magnitude"] || "";
                    $scope.ehr_weight_units = composition["digiscope/body_weight:0/any_event:0/weight|unit"] || "";
                    $scope.ehr_height_length = composition["digiscope/height_length:0/any_event:0/height_length|magnitude"] || "";
                    $scope.ehr_height_length_units = composition["digiscope/height_length:0/any_event:0/height_length|unit"] || "";
                    $scope.ehr_systolic  = composition["digiscope/blood_pressure:0/any_event:0/systolic|magnitude"] || "";
                    $scope.ehr_systolic_units = composition["digiscope/blood_pressure:0/any_event:0/systolic|unit"] || "";
                    $scope.ehr_diastolic = composition["digiscope/blood_pressure:0/any_event:0/diastolic|magnitude"] || "";
                    $scope.ehr_diastolic_units = composition["digiscope/blood_pressure:0/any_event:0/diastolic|unit"] || "";
                    $scope.ehr_spo2_num = composition["digiscope/indirect_oximetry:0/spo2|numerator"] || "";
                    $scope.ehr_spo2_den = composition["digiscope/indirect_oximetry:0/spo2|denominator"] || "";

                    var i = 0, loop = true;

                    while(loop){
                        if(composition["digiscope/examination_findings:" + i + "/auscultation_of_the_chest:0/findings/lung/area/region|code"]){

                            var area, code = composition["digiscope/examination_findings:" + i + "/auscultation_of_the_chest:0/findings/lung/area/region|code"];

                            for(var key in $scope.examinations) {
                                if($scope.examinations.hasOwnProperty(key)) {
                                    if($scope.examinations[key]['ehr_region'] == code){
                                        area = key;
                                        break;
                                    }
                                }
                            }

                            $scope.examinations[area]['ehr_normal_statement'] = composition["digiscope/examination_findings:" + i + "/auscultation_of_the_chest:0/normal_statements/normal_statement:0|code"] || "";
                            $scope.examinations[area]['ehr_phonogram'] = {};
                            $scope.examinations[area]['ehr_phonogram']['href'] = composition["digiscope/examination_findings:" + i + "/auscultation_of_the_chest:0/findings/heart_sounds_and_bruits/phonogram"] || "//:0";
                            $scope.examinations[area]['ehr_phonogram']['mimeType'] = composition["digiscope/examination_findings:" + i + "/auscultation_of_the_chest:0/findings/heart_sounds_and_bruits/phonogram|mediatype"] || "";
                            $scope.examinations[area]['ehr_phonogram']['fileName'] = composition["digiscope/examination_findings:" + i + "/auscultation_of_the_chest:0/findings/heart_sounds_and_bruits/phonogram|alternatetext"] || "";
                            $scope.examinations[area]['ehr_region'] = composition["digiscope/examination_findings:" + i + "/auscultation_of_the_chest:0/findings/lung/area/region|code"];
                            $scope.examinations[area]['ehr_phase_of_respiratory_cycle'] = composition["digiscope/examination_findings:" + i + "/auscultation_of_the_chest:0/findings/lung/area/phase_of_respiratory_cycle|code"] || "";
                            $scope.examinations[area]['ehr_findings'] = composition["digiscope/examination_findings:" + i + "/auscultation_of_the_chest:0/findings/lung/area/findings"] || "";
                            $scope.examinations[area]['ehr_clinical_description'] = composition["digiscope/examination_findings:" + i + "/auscultation_of_the_chest:0/findings/lung/clinical_description"] || "";
                            $scope.examinations[area]['ehr_interpretation'] = composition["digiscope/examination_findings:" + i + "/interpretation:0"] || "";

                            $scope.examinations[area]["isUsed"] = true;

                            i++;
                        }
                        else loop = false;
                    }

                };

                this.setInputs = function(el){

                    var html = '', id = "ehr_" + el.id, type = el.rmType;

                    switch(type){

                        case "DV_CODED_TEXT":

                            html += "<div class='col-sm-8'>";
                            (el.id != "region")? html += "<select ng-model=examinations[activeArea]['" + id + "'] i-selecter>":
                                html += "<select ng-model=examinations[activeArea]['" + id + "'] i-selecter disabled='disabled'>";
                            html += "<option value=''></option>";

                            for(var i=0; i<el.inputs[0].list.length; i++){
                                html += "<option value='" + el.inputs[0].list[i].value + "'>"+ el.inputs[0].list[i].label + "</option>";
                            }

                            html += "</select>";
                            html += "</div>";

                            break;

                        case "DV_DATE_TIME":

                            html += "<div class='col-sm-4'>";
                            html += "<input id='" + id + "' type='text' class='form-control datetimepicker' />";
                            html += "</div>";

                            break;

                        case "DV_MULTIMEDIA":

                            html += "<div class='col-sm-8'>";
                            html += "<audio ng-src='{{currentAudio | trusted}}' controls></audio><br>";
                            html += "<form ng-model=examinations[Aortic]['" + id + "'] ng-show=activeArea=='Aortic' class='dropzone' dropzone='dropzoneConfig'></form>";
                            html += "<form ng-model=examinations[Tricuspid]['" + id + "'] ng-show=activeArea=='Tricuspid' class='dropzone' dropzone='dropzoneConfig'></form>";
                            html += "<form ng-model=examinations[Mitral]['" + id + "'] ng-show=activeArea=='Mitral' class='dropzone' dropzone='dropzoneConfig'></form>";
                            html += "<form ng-model=examinations[Pulmonary]['" + id + "'] ng-show=activeArea=='Pulmonary' class='dropzone' dropzone='dropzoneConfig'></form>";
                            html += "</div>";
                            break;

                        case "DV_PROPORTION":

                            html += "<div class='col-sm-4'>";
                            html += "<input ng-model='" + id + "_num' type='number' class='form-control' />";
                            html += "</div>";
                            
                            var percent = false;
                            if (el.inputs.length > 1) {
                                var den = el.inputs[1];
                                if (den.validation && den.validation.range && den.validation.range.min && den.validation.range.max &&
                                   den.validation.range.min === 100.0 && den.validation.range.max === 100.0) {
                                    percent = true;
                                }
                            }
                                    
                            html += "<div class='col-sm-4'>";
                            if (percent) {
                                html += "<div class='form-label'>%</div>";
                            } else {
                                html += "<input ng-model='" + id + "_den' type='number' class='form-control' />";
                            }
                            html += "</div>";
                            break;

                        case "DV_TEXT":

                            for (var i=0; i< el.inputs.length; i++){
                                html += "<div class='col-sm-8'>";
                                html += "<input ng-model=examinations[activeArea]['" + id + "'] type='" + el.inputs[i].type.toLowerCase() + "' class='form-control' />";
                                html += "</div>";
                            }
                            break;

                        case "DV_QUANTITY":

                            html += "<div class='col-sm-4'>";
                            html += "<input ng-model='" + id + "' type='number' class='form-control' />";
                            html += "</div>";

                            html += "<div class='col-sm-4'>";
                            html += "<select class='combo-quantity' ng-model='" + id + "_units' i-selecter><option value=''></option>";

                            for(var i=0; i<el.inputs[1].list.length; i++){
                                html += "<option value='" + el.inputs[1].list[i].value + "'>"+ el.inputs[1].list[i].label + "</option>";
                            }

                            html += "</select>";
                            html += "</div>";

                            break;
                    }

                    return html;

                };

                $scope.examinations = {
                    Aortic: {
                        "ehr_region": "at0.84",
                        "isUsed": false
                    },
                    Tricuspid: {
                        "ehr_region": "at0.85",
                        "isUsed": false
                    },
                    Mitral: {
                        "ehr_region": "at0.86",
                        "isUsed": false
                    },
                    Pulmonary: {
                        "ehr_region": "at0.87",
                        "isUsed": false
                    }
                };

                for(var key in $scope.examinations) {
                    if($scope.examinations.hasOwnProperty(key)) {
                        $scope.$watch("examinations."+ key + ".isUsed", function() {

                            //Paint Area

                            var area = this.exp.split('.')[1], areaEl = $("area[title='" + area + "']"), color;

                            ($scope.examinations[area]["isUsed"]) ? color = "DA4453" : color = "F6BB42";

                            var data = areaEl.data('maphilight') || {};
                            data.fillColor = color; // Sample color
                            data.strokeColor = 'E6E9ED';

                            areaEl.data('maphilight', data).trigger('alwaysOn.maphilight');

                        }, true);
                    }
                }

                $scope.dropzoneConfig = {
                    'options': { // passed into the Dropzone constructor
                        'url' : AppConfig.storeURL,
                        'addRemoveLinks': true,
                        'maxFiles': 1,
                        'dictDefaultMessage': "Drop file (or click) here to upload<div class='dz-icon'><i class='fa fa-2x fa-file-o'></i></div>"
                    },
                    'eventHandlers': {
                        'removedfile': function () {
                            $scope.examinations[$scope.activeArea]['ehr_phonogram'] = {};

                            $scope.currentAudio = "//:0";

                            $scope.$apply(); //this triggers a $digest

                        },
                        'success': function (file, response) {
                            $scope.examinations[$scope.activeArea]['ehr_phonogram'] = response[0];

                            $scope.currentAudio = $scope.examinations[$scope.activeArea]['ehr_phonogram']['href'];

                            $scope.$apply(); //this triggers a $digest
                        }
                    }
                };

                $scope.$watch('patient.ehrId', function(value){

                    if(value) {

                        TemplateResource.load(
                            function (success) {

                                ctrl.templateTree = success.webTemplate.tree;

                                ctrl.buildMHForm();
                            }
                        );

                        QueryResource($scope.patient.ehrId).querySaved().$promise.then(
                            function (success) {
                                $scope.compositions = success.resultSet;
                            },

                            function (fail) {
                                console.log("Fail", fail);
                            });

                    }

                });

                $scope.openFindings = function(event){

                    $scope.activeArea = event.target.title;

                    if($scope.examinations[$scope.activeArea]['ehr_phonogram']) {
                        $scope.currentAudio = $scope.examinations[$scope.activeArea]['ehr_phonogram']['href'] || "//:0";
                    }
                    else{
                        $scope.currentAudio = "//:0";
                    }

                    $('.cbp-spmenu-right').addClass('cbp-spmenu-open');

                    var contentTr = angular.element('<div class="overlay" ng-click="closeFindings()"></div>');
                    contentTr.insertAfter('body');
                    $compile(contentTr)($scope);

                };

                $scope.closeFindings = function(){

                    $(".jPushMenuBtn, body, .cbp-spmenu").removeClass('disabled active cbp-spmenu-open cbp-spmenu-push-toleft cbp-spmenu-push-toright');
                    $(".overlay").remove();

                    if($scope.examinations[$scope.activeArea]["isUsed"]){
                        $scope.examinations[$scope.activeArea]["isUsed"] = false;
                    }

                    $timeout(function() {  //ugly (force $watch)
                        $scope.examinations[$scope.activeArea]["isUsed"] = true;
                    });

                    $('.area').removeClass('selectedArea');

                };

                $scope.loadComposition = function ($event) {
                    $event.preventDefault();
                    var compositionUid = $($event.target).attr("data-composition-uid");
                    if (compositionUid) {
                        CompositionResource.load({uid: compositionUid},
                            function (success) {

                                $scope.examinations["Aortic"]["isUsed"] = false;
                                $scope.examinations["Tricuspid"]["isUsed"] = false;
                                $scope.examinations["Mitral"]["isUsed"] = false;
                                $scope.examinations["Pulmonary"]["isUsed"] = false;

                                ctrl.refreshValues(success.composition);  //loadFormData

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

                $scope.saveComposition = function(){
                    var cr = new CompositionResource( ctrl.prepareComposition() );
                    cr.$add({templateId: AppConfig.templateId, ehrId: $scope.patient.ehrId},
                        function (success) {

                            var h = success.meta.href;
                            var parsedHref = h.split("/");
                            ctrl.lastSaved = parsedHref[parsedHref.length - 1];

                            if (!$scope.compositions) {
                                $scope.compositions = [];
                            }

                            var newComp = {
                                startTime: new Date(),
                                uid: ctrl.lastSaved,
                                templateId: AppConfig.templateId
                            };

                            if ($scope.compositions.length > 9) {
                                $scope.compositions.pop();
                            }

                            $scope.compositions.unshift(newComp);

                            var opts = {
                                icon: "check",
                                title: "Success!",
                                message: "Composition has been saved.",
                                type: "success"
                            };

                            ctrl.createGrowl(opts);

                        },
                        function (error) {

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
        .directive('iSelecter', function ($timeout, $parse) {
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

                        if(element.hasClass( 'combo-quantity' )) $scope[$attrs['ngModel']] = $attrs.$$element[0][1].value;

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
        })
        .directive('dynamic', function ($compile) {
            return {
                restrict: 'A',
                replace: true,
                link: function (scope, ele, attrs) {
                    scope.$watch(attrs.dynamic, function(html) {
                        ele.html(html);
                        $compile(ele.contents())(scope);
                    });
                }
            };
        })
        .filter('trusted', ['$sce', function ($sce) {
            return function(url) {
                return $sce.trustAsResourceUrl(url);
            };
        }]);
})();

//Dropzone module

angular.module('dropzone', []).directive('dropzone', function () {
    return function (scope, element, attrs) {
        var config, dropzone;

        config = scope[attrs.dropzone];

        // create a Dropzone for the element with the given options
        dropzone = new Dropzone(element[0], config.options);

        // bind the given event handlers
        angular.forEach(config.eventHandlers, function (handler, event) {
            dropzone.on(event, handler);
        });
    };
});
