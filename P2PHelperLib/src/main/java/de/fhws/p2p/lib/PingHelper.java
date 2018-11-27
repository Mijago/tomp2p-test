package de.fhws.p2p.lib;

import net.tomp2p.dht.PeerDHT;
import net.tomp2p.futures.BaseFuture;
import net.tomp2p.futures.BaseFutureListener;
import net.tomp2p.peers.PeerAddress;

public class PingHelper {
    private PeerDHT peer;
    private PingHelperThread thread;

    public PingHelper(PeerDHT peer) {
        this.peer = peer;
    }

    public boolean isRunning() {
        return thread != null && thread.isRunning();
    }

    public PeerDHT getPeer() {
        return peer;
    }

    /**
     * Start the thread of this PingHelper.
     * If there is already a thread running, it does nothing.
     */
    public void start() {
        if (this.thread == null || !this.thread.isRunning()) {
            this.thread = new PingHelperThread(peer);
            this.thread.start();
        }
    }

    /**
     * Stop the thread of this PingHelper.
     */
    public void stop() {
        if (this.thread != null && !this.thread.isRunning()) {
            this.thread.indicateStop();
        }
    }

    private class PingHelperThread extends Thread {
        private PeerDHT peer;
        private boolean shouldStop = false;

        PingHelperThread(PeerDHT peer) {
            this.peer = peer;
        }

        boolean isRunning() {
            return !shouldStop;
        }

        void indicateStop() {
            shouldStop = true;
        }

        @Override
        public void run() {
            while (!shouldStop) {
                for (final PeerAddress peerAddress : peer.peerBean().peerMap().all()) {
                    BaseFuture future = peer.peer().ping().peerAddress(peerAddress).tcpPing().start();
                    future.addListener(new BaseFutureListener<BaseFuture>() {
                        public void operationComplete(BaseFuture future) throws Exception {
                            if (future.isSuccess()) {
                                //System.out.println("peer online (TCP):" + peerAddress);
                            } else {
                                System.out.println("peer offline " + peerAddress.peerSocketAddress());
                            }
                        }

                        public void exceptionCaught(Throwable t) throws Exception {
                            System.out.println("exceptionCaught " + t);
                        }
                    });
                }
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
