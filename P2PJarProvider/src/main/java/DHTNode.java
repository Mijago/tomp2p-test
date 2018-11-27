import de.fhws.p2p.lib.FHWSResponsibilityListener;
import net.tomp2p.dht.PeerBuilderDHT;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.p2p.Peer;
import net.tomp2p.p2p.PeerBuilder;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.peers.PeerMapChangeListener;
import net.tomp2p.peers.PeerStatistic;
import net.tomp2p.replication.IndirectReplication;
import net.tomp2p.storage.Data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class DHTNode {
    private static final int PORT_BASE = 4000;
    final private PeerDHT peer;

    public DHTNode() throws IOException {

        Number160 myHash = Number160.createHash("provider");
        Peer builtPeer = new PeerBuilder(myHash).ports(4001).start();
        peer = new PeerBuilderDHT(builtPeer).start();

        this.registerBeanChangeListener();

        System.out.println("I am " + peer.peerAddress().peerSocketAddress());

        // Start indirect replication and output messages
        new IndirectReplication(peer)
                .addResponsibilityListener(new FHWSResponsibilityListener(peer))
                .start();


        writeP2PJarToDHT();
        System.out.println("Initialization DONE");

        // Auslesen der Jar aus der DHT
        /*
        Number160 p2pjarhash = Number160.createHash("p2pjar");
        peer.get(p2pjarhash).start().addListener(new BaseFutureListener<FutureGet>() {
            @Override
            public void operationComplete(FutureGet baseFuture) throws Exception {
                byte[] bytes = baseFuture.dataMap().values().iterator().next().toBytes();
                Files.write(Paths.get("P2PHelperLib.jar"), bytes);
            }

            @Override
            public void exceptionCaught(Throwable throwable) throws Exception {
                throwable.printStackTrace();
            }
        });
         //*/
    }

    private void writeP2PJarToDHT() throws IOException {
        Number160 p2pjarhash = Number160.createHash("p2pjar");
        System.out.println("I store data with key " + p2pjarhash.hashCode());


        byte[] jarBytes = Files.readAllBytes(Paths.get("out/artifacts/P2PHelperLib_jar/P2PHelperLib.jar"));
        // Put some default data
        peer.put(p2pjarhash)
                .data(new Data(jarBytes))
                .start()
                .awaitUninterruptibly();
    }

    public PeerDHT getPeer() {
        return peer;
    }

    private void registerBeanChangeListener() {
        peer.peerBean().peerMap().addPeerMapChangeListener(new PeerMapChangeListener() {
            public void peerInserted(PeerAddress peerAddress, boolean verified) {
                if (verified) {
                    System.out.println("Peer inserted: peerAddress=" + peerAddress.peerSocketAddress() + ", verified=" + verified);
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
}
