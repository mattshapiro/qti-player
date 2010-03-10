package com.qtitools.player.client.components.htmldnd;

import java.util.Vector;

import org.apache.xalan.templates.ElemNumber;

import com.google.gwt.user.client.ui.Widget;
import com.qtitools.player.client.components.Rectangle;

public class DragSlotManager {

	public DragSlotManager(){

		layout = DragSlotLayout.HORIZONTAL;
		slots = new Vector<Rectangle>(0);
		elements = new Vector<DragElement>(0);
	}

	private DragSlotLayout layout;
	
	private Vector<Rectangle> slots;
	private Vector<DragElement> elements;
	
	public boolean addDragElement(DragElement element){
		elements.add(element);
		addSlot(elements.size()-1);
		boolean result = verify();
		return result;
	}
	
	private Rectangle addSlot(int forElementIndex){
		//int left = findNextSlotLeft(-1);
		//int top = findNextSlotTop(-1);
		//DragSlot slot = new DragSlot(left, top, forWidget.getOffsetWidth(), forWidget.getOffsetHeight());
		Rectangle slot = findNextSlot(forElementIndex);
		slots.add(slot);
		return slot;
	}

	
	public int getCount(){
		return elements.size();
	}

	public Rectangle getSlot(int index){
		return slots.get(index);
	}

	public DragElement getDragElement(int index){
		return elements.get(index);
	}
	
	public void setSlotLayout(DragSlotLayout dsl){
		layout = dsl;
	}

	public void switchDragElements(int fromSlot, int toSlot){
		DragElement tmpElement = elements.get(fromSlot);
		elements.set(fromSlot, elements.get(toSlot));
		elements.set(toSlot, tmpElement);
	}
	
	public DragElement getDragElementByElementIndex(int elementIndex){
		if (elements.size() == 0)
			return null;
		
		for (DragElement currElement: elements){
			if (currElement.getElementIndex() == elementIndex)
				return currElement;
		}
		
		return null;
	}
	
	public int getSlotIndexByElementIndex(int elementIndex){
		if (elements.size() == 0)
			return 0;
		
		for (int i = 0 ; i < elements.size() ; i ++){
			if (elements.get(i).getElementIndex() == elementIndex)
				return i;
		}
		
		return 0;
	}
	
	public boolean updateSlotsForFloatingSlot(Rectangle floating, int slotIndex){
		if (slots.size() == 0)
			return false;
		
		int newSlotIndex = slotIndex;
		
		for (int i = 0 ; i < slots.size() ; i ++){
			
			int floatingBorder;
			int baseBorder = (layout == DragSlotLayout.HORIZONTAL) ? slots.get(i).getMiddleHorizontal() : slots.get(i).getMiddleVertical();
			
			if (i < slotIndex){
				floatingBorder = (layout == DragSlotLayout.HORIZONTAL) ? floating.getLeft() : floating.getTop();
				
				if (floatingBorder < baseBorder){
					newSlotIndex = i;
					break;
				}
									
			} else if (i > slotIndex){
				floatingBorder = (layout == DragSlotLayout.HORIZONTAL) ? floating.getRight() : floating.getBottom();

				if (floatingBorder > baseBorder){
					newSlotIndex = i;
					break;
				}
			}
			
		}
		
		if (slotIndex != newSlotIndex){
			
			int step = (slotIndex < newSlotIndex)? 1 : -1;			
			for (int s = slotIndex ; s != newSlotIndex ; s += step ){
				switchDragElements(s, s + step);
			}
			
			organizeSlots();
			
			return true;
		}
			
		return false;
	}
	
	protected Rectangle findNextSlot(int slotIndex){
		int left = 0;
		int top = 0;
		int width = 0;
		int height = 0;
		
		if (elements.size() > 0){
			
			int currElementIndex = slotIndex;
			if (currElementIndex > elements.size()-1)
				currElementIndex = elements.size()-1;
			
			DragElement currElement = elements.get(currElementIndex);
			width = currElement.getWidget().getOffsetWidth();
			height =  currElement.getWidget().getOffsetHeight();

			int prevSlotIndex = slotIndex-1;
			if (prevSlotIndex > slots.size()-1)
				prevSlotIndex = slots.size()-1;
			
			if (prevSlotIndex >= 0 && prevSlotIndex < slots.size()){
				Rectangle prevSlot = slots.get(prevSlotIndex);
				
				left = prevSlot.getLeft();
				top = prevSlot.getTop();
				if (layout == DragSlotLayout.HORIZONTAL){
					left += prevSlot.getWidth();
				} else if (layout == DragSlotLayout.VERTICAL){
					top += prevSlot.getHeight();
				}
			}
		}
		
		return new Rectangle(left, top, width, height);
	}
	
	private boolean verify(){
		if (slots.size() == elements.size())
			return true;
		
		if (slots.size() > elements.size()){
			int overflow = slots.size() - elements.size();
			while (overflow > 0){
				slots.remove(slots.size()-1);
				overflow--;
			}
		}
		
		if (elements.size() > slots.size()){
			int overflow = elements.size() - slots.size();
			while (overflow > 0){
				slots.remove(elements.size()-1);
				overflow--;
			}
		}
		
		return false;
			
	}
	/*
	protected int findNextSlotLeft(int slotIndex){
		int left = 0;
		
		if (slots.size() > 0){
			
			int prevSlotIndex = slotIndex-1;
			if (prevSlotIndex > slots.size()-1)
				prevSlotIndex = slots.size()-1;

			DragSlot prevSlot = slots.get(slotIndex);
			DragElement prevElement = elements.get(slotIndex);
			
			left = prevSlot.getLeft();
			if (layout == DragSlotLayout.HORIZONTAL){
				left += prevSlot.getWidth();
			}
		}
		return left;
	}
	
	protected int findNextSlotTop(int slotIndex){
		int top = 0;
		
		if (slots.size() > 0){
			
			int prevSlotIndex = slotIndex-1;
			if (prevSlotIndex > slots.size()-1  ||  slotIndex < 0)
				prevSlotIndex = slots.size()-1;
			
			DragSlot prevSlot= slots.get(slotIndex);
			
			top = prevSlot.getTop();
			if (layout == DragSlotLayout.HORIZONTAL){
				top += prevSlot.getHeight();
			}
		}
		return top;
	}
	*/

	private void organizeSlots(){
		for (int i = 0 ; i < slots.size() && i < elements.size() ; i ++){
			
			//DragElement currElement = elements.get(i);
			//DragSlot currSlot = slots.get(i);
			
			Rectangle nextSlot = findNextSlot(i);
			
			slots.set(i, nextSlot);
				
		}
	}
	
	
}