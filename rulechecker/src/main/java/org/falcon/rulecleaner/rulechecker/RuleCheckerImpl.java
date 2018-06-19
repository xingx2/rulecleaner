/*
 * Copyright (c) 2017 lixing and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.falcon.rulecleaner.rulechecker;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.Futures;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.binding.api.ReadWriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.controller.md.sal.common.api.data.TransactionCommitFailedException;
import org.opendaylight.yang.gen.v1.urn.falcon.rulecleaner.rule.checker.rev170929.*;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NetworkTopology;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;


public class RuleCheckerImpl implements RuleCheckerService {

    private static final Logger LOG = LoggerFactory.getLogger(RuleCheckerImpl.class);


    private DataBroker dataBroker;

    public RuleCheckerImpl(DataBroker dataBroker) {
        this.dataBroker = dataBroker;
    }


    public List<String> probeTesting() {
        System.out.println("[ProbeTesting] search faulty switches");
        List<String> faultySwitches = new ArrayList<>();

        ReadOnlyTransaction readOnlyTransaction = dataBroker.newReadOnlyTransaction();
        InstanceIdentifier<NetworkTopology> id = InstanceIdentifier.builder(NetworkTopology.class).build();
        CheckedFuture<Optional<NetworkTopology>, ReadFailedException> checkedFuture = readOnlyTransaction.read(LogicalDatastoreType.OPERATIONAL, id);

        try {
            Optional<NetworkTopology> optional = checkedFuture.checkedGet();
            if (optional.isPresent()) {
                int switchNum = optional.get().getTopology().get(0).getNode().size();
                Thread.sleep(switchNum);
                int faultyNum = (int) (Math.random() * switchNum);
                if (faultyNum == 0 && switchNum > 0) faultyNum = 1;
                for (int i = 0; i < faultyNum; i++) {
                    String data = optional.get().getTopology().get(0).getNode().get(i).getKey().getNodeId().getValue();
                    faultySwitches.add(data);
                }
            } else {
                System.out.println("Error: Data not found");
            }
        } catch (ReadFailedException e) {
            e.printStackTrace();
            System.out.println("Fail to read faulty switches");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return faultySwitches;
    }

    @Override
    public Future<RpcResult<RuleCheckOutput>> ruleCheck() {
        System.out.println("[RuleChecker] receive rpc request");
        RpcResultBuilder<RuleCheckOutput> rpcResultBuilder = null;
        rpcResultBuilder = RpcResultBuilder.failed();
        RuleCheckOutputBuilder ruleCheckOutputBuilder = new RuleCheckOutputBuilder();

        ReadWriteTransaction readWriteTransaction = dataBroker.newReadWriteTransaction();
        InstanceIdentifier<FaultySwitches> id = InstanceIdentifier.builder(FaultySwitches.class).build();
        FaultySwitchesBuilder faultySwitchesBuilder = new FaultySwitchesBuilder();
        List<String> faultySwitches = probeTesting();
        faultySwitchesBuilder.setId(faultySwitches);
        readWriteTransaction.put(LogicalDatastoreType.CONFIGURATION, id, faultySwitchesBuilder.build());
        try {
            readWriteTransaction.submit().checkedGet();
            rpcResultBuilder = RpcResultBuilder.success();
            if (faultySwitches.size() > 1)
                ruleCheckOutputBuilder.setResult("find " + faultySwitches.size() + " faulty switches");
            else
                ruleCheckOutputBuilder.setResult("find " + faultySwitches.size() + " faulty switch");
            System.out.println("[RuleChecker] Rule checking finished");
        } catch (TransactionCommitFailedException tcfe) {
            ruleCheckOutputBuilder.setResult("Woops, fail to checking rules");
        }
        rpcResultBuilder.withResult(ruleCheckOutputBuilder.build());
        return Futures.immediateFuture(rpcResultBuilder.build());
    }
}
