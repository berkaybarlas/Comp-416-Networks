package project2.transport;

import project2.main.Main;
import project2.network.Network;
import project2.network.Packet;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the transport layer.
 */
public class Transport {

    // The ports assigned to the receiver and the sender.
    public static final int RECEIVER_PORT = 8000;
    public static final int SENDER_PORT = 8001;

    // This flag is true when this transport layer is associated with the
    // sender, and it is false when it is associated with the receiver.
    private final boolean isSender;

    // The network layer, i.e., underlay.
    private final Network network;

    // Constructs the transport layer over the given network layer.
    public Transport(boolean isSender, Network network) {
        this.isSender = isSender;
        this.network = network;
    }

    /**
     * Receives the packets from the sender with Stop and Wait ARQ.
     * @return the ordered list of packets.
     */
    public Packet[] receiveWithARQ() {
        // For your reference, here is an implementation WITHOUT ARQ. You must change this implementation
        // and implement the Stop-and-Wait ARQ protocol.
        List<Packet> packets = new ArrayList<>();
        while(true) {
            Packet p = receivePacket(10000);
            // Stop receiving either on timeout or when we received the last message packet.
            if(p.timedOut) break;
            if (packets.isEmpty() || packets.get(packets.size() - 1).sequenceNumber != p.sequenceNumber)
            {
                packets.add(p);
                if(p.lastPacket) {
                    sendAck(p.sequenceNumber, true);
                    break;
                }
            }
            else{
                if(p.lastPacket) {
                    sendAck(p.sequenceNumber, true);
                }
            }
            sendAck(p.sequenceNumber, false);
        }
        Packet[] pks = new Packet[packets.size()];
        for(int i = 0; i < pks.length; i++) pks[i] = packets.get(i);
        return pks;
    }

    /**
     * Sends the packets to the receiver with Stop and Wait ARQ.
     * @param packets the packets to send.
     */
    public void sendWithARQ(Packet[] packets) {
        int timeout = 1000;
        // For your reference, here is an implementation WITHOUT ARQ. You must change this implementation
        // and implement the Stop-and-Wait ARQ protocol.
        for(int i = 0; i < packets.length; i++) {
            sendMsgPacket(i % 2, i == packets.length-1, packets[i]);
            while(true) {
                Packet ack = receivePacket(timeout);
                if(ack.timedOut){
                    sendMsgPacket(i % 2, i == packets.length-1, packets[i]);
                }
                else {
                    break;
                }
            }
        }
    }

    /**
     * Sends a message packet to the other process.
     * @param sequenceNumber the sequence number of
     * @param lastPacket whether this packet is the last packet for the original message.
     * @param packet the packet itself.
     */
    private void sendMsgPacket(int sequenceNumber, boolean lastPacket, Packet packet) {
        packet.ack = false;
        packet.sequenceNumber = sequenceNumber;
        packet.lastPacket = lastPacket;
        packet.targetPort = (isSender) ? RECEIVER_PORT : SENDER_PORT;
        network.send(packet);
    }

    /**
     * Sends an acknowledgement packet to the other process.
     * @param sequenceNumber the sequence number for the acknowledgement
     * @param lastAck should be set to true when the acknowledgement is for the last packet.
     */
    private void sendAck(int sequenceNumber, boolean lastAck) {
        Packet packet = new Packet(null);
        packet.ack = true;
        packet.sequenceNumber = sequenceNumber;
        packet.lastPacket = lastAck;
        packet.targetPort = (isSender) ? RECEIVER_PORT : SENDER_PORT;
        network.send(packet);
    }

    /**
     * Receives a packet from the other process.
     * @param timeout the duration in which the packet should be received. Otherwise, an
     *                empty packet with the timedOut flag set to true will be returned.
     * @return the received packet.
     */
    private Packet receivePacket(int timeout) {
        Packet p;
        try {
            p = network.receive(timeout);
        } catch (SocketTimeoutException e) {
            if(Main.DEBUG) System.out.println("[Transport.java] Timeout occurred at the "
                    + ((isSender) ? "sender" : "receiver") + ".");
            // Return an empty packet denoting that a timeout has occurred.
            p = new Packet(null);
            p.timedOut = true;
            return p;
        }
        return p;
    }
}
