package servlet;

import com.google.gson.Gson;
import dto.ExchangeDto;
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
import java.math.BigDecimal;
import java.util.Optional;

@WebServlet("/exchange")
public class ExchangeServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        Gson gson = new Gson();
        DataValidator dataValidator = DataValidator.getInstance();
        ExchangeRateService exchangeRateService = ExchangeRateService.getInstance();
        String from = request.getParameter("from");
        String to = request.getParameter("to");
        String amountString = request.getParameter("amount");

        //TODO: в датавалидатор прописать проверку количества - чтобы не 0, чтобы не минус
        //ну и собственно провреку для фром и ту, чтобы они были валютами
        //TODO: в сервис передать как эксчендж оперейшн calculateExchange()
        ExchangeDto finish = exchangeRateService.calculateExchange(from,to,amountString);

        /*if(dataValidator.checkNullAndBlank(from, to, amountString) ){
            Optional<ExchangeRateDto> exchangeRateByCode = exchangeRateService.getExchangeRateByCode(from, to);
            if (exchangeRateByCode.isEmpty()){
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            ExchangeRateDto exchangeRateDto = exchangeRateByCode.get();

            BigDecimal rate = exchangeRateDto.getRate();
            //создания биздецемала в метод наверное надо перенести
            BigDecimal amount = null;
            try{
                amount = new BigDecimal(amountString);
            }catch (NumberFormatException e){
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
            BigDecimal convertedAmount = rate.multiply(amount);

            ExchangeDto finish = new ExchangeDto(exchangeRateDto, amount, convertedAmount);*/
            response.setStatus(HttpServletResponse.SC_OK);
            String json = gson.toJson(finish);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(json);




        };

        // TODO: Теперь у нас есть 3 варианта проверок:
        // 1  - если обменный курс существует, то мы просто перемножаем количество на рейт
        // 2 - если прямого обменного курса нет, но есть обмен из фром в юсд и из юсд в to.
        // для этого from*FROMUSDRATE*TOUSDRATE*AMOUNT
        // 3 - существует обратный курс т.е. сначала берется курс
        // из to в from  to * rate = from и нам требуется сформировать новый обменный курс и сформировать
        // ответ исходя из to = from/rate * amount
        // ну и вариант что у нас просто нет такого обменного курса - выбрасываем исключение и ставим ошибку





    }

