package com.openforce.keycloak;

import java.util.Objects;

import org.jboss.logging.Logger;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.adapter.AbstractUserAdapterFederatedStorage;

/**
 * Connect and maps My User with the Keycloak User.
 * 
 * @author openFORCE
 */
public class UserAdapter extends AbstractUserAdapterFederatedStorage {

	private final User user;
	private final String keycloakId;
	private static final Logger log = Logger.getLogger(UserAdapter.class);

	public UserAdapter(KeycloakSession session, RealmModel realm, ComponentModel model, User user) {
		super(session, realm, model);
		this.user = user;
		this.keycloakId = StorageId.keycloakId(model, user.getEmail());
		this.setEnabled(user.isEnabled());
		this.setEmailVerified(user.isEmailVerified());
		if (user.isEmailVerified()) {
			this.removeRequiredAction(RequiredAction.VERIFY_EMAIL);
		} else {
			this.addRequiredAction(RequiredAction.VERIFY_EMAIL);
		}
		if (user.isPasswordSet()) {
			this.removeRequiredAction(RequiredAction.UPDATE_PASSWORD);
		} else {
			this.addRequiredAction(RequiredAction.UPDATE_PASSWORD);
		}
                this.setSingleAttribute(UserModel.LOCALE, user.getLocale());
		this.setGroups();
	}

	/**
	 * Maps the user's groups with a keycloak groups.
	 */
	private void setGroups() {
		if (user.getGroups() != null) {
			for (String group : user.getGroups()) {
				log.info("User " + user.getEmail() + " member of group: " + group);
				if (isGroupExisting(group) && !userHasGroup(group)) {
					this.joinGroup(realm.getGroups().stream()
							.filter(groupModel -> groupModel.getName().equals(group)).findAny().get());
				}
			}
		}
	}

	/**
	 * Checks if the group with the groupName exists in the realm
	 * 
	 * @param groupName
	 * @return true if the group with groupName exists in the realm
	 */
	private boolean isGroupExisting(String groupName) {
		return realm.getGroups().stream().anyMatch(groupModel -> groupModel.getName().equals(groupName));
	}

	/**
	 * Checks if the user in member of the group with the groupName in the realm
	 * 
	 * @param groupName
	 * @return true if the user is member of the group with groupName
	 */
	private boolean userHasGroup(String groupName) {
		return this.getGroups().stream().anyMatch(groupModel -> groupModel.getName().equals(groupName));
	}

	@Override
	public String getId() {
		return keycloakId;
	}

	/**
	 * Forcing email to be username.
	 */
	@Override
	public String getUsername() {
		if (Objects.nonNull(this.user)) {
			return user.getEmail();
		}
		return null;
	}

	@Override
	public void setUsername(String username) {
		user.setEmail(username);
	}

	@Override
	public String getEmail() {
		return user.getEmail();
	}

	@Override
	public void setEmail(String email) {
		super.setEmail(email);
		user.setEmail(email);
	}

	@Override
	public String getFirstName() {
		return user.getFirstName();
	}

	@Override
	public void setFirstName(String firstName) {
		super.setFirstName(firstName);
		user.setFirstName(firstName);
	}

	@Override
	public String getLastName() {
		return user.getLastName();
	}

	@Override
	public void setLastName(String lastName) {
		super.setLastName(lastName);
		user.setLastName(lastName);
	}

}
