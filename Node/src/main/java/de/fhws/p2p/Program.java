package de.fhws.p2p;

import de.fhws.p2p.lib.PeerMapListenerHelper;
import de.fhws.p2p.lib.PingHelper;

import java.io.IOException;

public class Program {
    public static void main(String[] args) throws IOException, PeerMapListenerHelper.AlreadyRegisteredException {

        assert(args.length == 3);
        // Usage Initiator: <prog> 1 127.0.0.1 4001
        // Usage Client 1:  <prog> 2 127.0.0.1 4001
        // Usage Client 2:  <prog> 3 127.0.0.1 4002

        int peerId = Integer.parseInt(args[0]);
        DHTNode dhtNode = new DHTNode(
                peerId, // My own Peer ID
                args[1],
                Integer.parseInt(args[2])
        );
        new PingHelper(dhtNode.getPeer()).start();
    }
}
