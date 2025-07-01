package main;

import main.dao.CurrencyDao;

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

        instance.findAll().forEach(System.out::println);
       /* instance.findById(2);
        System.out.println(instance.findById(2));
        System.out.println(instance.findByCode("RUB"));*/
        //ExchangeRateDao main.dao = ExchangeRateDao.getInstance();
        //CurrenciesPair pair = new CurrenciesPair("USD", "EUR");
        //System.out.println(main.dao.findRateByCode(pair));
        //main.dao.findAll().forEach(System.out::println);

        //var usdrub = main.dao.save(new CurrenciesPair("USD","RUB"),new BigDecimal("0.65"));
            //main.dao.updateExchangeRate(1, BigDecimal.valueOf(0.02));

    }
    }

