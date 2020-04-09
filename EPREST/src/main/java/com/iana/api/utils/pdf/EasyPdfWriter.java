/**
 * EasyPdfWriter
 * 
 */
package com.iana.api.utils.pdf;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.usermodel.CharacterRun;
import org.apache.poi.hwpf.usermodel.Range;
import org.apache.poi.hwpf.usermodel.Section;

import com.iana.api.utils.GlobalVariables;
import com.lowagie.text.Anchor;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.AcroFields;
import com.lowagie.text.pdf.PRAcroForm;
import com.lowagie.text.pdf.PdfCopy;
import com.lowagie.text.pdf.PdfDictionary;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;
import com.lowagie.text.pdf.PdfWriter;

/**
 * Provides a wrapper around iText. Use static methods to convert MS files to PDF.<br>
 * While using any of the Static methods, no other method calls are required. <br>
 * 
 * For example, <br>
 * 		<code><b>EasyPdfWriter.convertExcelToPdf(String ,String ,boolean);</b></code><br>
 * works on its own to create the pdf file. <br><br>
 * 
 * Create an EasyPdfWriter object if you need to create a new pdf file from scratch. <br>
 * Avoid calling static methods from EasyPdfWriter object. It will <b>result in loss of values</b> set via setter methods.
 * <br><br>
 * 
 * @author Ravi Bhatt(163214)
 * 
 */
public class EasyPdfWriter extends PdfPageEventHelper  {
	static Logger log = LogManager.getLogger(EasyPdfWriter.class);
	private String fileToWrite="";
	private double pageHeight=PageSize.A4.getHeight()/72.0;
	private double pageWidth=PageSize.A4.getWidth()/72.0;
	private Rectangle pageSize = null;
	private Document document = null;
	private Color pageBackColor=null;
	private boolean makeLandscape = false;
	@SuppressWarnings("unused")
	private static String  headerText = "";
	@SuppressWarnings("unused")
	private static boolean addheader = false;
	@SuppressWarnings("unused")
	private static String letterHead=GlobalVariables.NOTFN_LETTERHEAD_UIIA;
	
	/**
	 * Default constructor. 
	 * This does not create or open any pdf file. See setter methods.
	 * If used output file name must be set by using setFileToWrite. Once set any of Create methods must be called.
	 * Method Write can also be used to add content, but Write should be called strictly after calling create method. 
	 * See setter methods to control page size, page color etc.
	 */
	public EasyPdfWriter(){}
	/**
	 * Create a pdf document. Creates a document with default page size(A4) and page color. Page format is portrait.
	 * See setter methods to override values. See also: addNewPage, Write methods.
	 */
	public EasyPdfWriter(String fileToWrite)
	{
		pageHeight=PageSize.A4.getHeight()/72.0;
		pageWidth=PageSize.A4.getWidth()/72.0;
		pageBackColor = new Color(255,255,255);
		this.fileToWrite = fileToWrite;
		makeLandscape = false;
		create();
	}
	/**
	 * create a pdf document with specified page height and width.(in inches)
	 * See setter methods to override values. See also: addNewPage, Write methods.
	 * @param fileToWrite
	 * @param pageHeight in inches
	 * @param pageWidth in inches
	 */
	public EasyPdfWriter(String fileToWrite,double pageHeight,double pageWidth)
	{
		this.fileToWrite = fileToWrite;
		this.pageHeight = pageHeight;
		this.pageWidth = pageWidth;
		makeLandscape = false;
		create();
	}
	/**
	 * 
	 * @return returns the pdf file name to be created.
	 */
	public String getFileToWrite()
	{
		return fileToWrite;
	}
	/**
	 * set file name of the pdf to be created.
	 * @param fileToWrite name of pdf file to create.
	 */
	public void setFileToWrite(String fileToWrite)
	{
		this.fileToWrite = fileToWrite;
	}
	/**
	 * 
	 * @return returns page height in inches.
	 */
	public double getPageHeight()
	{
		return pageHeight;
	}
	/**
	 * set the page height of the document in inches.
	 * @param pageHeight height in inches.
	 */
	public void setPageHeight(double pageHeight)
	{
		this.pageHeight = pageHeight;
	}
	/**
	 * 
	 * @return returns page width in inches.
	 */
	public double getPageWidth()
	{
		return pageWidth;
	}
	/**
	 * set the page width of the document in inches.
	 * @param pageWidth pageWidth in inches.
	 */
	public void setPageWidth(double pageWidth)
	{
		this.pageWidth = pageWidth;
	}
	@SuppressWarnings("unused")
	private Rectangle getPageSize()
	{
		return pageSize;
	}
	@SuppressWarnings("unused")
	private void setPageSize(Rectangle pageSize)
	{
		this.pageSize=pageSize;
	}
	/**
	 * 
	 * @return returns the page color for the document.
	 */
	public Color getPageBackColor()
	{
		return pageBackColor;
	}
	/**
	 * set the background page color for the document.
	 * @param pageBackColor
	 */
	public void setPageBackColor(Color pageBackColor)
	{
		this.pageBackColor = pageBackColor;
	}
	/**
	 * Creates a pdf file.Adds nothing to the file.
	 * See methods write, addpage for adding content. Use setter methods before calling this.
	 * Method setFileToWrite or any non default constructor must have been called before a call to this method.
	 */
	public void create()
	{
		//convert the height and width into points from inches.
		pageSize = new Rectangle((float)(pageWidth*72.0),(float)(pageHeight*72.0));
		pageSize.setBackgroundColor(pageBackColor);
				
		if(makeLandscape)
			pageSize=pageSize.rotate();
		
		document = new Document(pageSize);
		
		try
		{
			@SuppressWarnings("unused")
			PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(fileToWrite));
			//writer.setPageEvent(new EasyPdfWriter());
			document.open();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
	}
	/**
	 * Creates a Pdf.
	 * Method setFileToWrite or any non default constructor must have been called before a call to this method.
	 * @param data String to write into the pdf file.
	 */
	public void create(String data)
	{
		if(document==null)	
			create();
		write(data);
	}
	/**
	 * Creates a Pdf.
	 * Method setFileToWrite or any non default constructor must have been called before a call to this method.
	 * @param data Array of ArrayList representing tabular data.
	 * @param makeTabular new pdf contains data in a table, if set to true.
	 */
	public void create(ArrayList<?> data[],boolean makeTabular)
	{
		if(makeTabular)
			create(data);
		else
		{
			try
			{
				if(document==null)
					create();				
				for(int i=0;i<data.length;i++)
				{
					ListIterator<?> l = data[i].listIterator();
					
					String s= new String();
					
					while(l.hasNext())
						s= s + " " + (String)l.next();
					
					document.add(new Paragraph(s));
				}	
				
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		
	}
	/**
	 * Creates a pdf containing a table.
	 * @param data Array of ArrayList representing tabular data.
	 */
	private void create(ArrayList<?> data[])
	{
		try
		{
			if(document==null)
				create();
			int numCols=0;
			for(int i=0;i<data.length;i++)
			{
				if(data[i].size()>numCols)
					numCols=data[i].size();
			}
			
			PdfPTable table = new PdfPTable(numCols);

			for(int i=0;i<data.length;i++)
			{
				ListIterator<?> l = data[i].listIterator();
				int j=0;
				PdfPCell c;
				while(l.hasNext())
				{
					 String s= (String)l.next();
					 
					 StringTokenizer tk = new StringTokenizer(s,":");
					 int mergeCount=0;
					 String celltext;
					 ///increase the colspan for merged cells.
					 if(tk.countTokens()>1)
					 {
						 celltext = (String)tk.nextToken();
						 mergeCount=Integer.parseInt(tk.nextToken());
						 c= new PdfPCell(new Paragraph(celltext));
						 c.setColspan(mergeCount);
						 c.setBorder(0);
						 table.addCell(c);
						 j+=mergeCount;
					 }
					 else
					 {
						 c= new PdfPCell(new Paragraph(s));
						 c.setBorder(0);
						 table.addCell(c);
						 j++;
					 }
				}	
				
				while(j++<numCols)
				{
					 c= new PdfPCell(new Paragraph(" "));
					 c.setBorder(0);
					table.addCell(c);
				}

			}
			table.setWidthPercentage(100);
			document.add(table);
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	/**
	 * Close the pdf document.
	 *
	 */
	public void close()
	{
		document.close();
	}
	/**
	 * Adds a new page in the pdf file.
	 *
	 */
	public void addNewPage()
	{
		try
		{
			document.newPage();
		}
		catch(Exception de)
		{
			de.printStackTrace();
		}
	}
	/**
	 * Check whether document has landscape format.
	 * @return returns True if document has landscape format.
	 */
	public boolean isLandscape()
	{
		return makeLandscape;
	}
	/**
	 * make a document in landscape format. Must be called before call to a non default constructor or any of the create methods.
	 * @param makeLandscape set true to make the document landscape.
	 */
	public void makeLandscape(boolean makeLandscape)
	{
		this.makeLandscape = makeLandscape;
	}
	/**
	 * Write string data to current page.
	 * @param data String data
	 */
	public void write(String data)
	{
		write(data,ALIGN_LEFT);
	}
	/**
	 * Write string data with alignment to current page. 
	 * @param data String data
	 * @param alignment Alignment for this piece of text. See EasyPdfWriter's static members.
	 */
	public void write(String data,int alignment)
	{
		try
		{
			Paragraph p = new Paragraph(data);
			p.setAlignment(alignment);
			document.add(p);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	
	//static fields
	public static final int ALIGN_LEFT=Element.ALIGN_LEFT;
	public static final int ALIGN_RIGHT=Element.ALIGN_RIGHT;
	public static final int ALIGN_CENTER=Element.ALIGN_CENTER;
	public static final int ALIGN_JUSTIFIED=Element.ALIGN_JUSTIFIED;
	
	/////Static Methods Start
	///consider addign parameter for Landscape/Portrait
	/**
	 * Fills data into an existing pdf form.
	 * @param fileToRead The Pdf file to be filled.
	 * @param fileToWrite The Pdf file to be created.
	 * @param formData The data to be filled in, in form order.
	 * @param makeReadOnly Makes the pdf readonly. Removes the form from the pdf.
	 */
	public static void fillPdfForm(String fileToRead,String fileToWrite,String formData[],boolean makeReadOnly)
	{
		try
		{
			PdfReader reader = new PdfReader(fileToRead);
            PdfStamper stamp1 = new PdfStamper(reader, new FileOutputStream(fileToWrite));
            
            AcroFields form2=stamp1.getAcroFields();
            
            int j=0;
            
            for(Iterator<?> i = reader.getAcroForm().getFields().iterator(); i.hasNext();) {
            	PRAcroForm.FieldInformation field = (PRAcroForm.FieldInformation) i.next();
            	form2.setField(field.getName(), formData[j++]);
            	}

            stamp1.setFormFlattening(makeReadOnly);
            
            stamp1.close();
                
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	/**
	 * Fills data into an existing pdf form.
	 * @param fileToRead The Pdf file to be filled.
	 * @param fileToWrite The Pdf file to be created.
	 * @param formData HashMap containing field names and values.
	 */
	public static void fillPdfForm(String fileToRead,String fileToWrite,HashMap<?,?> formData)
	{
		try
		{
			 PdfReader reader = new PdfReader(fileToRead);
			 PdfDictionary acro = (PdfDictionary)PdfReader.getPdfObject(reader.getCatalog().get(PdfName.ACROFORM));
			 //removes the XFA form but also removes any javascript.
			 acro.remove(new PdfName("XFA"));
			 
			 PdfStamper stamp = new PdfStamper(reader, new FileOutputStream(fileToWrite));
			 
			 AcroFields form2=stamp.getAcroFields();
             
			 HashMap<?,?> fields = form2.getFields();
	  
	         Set<?> keys = formData.keySet();
	    
            for(Iterator<?> i =keys.iterator();i.hasNext();)
            {
            	String key = (String)i.next();
            	//System.out.println("key =" + key);
            	if(fields.containsKey(key))
            	{
            		String value = (String)formData.get(key);
            		//System.out.println("value =" + value);
            		if("on".equalsIgnoreCase(value))
            		{
            			//it is a checkbox or radio button.
            			 String[] appearances = form2.getAppearanceStates(key); 
            			 form2.setField(key, appearances[0]); 
            		}
            		else	
            		form2.setField(key,value,value);
            	}
            }
            
            form2.removeField("F[0].P1[0].PrintButton1[0]");
            form2.removeField("F[0].P1[0].EmailSubmitButton1[0]");
            stamp.setFormFlattening(true);

            stamp.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	/**
	 * Converts a text file into a new pdf file. Sets default page size as A4.
	 * @param textFile Text file to convert.
	 * @param newPdfFile Name of the new pdf file with full path.
	 */
	public static void convertTextToPdf(String textFile,String newPdfFile)
	{
		convertTextToPdf(textFile,newPdfFile,(float)(PageSize.A4.getHeight()/72.0),(float)(PageSize.A4.getWidth()/72.0));
	}
	/**
	 * Converts a text file into a new pdf file. 
	 * @param textFile Path for text file.
	 * @param newPdfFile Name of the new pdf file with full path.
	 * @param height height of a page in new pdf in inches.
	 * @param width width of a page in new pdf in inches.
	 */
	public static void convertTextToPdf(String textFile,String newPdfFile,float height,float width)
	{	
		try
		{
			FileReader fr= new FileReader(textFile);
		
			@SuppressWarnings("resource")
			BufferedReader br = new BufferedReader(fr);
		
			String data=new String("");
		
			String fileData = new String("");
		
			while((data = br.readLine())!=null)
			{
			
				StringTokenizer tk = new StringTokenizer(data,"\n");
			
				if(tk.countTokens()==0)
					fileData=fileData+"\n";
				while(tk.hasMoreTokens())
				{
					String s= tk.nextToken();
				
					fileData = fileData + s +"\n";
				}
			}
		
			EasyPdfWriter epw = new EasyPdfWriter();
			epw.setFileToWrite(newPdfFile);
			epw.setPageHeight(height);
			epw.setPageWidth(width);
			epw.create(fileData);
			epw.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
	}
	/**
	 * Converts an excel file to a pdf file. Sets default page size as A4.
	 * @param excelFile Excel file to convert.
	 * @param newPdfFile Name of the new pdf file with full path.
	 * @param makeTabular new pdf contains data in a table, if set to true.
	 */
//	public static void convertExcelToPdf(String excelFile,String newPdfFile,boolean makeTabular)
//	{
//		convertExcelToPdf(excelFile,newPdfFile,(float)(PageSize.A4.getHeight()/72.0),(float)(PageSize.A4.getWidth()/72.0),makeTabular);
//	}
	/**
	 * Converts an excel file to a pdf file.
	 * @param excelFile Excel file to convert.
	 * @param newPdfFile Name of the new pdf file with full path.
	 * @param height height of a page in new pdf in inches.
	 * @param width width of a page in new pdf in inches.
	 * @param makeTabular new pdf contains data in a table, if set to true.
	 */
//	public static void convertExcelToPdf(String excelFile,String newPdfFile,float height,float width,boolean makeTabular)
//	{
//		EasyExcelReaderWriter p = new EasyExcelReaderWriter(excelFile);
//		ArrayList excelData[];
//		if(makeTabular)
//			excelData = p.getExcelData(true);
//		else
//			excelData = p.getExcelData(false);
//		
//		EasyPdfWriter epw = new EasyPdfWriter();
//		epw.setFileToWrite(newPdfFile);
//		epw.setPageHeight(height);
//		epw.setPageWidth(width);
//		epw.create(excelData,makeTabular);
//		epw.close();
//	}

	/**
	 * 
	 * @param wordFile path for the word file to be converted.
	 * @param newPdfFile path for new pdf file.
	 */
	// for non uiia mc
	public synchronized static void convertWordToPdf(String wordFile,String newPdfFile, String notifLetterHead)
	{
		letterHead = notifLetterHead;
		convertWordToPdf(wordFile, newPdfFile,(float)(PageSize.A4.getHeight()/72.0),(float)(PageSize.A4.getWidth()/72.0));
	}
	
	/**
	 * 
	 * @param wordFile path for the word file to be converted.
	 * @param newPdfFile path for new pdf file.
	 */
	public synchronized static void convertWordToPdf(String wordFile,String newPdfFile)
	{
		convertWordToPdf(wordFile, newPdfFile,(float)(PageSize.A4.getHeight()/72.0),(float)(PageSize.A4.getWidth()/72.0));
	}
	
	public synchronized static void convertWordToPdf4FCS(String wordFile,String newPdfFile)
	{
		convertWordToPdf4FCS(wordFile, newPdfFile,(float)(PageSize.A4.getHeight()/72.0),(float)(PageSize.A4.getWidth()/72.0));
	}
	public synchronized static void convertWordToPdf(String wordFile,String newPdfFile,boolean pageHeader, String pageHeaderText)
	{
		addheader = pageHeader;
		headerText = pageHeaderText;
		convertWordToPdf(wordFile, newPdfFile,(float)(PageSize.A4.getHeight()/72.0),(float)(PageSize.A4.getWidth()/72.0));
	}

	public synchronized static void convertWordToPdf4FCS(String wordFile,String newPdfFile,float height,float width)
	{
		//set the page size and background color(default is white).
		Rectangle pageSize = new Rectangle((float)(width*72.0),(float)(height*72.0));
		pageSize.setBackgroundColor(new Color(255,255,255));
		
		Document document = new Document(pageSize);
		
		try {
			PdfWriter writer  =PdfWriter.getInstance(document,new FileOutputStream(newPdfFile));
			//writer.setSpaceCharRatio(PdfWriter.NO_SPACE_CHAR_RATIO);		
			EasyPdfWriter easyPDFwriterObj =new EasyPdfWriter();
			EasyPdfWriter.letterHead="";
			writer.setPageEvent(easyPDFwriterObj);			
			document.open();
			
			try
	        {
	            @SuppressWarnings("resource")
				HWPFDocument doc = new HWPFDocument(new FileInputStream(wordFile));
	            Range r = doc.getRange();

	            for(int x = 0; x < r.numSections(); x++)
	            {
	                Section s = r.getSection(x);
 
	                for(int y = 0; y < s.numParagraphs(); y++)
	                {
	                	org.apache.poi.hwpf.usermodel.Paragraph p = s.getParagraph(y);                         
	                	
	                    com.lowagie.text.Paragraph ph = new com.lowagie.text.Paragraph();//,toPdfFont);
	                    //ph.setKeepTogether(true);
	                    for(int z = 0; z < p.numCharacterRuns(); z++)
	                    {
	                    	                    	
	                        CharacterRun run = p.getCharacterRun(z);                  
	                        Font toPdfFont = new Font();
	                        
	                        String fontName = run.getFontName();
	                        int fontSize = run.getFontSize()/2;
	                        int fontColor = run.getColor();
	                        
	                        toPdfFont.setFamily(fontName);
	                        toPdfFont.setSize(fontSize);
	                        
	                        if(run.isBold())
	                        	toPdfFont.setStyle(Font.BOLD);
	                        if(run.getUnderlineCode()!=0)
	                        	toPdfFont.setStyle(Font.UNDERLINE);
	                        
	                       
	                        Color c;
	                        if(fontColor!=-1)
	                        	c = new Color(fontColor);
	                        else
	                         	c= new Color(0);
	                        
	                        toPdfFont.setColor(c);
	                        
	                        String text = run.text();         
	                        
	                        Chunk ch=null;
	                        
	                        StringBuilder sb = new StringBuilder(run.text());
	                        
	                        if(sb.indexOf("@newpage")!=-1)
	                        {
	                        	//System.out.println("@newpage found....adding new page..");
	                        	document.newPage();
	                        	continue;
	                        }
	                        else if(sb.indexOf("@end")!=-1)
	                        {
	                        	//System.out.println("@end found....ignoring all other runs..");
	                        	//break forcefully
	                        	x = r.numSections();
	                        	y = s.numParagraphs();
	                        	break;
	                        }
	                        else
	                        {
	                        	//System.out.println("in else part..run is:" + text +"*");
	                        	ch = new Chunk(text,toPdfFont);                    	
	                        	ph.setAlignment(p.getJustification());	                        	
	                        	ph.add(ch);	                        	
	                        }
	                    }
	                    	
	                    ph.setLeading(13f);
	                    document.add(ph);	          
	                }
	            }     
	        
	        }
	        catch(Throwable t)
	        {
	        	addheader = false;
	    		headerText = "";
	    		letterHead= GlobalVariables.NOTFN_LETTERHEAD_UIIA;
	        	t.printStackTrace();
	        }
		} catch (DocumentException de) {
			addheader = false;
			headerText = "";
			letterHead= GlobalVariables.NOTFN_LETTERHEAD_UIIA;
			System.err.println(de.getMessage());
		} catch (IOException ioe) {
			addheader = false;
			headerText = "";
			letterHead= GlobalVariables.NOTFN_LETTERHEAD_UIIA;
			System.err.println(ioe.getMessage());
		}
		document.close();
		addheader = false;
		headerText = "";
		letterHead= GlobalVariables.NOTFN_LETTERHEAD_UIIA;
		
	}
	
	public synchronized static void convertWordToPdf(String wordFile,String newPdfFile,boolean pageHeader, String pageHeaderText,String letterHeadStr)
	{
		addheader = pageHeader;
		headerText = pageHeaderText;
		letterHead = letterHeadStr;
		convertWordToPdf(wordFile, newPdfFile,(float)(PageSize.A4.getHeight()/72.0),(float)(PageSize.A4.getWidth()/72.0));
	}
	
	/**
	 * 
	 * @param wordFile path for the word file to be converted.
	 * @param newPdfFile path for new pdf file.
	 * @param height height of a page in new pdf file in inches.
	 * @param width	width of a page in new pdf file in inches.
	 */
	public synchronized static void convertWordToPdf(String wordFile,String newPdfFile,float height,float width)
	{
		//set the page size and background color(default is white).
		Rectangle pageSize = new Rectangle((float)(width*72.0),(float)(height*72.0));
		pageSize.setBackgroundColor(new Color(255,255,255));
		
		Document document = new Document(pageSize);
		
		try {
			PdfWriter writer  =PdfWriter.getInstance(document,new FileOutputStream(newPdfFile));
			//writer.setSpaceCharRatio(PdfWriter.NO_SPACE_CHAR_RATIO);
			writer.setPageEvent(new EasyPdfWriter());
			document.open();
			
			try
	        {
	            @SuppressWarnings("resource")
				HWPFDocument doc = new HWPFDocument(new FileInputStream(wordFile));
	            Range r = doc.getRange();
	            boolean tableExist = false;
	            StringBuilder tableSB = new StringBuilder();
	            for(int x = 0; x < r.numSections(); x++)
	            {
	                Section s = r.getSection(x);
 
	                for(int y = 0; y < s.numParagraphs(); y++)
	                {
	                	org.apache.poi.hwpf.usermodel.Paragraph p = s.getParagraph(y);                         
	                	
	                    com.lowagie.text.Paragraph ph = new com.lowagie.text.Paragraph("");//,toPdfFont);
	                    //ph.setKeepTogether(true);
	                    for(int z = 0; z < p.numCharacterRuns(); z++)
	                    {
	                    	                    	
	                        CharacterRun run = p.getCharacterRun(z);                  
	                        Font toPdfFont = new Font();
	                        
	                        String fontName = run.getFontName();
	                        int fontSize = run.getFontSize()/2;
	                        int fontColor = run.getColor();
	                        
	                        toPdfFont.setFamily(fontName);
	                        toPdfFont.setSize(fontSize);
	                        
	                        if(run.isBold())
	                        	toPdfFont.setStyle(Font.BOLD);
	                        if(run.getUnderlineCode()!=0)
	                        	toPdfFont.setStyle(Font.UNDERLINE);
	                        
	                        
	                        Color c;
	                        if(fontColor!=-1)
	                        	c = new Color(fontColor);
	                        else
	                         	c= new Color(0);
	                        
	                        toPdfFont.setColor(c);
	                        
	                        String text = run.text();         
	                        
	                        Chunk ch=null;
	                        
	                        StringBuilder sb = new StringBuilder(run.text());
	                        
	                        //check if the text contains any hyperlinks.
	                        if(sb.indexOf("HYPERLINK")!=-1)
	                        {
	                        	StringTokenizer wordHyperLink = new StringTokenizer(run.text()," ");
	                        	wordHyperLink.nextToken();
	                        	String uri = wordHyperLink.nextToken();
	                        	
	                        	//uri will be "http://www.uiia.org"
	                        	StringTokenizer hyperLink = new StringTokenizer(uri,":");
	                        	String protocol = hyperLink.nextToken();
	                        	//now remove the " from the protocol name
	                        	protocol = protocol.substring(1);
	                        	
	                        	String location = hyperLink.nextToken();
	                        	//remove // from the begining and " from the last
	                        	location=location.substring(2,location.length()-1);
	                        	
	                        	CharacterRun runUriText = p.getCharacterRun(z+3);  
	                        	String uriText = runUriText.text();
	                        	
	                        	toPdfFont.setStyle(Font.UNDERLINE);
	                        	toPdfFont.setColor(new Color(0,0,255));
	                        	
	                        	Anchor anchor = new Anchor(uriText,toPdfFont);
	                        	anchor.setReference(""+ new URL(protocol,location,80,""));
	                        	anchor.setName(uri);
	                        	
	                        	z=z+3;
	                        	ph.add(anchor);              
	                        }
	                        else if(sb.indexOf("@ts")!=-1)
	                        {
	                        	tableSB.append(sb.toString());
	                        	tableExist = true;
	                        }
	                        else if(tableExist && sb.indexOf("@te") == -1) {
	                        	tableSB.append("\r");
	                        	tableSB.append(sb.toString());
	                        }
	                        else if(sb.indexOf("@te")!=-1)
	                        {
	                        	tableSB.append("\r");
	                        	tableSB.append(sb.toString());
	                        	generateTable(document, writer, fontName,
										fontSize, tableSB);
	                        	tableSB = new StringBuilder();
	                        	tableExist = false;
	                        }
	                        else if(sb.indexOf("@newpage")!=-1)
	                        {
	                        	//System.out.println("@newpage found....adding new page..");
	                        	document.newPage();
	                        	continue;
	                        }
	                        else if(sb.indexOf("@end")!=-1)
	                        {
	                        	//System.out.println("@end found....ignoring all other runs..");
	                        	//break forcefully
	                        	x = r.numSections();
	                        	y = s.numParagraphs();
	                        	break;
	                        }
	                        else
	                        {
	                        	//System.out.println("in else part..run is:" + text +"*");
	                        	ch = new Chunk(text,toPdfFont);
	                        	ph.add(ch);
	                        }
	                    }
	                    ph.setLeading(13f);
	                    document.add(ph);	          
	                }
	            }     
	        
	        }
	        catch(Throwable t)
	        {
	        	addheader = false;
	    		headerText = "";
	    		letterHead= GlobalVariables.NOTFN_LETTERHEAD_UIIA;
	        	t.printStackTrace();
	        }
		} catch (DocumentException de) {
			addheader = false;
			headerText = "";
			letterHead= GlobalVariables.NOTFN_LETTERHEAD_UIIA;
			System.err.println(de.getMessage());
		} catch (IOException ioe) {
			addheader = false;
			headerText = "";
			letterHead= GlobalVariables.NOTFN_LETTERHEAD_UIIA;
			System.err.println(ioe.getMessage());
		}
		document.close();
		addheader = false;
		headerText = "";
		letterHead= GlobalVariables.NOTFN_LETTERHEAD_UIIA;
		
	}
	private static void generateTable(Document document, PdfWriter writer,
			String fontName, int fontSize, StringBuilder sb)
			throws DocumentException {
		//we have got a table of data now.
		//tabular data is enclosed betwen @ts and @te
		
		StringBuilder tableData = new StringBuilder(sb.substring(3,sb.length()-3));
		//System.out.println("In EasyPdfWriter, Table: " );
		//System.out.println(tableData);
		
		//tokenize this table first with "\r", then with "|" to get data for 
		//individual cells.
		
		StringTokenizer rows = new StringTokenizer(tableData.toString(),"\r");
		
		//data will be added to this table.
		PdfPTable table = null;
		PdfPCell cell=null;
		
		int numCols = 0;
		
		//get the first row...that is heading.
		if(rows.hasMoreTokens())
		{
			String firstRow = rows.nextToken();
			
			StringTokenizer headerCells= new StringTokenizer(firstRow,"|");
			//System.out.println("In EasyPdfWriter, No of columns: " + headerCells.countTokens());
			
			numCols = headerCells.countTokens();
			
			int totalCellCount = headerCells.countTokens();
			
			table = new PdfPTable(headerCells.countTokens());
			
			while(headerCells.hasMoreTokens())
			{
				String headerTxt = headerCells.nextToken();
				cell= new PdfPCell(new Paragraph(headerTxt,FontFactory.getFont(fontName,fontSize)));
				cell.setBorder(0);
				if(totalCellCount == 2 && headerCells.countTokens()==0)
				{
					//we have a table with 2 columns, right justify the second column.
					cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
				}
				table.addCell(cell);
			}
		}
		
		while(rows.hasMoreTokens())
		{
			String row = rows.nextToken();
			
			//System.out.println("In EasyPdfWriter, row is: " + row);
			
			StringTokenizer cells = new StringTokenizer(row,"|");
			
			int totalCellCount = cells.countTokens();
			
			boolean noData = false;
			//System.out.println("In EasyPdfWriter, Token count: " + cells.countTokens());
			//incase of no data found or table with one column.
			if(cells.countTokens() == 1)
			{
				cell= new PdfPCell(new Paragraph(cells.nextToken(),FontFactory.getFont(fontName,fontSize)));
				cell.setBorder(0);
				table.addCell(cell);
				noData = true;
				
				//add empty cells..to have entire row.
				for(int i=1;i<numCols;i++)
				{
					cell= new PdfPCell(new Paragraph(""));
					cell.setBorder(0);
					table.addCell(cell);
				}
			}
			
			while(!noData && cells.hasMoreTokens())
			{
				//System.out.println("In EasyPdfWriter, in while for rows..");
				String cellTxt = cells.nextToken();
				cell= new PdfPCell(new Paragraph(cellTxt,FontFactory.getFont(fontName,fontSize)));
				cell.setBorder(0);
				if(totalCellCount == 2 && cells.countTokens()==0)
				{
					//we have a table with 2 columns, right justify the second column.
					cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
				}
				table.addCell(cell);
			}
		}
		table.setWidthPercentage(100);
		table.setSpacingBefore(5f);
		//table.setSpacingAfter(10f);
		//writer.fitsPage(table);
		document.add(table);
		//ph.add(table);
	}
	/**
	 * Merges data from a text file into a pdf file and creates a new pdf file.
	 * @param textFile Path for text file.
	 * @param pdfFile Path for pdf file.
	 * @param newPdf Name of the new pdf file with full path.
	 */
	public static void mergeTextToPdf(String textFile,String pdfFile,String newPdf)
	{
		
		try
		{
			EasyPdfCopier p = new EasyPdfCopier();
			p.loadPdf(pdfFile);
			
			convertTextToPdf(textFile,".\\tmp.pdf",(float)(p.getHeight()/72.0),(float)(p.getWidth()/72.0));
				
			String files[]= new String[2];
			files[0]=pdfFile;
			files[1]=".\\tmp.pdf";
			
			mergeNPdfs(files,newPdf);
			
			//now delete the temporary pdf file.
			File tmp = new File(".\\tmp.pdf");
			tmp.delete();
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	/**
	 * Merges N pdf files and creates a new one.
	 * @param files string array of paths of pdfs to be merged.
	 * @param outFile Name of the new pdf file with full path.
	 */
	public static void mergeNPdfs(String[] files, String outFile)
	{	
		log.info("Entering mergeNpdfs");
		Document document = null;
		PdfCopy writer = null;
		try
		{
			for(int j=0;j<files.length;j++)
			{
				if(files[j]!=null && !files[j].equalsIgnoreCase("")){
				PdfReader reader1 = new PdfReader(files[j]);
				reader1.consolidateNamedDestinations();
				int n = reader1.getNumberOfPages();
			
				if(j==0)
				{
					document = new Document(reader1.getPageSizeWithRotation(1));
					writer = new PdfCopy(document, new FileOutputStream(outFile));
					document.open();
				}
            
				PdfImportedPage page;
            
				for (int i = 0; i < n; ) {
					++i;
					page = writer.getImportedPage(reader1, i);
					writer.addPage(page);
				}
				 writer.freeReader(reader1);}
			}
	
			document.close();
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		log.info("Exiting mergeNpdfs : ");

	}
}
