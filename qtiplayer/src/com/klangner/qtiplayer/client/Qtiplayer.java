package com.klangner.qtiplayer.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Node;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.RootPanel;
import com.klangner.qtiplayer.client.model.Assessment;
import com.klangner.qtiplayer.client.model.AssessmentItem;
import com.klangner.qtiplayer.client.model.IDocumentLoaded;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Qtiplayer implements EntryPoint {

	private Assessment					assessment;
	private PlayerView					playerView;
	private int									currentItemIndex;
	private AssessmentItem			currentItem;
	private ResponseProcessing	responseProcessing;
	
	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {

		Element element = RootPanel.get("player").getElement();
		Node node = element.getFirstChild();
		element.removeChild(node);
		
		loadAssessment(node.getNodeValue());
	}
	
	/**
	 * Create user interface
	 */
	private void initialize() {
		
		playerView = new PlayerView(assessment);
		RootPanel.get("player").add(playerView.getView());
		
		playerView.getCheckButton().addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {
				checkItemScore();
			}
		});

		playerView.getNextButton().addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {
				loadAssessmentItem(currentItemIndex+1);
			}
		});
		
		playerView.getFinishButton().addClickHandler(new ClickHandler(){
			public void onClick(ClickEvent event) {
				showAssessmentResult();
			}
		});


		// Switch to first item
		loadAssessmentItem(0);
	}
	
	/**
	 * create view for assessment item
	 */
	private void showCurrentItem(){
		
		responseProcessing = new ResponseProcessing(currentItem);
		playerView.showAssessmentItem(currentItem);
		
		playerView.getCheckButton().setVisible(true);
		playerView.getNextButton().setVisible(false);
		playerView.getFinishButton().setVisible(false);
		
	}
	
	/**
	 * Load assessment file from server
	 */
	private void loadAssessment(String url){
		
		assessment = new Assessment();
		assessment.load(url, new IDocumentLoaded(){

			public void finishedLoading() {
        initialize();
			}
		});
	}
	
	
	/**
	 * Show assessment item in body part of player
	 * @param index
	 */
	private void loadAssessmentItem(int index){
		String 					url = assessment.getItemRef(index);

		currentItem = new AssessmentItem();
		currentItemIndex = index;

		currentItem.load(url, new IDocumentLoaded(){

			public void finishedLoading() {
				showCurrentItem();
			}
		});

	}
	
	/**
	 * Check score
	 * @param index
	 */
	private void checkItemScore(){

		playerView.getCheckButton().setVisible(false);
		
		if(currentItemIndex+1 < assessment.getItemCount()){
			playerView.getNextButton().setVisible(true);
		}
		else{
			playerView.getFinishButton().setVisible(true);
		}

		playerView.showFeedback(responseProcessing.getFeedback());
	}
	
	/**
	 * Show assessment item in body part of player
	 * @param index
	 */
	private void showAssessmentResult(){
		
		playerView.showResultPage();
	}
	 
}