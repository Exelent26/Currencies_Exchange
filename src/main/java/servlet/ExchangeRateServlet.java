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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import service.ExchangeRateService;
import util.DataValidator;
import util.RequestBodyParser;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Optional;
import java.util.stream.Collectors;

@WebServlet("/exchangeRate/*")
public class ExchangeRateServlet extends HttpServlet {
    private static final Logger log = LoggerFactory.getLogger(ExchangeRateServlet.class);
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
            log.error("Непредвиденная ошибка в классе курсов в гет методе обмена валют", e);
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
        String baseCurrencyCode = null;
        String targetCurrencyCode = null;
        String body = null;
        try {
            String path = request.getPathInfo();
            baseCurrencyCode = path.substring(1, 4);
            targetCurrencyCode = path.substring(4, 7);

            body = collectParameters(request);
            String rateString = RequestBodyParser.parametersPairCreatorFromBody(body).get("rate");

            ExchangeRate updated = exchangeRateService.updateExchangeRate(baseCurrencyCode, targetCurrencyCode, rateString);

            response.setStatus(HttpServletResponse.SC_OK);
            try (PrintWriter out = response.getWriter()) {
                out.write(gson.toJson(updated));
            }

        } catch (ServiceException e) {
            log.error("ServiceException PATCH base={} target={} body={} rid={}",
                    baseCurrencyCode, targetCurrencyCode, body, MDC.get("rid"), e);
            response.setStatus(e.getHttpStatusCode());
        }catch (Exception e) {
            log.error("Непредвиденная ошибка в классе курсов в патч методе обмена валют", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private static String collectParameters(HttpServletRequest request) throws IOException {
        return request.getReader().lines().collect(Collectors.joining());
    }

}
