package servlet;

import com.google.gson.Gson;

import dto.CurrencyDto;
import entity.Currency;
import entity.ExchangeRate;
import exception.ServiceException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import service.CurrencyService;
import service.ExchangeRateService;
import util.DataValidator;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.Optional;


@WebServlet("/exchangeRates")
public class ExchangeRatesServlet extends HttpServlet {
    private final ExchangeRateService exchangeRateService = ExchangeRateService.getInstance();
    Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            var exchangeRates = exchangeRateService.getExchangeRates();
            response.setContentType("application/json; charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            response.setStatus(HttpServletResponse.SC_OK);
            if (exchangeRates.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            } else {
                response.setStatus(HttpServletResponse.SC_OK);
                var json = gson.toJson(exchangeRates);
                try (var writer = response.getWriter()) {
                    writer.write(json);
                }
            }


        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String baseCurrencyCode = request.getParameter("baseCurrency");
        String targetCurrencyCode = request.getParameter("targetCurrency");
        String rateString = request.getParameter("rate");
        // тут и в валютах надо этот метод унифицировать,
        // чтобы просто принимал стрингу и чекал ее на нули и = что она пустая бланком
        DataValidator dataValidator = DataValidator.getInstance();

        if(dataValidator.nullAndBlankCheck(baseCurrencyCode,targetCurrencyCode, rateString)){
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        if (baseCurrencyCode == null || targetCurrencyCode == null || rateString == null
                || baseCurrencyCode.isBlank() || targetCurrencyCode.isBlank() || rateString.isBlank()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // распарсим биг десимал
        BigDecimal rate;
        try {
            rate = new BigDecimal(rateString);
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        // првоеряем что валюты вообще существуют, ищем их в базе
        CurrencyService currencyService = CurrencyService.getInstance();
        Optional<CurrencyDto> baseCurrencyDTO = currencyService.getCurrencyByCode(baseCurrencyCode);
        // сделать общий метод для проверок базовой валюты и таргетной

        if (baseCurrencyDTO.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        Optional<CurrencyDto> targetCurrencyDTO = currencyService.getCurrencyByCode(targetCurrencyCode);
        if (targetCurrencyDTO.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        Currency baseCurrency = baseCurrencyDTO.map(baseDTO -> new Currency(baseDTO.getId(),
                baseDTO.getCode(),
                baseDTO.getFullName(),
                baseDTO.getSign())).get();
        Currency targetCurrency = targetCurrencyDTO.map(targetDTO ->
                new Currency(targetDTO.getId(),
                        targetDTO.getCode(),
                        targetDTO.getFullName(),
                        targetDTO.getSign())).get();


        // проверили что валюты существуют, теперь нужно убедиться что такого обменного курса еще нет
        // а если такого нет, то мы собираем сначала обменный курс из айдишек валют и рейта и сохраняем их в базу
        try {
            var exchangeRateByCode = exchangeRateService.getExchangeRateByCode(baseCurrencyCode, targetCurrencyCode);

            if (exchangeRateByCode.isPresent()) {
                response.setStatus(HttpServletResponse.SC_CONFLICT);
                return;
            }
            ExchangeRate exchangeRate = exchangeRateService.buildExchangeRate(baseCurrency, targetCurrency, rate);

            ExchangeRate savedExchangeRate = exchangeRateService.save(exchangeRate);
            response.setContentType("application/json; charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            response.setStatus(HttpServletResponse.SC_CREATED);
            try (PrintWriter writer = response.getWriter()) {
                writer.write(gson.toJson(savedExchangeRate));
            }
        } catch (ServiceException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }


    }


}
    
