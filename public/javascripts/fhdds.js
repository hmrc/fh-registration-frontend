 $(document).ready(function() {
    var $radio = $('input:radio'), // cache all radio buttons
        $checkOther = $('input:checkbox[value=99]'), // cache all 'other' checkboxes, these will always have a value of 99
        $checkAll = $('label:contains("All") input:checkbox'), // cache all checkboxes with label all
        $checks = $('label input:checkbox:not(label input:checkbox[value=99]):not(label:contains("All") input:checkbox)'), // cache all the checkboxes that are not the previous two
        $exportCheckboxes = $('#doYouExportAlcohol_no, #doYouExportAlcohol_euDispatches, #doYouExportAlcohol_outsideEU'),
        $personOrCompany = $('input:radio[name="personOrCompany"]'),
        $partnerDetails = $('input:radio[name="entityType"]'),
        $suppliers = $('input:radio[name="ukSupplier"]'),
        $deRegistrationReason = $('input:radio[name="deRegistrationReason"]'),
        $withdrawalReason = $('input:radio[name="reason"]'),
        $feedbackLedeLink = $('#feedbackLedeLink');

    function showHide(radio) {
        if (radio.indexOf(".") >= 0) {
            radio = radio.replace(".", "\\.");
        }
        // if all radios are unchecked, hide all content/headings shown based on checked radios
        if ($('#' + radio + '-yes').not(':checked') && $('#' + radio + '-no').not(':checked')) {
            if ($('#' + radio + '-all-content') !== undefined) {
                $('#' + radio + '-all-content').hide();
            }
            if ($('#' + radio + '-yes-content') !== undefined) {
                $('#' + radio + '-yes-content').hide();
            }
            if ($('#' + radio + '-no-content') !== undefined) {
                $('#' + radio + '-no-content').hide();
            }
        }
        // show content for yes radios, hide content for no
        if ($('#' + radio + '-yes').is(':checked')) {
            if ($('#' + radio + '-all-content') !== undefined) {
                $('#' + radio + '-all-content').show();
            }
            if ($('#' + radio + '-yes-content') !== undefined) {
                $('#' + radio + '-yes-content').show();
            }
            if ($('#' + radio + '-no-content') !== undefined) {
                $('#' + radio + '-no-content').hide().find('input:text').val('');
                $('#' + radio + '-no-content').find('input:radio, input:checkbox').attr('checked', false).each(function() {
                    showHide(this.name);
                });
            }
        }
        // show content for no radios, hide content for yes
        if ($('#' + radio + '-no').is(':checked')) {
            if ($('#' + radio + '-all-content') !== undefined) {
                $('#' + radio + '-all-content').show();
           }
            if ($('#' + radio + '-no-content') !== undefined) {
                $('#' + radio + '-no-content').show();
            }
            if ($('#' + radio + '-yes-content') !== undefined) {
                $('#' + radio + '-yes-content').hide().find('input:text').val('');
                $('#' + radio + '-yes-content').find('input:radio, input:checkbox').attr('checked', false).each(function() {
                    showHide(this.name);
                });
            }
        }
    }

    function showHideOther(checkbox) {
        if ($('#' + checkbox).is(':checked')) {
            $('#other-' + checkbox.replace('_99', '')).show();
        } else {
            $('#other-' + checkbox.replace('_99', '')).hide();
            $('#other-' + checkbox.replace('_99', '') + ' input').val('');
        }
    }



    function checkAll(checkbox, name) {
        $(checkbox).is(':checked') ?
            $checks.filter('[name="' + name + '"]').prop('checked', true) :
            $checks.filter('[name="' + name + '"]').prop('checked', false);
    }

    // check/uncheck 'all' checkbox if all other checkboxes are checked/unchecked
    function checks(name) {
        $checks.filter('[name="' + name + '"]').length === $checks.filter('[name="' + name + '"]').filter(':checked').length ?
            $checkAll.filter('[name="' + name + '"]').prop('checked', true) :
            $checkAll.filter('[name="' + name + '"]').prop('checked', false);
    }

    // mutex export alcohol fields, if no is selected, the others should not be, and vice versa
    function exportAlcohol(id) {
        if (id === 'doYouExportAlcohol_no') {
            $('#doYouExportAlcohol_euDispatches').prop('checked', false);
            $('#doYouExportAlcohol_outsideEU').prop('checked', false);
        }
        if (id === 'doYouExportAlcohol_euDispatches' || id === 'doYouExportAlcohol_outsideEU') {
            $('#doYouExportAlcohol_no').prop('checked', false);
        }
    }

    // show and hide content according to director, company secretary & individual and company status
    function personOrCompany() {

        if ($('#personOrCompany-person').is(':checked')) {
            $('#individual').show();
            hideAndClear('#company');
        }

        if ($('#personOrCompany-company').is(':checked')) {
            $('#company').show();
            hideAndClear('#individual');
        }

        if (!$personOrCompany.is(':checked')) {
            $('#company, #individual').hide();
        }
    }

    function supplierRadioLoad() {
        if ($('#ukSupplier-yes').is(':checked')) {
            if ($('#manual-address-span-0').text() === 'Enter address manually') {
                $('.address-lines').hide();
            }
            if ($('#manual-address-span-0').text() === 'Look up address') {
                $('.address-lines').show();
            }
            $('#manual-address-0').show();
            $('.uk-address').show();
            $('.non-uk-address').hide();
        }
        if ($('#ukSupplier-no').is(':checked')) {
            $('#manual-address-0, .uk-address').hide();
            $('.address-lines, .non-uk-address').show();
        }
    }

    function supplierRadioChange() {
        $('.address-lines').find('input:text').val('');
        $('.non-uk-address').find('input:text').val('');
        $('.uk-address').find('input:text').val('');
    }

    function partnerDetails() {

        if ($('#entityType-individual').is(':checked')) {
            hideAndClear('.corporate_body:not(.individual)');
            hideAndClear('.sole_trader:not(.individual)');
            $('.individual').show();
        }

        if ($('#entityType-corporate_body').is(':checked')) {
            hideAndClear('.individual:not(.corporate_body)');
            hideAndClear('.sole_trader:not(.corporate_body)');
            $('.corporate_body').show();
        }

        if ($('#entityType-sole_trader').is(':checked')) {
            hideAndClear('.corporate_body:not(.sole_trader)');
            hideAndClear('.individual:not(.sole_trader)');
            $('.sole_trader').show();
        }

        if (!$partnerDetails.is(':checked')) {
            $('.corporate_body, .individual, .sole_trader').hide();
        }
    }

    function hideAndClear(id) {
        $(id).hide();
        $(id).find('input:text').val('');
        $(id).find('input:radio').attr('checked', false).each(function() {
            showHide(this.name);
        });
        $(id).find('input:radio').parent().removeClass('selected');
    }

    function clearById(id) {
        $(id).find('input:text').val('');
        $(id).find('input:radio').attr('checked', false);
        $(id).find('input:radio').parent().removeClass('selected');
    }

    function clearErrors() {
        $('#errors').remove();
        $('.error-notification').remove();
        $('.form-field--error').removeClass('form-field--error');
    }

    function deRegistrationReason() {
        $('#deRegistrationReason-others').is(':checked') ?
            $('#others').show() : $('#others').hide().find('input:text').val('');
    }

    function withdrawalReason() {
        $('#reason-others').is(':checked') ?
            $('#other-withdrawalReason').show() : $('#other-withdrawalReason').hide().find('input:text').val('');
    }

    // each function to read radio buttons and checkboxes on load, change function to track changes
    $radio.each(function() {
        showHide(this.name);
    });
    $radio.on('change', function() {
        showHide(this.name);
    });

    $checkOther.each(function() {
        showHideOther(this.id);
    });
    $checkOther.on('change', function() {
        showHideOther(this.id);
    });

    $checkAll.on('change', function() {
        checkAll(this, this.name);
    });

    $checks.each(function() {
        checks();
    });
    $checks.on('change', function() {
        checks(this.name);
    });

    $exportCheckboxes.on('change', function() {
        exportAlcohol(this.id);
    });

    $personOrCompany.each(function() {
        personOrCompany();
    });
    $personOrCompany.on('change', function() {
        clearById('#directorsAndCompanySecretaries_field');
        personOrCompany();
    });

    $partnerDetails.each(function() {
        partnerDetails();
    });
    $partnerDetails.on('change', function() {
        hideAndClear('.individual');
        hideAndClear('.corporate_body');
        hideAndClear('.sole_trader');
        clearErrors();
        partnerDetails();
    });

    $suppliers.each(function() {
        supplierRadioLoad();
    });
    $suppliers.on('change', function() {
        supplierRadioLoad();
        supplierRadioChange();
    });

    $deRegistrationReason.each(function() {
        deRegistrationReason();
    });
    $deRegistrationReason.on('change', function() {
        deRegistrationReason();
    });

    $withdrawalReason.each(function() {
        withdrawalReason();
    });
    $withdrawalReason.on('change', function() {
        withdrawalReason();
    });

    $('a[rel="external"]').attr('target', '_blank');

     $feedbackLedeLink.on('click', function() {
        $('#get-help-action').trigger('click');
     });
     // ga tagging
     $('#save_and_logout').click(function(){
         ga('send', 'event', this.id, 'click');
     });
     $('input:radio').click(function(){
         ga('send', 'event', this.id, 'click');
     });
     $('input:checkbox').click(function() {
         ga('send', 'event', this.id, 'click');
     });

//     $('#view-application').click(function() {
//        if (this.getAttribute('data-page') == "index") {
//            ga('send', 'event', "view-print-application-index-page", 'click');
//        }
//     });
//
//     $('#print').click(function() {
//        if (this.getAttribute('data-page') == "application-summary") {
//            ga('send', 'event', "print-application-summary-page", 'click');
//        }
//     });

    $('.validation-summary-message a').on('click', function(e){
        e.preventDefault();
            var focusId = $(this).attr('data-focuses');
            thingToFocus = $("#"+focusId.replace(/\./g, '\\\.'));
        $('html, body').animate({
            scrollTop: thingToFocus.parent().offset().top
        }, 500);
        thingToFocus.parent().find('.block-label').first().focus();
        thingToFocus.parent().find('.form-control').first().focus();
    });

    $('#errors').focus();

	$(".skiplink").click(function(event){
		// strip the leading hash and declare
		// the content we're skipping to
		var skipTo="#"+this.href.split('#')[1];
		// Setting 'tabindex' to -1 takes an element out of normal
		// tab flow but allows it to be focused via javascript
		$(skipTo).attr('tabindex', -1).on('blur focusout', function () {
			// when focus leaves this element,
			// remove the tabindex attribute
			$(this).removeAttr('tabindex');
		}).focus(); // focus on the content container
	});

	// ----------------------------------------------------------
	// If you're not in IE (or IE version is less than 5) then:
	// ie === undefined
	// If you're in IE (>=5) then you can determine which version:
	// ie === 7; // IE7
	// Thus, to detect IE:
	// if (ie) {}
	// And to detect the version:
	// ie === 6 // IE6
	// ie > 7 // IE8, IE9, IE10 ...
	// ie < 9 // Anything less than IE9
	// ----------------------------------------------------------
	var ie = (function(){
	    var undef,rv = -1; // Return value assumes failure.
	    var ua = window.navigator.userAgent;
	    var msie = ua.indexOf('MSIE ');
	    var trident = ua.indexOf('Trident/');
	    var edge = ua.indexOf('Edge/');
	    if (msie > 0) {
	        // IE 10 or older => return version number
	        rv = parseInt(ua.substring(msie + 5, ua.indexOf('.', msie)), 10);
	    } else if (trident > 0) {
	        // IE 11 (or newer) => return version number
	        var rvNum = ua.indexOf('rv:');
	        rv = parseInt(ua.substring(rvNum + 3, ua.indexOf('.', rvNum)), 10);
	    } else if (edge > 0) {
			// Edge
			rv = 13
        }
	    return ((rv > -1) ? rv : undef);
	}());

    $("summary").keypress(
        function(event) {
            event = event || window.event
            if (event.preventDefault && ( event.which != 13 && ie) ) {
                event.preventDefault();
            } else { // IE<9 variant:
                event.returnValue = false;
            }
        }
	);

    // Resolve application.js issue with Yes/No radio showing Checkbox Group (Trading Activity)
    if ($('#doYouExportAlcohol-yes').not(':checked') && $('#doYouExportAlcohol-no').not(':checked')) {
        $("#exportLocation-content").hide();
    }
    if ($('#doYouExportAlcohol-no').is(':checked')) {
        $("#exportLocation-content").hide();
    }
    if ($('#doYouExportAlcohol-yes').is(':checked')) {
        $("#exportLocation-content").show();
    }
    $("#doYouExportAlcohol-no").click(function(e) {
        $("#exportLocation-content").hide();
    });
    $("#doYouExportAlcohol-yes").click(function(e) {
        $("#exportLocation-content").show();
    });


});


