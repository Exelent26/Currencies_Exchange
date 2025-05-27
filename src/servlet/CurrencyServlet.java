package servlet;

import com.google.gson.Gson;
import dto.CurrencyDto;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import service.CurrencyService;

import java.io.IOException;
import java.util.Map;


@WebServlet("/currencies")
public class CurrencyServlet extends HttpServlet {
    private final CurrencyService currencyService = CurrencyService.getInstance();
    Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        try (var writer = resp.getWriter()) {
            var currencies = currencyService.getCurrencies();
            if (!currencies.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_OK);
                var json = gson.toJson(currencies);
                writer.write(json);
            } else {
                resp.setStatus(HttpServletResponse.SC_NO_CONTENT);

                resp.setContentType("application/json");
                resp.setCharacterEncoding("UTF-8");


            }
        } catch (Exception ex) {
            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(gson.toJson(Map.of("error", "Something went wrong")));
        }
    }
}

       /* try (var printWriter = resp.getWriter()) {
            printWriter.println("<h1>Список валют:</h1>");
            printWriter.println("<ul>");
            currencyService.getCurrencies().forEach(currencyDto -> {
                printWriter.write("""
                        <li> "id:" %d,
                        "name": %s,
                        "code": %s,
                        "sign": %s
                        </li>""".formatted(currencyDto.getId(),
                        currencyDto.getFullName(),
                        currencyDto.getCode(),
                        currencyDto.getSign()));
            });
            printWriter.println("</ul>");*/





