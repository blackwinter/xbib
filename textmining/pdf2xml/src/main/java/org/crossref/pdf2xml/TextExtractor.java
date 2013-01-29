package org.crossref.pdf2xml;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.PDGraphicsState;
import org.apache.pdfbox.util.PDFTextStripper;
import org.apache.pdfbox.util.TextPosition;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.crossref.pdf2xml.data.Page;
import org.crossref.pdf2xml.data.Text;

/**
 * Extract text from a PDF document with position and style information.
 * This class attempts to coalesce runs of text on the page; that is, 
 * 
 * @author Karl Ward
 */
public class TextExtractor extends PDFTextStripper {
	
	private ArrayList<Page> previousPages = new ArrayList<Page>();
	
	private Page currentPage = null;
	
	private int pageCount = 0;
	
	public TextExtractor() throws IOException {
		super();
	}
	
	@Override
	public void processStream(PDPage aPage, PDResources resources,
			COSStream cosStream) throws IOException {
		currentPage = new Page(aPage.findCropBox(), ++pageCount);
		
		super.processStream(aPage, resources, cosStream);
		coalesceRows(currentPage);
		removeDuplicates(currentPage);
		
		previousPages.add(currentPage);
		currentPage = null;
	}

	protected void processTextPosition(TextPosition tp) {
		PDGraphicsState gs = getGraphicsState();
        currentPage.addText(Text.newFor(tp, gs));
	}
	
	private void coalesceRows(Page page) {
		for (Float f : page.getYPosWithText()) {
			List<Text> ts = page.getTextAtY(f);
			
			Collections.sort(ts);
			
			int i=0;
			while (i+1 < ts.size()) {
				Text first = ts.get(i);
				Text snd = ts.get(i+1);
				
				if (first.hasMatchingStyle(snd)) {
					first.addAfter(snd);
					page.removeText(snd);
				} else {
					i++;
				}
			}
		}
	}
	
	// TODO Should handle drop shadow that is positioned on a slightly
	// different x, y.
	private void removeDuplicates(Page page) {
		for (Float f : page.getYPosWithText()) {
			List<Text> ts = page.getTextAtY(f);
			
			Collections.sort(ts);
			
			int i=0;
			while (i+1 < ts.size()) {
				Text first = ts.get(i);
				Text snd = ts.get(i+1);
				
				if (first.getRun().equals(snd.getRun())
						&& first.getX() == snd.getX()) {
					page.removeText(snd);
				} else {
					i++;
				}
			}
		}
	}
	
	public List<Page> getPages() {
	    return previousPages;
	}
	
	public String toString() {
		String s = "";
		for (Page page : previousPages) {
			s += "Page @ " + page.getClipBox().getUpperRightY()
						   + ", " + page.getClipBox().getLowerLeftX();
			for (Text t : page.getText()) {
				s += t.getRun() + " @ " + t.getX() + "," + t.getBaseline() 
							+ " w " + t.getWidth()
							+ " : " + t.getBaseFontName() 
							+ " "   + t.getPointSize() + "pt"
							+ " C " + t.getForegroundColor()
							+ "\n";
			}
		}
		return s;
	}
	
	public String toXml() {
		String r = "";
		
		try {
			DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
			DocumentBuilder b = f.newDocumentBuilder();
			
			Document doc = b.newDocument();
			Element pdf2xml = doc.createElement("pdf2xml");
			doc.appendChild(pdf2xml);
			
			for (Page page : previousPages) {
				Element pageEle = doc.createElement("page");
				PDRectangle cb = page.getClipBox();
				pageEle.setAttribute("width", String.valueOf(cb.getWidth()));
				pageEle.setAttribute("height", String.valueOf(cb.getHeight()));
				pageEle.setAttribute("number", String.valueOf(page.getNumber()));
				pdf2xml.appendChild(pageEle);
				
				for (Text t : page.getText()) {
					Element text = doc.createElement("text");
					text.setAttribute("top", String.valueOf(t.getTop()));
					text.setAttribute("left", String.valueOf(t.getX()));
					text.setAttribute("width", String.valueOf(t.getWidth()));
					text.setAttribute("height", String.valueOf(t.getHeight()));
					text.setAttribute("size", String.valueOf((int) t.getPointSize()));
					text.setAttribute("family", t.getFontFamily());
					text.setAttribute("face", t.getFontFace());
					text.setAttribute("color", t.getForegroundColor());
				
					CDATASection cdata = doc.createCDATASection(t.getRun());
				
					pageEle.appendChild(text);
					text.appendChild(cdata);
				}
			}
			
			Source source = new DOMSource(doc);
			StringWriter sw = new StringWriter();
			Result result = new StreamResult(sw);
			
			Transformer xformer = TransformerFactory.newInstance().newTransformer();
			xformer.setOutputProperty(OutputKeys.INDENT, "yes");
			xformer.setOutputProperty(OutputKeys.STANDALONE, "no");
			xformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			xformer.transform(source, result);
			
			return sw.toString();
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerFactoryConfigurationError e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
        
		return r;
	}
	
	/**
	 * @return Answers an image that contains coloured rectangles representing
	 * the locations of text runs.
	 */
	public BufferedImage toMaskImage(int pageN) {
	    Page p = previousPages.get(pageN - 1);
	    BufferedImage bi = new BufferedImage((int) p.getClipBox().getWidth(), 
	                                         (int) p.getClipBox().getHeight(),
	                                         BufferedImage.TYPE_INT_RGB);
	    Graphics g = bi.getGraphics();
	    g.setColor(Color.WHITE);
	    g.fillRect(0, 0, bi.getWidth(), bi.getHeight());
	    g.setColor(Color.BLACK);
	    for (Text t : p.getText()) {
	        g.fillRect((int) t.getX(), 
	                   (int) t.getTop(), 
	                   (int) t.getWidth(), 
	                   (int) t.getHeight());
	    }
	    
	    return bi;
	}
}

