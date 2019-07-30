package com.android.i18n.phonenumbers;

public class Phonenumber {
    public static class PhoneNumber {
        public int getCountryCode() {
            return 0;
        }

        public Phonenumber.PhoneNumber.CountryCodeSource getCountryCodeSource() {
            return null;
        }

        public enum CountryCodeSource {
            FROM_NUMBER_WITH_PLUS_SIGN,
            FROM_NUMBER_WITH_IDD,
            FROM_NUMBER_WITHOUT_PLUS_SIGN,
            FROM_DEFAULT_COUNTRY;

            CountryCodeSource() {
            }
        }
    }
}
