package com.savoia.productapi.utils;

import org.springframework.context.annotation.Bean;

import java.security.SecureRandom;
import java.util.Base64;

public class StringUtility {


    public static String cleanString(String string) {
        if (string == null) {
            return null;
        }
        return string.toLowerCase().trim();
    }

    public static String cleanStringUpper(String string){
        if (string == null) {
            return null;
        }
        return string.toUpperCase().trim();
    }

    public static String formatText(String text) {
        return StringUtility.capitalizeFirstLetter(StringUtility.cleanString(text));
    }

    public static String capitalizeFirstLetter(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }

        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

}