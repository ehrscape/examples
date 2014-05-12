/**
 * Created by LeandroG on 7.4.2014.
 */


(function ( $, window, document, undefined ) {

    var pluginName = "ehrscapeTimeline",
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

            this.getDocs();

        },

        _formatDate: function(date, completeDate){

            var d = new Date(date);

            var curr_date = d.getDate();
            curr_date = this._normalizeDate(curr_date);

            var curr_month = d.getMonth();
            curr_month++;
            curr_month = this._normalizeDate(curr_month);

            var curr_year = d.getFullYear();

            var curr_hour = d.getHours();
            curr_hour = this._normalizeDate(curr_hour);

            var curr_min = d.getMinutes();
            curr_min = this._normalizeDate(curr_min);

            var curr_sec = d.getSeconds();
            curr_sec = this._normalizeDate(curr_sec);

            var dateString;
            if (completeDate) dateString = curr_date + "-" + curr_month + "-" + curr_year + " " + curr_hour + ":" + curr_min;// + ":" + curr_sec;
            else dateString = curr_date + "-" + curr_month + "-" + curr_year;

            return dateString;

        },

        _normalizeDate: function(el){

            el = el + "";

            if (el.length == 1){
                el = "0" + el;
            }

            return el;
        },

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

        capitaliseFirstLetter: function(string){

            return string.charAt(0).toUpperCase() + string.slice(1);

        },

        flatData: function(data, parent){

            for (var i=0; i<data.length; i++){

                if(data[i].displayValue && parent != null){

                    if(parent.label.toLocaleLowerCase() != "context"){
                        var value = data[i].displayValue;
                        if(data[i].label.toLowerCase().indexOf("time") != -1 && data[i].label.length < 10){
                            value = this._formatDate(value, true);

                            parent.label += ' (at '+ value +')';

                            data.splice(i, 1);
                        }
                    }

                }
                else{

                    if(data[i].children){

                        this.flatData(data[i].children, data[i]);

                    }

                }

            }

            return data;
        },

        getContent: function(data){

            data = this.flatData(data, null);

            this.details = '';

            this.contentHTML(data);

            return this.details;

        },

        contentHTML: function(data){

            this.details += '<ul>';
            for (var i=0; i<data.length; i++){

                if(data[i].displayValue){

                    var value = data[i].displayValue;
                    if(data[i].label.toLowerCase().indexOf( "time") != -1 || data[i].label.toLowerCase().indexOf( "date") != -1){
                        value = this._formatDate(value, true);
                    }

                    this.details += '<li>' + this.capitaliseFirstLetter(data[i].label) + ": " + value + '</li>';

                }
                else{

                    if(data[i].children){

                        this.details += '<li>' + this.capitaliseFirstLetter(data[i].label) + '</li>';

                        this.contentHTML(data[i].children);

                        this.details += '</ul>';

                    }

                }

            }

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

                    self.renderDocs(docs);

                    self._closeSession(sessionId);
                }
            });
        },

        renderDocs: function(data){

            this.$element.html('<ul class="timeline"></ul>');

            var timelineItem = Handlebars.compile( $("#timeline-entry").html() );

            for(var i=0; i<data.length; i++){

                var title = data[i].metadata.templateId;

                if(title != "Allergies" && data[i].composition.children){

                    var icon, time = data[i].metadata.startTime;

                    this.monthNames = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
                    var date = new Date(time);

                    var dateAbbr = date.getDate() + '-' + this.monthNames[date.getMonth()];

                    switch(title){
                        case "Allergies":
                            icon = "fa-ambulance";
                            break;
                        case "Medical Diagnosis":
                            icon = "fa-stethoscope";
                            break;
                        case "Medications":
                            icon = "fa-medkit";
                            break;
                        case "Vital Signs":
                            icon = "fa-plus-square";
                            break;
                    }

                    var content = this.getContent(data[i].composition.children);

                    var context = {
                        datetime: time,
                        date_complete: this._formatDate(date, true),
                        date_principal: dateAbbr,
                        icon: icon,
                        title: title,
                        composer: data[i].metadata.composer,
                        content: content
                    };

                    var html = timelineItem(context);
                    $('.timeline').append(html);

                    this.setContentStyle($('.timeline').find('.timeline-item:last-child').find('.timeline-content'));

                }
                else{
                    //if(title == "Allergies") console.log(data[i]);
                }
            }

            this.setDetailEvents();

            $('.timeline-content').hide();

            var self = this;

            $('.timeline > li .timeline-label .timeline-title').bind({
                click: function() {

                    var el = $(this);

                    el.toggleClass('collapsed expanded');

                    var content = el.siblings('.timeline-content');

                    content.slideToggle('2000', 'easeInOutCubic');

                    /*var scroll, width = (window.innerWidth > 0) ? window.innerWidth : screen.width;
                    if(width > 755) scroll = 10;
                    else scroll = 70;

                    self.$element.parent().animate({
                        scrollTop: el.offset().top - self.$element.parent().offset().top - scroll
                    }, 1000);*/
                }
            });

            $('.timeline > li .timeline-label .display-item').bind({
                click: function() {
                    var li = $(this).closest('li');
                    li.fadeOut(300, function(){
                        li.remove();
                    });

                }
            });

        },

        setContentStyle: function(el){
            el.find('ul:first').addClass("form-element");

            var self = this;
            el.find("li").each(function() {

                if($(this).next().length > 0){
                    if( $(this).next().get(0).tagName == 'UL' ){
                        self.setStyle(this, true);
                    }
                    else{
                        self.setStyle(this, false);
                    }
                }
                else self.setStyle(this, false);

            });

        },

        setStyle: function(el, isTitle){

            if(isTitle){

                $(el).prepend('<i class="fa fa-angle-down"></i>');
                $(el).addClass('form-header opened');

            }
            else{

                $(el).addClass('form-content');
                var text = $(el).text();
                var index = text.indexOf(': ');
                var title = text.substring(0, index+2);
                var value = text.slice(index+2, text.length);

                $(el).html('<span>'+title+'</span>'+value);
                //else $(el).html('<div class="longtext-title">'+title+'</div><br><div>'+value+'</div>');
            }

        },

        setDetailEvents: function(){

            $('.form-header').bind({
                click: function() {

                    if( $(this).find('i').hasClass('fa-rotate-45') )  $(this).find('i').removeClass('fa-rotate-45');

                    $(this).next().slideToggle('2000', 'easeInOutCubic');

                    $(this).toggleClass("opened closed");

                    $(this).find('i').toggleClass("fa-angle-right fa-angle-down");

                },
                mouseenter: function() {

                    if( $(this).hasClass('closed') ){
                        $(this).find('i').addClass('fa-rotate-45');
                        $(this).find('i').css('color', '#3498DB');
                    }

                },
                mouseleave: function() {

                    if( $(this).hasClass('closed') ){
                        $(this).find('i').removeClass('fa-rotate-45');
                        $(this).find('i').css('color', '#c9cdd7');
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
