package main.dao;

import main.entity.Currency;
import main.entity.ExchangeRate;
import main.exception.DaoException;
import main.util.ConnectionManager;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    private static final String GET_EXCHANGE_RATE_BY_ID = """
            SELECT er.id,
                   er.Rate,
                   bc.id bc_id, bc.code bc_code, bc.full_name bc_fullname, bc.sign bc_sign,
                   tc.id tc_id, tc.code tc_code, tc.full_name tc_fullname, tc.sign tc_sign
            FROM ExchangeRates er
            left join main.Currencies bc on bc.id = er.BaseCurrencyId
            left join main.Currencies tc on tc.id = er.TargetCurrencyId
            where er.id = ?
            """;

    public static ExchangeRateDao getInstance() {
        return INSTANCE;
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

    public Optional<ExchangeRate> findRateByCode(String baseCode, String targetCode) {
        try (Connection connection = ConnectionManager.get();
             var prepareStatement = connection.prepareStatement(GET_EXCHANGE_RATE_BY_CODE)) {
            try {
                prepareStatement.setString(1, baseCode);
            }catch (SQLException e) {
                throw new DaoException("Cannot get exchange rate from database, base code incorrect", e);
            }
            try {
                prepareStatement.setString(2, targetCode);
            }catch (SQLException e) {
                throw new DaoException("Cannot get exchange rate from database, target code incorrect", e);
            }
            var resultSet = prepareStatement.executeQuery();
            return resultSet.next() ? Optional.of(buildCurrencyPairRate(resultSet)) : Optional.empty();
        } catch (SQLException e) {
            throw new DaoException("Cannot get currencies pair rate from database", DaoException.ErrorCode.DATABASE_ERROR);
        }
    }
    @Override
    public Optional<ExchangeRate> findById(int exchangeRateId) {
        try(Connection connection = ConnectionManager.get();
            PreparedStatement preparedStatement = connection.prepareStatement(GET_EXCHANGE_RATE_BY_ID)){
            preparedStatement.setInt(1, exchangeRateId);
            var resultSet = preparedStatement.executeQuery();
            return resultSet.next() ? Optional.of(buildCurrencyPairRate(resultSet)) : Optional.empty();
        }catch (SQLException e) {
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


    public ExchangeRate save(String baseCode, String targetCode, BigDecimal rate) {
        if (baseCode == null || rate == null|| targetCode == null) {
            throw new DaoException("Invalid input: baseCode or targetCode  or rate is null", DaoException.ErrorCode.INVALID_INPUT);
        }
        if (this.findRateByCode(baseCode, targetCode).isEmpty()) {
            try (Connection connection = ConnectionManager.get();
                 var prepareStatement = connection.prepareStatement(SAVE_EXCHANGE_RATE, Statement.RETURN_GENERATED_KEYS)) {

                try {
                    connection.setAutoCommit(false);
                    CurrencyDao currencyDao = CurrencyDao.getInstance();
                    Currency base = currencyDao.findByCode(baseCode).orElseThrow(() -> new DaoException(
                            "Base currency not found: " + baseCode,
                            DaoException.ErrorCode.CURRENCY_NOT_FOUND
                    ));
                    Currency target = currencyDao.findByCode(targetCode).orElseThrow(() -> new DaoException(
                            "Target currency not found: " + targetCode,
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
            throw new DaoException("Invalid ExchangeRate main.entity", DaoException.ErrorCode.INVALID_INPUT);
        }
        return save(
                entity.getBaseCurrency().getCode(), entity.getTargetCurrency().getCode(),
                entity.getRate()
        );
    }

}
