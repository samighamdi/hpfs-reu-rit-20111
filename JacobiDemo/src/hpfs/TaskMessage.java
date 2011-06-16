package hpfs;

import java.io.Serializable;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;

/**
 *
 * @author Ben Paretzky
 */
public class TaskMessage extends JacobiMessage {

    public TaskMessage(RecursiveTask<Serializable> task, long clientId) {
        super(MSG_TYPE.TASK, clientId, task);
    }

    public RecursiveTask<Serializable> getTask() {
        return (RecursiveTask<Serializable>) data;
    }

}
