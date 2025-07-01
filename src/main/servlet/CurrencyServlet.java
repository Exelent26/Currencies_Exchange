package main.servlet;

import com.google.gson.Gson;
import main.dto.CurrencyDto;
import main.exception.ServiceException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import main.service.CurrencyService;

import java.io.IOException;
import java.util.Optional;

import static java.nio.charset.StandardCharsets.*;

@WebServlet("/currency/*")
public class CurrencyServlet extends HttpServlet {
    private final CurrencyService currencyService = CurrencyService.getInstance();
    Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {

            String path = req.getPathInfo();

            Optional<CurrencyDto> currencyByCode = currencyService.findCurrencyByRequestPath(path);

            resp.setStatus(HttpServletResponse.SC_OK);
            var json = gson.toJson(currencyByCode);
            try (var printWriter = resp.getWriter()) {
                printWriter.write(json);
            }

        } catch (ServiceException e) {
            resp.setStatus(e.getHttpStatusCode());
        }
    }
}
