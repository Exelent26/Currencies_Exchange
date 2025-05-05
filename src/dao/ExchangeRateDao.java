package dao;

import dto.CurrenciesPair;
import entity.Currency;
import entity.ExchangeRate;
import exception.DaoException;
import util.ConnectionManager;

import java.sql.ResultSet;
import java.sql.SQLException;
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
    private static Optional<ExchangeRate> findCurrencyPairRateByCode(CurrenciesPair currenciesPair) {

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
        return null;
    }

}
