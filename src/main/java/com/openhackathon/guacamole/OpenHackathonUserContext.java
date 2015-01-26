package com.openhackathon.guacamole;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import org.glyptodon.guacamole.net.auth.ConnectionGroup;
import org.glyptodon.guacamole.net.auth.Directory;
import org.glyptodon.guacamole.net.auth.User;
import org.glyptodon.guacamole.net.auth.simple.SimpleConnectionGroup;
import org.glyptodon.guacamole.net.auth.simple.SimpleUser;
import org.glyptodon.guacamole.net.auth.simple.SimpleUserContext;
import org.glyptodon.guacamole.net.auth.simple.SimpleUserDirectory;
import org.glyptodon.guacamole.protocol.GuacamoleConfiguration;

import com.openhackathon.guacamole.OpenHackathonConnectionDirectory;




import org.glyptodon.guacamole.net.auth.simple.SimpleConnectionGroupDirectory;

public class OpenHackathonUserContext extends SimpleUserContext {
	
	private final ConnectionGroup connectionGroup;
	private final Directory<String, User> userDirectory;
	private final User self;
	
	
    public OpenHackathonUserContext(Map<String, GuacamoleConfiguration> configs) {
        this(UUID.randomUUID().toString(), configs);
    }

	public OpenHackathonUserContext(String username, Map<String, GuacamoleConfiguration> configs) {
		super(configs);

        // Add root group that contains only configurations
        this.connectionGroup = new SimpleConnectionGroup("ROOT", "ROOT",
                new OpenHackathonConnectionDirectory(configs),
                new SimpleConnectionGroupDirectory(Collections.EMPTY_LIST));

        // Build new user from credentials, giving the user an arbitrary name
        this.self = new SimpleUser(username,
                configs, Collections.singleton(connectionGroup));

        // Create user directory for new user
        this.userDirectory = new SimpleUserDirectory(self);
        		
	}
	
}