package com.hackathon.guacamole;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;

import javax.naming.spi.DirStateFactory.Result;
import javax.servlet.http.Cookie;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;


public class Connect2Osslab {

	private Logger logger = Logger.getLogger(Connect2Osslab.class.getClass());
	private static HttpURLConnection  conn = null;
	private URL url = null ;
    private BufferedReader in = null;
	
	
	public Connect2Osslab() throws Exception{
		
		PropertyConfigurator.configure("/etc/guacamole/logger.properties");
		
        conn.setRequestMethod("GET");  
        conn.setDoOutput(true);  
        conn.setDoInput(true);
        conn.setUseCaches(false); 
	}
	
	/*check user withn cookies */
	public  String checkUser(String cookieString) {
		
		String URLstring = "http://osslab.msopentech.cn/checkguacookies";
        String result = "";
     
        try {
        	 url = new URL(URLstring);
        	 conn = (HttpURLConnection) url.openConnection();
             conn.setRequestProperty("soapActionString",URLstring);
             conn.setRequestProperty("Cookie", cookieString);
             conn.connect();
            
             in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
             String line;
             while ((line = in.readLine()) != null) {
                 result += line;
             }
            
        } catch (Exception e) {
        	logger.error("Exception when connect with OSSLAB to check User Cookies BBB");
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
  
	/*get user connet guacamole login infomathons*/
	public String getUserinfo() throws IOException {
		
		String result = "";
		String URLstring = "http://osslab.msopentech.cn/getuserloginguacdinfo";
		
		try {
       	 	url = new URL(URLstring);
       	 	conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("soapActionString",URLstring);
            conn.connect();
            
            conn.getResponseCode();
           
            in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
           
		} catch (Exception e) {
       	   logger.error("Exception happend when get user login guacamole from OSSLAB!!!");
           e.printStackTrace();
		}
		finally {
    	   
           if (in != null) in.close();       
           if (conn != null) conn.disconnect();    	   
		}
		
		return result ;
	}	
}

	

