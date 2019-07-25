/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.support.v7.mms;

import android.os.Build;
import android.util.Log;

import com.android.i18n.phonenumbers.NumberParseException;
import com.android.i18n.phonenumbers.PhoneNumberUtil;
import com.android.i18n.phonenumbers.Phonenumber;

/**
 * Helper methods for phone number formatting
 * This is isolated into a standalone class since it depends on libphonenumber
 */
public class PhoneNumberHelper {
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            return phoneNumberUtil.parse((CharSequence) phoneText,country);
        } else {
            return phoneNumberUtil.parse(phoneText,country);
        }
    }
}
