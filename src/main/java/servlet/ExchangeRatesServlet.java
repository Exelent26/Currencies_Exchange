package servlet;

import com.google.gson.Gson;

import dto.ExchangeRateDto;
import entity.ExchangeRate;
import exception.ServiceException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.ExchangeRateService;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Optional;


@WebServlet("/exchangeRates")
public class ExchangeRatesServlet extends HttpServlet {
    private final ExchangeRateService exchangeRateService = ExchangeRateService.getInstance();
    private static final Logger log = LoggerFactory.getLogger(ExchangeRatesServlet.class);
    Gson gson = new Gson();
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)  {

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
            log.error("Непредвиденная ошибка", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws  IOException {
        String baseCurrencyCode = request.getParameter("baseCurrencyCode");
        String targetCurrencyCode = request.getParameter("targetCurrencyCode");
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
            log.error("Ошибка в процессе получения обменных курсов");
            response.setStatus(e.getHttpStatusCode());
        }catch (Exception e) {
            log.error("Непредвиденная ошибка", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
    
