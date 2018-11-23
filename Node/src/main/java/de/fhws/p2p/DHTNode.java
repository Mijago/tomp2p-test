package de.fhws.p2p;

import net.tomp2p.dht.FutureGet;
import net.tomp2p.dht.PeerBuilderDHT;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.futures.BaseFuture;
import net.tomp2p.futures.BaseFutureListener;
import net.tomp2p.futures.FutureBootstrap;
import net.tomp2p.futures.FutureDone;
import net.tomp2p.p2p.Peer;
import net.tomp2p.p2p.PeerBuilder;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.peers.PeerMapChangeListener;
import net.tomp2p.peers.PeerStatistic;
import net.tomp2p.replication.IndirectReplication;
import net.tomp2p.replication.ResponsibilityListener;
import net.tomp2p.storage.Data;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class DHTNode {
    private static final int PORT_BASE = 4000;
    final private PeerDHT peer;

    public DHTNode(int peerId, String targetAddress, int targetPort) throws IOException {

        Number160 myHash = Number160.createHash(peerId);
        Peer builtPeer = new PeerBuilder(myHash).ports(PORT_BASE + peerId).start();
        peer = new PeerBuilderDHT(builtPeer).start();

        this.registerBeanChangeListener();
        // Bootstrap to the initial peer
        this.bootstrap(targetAddress, targetPort);

        System.out.println("I am " + peer.peerAddress().peerSocketAddress());

        // Start indirect replication and output messages
        new IndirectReplication(peer)
                .addResponsibilityListener(new ResponsibilityListener() {
                    @Override
                    public FutureDone<?> meResponsible(Number160 number160) {
                        System.out.println("I now responsible for " + number160.hashCode());

                        try {
                            Thread.sleep(500);
                            tryGetData(number160);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
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
                        try {
                            Thread.sleep(500);
                            tryGetData(number160);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        return null;
                    }
                })
                .start();


        System.out.println("I store data with key " + myHash.hashCode());


        // Put some default data
        peer.put(myHash)
                .data(new Data(String.format("peerId=%d; targetAddress=%s; targetPort=%d", peerId, targetAddress, targetPort)))
                .start()
                .awaitUninterruptibly();

        System.out.println("Initialization DONE");
    }

    private void tryGetData(Number160 number160) {

        FutureGet fg = peer.get(number160).all().start().addListener(new BaseFutureListener<FutureGet>() {
            @Override
            public void operationComplete(FutureGet baseFuture) throws Exception {

                String s = baseFuture.dataMap().values().iterator().next().object().toString();
                System.out.println(number160.hashCode() + " contains: '" + s + "'");

            }

            @Override
            public void exceptionCaught(Throwable throwable) throws Exception {
                throwable.printStackTrace();
            }
        });
    }

    private void bootstrap(String targetAddress, int targetPort) throws UnknownHostException {
        FutureBootstrap fb = this.peer.peer().bootstrap().inetAddress(InetAddress.getByName(targetAddress)).ports(targetPort).start();
        fb.awaitUninterruptibly();
        if (fb.isSuccess()) {
            peer.peer().discover().peerAddress(fb.bootstrapTo().iterator().next()).start().awaitUninterruptibly();
        }
    }

    private void registerBeanChangeListener() {
        peer.peerBean().peerMap().addPeerMapChangeListener(new PeerMapChangeListener() {
            public void peerInserted(PeerAddress peerAddress, boolean b) {
                if (b) {
                    System.out.println("Peer inserted: peerAddress=" + peerAddress.peerSocketAddress() + ", verified=" + b);
                }
            }

            public void peerRemoved(PeerAddress peerAddress, PeerStatistic peerStatistic) {
                System.out.println("Peer removed: peerAddress=" + peerAddress.peerSocketAddress() + ", peerStatistics=" + peerStatistic);
            }

            public void peerUpdated(PeerAddress peerAddress, PeerStatistic peerStatistic) {
                // Get's quite spammy.
                // System.out.println("Peer updated: peerAddress=" + peerAddress + ", peerStatistics=" + peerStatistic);
            }
        });
    }


    // Ping all peers to detect removal
    public void run() {
        for (; ; ) {
            // Optional pinging
            boolean pingPeers = true;
            if (pingPeers) {
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
