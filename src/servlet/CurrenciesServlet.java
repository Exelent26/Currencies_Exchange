package servlet;

import com.google.gson.Gson;
import entity.Currency;
import exception.DaoException;
import exception.ServiceException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import service.CurrencyService;

import java.io.IOException;
import java.util.Map;

import static exception.ServiceException.ErrorCode.VALIDATION_ERROR;


@WebServlet("/currencies")
public class CurrenciesServlet extends HttpServlet {
    private final CurrencyService currencyService = CurrencyService.getInstance();
    Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        try (var writer = resp.getWriter()) {
            var currencies = currencyService.getCurrencies();
            resp.setContentType("application/json; charset=UTF-8");
            resp.setCharacterEncoding("UTF-8");
            if (!currencies.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_OK);
                var json = gson.toJson(currencies);
                writer.write(json);
            } else {
                resp.setStatus(HttpServletResponse.SC_NO_CONTENT);

            }
        } catch (Exception ex) {
            resp.setContentType("application/json; charset=UTF-8");
            resp.setCharacterEncoding("UTF-8");
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(gson.toJson(Map.of("error", "Something went wrong")));
        }
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String name = req.getParameter("name");
        String code = req.getParameter("code");
        String sign = req.getParameter("sign");

        if (name == null || code == null || sign == null || name.isBlank() || code.isBlank() || sign.isBlank()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            try (var writer = resp.getWriter()) {
                writer.write(gson.toJson(Map.of("error", "Missing required fields")));
                return;
            }
        }
        if (currencyService.getCurrencyByCode(code).isPresent()) {
            resp.setStatus(HttpServletResponse.SC_CONFLICT);
            try (var writer = resp.getWriter()) {
                writer.write(gson.toJson(Map.of("error", "Currency already exists")));
                return;
            }
        }
        try {

            var currency = currencyService.buildCurrency(code, name, sign);
            var savedCurrency = currencyService.saveCurrency(currency);
            resp.setContentType("application/json; charset=UTF-8");
            resp.setCharacterEncoding("UTF-8");
            resp.setStatus(HttpServletResponse.SC_CREATED);
            try (var writer = resp.getWriter()) {
                writer.write(gson.toJson(savedCurrency));
            }


        } catch (ServiceException e) {
            switch (e.getErrorCode()) {
                case VALIDATION_ERROR -> resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                case DUPLICATE_ENTITY -> resp.setStatus(HttpServletResponse.SC_CONFLICT);
                //case DAO_ERROR -> resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                case NOT_FOUND -> resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                default -> resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
            resp.setContentType("application/json; charset=UTF-8");
            resp.setCharacterEncoding("UTF-8");
            try (var writer = resp.getWriter()) {
                writer.write(gson.toJson(Map.of("error", e.getMessage())));
            }

        }
    }
}








