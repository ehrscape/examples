/**
 * Created by LeandroG on 21.5.2014.
 */


(function ( $, window, document, undefined ) {

    var pluginName = "flipRecords",
        defaults = {
            baseUrl: "https://rest.ehrscape.com/rest/v1",
            ehrId: "6f81d77a-26ef-4cf4-926f-40ccfafd8a1f",
            username: "guidemo",
            password: "gui?!demo123"
        };

    function Plugin( element, options ) {
        this.element = element;
        this.$element = $(this.element);

        this.options = $.extend( {}, defaults, options) ;

        this._defaults = defaults;
        this._name = pluginName;

        this.init();
    }

    Plugin.prototype = {

        init: function() {

            this.getDemographics();

        },

        //Internal helper functions

        _capitaliseFirstLetter: function(string){

            return string.charAt(0).toUpperCase() + string.slice(1);

        },

        _dynamicSort: function(property) {
            var sortOrder = 1;
            if(property[0] === "-") {
                sortOrder = -1;
                property = property.substr(1);
            }
            return function (a,b) {
                var result = (a.metadata[property] < b.metadata[property]) ? -1 : (a.metadata[property] > b.metadata[property]) ? 1 : 0;
                return result * sortOrder;
            }
        },

        _dynamicSortMultiple: function() {
            /*
             * save the arguments object as it will be overwritten
             * note that arguments object is an array-like object
             * consisting of the names of the properties to sort by
             */
            var self = this,props = arguments;
            return function (obj1, obj2) {
                var i = 0, result = 0, numberOfProperties = props.length;
                /* try getting a different result from 0 (equal)
                 * as long as we have extra properties to compare
                 */
                while(result === 0 && i < numberOfProperties) {
                    result = self._dynamicSort(props[i])(obj1, obj2);
                    i++;
                }
                return result;
            }
        },

        _formatDate: function(date, completeDate){

            if(date){
                var d = new Date(date);

                var curr_date = d.getDate();
                curr_date = this._normalizeDate(curr_date);

                var curr_month = d.getMonth();

                var curr_year = d.getFullYear();

                var curr_hour = d.getHours();
                curr_hour = this._normalizeDate(curr_hour);

                var curr_min = d.getMinutes();
                curr_min = this._normalizeDate(curr_min);

                var curr_sec = d.getSeconds();
                curr_sec = this._normalizeDate(curr_sec);

                var dateString, monthNames = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];

                if (completeDate){
                    dateString = curr_date + "-" + monthNames[curr_month] + "-" + curr_year + " at " + curr_hour + ":" + curr_min; // + ":" + curr_sec;
                }
                else dateString = curr_date + "-" + monthNames[curr_month] + "-" + curr_year;
            }
            else{
                dateString = " - ";
            }

            return dateString;

        },

        _formatDateUS: function(date) {
            var d = new Date(date);

            var curr_date = d.getDate();
            curr_date = this._normalizeDate(curr_date);

            var curr_month = d.getMonth();
            curr_month++;
            curr_month = this._normalizeDate(curr_month);

            var curr_year = d.getFullYear();

            return curr_month + "-" + curr_date + "-" + curr_year;

        },

        _getFullAge: function(dateString) {

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

            /*if ((age.years > 0) && (age.months > 0) && (age.days > 0))
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
            else ageString = "Oops! Could not calculate age!";*/

            //return ageString;
            return age.years + yearString;
        },

        _normalizeDate: function(el){

            el = el + "";

            if (el.length == 1){
                el = "0" + el;
            }

            return el;
        },

        //Ehrscape session management

        _getSessionId: function() {
            var response = $.ajax({
                type: "POST",
                url: this.options.baseUrl + "/session?username=" + encodeURIComponent(this.options.username) +
                    "&password=" + encodeURIComponent(this.options.password),
                async: false
            });
            return response.responseJSON.sessionId;
        },

        _closeSession: function(sessionId) {
            $.ajax({
                type: "DELETE",
                url: this.options.baseUrl + "/session",
                headers: {
                    "Ehr-Session": sessionId
                }
            });
        },

        //Main functions

        contentHTML: function(data){

            this.details += '<ul>';
            for (var i=0; i<data.length; i++){

                if(data[i].displayValue){

                    var value = data[i].displayValue;
                    if(data[i].label.toLowerCase().indexOf( "time") != -1 || data[i].label.toLowerCase().indexOf( "date") != -1 || data[i].label.toLowerCase().indexOf( "recorded") != -1){
                        value = this._formatDate(value, true);
                    }

                    var label = this._capitaliseFirstLetter(data[i].label);
                    if(label == "Result value") {
                        var normal = this.labResults(data[i].rawValue); // Lab results

                        if(normal.status == "normal"){
                            this.details += '<li class="paragraph-label">' + label + ': <span class="' + normal.className + '">' + value + '</span></li>';
                        }
                        else{
                            this.details += '<li class="paragraph-label">' + label + ': <span class="' + normal.className + '">' + value + '<i class=" fa fa-chevron-circle-' + normal.status + '"></i></span></li>';
                        }
                    }
                    else{
                        this.details += '<li class="paragraph-label">' + label + ': <span>' + value + '</span></li>';
                    }
                }
                else{

                    if(data[i].children){

                        this.details += '<li>' + this._capitaliseFirstLetter(data[i].label) + '</li>';

                        this.contentHTML(data[i].children);

                        this.details += '</ul>';

                    }

                }

            }

        },

        createBook: function(){

            var html = "", self = this, data = this.data, numberRecords = this.data.length;

            for(var i=0; i<numberRecords; i++){

                if(i%2 != 0){

                    var newPage = Handlebars.compile( $("#pagemarkup-template").html() );

                    var context;

                    context = {
                        demographics: self.demographics,
                        title_left: data[i-1].metadata.name,
                        date_left: self._formatDate(data[i-1].metadata.startTime, false),
                        content_left: self.render(data[i-1].composition),
                        title_right: data[i].metadata.name,
                        date_right: self._formatDate(data[i].metadata.startTime, false),
                        content_right: self.render(data[i].composition)
                    };

                    html += newPage(context);

                }
            }

            if(numberRecords%2 != 0){ //last page ("odd")
                var lastPage = Handlebars.compile( $("#pagemarkup-template").html() );

                var lastContext = {
                    demographics: self.demographics,
                    title_left: data[numberRecords-1].metadata.name,
                    date_left: self._formatDate(data[numberRecords-1].metadata.startTime, false),
                    content_left: self.render(data[numberRecords-1].composition),
                    title_right: "",
                    date_right: "",
                    content_right: ""
                };

                html += lastPage(lastContext);
            }

            this.print(html);

        },

        createMenuToc: function(){

            var data = this.data, numberPages = this.data.length, self = this;

            for(var i=0; i<numberPages; i++){

                var type = data[i].metadata.name;
                if(type == "Allergies")
                    $('.pushy > ul').append('<li data-category="' + type + '"><a href="#">' + type +'</a></li>');
                else
                    $('.pushy > ul').append('<li data-category="' + type + '"><a href="#">' + type + '<span>' + self._formatDate(data[i].metadata.startTime, false) + '</span></a></li>');

            }

            Pushy.display();

            $('.pushy > ul > li').on('click', function() {

                var $el = $( this ),
                    idx = $el.index();

                Pushy.close();

                self.$element.bookblock('jump', Math.floor(idx/2) + 3 );

                return false;

            } );

        },

        filterRecords: function(category){

            var checkCat, index = 0, jumpPage = false;

            $( ".pushy > ul > li" ).each(function( i ) {

                checkCat = false;

                var value = $(this).attr('data-category');

                if(jumpPage == false){   //get page to "jump"
                    if(category == value){
                        index = i;
                        jumpPage = true;
                    }
                }

                /*if(jQuery.inArray(value, selectedCats)!==-1){  //filter menu
                 checkCat = true;
                 }

                 if(checkCat == true) $(this).css("display", "list-item");
                 else $(this).css("display", "none");*/

            });

            this.$element.bookblock('jump', Math.floor(index/2) + 3 );

        },

        getCategories: function(){

            var data = this.data, stringCat = [];
            for (var i=0; i<data.length; i++){
                stringCat.push(data[i].metadata.name);
            }

            var uniqueCat = [];

            $.each(stringCat, function(i, el){
                if($.inArray(el, uniqueCat) === -1) uniqueCat.push(el);
            });

            this.initPage(uniqueCat);

        },

        getDemographics: function(){

            var self = this;
            var sessionId = self._getSessionId();

            return $.ajax({
                    url: self.options.baseUrl + "/demographics/ehr/" + self.options.ehrId + "/party",
                    type: 'GET',
                    headers: {
                        "Ehr-Session": sessionId
                    },
                    success: function (data) {
                        var party = data.party;

                        // Name
                        var patientName = party.firstNames + ' ' + party.lastNames;

                        // Gender
                        var gender = party.gender;
                        //var patientGender = gender.substring(0, 1) + gender.substring(1).toLowerCase();
                        var patientGender = gender.toLowerCase();


                        // Complete age
                        var patientAge = self._getFullAge(self._formatDateUS(party.dateOfBirth));

                        self.demographics = patientName + " (" + patientGender + ", " + patientAge + ")";

                        self._closeSession(sessionId);

                        self.getDocs();

                    }
            });
        },

        getDocs: function(){

            var self = this;
            var sessionId = self._getSessionId();

            return $.ajax({
                type: "POST",
                url: this.options.baseUrl + "/presentation",
                data: JSON.stringify({
                    queryRequestData: {
                        aql: "SELECT c FROM EHR[ehr_id/value='" + this.options.ehrId + "'] CONTAINS COMPOSITION c ORDER BY c/context/start_time DESC FETCH 20"
                    }
                }),
                contentType: 'application/json',
                headers: {
                    "Ehr-Session": sessionId
                },
                success: function (docs) {

                    self.data = docs.sort( self._dynamicSortMultiple("name", "-startTime") ); //sorting

                    self.data.push( self.data.shift() ); //allergies in the end

                    self.getCategories();

                    self._closeSession(sessionId);
                }
            });
        },

        initPage: function(categories){

            var self = this, el = $('#catList');

            for (var i=0; i<categories.length; i++){
                el.append('<div class="box"><span>'+ categories[i]+'</span></div>');
            }

            $('.box').on("click touchstart", function(){

                var el = $(this)[0];
                var text = ('innerText' in el) ? el.innerText : el.textContent;

                self.filterRecords(text);

                return false;
            });

            this.createBook();

            this.createMenuToc();

        },

        labResults: function(data){
            var obj = {};

            if (data.normal_status.code_string == "N"){
                obj.className = "success";
                obj.status = "normal"
            }
            else{
                if(data.normal_status.code_string == "H"){
                    obj.status = "up";
                }
                else{
                    obj.status = "down";
                }
                obj.className = "danger";
            }

            return obj;
        },

        print: function(pages){

            this.$element.append( pages );

            this.setContentStyle();

            Page.init();

        },

        render: function(data){

            this.details = '<div class="content-wrapper">';

            if(data.children) this.contentHTML(data.children);

            this.details += '</div>';

            return this.details;
        },

        setContentStyle: function(){

            $( ".bb-renderContent ul li" ).each(function() {
                if($(this).next().length > 0){
                    if( $(this).next().get(0).tagName == 'UL' ){
                        $(this).addClass('header');
                    }
                }
            });

        }
    };

    $.fn[pluginName] = function ( options ) {
        return this.each(function () {
            if (!$.data(this, "plugin_" + pluginName)) {
                $.data(this, "plugin_" + pluginName,
                    new Plugin( this, options ));
            }
        });
    };

})( jQuery, window, document );
