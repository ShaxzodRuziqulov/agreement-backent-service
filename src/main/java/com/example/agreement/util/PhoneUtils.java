package com.example.agreement.util;

public class PhoneUtils {

    private PhoneUtils() {}

    public static String normalize(String phoneNumber) {
        if (phoneNumber == null) {
            return null;
        }

        String cleaned = phoneNumber.replaceAll("[^0-9]", "");

        if (cleaned.startsWith("998")) {
            return cleaned;
        }

        if (cleaned.startsWith("0")) {
            return "998" + cleaned.substring(1);
        }

        return "998" + cleaned;
    }
}
