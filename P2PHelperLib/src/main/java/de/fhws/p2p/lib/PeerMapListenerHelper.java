package de.fhws.p2p.lib;

import net.tomp2p.dht.PeerDHT;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.peers.PeerMapChangeListener;
import net.tomp2p.peers.PeerStatistic;

public class PeerMapListenerHelper {
    private PeerDHT peer;
    private boolean registered = false;

    private PeerChangeCallback cbInserted;
    private PeerChangeCallback cbRemoved;
    private PeerChangeCallback cbUpdated;

    public PeerMapListenerHelper(PeerDHT peer) {
        this.peer = peer;

        cbInserted = peerAddress -> System.out.println("New Peer inserted to the DHT: peerAddress=" + peerAddress.peerSocketAddress());
        cbRemoved = peerAddress -> System.out.println("Peer removed from the DHT: peerAddress=" + peerAddress.peerSocketAddress());
    }

    /**
     * Registers a new callback into the given peerDHT.
     * The callback is a listener that enables callbacks when a new peer connects to the network, a peer is
     * removed from the network or a peer is updated.
     *
     * @throws AlreadyRegisteredException If the function is called more than once.
     */
    public void registerListenerToDHT() throws AlreadyRegisteredException {
        if (registered)
            throw new AlreadyRegisteredException();

        peer.peerBean().peerMap().addPeerMapChangeListener(new PeerMapChangeListener() {
            public void peerInserted(PeerAddress peerAddress, boolean verified) {
                if (verified && cbInserted != null) {
                    cbInserted.onChange(peerAddress);
                }
            }

            public void peerRemoved(PeerAddress peerAddress, PeerStatistic peerStatistic) {
                if (cbRemoved != null)
                    cbRemoved.onChange(peerAddress);
            }

            public void peerUpdated(PeerAddress peerAddress, PeerStatistic peerStatistic) {
                // Get's quite spammy.
                if (cbUpdated != null)
                    cbUpdated.onChange(peerAddress);
            }
        });
        registered = true;
    }

    /**
     * @return True, if registerListenerToDHT has been called.
     */
    public boolean isRegistered() {
        return registered;
    }

    public PeerChangeCallback getInsertedCallback() {
        return cbInserted;
    }

    /**
     * Set the callback that should be called when a new peer is inserted into the network.
     *
     * @param cbInserted The new callback.
     */
    public void setInsertedCallback(PeerChangeCallback cbInserted) {
        this.cbInserted = cbInserted;
    }

    public PeerChangeCallback getRemoveCallback() {
        return cbRemoved;
    }

    /**
     * Set the callback that should be called when an existing peer is removed from the network.
     *
     * @param cbRemoved The new callback.
     */
    public void setRemovedCallback(PeerChangeCallback cbRemoved) {
        this.cbRemoved = cbRemoved;
    }

    public PeerChangeCallback getUpdateCallback() {
        return cbUpdated;
    }

    /**
     * Set the callback that should be called when an existing peer in the network is updated.
     *
     * @param cbUpdated The new callback.
     */
    public void setUpdateCallback(PeerChangeCallback cbUpdated) {
        this.cbUpdated = cbUpdated;
    }

    public interface PeerChangeCallback {
        void onChange(PeerAddress peerAddress);
    }

    public class AlreadyRegisteredException extends Exception {
    }

}
