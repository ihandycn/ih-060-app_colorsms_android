package android.support.v7.mms;

import android.os.Build;
import android.util.Log;

import com.android.i18n.phonenumbers.NumberParseException;
import com.android.i18n.phonenumbers.PhoneNumberUtil;
import com.android.i18n.phonenumbers.Phonenumber;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Helper methods for phone number formatting
 * This is isolated into a standalone class since it depends on libphonenumber
 */
public class PhoneNumberHelper {
    private static final String KOREA_ISO_COUNTRY_CODE = "KR";
    private static final String JAPAN_ISO_COUNTRY_CODE = "JP";

    /**
     * Given a phone number, get its national part without country code
     *
     * @param number  the original number
     * @param country the country ISO code
     * @return the national number
     */
    static String getNumberNoCountryCode(final String number, final String country) {
        final PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
        final Phonenumber.PhoneNumber parsed = getParsedNumber(phoneNumberUtil, number, country);
        if (parsed == null) {
            return number;
        }
        return phoneNumberUtil
                .format(parsed, PhoneNumberUtil.PhoneNumberFormat.NATIONAL)
                .replaceAll("\\D", "");
    }

    // Parse the input number into internal format
    private static Phonenumber.PhoneNumber getParsedNumber(PhoneNumberUtil phoneNumberUtil,
                                                           String phoneText, String country) {
        try {
            final Phonenumber.PhoneNumber phoneNumber = parse(phoneNumberUtil, phoneText, country);
            if (phoneNumberUtil.isValidNumber(phoneNumber)) {
                return phoneNumber;
            } else {
                Log.e("PhoneNumbnerHelper", "getParsedNumber: not a valid phone number"
                        + " for country " + country);
                return null;
            }
        } catch (final NumberParseException e) {
            Log.e("PhoneNumbnerHelper", "getParsedNumber: Not able to parse phone number");
            return null;
        }
    }

    public static Phonenumber.PhoneNumber parse(PhoneNumberUtil phoneNumberUtil,
                                                String phoneText, String country) throws NumberParseException {
        try {
            return phoneNumberUtil.parse((CharSequence) phoneText, country);
        } catch (NoSuchMethodError e) {
            try {
                return phoneNumberUtil.parse(phoneText, country);
            } catch (NoSuchMethodError e1) {
                try {
                    Method method = phoneNumberUtil.getClass().getMethod("parseHelper",
                            CharSequence.class, String.class, boolean.class, boolean.class, Phonenumber.PhoneNumber.class);
                    method.setAccessible(true);
                    Phonenumber.PhoneNumber pn = null;
                    method.invoke(phoneNumberUtil, (CharSequence)phoneText, country, false, true, pn);
                    return pn;
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e2) {
                    Log.d("---->>>>", "parse Error: " + e2.getMessage());
                    e2.printStackTrace();
                }
            }
        }
        return new Phonenumber.PhoneNumber();
    }

    public static Phonenumber.PhoneNumber parseAndKeepRawInput(PhoneNumberUtil phoneNumberUtil,
                                                               String phoneText, String country) throws NumberParseException {
        try {
            return phoneNumberUtil.parseAndKeepRawInput((CharSequence) phoneText, country);
        } catch (NoSuchMethodError e) {
            try {
                return phoneNumberUtil.parseAndKeepRawInput(phoneText, country);
            } catch (NoSuchMethodError e1) {
                try {
                    Log.d("---->>>>", "Input: " + phoneText);
                    Method method = phoneNumberUtil.getClass().getMethod("parseHelper",
                            CharSequence.class, String.class, boolean.class, boolean.class, Phonenumber.PhoneNumber.class);
                    method.setAccessible(true);
                    //Class cl = Class.forName("com.android.i18n.phonenumbers.Phonenumber$PhoneNumber");

                    //Phonenumber.PhoneNumber pn = (Phonenumber.PhoneNumber) cl.newInstance();
                    Phonenumber.PhoneNumber pn = null;
                    method.invoke(phoneNumberUtil, (CharSequence)phoneText, country, true, true, pn);
                    //Log.d("---->>>>", "Output: " + pn.getCountryCode());
                    return pn;
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
        }

        return new Phonenumber.PhoneNumber();
    }

    public static String formatNumber(String phoneNumber, String countryIso) {
        // Do not attempt to format numbers that start with a hash or star symbol.
        if (phoneNumber.startsWith("#") || phoneNumber.startsWith("*")) {
            return phoneNumber;
        }

        PhoneNumberUtil util = PhoneNumberUtil.getInstance();
        String result;
        Phonenumber.PhoneNumber pn;
        try {
            pn = PhoneNumberHelper.parseAndKeepRawInput(util, phoneNumber, countryIso);
            if (KOREA_ISO_COUNTRY_CODE.equalsIgnoreCase(countryIso) &&
                    (pn.getCountryCode() == util.getCountryCodeForRegion(KOREA_ISO_COUNTRY_CODE)) &&
                    (pn.getCountryCodeSource() ==
                            Phonenumber.PhoneNumber.CountryCodeSource.FROM_NUMBER_WITH_PLUS_SIGN)) {
                /**
                 * Need to reformat any local Korean phone numbers (when the user is in Korea) with
                 * country code to corresponding national format which would replace the leading
                 * +82 with 0.
                 */
                result = util.format(pn, PhoneNumberUtil.PhoneNumberFormat.NATIONAL);
            } else if (JAPAN_ISO_COUNTRY_CODE.equalsIgnoreCase(countryIso) &&
                    pn.getCountryCode() == util.getCountryCodeForRegion(JAPAN_ISO_COUNTRY_CODE) &&
                    (pn.getCountryCodeSource() ==
                            Phonenumber.PhoneNumber.CountryCodeSource.FROM_NUMBER_WITH_PLUS_SIGN)) {
                /**
                 * Need to reformat Japanese phone numbers (when user is in Japan) with the national
                 * dialing format.
                 */
                result = util.format(pn, PhoneNumberUtil.PhoneNumberFormat.NATIONAL);
            } else {
                result = util.format(pn, PhoneNumberUtil.PhoneNumberFormat.NATIONAL);
            }
        } catch (NumberParseException e) {
            result = phoneNumber;
        }
        return result;
    }
}
