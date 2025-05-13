package dao;

import dto.CurrenciesPair;
import entity.Currency;
import entity.ExchangeRate;
import exception.DaoException;
import util.ConnectionManager;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static exception.DaoException.ErrorCode.DATABASE_ERROR;

public class ExchangeRateDao implements CrudDao<ExchangeRate> {

    private static final ExchangeRateDao INSTANCE = new ExchangeRateDao();

    private ExchangeRateDao() {
    }

    private static final String GET_EXCHANGE_RATES = """
            SELECT er.id,
                   er.Rate,
                   bc.id bc_id, bc.code bc_code, bc.full_name bc_fullname, bc.sign bc_sign,
                   tc.id tc_id, tc.code tc_code, tc.full_name tc_fullname, tc.sign tc_sign
            FROM ExchangeRates er
            left join main.Currencies bc on bc.id = er.BaseCurrencyId
            left join main.Currencies tc on tc.id = er.TargetCurrencyId
            """;

    private static final String GET_EXCHANGE_RATE_BY_CODE = """
            SELECT er.id,
                   er.Rate,
                   bc.id bc_id, bc.code bc_code, bc.full_name bc_fullname, bc.sign bc_sign,
                   tc.id tc_id, tc.code tc_code, tc.full_name tc_fullname, tc.sign tc_sign
            FROM ExchangeRates er
            left join main.Currencies bc on bc.id = er.BaseCurrencyId
            left join main.Currencies tc on tc.id = er.TargetCurrencyId
            where bc.code = ? and tc.code = ?
            """;
    private static final String SAVE_EXCHANGE_RATE = """
            INSERT INTO ExchangeRates(BaseCurrencyId, TargetCurrencyId, Rate)
            VALUES (?,?,?)
            """;

    private static final String UPDATE_EXCHANGE_RATE = """
            UPDATE ExchangeRates
            SET Rate = ?
            WHERE id = ?
            """;

    public static ExchangeRateDao getInstance() {
        return INSTANCE;
    }

    @Override
    public Optional<ExchangeRate> findById(int id) {
        return Optional.empty();
    }

    @Override
    public List<ExchangeRate> findAll() {
        try (var connection = ConnectionManager.get();
             var prepareStatement = connection.prepareStatement(GET_EXCHANGE_RATES)) {
            var resultSet = prepareStatement.executeQuery();
            List<ExchangeRate> exchangeRates = new ArrayList<>();
            while (resultSet.next()) {
                exchangeRates.add(buildCurrencyPairRate(resultSet));
            }
            return exchangeRates;

        } catch (SQLException e) {
            throw new DaoException("Cannot get exchange rates from database", e);
        }
    }

    public Optional<ExchangeRate> findRateByCode(CurrenciesPair currenciesPair) {
        try (Connection connection = ConnectionManager.get();
             var prepareStatement = connection.prepareStatement(GET_EXCHANGE_RATE_BY_CODE)) {
            prepareStatement.setString(1, currenciesPair.baseCode());
            prepareStatement.setString(2, currenciesPair.targetCode());
            var resultSet = prepareStatement.executeQuery();
            return resultSet.next() ? Optional.of(buildCurrencyPairRate(resultSet)) : Optional.empty();
        } catch (SQLException e) {
            throw new DaoException("Cannot get currencies pair rate from database", DaoException.ErrorCode.DATABASE_ERROR);
        }
    }
    public ExchangeRate updateExchangeRate(int exchangeRateId, BigDecimal newRate) {
        try (var connection = ConnectionManager.get();
        var prepareStatement = connection.prepareStatement(UPDATE_EXCHANGE_RATE)) {
            prepareStatement.setBigDecimal(1, newRate);
            prepareStatement.setInt(2, exchangeRateId);
            prepareStatement.executeUpdate();

            int updatedRows = prepareStatement.executeUpdate();
            if (updatedRows == 0) {
                throw new DaoException("Exchange rate not found", DaoException.ErrorCode.EXCHANGE_RATE_NOT_FOUND);
            }

            return findById(exchangeRateId)
                    .orElseThrow(() -> new DaoException("Exchange rate not found after update", DaoException.ErrorCode.EXCHANGE_RATE_NOT_FOUND));

        } catch (SQLException e) {
            throw new DaoException("Failed to update rate", DaoException.ErrorCode.DATABASE_ERROR, e);
        }
    }

    public ExchangeRate save(CurrenciesPair currenciesPair, BigDecimal rate) {
        if (currenciesPair == null || rate == null) {
            throw new DaoException("Invalid input: currenciesPair or rate is null", DaoException.ErrorCode.INVALID_INPUT);
        }
        if (this.findRateByCode(currenciesPair).isEmpty()) {
            try (Connection connection = ConnectionManager.get();
                 var prepareStatement = connection.prepareStatement(SAVE_EXCHANGE_RATE, Statement.RETURN_GENERATED_KEYS)) {

                try {
                    connection.setAutoCommit(false);
                    CurrencyDao currencyDao = CurrencyDao.getInstance();
                    Currency base = currencyDao.findByCode(currenciesPair.baseCode()).orElseThrow(() -> new DaoException(
                            "Base currency not found: " + currenciesPair.baseCode(),
                            DaoException.ErrorCode.CURRENCY_NOT_FOUND
                    ));
                    Currency target = currencyDao.findByCode(currenciesPair.targetCode()).orElseThrow(() -> new DaoException(
                            "Target currency not found: " + currenciesPair.targetCode(),
                            DaoException.ErrorCode.CURRENCY_NOT_FOUND
                    ));

                    prepareStatement.setInt(1, base.getId());
                    prepareStatement.setInt(2, target.getId());
                    prepareStatement.setBigDecimal(3, rate);
                    prepareStatement.executeUpdate();

                    try (var generatedKeys = prepareStatement.getGeneratedKeys();) {
                        if (generatedKeys.next()) {
                            int id = generatedKeys.getInt(1);
                            connection.commit();
                            return new ExchangeRate(id, base, target, rate);

                        } else {
                            throw new DaoException("No ID returned after insert");
                        }
                    }
                } catch (SQLException e) {
                    connection.rollback();
                    throw new DaoException("Transaction failed", DaoException.ErrorCode.DATABASE_ERROR, e);
                }
            } catch (SQLException e) {
                if (e.getErrorCode() == 19) {
                    throw new DaoException("Currencies pair rate already exists", DaoException.ErrorCode.DUPLICATE_EXCHANGE_RATE);
                } else {
                    throw new DaoException("Database error", DaoException.ErrorCode.DATABASE_ERROR);
                }
            }

        } else {
            throw new DaoException("Currencies pair rate already exists",
                    DaoException.ErrorCode.DUPLICATE_EXCHANGE_RATE);
        }

    }

    private static ExchangeRate buildCurrencyPairRate(ResultSet resultSet) throws SQLException {
        Currency base = new Currency(resultSet.getInt("bc_id"),
                resultSet.getString("bc_code"),
                resultSet.getString("bc_fullname"),
                resultSet.getString("bc_sign"));
        Currency target = new Currency(resultSet.getInt("tc_id"),
                resultSet.getString("tc_code"),
                resultSet.getString("tc_fullname"),
                resultSet.getString("tc_sign"));

        return new ExchangeRate(resultSet.getInt("id"),
                base,
                target,
                resultSet.getBigDecimal("rate")
        );

    }

    @Override
    public ExchangeRate save(ExchangeRate entity) {
        if (entity == null || entity.getBaseCurrency() == null || entity.getTargetCurrency() == null) {
            throw new DaoException("Invalid ExchangeRate entity", DaoException.ErrorCode.INVALID_INPUT);
        }
        return save(
                new CurrenciesPair(entity.getBaseCurrency().getCode(), entity.getTargetCurrency().getCode()),
                entity.getRate()
        );
    }

}
