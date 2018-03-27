(function($) {
  // initialise the display of js-only controls
  // the display will default to an open manual address
  // without a link to postcode lookup - if JS is enabled
  // then either open the lookup widget or default to the
  // manual entry with existing values populated

  // because we can have multiple instances on the same page,
  // grab all postcode fields - if any have a value
  // open the parent manual container
  // todo: aria labelling
  $('[name$="postcode"]').each(function () {
    var $this = $(this),
      $manual = $this.parents('.address-manual-container'),
      $lookup = $manual.siblings('.address-lookup-container'),
      $lookupLink = $manual.find('.lookup-link-container');

    $lookup.show();

    if($this.val().length || $this.hasClass('form-control-error')) {
      $lookup.addClass('js-hidden');
      $manual.removeClass('js-hidden');
      $lookupLink.show()
    }
  });

  var lookUpPath = '/fhdds/address-lookup?postcode=';
  var postcodeRegex = /(([gG][iI][rR] {0,}0[aA]{2})|((([a-pr-uwyzA-PR-UWYZ][a-hk-yA-HK-Y]?[0-9][0-9]?)|(([a-pr-uwyzA-PR-UWYZ][0-9][a-hjkstuwA-HJKSTUW])|([a-pr-uwyzA-PR-UWYZ][a-hk-yA-HK-Y][0-9][abehmnprv-yABEHMNPRV-Y]))) {0,}[0-9][abd-hjlnp-uw-zABD-HJLNP-UW-Z]{2}))$/;

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

  function showError(error, $container) {
    clearError($container);
    $container.find('.lookup-results-fieldset').empty().before('<span class="error-message" role="alert">' + error + '</span>')
  }

  function clearError($container) {
    $container.find('.error-message').remove()
  }

  function showResult(data, context) {
    var results = data.addresses,
      count = results.length,
      $container = $('#' + context + '-results').off(),
      addressStringBuilder = [];

    function selectAddress(e) {
      var index = $(e.currentTarget).val();
      populateAddress(results[index].address, context)
    }

    addressStringBuilder.push('<legend class="form-label-bold">' + count + ' ' + (count === 1 ? 'address' : 'addresses') + ' found...</legend>');
    jQuery.each(results, function(i, result) {
      var address = result.address;
      addressStringBuilder.push('<div class="multiple-choice"><input class="postcode-lookup-result" type="radio" id="' + context + '-result-' + i + '" name="' + context + '-result" value="' + i + '"><label for="' + context + '-result-' + i + '">');
      addressStringBuilder.push(address.lines.join(', '));
      addressStringBuilder.push(address.town + ', ');
      addressStringBuilder.push(address.postcode);
      addressStringBuilder.push('</label></div>');
    });

    $container
      .html(addressStringBuilder.join(''))
      .on('click', '.postcode-lookup-result', selectAddress)
      .find('.postcode-lookup-result:first').focus()
  }

  function searchAddress(url, context) {
    var $resultsEl = $('#' + context + '-results'),
      $container = $resultsEl.parents('.address-lookup-container');
    // remove previous results
    $resultsEl.empty();
    // clear down previous address fields
    clearAddressFields(context);
    // clear down errors
    clearError($container);

    $.ajax({
      type: 'GET',
      url: url,
      dataType: "json",
      success: function(data) {
        showResult(data, context);
      },
      error: function(jqXHR) {
        //doError(jqXHR, context);
        showError('Sorry something went wrong, please try again', $container)
      },
      headers: {"X-Hmrc-Origin": "fhdds"}
    });
  }

  $('.address-lookup').on('click', function() {
    var $container = $(this).parents('.address-lookup-container'),
      postcode = $container.find('.postcode-value').val().replace(/\s/g,''),
      url = lookUpPath + postcode,
      context = CSS.escape($(this).data('context'));

    if (!postcode.match(postcodeRegex)){
      showError('The postcode you have searched with is not a valid UK postcode', $container);
      return;
    }

    searchAddress(url, context);
  });

  var manualMode = function (context) {
    $('#' + context + '-manual-container').removeClass('js-hidden');
    $('#' + context + '-lookup-container').addClass('js-hidden');
    $('.lookup-address-mode').parent().show();
  };

  var lookupMode = function (context) {
    $('#' + context + '-manual-container').addClass('js-hidden');
    $('#' + context + '-lookup-container').removeClass('js-hidden');
  };

  $('.manual-address-mode').on('click', function (e) {
    e.preventDefault();
    manualMode(CSS.escape($(this).data('context')));
  });

  $('.lookup-address-mode').on('click', function (e) {
    e.preventDefault();
    lookupMode(CSS.escape($(this).data('context')));
  });

})(jQuery);