/**
 * Created by LeandroG on 10.4.2014.
 */
/**
 * Created by LeandroG on 7.4.2014.
 */


(function ( $, window, document, undefined ) {

    var pluginName = "socialStream",
        defaults = {
            baseUrl: "http://thinkehr2.marand.si:8082/rest/v1",
            storeURL: "https://rest.ehrscape.com/store/rest/store/form",
            ehrId: "e2540dc5-4f68-4813-8ef6-df128a1a639e",
            username: "user",
            password: "pwd"
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

            var self = this;
            self.media = [];

            Dropzone.options.uploader = {
                url : self.options.storeURL,
                addRemoveLinks: true,
                /*headers: {
                    "Ehr-Session": self._getSessionId()
                },*/
                init: function() {
                    this.on("success", function(file, response) {

                        self.media.push(response);

                    });

                    this.on("removedfile", function(file) {

                        for(var i=0; i<self.media.length; i++){
                            if(self.media[i][0].fileName == file.name){
                                self.media.splice(i, 1);
                            }
                        }

                    });

                    var _this = this;

                    $("#submitBt").on("click", function() {
                        _this.removeAllFiles();
                    });
                }
            };

            this.eventListeners();

        },

        bindNewEvents: function(li){

            var self = this;

            li.find('.timeline-title').click(function(){

                var el = $(this);

                el.toggleClass('collapsed expanded');

                var content = el.siblings('.timeline-status, .timeline-comments');

                content.slideToggle('2000', 'easeInOutCubic');

            });

            li.find(".comments-input").keyup(function (e) {
                if (e.keyCode == 13) {

                    var comment = $(this).val();

                    if(comment){

                        var commentHTML = self.buildCommentStr(comment);

                        var commentItem = Handlebars.compile( $("#comment-entry").html() );

                        var context = {
                            datetime: new Date().toISOString(),
                            comment: commentHTML
                        };

                        var html = commentItem(context);

                        var el = $(this).closest('.timeline-commentsInput').siblings('.timeline-commentsList');

                        $(html).hide().appendTo(el).fadeIn();

                        el.find("li:last-child").find("time.timeago").timeago();

                        el.find("li:last-child").find(".user-delete-comment").bind("click", function() {
                            var target = $(this).closest('li');

                            target.fadeOut(300, function(){
                                $(this).remove();
                            });
                        });

                        $(this).val('');

                    }
                }
            });

            li.find('.timeline-item-delete').click(function(){

                var target = $(this).closest('.timeline-item');

                target.fadeOut(300, function(){
                    $(this).remove();
                });

            });

        },

        buildCommentStr: function(comment){

            comment = $.trim(comment);

            var html ='<span>', res = comment.split(" ");

            for(var i=0; i<res.length; i++){
                if(!this._isValidUrl(res[i])){
                    html+= res[i] + " ";
                }
                else{
                    var target;
                    if (res[i].substring(0,4) == 'http') target = res[i];
                    else target = 'http://' + res[i];

                    html+= '<a href="'+target+'" target="_blank">' + res[i] + '</a> ';
                }
            }

            html += '</span>';
            return html;

        },

        createComposition: function(sessionId, compositionData) {

            var self = this;
            $.ajaxSetup({
                headers: {
                    "Ehr-Session": sessionId
                }
            });
            var queryParams = {
                "ehrId": this.options.ehrId,
                templateId: 'Plain Note',
                format: 'FLAT',
                committer: 'Test User'
            };
            $.ajax({
                url: this.options.baseUrl + "/composition?" + $.param(queryParams),
                type: 'POST',
                contentType: 'application/json',
                data: JSON.stringify(compositionData),
                success: function (res) {
                    console.log(res);
                    self._closeSession(sessionId);
                }
            });
        },

        eventListeners: function(){

            var self = this;

            $('#submitBt').on("click", function(){

                if($('#status-text').val()){

                    var timelineItem = Handlebars.compile( $("#timeline-entry").html() );

                    var statusStr = self.buildCommentStr( $('#status-text').val() );

                    if(self.media.length>0){

                        var compositionData = self.setCompositionData(statusStr);

                        //self.createComposition(self._getSessionId(), compositionData);

                        statusStr += '<div class="attach"><i class="fa fa-paperclip"></i> Attachments: ';

                        for (var i=0; i<self.media.length; i++){

                            statusStr += '<div class="attach-item"><a href=' + self.media[i][0].href + ' target="_blank">';

                            if(self.media[i][0].mimeType.indexOf("image")!= -1) {
                                statusStr += '<img class="attach-thumb" title="' + self.media[i][0].fileName + '" src=' + self.media[i][0].href + ' />';
                            }
                            else{
                                statusStr += '<img class="attach-thumb" title="' + self.media[i][0].fileName + '" src="img/file-icon.png" />';
                            }

                            statusStr += '<div class="filename">' + self.media[i][0].fileName + '</div></a></div>';
                        }

                        statusStr += '</div>';

                    }

                    var context = {
                        time: self._formatDate(new Date()),
                        timeAbbr: self._formatDateAbbr(new Date()),
                        status: statusStr
                    };

                    var html = timelineItem(context);

                    $(html).hide().prependTo('.timeline').slideDown("slow");

                    self.bindNewEvents(self.$element.find('.timeline-item:first-child'));

                    $('#status-text').val('');

                    self.media = [];

                }
            });

        },

        setCompositionData: function(note){
            var compositionData = {
                "ctx/language": "en",
                "ctx/territory": "CA",
                "plain_note/plain_note/note": note
                //"plain_note/plain_note/resource:0|mediatype": "image/png",
                //"plain_note/plain_note/resource:0|uri": "http://thinkehr3:8080/store/rest/store/b4fb38a8-04b4-4a64-8c74-89c6939e5edb"
            };

            for (var i=0; i<this.media.length; i++){
                compositionData["plain_note/plain_note/resource:"+i+"|mediatype"] = this.media[i][0].mimeType;
                compositionData["plain_note/plain_note/resource:"+i+"|uri"] = this.media[i][0].href;
            }

            return compositionData;
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

        _formatDate: function(date){

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

            return curr_date + "-" + curr_month + "-" + curr_year + " " + curr_hour + ":" + curr_min;// + ":" + curr_sec;

        },

        _formatDateAbbr: function(date){

            var d = new Date(date);

            var monthNames = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];

            var curr_date = d.getDate();

            var curr_month = d.getMonth();

            return curr_date + "-" + monthNames[curr_month];

        },

        _normalizeDate: function(el){

            el = el + "";

            if (el.length == 1){
                el = "0" + el;
            }

            return el;
        },

        _isValidUrl: function(str) {

            var pattern = new RegExp('^(https?:\\/\\/)?'+ // protocol
                '((([a-z\\d]([a-z\\d-]*[a-z\\d])*)\\.)+[a-z]{2,}|'+ // domain name
                '((\\d{1,3}\\.){3}\\d{1,3}))'+ // OR ip (v4) address
                '(\\:\\d+)?(\\/[-a-z\\d%_.~+]*)*'+ // port and path
                '(\\?[;&a-z\\d%_.~+=-]*)?'+ // query string
                '(\\#[-a-z\\d_]*)?$','i'); // fragment locator
            if(!pattern.test(str)) {
                return false;
            } else {
                return true;
            }

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
