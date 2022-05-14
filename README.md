# load-balancer

## Description
A load balancer is a component that, once invoked, distributes incoming requests to registered providers
and returns the result obtained from one of these providers to the caller. \
For simplicity, a provider only returns its ID. 

## Non-Functional Requirements
* Concurrency-safe
* Blazing fast lookup and routing

## Implementation Details
I'm using Kotlin with JUnit 5. This software product is built with Maven and is runnable through the CLI. \
Currently, the LoadBalancer is the entrypoint.

## TO DO
* Move registering providers to a component called Registrar
that shares with the LoadBalancer a Registry of Providers.

## Steps
1. Create a Provider definition.
2. Allow registering providers. Max: 10
3. Randomly select a provider when calling the LoadBalancer.
4. Implement a round-robin selection.
5. Allow including or excluding specific providers.
6. Develop a heart-beat check that discovers if a provider is in good health.
If not, exclude it.
7. Enhance the heart-beat check to re-include a provider if it passes the check 2 consecutive times.
8. Limit the number of parallel requests to a provider and therefore to the loadbalancer. 

---
_Engineered and developed by Raafat_
