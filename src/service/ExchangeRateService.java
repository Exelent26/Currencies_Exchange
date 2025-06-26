package service;

import dao.ExchangeRateDao;
import dto.ExchangeDto;
import dto.ExchangeRateDto;
import entity.Currency;
import entity.ExchangeRate;
import exception.DaoException;
import exception.ServiceException;
import util.DataValidator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ExchangeRateService {
    private static final ExchangeRateService INSTANCE = new ExchangeRateService();

    private final ExchangeRateDao exchangeRateDao = ExchangeRateDao.getInstance();

    private ExchangeRateService() {
    }

    public static ExchangeRateService getInstance() {
        return INSTANCE;
    }

    public List<ExchangeRateDto> getExchangeRates() {
        return exchangeRateDao.findAll().stream()
                .map(exchangeRate -> new ExchangeRateDto(exchangeRate.getId(), exchangeRate.getBaseCurrency(), exchangeRate.getTargetCurrency(), exchangeRate.getRate()))
                .collect(Collectors.toList());
    }

    public Optional<ExchangeRateDto> getExchangeRateByCode(String baseCurrencyCode, String targetCurrencyCode) {
        return exchangeRateDao.findRateByCode(baseCurrencyCode, targetCurrencyCode).stream().findFirst()
                .map(gottenRate -> new ExchangeRateDto(
                        gottenRate.getId(),
                        gottenRate.getBaseCurrency(),
                        gottenRate.getTargetCurrency(),
                        gottenRate.getRate()));
    }

    public ExchangeRate save(ExchangeRate exchangeRate) {
        try {
            return exchangeRateDao.save(exchangeRate);
        } catch (DaoException e) {
            throw new ServiceException("Can't save exchangeRate", e, ServiceException.ErrorCode.DAO_ERROR);
        }
    }

    public ExchangeRate buildExchangeRate(Currency baseCurrency, Currency targetCurrency, BigDecimal rate) {
        return new ExchangeRate(baseCurrency, targetCurrency, rate);
    }

    public ExchangeRate updateExchangeRate(Integer exchangeRateId, BigDecimal rate) {
        try {
            return exchangeRateDao.updateExchangeRate(exchangeRateId, rate);
        } catch (DaoException e) {
            throw new ServiceException("Can't update exchangeRate", e, ServiceException.ErrorCode.DAO_ERROR);
        }
    }

    public ExchangeRateDto createReverseExchangeRate(ExchangeRateDto exchangeRate) {
        BigDecimal rate = exchangeRate.getRate();
        BigDecimal reverseRate = BigDecimal.ONE.divide(rate, 8, RoundingMode.HALF_UP);
        return new ExchangeRateDto(exchangeRate.getId(), exchangeRate.getTargetCurrency(), exchangeRate.getBaseCurrency(), reverseRate);
    }

    public ExchangeDto calculateExchange(String from, String to, String amountString) {

        DataValidator dataValidator = new DataValidator();
        if (dataValidator.checkNullAndBlank(from, to, amountString)) {

            Optional<ExchangeRateDto> exchangeRateByCode = this.getExchangeRateByCode(from, to);

            Optional<ExchangeRateDto> reverseExchangeRate = this.getExchangeRateByCode(to, from);

            Optional<ExchangeRateDto> exchangeRateFromToUsd = this.getExchangeRateByCode(from, "USD");
            Optional<ExchangeRateDto> exchangeRateUsdTo = this.getExchangeRateByCode("USD", to);


            if (exchangeRateByCode.isPresent()) {
                ExchangeRateDto exchangeRateDto = exchangeRateByCode.get();

                return getExchangeDto(amountString, exchangeRateDto);
            } else if (reverseExchangeRate.isPresent()) {
                ExchangeRateDto reverseExchangeRateDto = reverseExchangeRate.get();
                ExchangeRateDto neededExchangeRate = this.createReverseExchangeRate(reverseExchangeRateDto);
                return getExchangeDto(amountString, neededExchangeRate);

            }/*else if(exchangeRateFromToUsd.isPresent() && exchangeRateUsdTo.isPresent()) {

                ExchangeRateDto exchangeRateToUsd = exchangeRateFromToUsd.get();

                BigDecimal tempUsdIdintification = a

                ExchangeRateDto exchangeRateFromUsd = exchangeRateUsdTo.get();
            }*/ else {
                throw new ServiceException("Can't find exchange rate", ServiceException.ErrorCode.NOT_FOUND);
            }
        } else {
            throw new ServiceException("Can't calculate exchange", ServiceException.ErrorCode.NOT_FOUND);
        }
    }


    private ExchangeDto getExchangeDto(String amountString, ExchangeRateDto neededExchangeRate) {
        BigDecimal rate = neededExchangeRate.getRate();

        BigDecimal amount = null;
        try {
            amount = new BigDecimal(amountString);
        } catch (NumberFormatException e) {
            throw new ServiceException("Can't parse amount string", ServiceException.ErrorCode.NOT_FOUND);
        }
        BigDecimal convertedAmount = rate.multiply(amount);
        return new ExchangeDto(neededExchangeRate, amount, convertedAmount);
    }

}