# load-balancer

## Description
A load balancer is a component that, once invoked, distributes incoming requests to registered providers
and returns the result obtained from one of these providers to the caller. \
For simplicity, a provider only returns its ID. 

## NFRs:
* Concurrency-safe
* Blazing fast lookup and routing

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
