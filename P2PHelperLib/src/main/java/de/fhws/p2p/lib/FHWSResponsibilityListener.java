package de.fhws.p2p.lib;

import net.tomp2p.dht.PeerDHT;
import net.tomp2p.futures.FutureDone;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.replication.ResponsibilityListener;

public class FHWSResponsibilityListener implements ResponsibilityListener {
    private PeerDHT peer = null;

    public FHWSResponsibilityListener() {
        this(null);
    }

    public FHWSResponsibilityListener(PeerDHT peer) {
        this.peer = peer;
    }

    @Override
    public FutureDone<?> meResponsible(Number160 number160) {
        System.out.println("I am now responsible for " + number160.hashCode());

        tryFetchInfo(number160);
        return null;
    }

    @Override
    public FutureDone<?> meResponsible(Number160 number160, PeerAddress peerAddress) {
        // the other node now knows that I have this key
        System.out.println("I sync " + number160.hashCode() + " to " + peerAddress.peerSocketAddress());

        return null;
    }

    @Override
    public FutureDone<?> otherResponsible(Number160 number160, PeerAddress peerAddress) {
        // I know that the other node has this key
        System.out.println("Other peer " + peerAddress.peerSocketAddress() + " is responsible for " + number160.hashCode() + ".");

        tryFetchInfo(number160);
        return null;
    }

    private void tryFetchInfo(Number160 number160) {
        if (this.peer != null) {
            try {
                Thread.sleep(500);

                DataHelper.tryGet(this.peer, number160, obj -> {
                    System.out.println("Value of '" + number160.hashCode() + "' is '" + obj + "' (type: '" + obj.getClass() + "')");
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
