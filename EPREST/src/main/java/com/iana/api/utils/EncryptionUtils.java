package com.iana.api.utils;

import java.io.ByteArrayOutputStream;
import java.security.Key;
import java.util.StringTokenizer;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EncryptionUtils {
	
	static Logger log = LogManager.getLogger(EncryptionUtils.class);
	
    private static final String KEY_STRING = "84-131-205-146-148-88-208-206";

	/** 
	 * Method to encrypt a text, uses 2 way encrption (symmetric encryption) 
	 * @param  text String to be encrypted 
	 * @return 		Encrypted string    
	 * 
	 */
	public static String encrypt(String text) throws Exception
	{	
		//log.info("Entered method: text[********]" );
        // Get our secret key
		Key key = getKey();
		log.debug("Got the key ");
		try {
			//Get the cipher for DES algorithm
			Cipher desCipher = Cipher.getInstance("DES");
			//Initialise Cipher for encrypting
			desCipher.init(Cipher.ENCRYPT_MODE,key);
			log.debug("Initialised Cipher for encrypting");
			byte[] cleartext = text.getBytes();
			//Encrypt !! 
			byte[] ciphertext = desCipher.doFinal(cleartext);
			//log.info("Returning encrypted String: [" + getString(ciphertext) + "]");
			//Return encrypted data as String
			return getString( ciphertext);
		} catch (Exception e) {
			log.error("Caught Exception while encrypting: " + e.getMessage());
			//throw new SystemException(e);
			throw new Exception(e);
		}	
		
	}

	
	/** 
	 * Method to decrypt text, uses 2 way decryption (symmetric encryption) 
	 * @param  text String to be decrypted 
	 * @return 		Dcrypted string    
	 * 
	 */
	public static String decrypt( String source ) throws Exception
	{
	    log.info("Entered method: source[" + source + "]" );
	    // Get our secret key
	    Key key = getKey();
		log.debug("Got the key ");
		try
		{
		  //Get the cipher for DES algorithm
	      Cipher desCipher = Cipher.getInstance("DES");
		  //Initialise Cipher for decrypting
	      desCipher.init(Cipher.DECRYPT_MODE, key);
		  log.debug("Initialised Cipher for decrypting");
	      //Convert string to bytes
	      byte[] ciphertext = getBytes( source );
	      // Decrypt !! 
	      byte[] cleartext = desCipher.doFinal(ciphertext);
	      // Return the clear text
		  log.info("Returning decrypted String: [**************]");
	      return new String( cleartext );
	    }
	    catch( Exception e )
	    {
			log.error("Caught Exception while decrypting: " + e.getMessage());
			//throw new SystemException(e);
			throw new Exception(e);
	    }
	    
	  }

	/** 
	 * Method to OUR secret key for 2 way decryption (symmetric encryption) 
	 * @return 		secret key    
	 * 
	 */	
	private static Key getKey()
	  {
	    try
	    {
	      byte[] bytes = getBytes( KEY_STRING );
	      DESKeySpec pass = new DESKeySpec( bytes ); 
	      SecretKeyFactory skf = SecretKeyFactory.getInstance("DES"); 
	      SecretKey s = skf.generateSecret(pass); 
	      return s;
	    }
	    catch( Exception e )
	    {
	      e.printStackTrace();
	    }
	    return null;
	  }

	private static byte[] getBytes( String str )
	{
	   ByteArrayOutputStream bos = new ByteArrayOutputStream();
	   StringTokenizer st = new StringTokenizer( str, "-", false );
	   while( st.hasMoreTokens() )
	   {
	     int i = Integer.parseInt( st.nextToken() );
	     bos.write( ( byte )i );
	   }
	   return bos.toByteArray();
	}

	private static String getString( byte[] bytes )
	{
	   StringBuilder sb = new StringBuilder();
	   for( int i=0; i<bytes.length; i++ )
	   {
	     byte b = bytes[ i ];
	     sb.append( ( int )( 0x00FF & b ) );
	     if( i+1 <bytes.length )
	     {
	       sb.append( "-" );
	     }
	   }
	  return sb.toString();
	 }
	
	public static String decryptCreditCardNo(String encryptedCreditNo)throws Exception
	{
		log.info("Entering method decryptCreditCardNo("+encryptedCreditNo+") of class EncryptionUtils");
		String sCreditCardNo = "";
		StringBuilder sbtemp = new StringBuilder("");
		if(!encryptedCreditNo.equals(""))
		{
			sCreditCardNo = decrypt(encryptedCreditNo);
			String sLstFourDigits = sCreditCardNo.substring(sCreditCardNo.length()-4,sCreditCardNo.length());
			
			for(int i=0;i<12;i++)
			{
				sbtemp.append("X");
			}
			
			sbtemp.append(sLstFourDigits);
		}
		
		
		log.info("Exiting method decryptCreditCardNo of class EncryptionUtils");
		return sbtemp.toString();
	}

}
