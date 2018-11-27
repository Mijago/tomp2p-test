package de.fhws.p2p;

import de.fhws.p2p.lib.FHWSResponsibilityListener;
import de.fhws.p2p.lib.PeerMapListenerHelper;
import net.tomp2p.dht.PeerBuilderDHT;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.futures.FutureBootstrap;
import net.tomp2p.p2p.Peer;
import net.tomp2p.p2p.PeerBuilder;
import net.tomp2p.peers.Number160;
import net.tomp2p.replication.IndirectReplication;
import net.tomp2p.storage.Data;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class DHTNode {
    private static final int PORT_BASE = 4000;
    final private PeerDHT peer;

    public DHTNode(int peerId, String targetAddress, int targetPort) throws IOException, PeerMapListenerHelper.AlreadyRegisteredException {

        Number160 myHash = Number160.createHash(peerId);
        Peer builtPeer = new PeerBuilder(myHash).ports(PORT_BASE + peerId).start();
        peer = new PeerBuilderDHT(builtPeer).start();

        new PeerMapListenerHelper(peer).registerListenerToDHT();

        // Bootstrap to the initial peer
        this.bootstrapToTarget(targetAddress, targetPort);

        System.out.println("I am " + peer.peerAddress().peerSocketAddress());

        // Start indirect replication and output messages
        new IndirectReplication(peer)
                .addResponsibilityListener(new FHWSResponsibilityListener(peer).setEnableDataFetching(true))
                .start();


        System.out.println("I store data with intValue " + myHash.intValue());


        // Put some default data
        peer.put(myHash)
                .data(new Data(String.format("peerId=%d; targetAddress=%s; targetPort=%d", peerId, targetAddress, targetPort)))
                .start()
                .awaitUninterruptibly();

        System.out.println("Initialization DONE");
    }

    public PeerDHT getPeer() {
        return peer;
    }

    private void bootstrapToTarget(String targetAddress, int targetPort) throws UnknownHostException {
        FutureBootstrap fb = this.peer.peer().bootstrap().inetAddress(InetAddress.getByName(targetAddress)).ports(targetPort).start();
        fb.awaitUninterruptibly();
        if (fb.isSuccess()) {
            peer.peer().discover().peerAddress(fb.bootstrapTo().iterator().next()).start().awaitUninterruptibly();
        }
    }
}
