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
  $('.address-lookup-container, .lookup-link-container').removeAttr('hidden');


  // because we can have multiple instances on the same page,
  // grab all postcode fields - if any have a value
  // open the parent manual container
  $('[name$="postcode"]').each(function () {
    var $this = $(this),
      $manual = $this.parents('.address-manual-container'),
      $lookup = $manual.siblings('.address-lookup-container'),
      $lookupLink = $manual.find('.lookup-link-container');

    $lookup.show();

    if($this.val().length || $this.hasClass('govuk-input--error')) {
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
    store.$results.siblings('.govuk-pagination').off().remove();
    // build the pagination navigation
    var paginationNav = ['<nav class="govuk-pagination" role="navigation" aria-label="Pagination">'];
    // we are using zero indexed paging
    if (prevPage || prevPage === 0) {
      paginationNav.push('<div class="govuk-pagination__prev"><a class="govuk-link govuk-pagination__link" href="#" rel="prev">\n' +
          '          <svg class="govuk-pagination__icon govuk-pagination__icon--prev" xmlns="http://www.w3.org/2000/svg"\n' +
          '               height="13" width="15" aria-hidden="true" focusable="false" viewBox="0 0 15 13">\n' +
          '            <path\n' +
          '                d="m6.5938-0.0078125-6.7266 6.7266 6.7441 6.4062 1.377-1.449-4.1856-3.9768h12.896v-2h-12.984l4.2931-4.293-1.414-1.414z"></path>\n' +
          '          </svg>\n' +
          '          <span class="govuk-pagination__link-title">Previous<span class="page-nav__title"> ' + perPage + ' addresses</span>\n' +
          '      </span></a></div>'
      )
    }
    if (nextPage) {
      var remainder = end + perPage <= store.count ? perPage : store.count % end;
      paginationNav.push('<div class="govuk-pagination__next"><a class="govuk-link govuk-pagination__link" href="#" rel="next">\n' +
          '      <span class="govuk-pagination__link-title">\n' +
          '        Next<span class="page-nav__title"> ' + remainder + ' ' + Utils.pluralise(remainder, 'address', 'addresses') + '</span>\n' +
          '      </span>\n' +
          '          <svg class="govuk-pagination__icon govuk-pagination__icon--next" xmlns="http://www.w3.org/2000/svg"\n' +
          '               height="13" width="15" aria-hidden="true" focusable="false" viewBox="0 0 15 13">\n' +
          '            <path\n' +
          '                d="m8.107-0.0078125-1.4136 1.414 4.2926 4.293h-12.986v2h12.896l-4.1855 3.9766 1.377 1.4492 6.7441-6.4062-6.7246-6.7266z"></path>\n' +
          '          </svg></a></div>'
      )
    }
    paginationNav.push('</nav>');
    // insert pagination navigation to DOM
    $(paginationNav.join('')).insertAfter(store.$results);
    $('.govuk-pagination')
        .on('click', 'a[rel="next"]', function (e) {
          e.preventDefault();
          showPage(nextPage, store);
        })
        .on('click', 'a[rel="prev"]', function (e) {
          e.preventDefault();
          showPage(prevPage, store);
        });

    showResults(start, end, store);
  }

  function showError(error, store, $input) {
    clearError(store);
    var $error = $('<p class="govuk-error-message">' + error + '</p>');
    if ($input) {
      // a user input error
      $error.insertBefore($input);
      $input.addClass('govuk-input--error').focus();
    } else {
      // a service level error
      $error.insertAfter(store.$submitButton);
    }

    store.$results.empty().siblings('.govuk-pagination').remove();
    store.$submitButton.removeAttr('disabled');
  }

  function clearError(store) {
    store
        .$container
        .find('.govuk-input--error')
        .removeClass('govuk-input--error')
        .end()
        .find('.govuk-error-message')
        .remove();
  }

  function initStore(context) {
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

  function buildRadios(start, end, store) {
    var results = store.addresses.slice(start, end);
    var i = start;
    return results.reduce(function (list, next) {
      var address = next.address;
      list.push('<div class="govuk-radios__item"><input class="postcode-lookup-result govuk-radios__input" type="radio" id="' + store.context + '-result-' + i + '" name="' + store.context + '-result" value="' + i + '"><label class="govuk-label govuk-radios__label" for="' + store.context + '-result-' + i + '">');
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
    store.legend = '<legend class="govuk-fieldset__legend govuk-fieldset__legend--s">' + store.count + ' ' + Utils.pluralise(store.count, 'address', 'addresses') + ' found:</legend>';

    if (store.count > maxCount) {
      showError('We found more than ' + maxCount + ' results for "' + store.$postcodeInput.val() + '", please enter a property name or number and try again or enter the address manually', store, store.$filterInput);
      return false;
    }
    showPage(0, store);
  }

  function showResults (start, end, store) {
    var pagedResults = buildRadios(start, end, store);
    store.$results
      .html('<fieldset class="lookup-results-fieldset govuk-fieldset">' + store.legend + '<div class="govuk-radios govuk-radios--small" data-module="govuk-radios">' + pagedResults.join('') + '</div></fieldset>')
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
      .html('<p class="govuk-body">searching...</p>')
      .siblings('.govuk-pagination')
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
    $('.lookup-address-mode').parent().show(); // legacy - can be removed when all pages upgraded
    $('#' + context + '-manual-container').find('input').first().trigger('focus');
  };

  var lookupMode = function (context) {
    $('#' + context + '-manual-container').addClass('js-hidden');
    $('#' + context + '-lookup-container').removeClass('js-hidden');
    $('#' + context + '-lookup-container').find('input').first().trigger('focus');
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
