package com.cancio;

import net.rim.device.api.ui.UiApplication;

public class CardClient extends UiApplication{

	public static void main(String[] args) {
		CardClient cards = new CardClient();
		cards.enterEventDispatcher();
	}
	
	public CardClient(){
		CardModel current = new CardModel();
		current.initCard();
		pushScreen(current);
	}
	

}
