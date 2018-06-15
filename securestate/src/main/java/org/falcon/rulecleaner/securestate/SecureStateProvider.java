/*
 * Copyright (c) 2016 Inocybe and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.falcon.rulecleaner.securestate;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.yang.gen.v1.urn.falcon.rulecleaner.secure.state.rev170929.SecureStateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SecureStateProvider {
    private final static Logger LOG = LoggerFactory.getLogger(SecureStateProvider.class);

    private final DataBroker dataBroker;
    private final RpcProviderRegistry rpcProviderRegistry;
    private BindingAwareBroker.RpcRegistration<SecureStateService> serviceRpcRegistration;

    public SecureStateProvider(final DataBroker dataBroker,
                               RpcProviderRegistry rpcProviderRegistry) {
        this.dataBroker = dataBroker;
        this.rpcProviderRegistry = rpcProviderRegistry;
    }

    public void init() {
        serviceRpcRegistration = rpcProviderRegistry.addRpcImplementation(SecureStateService.class, new SecureStateImpl(dataBroker));
        SecureStateImpl secureStateImpl = new SecureStateImpl(dataBroker);
        LOG.info("SecureState initialized.");
    }

    public void close() throws Exception {
        if (serviceRpcRegistration != null) {
            serviceRpcRegistration.close();
        }
        LOG.info("SecureState (instance {}) torn down.", this);
    }
}
