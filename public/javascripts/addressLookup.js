(function($) {
    var env = (!window.location.origin) ? window.location.protocol + "//" + window.location.hostname + (window.location.port ? ':' + window.location.port: '') : window.location.origin,
        lookUpPath = '/fhdds/address-lookup?postcode=';

    function showResult(data, num) {
        var address = data.addresses.length != 1 ? "addresses" : "address";
        var legend = '<legend class="form-label-bold">' + data.addresses.length + ' ' + address + ' found...</legend>';
        var resultsArray = [];
        jQuery.each(data.addresses, function(i, result) {
            var address = result.address;
            resultsArray.push('<div class="multiple-choice"><input class="postcode-lookup-results-entry" type="radio" id="result-' + num + 'choice' + i + '" name="res" value="' + i + '"><label for="result-' + num + 'choice' + i + '">');
            resultsArray.push(address.lines.join(', '));
            resultsArray.push(address.town + ', ');
            resultsArray.push(address.postcode);
            resultsArray.push('</label></div>');
        });

        $('#results-1').html(legend + resultsArray.join('')).focus();
    }

    function searchAddress(url, num) {
        $('#results-1').empty();
        $.ajax({
            type: 'GET',
            url: url,
            dataType: "json",
            success: function(data) {
                console.log('results', data);
                showResult(data, num);
            },
            error: function(jqXHR) {
                //doError(jqXHR, num);
            },
            headers: {"X-Hmrc-Origin": "fhdds"}
        });
    }

    $('.address-lookup').on('click', function() {
        var postcode = $(this).parents('.address-lookup-container').find('.postcode-value').val().replace(/\s/g,''),
            url = env + lookUpPath + postcode;

        searchAddress(url, "1");
    });

})(jQuery);