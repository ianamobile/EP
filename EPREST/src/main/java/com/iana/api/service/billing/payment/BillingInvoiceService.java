package com.iana.api.service.billing.payment;

import java.util.List;

import com.iana.api.domain.FinanceSearch;
import com.iana.api.domain.InternalFinanceSearch;
import com.iana.api.domain.InvoiceHeader;
import com.iana.api.domain.SecurityObject;

public interface BillingInvoiceService {

	List<FinanceSearch> getInvoices(InternalFinanceSearch internalFinanceSearch) throws Exception;

	InvoiceHeader getTempInvData(SecurityObject uBean, FinanceSearch searchParams) throws Exception;

}
