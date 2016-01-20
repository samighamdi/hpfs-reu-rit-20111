# What we do #

Our basic computation scheme goes like this:

Frontend (the server) sends a slice of the image to backend nodes (clients) in the form of a RecursiveAction

Clients compute the next frame of the Jacobi relaxation, and send both the max temperature in their slice, and the slice itself (separately) back to the frontend. The slice has to be sent for the server to prepare the next frame.

Once the server has all of the max temps, it finds the highest of them and sends it out to all the clients so that they can prepare the actual image (with colors scaled to the max temp)

The clients compute the colors for their image slices, and send the image to the server. Once the server receives all the image slices, it stitches them together, sends the full-size image to each mobile device, and moves to the next frame of computation.

# How we do #

Presently, 2 modes of communication; Task and Result. (There are two more but they're for monitoring the server state -- TODO after we get computation online). These are implemented as subclasses of JacobiMessage, and processed in a inner class of the JacobiClient/Server classes. Tasks are RecursiveActions that can be immediately executed on backend nodes. A Result can be any Serializable object - it'll be used for sending: the max temp local to each client; the array slices; the global max temp; and the final image slices from each client.