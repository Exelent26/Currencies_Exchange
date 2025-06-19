package servlet;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import service.ExchangeRateService;
import util.DataValidator;

import java.io.IOException;
import java.io.PrintWriter;

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

                if (dataValidator.nullAndBlankCheck(baseCurrencyCode, targetCurrencyCode)) {


                    var exchangeRateByCode = exchangeRateService.getExchangeRateByCode(baseCurrencyCode, targetCurrencyCode);
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

}
