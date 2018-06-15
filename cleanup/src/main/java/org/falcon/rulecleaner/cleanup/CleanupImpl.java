/*
 * Copyright (c) 2017 lixing and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.falcon.rulecleaner.cleanup;


import com.google.common.base.Optional;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.Futures;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.yang.gen.v1.urn.falcon.rulecleaner.cleanup.rev170929.*;
import org.opendaylight.yang.gen.v1.urn.falcon.rulecleaner.secure.state.rev170929.SecureState;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Future;

public class CleanupImpl implements CleanupService {
    private static final Logger LOG = LoggerFactory.getLogger(CleanupImpl.class);
    private NotificationPublishService notificationPublishService;
    private DataBroker dataBroker;
    private CleanupConfig.Cordon cordon;

    public CleanupImpl(DataBroker dataBroker, NotificationPublishService notificationPublishService, CleanupConfig.Cordon cordon) {
        this.dataBroker = dataBroker;
        this.notificationPublishService = notificationPublishService;
        this.cordon = cordon;
    }

    @Override
    public Future<RpcResult<RuleCleanupOutput>> ruleCleanup() {
        System.out.println("[Cleanup] receive restful request");
        RpcResultBuilder<RuleCleanupOutput> rpcResultBuilder = null;
        rpcResultBuilder = RpcResultBuilder.failed();

        RuleCleanupOutputBuilder ruleCleanupOutputBuilder = new RuleCleanupOutputBuilder();
        ReadOnlyTransaction readOnlyTransaction = dataBroker.newReadOnlyTransaction();
        InstanceIdentifier<SecureState> id = InstanceIdentifier.builder(SecureState.class).build();
        CheckedFuture<Optional<SecureState>, ReadFailedException> checkedFuture = readOnlyTransaction.read(LogicalDatastoreType.CONFIGURATION, id);

        try {
            Optional<SecureState> optional = checkedFuture.checkedGet();
            if (optional.isPresent()) {
                if (optional.get().getLevel() >= cordon.getIntValue()) {
                    NetworkCleanupBuilder networkCleanupBuilder = new NetworkCleanupBuilder();
                    networkCleanupBuilder.setIfCleanup(true);
                    try {
                        notificationPublishService.putNotification(networkCleanupBuilder.build());
                        System.out.println("[Cleanup] publish network cleanup notification.");
                        ruleCleanupOutputBuilder.setResult("Success to publish network cleanup notification.");
                        rpcResultBuilder = RpcResultBuilder.success();

                    } catch (InterruptedException ie) {
                        ruleCleanupOutputBuilder.setResult("Woops, fail to publish network destruct notification.");
                    }
                } else {
                    System.out.println("[Cleanup] We can still make it.");
                    ruleCleanupOutputBuilder.setResult("The secure state is lower than cordon");
                }
            } else {
                System.out.println("Error: Data not found");
            }
        } catch (ReadFailedException e) {
            e.printStackTrace();
            System.out.println("Fail to read secure state");
        }
        rpcResultBuilder.withResult(ruleCleanupOutputBuilder.build());
        return Futures.immediateFuture(rpcResultBuilder.build());
    }
}
