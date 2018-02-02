(function($) {
    addFilteringFunctionalityIfBrowserIsIE8();
    var $lookupInput = $('input[id*="postcode"]'),
        $manualAddressLink = $('.address-container .font-small'),
        results = results || {},
        postcodeHistory = postcodeHistory || {},
        lookupRegex = /(([gG][iI][rR] {0,}0[aA]{2})|((([a-pr-uwyzA-PR-UWYZ][a-hk-yA-HK-Y]?[0-9][0-9]?)|(([a-pr-uwyzA-PR-UWYZ][0-9][a-hjkstuwA-HJKSTUW])|([a-pr-uwyzA-PR-UWYZ][a-hk-yA-HK-Y][0-9][abehmnprv-yABEHMNPRV-Y]))) {0,}[0-9][abd-hjlnp-uw-zABD-HJLNP-UW-Z]{2}))$/,
        northernIrelandRegex = /^B{1}T{1}/i,
        env = (!window.location.origin) ? window.location.protocol + "//" + window.location.hostname + (window.location.port ? ':' + window.location.port: '') : window.location.origin,
        lookUpPath = '/fhdds/address-lookup?postcode=',
        auditPath = '/fhdds/submitMainBusinessAddress',
        spinner,
        ajaxSuccess = true,
        data = {
            'addressAudits': []
        },
        detail = {},
        eventType;

    var leftArrow = 37, rightArrow = 39, downArrow = 40, upArrow = 38, enterKey = 13, spaceKey = 32;

    function getId(el) {
        var id = el.id;

        if (id.indexOf(".") >= 0) {
            id = id.replace(".", "\\.");
        }
        return id;
    }

    function updateAudits(auditConfig, fromAddress) {
        var addressContainer = auditConfig.addressContainer,
            eventType = auditConfig.eventType,
            index = auditConfig.index,
            uprn = auditConfig.uprn,
            submitting = auditConfig.submitting,
            id = addressContainer.id,
            $fields = $('#' + id).find('input:text'),
            fieldValues = [],
            fieldId = $('#' + id).find('input:text').attr('id'),
            addressId = fieldId.substr(0, fieldId.indexOf(".")),
            detail = detail || {};

        $fields.each(function() {
            fieldValues.push(this.value);
        });

        detail['auditPointId'] = addressId;
        detail['eventType'] = eventType;
        detail['uprn'] = uprn;
        // we do not want to update the from address when submitting the form so use address passed in
        if (!submitting) {
            detail.fromAddress = {};
            detail.fromAddress.addressLine1 = fieldValues[0];
            detail.fromAddress.addressLine2 = fieldValues[1];
            detail.fromAddress.addressLine3 = fieldValues[2];
            detail.fromAddress.addressLine4 = fieldValues[3];
            if (detail.eventType == 'internationalAddressSubmitted') {
                detail.fromAddress.addressCountry = fieldValues[5];
            } else {
                detail.fromAddress.postcode = fieldValues[4];
            }
        } else {
            detail.fromAddress = fromAddress;
        }

        detail.toAddress = {};
        detail.toAddress.addressLine1 = fieldValues[0];
        detail.toAddress.addressLine2 = fieldValues[1];
        detail.toAddress.addressLine3 = fieldValues[2];
        detail.toAddress.addressLine4 = fieldValues[3];
        if (detail.eventType == 'internationalAddressSubmitted') {
            detail.toAddress.addressCountry = fieldValues[5];
        } else {
            detail.toAddress.postcode = fieldValues[4];
        }

        data.addressAudits.splice(index, 1, detail);
    }

    $('.address-container').each(function(index) {
        updateAudits({
            addressContainer : this,
            eventType : $('#ukSupplier-no').is(':checked') ? 'internationalAddressSubmitted' : 'manualAddressSubmitted',
            index : index,
            uprn : '',
            submitting : false
        });
    });

    function filterData(data) {
        data.addresses = data.addresses.filter(function(value) {
            var valid = true;
            // check town is not longer than 35 chars
            if (value.address.town.length > 35) {
                return false;
            } else {
                // check address lines are not longer than 35 chars
                jQuery.map(value.address.lines, function( line ) {
                    if (line.length > 35) {
                        valid =  false;
                    }
                });

            }
            return valid;
        });
        results = data;
    }

    function buildOptions(data, num) {
        filterData(data);
        var address = data.addresses.length != 1 ? "addresses" : "address";
        var options = '<legend>' + data.addresses.length + ' ' + address + ' found...</legend>';
        jQuery.map(data.addresses, function(results,index) {
            options += '<label id="result-' + num + 'choi' + index + '" class="block-label" for="result-' + num + 'choice' + index + '" value="' + index + '"><input class="postcode-lookup-results-entry" type="radio" id="result-' + num + 'choice' + index + '" name="res" value="' + index + '">' +
                jQuery.map(results.address.lines, function( line,index ) {
                    return index == 0 ? line : ' ' + line;
                }) + ', ' +
                results.address.town + ', ' +
                results.address.postcode +
                '</label>';
            return options;
        });



        $('#result-' + num).html(options);



        $('#result-' + num +'choi0').addClass('selected add-focus').focus();
                var addressSize = data.addresses.length;
                $(dynamicListener(data, num, addressSize));
            }

            function dynamicListener(data, num, addressSize) {
                jQuery.map(data.addresses, function(results,index) {
                    $('#result-' + num + 'choi' + index).on('keydown', function(e) {
                        blockLeftAndRightArrowNavigation(e);

                        if (e.which == downArrow && index < (addressSize - 1)) {
                            higlightNextElement($(this),index + 1,num)
                        }

                        if (e.which == downArrow && index == (addressSize-1)) {
                            higlightNextElement($(this),0,num)
                        }

                        if (e.which == upArrow && index > 0) {
                            higlightNextElement($(this),index - 1,num)
                        }

                        if (e.which == upArrow && index == 0) {
                            higlightNextElement($(this),addressSize - 1,num)
                        }

                    }).on('keypress', function(e) {
                        e.preventDefault();
                        if (e.which == enterKey || e.which == spaceKey) {
                            var $this = $('#' + $(this).attr('id')),
                                num = spinner,
                                $parent = $('#address-' + num),
                                resultIndex = index,
                                results = data;
                            $('#postcode-lookup-button-' + num).hide();
                            fillAddressFields($this, $parent, resultIndex, results,num);
                        }
                    }).on('mouseup touchend', function() {
                        var $this = $('#' + this.id).find("input"),
                            num = spinner,
                            $parent = $('#address-' + num),
                            resultIndex = this.getAttribute('value');
                        $('#postcode-lookup-button-' + num).hide();
                        fillAddressFields($this, $parent, resultIndex, data,num)
                    });
                });
            }

            function fillAddressFields(elem, parent, resultIndex, results,num) {
                if (elem.val() != 'first') {
                    parent.find('input[id*="addressLine"]').val('');
                    parent.find('.address-lines').show();

                    uprn = results.addresses[resultIndex].id;

                    var townFieldNumber = results.addresses[resultIndex].address.lines.length + 1;

                    jQuery.map(results.addresses[resultIndex].address.lines, function(line,index) {
                        parent.find('input[id$=addressLine' + (index + 1) + ']').val(line);
                    });

                    parent.find('input[id$=addressLine' + townFieldNumber + ']').val(results.addresses[resultIndex].address.town);
                    parent.find('input[id*="postcode"]').focus().val(results.addresses[resultIndex].address.postcode);

                    parent.find('.postcode-lookup').hide();

                    parent.find('.link-style').text('Look up address');

                    parent.each(function() {
                        updateAudits({
                            addressContainer : this,
                            eventType : 'postcodeAddressSubmitted',
                            index : resultIndex,
                            uprn : uprn,
                            submitting : false
                        });
                    });

                    clearResults(resultIndex);
                    $('.dropdown-menu').empty();
                    $('#address-' + num +' input:first').addClass('selected add-focus').focus()
                }
    }

    function showResult(data, num) {
        buildOptions(data, num);
        if (data.addresses.length == 0) {
            showErrorMessage('No results found, check postcode and try again.', num);
            $(".dropdown-menu").hide();
        } else {
            $('#result-' + num).addClass('show').focus();
            $(".dropdown-menu").show();
            hideErrorMessage(num);
            $(".postcode-results-fieldset").find("input").first().prop("checked",true)
        }
    }

    function clearResults(num) {
        var $result = $('#result-' + num);
        $result.removeClass('show').find('option').remove();
    }

    function higlightNextElement(element,index,num){
        $('#' + element.attr('id')).removeClass('selected add-focus');
        $('#result-' + num + 'choi' + index).addClass('selected add-focus').focus();
    }

    function removeResultCount(){
         $('.postcode-results-fieldset').find("legend").empty();
    }

    function clearSearchResults(){
         $(".postcode-results-fieldset").find("label").each(function() {
           $( this ).remove();
         });
    }

    function showErrorMessage(message, num) {
        var $postcodeLookupWrapper = $('#postcode-lookup-button-' + num).parent('div');

        clearSearchResults();
        removeResultCount();

        if ($postcodeLookupWrapper.hasClass('form-field--error')) {
            $postcodeLookupWrapper.find('.error-notification').text(message);
        }

        if (!$postcodeLookupWrapper.hasClass('form-field--error')) {
            $postcodeLookupWrapper.addClass('form-field--error');

            $postcodeLookupWrapper.find('label').prepend(
                '<span class="error-notification" role="tooltip" data-journey="search-page:error:additionalAddress.postcode">' + message + '</span>'
            );
        }
        $('.postcode-results-fieldset').hide();
        clearResults(num);
        $postcodeLookupWrapper.find('input[type="text"]').focus();
    }

    function hideErrorMessage(num) {
        $('.postcode-results-fieldset').show();

        var $address = $('#postcode-lookup-button-' + num).parents('#address-' + num);

        if ($address.find('div.form-field').hasClass('form-field--error')) {
            $address.find('.error-notification').remove();
        }
        if ($address.data("attribute") == "panel-indent") {
            $address.find('div.form-field').removeClass('form-field--error').addClass('panel-indent');
        } else {
            $address.find('div.form-field').removeClass('form-field--error');
        }
    }

    function clearAddress(num) {
        var $address = $('#postcode-lookup-button-' + num).parents('#address-' + num);

        $address.find('.address-lines input').val('');
    }

    function doError(jqXHR, num) {
        if (jqXHR.status == 0) {
            showErrorMessage('Your session has timed out.', num);
        }
        if (jqXHR.status == 400) {
            showErrorMessage('No results found, check postcode and try again.', num);
        }
        if (jqXHR.status == 404) {
            showErrorMessage('Not found. Unknown URI accessed.', num);
        }
        if (jqXHR.status == 405) {
            showErrorMessage('Bad method. Unacceptable method used.', num);
        }
        if (jqXHR.status == 500) {
            showErrorMessage('Postcode Lookup is currently unavailable. Enter your address manually or try again later.', num);
        }
    }

    function validation(postcode, url, num, id) {
        var valid = true;
        // check not empty
        if (postcode == '') {
            valid = false;
            showErrorMessage('Postcode must not be empty', num);
        }

        // check for illegal chars
        if (valid && !postcode.match(lookupRegex)) {
            valid = false;
            showErrorMessage('The postcode is not valid, check the postcode and try again', num);
        }

        // exclude NI postcodes
        if (valid && postcode.match(northernIrelandRegex)) {
            valid = false;
            showErrorMessage('Currently, we cannot return any results for a Northern Ireland postcode', num);
        }

        // only do ajax call when postcode is entered and changes and passes regex
        if (valid) {
            if(ajaxSuccess){
                hideErrorMessage(num);
            }
            searchAddress(url, num);
        }
    }

    function searchAddress(url, num) {
        spinner = num;
        $.ajax({
            type: 'GET',
            url: url,
            dataType: "json",
            success: function(data) {
                ajaxSuccess = true;
                showResult(data, num);
            },
            error: function(jqXHR) {
                ajaxSuccess = false;
                doError(jqXHR, num);
            },
            headers: {"X-Hmrc-Origin": "awrs"}
        });
    }
    
    function auditEvents(url, data, form) {
        $.ajax({
            type: 'POST',
            url: url,
            data: JSON.stringify(data),
            contentType: 'application/json',
            processData: false,
            success: function() {
                form.submit();
            },
            error: function() {
                form.submit();
            },
            headers: {"X-Hmrc-Origin": "awrs"}
        });
    }

    function manualLookupHandler(el, num) {
        var $this = el;

        postcodeHistory = {};

        $this.text() == 'Enter address manually' ? $this.text('Look up address') : $this.text('Enter address manually');

        if ($this.text() == 'Look up address') {
            $('.postcode-results-fieldset').hide();
            $('#address-' + num + ' .address-lines').show();
            $('#address-' + num + ' input[id$="addressLine1"]').focus();
            $('#result-' + spinner + '_field').attr('aria-hidden', 'true');
            $('#postcode-lookup-button-' + num).hide();
        } else {
            $('.postcode-results-fieldset').show();
            $('#address-' + num + ' .address-lines').hide();
            $('#postcode-lookup-button-' + num).show();
            $('#address-' + num + ' input[id$="postcode"]').focus();
        }

        clearResults(num);
    }

    function blockLeftAndRightArrowNavigation(e) {
        if (e.which == leftArrow || e.which == rightArrow) {
            e.preventDefault();
        }
    }

    function addFilteringFunctionalityIfBrowserIsIE8(){
        if (!Array.prototype.filter) {
            Array.prototype.filter = function(fun/*, thisArg*/) {
                'use strict';

                if (this === void 0 || this === null) {
                    throw new TypeError();
                }

                var t = Object(this);
                var len = t.length >>> 0;
                if (typeof fun !== 'function') {
                    throw new TypeError();
                }

                var res = [];
                var thisArg = arguments.length >= 2 ? arguments[1] : void 0;
                for (var i = 0; i < len; i++) {
                    if (i in t) {
                        var val = t[i];

                        // NOTE: Technically this should Object.defineProperty at
                        //       the next index, as push can be affected by
                        //       properties on Object.prototype and Array.prototype.
                        //       But that method's new, and collisions should be
                        //       rare, so use the more-compatible alternative.
                        if (fun.call(thisArg, val, i, t)) {
                            res.push(val);
                        }
                    }
                }

                return res;
            };
        }
    }

    $manualAddressLink.show();
    $('.postcode-lookup').show();

    // load state
    $('.address-container').each(function() {
        var id = this.id,
            num = id.substr(id.length - 1),
            $this = $('#' + this.id),
            $addressLines = $($this).find('.address-lines'),
            $manualLookupSpan = $($this).find('#manual-address-span-' + num),
            $postcodeLookupButton = $($this).find('#postcode-lookup-button-' + num);

        // if address lines pull back data, show them and set lookup accordingly
        if ($addressLines.find('input').length == $addressLines.find('input[value=""]').length && $addressLines.find('div.form-field--error').length == 0) {
            $addressLines.hide();
            $manualLookupSpan.text('Enter address manually');
            $postcodeLookupButton.show();
        }
        else {
            $addressLines.show();
            $manualLookupSpan.text('Look up address');
            $postcodeLookupButton.hide();
        }

    });

    $(document).ajaxStart(function() {
        $('#spinner-' + spinner).show();
        $('#result-' + spinner + '_field').removeAttr('aria-hidden');
    }).ajaxStop(function() {
        $('#spinner-' + spinner).hide();
    });

    $lookupInput.on('focus', function() {
        $('#' + getId(this)).next('a').filter(':visible').addClass('postcode-lookup-color-change');
    });

    $lookupInput.on('focusout', function() {
        $('#' + getId(this)).next('a').filter(':visible').removeClass('postcode-lookup-color-change');
    });

    $('input[id*="postcode"]').on('keydown, keyup, keypress', function(e) {
        var $this = $('#' + getId(this));

        if (e.which == enterKey) {
            e.preventDefault();
            $this.next('a').filter(':visible').click();
            return false;
        }
    });

    $('.link-style').on('click', function() {
        var id = this.id,
            $this = $('#' + this.id),
            num = id.substr(id.length - 1);

        hideErrorMessage(num);
        clearAddress(num);
        manualLookupHandler($this, num);
    });

    $('.link-style').on('keydown, keyup, keypress', function(e) {
        var id = this.id,
            $this = $('#' + this.id),
            num = id.substr(id.length - 1);

        if (e.which == spaceKey || e.which == enterKey) {
            e.preventDefault();
            hideErrorMessage(num);
            clearAddress(num);
            manualLookupHandler($this, num);
        }
    });

    // this function is added to allow the post code to be searched again if say the user accidentally hide the form
    // section and cleared the data
    // in this model we allow the user to perform additional ajax calls as long as there has been a change in the search
    // input box
    $('input[id*="postcode"]').on('input', function() {
        postcodeHistory = {};
    });

    $('.postcode-lookup').on('click', function() {
        var postcode = $(this).prev().val().replace(/\s/g,''),
            url = env + lookUpPath + postcode,
            id = getId(this),
            num = id.substr(id.length -1);

        validation(postcode, url, num, id);
    });

    $('.postcode-lookup').on('keydown, keyup, keypress', function(e) {
        var postcode = $(this).prev().val().replace(/\s/g,''),
            url = env + lookUpPath + postcode,
            id = getId(this),
            num = this.id.substr(this.id.length -1);

        if (e.which == spaceKey || e.which == enterKey) {
            validation(postcode, url, num, id);
            return false;
        }
    });

    $('form').submit(function(e){
        e.preventDefault();
        var form = this;

        $('.address-container').each(function(index) {
            var audit = data.addressAudits[index];

            updateAudits({
                addressContainer : this,
                eventType : $('#ukSupplier-no').is(':checked') ? 'internationalAddressSubmitted' : audit.eventType,
                index : index,
                uprn : audit.uprn,
                submitting : true
            }, audit.fromAddress);
        });

        auditEvents(env + auditPath, data, form);
    });
})(jQuery);