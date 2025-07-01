package main.service;

import main.dto.ExchangeDto;
import main.dto.ExchangeRateDto;
import main.exception.ServiceException;
import main.util.DataValidator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

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

        if (dataValidator.isNullOrBlank(baseCurrency, targetCurrency)) {
            throw new ServiceException("Base or target currency is null or blank", ServiceException.ErrorCode.VALIDATION_ERROR);
        }
        if(!dataValidator.isAmountValid(amountString)){
            throw new ServiceException("Amount is not valid or missing ", ServiceException.ErrorCode.VALIDATION_ERROR);
        }

        BigDecimal amount = exchangeRateService.getRateFromString(amountString);

        Optional<ExchangeRateDto> exchangeRateByCode = exchangeRateService.getExchangeRateByCode(baseCurrency, targetCurrency);
        Optional<ExchangeRateDto> reverseExchangeRate = exchangeRateService.getExchangeRateByCode(targetCurrency, baseCurrency);
        Optional<ExchangeRateDto> baseCurrencyToUsd = exchangeRateService.getExchangeRateByCode(baseCurrency, "USD");
        Optional<ExchangeRateDto> usdToTarget = exchangeRateService.getExchangeRateByCode("USD", targetCurrency);
        Optional<ExchangeRateDto> usdToBase = exchangeRateService.getExchangeRateByCode("USD", baseCurrency);

        if (exchangeRateByCode.isPresent()) {
            ExchangeRateDto exchangeRateDto = exchangeRateByCode.get();

            return exchangeRateService.getExchangeDto(amount, exchangeRateDto);
        } else if (reverseExchangeRate.isPresent()) {

            ExchangeRateDto reverseExchangeRateDto = reverseExchangeRate.get();
            ExchangeRateDto requestedExchangeRate = exchangeRateService.createReverseExchangeRate(reverseExchangeRateDto);
            return exchangeRateService.getExchangeDto(amount, requestedExchangeRate);

        } else if (baseCurrencyToUsd.isPresent() && usdToTarget.isPresent()) {
            ExchangeRateDto exchangeRateBaseToUsd = baseCurrencyToUsd.get();
            ExchangeRateDto exchangeRateUsdToTargetCurrency = usdToTarget.get();

            return getExchangeViaUsdDirect(exchangeRateBaseToUsd, exchangeRateUsdToTargetCurrency, amount);

        } else if(usdToTarget.isPresent() && usdToBase.isPresent()){
            ExchangeRateDto exchangeRateUsdToBase = usdToBase.get();
            ExchangeRateDto exchangeRateUsdToTarget = usdToTarget.get();


            return getExchangeViaUsdReverse(exchangeRateUsdToBase, exchangeRateUsdToTarget, amount);


        } else {
            throw new ServiceException("Can't find exchange rate", ServiceException.ErrorCode.NOT_FOUND);
        }
    }

    private static ExchangeDto getExchangeViaUsdDirect(ExchangeRateDto exchangeRateFromBaseToUsd, ExchangeRateDto exchangeRateUsdToTarget, BigDecimal amount) {

        BigDecimal baseToUsdRate = exchangeRateFromBaseToUsd.getRate();

        BigDecimal usdToTargetRate = exchangeRateUsdToTarget.getRate();

        BigDecimal calculatedAmount = (baseToUsdRate.multiply(usdToTargetRate)).multiply(amount);

        return new ExchangeDto(exchangeRateFromBaseToUsd.getBaseCurrency(), exchangeRateUsdToTarget.getTargetCurrency(), amount, calculatedAmount);
    }

    private static ExchangeDto getExchangeViaUsdReverse(ExchangeRateDto usdToBase, ExchangeRateDto usdToTarget, BigDecimal amount) {

        BigDecimal usdToBaseRate = usdToBase.getRate();
        BigDecimal usdToTargetRate = usdToTarget.getRate();

        BigDecimal rate = (BigDecimal.ONE.divide(usdToBaseRate, 8, RoundingMode.HALF_UP)).multiply(usdToTargetRate);
        BigDecimal convertedAmount = rate.multiply(amount);
        return new ExchangeDto(usdToBase.getTargetCurrency(), usdToTarget.getTargetCurrency(), amount, convertedAmount);


    }
}
