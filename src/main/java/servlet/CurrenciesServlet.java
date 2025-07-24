package servlet;

import com.google.gson.Gson;
import dto.CurrencyDto;
import entity.Currency;
import exception.ServiceException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import service.CurrencyService;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;


@WebServlet("/currencies")
public class CurrenciesServlet extends HttpServlet {
    private final CurrencyService currencyService = CurrencyService.getInstance();
    Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {


        try {
            List<CurrencyDto> currencies = currencyService.getCurrencies();
            if (!currencies.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_OK);
                String json = gson.toJson(currencies);
                try (PrintWriter writer = resp.getWriter()) {
                    writer.write(json);
                }
            } else {
                resp.setStatus(HttpServletResponse.SC_NO_CONTENT);

            }
        } catch (Exception ex) {

            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(gson.toJson(Map.of("error", "Something went wrong")));
        }
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String name = req.getParameter("name");
        String code = req.getParameter("code");
        String sign = req.getParameter("sign");


        try {

            Currency newCurrency = currencyService.createCurrency(code, name, sign);
            resp.setStatus(HttpServletResponse.SC_CREATED);
            try (PrintWriter writer = resp.getWriter()) {
                writer.write(gson.toJson(newCurrency));
            }


        } catch (ServiceException e) {
            resp.setStatus(e.getHttpStatusCode());
        }

    }
}









