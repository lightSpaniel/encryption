jQuery(document).ready(function(){
    var $table = $('.nominal table');
    var getNominalList = function(){
        var nominalListUrl = $table.data('list');

        jQuery.get(nominalListUrl, function(nominal){

            $table.empty();
            jQuery.each(nominal, function(index, value){
                var row = jQuery('<tr/>').append( jQuery('<td/>').text("Nominal ")).append( jQuery('<td/>').text(value.name));
                $table.append(row);
            });
        });
        setTimeout(getNominalList, 3000);
    };

    getNominalList();

});