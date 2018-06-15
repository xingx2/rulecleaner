/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.falcon.rulecleaner.cleanuphandler;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.yangtools.concepts.Registration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CleanupHandlerProvider {

    private final static Logger LOG = LoggerFactory.getLogger(CleanupHandlerProvider.class);
    private final DataBroker dataService;
    private final NotificationProviderService notificationService;
    private Registration networkCleanupReg = null;


    public CleanupHandlerProvider(final DataBroker dataBroker,
                                  final NotificationProviderService notificationService) {
        this.dataService = dataBroker;
        this.notificationService = notificationService;
    }

    public void init() {
        CleanupHandler cleanupHandler = new CleanupHandler(dataService);
        networkCleanupReg = notificationService.registerNotificationListener(cleanupHandler);
        LOG.info("CleanupHandler initialized.");
    }

    public void close() throws Exception {
        if (networkCleanupReg != null) {
            networkCleanupReg.close();
        }
        LOG.info("CleanupHandler (instance {}) torn down.", this);
    }

}
