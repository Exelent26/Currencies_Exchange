package service;

import dto.CurrencyDto;
import dto.ExchangeDto;
import dto.ExchangeRateDto;
import exception.ServiceException;
import util.DataValidator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

import static util.Constants.USD;

public class ExchangeService {
    private ExchangeService() {
    }

    private static final ExchangeService INSTANCE = new ExchangeService();

    public static ExchangeService getInstance() {
        return INSTANCE;
    }

    public ExchangeDto performExchange(String baseCurrency, String targetCurrency, String amountString) {

        DataValidator dataValidator = DataValidator.getInstance();
        ExchangeRateService exchangeRateService = ExchangeRateService.getInstance();

        CurrencyService currencyService = CurrencyService.getInstance();

        if (dataValidator.isNullOrBlank(baseCurrency, targetCurrency)) {
            throw new ServiceException("Base or target currency is null or blank", ServiceException.ErrorCode.VALIDATION_ERROR);
        }
        if (!dataValidator.isAmountValid(amountString)) {
            throw new ServiceException("Amount is not valid or missing ", ServiceException.ErrorCode.VALIDATION_ERROR);
        }

        Optional<CurrencyDto> baseCurrencyByCode = currencyService.getCurrencyByCode(baseCurrency);
        if (baseCurrencyByCode.isEmpty()) {
            throw new ServiceException("Base currency incorrect or missing in currencies table", ServiceException.ErrorCode.NOT_FOUND);

        }
        Optional<CurrencyDto> targetCurrencyByCode = currencyService.getCurrencyByCode(targetCurrency);
        if (targetCurrencyByCode.isEmpty()) {
            throw new ServiceException("Target currency incorrect or is missing in currencies table", ServiceException.ErrorCode.NOT_FOUND);

        }

        BigDecimal amount = exchangeRateService.getRateFromString(amountString);

        Optional<ExchangeRateDto> exchangeRateByCode = exchangeRateService.getExchangeRateByCode(baseCurrency, targetCurrency);
        if (exchangeRateByCode.isPresent()) {
            ExchangeRateDto exchangeRateDto = exchangeRateByCode.get();

            return exchangeRateService.getExchangeDto(amount, exchangeRateDto);
        }
        Optional<ExchangeRateDto> reverseExchangeRate = exchangeRateService.getExchangeRateByCode(targetCurrency, baseCurrency);
        if (reverseExchangeRate.isPresent()) {

            ExchangeRateDto reverseExchangeRateDto = reverseExchangeRate.get();
            ExchangeRateDto requestedExchangeRate = exchangeRateService.createReverseExchangeRate(reverseExchangeRateDto);

            return exchangeRateService.getExchangeDto(amount, requestedExchangeRate);

        }
        Optional<ExchangeRateDto> baseCurrencyToUsd = exchangeRateService.getExchangeRateByCode(baseCurrency, USD);
        Optional<ExchangeRateDto> usdToTarget = exchangeRateService.getExchangeRateByCode(USD, targetCurrency);
        if (baseCurrencyToUsd.isPresent() && usdToTarget.isPresent()) {
            ExchangeRateDto exchangeRateBaseToUsd = baseCurrencyToUsd.get();
            ExchangeRateDto exchangeRateUsdToTargetCurrency = usdToTarget.get();

            return getExchangeViaUsdDirect(exchangeRateBaseToUsd, exchangeRateUsdToTargetCurrency, amount);

        }

        Optional<ExchangeRateDto> usdToBase = exchangeRateService.getExchangeRateByCode(USD, baseCurrency);
        if (usdToTarget.isPresent() && usdToBase.isPresent()) {
            ExchangeRateDto exchangeRateUsdToBase = usdToBase.get();
            ExchangeRateDto exchangeRateUsdToTarget = usdToTarget.get();

            return getExchangeViaUsdReverse(exchangeRateUsdToBase, exchangeRateUsdToTarget, amount);


        } else {
            throw new ServiceException("Can't find exchange rate", ServiceException.ErrorCode.NOT_FOUND);
        }
    }

    private static ExchangeDto getExchangeViaUsdDirect(ExchangeRateDto exchangeRateFromBaseToUsd, ExchangeRateDto exchangeRateUsdToTarget, BigDecimal amount) {

        BigDecimal baseToUsdRate = exchangeRateFromBaseToUsd.rate();

        BigDecimal usdToTargetRate = exchangeRateUsdToTarget.rate();

        BigDecimal rateForExchange = baseToUsdRate.multiply(usdToTargetRate);

        BigDecimal calculatedAmount = (rateForExchange).multiply(amount);

        return new ExchangeDto(exchangeRateFromBaseToUsd.baseCurrency(), exchangeRateUsdToTarget.targetCurrency(), rateForExchange, amount, calculatedAmount);
    }

    private static ExchangeDto getExchangeViaUsdReverse(ExchangeRateDto usdToBase, ExchangeRateDto usdToTarget, BigDecimal amount) {

        BigDecimal usdToBaseRate = usdToBase.rate();
        BigDecimal usdToTargetRate = usdToTarget.rate();

        BigDecimal rate = (BigDecimal.ONE.divide(usdToBaseRate, 2, RoundingMode.HALF_UP)).multiply(usdToTargetRate);
        BigDecimal convertedAmount = rate.multiply(amount);
        return new ExchangeDto(usdToBase.targetCurrency(), usdToTarget.targetCurrency(), rate, amount, convertedAmount);


    }
}
