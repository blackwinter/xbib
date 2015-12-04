package org.xbib.cluster.operation.heartbeat;

import com.google.auto.service.AutoService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xbib.cluster.Member;
import org.xbib.cluster.OperationContext;
import org.xbib.cluster.operation.Operation;
import org.xbib.cluster.serialize.KryoSerializable;
import org.xbib.cluster.service.InternalService;

@KryoSerializable(id = 3)
@AutoService(KryoSerializable.class)
public class HeartbeatOperation implements Operation<InternalService> {
    private final static Logger logger = LogManager.getLogger(HeartbeatOperation.class);

    Member sender;

    public HeartbeatOperation(Member sender) {
        this.sender = sender;
    }

    @Override
    public void run(InternalService service, OperationContext ctx) {
        Member masterMember = service.getCluster().getMaster();
        if (sender == null) {
            return;
        }
        if (sender.equals(masterMember)) {
            service.getCluster().setLastContactedTimeMaster(System.currentTimeMillis());
        } else {
            logger.trace("got message from a member who thinks he is the master: {0}", sender);
            if (!service.getCluster().getMembers().contains(sender)) {
                logger.trace("it seems this is new master added me in his cluster and" +
                        " it will most probably send changeCluster request to me");
            }
        }
    }
}
