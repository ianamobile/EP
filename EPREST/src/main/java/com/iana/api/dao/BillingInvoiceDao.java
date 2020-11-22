package com.iana.api.dao;

import java.util.List;

import com.iana.api.domain.Batch;
import com.iana.api.domain.FinanceSearch;
import com.iana.api.domain.InternalFinanceSearch;
import com.iana.api.domain.InvoiceHeader;

public interface BillingInvoiceDao {

	List<FinanceSearch> getEPMCInvData(InternalFinanceSearch internalFinanceSearch, boolean pagination)
			throws Exception;

	InvoiceHeader getTempInvData(FinanceSearch searchParams) throws Exception;

	List<Batch> getBatchInvoice() throws Exception;

}