package com.hackathon.guacamole;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;

import javax.servlet.http.Cookie;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;


public class Connect2Osslab {

	private Logger logger = Logger.getLogger(Connect2Osslab.class.getClass());
	private static HttpURLConnection  conn = null;
	private URL url = null ;
	
	
	public Connect2Osslab() throws Exception{
		
		PropertyConfigurator.configure("/etc/guacamole/logger.properties");
        conn.setRequestMethod("GET");  
        conn.setDoOutput(true);  
        conn.setDoInput(true);
        conn.setUseCaches(false); 

	}
	
	public  String checkUser(String cookieString) {
		
		String URLstring = "http://osslab.msopentech.cn/checkguacookies";
        String result = "";

        BufferedReader in = null;
        
        try {
        	 url = new URL(URLstring);
        	 conn = (HttpURLConnection) url.openConnection();
             conn.setRequestProperty("soapActionString",URLstring);
             conn.addRequestProperty("Cookie", cookieString);
             conn.connect();
            
            in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
            
        } catch (Exception e) {
        	logger.error("Exception when connect with OSSLAB to check User Cookies");
            e.printStackTrace();
        }
        finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (conn != null) {
                    conn.disconnect();
				}
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return result;
    }
  
}

	

