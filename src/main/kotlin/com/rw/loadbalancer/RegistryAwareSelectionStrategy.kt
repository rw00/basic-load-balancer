package com.rw.loadbalancer

import com.rw.loadbalancer.registry.RegistrationUpdatesSubscriber
import com.rw.loadbalancer.strategy.SelectionStrategy

interface RegistryAwareSelectionStrategy : RegistrationUpdatesSubscriber, SelectionStrategy
