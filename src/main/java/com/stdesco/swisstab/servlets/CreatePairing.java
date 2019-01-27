package com.stdesco.swisstab.servlets;

import java.io.IOException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import com.stdesco.swisstab.webapp.GlobalsUtility;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.gson.Gson;
//import com.stdesco.swisstab.apicode.InitialisationPost;
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
import com.stdesco.swisstab.appcode.Tournament;
import com.stdesco.swisstab.appcode.Tournament.FirstRoundPairingRule;
import com.stdesco.swisstab.appcode.Pairing;

/**
 * Servlet for running the process to create a new team 
 * req passes in a teamname and tournament name.
 * The team is added to the data-store with its variables
 * initialized and then added to the list of team names 
 * in the tournament with tournament name 
 */

@WebServlet("/CreatePairing")

//Possibly takes in a tournamentcode

public class CreatePairing extends HttpServlet {
  private static final long serialVersionUID = 1l;
  @SuppressWarnings("unused")
  private static Logger LOGGER = 
  					Logger.getLogger(CreatePairing.class.getName());
  DatastoreService datastore = 
		  			DatastoreServiceFactory.getDatastoreService();
  
  private List<String> teamslist;
  FirstRoundPairingRule pairingrule;
  Pairing pairing;
  private int rounds;
  private int currentround;
  private int pairingruleint;
  private int numberofteams;
  private Entity tournamentEntity;
 
@SuppressWarnings("unchecked")
public void doPost(HttpServletRequest req, HttpServletResponse resp)
		  throws ServletException, IOException {  
	  
	  System.out.print("CreatePairing:44: Running \n");
	  
	  //initialize the hash-map for response back to web-app
	  Map<String, Object> map = new HashMap<String, Object>();
	  int tournamentID = 4579;
	  int providerID = new GlobalsUtility().getGlobalProviderID();
	  
	  System.out.print("CreatePairing:55: TournamentID:"
	  		+ tournamentID + "ProviderID:" + providerID + "\n");
	  
	  
      /* Run a query on the Tournament Code and get the list of teams 
       * check that that list is of okay format for creating a pairing
       * then assign the local variable teamslist.
       */	  
      Filter propertyFilter =
          new FilterPredicate("tournamentID", FilterOperator.EQUAL, 
        		  											tournamentID); 
      //initialize the query
      Query q = new Query("Tournament").setFilter(propertyFilter);

      try {   
    	//Run the query  
        PreparedQuery pq = datastore.prepare(q);
        Entity qresult = pq.asSingleEntity();
        System.out.print("CreatePairing:82: query result" 
        							+ qresult.toString() + "\n");
        
        //Check if the list of teams that is retrieved is null
        if((List<String>) qresult.getProperty("teams") == null) {
        	//Set response code == 2 - List of teams returned null
        	System.out.print("CreatePairing:88: teamlist is null\n");
      	  	map.put("respcode", 2);       
      	  	write(resp, map); 
      	  	return; //Abort Process
      	
      	//Check if at list 2 teams exist in the tournament  	
        }else if(((List<String>) qresult.getProperty("teams")).size() < 2){
        	//Set response code == 1 - List of teams is less than 2
        	System.out.print("CreatePairing:88: teamslist has less than 2 "
        			+ "entries \n");
      	  	map.put("respcode", 1);       
      	  	write(resp, map); 
      	  	return; //Abort Process
      	
      	//Set local teamslist to variable teamlist
        }else{
        	//The team list is all good
        	System.out.print("CreatePairing:88: teamslist is all good\n");
        	teamslist = (List<String>) qresult.getProperty("teams");
        }
        
        //Yes that tournament name exists you are all good to go.            
        
      } catch (Exception e) {        
        // TODO Complete the exception handle
        e.printStackTrace();
        System.out.print("Query result :" + "NULL" + ": \n"); 
        //Set response code == 3 - No tournament was found 
        map.put("respcode", 3);       
        write(resp, map);
        return; //Abort Process
      }
	     
      
      
      /* Begin tournament pairing process. Convert the datastore state 
       * into a useable object -> run the pairing and then save the state
       * Checks wether or not allGames have results from the previous round
       * before going ahead with the pairing. 
       */
      
      //Pull the current state
	  long longProviderID = providerID;
	  long longTournamentID = tournamentID;
	  Key tournamentKey = new KeyFactory.Builder("Provider", longProviderID)
			.addChild("Tournament", longTournamentID)
			.getKey();
	  try {
		 tournamentEntity = datastore.get(tournamentKey);
	  } catch (EntityNotFoundException e) {
		 // TODO Note - the above logging is not handling this exception.
	  }
      
	  
	  //Update the local variables
	  rounds = Math.toIntExact((long)tournamentEntity.getProperty("rounds"));
  	  currentround = Math.toIntExact((long) 
  			tournamentEntity.getProperty("currentRound"));
  	  pairingruleint = Math.toIntExact((long) 
    			tournamentEntity.getProperty("pairingRule"));
  	  numberofteams = Math.toIntExact((long) 
  			tournamentEntity.getProperty("numberOfTeams"));
  	  
  	  setPairingRuleConverter(1);
  	  
	  //Create object of type tournament

      Tournament tournament = new 
    		  		   Tournament(rounds, numberofteams, teamslist);     
      tournament.setFirstRoundPairingRule(pairingrule);
      pairing = tournament.pairNextRound();
      
      System.out.println("CreatePairing:168: " + pairing + "\n");
      System.out.println("CreatePairing:169: " + pairing.getGames() + "\n"); 
      try {
    	  System.out.println("CreatePairing:171: " + tournament.getByeTeam());
      } catch (IllegalStateException e) {
    	  System.out.println("CreatePairing:172: no bye team set");
      }
      
      
      /*FirstRoundPairingRule pairingrule;
      setPairingRule(pairingrule);
      int pairingindicator = tournamententity.getproperty("pairingrule");
      if (pairingindicator = 1) {
    	  pairingrule = pairingrule.FIRST_ROUND_GAME_ORDERED;
      } if (pairingindicator = 2) {
    	  pairingrule = pairingrule.FIRST_ROUND_GAME_RANDOM;
      } else {
    	  throw new IllegalStateException("Someone has entered an invalid " 
    			  + "pairing rule indicator into the datastore!");
      }
      
      
      if(currentround == 0) {
    	  tournament.
      }else {
    	  //perform second round pairing
      }*/
      
      
 }
	
	/**
	 * Writes the HttpServletResponse resp back to the web-application
	 * Map is sent back a JSON which can be accessed by the AJAX callback
	 * function and return information to user
	 * @param resp
	 * @param map
	 * @throws IOException
	 */
	private void write(HttpServletResponse resp, Map<String, Object> map)
			throws IOException {
		resp.setContentType("application/json");
		resp.setCharacterEncoding("UTF-8");
		System.out.print("CreateTeam:154: Sending JSON Response \n");
		System.out.print(new Gson().toJson(map).toString() + "\n");
		resp.getWriter().write(new Gson().toJson(map));
	}
	
	/**
	 * Takes the datastore pairing rule indicator and sets the tournament
	 * pairing rule. 
	 * 
	 * @param pairingindicator
	 * 
	 * @throws IllegalStateException when the pairing rule in the datastore is
	 * not a 1 or a 2.
	 */
	private void setPairingRuleConverter(int pairingindicator) {
		System.out.println(pairingindicator + "\n");
		System.out.println((pairingindicator == 1) + "\n");
		
		if (pairingindicator == 1) {
	    	  pairingrule = pairingrule.FIRST_ROUND_GAME_ORDERED;
	    } else if (pairingindicator == 2) {
	    	  pairingrule = pairingrule.FIRST_ROUND_GAME_RANDOM;
	    } else {
	    	  throw new IllegalStateException("Someone has entered an invalid" 
	    			  + "pairing rule indicator into the datastore! \n");
	    }	
	}
	
	

}	

