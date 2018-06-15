/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.falcon.rulecleaner.cleanup;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.yang.gen.v1.urn.falcon.rulecleaner.cleanup.config.rev170929.CleanupConfig;
import org.opendaylight.yang.gen.v1.urn.falcon.rulecleaner.cleanup.config.rev170929.CleanupConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CleanupProvider {

    private final static Logger LOG = LoggerFactory.getLogger(CleanupProvider.class);

    private final DataBroker dataService;

    private final CleanupConfig config;
    private final RpcProviderRegistry rpcProviderRegistry;
    private final NotificationPublishService notificationPublishService;

    private BindingAwareBroker.RpcRegistration<CleanupConfigService> rpcRegistration;

    public CleanupProvider(final DataBroker dataBroker,
                           final CleanupConfig config,
                           NotificationPublishService notificationPublishService,
                           RpcProviderRegistry rpcProviderRegistry) {
        this.dataService = dataBroker;
        this.config = config;
        this.notificationPublishService = notificationPublishService;
        this.rpcProviderRegistry = rpcProviderRegistry;
    }

    public void init() {
        rpcRegistration = rpcProviderRegistry.addRpcImplementation(CleanupConfigService.class, new CleanupImpl(dataService, notificationPublishService, config.getCordon()));

        LOG.info("Cleanup initialized.");
    }

    public void close() throws Exception {
        if (rpcRegistration != null) {
            rpcRegistration.close();
        }
        LOG.info("Cleanup (instance {}) torn down.", this);
    }
}
