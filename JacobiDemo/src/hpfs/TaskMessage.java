package hpfs;

import java.util.concurrent.RecursiveAction;

/**
 *
 * @author Ben Paretzky
 */
public class TaskMessage extends JacobiMessage {

    public TaskMessage(RecursiveAction task, long clientId) {
        super(MSG_TYPE.TASK, clientId, task);
    }

    public RecursiveAction getTask() {
        return (RecursiveAction) data;
    }

}
