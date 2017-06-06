package vt.wurrego;

import vt.wurrego.utils.IPUtils;
import vt.wurrego.utils.Logger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Random;

/**
 * Class for selecting content
 * Created by wurrego on 6/4/17.
 */
public class ContentSelector {

    private String userProvidedName;
    private boolean hasUDPHeader;
    private boolean debugLogging;
    private int debugLevel;
    private String TAG;

    private String sourceIPAddr;
    private String destIPAddr;
    private short sourcePort;
    private short destPort;
    private String contentFileName;

    // Content Descriptor
    private ContentInfo.ContentDescriptors contentDescriptor;

    /**
     * ContentSelector - constructor for selecting content
     * @param cd
     */
    public ContentSelector(ContentInfo.ContentDescriptors cd, int debugLevel) {

        this.hasUDPHeader = cd.udp_packet;
        this.userProvidedName = cd.name;

        // debug parameters
        TAG = " [" + this.getClass().getSimpleName() + " - " + userProvidedName + "] ";
        this.debugLevel = debugLevel;
        debugLogging = false;

        if (debugLevel > 0)
            debugLogging = true;

        if (debugLogging) {   Logger.log( TAG , "- Init" ); }

        // ip header parameters
        this.sourceIPAddr = cd.packet_src_address;
        this.destIPAddr = cd.packet_dst_address;

        // udp header parameters
        this.sourcePort = cd.packet_src_port;
        this.destPort = cd.packet_dst_port;

        // content file
        this.contentFileName = cd.content_file_path;
    }

    /**
     * headerEncapsulator - generates a IPV4/UDP packet with provided data as datagram
     * @return byte[]
     */
    private byte[] headerEncapsulator(byte[] data) {

        // Create the IP header byte buffer
        ByteBuffer ipHeaderBuffer = ByteBuffer.allocateDirect(IPUtils.IP_HEADER_SIZE);

        // Create the UDP header byte buffer
        ByteBuffer udpHeaderBuffer = ByteBuffer.allocateDirect(IPUtils.UDP_HEADER_SIZE);

        /** Standard IPV4 Header 20 bytes **/
            // version (4 bits)                 -- 0x04
            // IHL (4 bits)                     -- 0x05
            ipHeaderBuffer.put((byte)0x45);

            // DSCP (6 bits)                    -- 0
            // ECN (2 bits)
            ipHeaderBuffer.put((byte)0x00);

            // Total Length (2 bytes)
            short totalLength = (short) (IPUtils.IP_HEADER_SIZE + IPUtils.UDP_HEADER_SIZE + (data.length));
            ipHeaderBuffer.putShort(totalLength);

            // Identification (2 bytes)         -- 0
            ipHeaderBuffer.putShort((short)0);

            // Flags (3 bits)
            // Fragment Offset (13 bits)        -- 0
            ipHeaderBuffer.putShort((short)0);

            // TTL (1 byte)                     -- d'256
            ipHeaderBuffer.put((byte)0xFF);

            // Protocol (1 byte)                - d'17 [UDP]
            ipHeaderBuffer.put((byte)0x11);

            // Header Checksum (2 bytes)        - placeholder
            int ipHeaderChkSumPos = ipHeaderBuffer.position();
            ipHeaderBuffer.putShort((short)0);

            // Source IP Address (4 bytes)      - set via input
            InetAddress sourceIP = null;
            byte[] sourceAddress_bytes = null;
            try {
                sourceIP = InetAddress.getByName(sourceIPAddr);
                sourceAddress_bytes = sourceIP.getAddress();
                ipHeaderBuffer.put(sourceAddress_bytes);
            } catch (UnknownHostException e) {
                Logger.log( TAG , "- Error Unknown Source Address." );
            }

            // Destination IP Address (4 bytes) - set via input
            InetAddress destIP = null;
            byte[] destAddress_bytes = null;
            try {
                destIP = InetAddress.getByName(destIPAddr);
                destAddress_bytes = destIP.getAddress();
                ipHeaderBuffer.put(destAddress_bytes);
            } catch (UnknownHostException e) {
                Logger.log( TAG , "- Error Unknown Destination Address." );
            }

            // calculate IP Header checksum
            ipHeaderBuffer.rewind();
            byte[] ipHeaderData = new byte[ipHeaderBuffer.remaining()];
            ipHeaderBuffer.get(ipHeaderData);
            short ipChkSum = IPUtils.IPHeaderChecksum.calculateChecksum(ipHeaderData);

            // add checksum to buffer
            ipHeaderBuffer.putShort(ipHeaderChkSumPos, ipChkSum);

            // copy to byte []
            ipHeaderBuffer.rewind();
            ipHeaderBuffer.get(ipHeaderData);

        /** Standard UDP Header 8 bytes **/

            // Source Port (2 bytes)
            udpHeaderBuffer.putShort(sourcePort);

            // Destination Port (2 bytes)
            udpHeaderBuffer.putShort(destPort);

            // Length (2 bytes)
            short udpLength = (short) (IPUtils.UDP_HEADER_SIZE + (data.length));
            udpHeaderBuffer.putShort(udpLength);

            // Checksum (2 bytes)
            int udpChkSumPos = udpHeaderBuffer.position();
            udpHeaderBuffer.putShort((short)0); // placeholder

            // calclate UDP checksum
            udpHeaderBuffer.rewind();
            byte[] udpHeaderData = new byte[udpHeaderBuffer.remaining()];
            udpHeaderBuffer.get(udpHeaderData);
            short udpChkSum = IPUtils.IPHeaderChecksum.calculateUDPChecksum(sourceAddress_bytes, destAddress_bytes, (byte)0x11, udpLength, udpHeaderData, data);

            // add checksum to buffer
            udpHeaderBuffer.putShort(udpChkSumPos, udpChkSum);

            // copy to byte []
            udpHeaderBuffer.rewind();
            udpHeaderBuffer.get(udpHeaderData);

        /** Encapsulate **/
        byte[] packet = new byte[ipHeaderData.length + udpHeaderData.length + data.length];

        System.arraycopy(ipHeaderData,0,packet,0         ,ipHeaderData.length);
        System.arraycopy(udpHeaderData,0,packet,ipHeaderData.length,udpHeaderData.length);
        System.arraycopy(data,0,packet,udpHeaderData.length,data.length);

        return packet;

    }

    /**
     * dataGenerator_fromFile - generates content from user-provided file
     * @return byte[]
     */
    private byte[] dataGenerator_fromFile(int size) {

        // data buffer
        byte[] data = new byte[size];

        try {
            RandomAccessFile file = new RandomAccessFile(contentFileName,"r");

            // select random position in file to read content of length "size"
            long max_length = file.length();

            // select random content
            Random rGen = new Random();
            int choice = rGen.nextInt((int) (max_length-size));

            // move file pointer to the random position
            file.seek(choice);

            // read content of size into data byte []
            file.read(data);

            file.close();

            if (debugLogging) {   Logger.log( TAG , "- Read " + data.length + " bytes [" + new String(data, StandardCharsets.UTF_8) +"]"  ); }



        } catch (FileNotFoundException e) {
            Logger.log( TAG , "- Error Content File not Found." );
        } catch (IOException e) {
            Logger.log( TAG , "- Error Content file length unknown." );
        }

        return data;

    }

    /**
     * dataGenerator - generates content
     * @return byte[]
     */
    private byte[] dataGenerator_test(int size) {

        // data buffer
        byte[] sendData = new byte[size];

        // content choices
        String content1 = "This is a test message.";
        String content2 = "Wakeup";
        String content3 = "I have a lot to say about the weather AND other InTeresting ToPicS !";
        String content4 = "Testing 1234 ...... Testing 1234 ././././ Chatty Topic #2 #5, %D% R1";
        String content5 = "Another message another day.";

        String contentToSend = "";

        // select random content
        Random rGen = new Random();
        int choice = rGen.nextInt(5) + 1;

        switch (choice) {
            case 1:
                contentToSend = content1;
                break;
            case 2:
                contentToSend = content2;
                break;
            case 3:
                contentToSend = content3;
                break;
            case 4:
                contentToSend = content4;
                break;
            case 5:
                contentToSend = content5;
                break;
            default:
                contentToSend = TAG + "Error - check rGen !";
        }

        sendData = contentToSend.getBytes();
        return sendData;
    }

    /**
     * getContentPacket - generates content and encapsulates into a IPV4/UDP packet
     * @param mtuSize
     * @param varyPacketSize
     * @return byte[]
     */
    public byte[] getContentPacket(int mtuSize, boolean varyPacketSize) {

        int packetSize = mtuSize;

        if (varyPacketSize)
        {
            // select random content
            Random rGen = new Random();
            packetSize = rGen.nextInt(mtuSize) + 1;
        }

        if (hasUDPHeader)
            return headerEncapsulator(dataGenerator_fromFile(packetSize));

        return dataGenerator_fromFile(packetSize);
    }
}
