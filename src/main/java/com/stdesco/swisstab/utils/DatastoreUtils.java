package com.stdesco.swisstab.utils;

import java.util.logging.Logger;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;

/**
 * Copyright (C) Zachary Thomas - All Rights Reserved
 * Unauthorised copying of this file, via any medium, is strictly
 * prohibited. Proprietary & Non-Free.
 * 
 * This file cannot be copied and/or distributed without the express
 * permission of the author.
 * 
 * Tournament Class
 * 
 * @author zthomas
 * January 2019
 *
 */

public class DatastoreUtils {
	static Globals globals = new Globals();
	@SuppressWarnings("unused")
	private static final long serialVersionUID = 1l;
	private static Logger LOGGER = 
	 				Logger.getLogger(DatastoreUtils.class.getName());
	static DatastoreService datastore = 
			  		DatastoreServiceFactory.getDatastoreService();	
	
	/** Public Method with the Datastore Utils to return the key to provider
	 * 
	 * @return Key return a Key to the Datastore kind "Provider" 
	 */
	
	public static Key getProviderKey() {
		Key key;
		int providerID = globals.getGlobalProviderID();
		
		key = KeyFactory.createKey("Provider", providerID);
		return key;
	}
	
	/** Public Method with the Datastore Utils to return the key to a 
	 *  tournament with a given ID 
	 * 
	 * @param tournamentID - integer of the tournamentID
	 * @return Key for an entity of Kind "Tournament" with a given tournamentID
	 */
	public static Key getTournamentKey(int tournamentID) {
		int providerID = globals.getGlobalProviderID();
		
		Key key;
		
		key = new KeyFactory.Builder("Provider", providerID)
				.addChild("Tournament", tournamentID)
				.getKey();
		
		LOGGER.finest("DatastoreUtils:69: Returning Tournament Key: input: "
				+ Integer.toString(tournamentID));
		
		return key;
	}
	
	/** Public method for getting the tournamentID from the Tournament Name
	 * 
	 * @param String - Tournament Name 
	 * @return int - Tournament ID matching Tournament Name
	 * 
	 * @throws - NullPointerException if the tournamentName is not found within
	 * the datastore 
	 */
	public static int getTournamentID(String tournamentName) 
				throws NullPointerException {
		
		int tournamentID = 0;
		
		//---- Run a Query on the Tournament Name to get info ----//
		
	    Filter propertyFilter =
	        new FilterPredicate("tournamentName", 
	        		  				FilterOperator.EQUAL, tournamentName);
	    Query q = new Query("Tournament")
	    		.setFilter(propertyFilter); 
        PreparedQuery pq = datastore.prepare(q);
        
        //Throw a null pointer exception if that tournamentName was not returned
        if(pq == null) {
        	LOGGER.warning("DatastoreUtils:103: "
        			+ " Tournament: " + tournamentName + ": Was not Found\n");
        	throw new NullPointerException("DatastoreUtils:103: "
        			+ " Tournament: " + tournamentName + ": Was not Found\n");
        }
        
        Entity result = pq.asSingleEntity();
        tournamentID = Math.toIntExact((long) 
        		       result.getProperty("tournamentID"));
        LOGGER.finer("DatastoreUtils:103: converted tournament name to id"
       		 + ": " + tournamentID + "\n");
        
	    return tournamentID;
	 
	}
	
	/** Public method for getting a key to a given Pairing with input parameters
	 * 
	 * @param round - integer for the requested round
	 * @param tournamentName - String of the tournamentName
	 * @return - Key for the requested pairing
	 * 
	 * @throws - NullPointerException if the tournamentName is not found within
	 * the datastore 
	 */
	public static Key getPairingKey(int round, String tournamentName)
			throws NullPointerException {
			
		int providerID = globals.getGlobalProviderID();
		int tournamentID = 0;
		Key key;
		
		//---- Run a Query on the Tournament Name to get info ----//
		
	    Filter propertyFilter =
	        new FilterPredicate("tournamentName", 
	        		  				FilterOperator.EQUAL, tournamentName);
	    Query q = new Query("Tournament")
	    		.setFilter(propertyFilter); 
        PreparedQuery pq = datastore.prepare(q);
        
        //Throw a null pointer exception if that tournamentName was not returned
        if(pq == null) {
        	LOGGER.warning("DatastoreUtils:103: "
        			+ " Tournament: " + tournamentName + ": Was not Found\n");
        	throw new NullPointerException("DatastoreUtils:103: "
        			+ " Tournament: " + tournamentName + ": Was not Found\n");
        }
        
        Entity result = pq.asSingleEntity();
        tournamentID = Math.toIntExact((long) 
        		       result.getProperty("tournamentID"));
        
        LOGGER.finer("DatastoreUtils:103: converted tournament name to id"
       		 + ": " + tournamentID + "\n");
	   
		key = new KeyFactory.Builder("Provider", providerID)
				.addChild("Tournament", tournamentID)
				.addChild("Pairing", round)
				.getKey();
		
		return key;
	}
	
	/** Public method for getting a key to a given Pairing with input parameters
	 * 
	 * @param round - integer for the requested round
	 * @param tournamentID - integer of the tournamentID 
	 * @return - Key for the requested pairing
	 */
	
	public static Key getPairingKey(int round, int tournamentID) {
		int providerID = globals.getGlobalProviderID();
		
		Key key;
		
		key = new KeyFactory.Builder("Provider", providerID)
				.addChild("Tournament", tournamentID)
				.addChild("Pairing", round)
				.getKey();
		
		return key;
	}
	
	/** Public method for getting a key to the given gameKey with the inputs
	 * 
	 * @param gameID - String Itentifier for the requested game
	 * @param round - Integer for the current round - Pairing key
	 * @param tournamentID - int tournamentID
	 * @return
	 */
	public static Key getGameKey(String gameID, int round, int tournamentID) {
		
		int providerID = globals.getGlobalProviderID();
		
		Key gameKey = new KeyFactory.Builder("Provider", providerID)
				.addChild("Tournament", tournamentID)
				.addChild("Pairing", round)
				.addChild("Game", gameID)
				.getKey();
		
		return gameKey;
		
	}
	
	/** Public method for getting the datastore TeamKey
	 * 
	 * @param teamName - String team name = id
	 * @param providerKey - Key for the parent Provider of Team
	 * @return teamKey - Returns a key for datastore lookup of Team
	 */	
	public static Key getTeamKey(String teamName, Key tournamentKey) {
		
		Key teamKey = new KeyFactory.Builder(tournamentKey)
				.addChild("Team", teamName)
				.getKey();
		
		return teamKey;
	}
	
	/** Public method for getting a team key using parent TournamentID
	 * 
	 * @param teamName - String of the team we are generating a key for
	 * @param TournamentID - int of the parent tournamentID
	 * @return Key for the requested team
	 */
	public static Key getTeamKey(String teamName, int TournamentID) {
		
		int providerID = globals.getGlobalProviderID();
		
		Key teamKey = new KeyFactory.Builder("Provider", providerID)
				.addChild("Tournament", TournamentID)
				.addChild("Team", teamName)
				.getKey();
		
		return teamKey;
	}
	
	/** Public method for getting a key to the given gameKey with the inputs
	 * 	
	 * @param gameID - String for corresponding gameID
	 * @param pairingKey - Key for the parent Pairing of the game
	 * @return
	 */
	
	public static Key getGameKey(String gameID, Key pairingKey) {
		
		Key gameKey = new KeyFactory.Builder(pairingKey)
				.addChild("Game", gameID).getKey();
		
		return gameKey;
		
	}
	
	/** Publice method for getting a datastore Entity 
	 * 
	 * @param key - Key to get the entity from the datastore
	 * @return Entity 
	 */
	public static Entity getdataStoreEntity(Key key) {		
		 Entity entity;	
		 
		 try {
			   entity = datastore.get(key);
			   return entity;
			} catch (EntityNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		       return null;	
		  }	
	}

}
