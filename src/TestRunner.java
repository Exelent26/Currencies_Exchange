import dao.CurrencyDao;
import dao.ExchangeRateDao;
import dto.CurrenciesPair;
import entity.Currency;

import java.sql.SQLException;

public class TestRunner {
    public static void main(String[] args) throws SQLException {
   /*     String SQL = "select * from Currencies";
        var connection = ConnectionManager.get();
        var prepareStatement = connection.prepareStatement(SQL);
        var resultSet = prepareStatement.executeQuery();
        while (resultSet.next()) {
            var currency = resultSet.getString("full_name");
            System.out.println(currency);*/
        var instance = CurrencyDao.getInstance();
        /*var currency = new Currency("RUB", "Russian Ruble", "₽");
        instance.save(currency);
        System.out.println(instance);*/

        //instance.findAll().forEach(System.out::println);
       /* instance.findById(2);
        System.out.println(instance.findById(2));
        System.out.println(instance.findByCode("RUB"));*/
        ExchangeRateDao dao = ExchangeRateDao.getInstance();
        dao.findAll().forEach(System.out::println);
        CurrenciesPair pair = new CurrenciesPair("USD", "EUR");
    }
    }

