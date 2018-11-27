import de.fhws.p2p.lib.PeerMapListenerHelper;
import de.fhws.p2p.lib.PingHelper;

import java.io.IOException;

public class Program {
    public static void main(String[] args) throws IOException, PeerMapListenerHelper.AlreadyRegisteredException {
        DHTNode dhtNode = new DHTNode();
        PingHelper pingHelper = new PingHelper(dhtNode.getPeer());
        pingHelper.start();
    }
}
