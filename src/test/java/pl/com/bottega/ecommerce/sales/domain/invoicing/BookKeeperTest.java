package pl.com.bottega.ecommerce.sales.domain.invoicing;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import pl.com.bottega.ecommerce.canonicalmodel.publishedlanguage.ClientData;
import pl.com.bottega.ecommerce.canonicalmodel.publishedlanguage.Id;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.ProductData;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.ProductType;
import pl.com.bottega.ecommerce.sharedkernel.Money;

import java.math.BigDecimal;
import java.util.Date;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class BookKeeperTest {

    BookKeeper bookKeeper;
    InvoiceRequest invoiceRequest;
    ProductData productData;
    @Mock TaxPolicy taxPolicy;

    @Before public void init() {
        bookKeeper = new BookKeeper(new InvoiceFactory());
        invoiceRequest = new InvoiceRequest(new ClientData());
        productData = new ProductData(Id.generate(), new Money(BigDecimal.ONE), "product", ProductType.STANDARD, new Date());
        MockitoAnnotations.initMocks(this);
    }

    @Test public void issuance_testIfInvoiceRequestWithOneItemReturnOneInvoice() {
        RequestItem requestItem = new RequestItem(productData, 10, new Money(new BigDecimal(10)));
        invoiceRequest.add(requestItem);

        when(taxPolicy.calculateTax(any(ProductType.class), any(Money.class))).thenReturn(new Tax(new Money(new BigDecimal(1)), ""));
        Invoice invoice = bookKeeper.issuance(invoiceRequest, taxPolicy);

        assertThat(invoice.getItems().size(), is(1));
    }

    @Test public void issuance_testIfInvoiceRequestWithTwoItemsInvokeCalculateTaxMethodTwice() {
        RequestItem requestItem = new RequestItem(productData, 10, new Money(new BigDecimal(10)));
        RequestItem requestItem12 = new RequestItem(productData, 10, new Money(new BigDecimal(20)));
        invoiceRequest.add(requestItem);
        invoiceRequest.add(requestItem12);

        when(taxPolicy.calculateTax(any(ProductType.class), any(Money.class))).thenReturn(new Tax(new Money(new BigDecimal(300)), ""));
        Invoice invoice = bookKeeper.issuance(invoiceRequest, taxPolicy);
        verify(taxPolicy, times(2)).calculateTax(any(ProductType.class), any(Money.class));

    }

    @Test public void issuance_testIfInvoiceRequestWithOneItemInvokeCalculateTaxMethodOnce()
    {
        RequestItem requestItem = new RequestItem(productData, 10, new Money(new BigDecimal(10)));
        invoiceRequest.add(requestItem);

        when(taxPolicy.calculateTax(any(ProductType.class), any(Money.class))).thenReturn(new Tax(new Money(new BigDecimal(300)), ""));
        Invoice invoice = bookKeeper.issuance(invoiceRequest, taxPolicy);
        verify(taxPolicy, times(1)).calculateTax(any(ProductType.class), any(Money.class));
    }

    @Test public void issuance_testIfInvoiceRequestWithTwoItemsReturnTwoInvoices()
    {
        RequestItem requestItem = new RequestItem(productData, 10, new Money(new BigDecimal(10)));
        RequestItem requestItem2 = new RequestItem(productData, 10, new Money(new BigDecimal(20)));
        invoiceRequest.add(requestItem);
        invoiceRequest.add(requestItem2);

        when(taxPolicy.calculateTax(any(ProductType.class), any(Money.class))).thenReturn(new Tax(new Money(new BigDecimal(1)), ""));
        Invoice invoice = bookKeeper.issuance(invoiceRequest, taxPolicy);

        assertThat(invoice.getItems().size(), is(2));
    }
}
