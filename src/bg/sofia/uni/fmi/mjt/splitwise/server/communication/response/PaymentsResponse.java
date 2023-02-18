package bg.sofia.uni.fmi.mjt.splitwise.server.communication.response;

import bg.sofia.uni.fmi.mjt.splitwise.server.logic.payment.Payment;

import java.util.List;

public class PaymentsResponse extends Response {
    List<Payment> payments;

    public PaymentsResponse(boolean ok, String response, List<Payment> payments) {
        super(ok, response);
        this.payments = payments;
    }
}
