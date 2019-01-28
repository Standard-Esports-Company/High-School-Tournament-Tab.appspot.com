package com.stdesco.swisstab.servlets;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.gson.Gson;
import com.stdesco.swisstab.apicode.TournamentAPI;
//import com.stdesco.swisstab.apicode.InitialisationPost;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;


/**
 * Servlet implementation class UpdateUsername
 * 
 */

@WebServlet("/CreateTournament")
public class CreateTournament extends HttpServlet {
  private static final long serialVersionUID = 1l;
  private static Logger LOGGER = 
  					Logger.getLogger(CreateTournament.class.getName());
  DatastoreService datastore = 
		  			DatastoreServiceFactory.getDatastoreService();
  
  Entity entity;
  String region;
  Key key;
  String apiKey;
  int providerID;
  int tournamentID;
  Entity tour;


 
  public void doPost(HttpServletRequest req, HttpServletResponse resp)
		  throws ServletException, IOException {
	  
	  System.out.print("CreateTournament:50: Running \n");
	  
	  String tname = req.getParameter("tname");
	  
	  System.out.print("CreateTournament:54: Tournament Name: " + tname + "\n");
	  
	  Map<String, Object> map = new HashMap<String, Object>();
	  int respcode = 0;
		
      /*
       * Query to check whether or not that name already exist and if it 
       * does not exist already continue to create the tournament code 
       * and record using the API. 
       */
      Filter propertyFilter =
          new FilterPredicate("tournamentName", FilterOperator.EQUAL, tname);
      Query q = new Query("Tournament").setFilter(propertyFilter);

      try {
    	//attempt to run the query 
        PreparedQuery pq = datastore.prepare(q);
        Entity result = pq.asSingleEntity();
        System.out.print("Cannot override tournament" + result.toString() + "\n");
		map.put("respcode", 0);	
		write(resp, map);
		return;
        
      } catch (Exception e) {        
        // TODO Auto-generated catch block
        e.printStackTrace();
        System.out.print("CreateTournament:54: No record was found continue with "
        		+ "creation of the tournament" + tname + "\n");
        //respcode = 1;
      }
      
	    
		// Generates the Provider Entity Key for parenting.
		int count = 0;
		while(true) {
			try {
				key = getProviderKey();
				break;
			} catch (EntityNotFoundException e) {
				if (count == 1) {
					LOGGER.severe("EntityNotFoundException despite Globals "
							+ "set!");
					e.printStackTrace();
					break;
				}
				LOGGER.warning("Global entity not found. Need to init Globals "
						+ "first ");
				InitDatastore.createGlobals();
				count++;
			}
		}
		
		// Generates the tournamentID to be used as the keyName.
		try {
			tournamentID = generateTournamentID(tname);
		} catch (Exception e) {
			// TODO Handles this. Probs with alert to Admin.
			LOGGER.severe("Tournament could not construct. Probably because "
					+ "apiKey or providerID invalid, or tournamentName "
					+ "illegal.");
			e.printStackTrace();
		}
		
		// Generates the Tournament Entity.
		createTournament(tournamentID, key, tname);
		
		// Set dummy data
		//dummyMethod(tournamentID);
		
		// I don't know what this is...
		map.put("tournament", Integer.toString(tournamentID));
		map.put("respcode", 1); //Record successfully created
		write(resp, map);
		
	}
	
	/**
	 * I don't know what this does. 
	 * 
	 * @param resp
	 * @param map
	 * @throws IOException
	 */
	private void write(HttpServletResponse resp, Map<String, Object> map)
			throws IOException {
		resp.setContentType("application/json");
		resp.setCharacterEncoding("UTF-8");
		System.out.print("Sending back response to the webapp -> GSON\n");
		resp.getWriter().write(new Gson().toJson(map));
	}
	
	/**
	 * Grabs the key to the Provider Entity by pulling the provider ID from the
	 * Globals Entity.
	 * 
	 * @return 							The Key to the Provider Entity
	 * @throws EntityNotFoundException	When the Provider Entity cannot be found
	 * 									using the providerID. Provider has 
	 * 									probably not been constructed. 
	 */
	private Key getProviderKey() throws EntityNotFoundException {
		Key globalsKey = KeyFactory.createKey("Globals", "highschool");
		Entity globals = datastore.get(globalsKey);
		
		//Grab the global variables
		providerID = Math.toIntExact((long) 
							globals.getProperty("providerID"));
		
		apiKey = (String) globals.getProperty("apiKey");
		
		System.out.print("CreateTournament:140: Provider ID " + 
				providerID + " :API KEY: " + apiKey + " \n");
				
		if (providerID == 0) {
			// ProviderID has not been initialised.
			int count = 0;
			while (true) {
				try {
					ProviderServlet.createProvider();
					globals = datastore.get(globalsKey);
					providerID = (int) globals.getProperty("providerID");
					break;
				} catch (EntityNotFoundException e) {
					if (count == 1) {
						LOGGER.severe("Globals Entity was not found after "
								+ "initialising! Unresolvable Exception.");
						e.printStackTrace();
						break;
					}
					LOGGER.warning("Globals Entity was not found at InitDummy "
							+ "ln:74. Reinitialising the Globals Entity");
					InitDatastore.createGlobals();
					count++;
				} catch (Exception e) {
					// TODO Handle this. Probably with alert to contact admin.
					LOGGER.severe("Invalid Return Post URL, API Key or Region");
					e.printStackTrace();
					break;
				}
			}
		}
		
		Key providerKey = KeyFactory.createKey("Provider", providerID);
		return providerKey;
	}
	
	/**
	 * Constructs a new tournament, and then grabs its tournament ID.
	 * 
	 * @return 				The tournamentID
	 * @argument			String tname with the tournament name
	 * @throws Exception	If the API Key, providerID or tournament name is
	 * 						illegal / invalid. Or if the world is ending. 
	 * 						Seriously, if it's for another reason it's probably
	 * 						a late onset Y2K or some shit. 
	 */
	private int generateTournamentID(String tname) throws Exception {
		TournamentAPI tournament = new TournamentAPI(apiKey, tname, providerID);
		return tournament.getTournamentID();
	}
	
	/**
	 * Creates a new Tournament Entity from a given Tournament ID and Tournament
	 * Name. The new Entity will be the child of the given providerKey.
	 * 
	 * @param tournamentID	The Tournament ID
	 * @param providerKey	The Key to unlock the Parent Provider Entity
	 * @param tname			The desired name of the tournament.
	 */
	private void createTournament(int tournamentID, Key providerKey, 
								  String tname) {
		tour = new Entity("Tournament", tournamentID, providerKey);
		tour.setProperty("tournamentID", tournamentID);
		tour.setProperty("teams", null); // List<String>
		tour.setProperty("rounds", 0);
		tour.setProperty("pairingRule", 0);
		tour.setProperty("numberOfTeams", 0);
		tour.setProperty("currentRound", 0);
		tour.setProperty("allPairings", null); // List<Pairing>
		tour.setProperty("allGames", null); // List<Game>
		tour.setProperty("tournamentName", tname); // List<Game>
		datastore.put(tour);
		return;	
	}
	
	/**
	 * Stores a collection of dummy data for testing purposes. 
	 * 
	 * @param tournamentID	the tournamentID for the desired tournament.
	 */
	@SuppressWarnings("unused")
	private void dummyMethod(int tournamentID) {
		// Set dummy data
		List<String> teams = Arrays.asList("UQ", "UNSW", "Sydney", "ANU", 
				"Melbourne", "Monash", "Adelaide", "UWA");
		int rounds = 3;
		int pairingRule = 1;
		int numberOfTeams = 8;
		
		// Retrieve tournament entity
		long longProviderID = providerID;
		long longTournamentID = tournamentID;
		Key tourKey = new KeyFactory.Builder("Provider", longProviderID)
				.addChild("Tournament", longTournamentID)
				.getKey();
		try {
			tour = datastore.get(tourKey);
		} catch (EntityNotFoundException e) {
			LOGGER.severe("Haha this will never happen lol XD XD. \n ... \n ..."
					+ " \n Something has crashed. Oh god help there's so much"
					+ " blood! PLEASE! SEND HELP!");
			// TODO Note - the above logging is not handling this exception.
		}
		
		// Save the dummy data
		tour.setProperty("teams", teams);
		tour.setProperty("rounds", rounds);
		tour.setProperty("pairingRule", pairingRule);
		tour.setProperty("numberOfTeams", numberOfTeams);
		datastore.put(tour);
	}
}

