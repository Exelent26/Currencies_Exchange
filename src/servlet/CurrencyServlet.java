package servlet;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import service.CurrencyService;

import java.io.IOException;

@WebServlet("/currency/*")
public class CurrencyServlet extends HttpServlet {
    private final CurrencyService currencyService = CurrencyService.getInstance();
    Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try (var printWriter = resp.getWriter()) {

            String pathInfo = req.getPathInfo();

            if (pathInfo == null || pathInfo.length() != 4) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            String code = pathInfo.substring(1);

            if (!code.matches("[A-Z]{3}")) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            var currencyByCode = currencyService.getCurrencyByCode(code);
            if (currencyByCode.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            } else {
                resp.setContentType("application/json; charset=UTF-8");
                resp.setCharacterEncoding("UTF-8");
                resp.setStatus(HttpServletResponse.SC_OK);
                var json = gson.toJson(currencyByCode);
                printWriter.write(json);

            }

        }catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
