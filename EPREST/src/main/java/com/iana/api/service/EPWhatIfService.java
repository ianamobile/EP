package com.iana.api.service;

import com.iana.api.domain.EPWhatIfForm;

public interface EPWhatIfService {

	EPWhatIfForm getWhatIfTemplate(String epAcctNo) throws Exception;
}
