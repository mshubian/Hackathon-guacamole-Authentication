package com.openhackathon.guacamole;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.glyptodon.guacamole.net.auth.Connection;
import org.glyptodon.guacamole.net.auth.simple.SimpleConnection;
import org.glyptodon.guacamole.net.auth.simple.SimpleConnectionDirectory;
import org.glyptodon.guacamole.protocol.GuacamoleConfiguration;

public class OpenHackathonConnectionDirectory extends SimpleConnectionDirectory{

    private Map<String, Connection> connections = new HashMap<String, Connection>();	

	public OpenHackathonConnectionDirectory(Map<String, GuacamoleConfiguration> configs) {
		super(configs);
		
		 for (Entry<String, GuacamoleConfiguration> entry : configs.entrySet())
	            connections.put(entry.getKey(),
	                    new SimpleConnection(entry.getValue().getParameter("name"), entry.getValue().getProtocol(), 
	                entry.getValue()));		
	}
	
	@Override
    public Connection putConnection(Connection connection) {
        return connections.put(connection.getName(), connection);
    }
}
