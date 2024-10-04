package uk.gov.dwp.uc.pairtest;

import org.junit.Test;
import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class TicketServiceTest {

    @Test
    public void testValidPurchaseWithAdultChildAndInfantTickets() throws InvalidPurchaseException {
        TicketPaymentService paymentService = mock(TicketPaymentService.class);
        SeatReservationService reservationService = mock(SeatReservationService.class);
        TicketServiceImpl ticketService = new TicketServiceImpl(paymentService , reservationService);

        ticketService.purchaseTickets(123L ,
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT , 2) ,
                new TicketTypeRequest(TicketTypeRequest.Type.CHILD , 3) ,
                new TicketTypeRequest(TicketTypeRequest.Type.INFANT , 1)
        );

        verify(paymentService).makePayment(123L , 95); // 2*25 + 3*15 = 95
        verify(reservationService).reserveSeat(123L , 5); // 2 adults + 3 children = 5 seats
    }

    @Test(expected = InvalidPurchaseException.class)
    public void testPurchaseExceedsMaxTickets() throws InvalidPurchaseException {
        TicketPaymentService paymentService = mock(TicketPaymentService.class);
        SeatReservationService reservationService = mock(SeatReservationService.class);
        TicketServiceImpl ticketService = new TicketServiceImpl(paymentService , reservationService);

        ticketService.purchaseTickets(123L ,
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT , 10) ,
                new TicketTypeRequest(TicketTypeRequest.Type.CHILD , 10) ,
                new TicketTypeRequest(TicketTypeRequest.Type.INFANT , 6) // total = 26 tickets
        );
    }

    @Test(expected = InvalidPurchaseException.class)
    public void testPurchaseChildTicketsWithoutAdult() throws InvalidPurchaseException {
        TicketPaymentService paymentService = mock(TicketPaymentService.class);
        SeatReservationService reservationService = mock(SeatReservationService.class);
        TicketServiceImpl ticketService = new TicketServiceImpl(paymentService , reservationService);

        ticketService.purchaseTickets(123L ,
                new TicketTypeRequest(TicketTypeRequest.Type.CHILD , 2) // No adults
        );
    }

    @Test(expected = InvalidPurchaseException.class)
    public void testPurchaseInfantTicketsWithoutAdult() throws InvalidPurchaseException {
        TicketPaymentService paymentService = mock(TicketPaymentService.class);
        SeatReservationService reservationService = mock(SeatReservationService.class);
        TicketServiceImpl ticketService = new TicketServiceImpl(paymentService , reservationService);

        ticketService.purchaseTickets(123L ,
                new TicketTypeRequest(TicketTypeRequest.Type.INFANT , 1) // No adults
        );
    }

    @Test
    public void testValidPurchaseWithOnlyAdultTickets() throws InvalidPurchaseException {
        TicketPaymentService paymentService = mock(TicketPaymentService.class);
        SeatReservationService reservationService = mock(SeatReservationService.class);
        TicketServiceImpl ticketService = new TicketServiceImpl(paymentService , reservationService);

        ticketService.purchaseTickets(123L ,
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT , 4)
        );

        verify(paymentService).makePayment(123L , 100); // 4 * 25 = 100
        verify(reservationService).reserveSeat(123L , 4); // 4 seats for adults
    }

    @Test
    public void testPurchaseExactly25Tickets() throws InvalidPurchaseException {
        TicketPaymentService paymentService = mock(TicketPaymentService.class);
        SeatReservationService reservationService = mock(SeatReservationService.class);
        TicketServiceImpl ticketService = new TicketServiceImpl(paymentService , reservationService);

        ticketService.purchaseTickets(123L ,
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT , 10) ,
                new TicketTypeRequest(TicketTypeRequest.Type.CHILD , 10) ,
                new TicketTypeRequest(TicketTypeRequest.Type.INFANT , 5) // total = 25 tickets
        );

        verify(paymentService).makePayment(123L , 400); // 10 * 25 + 10 * 15 = 400
        verify(reservationService).reserveSeat(123L , 20); // 10 adults + 10 children = 20 seats
    }

    @Test(expected = InvalidPurchaseException.class)
    public void testInvalidAccountId() throws InvalidPurchaseException {
        TicketPaymentService paymentService = mock(TicketPaymentService.class);
        SeatReservationService reservationService = mock(SeatReservationService.class);
        TicketServiceImpl ticketService = new TicketServiceImpl(paymentService , reservationService);

        ticketService.purchaseTickets(null , new TicketTypeRequest(TicketTypeRequest.Type.ADULT , 1)); // Invalid accountId
    }

    @Test(expected = InvalidPurchaseException.class)
    public void testZeroTicketsPurchase() throws InvalidPurchaseException {
        TicketPaymentService paymentService = mock(TicketPaymentService.class);
        SeatReservationService reservationService = mock(SeatReservationService.class);
        TicketServiceImpl ticketService = new TicketServiceImpl(paymentService , reservationService);

        ticketService.purchaseTickets(123L , new TicketTypeRequest(TicketTypeRequest.Type.ADULT , 0)); // No tickets
    }

}
