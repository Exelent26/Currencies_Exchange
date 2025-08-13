package servlet;

import com.google.gson.Gson;
import dto.ExchangeRateDto;
import entity.ExchangeRate;
import exception.ServiceException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import service.ExchangeRateService;
import util.DataValidator;
import util.RequestBodyParser;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Optional;
import java.util.stream.Collectors;

@WebServlet("/exchangeRate/*")
public class ExchangeRateServlet extends HttpServlet {
    private final ExchangeRateService exchangeRateService = ExchangeRateService.getInstance();
    Gson gson = new Gson();
    DataValidator dataValidator = DataValidator.getInstance();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {

        try {

            String path = request.getPathInfo();
            if (dataValidator.isExchangeRatePathIncorrect(path)) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            String baseCurrencyCode = path.substring(1, 4);
            String targetCurrencyCode = path.substring(4, 7);

            if (dataValidator.isNullOrBlank(baseCurrencyCode, targetCurrencyCode)) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            Optional<ExchangeRateDto> exchangeRateByCode = exchangeRateService.getExchangeRateByCode(baseCurrencyCode, targetCurrencyCode);
            if (exchangeRateByCode.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            response.setStatus(HttpServletResponse.SC_OK);
            var json = gson.toJson(exchangeRateByCode);
            try (PrintWriter printWriter = response.getWriter()) {
                printWriter.write(json);
            }

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (req.getMethod().equalsIgnoreCase("PATCH")) {
            doPatch(req, resp);
        } else {
            super.service(req, resp);
        }
    }

    protected void doPatch(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String path = request.getPathInfo();
            String baseCurrencyCode = path.substring(1, 4);
            String targetCurrencyCode = path.substring(4, 7);

            String body = collectParameters(request);
            String rateString = RequestBodyParser.parametersPairCreatorFromBody(body).get("rate");

            ExchangeRate updated = exchangeRateService.updateExchangeRate(baseCurrencyCode, targetCurrencyCode, rateString);

            response.setStatus(HttpServletResponse.SC_OK);
            try (PrintWriter out = response.getWriter()) {
                out.write(gson.toJson(updated));
            }

        } catch (ServiceException e) {
            response.setStatus(e.getHttpStatusCode());
        }
    }

    private static String collectParameters(HttpServletRequest request) throws IOException {
        return request.getReader().lines().collect(Collectors.joining());
    }

}
