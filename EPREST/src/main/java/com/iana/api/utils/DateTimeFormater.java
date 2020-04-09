package com.iana.api.utils;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DateTimeFormater {

	static Logger log = LogManager.getLogger(DateTimeFormater.class);

	public static final String FORMAT1 = "MM/yyyy";
	public static final String FORMAT4 = "MM/dd/yyyy";
	public static final String FORMAT9 = "yyyy-MM-dd HH:mm:ss";
	public static final String FORMAT5 = "MM/dd/yyyy HH:mm";
	public static final String FORMAT6 = "yyyy-MM-dd HH:mm:ss.S";
	public static final String FORMAT7 = "yyyy-MM-dd HH:mm";
	
	public static final String FORMAT8 = "MM/YYYY";
	public static final String FORMAT10 = "HH:mm";
	public static final String FORMAT11 = "yyyy-MM-dd";
	public static final String FORMAT12 = "yyyy/MM/dd HH:mm:ss";
	public static final String FORMAT13	= "MM/dd/yyyy HH:mm:ss";
	public static final String FORMAT14 = "yyyyMMdd";
	public static final String FORMAT15 = "yyyyMMddHHmmss";
	
	public static java.sql.Timestamp getSqlSysTimestamp() {
		return new java.sql.Timestamp(System.currentTimeMillis());
	}

	/*
	 * public static java.sql.Date getSqlSysdate() { Calendar cal = Calendar.getInstance();
	 * cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0);
	 * cal.set(Calendar.MILLISECOND, 0); return new java.sql.Date(cal.getTime().getTime()); }
	 */
	/**
	 * Returns SQL date based on String date and format passed
	 */
	public static java.sql.Date stringToSqlDate(String p_string, String p_format) {

		Date date = null;
		SimpleDateFormat df = new SimpleDateFormat(p_format);
		if (StringUtils.isBlank(p_string)) {
			return null;
		}
		try {
			df.setLenient(false);
			date = df.parse(p_string);
		} catch (ParseException pe) {
			log.info("Exception :" + pe);
		}

		if (date == null)
			return null;
		else
			return (new java.sql.Date(date.getTime()));

	}
	
	public static String format_datetime(){
		Calendar c = Calendar.getInstance();
		DateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
		String date = f.format(c.getTime());
		log.info(date);
		return date;
	}
	
	public static String getCurrentDateStr(String format){
		Calendar c = Calendar.getInstance();
		DateFormat f = new SimpleDateFormat(format);
		String date = f.format(c.getTime());
		log.info(date);
		return date;
		
	}
	
	public static Date getCurrentDateInstance(){
		Calendar c = Calendar.getInstance();
		return c.getTime();
	}
	
	public static Date getStrToDate(String strDate,String format){
		SimpleDateFormat df = new SimpleDateFormat(format);

		try {
			df.setLenient(false);
			return df.parse(strDate);
		} catch (ParseException e) {
			log.error("getStrToDate ParseException:", e);
			return null;
		}
	}
	
	public static String toSqlDate(String strJavaScriptDateTime) {
	    // converts a date variable from Javascript format(MM/dd/yyyy) to MySQL format(yyyy-MM-dd)
	    DateFormat javaScriptDateFormat = new SimpleDateFormat("MM/dd/yyyy");
	    DateFormat sqlDateFormat = new SimpleDateFormat("yyyy-MM-dd");
	    Date dt = new Date();
	    try {
	      dt = javaScriptDateFormat.parse(strJavaScriptDateTime);
	    } catch (Exception ex) {
	    }
	    return sqlDateFormat.format(dt);
	  }
	
	
	
	public static String format_datetime_file(){
			return format_datetime().replaceAll(GlobalVariables.COLON, StringUtils.EMPTY).replaceAll(GlobalVariables.HYPHEN, StringUtils.EMPTY).replace(" ", StringUtils.EMPTY);
	}

	public static String getTimeFromSQLDateString(String date) {
		DateFormat df = new SimpleDateFormat(FORMAT6);
		Date strDate;
		try {
			strDate = df.parse(date);
			DateFormat df1 = new SimpleDateFormat("HH:mm");
		    return df1.format(strDate).toString();

		} catch (ParseException e) {
			e.printStackTrace();
			log.error("getTimeFromSQLDate: ParseException:=>", e);
		}
		
		return "";
	}

  public static String toSqlDateAndTime(String strJavaScriptDateTime) {
	    // converts a date-time variable from Javascript format(MM/dd/yyyy HH:mm:ss) to MySQL format(yyyy-MM-dd
	    // HH:mm:ss)
	    DateFormat javaScriptDateFormat = new SimpleDateFormat(FORMAT4);
	    DateFormat sqlDateFormat = new SimpleDateFormat("yyyy-MM-dd");
	    Date dt = new Date();
	    String strDate = strJavaScriptDateTime.substring(0, strJavaScriptDateTime.indexOf(" "));
	    String strTime = strJavaScriptDateTime.substring(strJavaScriptDateTime.indexOf(" ") + 1,
	        strJavaScriptDateTime.length());
	    try {
	      dt = javaScriptDateFormat.parse(strDate);
	    } catch (Exception ex) {
	    }
	    return sqlDateFormat.format(dt) + " " + strTime;
  }

  	public static String getStringFromSQLDateString(String date, String format) {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
		Date strDate;
		try {
			if(StringUtils.isBlank(date)) {
				return "";		
			}
			
			strDate = df.parse(date);
			DateFormat df1 = new SimpleDateFormat(format);
		    return df1.format(strDate).toString();

		} catch (ParseException e) {
			log.error("getTimeFromSQLDate: ParseException:=>", e);
		}
		
		return "";
	}
  	
  	public static boolean dateDiff(String date,String format,Long longerThen)
	{//calculate difference between two dates
								
		Date   datDate   = getStrToDate(date,format);
		Date   nowDate   = getCurrentDateInstance();
		
		Long datediff = ((datDate.getTime()-nowDate.getTime())/(24*60*60*1000));
		if(datediff>longerThen)
			return true;
		
		return false;										
	}	
  	
  	public static boolean isBeyondMonth(String strDate,String format){
  		
  		SimpleDateFormat formater = new SimpleDateFormat(format);
  		try {
			Date fhwa_date = formater.parse(strDate);
			GregorianCalendar c1 = new GregorianCalendar();
			GregorianCalendar c2 = new GregorianCalendar(); 
			c1.setTime(fhwa_date);
			Date   nowDate   = getCurrentDateInstance();
			c2.setTime(nowDate);
			
			int monthCount = -1;
			while(c1.before(c2)){
				c1.add(Calendar.MONTH,1);
				monthCount++;
			}
			if(monthCount == -1){
				return true;
			}else{
				return false;
			}
			
		} catch (ParseException e) {
			log.error("isBeyondMonth: ParseException:=>", e);
		}
  		return false;
  	}
  	
  	
  	public static boolean isPastDate(String strDate){
  	    boolean dateInThePast = false;
  		SimpleDateFormat myFormat = new SimpleDateFormat(FORMAT11);
  		Date dt;
		try {
			myFormat.setLenient(false);
			dt = myFormat.parse(strDate);
			if(!dt.after(new Date())){
	  			dateInThePast =  true;
	  		}
		} catch (ParseException e) {
			log.error("isPastDate: ParseException:=>", e);
		}
  		
  		return dateInThePast;
  		
  	}
  	
  	public static String validateCurrDateTime(String strEffDate, String strEffTime, String strCurDBdateTime) {
  	    String relation = null;
  	    String datetime = null;
  	    datetime = strEffDate + " " + strEffTime;
  	    try {
  	      DateFormat df = new SimpleDateFormat("MM/dd/yyyy hh:mm");
  	      // Get Date 1
  	      Date d1 = df.parse(datetime);
  	      Date d2 = df.parse(strCurDBdateTime);

  	      if (d1.equals(d2)) {
  	        relation = "same";
  	      } else if (d1.before(d2)) {
  	        relation = "before";
  	      } else {
  	        relation = "after";
  	      }

  	    } catch (ParseException e) {
  	    	log.error("validateCurrDateTime: ParseException:=>", e);
  	    }
  	    return relation;
  	  }

  	public static String getCurrentTime() {
	    DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
    	Date date = new Date();
	    return dateFormat.format(date).toString();
	}
  	
  	public static String mYtoYmd(String date){
  		String[] s = date.split("/");
		return s[1]+"-"+s[0]+"-01";
  	}
  	
  	
	public static String mdytomY(String date){//mdy
		String[] s = date.split("/");
		return s[0]+"/"+s[2];
  	}
  	 public static String toJavaScriptDate(String strSqlDate) {
  	    // converts a date variable from MySql format to Javascript format
  	    if (strSqlDate == null) {
  	      return "";
  	    }
  	    DateFormat javaScriptDateFormat = new SimpleDateFormat("MM/dd/yyyy");
  	    DateFormat sqlDateFormat = new SimpleDateFormat("yyyy-MM-dd");
  	    Date dt = new Date();
  	    try {
  	      dt = sqlDateFormat.parse(strSqlDate);
  	    } catch (Exception ex) {
  	    }

  	    return javaScriptDateFormat.format(dt);
  	  }
  	 
  	public static String getTimeOnlyFromSql(String date,String format) {
  		SimpleDateFormat formate = new SimpleDateFormat(format);
  		Date dt = null;
		try {
			dt = formate.parse(date);
		} catch (ParseException e) {
			log.error("getTimeOnlyFromSql: ParseException:=>", e);
		}
  		
  		SimpleDateFormat hm = new SimpleDateFormat("HH:mm");
  		return hm.format(dt);
  		
  	 }

	public static boolean dateDiff(String date, String time, String effectiveDate) {
		
		boolean return_value = false;
		
		String strDate1 = date + GlobalVariables.SPACE + time;						
		String strDate2 = effectiveDate;
	
		Date   datDate1   = getStrToDate(strDate1, DateTimeFormater.FORMAT5);
		Date   datDate2   = getStrToDate(strDate2, DateTimeFormater.FORMAT7);
		
		long datediff = ((datDate1.getTime()-datDate2.getTime())/(24*60*60*1000));
		if(datediff>0)
			return_value = true;
		
		return return_value;										
	}

	public static boolean date_Diff(String date, Integer advance, Integer arrear) {
		
		boolean return_value = false;
		
		String strDate1 = date;						
		Date datDate1   = getStrToDate(strDate1, DateTimeFormater.FORMAT4);
		Date datDate2   = getCurrentDateInstance();
		
		long datediff = ((datDate1.getTime() - datDate2.getTime())/(24*60*60*1000));
		if((datediff < arrear)||(datediff > advance))
			return_value = true;
		
		return return_value;										
	}

 	 public static String formatFromOneToOther(String origin, String originFormat, String destinationFormat) {
   	    if (null == origin || null == originFormat || null == destinationFormat) {
   	      return null;
   	    }
   	    DateFormat originDateFormat = new SimpleDateFormat(originFormat);
   	    DateFormat destinationDateFormat = new SimpleDateFormat(destinationFormat);
   	    Date dt = new Date();
   	    try {
   	      dt = originDateFormat.parse(origin);
   	    } catch (Exception ex) {
   	    	return null;
   	    }

   	    return destinationDateFormat.format(dt);
   	  }
 	 
 	 
 	public static String getTodaysDate() {
		  String months[] = {
			      "January", "February", "March", "April",
			      "May", "June", "July", "August",
			      "September", "October", "November", "December"};
		  
		 GregorianCalendar gcalendar = new GregorianCalendar();
		 String date = months[gcalendar.get(Calendar.MONTH)] + " "+ gcalendar.get(Calendar.DATE) + ", "+  gcalendar.get(Calendar.YEAR);
		 return date;
	}

 	public static String formatSqlDate(java.sql.Date sqlDate,String d_format)
 	{
 		  SimpleDateFormat formatter = new SimpleDateFormat(d_format);
 		  String formattedDate = "";
 		  
 		  if(sqlDate != null)
 		  {
 			  formattedDate = formatter.format(sqlDate);
 		  }
 		  return formattedDate;
 	}

    public static Timestamp stringToTimestamp(String p_date)
 	{   java.util.Date date = null;
 	   try
 		{
 			//String abc = "12/12/1990 13:55:55";			

 		   	SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

 			 date = df.parse(p_date);
 	 
 		}
 		catch(Exception e)
 		{
 			log.debug(e);
 		}
         return new java.sql.Timestamp(date.getTime());
 	}

    public static String formatSqlDate(Timestamp sqlDate,String d_format)
    {
  	  SimpleDateFormat formatter = new SimpleDateFormat(d_format);
  	  String formattedDate = "";
  	  
  	  if(sqlDate != null)
  	  {
  		  formattedDate = formatter.format(sqlDate);
  	  }
  	 
  	  
  	  return formattedDate;
  	  
    }

    public static java.sql.Date getSqlSysdate() {
    		Calendar cal = Calendar.getInstance();
  		cal.set(Calendar.HOUR_OF_DAY,0);
  		cal.set(Calendar.MINUTE,0);
  		cal.set(Calendar.SECOND,0);
  		cal.set(Calendar.MILLISECOND,0);
  		
  		return new java.sql.Date(cal.getTime().getTime());		
    }

	public static java.sql.Date getSystemDate() {
		Date sysDate = new Date();
		return new java.sql.Date(sysDate.getTime()); 
	}

	
}
