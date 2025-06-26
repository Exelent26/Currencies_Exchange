package dto;

import java.math.BigDecimal;
import java.util.Objects;

public class ExchangeDto {
    ExchangeRateDto exchangeRate;
    BigDecimal amount;
    BigDecimal convertedAmount;

    public ExchangeDto(ExchangeRateDto exchangeRate, BigDecimal amount, BigDecimal convertedAmount) {
        this.exchangeRate = exchangeRate;
        this.amount = amount;
        this.convertedAmount = convertedAmount;
    }

    public ExchangeRateDto getExchangeRate() {
        return exchangeRate;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public BigDecimal getConvertedAmount() {
        return convertedAmount;
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
