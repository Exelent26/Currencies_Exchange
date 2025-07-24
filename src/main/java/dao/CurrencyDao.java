package dao;

import entity.Currency;
import exception.DaoException;
import util.ConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CurrencyDao implements CrudDao<Currency> {
    private static final CurrencyDao INSTANCE = new CurrencyDao();

    private static final String SAVE_CURRENCY = """
            INSERT INTO Currencies (code, full_name, sign)
            VALUES (?, ?, ?)
            """;
    private static final String GET_ALL_CURRENCIES = """
            SELECT Currencies.id, Currencies.code, Currencies.full_name, Currencies.sign
            FROM Currencies
            """;
    private static final String GET_CURRENCY_BY_ID = """
            SELECT Currencies.id, Currencies.code, Currencies.full_name, Currencies.sign
            FROM Currencies
            WHERE id = ?
            """;
    private static final String GET_CURRENCY_BY_CODE = """
            SELECT Currencies.id, Currencies.code, Currencies.full_name, Currencies.sign
            FROM Currencies
            WHERE code = ?
            """;

    private CurrencyDao() {
    }

    public static CurrencyDao getInstance() {
        return INSTANCE;
    }

    @Override
    public Currency save(Currency currency) {
        try (var connection = ConnectionManager.get();
             var prepareStatement = connection.prepareStatement(SAVE_CURRENCY, Statement.RETURN_GENERATED_KEYS)
        ) {
            prepareStatement.setString(1, currency.getCode());
            prepareStatement.setString(2, currency.getFullName());
            prepareStatement.setString(3, currency.getSign());
            prepareStatement.executeUpdate();
            int id;
            var generatedKeys = prepareStatement.getGeneratedKeys();
            if (generatedKeys.next()) {
                id = generatedKeys.getInt(1);
            } else {
                throw new DaoException("Can't get generated key. ID not created" + currency.getCode());
            }

            return new Currency(id, currency.getCode(), currency.getFullName(), currency.getSign());

        } catch (SQLException e) {
            if (e.getErrorCode() == 19) {
                throw new DaoException("Database constraint  error ", DaoException.ErrorCode.DB_CONSTRAINT_ERROR);
            } else {
                throw new DaoException("Database Error", DaoException.ErrorCode.DATABASE_ERROR);

            }

        }
    }

    @Override
    public Optional<Currency> findById(int id) {
        try (var connection = ConnectionManager.get();
             var prepareStatement = connection.prepareStatement(GET_CURRENCY_BY_ID)) {
            prepareStatement.setInt(1, id);
            var resultSet = prepareStatement.executeQuery();

            return resultSet.next()
                    ? Optional.of(buildCurrency(resultSet))
                    : Optional.empty();

        } catch (SQLException e) {
            throw new DaoException("Database error", DaoException.ErrorCode.DATABASE_ERROR);
        }
    }
    public Optional<Currency> findByCode(String code) {
        try (var connection = ConnectionManager.get();
             var prepareStatement = connection.prepareStatement(GET_CURRENCY_BY_CODE)) {
            prepareStatement.setString(1, code);
            var resultSet = prepareStatement.executeQuery();
            return resultSet.next()
                    ? Optional.of(buildCurrency(resultSet))
                    : Optional.empty();

        } catch (SQLException e) {
            throw new DaoException("Database error", DaoException.ErrorCode.DATABASE_ERROR);
        }
    }

    @Override
    public List<Currency> findAll() {
        try (var connection = ConnectionManager.get();
             var prepareStatement = connection.prepareStatement(GET_ALL_CURRENCIES)
        ) {
            var resultSet = prepareStatement.executeQuery();
            List<Currency> currencies = new ArrayList<>();
            while (resultSet.next()) {
                currencies.add(buildCurrency(resultSet));
            }
            return currencies;
        } catch (SQLException e) {
            throw new DaoException("Can't connect to database", e);
        }
    }

    public static Currency buildCurrency(ResultSet rs) throws SQLException {
        return new Currency(rs.getInt("id"),
                rs.getString("code"),
                rs.getString("full_name"),
                rs.getString("sign"));
    }
}
