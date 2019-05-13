package pl.com.bottega.ecommerce.sales.application.api.handler;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import pl.com.bottega.ecommerce.canonicalmodel.publishedlanguage.ClientData;
import pl.com.bottega.ecommerce.canonicalmodel.publishedlanguage.Id;
import pl.com.bottega.ecommerce.sales.application.api.command.AddProductCommand;
import pl.com.bottega.ecommerce.sales.domain.client.Client;
import pl.com.bottega.ecommerce.sales.domain.client.ClientRepository;
import pl.com.bottega.ecommerce.sales.domain.equivalent.SuggestionService;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.Product;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.ProductRepository;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.ProductType;
import pl.com.bottega.ecommerce.sales.domain.reservation.Reservation;
import pl.com.bottega.ecommerce.sales.domain.reservation.ReservationRepository;
import pl.com.bottega.ecommerce.sharedkernel.Money;
import pl.com.bottega.ecommerce.system.application.SystemContext;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Date;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class AddProductCommandHandlerTest {

    AddProductCommand addProductCommand;
    AddProductCommandHandler addProductCommandHandler;
    Client client;
    Reservation reservation;
    SystemContext systemContext;
    Product product;

    @Mock
    ReservationRepository reservationRepository;

    @Mock
    ProductRepository productRepository;

    @Mock
    SuggestionService suggestionService;

    @Mock
    ClientRepository clientRepository;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        reservation = new Reservation(Id.generate(), Reservation.ReservationStatus.OPENED, new ClientData(Id.generate(), "client"), new Date());
        product = new Product(Id.generate(), new Money(BigDecimal.ONE, Currency.getInstance("EUR")), "product", ProductType.STANDARD);
        systemContext = new SystemContext();
        addProductCommand = new AddProductCommand(Id.generate(), Id.generate(), 10);
        client = new Client();
        addProductCommandHandler = new AddProductCommandHandler(reservationRepository, productRepository, suggestionService, clientRepository, systemContext);

        when(reservationRepository.load(any(Id.class))).thenReturn(reservation);
        when(suggestionService.suggestEquivalent(any(Product.class), any(Client.class))).thenReturn(new Product());
        doNothing().when(reservationRepository).save(any(Reservation.class));
    }

    @Test
    public void handle_testIfSuggestEquivalentMethodWillNotBeCalledWhenProductIsAvailable() {
        when(productRepository.load(any(Id.class))).thenReturn(product);
        when(clientRepository.load(any(Id.class))).thenReturn(client);
        addProductCommandHandler.handle(addProductCommand);
        verify(suggestionService, never()).suggestEquivalent(product, client);
    }

    @Test
    public void handle_testIfSuggestEquivalentMethodWillBeCalledWhenProductIsNotAvailable() {
        when(productRepository.load(any(Id.class))).thenReturn(product);
        when(clientRepository.load(any(Id.class))).thenReturn(client);
        product.markAsRemoved();
        when(productRepository.load(any(Id.class))).thenReturn(product);

        addProductCommandHandler.handle(addProductCommand);
        verify(suggestionService, times(1)).suggestEquivalent(product, client);
    }

    @Test
    public void handle_testIfAvailableProductWasAddedToReservation() {
        when(productRepository.load(any(Id.class))).thenReturn(product);
        addProductCommandHandler.handle(addProductCommand);
        assertThat(reservation.contains(product), is(true));
    }

    @Test
    public void handle_testIfNotAvailableProductWasNotAddedToReservation() {
        when(productRepository.load(any(Id.class))).thenReturn(product);
        product.markAsRemoved();
        when(productRepository.load(any(Id.class))).thenReturn(product);

        addProductCommandHandler.handle(addProductCommand);
        assertThat(reservation.contains(product), is(false));
    }
}
