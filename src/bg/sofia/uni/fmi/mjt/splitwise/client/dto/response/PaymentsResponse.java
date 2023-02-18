package bg.sofia.uni.fmi.mjt.splitwise.client.dto.response;

import java.util.List;
import bg.sofia.uni.fmi.mjt.splitwise.client.dto.Payment;

public class PaymentsResponse extends Response {
    private List<Payment> payments;

    public PaymentsResponse(boolean ok, String response, List<Payment> payments) {
        super(ok, response);
        this.payments = payments;
    }

    public List<Payment> getPayments() {
        return payments;
    }
}
