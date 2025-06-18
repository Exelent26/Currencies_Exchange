package service;

import dao.ExchangeRateDao;
import dto.CurrenciesPair;
import dto.ExchangeRateDto;
import entity.ExchangeRate;

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

    public Optional<ExchangeRateDto> getExchangeRateByCode(String baseCurrency, String targetCurrency) {
        CurrenciesPair exchangeRate  = new CurrenciesPair(baseCurrency, targetCurrency);

        return exchangeRateDao.findRateByCode(exchangeRate).stream().findFirst()
                .map(gottenRate -> new ExchangeRateDto(
                        gottenRate.getId(),
                        gottenRate.getBaseCurrency(),
                        gottenRate.getTargetCurrency(),
                        gottenRate.getRate()));
    }
}
