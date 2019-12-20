package com.openforce.keycloak;

import java.util.HashMap;

/**
 * Fake database for storing users.
 *
 *@author openFORCE
 */
public class UserDatabase {

	/**
	 * This is my in memory database.  It could be improved with a real SQL database etc...
	 */
	static HashMap<String, User> users = new HashMap<String, User>();
	
	public UserDatabase(){
	}
	
	public User initUser(String email) {
		User user = new User();
		user.setEmail(email);
		user.setFirstName("");
		user.setLastName("");
		user.setPassword(null);
		user.setEmailVerified(false);
		user.setEnabled(false);
		user.setNewRegistration(true);
		user.setPasswordSet(false);
		users.put(email, user);
		return user;
	}
	
	public boolean updateUser(User user) {
		users.put(user.getEmail(), user);
		return users.containsKey(user.getEmail());
	}
	
	public User findUserByEmail(String email) throws UserNotFoundException {
		if (users.containsKey(email)) {
			return users.get(email);
		}
		throw new UserNotFoundException();
	}

	public boolean validateCredentials(String email, String password)  throws UserNotFoundException {
		User user = findUserByEmail(email);
		return user.getPassword() != null 
				&& user.getPassword().equals(password);
	}

	public User registerUser(User user, String password) {
		user.setPassword(password);
		user.setEnabled(true);
		user.setPasswordSet(true);
		user.setEmailVerified(true); // If you set up email server in Keycloak you can set this to false.
		updateUser(user);
		return user;
	}

	public boolean updateCredentials(String email, String password)  throws UserNotFoundException {
		User user = findUserByEmail(email);
		user.setPassword(password);
		updateUser(user);
		return updateUser(user);
	}

}
