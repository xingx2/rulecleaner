/*
 * Copyright (c) 2017 lixing and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.falcon.rulecleaner.statemonitor;


import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataTreeChangeListener;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.falcon.rulecleaner.rule.checker.rev170929.RuleCheckOutput;
import org.opendaylight.yang.gen.v1.urn.falcon.rulecleaner.rule.checker.rev170929.RuleCheckerService;
import org.opendaylight.yang.gen.v1.urn.falcon.rulecleaner.secure.state.rev170929.SecureState;
import org.opendaylight.yang.gen.v1.urn.falcon.rulecleaner.state.monitor.config.rev170929.StateMonitorConfig;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;


public class StateMonitor implements DataTreeChangeListener<SecureState> {
    private static final Logger LOG = LoggerFactory.getLogger(StateMonitor.class);

    private final DataBroker dataBroker;
    private StateMonitorConfig.Cordon cordon;
    private final RuleCheckerService ruleCheckerService;
    private InstanceIdentifier<SecureState> identifier = InstanceIdentifier.create(SecureState.class);


    public StateMonitor(DataBroker dataBroker, StateMonitorConfig.Cordon cordon, RuleCheckerService ruleCheckerService) {
        this.dataBroker = dataBroker;
        this.cordon = cordon;
        this.ruleCheckerService = ruleCheckerService;
    }

    public ListenerRegistration<StateMonitor> register(DataBroker dataBroker) {
        return dataBroker.registerDataTreeChangeListener(new DataTreeIdentifier<SecureState>(LogicalDatastoreType.CONFIGURATION, identifier), this);
    }

    @Override
    public void onDataTreeChanged(@Nonnull Collection<DataTreeModification<SecureState>> changes) {
        System.out.println("###address-tracker hears Secure-state data tree modification###");
        for (DataTreeModification<SecureState> change : changes) {
            SecureState dataAfter = change.getRootNode().getDataAfter();
            //ReadWriteTransaction readWriteTransaction = dataBroker.newReadWriteTransaction();
            //InstanceIdentifier<SelfDestructSwitch> id = InstanceIdentifier.builder(SelfDestructSwitch.class).build();
            //SelfDestructSwitchBuilder selfDestructSwitchBuilder = new SelfDestructSwitchBuilder();

            assert dataAfter != null;
            if (dataAfter.getLevel() >= cordon.getIntValue()) {
                //selfDestructSwitchBuilder.setSwitch(true);
                System.out.println("beyond the cordon, check rules...");
                final Future<RpcResult<RuleCheckOutput>> rpcResultFuture = ruleCheckerService.ruleCheck();
                try {
                    RuleCheckOutput result = rpcResultFuture.get().getResult();
                    if(result!=null){
                        System.out.println(result.getResult());
                    }
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            } else {
                //selfDestructSwitchBuilder.setSwitch(false);
                System.out.println("under the cordon");
            }
        }
    }
}
