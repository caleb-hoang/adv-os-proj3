Hello! I hope the past week has been well for you.

Here's what I've implemented so far, and what needs to be done.

I've made a lot of changes to my Project 2 code, and haven't been able to test them yet, since SSH to UTD servers is down at the moment. 
My apologies for any bugs I may have left behind.

I've removed any features regarding token-based total message ordering. Since total ordering matters only when two Clients attempt to write to the same object,
I believe that it is easier to handle total ordering in this specific edge case.

Implemented
----------------------------------
All instructions assume each program has been compiled using javac.

Creation of channels between Servers
    1. Use `hostname -I` in the terminal for the Coordinator to get the Coordinator's local IP address
    2. Run `java Coordinator`.

    3. For each Server instance, run `java Server [Coordinator's IP Address] [ID]`. Each Server should have a unique ID from zero to six. totalling seven.
       The Servers and Coordinator will automatically establish communication channels between each server via sockets.

Sending of messages from Server to Server
    1. Create a message object.
	2. Call the send method corresponding to the thread you want to send to.
	   i.e. sendMessage(new Message(timestamp, 0, "Hello, world!"), 1);
	3. The other Server will then respond with either "Received" or "Failed"
	   If "Received" the method will return true. If "Failed" it will return false.


To do:
----------------------------------
Implement communication between Client and Server
    I can handle this - I just wanted to get this out to you as soon as possible. Expect it sometime between Sunday and Tuesday.

Implement ways to open and close channels 
    Likewise, I can also take care of this. Let me know if you do, though.

Implement synchronization between replicas and the separation/reunion of partitions
    This is the main part that I've left for you to do. If you have any questions or need me to change the way certain methods work, let me know.
