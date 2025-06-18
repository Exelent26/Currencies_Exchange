package dto;

import entity.Currency;

public record CurrenciesPair(
        String baseCode,
        String targetCode
) {
    public CurrenciesPair {
        validateCode(baseCode);
        validateCode(targetCode);
        if (baseCode.equals(targetCode)) {
            throw new IllegalArgumentException("Currencies must be different");
        }
    }

    private static void validateCode(String code) {
        if (code == null || code.length() != 3 || !code.matches("[A-Z]{3}")) {
            throw new IllegalArgumentException("Invalid currency code: " + code);
        }
    }


    public static CurrenciesPair createCurrencyPairFromPath(String path) {
        return new CurrenciesPair(path.substring(1,4),path.substring(4,7));
    }

}