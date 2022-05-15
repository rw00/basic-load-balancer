# rw-basic-load-balancer

## Project Overview

A load balancer is a component that, when invoked, distributes incoming requests to registered providers
and returns the result obtained from one of these providers to the caller. \
For simplicity, a provider only returns its ID.

## Non-Functional Requirements

* Concurrency-safe
* Blazing fast lookup and routing

## Implementation Details

This software product is built with Maven and is runnable through the CLI. \
It is written in Kotlin based on Java 11.

**Test Coverage:** around 70% (excluding the CLI)

**Technologies:** \
For testing, I'm using JUnit 5 and Awaitility. \
For test assertions, I'm using AssertJ.

`NOTE:` The CLI was developed for convenience only. It was out of scope and is not covered by test automation.

Currently, the LoadBalancer is the entrypoint for managing the cluster and accepting requests.

## Prerequisites

Java 11 or 17 in your environment path.

## Build and Test

Execute this command: `./mvnw clean verify`

## Run

From the command line: `java -jar target/rw-load-balancer-cli.jar` \
Or from IntelliJ: `com.rw.loadbalancer.cli.CommandLineInteractiveOperatorMain.main`

## Architecture Description

The LoadBalancer internally initializes the Registry of Providers. \
The Registry, upon instantiation, starts the HeartBeatChecker. \
On each call, the LoadBalancer delegates the selection of the Provider to the preconfigured SelectionStrategy. \
Upon addition or removal of Providers, the Registry notifies the listener via callback. \
And finally, the LoadBalancer returns results asynchronously.

## TO DO

* Move registering providers to a component called `Registrar` that shares the `Registry` with the `LoadBalancer`.
  Make it the entrypoint for managing the cluster.
* Redesign `HeartBeatChecker` to make it more unit-testable or spy on it with MockK.
* Resurrect the Provider after a configurable x consecutive heart-beat checks (instead of hardcoded 2).
* Rewrite some parts in a more Kotlin-native way.
* Add logging...

## Vague Requirements

* **Excluding a provider:** does it mean unregistered or simply not accepting calls? \
  --> Since there is a limit to the number of registered providers, then this requirement is unclear. \
  --> I'm assuming that an excluded provider is still registered but inactive.
* **Cluster capacity limit:** the requirement itself begins with an assumption. \
  --> Does the loadBalancer select a non-busy provider based on the active requests per provider? \
  (this is very complex to implement) \
  --> I'm assuming the distribution of requests is 100% uniform without any variations in effort load, \
  and therefore the loadBalancer can compute whether the cluster is at maximum capacity by itself. \

## Step-by-Step Requirements

1. Create a Provider definition that handles a request.
2. Allow registering providers. Max: 10
3. Randomly select a provider when calling the LoadBalancer.
4. Implement a round-robin selection algorithm.
5. Allow excluding or including specific providers.
6. Develop a heart-beat check that discovers if a provider is in good health.
   If not, exclude it.
7. Enhance the heart-beat check to re-include a provider if it passes the check 2 consecutive times.
8. Limit the number of parallel requests to the loadBalancer.

---
_Engineered and developed by Raafat_
