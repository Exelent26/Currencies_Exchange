package servlet;

import com.google.gson.Gson;
import dto.CurrencyDto;
import exception.ServiceException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.CurrencyService;

import java.io.IOException;
import java.util.Optional;

@WebServlet("/currency/*")
public class CurrencyServlet extends HttpServlet {
    private static final Logger log = LoggerFactory.getLogger(CurrencyServlet.class);

    private final CurrencyService currencyService = CurrencyService.getInstance();
    Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
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
        }catch (Exception e) {
            log.error("Непредвиденная ошибка в классе процессе получения конкретной валюты в гет запросе", e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
