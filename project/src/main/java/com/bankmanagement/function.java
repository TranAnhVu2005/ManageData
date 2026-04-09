package com.bankmanagement;

import java.security.SecureRandom;

import com.bankmanagement.dao.UserAccoutsDAO;

public class function {
    public static String generateStringRandom(int length, String tableName, String columnName) {
        SecureRandom random = new SecureRandom();

        while (true) {
            StringBuilder result = new StringBuilder();

            // số đầu không phải 0
            result.append(random.nextInt(9) + 1);

            for (int i = 1; i < length; i++) {
                result.append(random.nextInt(10));
            }

            String generated = result.toString();

            // nếu không cần check DB
            if (tableName == null || columnName == null) {
                return generated;
            }

            // nếu chưa tồn tại trong DB thì trả về
            if (!UserAccoutsDAO.existedString(tableName, columnName, generated)) {
                return generated;
            }
        }
    }
}
