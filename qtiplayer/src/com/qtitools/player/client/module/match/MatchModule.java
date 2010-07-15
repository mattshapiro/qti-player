package com.qtitools.player.client.module.match;

import java.util.Vector;
import com.google.gwt.dom.client.Document;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.NodeList;
import com.qtitools.player.client.model.internalevents.InternalEvent;
import com.qtitools.player.client.model.internalevents.InternalEventTrigger;
import com.qtitools.player.client.model.variables.response.Response;
import com.qtitools.player.client.module.CommonsFactory;
import com.qtitools.player.client.module.IInteractionModule;
import com.qtitools.player.client.module.IModuleEventsListener;
import com.qtitools.player.client.module.IModuleSocket;
import com.qtitools.player.client.module.match.area.MatchContainer;
import com.qtitools.player.client.util.xml.XMLUtils;

public class MatchModule extends Composite implements IInteractionModule {

	/** response id */
	//private String responseIdentifier;
	/** The main panel for the module */
	private VerticalPanel mainPanel;
	/** The main panel for the module */
	private MatchContainer container;
	private String containerId;
	private String responseIdentifier;
	
	private boolean locked = false;
	
	public MatchModule(Element element, IModuleSocket moduleSocket, IModuleEventsListener moduleEventsListener){

		/** Shuffle? */
		boolean shuffle = XMLUtils.getAttributeAsBoolean(element, "shuffle");
		int maxAssociations = XMLUtils.getAttributeAsInt(element, "maxAssociations");

		responseIdentifier = XMLUtils.getAttributeAsString(element, "responseIdentifier");
		Response response = moduleSocket.getResponse(responseIdentifier);
		
		NodeList setNodes = element.getElementsByTagName("simpleMatchSet");
		
		container = new MatchContainer(setNodes, shuffle, maxAssociations, response, moduleEventsListener, this);
		containerId = Document.get().createUniqueId();
		container.getElement().setId(containerId);
		container.setStylePrimaryName("qp-match-container");
		
		mainPanel = new VerticalPanel();
		mainPanel.add(CommonsFactory.getPromptView(XMLUtils.getFirstElementWithTagName(element, "prompt")));
		mainPanel.add(container);
		mainPanel.setStylePrimaryName("qp-match-module");

		initWidget(mainPanel);
	}

	@Override
	public void onOwnerAttached() {
		container.init();

	}

	@Override
	public void markAnswers(boolean mark) {
		if (mark){
			locked = true;
			container.markAnswers();
		} else {
			container.unmark();
			locked = false;
		}

	}

	@Override
	public void reset() {
		container.reset();

	}

	@Override
	public void lock(boolean l) {
		locked = l;
		
	}

	@Override
	public void showCorrectAnswers(boolean show) {
		// TODO Auto-generated method stub

	}

	@Override
	public JSONArray getState() {
		JSONArray newState = new JSONArray();
		
		String currAnswer;
		for (int c = 0 ; c < container.connections.size() ; c ++){
			currAnswer = container.connections.get(c).from + " " + container.connections.get(c).to;
			newState.set(c, new JSONString(currAnswer));
		}
		return newState;
	}

	@Override
	public void setState(JSONArray newState) {
		String answer;
		container.removeAllLines();
		for (int j = 0 ; j < newState.size() ; j ++){
			answer = newState.get(j).isString().stringValue();
			String connectionIdentifiers[] = answer.split(" ");
			if (connectionIdentifiers.length == 2){
				container.addLine(connectionIdentifiers[0], connectionIdentifiers[1]);
			}
		}
		container.updateResponse(false);
	}

	@Override
	public Vector<InternalEventTrigger> getTriggers() {

		Vector<InternalEventTrigger> ids = new Vector<InternalEventTrigger>();

		ids.add(new InternalEventTrigger(container.area.areaCoverId, Event.ONMOUSEDOWN));
		ids.add(new InternalEventTrigger(container.area.areaCoverId, Event.ONMOUSEMOVE));
		ids.add(new InternalEventTrigger(container.area.areaCoverId, Event.ONMOUSEUP));


		ids.add(new InternalEventTrigger(container.area.landId, Event.ONMOUSEDOWN));
		ids.add(new InternalEventTrigger(container.area.landId, Event.ONMOUSEMOVE));
		ids.add(new InternalEventTrigger(container.area.landId, Event.ONMOUSEUP));
		
		ids.add(new InternalEventTrigger(containerId, Event.ONMOUSEOUT));
		
		return ids;
	}

	@Override
	public void handleEvent(String tagID, InternalEvent event) {
		if (locked)
			return;
		
		if (event.getTypeInt() == Event.ONMOUSEDOWN){
			container.startDrag(tagID, event.getClientX(), event.getClientY());
		} else if (event.getTypeInt() == Event.ONMOUSEUP){
			container.endDrag(tagID, event.getClientX(), event.getClientY());
		} else if (event.getTypeInt() == Event.ONMOUSEMOVE){
			container.processDrag(event.getClientX(), event.getClientY());
			event.stopPropagation();
		}  else if (event.getTypeInt() == Event.ONMOUSEOUT){
			container.endDrag("", -1, -1);
			
		}  

	}

	@Override
	public String getIdentifier() {
		return responseIdentifier;
	}
}
