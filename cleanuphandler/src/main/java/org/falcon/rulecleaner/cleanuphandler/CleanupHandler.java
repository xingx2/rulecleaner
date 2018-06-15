/*
 * Copyright (c) 2017 lixing and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.falcon.rulecleaner.cleanuphandler;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.CheckedFuture;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.yang.gen.v1.urn.falcon.rulecleaner.cleanup.rev170929.CleanupListener;
import org.opendaylight.yang.gen.v1.urn.falcon.rulecleaner.cleanup.rev170929.NetworkCleanup;
import org.opendaylight.yang.gen.v1.urn.falcon.rulecleaner.rule.checker.rev170929.FaultySwitches;
import org.opendaylight.yang.gen.v1.urn.falcon.rulecleaner.rule.checker.rev170929.FaultySwitchesBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;


public class CleanupHandler implements CleanupListener {
    private static final Logger LOG = LoggerFactory.getLogger(CleanupHandler.class);
    private DataBroker dataBroker;

    public CleanupHandler(DataBroker dataBroker) {
        this.dataBroker = dataBroker;
    }


    @Override
    public void onNetworkCleanup(NetworkCleanup notification) {
        System.out.println("[CleanupHandler] receive network-cleanup notification");
        if (notification.isIfCleanup()) {
            ReadOnlyTransaction readOnlyTransaction = dataBroker.newReadOnlyTransaction();

            InstanceIdentifier<FaultySwitches> id = InstanceIdentifier.builder(FaultySwitches.class).build();
            CheckedFuture<Optional<FaultySwitches>, ReadFailedException> checkedFuture = readOnlyTransaction.read(LogicalDatastoreType.CONFIGURATION, id);
            ReadWriteTransaction readWriteTransaction = dataBroker.newReadWriteTransaction();
            try {
                Optional<FaultySwitches> optional = checkedFuture.checkedGet();
                if (optional.isPresent()) {
                    if (optional.get().getId().size() == 0) {
                        System.out.println("[CleanupHandler] there is no faulty switch...");
                        return;
                    }
                    for (String faultySwitch : optional.get().getId()) {
                        System.out.println("[CleanupHandler] cleanup rules in switch: " + faultySwitch + " ...");
                    }
                    List<String> emptyFaultySwitchList = new ArrayList<>();
                    FaultySwitchesBuilder faultySwitchesBuilder = new FaultySwitchesBuilder();
                    faultySwitchesBuilder.setId(emptyFaultySwitchList);
                    readWriteTransaction.put(LogicalDatastoreType.CONFIGURATION, id, faultySwitchesBuilder.build());
                    try {
                        readWriteTransaction.submit().checkedGet();
                    } catch (TransactionCommitFailedException tcfe) {
                        System.out.println("Woops, fail to empty faulty switch list");
                    }
                } else {
                    System.out.println("Error: Data not found");
                }
            } catch (ReadFailedException e) {
                e.printStackTrace();
                System.out.println("Fail to read faulty switches");
            }
        } else {
            LOG.debug("Error: bad notification");
        }
    }
}
