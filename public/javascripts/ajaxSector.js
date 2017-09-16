jQuery(document).ready(function(){
    var $table = $('.sector table');
    var getSectorList = function(){
        var sectorListUrl = $table.data('list');

        jQuery.get(sectorListUrl, function(sector){

            $table.empty();
            jQuery.each(sector, function(index, value){
                var row = jQuery('<tr/>').append( jQuery('<td/>').text("Sector ")).append( jQuery('<td/>').text(value.name));
                $table.append(row);
            });
        });
        setTimeout(getSectorList, 3000);
    };

    getSectorList();

});