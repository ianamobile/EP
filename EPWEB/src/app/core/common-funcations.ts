import { IanaConfig } from './../shared/models/iana-config';
export function strProperCase(event) {
    if (event && event.target.value)
        event.target.value = event.target.value.toLowerCase().replace(/^(.)|\s(.)/g,
            function ($1) { return $1.toUpperCase(); });
}

export function strTrim(event) {
    if (event && event.target.value)
        event.target.value = event.target.value.replace(/^\s+|\s+$/g, '');
}

export function strProperCaseComp(event) {
    if (event && event.target.value) {
        strProperCase(event);
        var elem = event.target.value;
        var myRegExp = /Llc/;
        if (elem.search(myRegExp) != -1) {
            event.target.value = elem.replace('Llc', 'LLC');
        } else {
            return strProperCase(event);
        }
    }
}

export function strProperCaseTitle(event) {
    if (event && event.target.value) {
        var elem = event.target.value;
        var myRegExp = /CEO|Ceo|ceo|VP|Vp|vp|AVP|Avp|avp|CFO|Cfo|cfo|AVC|Avc|avc/;
        if (elem.search(myRegExp) != -1) {
            event.target.value = elem.toUpperCase();
        } else {
            return strProperCase(event);
        }
    }
}

export function strProperCaseSuffix(event) {
    if (event && event.target.value) {
        strProperCase(event);
        var elem = event.target.value;
        var myRegExp = /iii/;
        var myRegExp1 = /ii/;
        var myRegExp2 = /Iii/;
        var myRegExp3 = /Ii/;
        if (elem.search(myRegExp) != -1) {
            event.target.value = elem.replace('iii', 'III');
        } else if (elem.search(myRegExp1) != -1) {
            event.target.value = elem.replace('ii', 'II');
        } else if (elem.search(myRegExp2) != -1) {
            event.target.value = elem.replace('Iii', 'III');
        } else if (elem.search(myRegExp3) != -1) {
            event.target.value = elem.replace('Ii', 'II');
        } else {
            return strProperCase(event);
        }
    }
}

export function pobox(event) {
    if (event.target.value) {
        var value = event.target.value
        value = value.replace('Po ', 'PO');
        value = value.replace('P.o.', 'P.O.');
        value = value.replace('P.o', 'P.O');
        value = value.replace('Rr ', 'RR');
        value = value.replace('R.r.', 'R.R.');
        value = value.replace('R.r', 'R.R');
        value = value.replace('N.w', 'N.W');
        value = value.replace('N.w.', 'N.W.');
        value = value.replace('Nw ', 'NW');
        value = value.replace('N.e', 'N.E');
        value = value.replace('N.e.', 'N.E.');
        value = value.replace('Ne ', 'NE');
        value = value.replace('S.e', 'S.E');
        value = value.replace('S.e.', 'S.E.');
        value = value.replace('Se ', 'SE');
        value = value.replace('S.w ', 'S.W');
        value = value.replace('S.w.', 'S.W.');
        value = value.replace('Sw ', 'SW');
        event.target.value = value;
    }
}


export function valid_phone(event) {
    if (event.target.value) {
        var ph_no = event.target.value;
        var regexObj = /^\(?([0-9]{3})[\(\)]{0,2}?[-. ]?([0-9]{3})[-. ]?([0-9]{4})?[-. ]?\s?( Ext: \d{1,5})?$/;
        if (regexObj.test(ph_no)) {
            var formattedPhoneNumber = ph_no.replace(regexObj, "($1)$2-$3$4");
            event.target.value = formattedPhoneNumber;
            return true;
        } else {
            if (ph_no != "") {
                // event.target.value = "";
                // alert("Invalid Phone Number");
            }
        }
    }
}

export function myFormat(event, filter) {
    var key;
    var maxLength = filter.length;
    if (window.event || !event.which)
        key = event.keyCode; // for IE, same as window.event.keyCode
    else if (event)
        key = event.which; // netscape
    else
        return true;

    if (key == 8 || key == 0 || key == 9 || key == 13) // let user to enter: backspace, enter, tab 
        return true;
    if (key >= 48 && key <= 57) { //ignore rest
        if (event.target.value.length >= maxLength) return false; //ignore char more than max limit
        for (var r = 0; r <= maxLength; r++)
            if (filter.charAt(r) != "#")
                if (event.target.value.length == r)
                    event.target.value = event.target.value + filter.charAt(r);
        return true;
    }
    return false;
}

export function valid_fax(event) {
    var ph_no = event.target.value;
    var regexObj = /^\(?([0-9]{3})[\(\)]{0,2}?[-. ]?([0-9]{3})[-. ]?([0-9]{4})?[-. ]?\s?( Ext: \d{1,5})?$/;
    if (regexObj.test(ph_no)) {
        var formattedPhoneNumber = ph_no.replace(regexObj, "($1)$2-$3$4");
        event.target.value = formattedPhoneNumber;
        return true;
    } else {
        if (ph_no != "") {
            // alert("Invalid Fax Number");
            // event.target.value = "";
        }
    }
}

export function trimEmail(event) {
    var key;
    if (window.event || !event.which) key = event.keyCode; // for IE, same as window.event.keyCode
    else if (event) key = event.which; // netscape
    else return true;
    if ((key == 32) || (key == 17)) {
        var temp = event.target.value;
        event.target.value = trim1(temp);
    }
}

export function trim1(str) {
    return str.replace(/^\s+|\s+$/g, '');
}

export function setupPageLayout(ianaConfig: IanaConfig, pageTypeOpt: boolean) {
    ianaConfig.isUserLoggedIn = pageTypeOpt;
}