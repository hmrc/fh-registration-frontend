(function($) {
  function pluralise (num, singular, plural) {
    return num === 1 ? singular : plural;
  }

  var Utils = {
    pluralise: pluralise
  };
  var Store = {};
  var perPage = 10;
  var maxCount = 50;
  var postcodeRegex = /(([gG][iI][rR] {0,}0[aA]{2})|((([a-pr-uwyzA-PR-UWYZ][a-hk-yA-HK-Y]?[0-9][0-9]?)|(([a-pr-uwyzA-PR-UWYZ][0-9][a-hjkstuwA-HJKSTUW])|([a-pr-uwyzA-PR-UWYZ][a-hk-yA-HK-Y][0-9][abehmnprv-yABEHMNPRV-Y]))) {0,}[0-9][abd-hjlnp-uw-zABD-HJLNP-UW-Z]{2}))$/;
  var filterRegex = /^[A-Za-z0-9 !'‘’"“”(),./—–‐-]{1,35}$/;
  var lookUpPath = '/fhdds/address-lookup?postcode=';
  // initialise the display of js-only controls
  // the display will default to an open manual address
  // without a link to postcode lookup - if JS is enabled
  // then either open the lookup widget or default to the
  // manual entry with existing values populated

  // because we can have multiple instances on the same page,
  // grab all postcode fields - if any have a value
  // open the parent manual container
  $('[name$="postcode"]').each(function () {
    var $this = $(this),
      $manual = $this.parents('.address-manual-container'),
      $lookup = $manual.siblings('.address-lookup-container'),
      $lookupLink = $manual.find('.lookup-link-container');

    $lookup.show();

    if($this.val().length || $this.hasClass('form-control-error')) {
      $lookup.addClass('js-hidden');
      $manual.removeClass('js-hidden');
      $lookupLink.show();
    }
  });

  // address mapping is custom given the different field names
  // in ETMP and DES schemas and the ADDRESS_LOOKUP response:
  function populateAddress (index, store) {
    var address = store.addresses[index].address;
    $.each([1, 2, 3], function (i, lineNum) {
      var line = address.lines[i] || '';
      $('[name="' + store.context + '.' + 'Line' + (lineNum) + '"]').val(line);
    });
    $('[name="' + store.context + '.' + 'Line4"]').val(address.town);
    $('[name="' + store.context + '.' + 'postcode"]').val(address.postcode);
    $('[name="' + store.context + '.' + 'lookupId"]').val(store.addresses[index].id);
  }

  function clearAddressFields (context) {
    $.each( ['Line1', 'Line2', 'Line3', 'Line4', 'postcode', 'lookupId'], function (i, line) {
      $('[name="' + context + '.' + line + '"]').val('');
    });
  }

  function showPage (page, store) {
    var start = page * perPage;
    var end = start + perPage;
    var nextPage = store.count > end ? page + 1 : null;
    var prevPage = end - perPage > 0 ? page - 1 : null;
    // remove any previous pagination navigation
    store.$results.siblings('.pagination').off().remove();
    // build the pagination navigation
    var paginationNav = ['<div class="pagination" style="margin-top:1em;position:relative;overflow:visible;">'];
    // we are using zero indexed paging
    if (prevPage || prevPage === 0) {
      paginationNav.push('<div style="float:left; width: 49%;"><a href="#" class="prev-page page-nav__link page-nav__link--previous"><span class="page-nav__label">Previous</span><span class="page-nav__title">' + perPage + ' addresses</span></a></div>');
    }
    if (nextPage) {
      var remainder = end + perPage <= store.count ? perPage : store.count % end;
      paginationNav.push('<div style="float:right; width: 49%"><a href="#" class="next-page page-nav__link page-nav__link--next"><span class="page-nav__label">Next</span><span class="page-nav__title">' + remainder + ' ' + Utils.pluralise(remainder, 'address', 'addresses') + '</span></a></div>');
    }
    paginationNav.push('</div>');
    // insert pagination navigation to DOM
    $(paginationNav.join('')).insertAfter(store.$results);
    $('.pagination')
      .on('click', 'a.next-page', function (e) {
        e.preventDefault();
        showPage(nextPage, store);
      })
      .on('click', 'a.prev-page', function (e) {
        e.preventDefault();
        showPage(prevPage, store);
      });

    showResults(start, end, store);
  }

  function showError (error, store, $input) {
    clearError(store);
    var $error = $('<span class="error-message" role="alert">' + error + '</span>');
    if ($input) {
      // a user input error
      $error.insertBefore($input);
      $input.addClass('form-control-error').focus();
    } else {
      // a service level error
      $error.insertAfter(store.$submitButton);
    }

    store.$results.empty().siblings('.pagination').remove();
    store.$submitButton.removeAttr('disabled');
  }

  function clearError (store) {
    store
      .$container
      .find('.form-control-error')
      .removeClass('form-control-error')
      .end()
      .find('.error-message')
      .remove();
  }

  function initStore (context) {
    var $container = $('#' + context + '-fieldset');
    Store[context] = {
      context: context,
      count: 0,
      addresses: [],
      $container: $container,
      $results: $('#' + context + '-results').off(),
      $postcodeInput: $container.find('input.postcode-value'),
      $filterInput: $container.find('input.property-value'),
      $submitButton: $container.find('.address-lookup'),
      legend: ''
    };
    return Store[context];
  }

  function buildRadios (start, end, store) {
    var results = store.addresses.slice(start, end);
    var i = start;
    return results.reduce(function (list, next) {
      var address = next.address;
      list.push('<div class="multiple-choice"><input class="postcode-lookup-result" type="radio" id="' + store.context + '-result-' + i + '" name="' + store.context + '-result" value="' + i + '"><label for="' + store.context + '-result-' + i + '">');
      list.push(address.lines.join(', '));
      list.push(', ' + address.town + ', ');
      list.push(address.postcode);
      list.push('</label></div>');
      i++;
      return list;
    }, [])
  }

  function processResults (data, store) {
    store.count = data.addresses.length;
    store.addresses = data.addresses;
    store.legend = '<legend class="form-label-bold">' + store.count + ' ' + Utils.pluralise(store.count, 'address', 'addresses') + ' found:</legend>';

    if (store.count > maxCount) {
      showError('We found more than ' + maxCount + ' results for "' + store.$postcodeInput.val() + '", please enter a property name or number and try again or enter the address manually', store, store.$filterInput);
      return false;
    }
    showPage(0, store);
  }

  function showResults (start, end, store) {
    var pagedResults = buildRadios(start, end, store);
    store.$results
      .html(store.legend + pagedResults.join(''))
      .focus()
      .on('click', '.postcode-lookup-result', function (e) {
        var index = $(e.currentTarget).val();
        populateAddress(index, store)
      });

    store.$submitButton.removeAttr('disabled');
  }

  function searchAddress (url, store) {
    // remove previous results
    store.$results
      .html('searching...')
      .siblings('.pagination')
      .remove();
    // clear down previous address fields
    clearAddressFields(store.context);
    // clear down errors
    clearError(store);

    $.ajax({
      type: 'GET',
      url: url,
      dataType: "json",
      success: function (data) {
        processResults(data, store);
      },
      error: function () {
        showError('Sorry, there was problem performing this search, please try again and if the problem persists then enter the address manually', store, false);
      },
      headers: {"X-Hmrc-Origin": "fhdds"}
    });
  }

  function handleSubmit (e) {
    var context = CSS.escape($(e.currentTarget).data('context'));
    var url;
    var store = initStore(context);
    store.$submitButton.attr('disabled', 'disabled');
    var propertyFilter = store.$filterInput.val();
    var postcode = store.$postcodeInput.val().replace(/\s/g,'').toUpperCase();

    if (postcode === '') {
      showError('You must enter a UK postcode to look up an address', store, store.$postcodeInput);
      return false;
    }
    if (!postcode.match(postcodeRegex)) {
      showError('The postcode you have searched with is not a valid UK postcode', store, store.$postcodeInput);
      return false;
    }

    url = lookUpPath + postcode;

    if (propertyFilter !== '') {
      if(!propertyFilter.match(filterRegex)) {
        showError('A property name or number can be no longer than 35 characters long and only contain characters that are alpha-numeric, spaces and/or any of the following symbols !\'‘’"“”(),./—–‐-', store, store.$filterInput);
        return false;
      }
      url += '&filter=' + encodeURIComponent(propertyFilter);
    }

    searchAddress(url, store);
  }

  function handleKeyPress (e) {
    if ( e.which === 13 ) {
      e.preventDefault();
      e.stopPropagation();
      handleSubmit(e);
    }
  }

  $('.address-lookup').on('click', handleSubmit);
  $('.address-lookup, .postcode-value, .property-value').on('keypress', handleKeyPress);

  var manualMode = function (context) {
    $('#' + context + '-manual-container').removeClass('js-hidden');
    $('#' + context + '-lookup-container').addClass('js-hidden');
    $('.lookup-address-mode').parent().show();
  };

  var lookupMode = function (context) {
    $('#' + context + '-manual-container').addClass('js-hidden');
    $('#' + context + '-lookup-container').removeClass('js-hidden');
  };

  $('.manual-address-mode').on('click', function () {
    manualMode(CSS.escape($(this).data('context')));
  });

  $('.lookup-address-mode').on('click', function (e) {
    e.preventDefault();
    lookupMode(CSS.escape($(this).data('context')));
  });

})(jQuery);
