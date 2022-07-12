# Description

This is a sample project for demonstrating [[bi-directional]](https://pactflow.io/bi-directional-contract-testing/) contract testing with Pactflow.

This Spring Boot app represents the contract "consumer", requires Java 17 and for the full experience you will need a trial account to create your own Pact broker.
Add the Pact broker token to your GitHub Action secrets and adjust the broker URL in the .github/workflows/main.yml file.

The test uses the "standard consumer driven pacts", and since none of the sample projects get deployed, you will get a fail on the can-i-deploy step.



