/*
 * Copyright (c) 2017 lixing and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.falcon.rulecleaner.rulechecker;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.yang.gen.v1.urn.falcon.rulecleaner.rule.checker.rev170929.RuleCheckerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RuleCheckerProvider {
    private final static Logger LOG = LoggerFactory.getLogger(RuleCheckerProvider.class);
    private final DataBroker dataBroker;
    private final RpcProviderRegistry rpcProviderRegistry;
    private BindingAwareBroker.RpcRegistration<RuleCheckerService> serviceRpcRegistration;

    public RuleCheckerProvider(final DataBroker dataBroker,
                               RpcProviderRegistry rpcProviderRegistry) {
        this.dataBroker = dataBroker;
        this.rpcProviderRegistry = rpcProviderRegistry;
    }

    public void init() {
        serviceRpcRegistration = rpcProviderRegistry.addRpcImplementation(RuleCheckerService.class, new RuleCheckerImpl(dataBroker));
        LOG.info("RuleChecker initialized.");
    }

    public void close() throws Exception {
        if (serviceRpcRegistration != null) {
            serviceRpcRegistration.close();
        }
        LOG.info("RuleChecker (instance {}) torn down.", this);
    }
}
