package com.openforce.keycloak;

import java.util.Collections;
import java.util.Set;

import org.jboss.logging.Logger;
import org.keycloak.component.ComponentModel;
import org.keycloak.credential.CredentialInput;
import org.keycloak.credential.CredentialInputUpdater;
import org.keycloak.credential.CredentialInputValidator;
import org.keycloak.models.*;
import org.keycloak.models.UserModel.RequiredAction;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.UserStorageProvider;
import org.keycloak.storage.user.UserLookupProvider;
import org.keycloak.storage.user.UserRegistrationProvider;

/**
 * Storage Provider implements the Keycloak interfaces for storing users in an external store.
 * The external store is used to check passwords and get and set basic user data, and store new 
 * registrations.
 *
 * @author openFORCE
 */
public class MyUserStorageProvider implements
		UserStorageProvider, 
        UserRegistrationProvider,
        UserLookupProvider,
        CredentialInputUpdater, 
        CredentialInputValidator {

    private final KeycloakSession session;
    private final ComponentModel model;
    private final UserDatabase userDatabase;

    private static final Logger log = Logger.getLogger(MyUserStorageProvider.class);

    public MyUserStorageProvider(KeycloakSession session, ComponentModel model,  UserDatabase userDatabase) {
    	
    	log.info("MyUserStorageProvider Constructed.");
        this.session = session;
        this.model = model;
        this.userDatabase = userDatabase;
    }
    
    @Override
    public boolean supportsCredentialType(String credentialType) {
        return "password".equals(credentialType);
    }

    @Override
    public boolean isConfiguredFor(RealmModel realm, UserModel user, String credentialType) {
        return supportsCredentialType(credentialType);
    }

    @Override
    public boolean isValid(RealmModel realm, UserModel user, CredentialInput input) {
    	if (!supportsCredentialType(input.getType()) || !(input instanceof UserCredentialModel)) {
            return false;
        }
        UserCredentialModel cred = (UserCredentialModel) input;
        try {
        	return userDatabase.validateCredentials(user.getUsername(), cred.getValue());
        } catch (UserNotFoundException e) {
        	throw new ModelException("internalServerError");
		}
    }

    @Override
    public boolean updateCredential(RealmModel realm, UserModel userModel, CredentialInput input) {
    	
    	if (!supportsCredentialType(input.getType()) || !(input instanceof UserCredentialModel)) {
            return false;
        }
    	boolean success = false;
    	UserCredentialModel cred = (UserCredentialModel) input;
		try {
			User user = userDatabase.findUserByEmail(userModel.getEmail());
			if (user.isNewRegistration()) {
				user.setFirstName(user.getFirstName());
				user.setLastName(user.getLastName());
				user = userDatabase.registerUser(user, cred.getValue());
	    		if (user != null) {
	    			userModel.setEnabled(user.isEnabled());
	    			userModel.setEmailVerified(user.isEmailVerified());
	    			if (userModel.isEmailVerified()) {
	    				userModel.removeRequiredAction(RequiredAction.VERIFY_EMAIL);
	    			} else  {
	    				userModel.addRequiredAction(RequiredAction.VERIFY_EMAIL);
	    			}
	    			success = true;
	    		} else {
	    			userModel.addRequiredAction(RequiredAction.UPDATE_PASSWORD);
	    			throw new ModelException("internalServerError");
	    		}
			} else {
				
				// Update existing registered user.
				success = userDatabase.updateCredentials(user.getEmail(), cred.getValue());
				if (success) {
					log.info("Updated password for user: " + user.getEmail());
					user.setEnabled(true);
				} else {
					log.error("Failed to update password in ACS for user: " + user.getEmail());
					userModel.addRequiredAction(RequiredAction.UPDATE_PASSWORD);
				}
			}
		} catch (UserNotFoundException e) {
            throw new ModelException("internalServerError");
    	}
    	return success;
    }

    @Override
    public void disableCredentialType(RealmModel realm, UserModel user, String credentialType) {
    	// Not Implemented
    }

    @Override
    public Set<String> getDisableableCredentialTypes(RealmModel realm, UserModel user) {
    	// Not Implemented
    	return Collections.emptySet();
    }

    @Override
    public void close() {
    	log.info("MyUserStorageProvider Closed");
    }

    /**
     * Local storage user.
     */
    @Override
    public UserModel getUserById(String keycloakId, RealmModel realm) {
    	log.info("getUserById: " + keycloakId);
    	String email = StorageId.externalId(keycloakId);
		return getUserByEmail(email, realm);
    }

    /**
     * Username is email so use getUserByEmail.
     */
	@Override
    public UserModel getUserByUsername(String username, RealmModel realm) {
		log.info("getUserByUsername: " + username);
        return getUserByEmail(username, realm);
    }

    @Override
    public UserModel getUserByEmail(String email, RealmModel realm) {
    	log.info("getUserByEmail: " + email + " realm: " + realm.getName());
        try {
        	User user = userDatabase.findUserByEmail(email);
        	UserModel userModel = new UserAdapter(session, realm, model, user);
        	return userModel;
		} catch (UserNotFoundException e) {
			log.info("No user found for email: " + email);
			return null;
		}
    }

	@Override
	public UserModel addUser(RealmModel realm, String email) {
		try {
			return new UserAdapter(session, realm, model, userDatabase.findUserByEmail(email));
		} catch (UserNotFoundException e) {
			User user = userDatabase.initUser(email);
			UserModel userModel = new UserAdapter(session, realm, model, user);
			return userModel;
		}
	}
	
	@Override
	public boolean removeUser(RealmModel realm, UserModel userModel) {
		
		userModel.setEnabled(false);
		try {
			User user = userDatabase.findUserByEmail(userModel.getEmail());
			user.setEnabled(false);
			return userDatabase.updateUser(user);
		} catch (UserNotFoundException e) {
			log.error("No user found for removal: " + userModel.getEmail());
			throw new ModelException("internalServerError");
		}
	}

}
