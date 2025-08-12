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
    private static final DataValidator validator = DataValidator.getInstance();
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

        return validator.validateCurrencyData(code.toUpperCase(), fullName, sign);
    }

    public Currency saveCurrency(Currency currency) {
        try {
            return currencyDao.save(currency);
        } catch (DaoException e) {
            throw new ServiceException("Can't save currency", e, ServiceException.ErrorCode.DAO_ERROR);
        }
    }

    public Currency createCurrency(String code, String name, String sign) {


        if (validator.isNullOrBlank(name, code, sign)) {
            throw new ServiceException("Can't save currency", ServiceException.ErrorCode.VALIDATION_ERROR);
        }

        if (this.getCurrencyByCode(code).isPresent()) {
            throw new ServiceException("Currency already exists", ServiceException.ErrorCode.DUPLICATE_ENTITY);
        }

        Currency currency = this.buildCurrency(code, name, sign);
        return this.saveCurrency(currency);
    }

    public Optional<CurrencyDto> findCurrencyByRequestPath(String path) {

        if (validator.isCurrencyPathInvalid(path)) {
            throw new ServiceException("Invalid currency code in path", ServiceException.ErrorCode.NOT_FOUND);
        }

        String code = path.substring(1);

        DataValidator.validateCurrencyCode(code);

        Optional<CurrencyDto> currencyByCode = this.getCurrencyByCode(code);
        if (currencyByCode.isEmpty()) {
            throw new ServiceException("Can't find currency", ServiceException.ErrorCode.NOT_FOUND);
        }
        return currencyByCode;

    }


}

