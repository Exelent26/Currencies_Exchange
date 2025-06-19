package service;

import dao.ExchangeRateDao;
import dto.ExchangeRateDto;
import entity.Currency;
import entity.ExchangeRate;
import exception.DaoException;
import exception.ServiceException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ExchangeRateService {
    private static final ExchangeRateService INSTANCE = new ExchangeRateService();

    private final ExchangeRateDao exchangeRateDao = ExchangeRateDao.getInstance();
    private ExchangeRateService() {}

    public static ExchangeRateService getInstance() {return INSTANCE;}

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
        }catch (DaoException e) {
            throw new ServiceException("Can't save exchangeRate", e,ServiceException.ErrorCode.DAO_ERROR);
        }
    }

    public ExchangeRate buildExchangeRate(Currency baseCurrency, Currency targetCurrency, BigDecimal rate) {
        return new ExchangeRate(baseCurrency, targetCurrency, rate);

    }
}
