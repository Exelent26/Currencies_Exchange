package service;

import dao.ExchangeRateDao;
import dto.CurrencyDto;
import dto.ExchangeDto;
import dto.ExchangeRateDto;
import entity.Currency;
import entity.ExchangeRate;
import exception.DaoException;
import exception.ServiceException;
import jakarta.servlet.http.HttpServletResponse;
import mapper.CurrencyMapper;
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

    public ExchangeRate createNewExchangeRate(String baseCurrencyCode, String targetCurrencyCode, String rateString) {

        if (baseCurrencyCode.equals(targetCurrencyCode)) {
            throw new ServiceException("Base and target currency - same", ServiceException.ErrorCode.DUPLICATE_ENTITY);
        }

        DataValidator dataValidator = DataValidator.getInstance();

        if (dataValidator.isNullOrBlank(baseCurrencyCode, targetCurrencyCode, rateString)) {
            throw new ServiceException("One or few parameters are missing", ServiceException.ErrorCode.VALIDATION_ERROR);
        }

        BigDecimal rate = this.getRateFromString(rateString);
        if(!dataValidator.isRatePositive(rate)) {
            throw new ServiceException("Rate is negative", ServiceException.ErrorCode.VALIDATION_ERROR);
        }


        CurrencyService currencyService = CurrencyService.getInstance();
        Optional<CurrencyDto> baseCurrencyDTO = currencyService.getCurrencyByCode(baseCurrencyCode);
        Optional<CurrencyDto> targetCurrencyDTO = currencyService.getCurrencyByCode(targetCurrencyCode);

        if (baseCurrencyDTO.isEmpty()) {
            throw new ServiceException("Base currency not found", ServiceException.ErrorCode.NOT_FOUND);
        }

        if (targetCurrencyDTO.isEmpty()) {
            throw new ServiceException("Base currency not found", ServiceException.ErrorCode.NOT_FOUND);
        }
        Currency baseCurrency = CurrencyMapper.INSTANCE.toEntity(baseCurrencyDTO.get());

        Currency targetCurrency = CurrencyMapper.INSTANCE.toEntity(targetCurrencyDTO.get());
        Optional<ExchangeRateDto> exchangeRateByCode = this
                .getExchangeRateByCode(baseCurrencyCode, targetCurrencyCode);

        if (exchangeRateByCode.isPresent()) {
            throw new ServiceException("This exchange rate already exists", ServiceException.ErrorCode.DUPLICATE_ENTITY);
        }
        ExchangeRate newExchangeRate= this.buildExchangeRate(baseCurrency, targetCurrency, rate);

        return this.save(newExchangeRate);

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
        BigDecimal reverseRate = BigDecimal.ONE.divide(rate, 2, RoundingMode.HALF_UP);
        return new ExchangeRateDto(exchangeRate.getId(), exchangeRate.getTargetCurrency(), exchangeRate.getBaseCurrency(), reverseRate);
    }

    public BigDecimal getRateFromString(String rateString) {
        try {
            return new BigDecimal(rateString);
        } catch (NumberFormatException e) {
            throw new ServiceException("Bad rateString", ServiceException.ErrorCode.VALIDATION_ERROR);
        }
    }


    protected ExchangeDto getExchangeDto(BigDecimal amount, ExchangeRateDto neededExchangeRate) {

        BigDecimal rate = neededExchangeRate.getRate();
        BigDecimal convertedAmount = rate.multiply(amount);
        try {

            return new ExchangeDto(neededExchangeRate.getBaseCurrency(),
                    neededExchangeRate.getTargetCurrency(),neededExchangeRate.getRate(), amount, convertedAmount);
        }catch (ServiceException e) {
            throw new ServiceException("Can't convert rate", e, ServiceException.ErrorCode.VALIDATION_ERROR);
        }
    }


}