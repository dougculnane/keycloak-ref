# User Management with Keycloak reference SPI implementation

This is the foundation of a Single Sign On, Multi-host, Multi-realm, Role based, User management, Security system that has many configuration features like "Brute Force Detection", "Recaptcha", "Password Policy", "Email verification", "Forgot / reset Password", "One Time Password 2 factor authentication", etc....

```bash
git clone https://github.com/dougculnane/keycloak-ref.git
cd keycloak_ref
mvn clean install
cd jhipster_webapp
docker-compose -f src/main/docker/keycloak.yml up -d
mvn
```

You can now browse http://localhost:8080/ -> register and login.

