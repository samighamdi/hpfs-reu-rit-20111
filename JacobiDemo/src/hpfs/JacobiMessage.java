package hpfs;

import java.io.Serializable;

/**
 *
 * @author Ben Paretzky
 */
public class JacobiMessage implements Serializable {

    public static enum MSG_TYPE { TASK, RESULT, QUERY, ANSWER };
    /*
     * Task     RecursiveTask<T>
     * Result   T extends Object
     * Query    Monitor stuff TBD
     * Answer   Monitor stuff TBD
     */

    public final MSG_TYPE msgType;
    public final long clientId;
    public final Serializable data;

    public JacobiMessage(MSG_TYPE msgType, long clientId, Serializable data) {
        this.msgType = msgType;
        this.clientId = clientId;
        this.data = data;        
    }

}
