/**
 * Created by LeandroG on 24.11.2014
 * Copyright (c) 2014 Marand Lab
 */

$(function() {

    var $imageMap = $('img[usemap="#regionsMap"]');

    //$imageMap.rwdImageMaps();

    $imageMap.maphilight({
        alwaysOn: true,
        fillColor: 'F6BB42',//'F6BB42',
        fillOpacity: 0.8,
        strokeColor: 'E6E9ED'
    }).parent().addClass('center-map');

    $('.area').on("click touchstart", function(e) {
        e.preventDefault();
        $(this).addClass('selectedArea');
        var data = $(this).data('maphilight') || {};
        data.fillColor = '3BAFDA'; // Sample color
        data.strokeColor = 'FFFFFF';

        // This sets the new data, and finally checks for areas with alwaysOn set
        $(this).data('maphilight', data).trigger('alwaysOn.maphilight');
    });

});