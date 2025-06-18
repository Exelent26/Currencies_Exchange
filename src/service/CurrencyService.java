package service;

import dao.CurrencyDao;
import dto.CurrencyDto;
import entity.Currency;
import exception.DaoException;
import exception.ServiceException;
import util.DataValidator;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class CurrencyService {
    private static final CurrencyService INSTANCE = new CurrencyService();

    private final CurrencyDao currencyDao = CurrencyDao.getInstance();

    private CurrencyService() {
    }

    public static CurrencyService getInstance() {
        return INSTANCE;
    }

    public List<CurrencyDto> getCurrencies() {
        return currencyDao.findAll().stream().
                map(currency -> new CurrencyDto(currency.getId(), currency.getCode(),
                        currency.getFullName(), currency.getSign())).collect(Collectors.toList());
    }

    public Optional<CurrencyDto> getCurrencyByCode(String code) {
        return currencyDao.findByCode(code).stream().findFirst()
                .map(currency -> new CurrencyDto(currency.getId(), currency.getCode(),
                        currency.getFullName(), currency.getSign()));
    }

    public Currency buildCurrency(String code, String fullName, String sign) {

        DataValidator validator = DataValidator.getInstance();

        return validator.validateCurrencyData(code.toUpperCase(), fullName, sign);
    }

    public Currency saveCurrency(Currency currency) {
        try {
            return currencyDao.save(currency);
        } catch (DaoException e) {
            throw new ServiceException("Can't save currency", e, ServiceException.ErrorCode.DAO_ERROR);
        }
    }
}

