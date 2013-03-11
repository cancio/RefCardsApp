package com.cancio;

import java.util.Hashtable;

import net.rim.device.api.system.PersistentObject;
import net.rim.device.api.system.PersistentStore;


class CardData {
	private PersistentObject persistentObject;
	private Hashtable cardCount;
		
	public CardData(){
		// Hash of antoniomanuelcancio 0x9787015f06321e7cL
		persistentObject= PersistentStore.getPersistentObject(0x9787015f06321e7cL);
		
		synchronized(persistentObject){
			cardCount = (Hashtable)persistentObject.getContents();
			if (null == cardCount){
				cardCount = new Hashtable();
				persistentObject.setContents(cardCount);
				persistentObject.commit();
			}
		}
	}
	
	public Object get(String key) {
		return cardCount.get(key);
	}
	
	public void set(String key, Object value){
		cardCount.put(key, value);
	}
	
	public void commit(){
		persistentObject.commit();
	}
}
