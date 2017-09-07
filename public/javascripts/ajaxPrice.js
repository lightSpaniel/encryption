jQuery(document).ready(function(){
    var $table = $('.price table');
    var getPriceList = function(){
        var nominalListUrl = $table.data('list');

        jQuery.get(nominalListUrl, function(nominal){

            $table.empty();
            jQuery.each(nominal, function(index, value){
                var row = jQuery('<tr/>').append( jQuery('<td/>').text("Price ")).append( jQuery('<td/>').text(value.name));
                $table.append(row);
            });
        });
        setTimeout(getPriceList, 3000);
    };

    getPriceList();

});