package com.hackathon.guacamole;

import org.glyptodon.guacamole.GuacamoleException;
import org.glyptodon.guacamole.net.auth.Credentials;
import org.glyptodon.guacamole.net.auth.UserContext;
import org.glyptodon.guacamole.net.auth.simple.SimpleAuthenticationProvider;
import org.glyptodon.guacamole.net.auth.simple.SimpleConnection;
import org.glyptodon.guacamole.net.auth.simple.SimpleConnectionDirectory;
import org.glyptodon.guacamole.properties.GuacamoleProperties;
import org.glyptodon.guacamole.properties.IntegerGuacamoleProperty;
import org.glyptodon.guacamole.properties.StringGuacamoleProperty;
import org.glyptodon.guacamole.protocol.GuacamoleConfiguration;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import java.util.*;

public class HackathonAuthenticationProvider extends SimpleAuthenticationProvider {
	
	public static final Logger logger = Logger.getLogger(HackathonAuthenticationProvider.class.getClass());
    private SignatureVerifier signatureVerifier;
    private final TimeProviderInterface timeProvider;

    // Properties file params
    private static final StringGuacamoleProperty SECRET_KEY = new StringGuacamoleProperty() {
        @Override
        public String getName() { return "secret-key"; }
    };
    private static final StringGuacamoleProperty DEFAULT_PROTOCOL = new StringGuacamoleProperty() {
        @Override
        public String getName() { return "default-protocol"; }
    };
    private static final IntegerGuacamoleProperty TIMESTAMP_AGE_LIMIT = new IntegerGuacamoleProperty() {
        @Override
        public String getName() { return "timestamp-age-limit"; }
    };

    // these will be overridden by properties file if present
    private String defaultProtocol = "rdp";
    private String method ;
    public static final String PARAM_PREFIX = "guac.";

    private static final List<String> SIGNED_PARAMETER_LIST = new ArrayList<String>() {
    	{
	        add("username");
	        add("password");
	        add("hostname");
	        add("port");
    	}
    };

    /* two constructed functions */
    public HackathonAuthenticationProvider(TimeProviderInterface timeProvider) {
        this.timeProvider = timeProvider;
        logger.info("logger system init sucessed ");
    }
    public HackathonAuthenticationProvider() {
    	PropertyConfigurator.configure("/etc/guacamole/logger.properties");
    	logger.info("log init sucess ~~~");
        timeProvider = new DefaultTimeProvider();
    }

    @Override
    public Map<String, GuacamoleConfiguration> getAuthorizedConfigurations(Credentials credentials) throws GuacamoleException {
        if (signatureVerifier == null) {
            initFromProperties();
        }

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
        
        /* connect method */
        if (method.equals("connect")) {
            SimpleConnection connection = new SimpleConnection(id, id, config);
            connections.putConnection(connection);
        /* logout method */
		}else if (method.equals("logout")) {
			connections.removeConnection("id");
			logger.info("Logout! and remove the connection");
			return null ;
		}

        return context;
    }

    private GuacamoleConfiguration getGuacamoleConfiguration(HttpServletRequest request) throws GuacamoleException {
    	
    	/**
    	 * Rquest String should be like this:
    	 * 
    	 * http://42.159.29.99:8080/guacamole/client.xhtml?
    	 * id=c/123456789&
    	 * clientIP=10.0.2.15&
    	 * method=connect/logout&					
    	 * guac.protocol=ssh&
    	 * guac.hostname=42.159.29.99&
    	 * guac.port=22&
    	 * guac.username=opentech&
    	 * guac.password=Password01!&
    	 * signature=ykXXJ1WMUXWKAPK3Jtf4QsblVfM=
    	 * 
    	 * */
    	
    	logger.info("get request URI : "+request.getRequestURI());
    	logger.info("get request URI method: "+request.getMethod());
    	logger.info("get request URI pathinfo: "+request.getPathInfo());
    	logger.info("get request URI QueryString: "+request.getQueryString());
    	logger.info("get request URI sessionID: "+request.getRequestedSessionId());

/*
    	Enumeration enumer = request.getHeaderNames();
    	logger.info("==========================Headers info ===========================");
    	while (enumer.hasMoreElements()) {
			String object = (String) enumer.nextElement();
			String value = request.getHeader(object);
			logger.info(object+ "||" + value);
		}
    	logger.info("=====================================================");
*/    	
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
        
		try {
			Connect2Osslab conn = new Connect2Osslab();
			String checkResult = conn.checkUser(cookieString);
			logger.info("Check User result is :" + checkResult);
			
		} catch (Exception e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
        
    	
    	
    	
        if (signatureVerifier == null) {
            initFromProperties();
        }

        /*check request values*/
        if (request.getParameter("signature") == null || request.getParameter("clientIP") == null || request.getParameter("method") == null) {
            logger.error("signature or clientIP or method from Request is Lost, Please check the request-URL");
        	return null;
        }
        
        /*guacamole connection parameters parse from request*/
        GuacamoleConfiguration config = parseConfigParams(request);
        
        /*make up the message String*/
        String id = request.getParameter("id").substring(2);
	    	/**
	    	 *  This should really use BasicGuacamoleTunnelServlet's IdentfierType, but it is private!
	    	 *  Currently, the only prefixes are both 2 characters in length, but this could become invalid at some point.
	    	 *  see: guacamole-client@a0f5ccb:guacamole/src/main/java/org/glyptodon/guacamole/net/basic/BasicGuacamoleTunnelServlet.java:244-252
	         **/
        String clientIP = request.getParameter("clientIP");
        method = request.getParameter("method");
       
        
        /*the message String should be like this*/
        /**
         * <sessionID><protocol><clientIP><method>username<username>password<password>hostname<guacamoleIP>port<port>
         * 1a4fe20d3f5vnc10.0.2.15connectusernamerootpassword123456hostname42.159.29.99port5901 
         * */
        StringBuilder message = new StringBuilder();
        message.append(id);
        message.append(config.getProtocol());
        message.append(clientIP);
        message.append(method);

        for (String name : SIGNED_PARAMETER_LIST) {
            String value = config.getParameter(name);
            if (value == null) {
            	logger.error(name + " from Request is Lost, Please check the request-URL");
                return null;
            }
            message.append(name);
            message.append(value);
        }

        /*check signature matched or not*/
        String signature = request.getParameter("signature").replace(' ', '+');
        logger.info("signature from client request is:  " + signature);
        logger.info(" message from client request is :  " + message );
        if (!signatureVerifier.verifySignature(signature, message.toString())) {
            logger.error("Signature is not matched !!! ");
        	return null;
        }
        
        
        /*add id to be a param of config*/
        if (id == null) {
            id = "DEFAULT";
        } 
        // This isn't normally part of the config, but it makes it much easier to return a single object
        config.setParameter("id", id);
        return config;
    }
    
    /*remove check time-limit-age Function*/

    private GuacamoleConfiguration parseConfigParams(HttpServletRequest request) {
        GuacamoleConfiguration config = new GuacamoleConfiguration();

        Map<String, String[]> params = request.getParameterMap();

        for (String name : params.keySet()) {
            String value = request.getParameter(name);
            if (!name.startsWith(PARAM_PREFIX) || value == null || value.length() == 0) {
                continue;
            }
            else if (name.equals(PARAM_PREFIX + "protocol")) {
                config.setProtocol(request.getParameter(name));
            }
            else {
                config.setParameter(name.substring(PARAM_PREFIX.length()), request.getParameter(name));
            }
        }

        if (config.getProtocol() == null) config.setProtocol(defaultProtocol);

        return config;
    }

    private void initFromProperties() throws GuacamoleException {
        String secretKey = GuacamoleProperties.getRequiredProperty(SECRET_KEY);
        signatureVerifier = new SignatureVerifier(secretKey);
        defaultProtocol = GuacamoleProperties.getProperty(DEFAULT_PROTOCOL);
        if (defaultProtocol == null) defaultProtocol = "rdp";        
    }
}

