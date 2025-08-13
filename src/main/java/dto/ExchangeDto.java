package dto;

import entity.Currency;

import java.math.BigDecimal;
import java.util.Objects;

public class ExchangeDto {
    ExchangeRateDto exchangeRate;

    private final Currency baseCurrency;
    private final Currency targetCurrency;
    private final BigDecimal rate;
    BigDecimal amount;
    BigDecimal convertedAmount;


    public ExchangeDto(Currency baseCurrency, Currency targetCurrency, BigDecimal rate, BigDecimal amount, BigDecimal convertedAmount) {
        this.baseCurrency = baseCurrency;
        this.targetCurrency = targetCurrency;
        this.rate = rate;
        this.amount = amount;
        this.convertedAmount = convertedAmount;
    }



    public BigDecimal getAmount() {
        return amount;
    }



    public BigDecimal getRate() {
        return rate;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ExchangeDto that = (ExchangeDto) o;
        return Objects.equals(exchangeRate, that.exchangeRate) && Objects.equals(amount, that.amount) && Objects.equals(convertedAmount, that.convertedAmount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(exchangeRate, amount, convertedAmount);
    }

    @Override
    public String toString() {
        return "ExchangeDto{" +
               "exchangeRate=" + exchangeRate +
               ", amount=" + amount +
               ", convertedAmount=" + convertedAmount +
               '}';
    }
}
