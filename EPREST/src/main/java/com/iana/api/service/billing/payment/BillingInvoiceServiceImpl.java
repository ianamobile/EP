package com.iana.api.service.billing.payment;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.iana.api.dao.BillingInvoiceDao;
import com.iana.api.dao.EpDao;
import com.iana.api.domain.AccountInfo;
import com.iana.api.domain.Batch;
import com.iana.api.domain.FinanceSearch;
import com.iana.api.domain.InternalFinanceSearch;
import com.iana.api.domain.InvoiceDet;
import com.iana.api.domain.InvoiceDetails;
import com.iana.api.domain.InvoiceHeader;
import com.iana.api.domain.LabelValueForm;
import com.iana.api.domain.SecurityObject;
import com.iana.api.utils.CommonUtils;
import com.iana.api.utils.GlobalVariables;
import com.iana.api.utils.ValidationUtils;

@Service
public class BillingInvoiceServiceImpl extends CommonUtils implements BillingInvoiceService {
	Logger log = LogManager.getLogger(this.getClass().getName());

	@Autowired
	private EpDao epDao;

	@Autowired
	private BillingInvoiceDao billingInvoiceDao;

	@Override
	public List<FinanceSearch> getInvoices(InternalFinanceSearch internalFinanceSearch) throws Exception {
		List<FinanceSearch> invList = billingInvoiceDao.getEPMCInvData(internalFinanceSearch, true);
		DecimalFormat format = new DecimalFormat("#,##0.00");
		List<FinanceSearch> finalInvList = new ArrayList<FinanceSearch>(0);
		if (isNotNullOrEmpty(invList)) {
			for (int i = 0; i < invList.size(); i++) {
				FinanceSearch financeSearchBean = invList.get(i);
				if (StringUtils.isBlank(financeSearchBean.getInvoiceAmnt())) {
					financeSearchBean.setInvoiceAmnt("0.00");
				}
				financeSearchBean.setInvoiceAmnt(format.format(Double.parseDouble(financeSearchBean.getInvoiceAmnt())));

				if (StringUtils.isBlank(financeSearchBean.getPaidAmnt())) {
					financeSearchBean.setPaidAmnt("0.00");
				}
				financeSearchBean.setPaidAmnt(format.format(Double.parseDouble(financeSearchBean.getPaidAmnt())));
				finalInvList.add(financeSearchBean);
			}
		}
		return finalInvList;
	}

	@Override
	public InvoiceHeader getTempInvData(SecurityObject uBean, FinanceSearch searchParams) throws Exception {
		String cancPay = "NO";
		InvoiceHeader invoiceHeader = billingInvoiceDao.getTempInvData(searchParams);
		populateAllBeans(invoiceHeader);
		AccountInfo accountInfo = epDao.getBasicAcctDtls(uBean.getAccountNumber());
		accountInfo.setOldScac(accountInfo.getScacCode());
		accountInfo.setOldUiiaStatus(accountInfo.getUiiaStatus());
		if (accountInfo.getIddMember().equals(GlobalVariables.YES)) {
			accountInfo.setIddStatus(GlobalVariables.ACTIVEMEMBER);
		} else if (accountInfo.getIddMember().equals(GlobalVariables.NO)) {
			accountInfo.setIddStatus(GlobalVariables.DELETEDMEMBER);
		}

		String statusCode[] = validateObject(accountInfo.getUiiaStatusCd()).split(GlobalVariables.COMMA);
		for (String code : statusCode) {
			if (code.equals("C1") && uBean.getAccountNumber().startsWith(GlobalVariables.ROLE_MC)
					&& !GlobalVariables.CLOSED.equalsIgnoreCase(invoiceHeader.getStatus())) {
				cancPay = "YES";
				break;
			}
		}

		invoiceHeader.setMcPay(cancPay);

		return invoiceHeader;
	}

	public void populateAllBeans(InvoiceHeader hb) throws Exception {
		InvoiceDetails form = new InvoiceDetails();
		List<InvoiceDet> billReq = new ArrayList<InvoiceDet>();
		List<LabelValueForm> listBatchOptions = new ArrayList<LabelValueForm>();
		listBatchOptions.add(new LabelValueForm("New WorkSet ", "new"));

		List<Batch> batchList = billingInvoiceDao.getBatchInvoice();
		if (ValidationUtils.isNullOrEmpty(batchList)) {
			batchList = new ArrayList<Batch>();
		}

		if (batchList != null && batchList.size() > 0) {
			for (Batch batchBean : batchList) {
				listBatchOptions.add(new LabelValueForm(batchBean.getBatchCode() + " - " + batchBean.getPymtBatchId(),
						batchBean.getPymtBatchId() + ""));
			}
		}

		DecimalFormat format = new DecimalFormat("#,##0.00");
		form.setInvoiceNo(hb.getInvNo());
		form.setInvoiceDate(hb.getInvDate());
		form.setStatus(hb.getStatus());
		form.setInvTemplate(hb.getInvTemplate());
		form.setBillTo(hb.getBillTo());
		form.setAcctNo(hb.getAcctNo());
		form.setInvoiceAmt(format.format(DecimalFormat.getNumberInstance()
				.parse(StringUtils.isBlank(hb.getInvoiceAmnt()) ? "0" : hb.getInvoiceAmnt()).doubleValue()));
		form.setPaidAmt(format.format(DecimalFormat.getNumberInstance()
				.parse(StringUtils.isBlank(hb.getPaidAmnt()) ? "0" : hb.getPaidAmnt()).doubleValue()));
		form.setMember(hb.getMember());
		form.setRemarks(hb.getRemarks());
		form.setWorkSetNm(hb.getWorkSetNm());
		form.setAddToBatch(hb.getAddToBatch());

		if (hb.getBillReq() != null && hb.getBillReq().size() > 0) {
			List<InvoiceDet> lst = hb.getBillReq();
			for (InvoiceDet idb : lst) {
				InvoiceDet f = new InvoiceDet();
				f.setPkId(idb.getPkId());
				f.setBillCode(idb.getBillCode());
				String billAmt = idb.getBillCodeAmnt() == null ? ""
						: format.format(DecimalFormat.getNumberInstance()
								.parse(StringUtils.isBlank(idb.getBillCodeAmnt()) ? "0" : idb.getBillCodeAmnt())
								.doubleValue());
				f.setBillCodeAmnt(billAmt);
				f.setBillId(idb.getBillId());
				f.setDesc(idb.getDesc());
				f.setGlAcctNo(idb.getGlAcctNo());

				billReq.add(f);
			}
		}

		hb.setInvoiceDetails(form);
		hb.setBillReq(billReq);
		hb.setListBatchOptions(listBatchOptions);

	}

}
