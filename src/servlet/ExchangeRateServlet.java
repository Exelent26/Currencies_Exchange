package servlet;

import com.google.gson.Gson;
import dto.ExchangeRateDto;
import entity.ExchangeRate;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import service.ExchangeRateService;
import util.DataValidator;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Optional;
import java.util.stream.Collectors;

@WebServlet("/exchangeRate/*")
public class ExchangeRateServlet extends HttpServlet {
    private final ExchangeRateService exchangeRateService = ExchangeRateService.getInstance();
    Gson gson = new Gson();
    DataValidator dataValidator = DataValidator.getInstance();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {

            String path = request.getPathInfo();
            if (dataValidator.validatePath(path)) {


                String baseCurrencyCode = path.substring(1, 4);
                String targetCurrencyCode = path.substring(4, 7);

                if (dataValidator.checkNullAndBlank(baseCurrencyCode, targetCurrencyCode)) {


                    Optional<ExchangeRateDto> exchangeRateByCode = exchangeRateService.getExchangeRateByCode(baseCurrencyCode, targetCurrencyCode);
                    if (exchangeRateByCode.isEmpty()) {
                        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    } else {
                        response.setContentType("application/json; charset=UTF-8");
                        response.setCharacterEncoding("UTF-8");
                        response.setStatus(HttpServletResponse.SC_OK);
                        var json = gson.toJson(exchangeRateByCode);
                        try (PrintWriter printWriter = response.getWriter()) {
                            printWriter.write(json);
                        }
                    }
                }
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
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

    protected void doPatch(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            String path = request.getPathInfo();



            if (dataValidator.validatePath(path)) {
                String baseCurrencyCode = path.substring(1, 4);
                String targetCurrencyCode = path.substring(4, 7);

                String body = request.getReader().lines().collect(Collectors.joining());

                HashMap<String, String> stringStringHashMap = parametersPairCreatorFromBody(body);

                String rateString = stringStringHashMap.get("rate");



                //String rateString = request.getParameter("rate");
                if (dataValidator.checkNullAndBlank(baseCurrencyCode, targetCurrencyCode, rateString)) {
                    BigDecimal rate;
                    try {
                        rate = new BigDecimal(rateString);
                    } catch (NumberFormatException e) {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        return;
                    }
                    if(rate.floatValue()>0){
                    Optional<ExchangeRateDto> exchangeRateByCode = exchangeRateService.getExchangeRateByCode(baseCurrencyCode, targetCurrencyCode);

                        if (exchangeRateByCode.isPresent()) {
                            Integer CurrencyIDForRateUpdate = exchangeRateByCode.get().getId();
                            ExchangeRate updatedExchangeRate = exchangeRateService.updateExchangeRate(CurrencyIDForRateUpdate, rate);
                            response.setStatus(HttpServletResponse.SC_OK);
                            response.setContentType("application/json; charset=UTF-8");
                            response.setCharacterEncoding("UTF-8");

                            try (PrintWriter printWriter = response.getWriter()) {
                                printWriter.write(gson.toJson(updatedExchangeRate));
                            }
                        } else {
                            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        }
                    }else{
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    }

                }else {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                }

            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

    }
    private static HashMap<String,String> parametersPairCreatorFromBody(String body) {
        HashMap<String,String> result = new HashMap<>();
        String[] pairs = body.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            String key = idx > 0 ? pair.substring(0, idx) : pair;
            String value = idx > 0 ? pair.substring(idx + 1) : "";
            result.put(key, value);
        }
        return result;
    }
}
