/*
*  basic polyfill for CSS.escape()
*  https://drafts.csswg.org/cssom/#the-css.escape()-method
*/
;(function (window) {

    var root = typeof window != 'undefined' ? window : this

	if (root.CSS && root.CSS.escape) {
		return root.CSS.escape;
	}

	var cssEscape = function (value) {
		if (arguments.length == 0) {
			throw new TypeError('`CSS.escape` requires an argument.');
		}
		var permittedChars = /[0-9a-zA-Z_-]/,
		    string = String(value),
            length = string.length,
		    index = -1,
		    result = '';

		while (++index < length) {
            var char = string.charAt(index)
			result += char.match(permittedChars) ? char : '\\' + char;
		}
		return result;
	};

	if (!root.CSS) {
		root.CSS = {};
	}

	root.CSS.escape = cssEscape;
	return cssEscape;

})(window);

/* global $ */
/* global GOVUK */

/* updated GOVUK.ShowHideContent to support the use of dots
*  in selectors as required by Scala Play framework
*  https://github.com/alphagov/govuk_frontend_toolkit/blob/c88df4977fbb1d63950e8d19cc7dfbd02ede0709/javascripts/govuk/show-hide-content.js
*/

;(function (global) {
  'use strict'

  var $ = global.jQuery
  var GOVUK = global.GOVUK || {}

  function ShowHideContent () {
    var self = this

    // Radio and Checkbox selectors
    var selectors = {
      namespace: 'ShowHideContent',
      radio: '[data-target] > input[type="radio"]',
      checkbox: '[data-target] > input[type="checkbox"]'
    }

    // Escape selector for use in DOM selector
    // this function is used instead of
    function escapeSelector (str) {
      return CSS.escape(str)
    }

    // Adds ARIA attributes to control + associated content
    function initToggledContent () {
      var $control = $(this)
      var $content = getToggledContent($control)

      // Set aria-controls and defaults
      if ($content.length) {
        $control.attr('aria-controls', $content.attr('id'))
        $control.attr('aria-expanded', 'false')
        $content.attr('aria-hidden', 'true')
      }
    }

    // Return toggled content for control
    function getToggledContent ($control) {
      var id = $control.attr('aria-controls')

      // ARIA attributes aren't set before init
      if (!id) {
        id = $control.closest('[data-target]').data('target')
      }

      // Find show/hide content by id
      return $('#' + escapeSelector(id))
    }

    // Show toggled content for control
    function showToggledContent ($control, $content) {
      // Show content
      if ($content.hasClass('js-hidden')) {
        $content.removeClass('js-hidden')
        $content.attr('aria-hidden', 'false')

        // If the controlling input, update aria-expanded
        if ($control.attr('aria-controls')) {
          $control.attr('aria-expanded', 'true')
        }
      }
    }

    // Hide toggled content for control
    function hideToggledContent ($control, $content) {
      $content = $content || getToggledContent($control)

      // Hide content
      if (!$content.hasClass('js-hidden')) {
        $content.addClass('js-hidden')
        $content.attr('aria-hidden', 'true')

        // If the controlling input, update aria-expanded
        if ($control.attr('aria-controls')) {
          $control.attr('aria-expanded', 'false')
        }
      }
    }

    // Handle radio show/hide
    function handleRadioContent ($control, $content) {
      // All radios in this group which control content
      var selector = selectors.radio + '[name=' + escapeSelector($control.attr('name')) + '][aria-controls]'
      var $form = $control.closest('form')
      var $radios = $form.length ? $form.find(selector) : $(selector)

      // Hide content for radios in group
      $radios.each(function () {
        hideToggledContent($(this))
      })

      // Select content for this control
      if ($control.is('[aria-controls]')) {
        showToggledContent($control, $content)
      }
    }

    // Handle checkbox show/hide
    function handleCheckboxContent ($control, $content) {
      // Show checkbox content
      if ($control.is(':checked')) {
        showToggledContent($control, $content)
      } else { // Hide checkbox content
        hideToggledContent($control, $content)
      }
    }

    // Set up event handlers etc
    function init ($container, elementSelector, eventSelectors, handler) {
      $container = $container || $(document.body)

      // Handle control clicks
      function deferred () {
        var $control = $(this)
        handler($control, getToggledContent($control))
      }

      // Prepare ARIA attributes
      var $controls = $(elementSelector)
      $controls.each(initToggledContent)

      // Handle events
      $.each(eventSelectors, function (idx, eventSelector) {
        $container.on('click.' + selectors.namespace, eventSelector, deferred)
      })

      // Any already :checked on init?
      if ($controls.is(':checked')) {
        $controls.filter(':checked').each(deferred)
      }
    }

    // Get event selectors for all radio groups
    function getEventSelectorsForRadioGroups () {
      var radioGroups = []

      // Build an array of radio group selectors
      return $(selectors.radio).map(function () {
        var groupName = escapeSelector($(this).attr('name'))

        if ($.inArray(groupName, radioGroups) === -1) {
          radioGroups.push(groupName)
          return 'input[type="radio"][name="' + escapeSelector($(this).attr('name')) + '"]'
        }
        return null
      })
    }

    // Set up radio show/hide content for container
    self.showHideRadioToggledContent = function ($container) {
      init($container, selectors.radio, getEventSelectorsForRadioGroups(), handleRadioContent)
    }

    // Set up checkbox show/hide content for container
    self.showHideCheckboxToggledContent = function ($container) {
      init($container, selectors.checkbox, [selectors.checkbox], handleCheckboxContent)
    }

    // Remove event handlers
    self.destroy = function ($container) {
      $container = $container || $(document.body)
      $container.off('.' + selectors.namespace)
    }
  }

  ShowHideContent.prototype.init = function ($container) {
    this.showHideRadioToggledContent($container)
    this.showHideCheckboxToggledContent($container)
  }

  GOVUK.ShowHideContent = ShowHideContent
  global.GOVUK = GOVUK
})(window)


function init() {
  // show js-enabled items
  $('.js-show').show()
  // enable save4later routing on all forms
  $('[name="saveAction2"]').click(function (event) {

    $('[name="saveAction"]').val($(this).val());

  });

  // Use GOV.UK shim-links-with-button-role.js to trigger a link styled to look like a button,
  // with role="button" when the space key is pressed.
  GOVUK.shimLinksWithButtonRole.init()

  // Show and hide toggled content
  // Where .multiple-choice uses the data-target attribute
  // to toggle hidden content
  var showHideContent = new GOVUK.ShowHideContent()
  showHideContent.init()

  GOVUK.details.init()

  // Google Analytics event reporting, using template:
  // ga('send', 'event', [eventCategory], [eventAction], [eventLabel], [eventValue], [fieldsObject])
  // wrapInTimeout function to ensure forms get submitted when GA fails to respond
  function wrapInTimeout(callback, optionalTime) {
    var called = false;
    function fn() {
      if (!called) {
        called = true;
        callback();
      }
    }
    setTimeout(fn, optionalTime || 1000);
    return fn;
  }

  $('a[target="_blank"]').click(function() {
    ga('send', 'event', 'external link', 'click', this.innerText)
    ga('govuk_shared.send', 'event', 'external link', 'click', this.innerText)
  });

  $('summary span.summary').click(function() {
    var eventLabel = $(this).text();
    ga('send', 'event', 'help', 'click', eventLabel);
    ga('govuk_shared.send', 'event', 'help', 'click', eventLabel);
  });

  // because we will have a race condition on submission
  // we need to intercept the submission to post the analytics events
  $('form').on('submit', function (e) {
    var $pageHeading = $('h1');
    var $actionField = $('[name="saveAction"]');
    // we can only report on forms with headings and actions
    if ($pageHeading.length && $actionField.length) {
      e.preventDefault();
      var $form = $(this);
      var eventLabel = $pageHeading.text();
      var form = $form[0];
      var $selectedRadios = $form.find('input:radio:checked');
      $selectedRadios.each(function (i, option) {
        ga('send', 'event', 'radio selection', option.value, option.name)
        ga('govuk_shared.send', 'event', 'radio selection', option.value, option.name)
      });

      var eventAction = $actionField.val();
      ga('send', 'event', 'submit', eventAction, eventLabel, {
        hitCallback: wrapInTimeout(function() {
          form.submit();
        })
      })
    }
  });

  var $errorSummary = $('.error-summary');

  if ($errorSummary.length) {
    // summary focusing
    $errorSummary.focus();
    $('.error-summary-list li a').each(function (i, item) {
      var $link = $(item);
      // ga reporting
      var eventAction = $link.text();
      var eventLabel = $('h1').text();
      ga('send', 'event', 'error', eventAction, eventLabel);
      ga('govuk_shared.send', 'event', 'error', eventAction, eventLabel);
      // error focusing
      $link.on('click', function () {
        // escape handling for periods in selectors
        var target = CSS.escape($(this).attr('href').slice(1));
        window.setTimeout(function () {
          $('#' + target)
            .parent()
            .find('input.form-control-error')
            .first()
            .focus()
        }, 200)
      })

    })
  }

  $('a.address-lookup').click(function() {
    var eventLabel = $(this).parents('.address-lookup-container').siblings('legend').text();
    ga('send', 'event', 'postcode lookup', 'click', eventLabel)
    ga('govuk_shared.send', 'event', 'postcode lookup', 'click', eventLabel)
  });

  $('a.manual-address-mode').click(function() {
    var eventLabel = $(this).parents('.address-lookup-container').siblings('legend').text();
    ga('send', 'event', 'manual address preference', 'click', eventLabel)
    ga('govuk_shared.send', 'event', 'manual address preference', 'click', eventLabel)
  });

  $('a.lookup-address-mode').click(function() {
    var eventLabel = $(this).parents('.address-manual-container').siblings('legend').text();
    ga('send', 'event', 'postcode lookup preference', 'click', eventLabel)
    ga('govuk_shared.send', 'event', 'postcode lookup preference', 'click', eventLabel)
  });

  if ($('.transaction-banner--complete').length) {
    var eventLabel = $('.transaction-banner--complete').find('h1').text();
    ga('send', 'event', 'transaction complete', 'report', eventLabel)
    ga('govuk_shared.send', 'event', 'transaction complete', 'report', eventLabel)
  }

}

$(document).ready(init)
