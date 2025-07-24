package util;

import entity.Currency;
import exception.ServiceException;

import java.math.BigDecimal;

public class DataValidator {

    private static final DataValidator INSTANCE = new DataValidator();

    public DataValidator() {};

    public static DataValidator getInstance() {
        return INSTANCE;
    }

    public Currency validateCurrencyData(String currencyCode, String name, String sign) {
        validateCurrencyCode(currencyCode);

        validateCurrencyName(name);

        validateCurrencySign(sign);

        return new Currency(currencyCode, name, sign);
    }

    private static void validateCurrencySign(String sign) {
        if (sign.length() > 32) {
            throw new ServiceException("SIGN TOO LONG", ServiceException.ErrorCode.VALIDATION_ERROR);
        }
    }

    private static void validateCurrencyName(String name) {
        if (name.length() > 50) {
            throw new ServiceException("FullName TOO LONG", ServiceException.ErrorCode.VALIDATION_ERROR);
        }
    }
    public boolean isRatePositive(BigDecimal rate) {

        return rate.compareTo(BigDecimal.ZERO) > 0;

    }

    public static void validateCurrencyCode(String... currencyCodes) {
        for (String currencyCode : currencyCodes) {
            if (currencyCode.length() != 3) {
                throw new ServiceException("INVALID CODE", ServiceException.ErrorCode.VALIDATION_ERROR);
            }
            if (currencyCode.isBlank()) {
                throw new ServiceException("Currency code is blank", ServiceException.ErrorCode.VALIDATION_ERROR);
            }

            if (!(currencyCode.toUpperCase().matches("[A-Z]{3}"))) {
                throw new ServiceException("INVALID CODE", ServiceException.ErrorCode.VALIDATION_ERROR);
            }
            try {
                java.util.Currency.getInstance(currencyCode);
            } catch (IllegalArgumentException e) {
                throw new ServiceException("Currency code is not valid", ServiceException.ErrorCode.VALIDATION_ERROR);
            }
        }
    }
    public boolean isAmountValid(String amountString) {
         if(this.isNullOrBlank(amountString)){
             return false;
         }
        try {
            BigDecimal amount = new BigDecimal(amountString);
            return amount.compareTo(BigDecimal.ZERO) > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public boolean isExchangeRatePathInvalid(String path) {
        return isPathNullOrBlank(path) || (path.length() != 7) || isPathHasIncorrectStart(path);
    }

    public boolean isCurrencyPathInvalid(String path) {
        return isPathNullOrBlank(path) || (path.length() != 4) || isPathHasIncorrectStart(path);
    }

    private static boolean isPathInvalid(String path) {
        return path == null || !path.startsWith("/");
    }

    private  static boolean isPathNullOrBlank(String path) {
        return path == null || path.isBlank();
    }

    private static boolean isPathHasIncorrectStart(String path) {
        return !path.startsWith("/");
    }



    public boolean isNullOrBlank(String... strings) {
        for (String string : strings) {
            if (string == null || string.isBlank()) {
                return true;
            }
        }
        return false;
    }




}
