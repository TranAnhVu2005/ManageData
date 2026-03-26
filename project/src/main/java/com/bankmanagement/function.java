package com.bankmanagement;

import java.security.SecureRandom;

public class function {
    public static String generateStringRandom(int length) {
        SecureRandom random = new SecureRandom();
        StringBuilder result = new StringBuilder();

        result.append(random.nextInt(9) + 1);
        for (int i = 0; i < length - 1; i++) {
            result.append(random.nextInt(10));
        }
        return result.toString();
    }
}
