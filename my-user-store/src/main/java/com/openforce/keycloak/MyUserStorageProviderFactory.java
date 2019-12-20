package com.openforce.keycloak;

import org.jboss.logging.Logger;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.storage.UserStorageProviderFactory;

/**
 * Factory for registering and creating user provider.
 *
 * @author openFORCE
 */
public class MyUserStorageProviderFactory implements UserStorageProviderFactory<MyUserStorageProvider> {

    private static final Logger logger = Logger.getLogger(MyUserStorageProviderFactory.class);
    UserDatabase userDatabase;

    @Override
    public MyUserStorageProvider create(KeycloakSession session, ComponentModel model) {
        logger.info("Creating Storage Provider: " + getId());
        try {
            this.userDatabase = new UserDatabase();
            MyUserStorageProvider provider = new MyUserStorageProvider(session, model, this.userDatabase);
            return provider;
        } catch (Exception e) {
            logger.error("Create Storage Provider Error: " + e.toString(), e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getId() {
        return "my-user-provider";
    }

}
