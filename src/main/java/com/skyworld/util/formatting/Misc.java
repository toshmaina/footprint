package com.skyworld.util.formatting;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberToCarrierMapper;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.util.Locale;


public final class Misc {

    private static final PhoneNumberUtil PHONE_UTIL =
            PhoneNumberUtil.getInstance();

    private static final PhoneNumberToCarrierMapper CARRIER_MAPPER =
            PhoneNumberToCarrierMapper.getInstance();

    private Misc() {
    }

    public static String formatPhone(String phoneNumber, String countryCode) {
        if (phoneNumber == null || countryCode == null) {
            return "INVALID";
        }

        try {
            String region = countryCode.trim().toUpperCase(Locale.ROOT);

            Phonenumber.PhoneNumber number =
                    PHONE_UTIL.parse(phoneNumber, region);

            // Number must conform to the country's numbering plan.
            if (!PHONE_UTIL.isValidNumberForRegion(number, region)) {
                return "INVALID";
            }

            // Number must have a known original carrier. WAS causing false negatives.
//            String carrier = CARRIER_MAPPER.getNameForNumber(
//                    number,
//                    Locale.ENGLISH
//            );
//
//            if (carrier == null || carrier.isBlank()) {
//                return "INVALID";
//            }

            return PHONE_UTIL
                    .format(number, PhoneNumberUtil.PhoneNumberFormat.E164)
                    .substring(1);

        } catch (NumberParseException e) {
            return "INVALID";
        }
    }
}
