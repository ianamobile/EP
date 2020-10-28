package com.iana.api.utils;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/*
 * Created on 21-Jan-2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */

/**
 * @author PANDIAH
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
/**  <tr>
*     <td>C001</td>
*     <td>10/11/2004</td>
*     <td>Rimjhim Ray</td>
*     <td>Changes for CR4284</td>
*   </tr>
*/
public class Utility {
	public static final String FORMAT1 = "dd-MMM-yy";
	public static final String FORMAT2 = "yyyyMMdd";
	public static final String FORMAT3 = "dd/MM/yyyy";
	public static final String FORMAT4 = "MM/dd/yyyy";
	public static final String FORMAT5 = "yyyyMMddHHmmss"; // added by piyush
	public static final String FORMAT6 = "MM/dd/yyyy HH:mm:ss"; // added by swati...for system notes timestamp
	public static final String FORMAT7 = "yyyy-MM-dd";
	public static final String FORMAT8 = "MM-dd-yyyy";
	public static final String FORMAT9 = "yyyy-MM-dd HH:mm:ss";
	public static final String FORMAT10 = "MMddHHmm";
	public static final int PASSWORDLENGTH=8;
	public static final char fieldSeperator=',';
	public static final char quoteCharacter='"';
	public static boolean trimFields;
	public static boolean treatConsecutiveSeperatorsAsOne;
	public static final SimpleDateFormat SDF = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public static final String FORMAT15 = "yyyyMMdd";

	public static final String FORMAT_PROOFLIST_INTERNAL = "EEEEE MM/dd/YY";
	
	static Logger log = LogManager.getLogger(Utility.class);
	
	 private static SimpleDateFormat inSDF = new SimpleDateFormat(FORMAT4);
	  private static SimpleDateFormat outSDF = new SimpleDateFormat(FORMAT7);

	  public static DecimalFormat decimalFormat = new DecimalFormat("#,##0.00");
	  public static DecimalFormat integerFormat = new DecimalFormat("#,##,##0");
	  
	  public static String formatDate(String inDate) {
	    String outDate = "";
	    if (inDate != null) {
	        try {
	            Date date = inSDF.parse(inDate);
	            outDate = outSDF.format(date);
	        } catch (ParseException ex){ 
	        }
	    }
	    return outDate;
	  }
	  
	public static final boolean stringToBoolean(String str) 
	 {      
	 	    if (str == null)
	 	    {
	 	       return false;	
	 	    }
	 	    else if (str.equals(""))
	 	    {
	 	    	return false;
	 	    }
			else if (str.equals("0")) 
			{
				return false;
			}
		    str = str.toLowerCase();
			if (str.equals("false")) 
			{
				return false;
			}
			else if (str.equals("no")) 
			{
				return false;
			}
		    else if (str.equals("n")) 
			{
				return false;
			}
			//if it's non false, it's true by definition
			return true;
	 }
  
	public static final String booleanToYN(boolean p_boolean) 
	{
	 	if (p_boolean == true)
	 	{
	 		return "Y";      
	 	}
	 	else return "N";
	}


	public static final String booleanToString(boolean p_boolean) 
	{
		Boolean l_bl_temp = new Boolean(p_boolean);
        return l_bl_temp.toString();		
	}
	 
   public static Date stringToDate(String p_string, String p_format) 
   {
    Date date = null;
   	SimpleDateFormat df = new SimpleDateFormat(p_format);
   	if (p_string == null)
   	{
   	   return null; 
   	}
   	try
   	{
	 date = df.parse(p_string);
   	}
   	catch (ParseException pe)
   	{
   		log.debug("Exception :" + pe);
   	}
   	  	
	return date; 
   }
   /* returns SQL date based on String date and format passed  added by ashok 12/06/06 */
   public static java.sql.Date stringToSqlDate(String p_string, String p_format) 
   {
    Date date = null;
   	SimpleDateFormat df = new SimpleDateFormat(p_format);
   	if (p_string == null || p_string.equals(""))
   	{
   	   return null; 
   	}
   	try
   	{
	 date = df.parse(p_string);
	 /*log.debug("dda3 .. " + p_format);
	 log.debug("dda4 .. " + p_string);
	 log.debug("dda .. " + date);
	 log.debug("dda1 .. " + date.getTime());
	 log.debug("dda2 .. " + new java.sql.Date(date.getTime()));*/
   	}
   	catch (ParseException pe)
   	{
   		log.debug("Exception :" + pe);
   	}
   	
    if (date == null)
    	return null;
    else
    	return (new java.sql.Date(date.getTime())); 
   }
   // This will return date in yyyy/MM/dd HH:mm:ss format
   public static Timestamp stringToTimestampFormat(String p_date)
	{   java.util.Date date = null;
	   try
		{
			//String abc = "12/12/1990 13:55:55";			

		   	SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

			 date = df.parse(p_date);
	 
		}
		catch(Exception e)
		{
			log.debug(e);
		}
       return new java.sql.Timestamp(date.getTime());
	
	}
   //Start of C001
   //This method will return a Timestamp object constructed from given string 
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
	}//end of C001
   // This will return date in dd/MM/yyyy format
	 public static String  dateToString(Date date) 
	 {
		  Calendar c = Calendar.getInstance();
		  if(date == null)
		  {
			  return null;
		  }
		
		  c.setTime(date);		
		  return ""+c.get(Calendar.DAY_OF_MONTH)+"/"+(c.get(Calendar.MONTH)+1)+"/"+c.get(Calendar.YEAR);

	 }

	 // This will return date in dd/MM/yyyy format
	 public static String  dateToStringPay(Date date) 
	 {
		  Calendar c = Calendar.getInstance();
		  if(date == null)
		  {
			  return null;
		  }
		
		  c.setTime(date);		
		  return ""+(c.get(Calendar.MONTH)+1)+"/"+c.get(Calendar.DAY_OF_MONTH)+"/"+c.get(Calendar.YEAR);

	 }
	 
	//Returns String representation of integer 
	public static String intToString(int i)
	{
	   return String.valueOf(i); 
	}

   public static int stringToInt(String p_string) 
   {
    int value = 0;
	if (p_string == null)
	{
	   return value; 
	}
	else if (p_string.equals(""))
	{
		return value;
	}
	
	for(int i = 0; i < p_string.length(); i++)
	{
	  if(!Character.isDigit(p_string.charAt(i)))
 	  {
		return value;
  	   }
    }
	value = Integer.parseInt(p_string);
	return value; 
   }
   
   public static double stringToDouble(String p_string)
   {
   	double value = 0.00;

	if (p_string == null)
	{
	   return value; 
	}
	else if (p_string.equals(""))
	{
		return value;
	}
	
	for(int i = 0; i < p_string.length(); i++)
	{
	  if (!(Character.isDigit(p_string.charAt(i)) ||(p_string.charAt(i) == '.')))
	  {
		return value;
	   }
	}
	value = Double.parseDouble(p_string);
	return value; 
   }//end of stringToDouble
   
   /*added by ashok for converting String to double (The string can be comma separted also)
    * The commas will be removed and then StringtoDouble will be called*/
   public static double commaStringtoDouble(String p_string)
   {
	 double value=0.0;
	 if(p_string==null || p_string.equals(""))
	 {
		 return value;
	 }
	 /*Removing commas if any from the String* */
	 StringBuffer sbTempAmount=new StringBuffer();
	  //Removing commas from the String Amount passed
	  for(int i=0;i<p_string.length();i++)
	  {
		  char chAmt;
		  chAmt=p_string.charAt(i);
		  if(chAmt!=',')
		  {
			  sbTempAmount.append(chAmt);
		  }
	  }
	 //log.debug("String after removing commas:- "+sbTempAmount.toString());
	 /*Calling String to double for String conversion*/
	 value=stringToDouble(sbTempAmount.toString());
	 //log.debug("Double value from String to Double:- "+value);
	 return value;  
   }
   public static String commaStringtoString(String p_string)
   {
	 String value="";
	 if(p_string==null || p_string.equals(""))
	 {
		 return value;
	 }
	 /*Removing commas if any from the String* */
	 StringBuffer sbTempAmount=new StringBuffer();
	  //Removing commas from the String Amount passed
	  for(int i=0;i<p_string.length();i++)
	  {
		  char chAmt;
		  chAmt=p_string.charAt(i);
		  if(chAmt!=',')
		  {
			  sbTempAmount.append(chAmt);
		  }
	  }
	 //log.debug("String after removing commas:- "+sbTempAmount.toString());
	 /*Calling String to double for String conversion*/
	 value=sbTempAmount.toString();
	 //log.debug("Double value from String to Double:- "+value);
	 return value;  
   }
   
   public static String doubleToString(double p_double)
   {
	  return Double.toString(p_double); 
   }//end of doubleToString

   public static Date truncDate(String p_format,java.util.Date p_date)
	{
		SimpleDateFormat sd = new SimpleDateFormat(p_format);
		try
		{
			p_date = sd.parse(sd.format(p_date));
		}
		catch(Exception e)
		{
			log.debug(e);
		}

		return p_date;
	}

    //Add months to the sql.Date	
   public static java.sql.Date addMonths(java.sql.Date p_date, int p_months)
	{
		Calendar cal = Calendar.getInstance();
		cal.setTime(p_date);
		cal.add(Calendar.MONTH,p_months);
		return new java.sql.Date(cal.getTime().getTime());
	}

   //Add days to the sql.Date	
   public static java.sql.Date addDays(java.sql.Date p_date, int p_days)
	{
		Calendar cal = Calendar.getInstance();
		cal.setTime(p_date);
		cal.add(Calendar.DAY_OF_MONTH,p_days);
		return new java.sql.Date(cal.getTime().getTime());
	}

   //Add months to the util.Date	
   public static java.util.Date addMonths(java.util.Date p_date, int p_months)
	{
		Calendar cal = Calendar.getInstance();
		cal.setTime(p_date);
		cal.add(Calendar.MONTH,p_months);
		return new java.util.Date(cal.getTime().getTime());
	}

   //Add days to the sql.Date	
   public static java.util.Date addDays(java.util.Date p_date, int p_days)
	{
		Calendar cal = Calendar.getInstance();
		cal.setTime(p_date);
		cal.add(Calendar.DAY_OF_MONTH,p_days);
		return new java.util.Date(cal.getTime().getTime());
	}

  public static java.util.Date getUtilSysdate()
  {
  		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY,0);
		cal.set(Calendar.MINUTE,0);
		cal.set(Calendar.SECOND,0);
		cal.set(Calendar.MILLISECOND,0);
		return cal.getTime();		
  }

  public static java.util.Date truncUtilDate(java.util.Date p_date)
  {
  		Calendar cal = Calendar.getInstance();
		cal.setTime(p_date);
		cal.set(Calendar.HOUR_OF_DAY,0);
		cal.set(Calendar.MINUTE,0);
		cal.set(Calendar.SECOND,0);
		cal.set(Calendar.MILLISECOND,0);
		return cal.getTime();		
  }

  public static java.sql.Date getSqlSysdate()
  {
  		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY,0);
		cal.set(Calendar.MINUTE,0);
		cal.set(Calendar.SECOND,0);
		cal.set(Calendar.MILLISECOND,0);
		return new java.sql.Date(cal.getTime().getTime());		
  }
 /* To get current SQL timestamp  added by ashok 12/06/06 */
  public static java.sql.Timestamp getSqlSysTimestamp()
  {
  		return new java.sql.Timestamp(System.currentTimeMillis());		
  }
/* To convert the String into Long Amount
 * this fun will check for M .. If M, then to consider Million else
 * for normal amount to remove comma and then return the String as long ; 
 * */
  
  public static long amountConversion(String p_amount)
  {
	  final long MILMULTIPLIER=1000000;
	  long lnFinAmount=0;
	  if(p_amount==null || p_amount.equals(""))
	  {
		  return 0;
	  }
	  p_amount=p_amount.trim();
	  /*Checking whether decimal present or not and if present, then to remove the same*/
	  if(p_amount.indexOf(".")>0)
		{
			String []str=p_amount.split("\\.");
			p_amount=str[0];
		}
	  StringBuffer sbTempAmount=new StringBuffer();
	  //Removing commas from the String Amount passed
	  for(int i=0;i<p_amount.length();i++)
	  {
		  char chAmt;
		  chAmt=p_amount.charAt(i);
		  if(chAmt!=',')
		  {
			  sbTempAmount.append(chAmt);
		  }
	  }
	  //log.debug("String removing commas :- "+sbTempAmount.toString());
	  if(p_amount.endsWith("M"))
	  {
		  //log.debug("In M Part");
		  //log.debug("length of String passed:- "+p_amount.length());
		  p_amount=p_amount.substring(0,p_amount.length()-1);
		  //log.debug("String after removing M:- "+p_amount);
		  Float fltTemp=new Float(p_amount);
		  float f=(fltTemp.floatValue()* MILMULTIPLIER);
		  lnFinAmount=(long)f;
	  }
	  else
	  {
		  //log.debug("In Else Part");
		  //log.debug("length of String passed:- "+p_amount.length());
		  //log.debug("String removing commas :- "+sbTempAmount.toString());
		  Long lnTemp=new Long(sbTempAmount.toString());
		  lnFinAmount=lnTemp.longValue();
	  }
	  
  return lnFinAmount; 
}
	  
public static String convertThouIntoMil(String p_amount)
{
	
	if(p_amount==null || p_amount.equals(""))
	  {
		  return "0M";
	  }
	
	if(p_amount.length()<6)
		return p_amount;
	
	String out="";
	  String sbTempAmount="";
	  //Removing commas from the String Amount passed
	  for(int i=0;i<p_amount.length();i++)
	  {
		  char chAmt;
		  chAmt=p_amount.charAt(i);
		  if(chAmt!=',')
		  {
			  sbTempAmount=sbTempAmount+chAmt;
		  }
	  }
	  java.lang.Float fltTemp=new java.lang.Float(sbTempAmount);
	  float val=fltTemp.floatValue();
	  val = val/1000000;
	  out=String.valueOf(val)+"M";
	return out;
}
  
  
  public static java.sql.Date truncSqlDate(java.sql.Date p_date)
  {
  		Calendar cal = Calendar.getInstance();
		cal.setTime(new java.util.Date(p_date.getTime()));
		cal.set(Calendar.HOUR_OF_DAY,0);
		cal.set(Calendar.MINUTE,0);
		cal.set(Calendar.SECOND,0);
		cal.set(Calendar.MILLISECOND,0);
		return new java.sql.Date(cal.getTime().getTime());		
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
  
	/**
	 * getSystemDate():java.sql.Date gets the current system date 
	 * @return java.sql.Date
	 */
	public static java.sql.Date getSystemDate()
	{
		Date sysDate = new Date();
		java.sql.Date sqlDate = new java.sql.Date(sysDate.getTime()); 
		
		return sqlDate;
	}
	public static java.util.Date stringtoUtilDate(String p_string,String p_format)
	{
		Date date = null;
	   	SimpleDateFormat df = new SimpleDateFormat(p_format);
	   	if (p_string == null || p_string.equals(""))
	   	{
	   	   return null; 
	   	}
	   	try
	   	{
		 date = df.parse(p_string);
		 /*log.debug("dda3 .. " + p_format);
		 log.debug("dda4 .. " + p_string);
		 log.debug("dda .. " + date);
		 log.debug("dda1 .. " + date.getTime());
		 log.debug("dda2 .. " + new java.sql.Date(date.getTime()));*/
	   	}
	   	catch (ParseException pe)
	   	{
	   		log.debug("Exception :" + pe);
	   	}
	   	
	    if (date == null)
	    	return null;
	    else
	    	return (new java.util.Date(date.getTime())); 
	}
	/*--added by ashok*/
	public static long stringtoLong(String p_string)
	{
		long value = 0;
		if (p_string == null)
		{
		   return value; 
		}
		else if (p_string.equals(""))
		{
			return value;
		}
		value=Long.parseLong(p_string);
		return value; 
	}
	/*-added by ashok*/
	public static String getPassword() 
	{
	    char[] pwdString = new char[PASSWORDLENGTH];
	    int tempChar  = 'A';
	    int  rndm = 0;
	    for (int i=0; i <PASSWORDLENGTH; i++)
	    {
	      rndm = (int)(Math.random() * 3);
	      switch(rndm) {
	        case 0: tempChar = '0' +  (int)(Math.random() * 10); break;
	        case 1: tempChar = 'a' +  (int)(Math.random() * 26); break;
	        case 2: tempChar = 'A' +  (int)(Math.random() * 26); break;
	      }
	      pwdString[i] = (char)tempChar;
	    }
	    return new String(pwdString);

	}
	
	public static String addPaddingToNotes(String notes,String userName)
	{
		StringBuffer sbTemp = new StringBuffer();
		
		sbTemp.append("#START#U#").append(userName);
		sbTemp.append("#TS#").append(getSqlSysTimestamp().toString());
		sbTemp.append("#NOTES#").append(notes);
		sbTemp.append("#END");
		
		return sbTemp.toString();
	}
	
	public static String getCurrentDateStr(String format){
		Calendar c = Calendar.getInstance();
		DateFormat f = new SimpleDateFormat(format);
		String date = f.format(c.getTime());
		System.out.println(date);
		return date;
		
	}
	
	
	public static ArrayList removePaddingFrmNotes(String allnotes)
	{
		ArrayList displayLst = new ArrayList();
		if(allnotes.equals("") || !allnotes.endsWith("END"))
		{
			return displayLst;
		}
		String[] prevNotes = allnotes.split("#END");
		int iStart,iTstmpIndex,iNotesIndex;
		
		for(int i=0;i<prevNotes.length;i++)
		{
			StringBuffer sbTemp = new StringBuffer();
			iStart = prevNotes[i].indexOf("#START#U#");
			iTstmpIndex = prevNotes[i].indexOf("#TS#");
			iNotesIndex = prevNotes[i].indexOf("#NOTES#");
						
			sbTemp.append(prevNotes[i].substring(iTstmpIndex+4,iNotesIndex));
			sbTemp.append("-----------------");
			sbTemp.append(prevNotes[i].substring(iStart+9,iTstmpIndex));
			
			displayLst.add(sbTemp.toString());
			displayLst.add(prevNotes[i].substring(iNotesIndex+7));
		}
			
		return displayLst;
	}
	
	
}

