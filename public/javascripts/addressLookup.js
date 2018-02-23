(function($) {
    // initialise the display of js-only controls
    // initialise the display of js-only controls
    if ($('.address-line-postcode').val() == '') {
        $('.address-lookup-container').show();
    } else {
        $('.address-manual-container').removeClass('js-hidden');
        $('.lookup-link-container').show()
        $('.address-lookup-container').show().addClass('js-hidden');
    }

    var lookUpPath = '/fhdds/address-lookup?postcode=';

    // address mapping is custom given the different fieldnames
    // in ETMP and DES schemas and the ADDRESS_LOOKUP response:
    function populateAddress(address, context) {
        $.each([1, 2, 3], function (i, lineNum) {
            var line = address.lines[i] || '';
            $('[name="' + context + '.' + 'Line' + (lineNum)).val(line);
        })
        $('[name="' + context + '.' + 'Line4').val(address.town);
        $('[name="' + context + '.' + 'postcode').val(address.postcode);
    }

    function clearAddressFields(context) {
        $.each( ['Line1', 'Line2', 'Line3', 'Line4', 'postcode'], function (i, line) {
            $('[name="' + context + '.' + line).val('');
        });
    }

    function showResult(data, context) {
        var count = data.addresses.length;
        var addressStringBuilder = ['<legend class="form-label-bold">' + count + ' ' + (count == 1 ? 'address' : 'addresses') + ' found...</legend>'];
        jQuery.each(data.addresses, function(i, result) {
            var address = result.address;
            addressStringBuilder.push('<div class="multiple-choice"><input class="postcode-lookup-result" type="radio" id="' + context + '-result-' + i + '" name="' + context + '-result" value="' + i + '"><label for="' + context + '-result-' + i + '">');
            addressStringBuilder.push(address.lines.join(', '));
            addressStringBuilder.push(address.town + ', ');
            addressStringBuilder.push(address.postcode);
            addressStringBuilder.push('</label></div>');
        });

        $('#' + context + '-results')
            .html(addressStringBuilder.join(''))
            .focus()
            .on('click', '.postcode-lookup-result', function (e) {
                var index = $(e.currentTarget).val()
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
            context = CSS.escape($(this).data('context'));

        searchAddress(url, context);
    });

    var manualMode = function (context) {
        $('#' + context + '-manual-container').removeClass('js-hidden');
        $('#' + context + '-lookup-container').addClass('js-hidden');
        $('.lookup-address-mode').parent().show();
    }

    var lookupMode = function (context) {
        $('#' + context + '-manual-container').addClass('js-hidden');
        $('#' + context + '-lookup-container').removeClass('js-hidden');
    }

    $('.manual-address-mode').on('click', function (e) {
        e.preventDefault();
        manualMode(CSS.escape($(this).data('context')));
    })

    $('.lookup-address-mode').on('click', function (e) {
        e.preventDefault();
        lookupMode(CSS.escape($(this).data('context')));
    })

})(jQuery);