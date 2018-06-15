/*
 * Copyright (c) 2017 lixing and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.falcon.rulecleaner.securestate;


import com.google.common.util.concurrent.Futures;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.yang.gen.v1.urn.falcon.rulecleaner.secure.state.rev170929.*;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Future;


public class SecureStateImpl implements SecureStateService {
    private static final Logger LOG = LoggerFactory.getLogger(SecureStateImpl.class);

    private DataBroker dataBroker;

    public SecureStateImpl(DataBroker dataBroker) {
        this.dataBroker = dataBroker;
    }


    @Override
    public Future<RpcResult<SetSecureStateOutput>> setSecureState(SetSecureStateInput input) {
        System.out.println("[SecureState] receive rpc request");
        RpcResultBuilder<SetSecureStateOutput> rpcResultBuilder = null;
        rpcResultBuilder = RpcResultBuilder.failed();
        SetSecureStateOutputBuilder setSecureStateOutputBuilder = new SetSecureStateOutputBuilder();

        ReadWriteTransaction readWriteTransaction = dataBroker.newReadWriteTransaction();
        InstanceIdentifier<SecureState> id = InstanceIdentifier.builder(SecureState.class).build();
        SecureStateBuilder secureStateBuilder = new SecureStateBuilder();
        secureStateBuilder.setLevel(input.getLevel().getIntValue());
        readWriteTransaction.put(LogicalDatastoreType.CONFIGURATION, id, secureStateBuilder.build());
        try {
            readWriteTransaction.submit().checkedGet();
            rpcResultBuilder = RpcResultBuilder.success();
            System.out.println("[SecureState] The secure state is set to " + input.getLevel().getName());
            setSecureStateOutputBuilder.setResult("OK, the secure state is set to " + input.getLevel().getName());
        } catch (TransactionCommitFailedException tcfe) {
            setSecureStateOutputBuilder.setResult("Woops, fail to set secure state");
        }
        rpcResultBuilder.withResult(setSecureStateOutputBuilder.build());
        return Futures.immediateFuture(rpcResultBuilder.build());
    }
}
