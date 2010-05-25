/*
  The MIT License
  
  Copyright (c) 2009 Krzysztof Langner
  
  Permission is hereby granted, free of charge, to any person obtaining a copy
  of this software and associated documentation files (the "Software"), to deal
  in the Software without restriction, including without limitation the rights
  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  copies of the Software, and to permit persons to whom the Software is
  furnished to do so, subject to the following conditions:
  
  The above copyright notice and this permission notice shall be included in
  all copies or substantial portions of the Software.
  
  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  THE SOFTWARE.
*/
package com.qtitools.player.client.model;

import java.util.Vector;

import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.NodeList;
import com.qtitools.player.client.control.IDocumentLoaded;
import com.qtitools.player.client.control.ItemReference;
import com.qtitools.player.client.control.XMLData;
import com.qtitools.player.client.control.style.StyleLinkDeclaration;
import com.qtitools.player.client.model.feedback.AssessmentFeedbackManager;

public class Assessment{

	/** Array with references to items */
	private Vector<String>   itemRefs;
	/** Array with item titles */
	private Vector<String>  itemTitles;
	/** Variable used to load titles */
	private int             titlesLoadedCounter;
	/** Variable used to load titles */
	private IDocumentLoaded titlesListener;
	/** Whole assessment title */
	private String title;
	/** XML DOM of the assessment */
	private XMLData xmlData;

	public StyleLinkDeclaration styleDeclaration;
	
	private AssessmentFeedbackManager feedbackManager;
	
		
	/**
	 * C'tor
	 * @param data XMLData object as data source
	 */
	public Assessment(XMLData data){
		
		xmlData = data;
		
		Node rootNode = xmlData.getDocument().getElementsByTagName("assessmentTest").item(0);
		
		styleDeclaration = new StyleLinkDeclaration(xmlData.getDocument().getElementsByTagName("styleDeclaration"), data.getBaseURL());
		
		feedbackManager = new AssessmentFeedbackManager(xmlData.getDocument().getElementsByTagName("assessmentFeedback"));
	    
		NodeList nodes = xmlData.getDocument().getElementsByTagName("assessmentItemRef");
		Node itemRefNode;

		itemRefs = new Vector<String>();
    
	    for(int i = 0; i < nodes.getLength(); i++){
	    	itemRefNode = nodes.item(i);
	    	itemRefs.add( xmlData.getBaseURL() + ((Element)itemRefNode).getAttribute("href") );
	    }
	    
	    title = ((Element)rootNode).getAttribute("title");
	    
	}
	
	
	/**
	 * @return number of items in assessment
	 */
	public int getAssessmentItemsCount(){
		return itemRefs.size();
	}

	/**
	 * @return item ref
	 */
	public String getAssessmentItemRef(int index){
		return itemRefs.get(index);
	}

	/** 
	 * @return item title
	 */
	public String getAssessmentItemTitle(int index){
		return itemTitles.get(index);
	}

	/**
	 * @return assessment title
	 */
	public String getTitle(){
			return title;
	}
	
	public Widget getFeedbackView(int percentageScore){
		return feedbackManager.getView(percentageScore);
	}
	
	/**
	 * Load titles from item XML files
	 */
	public void loadTitles(IDocumentLoaded listener){

		// Load only once
		if(itemTitles == null){
			itemTitles = new Vector<String>();

			titlesLoadedCounter = itemRefs.size();
			titlesListener = listener;
			for(int i = 0; i < itemRefs.size(); i++){
				itemTitles.add(itemRefs.get(i));
				new com.qtitools.player.client.util.xml.XMLDocument(itemRefs.get(i), new TitleLoader(i));
				
			}

		}
		else{
			listener.finishedLoading(null, null);
		}
	}
	

	/**
	 * Move item to new position
	 * @param index - old position
	 * @param newPosition - new position
	 */
	public void moveItem(int index, int newPosition){
		String temp;

		// move DOM nodes
		NodeList  nodes = xmlData.getDocument().getElementsByTagName("assessmentItemRef");
		Element   assessmentSection = 
			(Element)xmlData.getDocument().getElementsByTagName("assessmentSection").item(0); 

		// change XML modes
		Node      node = nodes.item(index);
		Node      nodeRef;

		assessmentSection.removeChild(node);
		if(index < newPosition){
			nodeRef = nodes.item(newPosition);
			assessmentSection.insertBefore(node, nodeRef);
		}
		else if(newPosition >= nodes.getLength()){
			assessmentSection.appendChild(node);
		}
		else{
			nodeRef = nodes.item(newPosition);
			assessmentSection.insertBefore(node, nodeRef);
		}

		// move references
		temp = itemRefs.get(index);
		itemRefs.remove(index);
		itemRefs.insertElementAt(temp, newPosition);

		// move titles
		temp = itemTitles.get(index);
		itemTitles.remove(index);
		itemTitles.insertElementAt(temp, newPosition);

	}

  

	/**
	 * Send event when all titles loaded
	 */
	private synchronized void  titleLoaded(Document document, String baseURL){
	
	  titlesLoadedCounter --;
	  if(titlesLoadedCounter == 0){
	    titlesListener.finishedLoading(document, baseURL);
	  }
	}
	
	/**
	/* inner class for loading item title
	 */
	private class TitleLoader implements IDocumentLoaded{

		private int index;

		public TitleLoader(int index){
			this.index = index;
		}

		@Override
		public void finishedLoading(Document document, String baseURL) {
			itemTitles.set( index, (new ItemReference(document)).getTitle() );
			titleLoaded(document, baseURL);
		}

		@Override
		public void loadingErrorHandler(String error) {
						
		}
	};

	
}
