/*
 * Copyright (c) 2016 Inocybe and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.falcon.rulecleaner.statemonitor;


import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.yang.gen.v1.urn.falcon.rulecleaner.rule.checker.rev170929.RuleCheckerService;
import org.opendaylight.yang.gen.v1.urn.falcon.rulecleaner.state.monitor.config.rev170929.StateMonitorConfig;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StateMonitorProvider {

    private final static Logger LOG = LoggerFactory.getLogger(StateMonitorProvider.class);

    private final DataBroker dataBroker;
    private final StateMonitorConfig.Cordon cordon;
    private final RuleCheckerService ruleCheckerService;
    private ListenerRegistration<StateMonitor> listenerRegistration = null;

    public StateMonitorProvider(final DataBroker dataBroker,
                                final StateMonitorConfig config,
                                final RuleCheckerService ruleCheckerService) {
        this.dataBroker = dataBroker;
        this.cordon = config.getCordon();
        this.ruleCheckerService = ruleCheckerService;
    }

    public void init() {
        StateMonitor stateMonitor = new StateMonitor(dataBroker, cordon, ruleCheckerService);
        listenerRegistration = stateMonitor.register(dataBroker);
        LOG.info("state monitor initialized.");
    }

    public void close() {
        try {
            listenerRegistration.close();
        } catch (Exception e) {
            LOG.error("Failed to close registration={}", listenerRegistration, e);
        }
        LOG.info("state monitor torn down.", this);
    }
}