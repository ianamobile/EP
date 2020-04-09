/**
 * 
 */
package com.iana.api.utils.pdf;

import com.lowagie.text.Document;
import com.lowagie.text.pdf.*;
import java.io.FileOutputStream;
import com.lowagie.text.Rectangle;

/**
 * Allows to make duplicate copies of a pdf file.
 * @author Ravi Bhatt (163214)
 *
 */
public class EasyPdfCopier {
	
	private String fileToParse="";
	private int numbeOfPages=0;
	private PdfReader reader=null;
	private Document document = null;
	private PdfCopy  writer = null;
	private Rectangle pageSize=null;
	
	public String getFileToParse()
	{
		return fileToParse;
	}
	
	public void setFileToParse(String fileToParse)
	{
		this.fileToParse = fileToParse;
	}
	
	/**
	 * Get the number of pages in a Pdf file.Use after calling loadPdf.
	 * @return returns the number of pages.
	 */
	public int getNumberOfPages()
	{
		return numbeOfPages;
	}
	/**
	 * 
	 * @return returns the page height points.
	 */	
	public float getHeight()
	{
		return pageSize.getHeight();
	}
	/**
	 * 
	 * @return returns the page width in points.
	 */
	public float getWidth()
	{
		return pageSize.getWidth();
	}
	
	/**
	 * Loads a PDF file. Takes the path of the pdf as an argument.
	 * @param fileToParse pdf file name with path.
	 */
	public void loadPdf(String fileToParse)
	{
		setFileToParse(fileToParse);
		try
		{
			reader = new PdfReader(fileToParse);
			//Replaces all local named links with their actual destinations.
			reader.consolidateNamedDestinations();
			numbeOfPages=reader.getNumberOfPages();
			pageSize = reader.getPageSizeWithRotation(1);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	/**
	 * Creates a duplicate Pdf.
	 * @param newPdfFilePath name of new pdf file.
	 */
	public void createNewPdf(String newPdfFilePath)
	{
		createNewPdf(newPdfFilePath,1,numbeOfPages);
	}
	/**
	 * Create a new pdf from the original but starting from page number startPage.
	 * @param newPdfFilePath name of new pdf file.
	 * @param startPage page number to start coping from.
	 */
	public void createNewPdf(String newPdfFilePath, int startPage)
	{
		createNewPdf(newPdfFilePath,startPage,numbeOfPages);
	}
	/**
	 * Creates a new pdf with only specified pages.
	 * @param newPdfFilePath name of new pdf file.
	 * @param startPage page number to start coping from.
	 * @param endPage page number to end coping at.
	 */
	public void createNewPdf(String newPdfFilePath,int startPage,int endPage)
	{
        try
        {
        	document = new Document(reader.getPageSizeWithRotation(1));
        	writer = new PdfCopy(document, new FileOutputStream(newPdfFilePath));
        	document.open();
        	PdfImportedPage page;
            for (int i = startPage; i < endPage; i++) 
            {
                page = writer.getImportedPage(reader, i);
                writer.addPage(page);
            }
            document.close();
            
        }
        catch(Exception e)
        {
        	e.printStackTrace();
        }
	}
}

