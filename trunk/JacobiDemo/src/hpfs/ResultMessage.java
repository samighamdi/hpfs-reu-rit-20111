package hpfs;

import java.io.Serializable;

/**
 *
 * @author Ben Paretzky
 */
public class ResultMessage extends JacobiMessage {

    public ResultMessage(Serializable result, long clientId) {
        super(MSG_TYPE.TASK, clientId, result);
    }

    public Serializable getResult() {
        return (Serializable) data;
    }

}
