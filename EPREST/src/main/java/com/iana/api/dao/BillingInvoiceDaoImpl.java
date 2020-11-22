package com.iana.api.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Repository;

import com.iana.api.domain.Adjustments;
import com.iana.api.domain.Batch;
import com.iana.api.domain.FinanceSearch;
import com.iana.api.domain.InternalFinanceSearch;
import com.iana.api.domain.InvoiceDet;
import com.iana.api.domain.InvoiceHeader;
import com.iana.api.domain.Payment;
import com.iana.api.utils.CommonUtils;
import com.iana.api.utils.DateTimeFormater;
import com.iana.api.utils.GlobalVariables;
import com.iana.api.utils.ValidationUtils;

@Repository
public class BillingInvoiceDaoImpl extends GenericDAO implements BillingInvoiceDao {
	Logger log = LogManager.getLogger(this.getClass().getName());

	@Autowired
	@Qualifier("uiiaDataSource")
	private DataSource uiiaDataSource;

	@Override
	public List<FinanceSearch> getEPMCInvData(InternalFinanceSearch internalFinanceSearch, boolean pagination)
			throws Exception {
		List<Object> params = new ArrayList<>();
		StringBuilder sbGetQuery = new StringBuilder("");

		sbGetQuery.append(" SELECT a.company_name, i.inv_hdr_id, i.prm_inv_no, i.temp_inv_no, i.inv_date, ");
		sbGetQuery.append(" i.account_no, i.status, i.inv_amt, i.invoice_type,  i.inv_batch_id, i.bill_date, ");
		sbGetQuery.append(" p.pymt_dt, i.paid_amt,i.remarks,i.invoice_template invtemp ");
		sbGetQuery.append(" FROM (invoice_header i,account_info a) LEFT JOIN payment_details p ");
		sbGetQuery.append(" ON (i.inv_hdr_id = p.inv_hdr_id ) ");
		sbGetQuery.append(" WHERE i.account_no = a.account_no AND i.account_no = ? AND i.status like ? ");
		sbGetQuery.append(" GROUP BY a.company_name,i.inv_hdr_id, i.prm_inv_no, i.temp_inv_no, i.inv_date, ");
		sbGetQuery.append(" i.account_no, i.status, i.inv_amt, i.invoice_type,  i.inv_batch_id, i.bill_date ");
		sbGetQuery.append(" ORDER BY i.inv_date DESC ");

		params.add(internalFinanceSearch.getAccNo());
		params.add(internalFinanceSearch.getStatus());

		if (pagination) {
			sbGetQuery.append(" LIMIT ?, ? ");
			params.add(internalFinanceSearch.getRecordFrom());
			params.add(internalFinanceSearch.getPageSize());
		}

		return getSpringJdbcTemplate(this.uiiaDataSource).query(sbGetQuery.toString(), params.toArray(),
				new ResultSetExtractor<List<FinanceSearch>>() {

					@Override
					public List<FinanceSearch> extractData(ResultSet rs) throws SQLException, DataAccessException {
						FinanceSearch financeBean = null;
						List<FinanceSearch> invoiceList = new ArrayList<>();
						while (rs.next()) {
							financeBean = new FinanceSearch();
							financeBean.setInvId(rs.getString("inv_hdr_id") == null ? "" : rs.getString("inv_hdr_id"));
							financeBean.setBillTo(
									rs.getString("company_name") == null ? "" : rs.getString("company_name"));
							financeBean.setInvDate(rs.getString("inv_date") == null ? "" : rs.getString("inv_date"));
							if (rs.getString("prm_inv_no") != null)
								financeBean.setInvNo(rs.getString("prm_inv_no"));
							else if (rs.getString("temp_inv_no") != null)
								financeBean.setInvNo(rs.getString("temp_inv_no"));
							financeBean.setInvoiceAmnt(rs.getString("inv_amt") == null ? "0" : rs.getString("inv_amt"));
							financeBean.setStatus(rs.getString("status") == null ? "" : rs.getString("status"));
							financeBean.setPaidAmnt(rs.getString("paid_amt") == null ? "0" : rs.getString("paid_amt"));
							financeBean.setPayDate(rs.getString("pymt_dt") == null ? "" : rs.getString("pymt_dt"));
							financeBean.setInvType(
									rs.getString("invoice_type") == null ? "" : rs.getString("invoice_type"));
							financeBean.setRemarks(rs.getString("remarks") == null ? "" : rs.getString("remarks"));
							financeBean.setInvTemplate(
									rs.getString("invtemp") == null ? StringUtils.EMPTY : rs.getString("invtemp"));
							invoiceList.add(financeBean);
						}

						return invoiceList;
					}
				});

	}

	@Override
	public InvoiceHeader getTempInvData(FinanceSearch searchParams) throws Exception {
		String invNo = searchParams.getInvNo() == null ? "%" : searchParams.getInvNo();
		InvoiceHeader invHeader = new InvoiceHeader();

		StringBuilder sbQry = new StringBuilder();
		StringBuilder sbAdjQry = new StringBuilder();
		StringBuilder sbPayQry = new StringBuilder();

		sbQry.append(
				"SELECT h.inv_hdr_id as inv_id,h.prm_inv_no as pinv_no,h.temp_inv_no as temp_no,h.inv_date as inv_dt,h.account_no as acct_no,h.status as stat,h.inv_amt as inv_amt,h.invoice_type as inv_type,h.inv_batch_id as batch_id,h.member as mem,a.uiia_status as ustat,h.remarks,h.paid_amt as paid,h.invoice_template invtemp, ");
		sbQry.append(
				" d.inv_dtls_id as detail_id,d.bill_cd_id as bill_id,d.inv_gl_acc as gl_acc,b.bill_cd as bill_cd,b.bill_desc as bill_desc,d.inv_dtl_amt as bill_amt,a.company_name as bill_to");
		sbQry.append(" FROM invoice_header h,invoice_details d,account_info a,bill_codes b");
		sbQry.append(
				" WHERE h.inv_hdr_id=d.inv_hdr_id AND h.account_no = a.account_no AND d.bill_cd_id = b.bill_cd_id AND h.inv_hdr_id = ?");

		sbAdjQry.append(
				"SELECT inv_dtl_desc as reasons,inv_gl_acc as glacct,inv_dtl_amt as amount,inv_adj_type as adjtype");
		sbAdjQry.append(" FROM invoice_header h,invoice_details d,account_info a");
		sbAdjQry.append(
				" WHERE h.inv_hdr_id=d.inv_hdr_id AND h.account_no = a.account_no AND h.inv_hdr_id = ? and d.inv_adj_type is not null");

		sbPayQry.append(
				"SELECT p.paid_amt as paid_amt,p.pymt_dt as pymt_dt,pymt_mode as pymt_mode,p.auth_cd,b.batch_code,p.chq_no");
		sbPayQry.append(" FROM invoice_header h,payment_details p,account_info a,batch_payment b");
		sbPayQry.append(
				" WHERE h.inv_hdr_id=p.inv_hdr_id AND h.account_no = a.account_no AND p.pymt_batch_id=b.pymt_batch_id AND p.inv_hdr_id = ?");

		// getting bill code information
		List<InvoiceDet> detBean = getSpringJdbcTemplate(this.uiiaDataSource).query(sbQry.toString(),
				new ResultSetExtractor<List<InvoiceDet>>() {

					@Override
					public List<InvoiceDet> extractData(ResultSet rs) throws SQLException, DataAccessException {
						List<InvoiceDet> detBean = new ArrayList<>();
						while (rs.next()) {
							InvoiceDet invDetails = new InvoiceDet();
							invHeader.setInvPkid(rs.getInt("inv_id"));
							invHeader.setInvNo(rs.getString("pinv_no") == null ? rs.getString("temp_no")
									: rs.getString("pinv_no"));
							invHeader.setInvDate(
									DateTimeFormater.formatSqlDate(rs.getDate("inv_dt"), DateTimeFormater.FORMAT4));
							invHeader.setStatus(rs.getString("stat"));
							invHeader.setUiiaStatus(rs.getString("ustat"));
							invHeader.setAcctNo(rs.getString("acct_no"));
							invHeader.setBillTo(rs.getString("bill_to"));
							invHeader.setAddToBatch(rs.getString("batch_id"));

							invHeader.setPaidAmnt(rs.getString("paid"));
							invHeader.setPaidAmnt(CommonUtils.formatAmount.format(Double.parseDouble(
									(invHeader.getPaidAmnt().equals("") || invHeader.getPaidAmnt() == null ? "0"
											: invHeader.getPaidAmnt()))));

							invHeader.setInvoiceAmnt(rs.getString("inv_amt"));
							invHeader.setInvoiceAmnt(CommonUtils.formatAmount.format(Double.parseDouble(
									(invHeader.getInvoiceAmnt().equals("") || invHeader.getInvoiceAmnt() == null) ? "0"
											: invHeader.getInvoiceAmnt())));

							invHeader.setInvType(rs.getString("inv_type"));
							invHeader.setMember(rs.getString("mem") == null ? "" : rs.getString("mem"));
							invHeader.setInvTemplate(rs.getString("invtemp"));
							invHeader.setRemarks(rs.getString("remarks"));
							invDetails.setPkId(rs.getInt("detail_id"));
							invDetails.setBillId(rs.getString("bill_id"));
							invDetails.setBillCode(rs.getString("bill_cd"));
							invDetails.setDesc(rs.getString("bill_desc"));
							invDetails.setGlAcctNo(rs.getString("gl_acc"));

							invDetails.setBillCodeAmnt(rs.getString("bill_amt"));
							String billAmt = invDetails.getBillCodeAmnt() == null ? ""
									: CommonUtils.formatAmount
											.format(Double.parseDouble((invDetails.getBillCodeAmnt().equals("")
													|| invDetails.getBillCodeAmnt() == null) ? "0"
															: invDetails.getBillCodeAmnt()));
							invDetails.setBillCodeAmnt(billAmt);

							detBean.add(invDetails);

						}
						return detBean;
					}
				}, invNo);

		invHeader.setBillReq(detBean);
		invHeader.setInvDetBean(detBean);
		invHeader.setAdjBean(getSpringJdbcTemplate(this.uiiaDataSource).query(sbAdjQry.toString(),
				new ResultSetExtractor<List<Adjustments>>() {

					@Override
					public List<Adjustments> extractData(ResultSet rs) throws SQLException, DataAccessException {
						double revAmt = 0;
						List<Adjustments> adjustments = new ArrayList<>();
						while (rs.next()) {
							Adjustments adjBean = new Adjustments();
							adjBean.setAdjtype(rs.getString("adjtype"));
							adjBean.setAmtForAdj(rs.getString("amount"));
							adjBean.setGlAcct(rs.getString("glacct"));
							adjBean.setReasons(rs.getString("reasons"));
							if (rs.getString("adjtype") != null
									&& rs.getString("adjtype").equalsIgnoreCase("Reversal")) {
								revAmt = revAmt + rs.getDouble("amount");
							}
							adjBean.setRevAmt(revAmt);
							adjustments.add(adjBean);

						}
						if (ValidationUtils.isNotNullOrEmpty(adjustments) && adjustments.get(0) != null) {
							adjustments.get(0).setRevAmt(revAmt);
						}
						return adjustments;
					}
				}, invNo));
		invHeader.setPymtBean(getSpringJdbcTemplate(this.uiiaDataSource).query(sbPayQry.toString(),
				new ResultSetExtractor<List<Payment>>() {
					@Override
					public List<Payment> extractData(ResultSet rs) throws SQLException, DataAccessException {
						double paid_amt = 0;
						List<Payment> payments = new ArrayList<>();
						while (rs.next()) {
							Payment payBean = new Payment();
							payBean.setPymtDate(
									DateTimeFormater.formatSqlDate(rs.getDate("pymt_dt"), DateTimeFormater.FORMAT4));
							paid_amt = paid_amt + rs.getDouble("paid_amt");
							payBean.setPymtAmnt(rs.getString("paid_amt"));
							payBean.setPymtMode(rs.getString("pymt_mode"));
							payBean.setAuthCd((rs.getString("auth_cd") == null || rs.getString("auth_cd").equals(""))
									? rs.getString("chq_no")
									: rs.getString("auth_cd"));
							payBean.setPymtDtlId(rs.getString("batch_code"));
							payBean.setPaidAmt(paid_amt);
							payments.add(payBean);
						}
						if (ValidationUtils.isNotNullOrEmpty(payments) && payments.get(0) != null) {
							payments.get(0).setPaidAmt(paid_amt);
						}
						return payments;
					}
				}, invNo));

		double paid_amt = 0;
		double revAmt = 0;
		if (ValidationUtils.isNotNullOrEmpty(invHeader.getAdjBean()) && invHeader.getAdjBean().get(0) != null) {
			revAmt = invHeader.getAdjBean().get(0).getRevAmt();
		}

		if (ValidationUtils.isNotNullOrEmpty(invHeader.getPymtBean()) && invHeader.getPymtBean().get(0) != null) {
			paid_amt = invHeader.getPymtBean().get(0).getPaidAmt();
		}
		invHeader.setPaidAmnt(Double.toString(paid_amt - revAmt));
		return invHeader;

	}

	@Override
	public List<Batch> getBatchInvoice() throws Exception {
		StringBuffer sbGetQuery1 = new StringBuffer(" ");

		sbGetQuery1.append(" SELECT inv_batch_id, batch_code, batch_cnt,");
		sbGetQuery1.append(" batch_amt, batch_dt, batch_desc ");
		sbGetQuery1.append(" FROM batch_invoice where batch_status ='" + GlobalVariables.TEMP + "'");

		return getSpringJdbcTemplate(this.uiiaDataSource).query(sbGetQuery1.toString(),
				new ResultSetExtractor<List<Batch>>() {
					@Override
					public List<Batch> extractData(ResultSet rs) throws SQLException, DataAccessException {
						List<Batch> batchPymtList = new ArrayList<Batch>();
						while (rs.next()) {
							Batch batchPymtBean = new Batch();

							if (rs.getString("batch_amt") != null)
								batchPymtBean.setBatchAmnt(rs.getString("batch_amt"));
							if (rs.getString("batch_code") != null)
								batchPymtBean.setBatchCode(rs.getString("batch_code"));
							if (rs.getString("batch_cnt") != null)
								batchPymtBean.setBatchCount(rs.getString("batch_cnt"));
							if (rs.getString("batch_dt") != null)
								batchPymtBean.setBatchDate(rs.getString("batch_dt"));
							if (rs.getString("batch_desc") != null)
								batchPymtBean.setBatchDesc(rs.getString("batch_desc"));
							if (rs.getInt("inv_batch_id") != 0)
								batchPymtBean.setPymtBatchId(rs.getInt("inv_batch_id"));

							batchPymtList.add(batchPymtBean);
						}

						return batchPymtList;
					}
				});
	}

}
