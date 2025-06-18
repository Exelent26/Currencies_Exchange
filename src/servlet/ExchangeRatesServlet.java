package servlet;

import com.google.gson.Gson;
import dto.ExchangeRateDto;
import exception.ServiceException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import service.ExchangeRateService;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;


@WebServlet("/exchangeRates")
public class ExchangeRatesServlet extends HttpServlet {
    private final ExchangeRateService exchangeRateService = ExchangeRateService.getInstance();
    Gson gson = new Gson();
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            var exchangeRates = exchangeRateService.getExchangeRates();
            response.setContentType("application/json; charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            response.setStatus(HttpServletResponse.SC_OK);
            if (exchangeRates.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            }else {
                response.setStatus(HttpServletResponse.SC_OK);
                var json = gson.toJson(exchangeRates);
                try(var writer = response.getWriter()){
                    writer.write(json);
                }
            }


        }catch (Exception e){
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

}
