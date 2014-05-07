/* Sample implementations of SMART Connect Host glue methods */
SMART = new SMART_CONNECT_HOST();

var manifest = {
    "smart_version": "0.6.0",
    "api_base": "/smart",
    "name": "Think!EHR SMART Container",
    "description": "Think!EHR SMART Container",
    "admin": "info@ehrscape.com",

    "launch_urls": {
        "authorize_token": "../oauth/authorize",
        "exchange_token": "../oauth/access_token",
        "request_token": "../oauth/request_token"
    },

    "capabilities": {
        "http://smartplatforms.org/terms#Demographics": {
            "methods": [
                "GET"
            ]
        },
        "http://smartplatforms.org/terms#Encounter": {
            "methods": [
                "GET"
            ]
        },
        "http://smartplatforms.org/terms#VitalSignSet": {
            "methods": [
                "GET"
            ]
        }
    }
};

SMART.get_api_base = function (app_instance, callback) {
    callback("/smart");
};

SMART.get_iframe = function (app_instance, callback) {
    $("iframe").remove(); // clear old iframes
    $("body").append("<iframe src='about:blank;' id='app_iframe_" + app_instance.uuid + "' width=1200 height=800></iframe>");
    callback($("iframe")[0]);
};

SMART.handle_api = function (app_instance, api_call, callback_success, callback_error) {
    if (api_call.method == "GET" && api_call.func.match(/^\/manifest$/)) {
        $.ajax({type: 'get', url: 'manifest.json', dataType: 'text'})
            .success(function (data) {
                callback_success({
                    contentType: "application/json",
                    data: data});
            });
    } else if (api_call.method == "GET" && api_call.func.match(/^\/records\//)) {
        $.ajax({type: 'get', url: apiBaseUrl + api_call.func, dataType: 'text', headers: apiHeaders})
            .success(function (data) {
                callback_success({
                    contentType: "application/rdf+xml",
                    data: data});
            });
    } else {
        alert("Function " + api_call.func + " not implemented yet.");
    }
};

SMART.get_credentials = function (app_instance, callback) {
    console.log('get_credentials', app_instance);
};

SMART.on("request_fullscreen", function (app_instance) {
    $(app_instance.iframe)
        .css({
            position: 'fixed',
            width: '100%',
            height: '100%',
            left: 0,
            top: 0
        });
});