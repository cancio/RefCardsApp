package com.cancio;

import java.io.IOException;
import java.io.InputStream;

import javax.microedition.media.Manager;
import javax.microedition.media.Player;
import javax.microedition.media.control.VolumeControl;

import com.webtrends.mobile.analytics.IllegalWebtrendsParameterValueException;
import com.webtrends.mobile.analytics.rim.WebtrendsDataCollector;

import net.rim.blackberry.api.invoke.Invoke;
import net.rim.blackberry.api.invoke.MessageArguments;
import net.rim.blackberry.api.mail.MessagingException;
import net.rim.blackberry.api.mail.Multipart;
import net.rim.blackberry.api.mail.Message;
import net.rim.blackberry.api.mail.SupportedAttachmentPart;
import net.rim.blackberry.api.mail.TextBodyPart;
import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.io.IOUtilities;
import net.rim.device.api.system.Characters;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;
import net.rim.device.api.ui.decor.Background;
import net.rim.device.api.ui.decor.BackgroundFactory;

final class CardModel extends MainScreen implements LocalDefaultResource{
	private VerticalFieldManager _manager;
	private Background bg;
	private int currentColor;
	private int soundVolume = 100;
	
	private static ResourceBundle _res = ResourceBundle.getBundle(BUNDLE_ID, BUNDLE_NAME);
	
	private MenuItem mute = new MenuItem(_res.getString(MUTE), 98, 1) 
    {
		public void run(){
			soundVolume = 0;
			removeMenuItem(mute);
			addMenuItem(unmute);
		}
    };
	
    private MenuItem unmute = new MenuItem(_res.getString(UNMUTE), 99, 1)
    {
    	public void run(){
    		soundVolume = 100;
    		removeMenuItem(unmute);
    		addMenuItem(mute);
    	}
    };
    
    private MenuItem sendVia = new MenuItem(_res.getString(SENDVIA), 100, 3)
    {
    	public void run(){
			InputStream inputS = null;
			try{
				String dialogMessage = _res.getString(SENDVIA);
				Object[] choices = new Object[] {"Email", "Cancel"};
				Dialog sendViaDialog = new Dialog(dialogMessage, choices, null, 1, null);
				sendViaDialog.doModal();
				
				if(sendViaDialog.getSelectedValue() == 0){
					//Email
					String sendChoice;
	    			if (currentColor == Color.RED){
	    				sendChoice = "/red.jpg";
	    			}
	    			else {
	    				sendChoice = "/yellow.jpg";
	    			}
	    			inputS = getClass().getResourceAsStream(sendChoice);
	    			
	    			byte[] data = IOUtilities.streamToBytes(inputS);
	    			
	    			inputS.close();
	    			
					Multipart multipart = new Multipart();
				
					SupportedAttachmentPart attach = new SupportedAttachmentPart(multipart, "image/jpeg", "refcard.jpg", data);
					multipart.addBodyPart(attach);
					TextBodyPart emailBody = new TextBodyPart(multipart, _res.getString(EMAILBODY));
					multipart.addBodyPart(emailBody);
	    			Message m = new Message();
	    			m.setSubject(_res.getString(EMAILSUBJECT));
					m.setContent(multipart);
					Invoke.invokeApplication(Invoke.APP_TYPE_MESSAGES, new MessageArguments(m));
				}
				else if (sendViaDialog.getSelectedValue() == 1){
					sendViaDialog.cancel();
				}
				
			} catch (MessagingException e) {
				System.out.println("MessagingException");
			} catch (IOException e) {
				System.out.println("IOException");
			} catch (NullPointerException e) {
				System.out.println("NullPointerException");
			}
			try
			{
			    WebtrendsDataCollector.getInstance().onButtonClick("/refcardsapp/sendvia", 
			            "Send via", "sending via", null);
			}
			catch (IllegalWebtrendsParameterValueException err)
			{
			    WebtrendsDataCollector.getLog().e(err.getMessage());
			}
    	}
    };
    
    
    private MenuItem stats = new MenuItem(_res.getString(STATS), 100, 4)
    {
    	public void run(){
    		CardData cardData = new CardData();
    		String stats = _res.getString(RED) + ": " + (String)cardData.get("Red") + "\n" + _res.getString(YELLOW) + ": " + (String)cardData.get("Yellow");
    		Dialog.inform(stats);
    		
    		try
    		{
    		    WebtrendsDataCollector.getInstance().onButtonClick("/refcardsapp/stats", 
    		            "Stats", "stats viewed", null);
    		}
    		catch (IllegalWebtrendsParameterValueException err)
    		{
    		    WebtrendsDataCollector.getLog().e(err.getMessage());
    		}
    	}
    };
    
	public void initCard(){
		_manager = (VerticalFieldManager)getMainManager();
		bg = BackgroundFactory.createSolidBackground(Color.YELLOW);
		_manager.setBackground(bg);
		currentColor = Color.YELLOW;
		updateLayout();
		addMenuItem(mute);
		addMenuItem(sendVia);
		addMenuItem(stats);
		
		try
		{
		    WebtrendsDataCollector.getInstance().onButtonClick("/refcardsapp/initscreen", 
		            "Application inital screen", "application opened", null);
		}
		catch (IllegalWebtrendsParameterValueException err)
		{
		    WebtrendsDataCollector.getLog().e(err.getMessage());
		}
	}
	
	public void changeCard(int color){
		
		CardData cardData = new CardData();
		
		if(color == Color.RED){
			bg = BackgroundFactory.createSolidBackground(Color.YELLOW);
			_manager.setBackground(bg);
			currentColor = Color.YELLOW;
			
			int currentYellowCount;
			
			if ((String)cardData.get("Yellow") == null){
				currentYellowCount = 0;
			}
			else{
				currentYellowCount = Integer.parseInt((String)cardData.get("Yellow"));
			}
		
			currentYellowCount++;
			
			cardData.set("Yellow", Integer.toString(currentYellowCount));
			cardData.commit();
			updateLayout();
		}
		else if(color == Color.YELLOW){
			bg = BackgroundFactory.createSolidBackground(Color.RED);
			_manager.setBackground(bg);
			currentColor = Color.RED;
			int currentRedCount;
			
			if ((String)cardData.get("Red") == null){
				currentRedCount = 0;
			}
			else {
				currentRedCount = Integer.parseInt((String)cardData.get("Red"));
			}
			
			currentRedCount++;
			
			cardData.set("Red", Integer.toString(currentRedCount));
			cardData.commit();
			updateLayout();
		}
	}
	
	public void playSound(){
		try{
			Class cl = Class.forName("com.cancio.CardClient");
			InputStream input = cl.getResourceAsStream("/whistle.mp3");
			Player m_player = Manager.createPlayer(input,"audio/mpeg");
			m_player.realize();
			m_player.prefetch();
			VolumeControl vc;
			vc = (VolumeControl)m_player.getControl("VolumeControl");
			vc.setLevel(soundVolume);
			m_player.start();
		} catch (Exception audioFailException){
			System.out.println("No sound");
		}
	}

	protected boolean navigationUnclick(int status, int time){
		int colorBefore = currentColor;
		playSound();
		changeCard(currentColor);
		
		if (colorBefore != currentColor){
			return true;
		}
		return false;
	}
	
	protected boolean keyChar(char key, int status, int time){
		switch (key){
			case Characters.ENTER:
			{
				changeCard(currentColor);
				return true;
			}
			case Characters.SPACE:
			{
				changeCard(currentColor);
				return true;
			}
		}
		return super.keyChar(key, status, time);
	}
	
	public boolean onClose(){
		System.exit(0);
		return true;
		/*
		*int confirmationResult = Dialog.ask(Dialog.D_YES_NO, "Are you sure you want to exit?", Dialog.YES);
		*if(confirmationResult == Dialog.YES){
		*System.exit(0);
		*	return true;
		*}
		*else{
		*	return false;
		*}
		*/
	}
}
