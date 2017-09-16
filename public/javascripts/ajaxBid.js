jQuery(document).ready(function(){
    var $table = $('.bid table');

    var getBidList = function(){
        jQuery('.bidButton').onclick(function(){

            var sectorBidUrl = $table.data('list');

            jQuery.get(sectorBidUrl, function(bid){

                $table.empty();
                jQuery.each(bid, function(index, value){
                    var row = jQuery('<tr/>').append( jQuery('<td/>').text("Receipt ")).append(
                        jQuery('<td/>').text(value.name)).append(
                            jQuery('<td/>').text(value.description));
                    $table.append(row);
                });
            });
        });
    };

    getBidList();

});