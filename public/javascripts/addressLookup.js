(function($) {
    var lookUpPath = '/fhdds/address-lookup?postcode=';

    function populateAddress(address, context) {
        $('[name="' + context + '.' + 'Line1').val(address.lines[0]);
        if (address.lines[1]) {
            $('[name="' + context + '.' + 'Line2').val(address.lines[1]);
        } else {
            $('[name="' + context + '.' + 'Line2').val('')
        }
        if (address.lines[2]) {
            $('[name="' + context + '.' + 'Line3').val(address.lines[2]);
        } else {
            $('[name="' + context + '.' + 'Line3').val('')
        }

        $('[name="' + context + '.' + 'Line4').val(address.town);
        $('[name="' + context + '.' + 'postcode').val(address.postcode);
    }

    function clearAddressFields(context) {
        $('[name="' + context + '.' + 'Line1').val('');
        $('[name="' + context + '.' + 'Line2').val('')
        $('[name="' + context + '.' + 'Line3').val('')
        $('[name="' + context + '.' + 'Line4').val('');
        $('[name="' + context + '.' + 'postcode').val('');
    }

    function showResult(data, context) {
        var address = data.addresses.length != 1 ? "addresses" : "address";
        var legend = '<legend class="form-label-bold">' + data.addresses.length + ' ' + address + ' found...</legend>';
        var resultsArray = [];
        jQuery.each(data.addresses, function(i, result) {
            var address = result.address;
            resultsArray.push('<div class="multiple-choice"><input class="postcode-lookup-result" type="radio" id="' + context + '-result" name="' + context + '-result" value="' + i + '"><label for="' + context + '-result">');
            resultsArray.push(address.lines.join(', '));
            resultsArray.push(address.town + ', ');
            resultsArray.push(address.postcode);
            resultsArray.push('</label></div>');
        });

        console.log('attempting to populate #' + context + '-results');

        $('#' + context + '-results')
            .html(legend + resultsArray.join(''))
            .focus()
            .on('click', '.postcode-lookup-result', function (e) {
                var index = $(e.currentTarget).val()
                console.log('address selected for index ' + index, data.addresses[index])
                populateAddress(data.addresses[index].address, context)
            });

    }

    function searchAddress(url, context) {
        // remove previous results
        $('#' + context + '-results').empty();
        // clear down previous address fields
        clearAddressFields(context);
        $.ajax({
            type: 'GET',
            url: url,
            dataType: "json",
            success: function(data) {
                console.log('results', data);
                showResult(data, context);
            },
            error: function(jqXHR) {
                //doError(jqXHR, context);
            },
            headers: {"X-Hmrc-Origin": "fhdds"}
        });
    }

    $('.address-lookup').on('click', function() {
        var postcode = $(this).parents('.address-lookup-container').find('.postcode-value').val().replace(/\s/g,''),
            url = lookUpPath + postcode,
            context = $(this).data('context');

        searchAddress(url, context);
    });

    var manualMode = function (context) {
        $('#' + context + '-manual-container').removeClass('js-hidden');
        $('#' + context + '-lookup-container').addClass('js-hidden');
    }

    var lookupMode = function (context) {
        $('#' + context + '-manual-container').addClass('js-hidden');
        $('#' + context + '-lookup-container').removeClass('js-hidden');
    }

    $('.manual-address-mode').on('click', function (e) {
        e.preventDefault();
        manualMode($(this).data('context'));
    })

    $('.lookup-address-mode').on('click', function (e) {
        e.preventDefault();
        lookupMode($(this).data('context'));
    })

})(jQuery);