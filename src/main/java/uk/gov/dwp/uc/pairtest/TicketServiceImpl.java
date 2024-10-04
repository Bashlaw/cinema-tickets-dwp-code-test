package uk.gov.dwp.uc.pairtest;

import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

public class TicketServiceImpl implements TicketService {
    /**
     * Should only have private methods other than the one below.
     */

    private static final int MAX_TICKETS = 25;
    private static final int ADULT_TICKET_PRICE = 25;
    private static final int CHILD_TICKET_PRICE = 15;

    private final TicketPaymentService ticketPaymentService;
    private final SeatReservationService seatReservationService;

    public TicketServiceImpl(TicketPaymentService ticketPaymentService , SeatReservationService seatReservationService) {
        this.ticketPaymentService = ticketPaymentService;
        this.seatReservationService = seatReservationService;
    }

    @Override
    public void purchaseTickets(Long accountId , TicketTypeRequest... ticketTypeRequests) throws InvalidPurchaseException {

        validateAccount(accountId);

        int totalTickets = 0;
        int adultTickets = 0;
        int childTickets = 0;
        int infantTickets = 0;
        int totalAmountToPay = 0;

        // Process the tickets
        for (TicketTypeRequest request : ticketTypeRequests) {
            switch (request.getTicketType()) {
                case ADULT:
                    adultTickets += request.getNoOfTickets();
                    totalAmountToPay += request.getNoOfTickets() * ADULT_TICKET_PRICE;
                    break;
                case CHILD:
                    childTickets += request.getNoOfTickets();
                    totalAmountToPay += request.getNoOfTickets() * CHILD_TICKET_PRICE;
                    break;
                case INFANT:
                    infantTickets += request.getNoOfTickets();
                    // Infants are free, no addition to totalAmountToPay
                    break;
            }
            totalTickets += request.getNoOfTickets();
        }

        // Validate business rules
        if (totalTickets > MAX_TICKETS) {
            throw new InvalidPurchaseException("Cannot purchase more than 25 tickets at a time.");
        }
        if (adultTickets == 0 && (childTickets > 0 || infantTickets > 0)) {
            throw new InvalidPurchaseException("Child and Infant tickets cannot be purchased without an Adult ticket.");
        }

        // Process payment and seat reservations
        ticketPaymentService.makePayment(accountId , totalAmountToPay);
        int seatsToReserve = adultTickets + childTickets; // Infants don't need a seat
        seatReservationService.reserveSeat(accountId , seatsToReserve);

    }

    private void validateAccount(Long accountId) throws InvalidPurchaseException {
        if (accountId == null || accountId <= 0) {
            throw new InvalidPurchaseException("Invalid account ID.");
        }
    }

}
