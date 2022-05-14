package com.rw.loadbalancer

import com.rw.loadbalancer.registry.RegistrationCallback
import com.rw.loadbalancer.strategy.LoadBalancingStrategy

interface RegistryAwareStrategy : RegistrationCallback, LoadBalancingStrategy
