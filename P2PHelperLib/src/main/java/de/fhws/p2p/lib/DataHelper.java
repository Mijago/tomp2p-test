package de.fhws.p2p.lib;

import net.tomp2p.dht.FutureGet;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.futures.BaseFutureListener;
import net.tomp2p.peers.Number160;

import java.io.IOException;

/**
 * Used to fetch data from the network.
 */
public class DataHelper {
    public static void tryGet(PeerDHT peer, Number160 number160, DataHelperCallback cb) {
        FutureGet fg = peer.get(number160).all().start().addListener(new BaseFutureListener<FutureGet>() {
            @Override
            public void operationComplete(FutureGet baseFuture) throws IOException, ClassNotFoundException, ClassCastException {
                Object object = baseFuture.dataMap().values().iterator().next().object();
                cb.onDone(object);
            }

            @Override
            public void exceptionCaught(Throwable throwable) throws Exception {
                throwable.printStackTrace();
            }
        });
    }

    public interface DataHelperCallback {
        void onDone(Object o);
    }

}
