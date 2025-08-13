package servlet;

import com.google.gson.Gson;
import dto.ExchangeDto;
import exception.ServiceException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import service.ExchangeService;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/exchange")
public class ExchangeServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        Gson gson = new Gson();

        ExchangeService exchangeService = ExchangeService.getInstance();
        String from = request.getParameter("from");
        String to = request.getParameter("to");
        String amountString = request.getParameter("amount");


        try {
            ExchangeDto exchangeDto = exchangeService.performExchange(from, to, amountString);
            response.setStatus(HttpServletResponse.SC_OK);
            String json = gson.toJson(exchangeDto);
            try (PrintWriter writer = response.getWriter()) {
                writer.write(json);
            }
        } catch (ServiceException e) {
            response.setStatus(e.getHttpStatusCode());
            try (PrintWriter writer = response.getWriter()) {
                writer.write(e.getMessage());
            }

        }

    }
}
