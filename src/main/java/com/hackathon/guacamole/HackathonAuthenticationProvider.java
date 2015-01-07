package com.hackathon.guacamole;

import org.glyptodon.guacamole.GuacamoleException;
import org.glyptodon.guacamole.net.auth.Credentials;
import org.glyptodon.guacamole.net.auth.UserContext;
import org.glyptodon.guacamole.net.auth.simple.SimpleAuthenticationProvider;
import org.glyptodon.guacamole.net.auth.simple.SimpleConnection;
import org.glyptodon.guacamole.net.auth.simple.SimpleConnectionDirectory;
import org.glyptodon.guacamole.protocol.GuacamoleConfiguration;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import java.util.*;

public class HackathonAuthenticationProvider extends SimpleAuthenticationProvider {
	
	public static final Logger logger = Logger.getLogger(HackathonAuthenticationProvider.class.getClass());

    /* two constructed functions */
    public HackathonAuthenticationProvider() {
    	PropertyConfigurator.configure("/etc/guacamole/logger.properties");
    	logger.info("log init sucess ~~~");

    }

    @Override
    public Map<String, GuacamoleConfiguration> getAuthorizedConfigurations(Credentials credentials) throws GuacamoleException {

        GuacamoleConfiguration config = getGuacamoleConfiguration(credentials.getRequest());

        if (config == null) {
            return null;
        }

        Map<String, GuacamoleConfiguration> configs = new HashMap<String, GuacamoleConfiguration>();
        configs.put(config.getParameter("id"), config);
        return configs;
    }

    @Override
    public UserContext updateUserContext(UserContext context, Credentials credentials) throws GuacamoleException {
        HttpServletRequest request = credentials.getRequest();
        GuacamoleConfiguration config = getGuacamoleConfiguration(request);
      
        if (config == null) {
            return null;
        }
        String id = config.getParameter("id");
        SimpleConnectionDirectory connections = (SimpleConnectionDirectory) context.getRootConnectionGroup().getConnectionDirectory();
        SimpleConnection connection = new SimpleConnection(id, id, config);
        connections.putConnection(connection);
        return context;
    }

    private GuacamoleConfiguration getGuacamoleConfiguration(HttpServletRequest request) throws GuacamoleException {
    	
    	GuacamoleConfiguration config ;
    	String checkResult = null;
    	
    	
        /*get cookies */
    	String cookieString = "";
        Cookie cookies[]= request.getCookies();
        logger.info("=======The cookies info from client======:");
        for (int i = 0; i < cookies.length; i++) {
			Cookie cookie = cookies[i];		
			logger.info(cookie.getName() + "||" + cookie.getValue());
			cookieString = cookie.getName() + "=" + cookie.getValue();
		}
        logger.info("cookieString is : |" + cookieString);
        logger.info("==========================================");
        
        
        
        /*check user valid or not*/
		try {
			Connect2OpenHackathon conn = new Connect2OpenHackathon();
			checkResult = conn.checkUser(cookieString);
			logger.info("Check User result is :" + checkResult);
			
			
			/*if user is valid then connect withn OSSLAB and get user info*/
			if (checkResult != null) {
				config = new GuacamoleConfiguration();
				
				
				String id = request.getParameter("id").substring(2);
		    	/**
		    	 *  This should really use BasicGuacamoleTunnelServlet's IdentfierType, but it is private!
		    	 *  Currently, the only prefixes are both 2 characters in length, but this could become invalid at some point.
		    	 *  see: guacamole-client@a0f5ccb:guacamole/src/main/java/org/glyptodon/guacamole/net/basic/BasicGuacamoleTunnelServlet.java:244-252
		         **/
				config.setParameter("id", "id");
				config.setParameter("username", "username");
				config.setParameter("password", "password");
				config.setParameter("hostname", "hostname");
				config.setParameter("port", "port");
				config.setProtocol("vnc or ssh");
				
				return config ;
			
				
			/*if user is invalid absolutly return null*/	
			}else {
				return null ;
			}
	
			
		} catch (Exception e) {
			logger.error("Exception when connect with OSSLAB to check User Cookies AAA");
			e.printStackTrace();
			return null;
		}

    }
}

