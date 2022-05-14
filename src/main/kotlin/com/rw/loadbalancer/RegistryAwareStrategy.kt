package com.rw.loadbalancer

import com.rw.loadbalancer.registry.ProviderRegistrationSubscriber
import com.rw.loadbalancer.strategy.SelectionStrategy

interface RegistryAwareStrategy : ProviderRegistrationSubscriber, SelectionStrategy
