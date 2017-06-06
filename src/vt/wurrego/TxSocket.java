package vt.wurrego;

import com.google.common.util.concurrent.RateLimiter;
import vt.wurrego.utils.Logger;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Random;

/**
 * Class for handling sending of data
 * Created by wurrego on 5/7/17.
 */
public class TxSocket extends Thread{

    private boolean running;
    private DatagramSocket txSocket;
    private InetAddress destIP;
    private int destPort;
    private int mtuBytes;
    private int maxPacketsPerSecond;
    private boolean variableRate;
    private boolean debugLogging;
    private int debugLevel;
    private String TAG;
    private String userProvidedName;
    private ContentGenerator parent;
    private int packetsSent_count;
    private boolean varyPacketSize;

    // Content Descriptor
    private ContentInfo.ContentDescriptors contentDescriptor;


    /**
     * TxSocket - constructor for managing UDP socket for transmitting datagrams
     * @param parent
     * @param cd
     * @param debugLevel
     */
    public TxSocket(ContentGenerator parent, ContentInfo.ContentDescriptors cd, int debugLevel) {

        this.userProvidedName = cd.name;
        this.parent = parent;

        // debug parameters
        TAG = " [" + this.getClass().getSimpleName() + " - " + this.getName() + " - " + userProvidedName + "] ";
        this.debugLevel = debugLevel;
        debugLogging = false;

        if (debugLevel > 0)
            debugLogging = true;

        if (debugLogging) {   Logger.log( TAG , "- Init" ); }

        // init
        this.running = true;
        this.mtuBytes = cd.packet_mtu_size_bytes;
        this.varyPacketSize = cd.packet_variable_size;
        this.maxPacketsPerSecond = cd.max_packets_per_second;
        this.variableRate = cd.variable_rate;
        packetsSent_count = 0;

        try {
            txSocket = new DatagramSocket();
            this.destIP = InetAddress.getByName(cd.dest_ip);
            this.destPort = cd.dest_port;

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    @Override
    public void interrupt() {
        if (debugLogging) {  Logger.log( TAG , "- Interrupted" ); }

        this.running = false;

    }

    @Override
    public void run() {

        if (debugLogging) {   Logger.log( TAG , "- Start" ); }

        // rateLimiter will uniformely distribute the packet sends per second
        RateLimiter rateLimiter = RateLimiter.create(maxPacketsPerSecond);


        while(running)
        {
            if (debugLogging) {   Logger.log( TAG , "- Running" ); }

            // acquire rate token
            rateLimiter.acquire();

            // generate data
            byte[] sendData = parent.offerPacket(mtuBytes,varyPacketSize);

            // put data into UDP datagram and send
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, destIP, destPort);
            try {
                if (txSocket != null ) {
                    if (debugLogging) {   Logger.log( TAG , "- Sending Packet" ); }

                    txSocket.send(sendPacket);
                    packetsSent_count = packetsSent_count + 1;
                    parent.increment_PacketsSent();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (variableRate) {

                Random random = new Random();
                double newRate = random.nextDouble() * maxPacketsPerSecond;
                rateLimiter.setRate(newRate);
            }
        }


        txSocket.close();

        if (debugLogging) {   Logger.log( TAG , "- Finished" ); }
    }



    /** Getters / Setters **/

    public InetAddress getDestIP() {
        return destIP;
    }

    public void setDestIP(InetAddress destIP) {
        this.destIP = destIP;
    }

    public int getDestPort() {
        return destPort;
    }

    public void setDestPort(int destPort) {
        this.destPort = destPort;
    }

    public int getMtuBytes() {
        return mtuBytes;
    }

    public void setMtuBytes(int mtuBytes) {
        this.mtuBytes = mtuBytes;
    }

    public int getMaxPacketsPerSecond() {
        return maxPacketsPerSecond;
    }

    public void setMaxPacketsPerSecond(int maxPacketsPerSecond) {
        this.maxPacketsPerSecond = maxPacketsPerSecond;
    }

    public boolean isVariableRate() {
        return variableRate;
    }

    public void setVariableRate(boolean variableRate) {
        this.variableRate = variableRate;
    }
}
