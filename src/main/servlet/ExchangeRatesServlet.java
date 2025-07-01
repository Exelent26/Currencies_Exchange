package main.servlet;

import com.google.gson.Gson;

import main.dto.ExchangeRateDto;
import main.entity.ExchangeRate;
import main.exception.ServiceException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import main.service.ExchangeRateService;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Optional;


@WebServlet("/exchangeRates")
public class ExchangeRatesServlet extends HttpServlet {
    private final ExchangeRateService exchangeRateService = ExchangeRateService.getInstance();
    Gson gson = new Gson();
// отрефачил
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            var exchangeRates = exchangeRateService.getExchangeRates();


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
//Отрефачил
        String baseCurrencyCode = request.getParameter("baseCurrency");
        String targetCurrencyCode = request.getParameter("targetCurrency");
        String rateString = request.getParameter("rate");

        try {
            Optional<ExchangeRateDto> exchangeRateByCode = exchangeRateService.getExchangeRateByCode(baseCurrencyCode, targetCurrencyCode);

            if (exchangeRateByCode.isPresent()) {
                response.setStatus(HttpServletResponse.SC_CONFLICT);
                return;
            }

            ExchangeRate exchangeRate = exchangeRateService.createNewExchangeRate(baseCurrencyCode, targetCurrencyCode, rateString);
            response.setStatus(HttpServletResponse.SC_CREATED);


            try (PrintWriter writer = response.getWriter()) {
                writer.write(gson.toJson(exchangeRate));
            }
        } catch (ServiceException e) {
            response.setStatus(e.getHttpStatusCode());
        }
    }
}
    
