jQuery(document).ready(function(){

    var $table = $('.container table');

    var getText = function(){
        jQuery('#inputBox').keyup(function(){
            var string = jQuery(this).val();
            if (string === ""){
             string = '***'
            }
            getVentureList(string);
        });
    };

    var getVentureList = function(input){

        var ventureListUrl = $table.data('list').replace('***', input);

        jQuery.get(ventureListUrl, function(ventures){
            $table.empty();
            jQuery.each(ventures, function(index, value){
                var row = jQuery('<tr/>').append( jQuery('<td/>').text(value.name));
                $table.append(row);
            });
        });
    };

    getText();

});