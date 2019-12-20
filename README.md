# User Management with Keycloak

## The Problem: isConvenient() != secure && MyImpl != robust

The most challenging thing to manage in computers systems is oftern the users of those system... As a user of many computing systems manageing user accounts to thoses systems is also a challenge. So how can we make access convenient as well and secure and robust?

The computing standards that solve this problem best seams to be OAuth and OpenID Connect (OIDC).  Programming these standards and making them secure against the creative attacks users and annonimous users develop is a continuous struggle.  The task and its complexity grows at a rate faster than knoweldge and experiance aquired about it.

## Solution: Keycloak

"Keycloak is an open source Identity and Access Management solution aimed at modern applications and services. It makes it easy to secure applications and services with little to no code."

REF: https://www.keycloak.org/about.html

## User Stores

Keycloak will store users for you or allow you to use GitHub, Google, Facebook, LinkedIN... as identity providers. If you have users stored in a LDAP you just need to add and configure our user federation provider.  Then you can consentrate on implementing OAuth to secure your applicaitons and services.

If you have your own database storing your users and want to use keycloak to manage the security of your web applications you can implement the user provider interfaces with a little code.

## Lots of code

At openFORCE we like code so here is an example Jhipster monolithic web appliciation with keycloak integration and custom user provider implementation.


```bash
git clone https://github.com/dougculnane/keycloak-ref.git
cd keycloak_ref
mvn clean install
cd jhipster_webapp
docker-compose -f src/main/docker/app.yml up -d
mvn
```

You can now browse http://localhost:8080/ -> register and login.

We have glossed over a few details ( which you will find in the code ;-) ) but this is the foundation of a Single Sign On, Multi-host, Multi-realm, Role based, User management, Security system that has many configuration features like "Brute Force Detection", "Recaptcha", "Password Policy", "Email verification", "Forgot / reset Password", "One Time Password 2 factor authentication", etc....

