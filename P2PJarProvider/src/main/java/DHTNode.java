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
import java.nio.file.Files;
import java.nio.file.Paths;

public class DHTNode {
    private static final int PORT_BASE = 4000;
    final private PeerDHT peer;

    public DHTNode() throws IOException, PeerMapListenerHelper.AlreadyRegisteredException {

        Number160 myHash = Number160.createHash(1);
        Peer builtPeer = new PeerBuilder(myHash).ports(4001).start();
        peer = new PeerBuilderDHT(builtPeer).start();

        new PeerMapListenerHelper(peer).registerListenerToDHT();

        bootstrapToTarget("127.0.0.1", 4001);

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

        byte[] jarBytes = Files.readAllBytes(Paths.get("out/artifacts/P2PHelperLib_jar/P2PHelperLib.jar"));
        // Put some default data
        peer.put(p2pjarhash)
                .data(new Data(jarBytes))
                .start()
                .awaitUninterruptibly();

        System.out.println("I stored the 'P2PHelperLib.jar' with the key 'p2pjar'. IntValue: '" + p2pjarhash.intValue() + "'");
    }

    private void bootstrapToTarget(String targetAddress, int targetPort) throws UnknownHostException {
        FutureBootstrap fb = this.peer.peer().bootstrap().inetAddress(InetAddress.getByName(targetAddress)).ports(targetPort).start();
        fb.awaitUninterruptibly();
        if (fb.isSuccess()) {
            peer.peer().discover().peerAddress(fb.bootstrapTo().iterator().next()).start().awaitUninterruptibly();
        }
    }

    public PeerDHT getPeer() {
        return peer;
    }
}
