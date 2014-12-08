/**
 * Copyright (c) 2014 Marand Lab
 * Created by LeandroG on 29.10.2014.
 */

var ehrscapeDemo = new function(){

    /* Helper functions */

    var demographics = this;

    demographics.capitaliseFirstLetter = function(string){
        return string.charAt(0).toUpperCase() + string.slice(1);
    };

    demographics.formatDate = function(date) {
        var d = new Date(date);

        var monthNames = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];

        var curr_date = d.getDate();

        var curr_month = d.getMonth();

        var curr_year = d.getFullYear();

        return monthNames[curr_month] + ". " + curr_date + ", " + curr_year;

    };

    demographics.formatDateUS = function(date) {
        var d = new Date(date);

        var curr_date = d.getDate();
        curr_date = normalizeDate(curr_date);

        var curr_month = d.getMonth();
        curr_month++;
        curr_month = normalizeDate(curr_month);

        var curr_year = d.getFullYear();

        return curr_month + "-" + curr_date + "-" + curr_year;

    };

    demographics.getAge = function(dateString) {
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
    };

    function normalizeDate(el){

        el = el + "";

        if (el.length == 1){
            el = "0" + el;
        }

        return el;
    }


    /* Remove typehead*/

    $(document).click(function(event) {
        if(!$(event.target).closest('.typehead').length) {
            if($('.typehead').is(":visible")) {
                $('.typehead').hide();
            }
        }
    });

}();

