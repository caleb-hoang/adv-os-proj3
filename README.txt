Implemented
----------------------------------
All instructions assume each program has been compiled using javac.
For our demonstrations, the Coordinator is usually run on dc10, the Servers on dc20-26, and the clients on dc30-34.

Launching Servers
    1. Use `hostname -I` in the terminal for the Coordinator to get the Coordinator's local IP address
    2. Run `java Coordinator`.

    3. For each Server instance, run `java Server [Coordinator's IP Address] [ID]`. Each Server should have a unique ID from zero to six. totalling seven.
       The Servers and Coordinator will automatically establish communication channels between each server via sockets. The Servers will then automatically
       connect with each other - they will print in console once all connections are established and the program is ready to accept input.

Launching Client operations
    1. Run `java Client [client id] [desired object] [0 (for a write) or 1 (for a read)] [desired value (for writes only)]
    2. The program will automatically execute the write/read; if any replica is unavailable for a write, it will search for the next replica which is.
       It will print "Error!" if the requested object has not been written yet.

Partitioning the Servers
    1. On each Server, once interaction has been enabled, type "createPartition" in the command line. This must be done for each Server instance.
       This will create the partition, and any functionality related to partitions can be tested and executed.
    2. To close the partition, type "closePartition" in the command line. Once again, this must be done for each Server instance.
       This does not cause the partitions to merge - it only reopens communication.
    3. Once all Servers have closed their partitions, type "reconstructPartition" in the command line for any non-isolated replicas.
       This will cause the Servers to all synchronize and send any previously failed messages. Normal functionality will then continue until the next partition.
    4. It is also possible to manually close channels to create a unique partition. However, "closePartition" will not work if you choose to create a partition in this way.