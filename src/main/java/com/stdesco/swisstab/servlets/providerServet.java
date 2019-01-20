package com.stdesco.swisstab.servlets;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.gson.Gson;
//import com.stdesco.swisstab.apicode.InitialisationPost;
import com.stdesco.swisstab.apicode.Provider;


/**
 * Servlet implementation class UpdateUsername
 * 
 */

@WebServlet("/provider")

public class providerServet extends HttpServlet {
  private static final long serialVersionUID = 1l;
  private static Logger LOGGER = 
      Logger.getLogger(providerServet.class.getName());
  DatastoreService datastore = 
      DatastoreServiceFactory.getDatastoreService();
  
  Entity entity;
  String xriottoken;
  String httpreturn;
  String region;

 
  public void doPost(HttpServletRequest req, HttpServletResponse resp)
  
      throws ServletException, IOException {
 
        System.out.print("We in boyz\n");
        
        // Create a map to handle the data 
        Map <String, Object> map = new HashMap<String, Object>();
        boolean isValid = false;
        // String username = req.getParameter("username");     
        isValid = true;
        
        // Pull the global entity from Google Cloud Datastore
        try {
          entity = getEntity();
        } catch(EntityNotFoundException e) {
          // TODO Handle this
          e.printStackTrace();
        } 
        
        // Pull the global properties and set them as variables
        setVars(entity);
        
        // Create an object of class provider
        Provider prov = new Provider();
            
        try {
          
          prov.init_Provider(httpreturn, xriottoken, 
                              region);
               
          //Adds the boolean result of POST validity to the map
               
          //Adds the string result for username 
          map.put("username", Integer.toString(prov.get_ProviderId()));                  
          
          entity.setProperty("providerCode", prov.get_ProviderId());
          datastore.put(entity);
               
        } catch(IOException e) {
            e.printStackTrace();
            LOGGER.severe("Invalid Return Post URL, API Key or Region in "
                  + "servletBuilder.java");
            // TODO Handle Exception by messaging the USER to contact an Admin.
          
        } catch(Exception e)   {            
            e.printStackTrace();          
            // TODO When does this Exception throw?
            LOGGER.warning("Haha this will never happen XD");            
        } 
        
        //Create a new Entity of the provider in line with zac's DB structure
        //This doesn't do anything at the moment but is important when creating
        //A tournament. 
        
        Entity global = new Entity("Provider", 
                                       prov.get_ProviderId());      
        global.setProperty("providerID", prov.get_ProviderId());
        global.setProperty("region", region);
        global.setProperty("url", httpreturn);
        datastore.put(global);
        
        System.out.print("Added new entity provider in the Datastore\n");
        
        map.put("isValid", isValid);
        
        write(resp, map);
  }

  private void write(HttpServletResponse resp ,Map <String, Object> map) throws 
    IOException {
      resp.setContentType("application/json");
      resp.setCharacterEncoding("UTF-8");
      System.out.print("got here - GSON");
      resp.getWriter().write(new Gson().toJson(map));      
  }
  
  public void doGet(HttpServletRequest req, HttpServletResponse resp) 
      throws ServletException, IOException {
    
  }
  
  /**
   * 
   * @return
   * @throws EntityNotFoundException 
   */
  private Entity getEntity() throws EntityNotFoundException {
      // Generate the datastore, key and entity
      Key key = KeyFactory.createKey("Globals", "highschool");
      entity = datastore.get(key);
      return entity;
  }
  
  /**
   * Pull the properties from the new entity
   */
  private void setVars(Entity entity) {
    
    xriottoken = (String) entity.getProperty("apiKey");
    System.out.println("API Key:" + xriottoken);
    httpreturn = (String) entity.getProperty("appUrl");
    System.out.println("appUrl:" + httpreturn);
    region = (String) entity.getProperty("region");
    System.out.println("region:" + region);
  }

  
}  

