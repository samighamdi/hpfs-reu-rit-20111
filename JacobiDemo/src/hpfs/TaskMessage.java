package hpfs;

import java.util.concurrent.RecursiveTask;

/**
 *
 * @author Ben Paretzky
 */
public class TaskMessage extends JacobiMessage {

    public TaskMessage(RecursiveTask task, long clientId) {
        super(MSG_TYPE.TASK, clientId, task);
    }

    public RecursiveTask getTask() {
        return (RecursiveTask) data;
    }

}
