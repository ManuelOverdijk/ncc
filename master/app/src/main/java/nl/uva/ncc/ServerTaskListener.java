package nl.uva.ncc;

import com.example.mymodule.app2.Slave;

/**
 * Created by datwelk on 24/06/14.
 */
public interface ServerTaskListener {
    public void onLocationReceived(Slave receivedSlave);
}
