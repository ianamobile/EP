package com.iana.api.security;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.iana.api.dao.UserDao;
import com.iana.api.domain.AccountInfo;
import com.iana.api.domain.ContactDetail;
import com.iana.api.domain.Login;
import com.iana.api.domain.LoginHistory;
import com.iana.api.domain.SecurityObject;
import com.iana.api.domain.User;
import com.iana.api.utils.CommonUtils;
import com.iana.api.utils.GlobalVariables;

@Service
public class JwtUserDetailsService extends CommonUtils {
	Logger log = LogManager.getLogger(this.getClass().getName());

	@Autowired
	private UserDao userDao;

	
	public SecurityObject loadUserByLoginInfo(Login login) throws Exception {
		SecurityObject securityObject = initSecurityObject();
		
		User user = this.userDao.validate(login);
		if (StringUtils.isBlank(user.getAccountNumber())) {
			throw new BadCredentialsException(env.getProperty("msg_error_invalid_username_password"));
		}
		
		securityObject.setUsername(user.getUsername());
		securityObject.setAccountNumber(user.getAccountNumber());
		securityObject.setRoleName(user.getRoleName());
		securityObject.setUiiaStaff(false);
		
		List<AccountInfo> accountInfoList = userDao.getAccountInfos(user.getAccountNumber());
		if (isNotNullOrEmpty(accountInfoList)) {
			
			if (accountInfoList.size() > 1) {
				throw new UsernameNotFoundException(env.getProperty("msg_error_moredata_get_accountInfo"));

			} else {

				AccountInfo accountInfo = accountInfoList.get(0);
				securityObject.setAccountNumber(accountInfo.getAccountNumber());
				securityObject.setScac(CommonUtils.validateObject(accountInfo.getScac()));
				securityObject.setCompanyName(CommonUtils.validateObject(accountInfo.getCompanyName()));
				securityObject.setStatus(CommonUtils.validateObject(accountInfo.getUiiaStatus()));
				
				// code to get last login information starts
				LoginHistory loginHistory = userDao.getLastLogin(securityObject.getUsername(), securityObject.getAccountNumber());
				
				securityObject.setIpAddress(StringUtils.isBlank(loginHistory.getIpAddress()) ? StringUtils.EMPTY : loginHistory.getIpAddress());
				securityObject.setLastLoginDateTime(StringUtils.isBlank(loginHistory.getLoginTime()) ? StringUtils.EMPTY : loginHistory.getLoginTime());
				
				// code to insert login details in history starts
				userDao.createLoginHistory(securityObject);
				
				ContactDetail cd = userDao.getContact(null, user.getAccountNumber(), GlobalVariables.CONTACTADDTYPE);
				 
				securityObject.setEmail(CommonUtils.validateObject(cd.getPriEmail()));
				securityObject.setFirstName(CommonUtils.validateObject(cd.getFirstName()));
				securityObject.setLastName(CommonUtils.validateObject(cd.getLastName()));
				securityObject.setEnabled(true);
				List<Authority> authorities = new ArrayList<>();
				if(GlobalVariables.ROLE_EP.equalsIgnoreCase(securityObject.getRoleName())) {
					authorities.add(new Authority(1L, AuthorityName.ROLE_EP));
				}
				
				securityObject.setAuthorities(mapToGrantedAuthorities(authorities));
			
			}
		} else {
			throw new UsernameNotFoundException(env.getProperty("msg_error_nodata_get_accountInfo"));
		}
		
		log.info("securityObject:" + securityObject);
		return securityObject;
	}

	private static List<GrantedAuthority> mapToGrantedAuthorities(List<Authority> authorities) {
        return authorities.stream()
                .map(authority -> new SimpleGrantedAuthority(authority.getName().name()))
                .collect(Collectors.toList());
    }

	
}
