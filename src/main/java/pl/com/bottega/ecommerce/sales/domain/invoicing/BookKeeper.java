/*
 * Copyright 2011-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pl.com.bottega.ecommerce.sales.domain.invoicing;

import pl.com.bottega.ecommerce.sharedkernel.Money;

public class BookKeeper {

	private InvoiceFactory invoiceFactory;
	public BookKeeper(InvoiceFactory invoiceFactory) {
		this.invoiceFactory = invoiceFactory;
	}
	public Invoice issuance(InvoiceRequest invoiceRequest, TaxPolicy taxPolicy) {
		Invoice invoice = invoiceFactory.create(invoiceRequest.getClientData());

		for (RequestItem item : invoiceRequest.getItems()) {
			Money net = item.getTotalCost();
			Tax tax = taxPolicy.calculateTax(item.getProductData().getType(),
					net);

			InvoiceLine invoiceLine = new InvoiceLine(item.getProductData(),
					item.getQuantity(), net, tax);
			invoice.addItem(invoiceLine);
		}
		

		return invoice;
	}

}
