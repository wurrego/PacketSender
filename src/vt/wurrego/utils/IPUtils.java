package vt.wurrego.utils;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by wurrego on 6/4/17.
 */
public class IPUtils {
    public static final int IP_HEADER_SIZE = 20;
    public static final int UDP_HEADER_SIZE = 8;


    public static class IPHeaderChecksum {

        /**
         * Calculate the Internet Checksum of a buffer (RFC 1071 - http://www.faqs.org/rfcs/rfc1071.html)
         * Algorithm is
         * 1) apply a 16-bit 1's complement sum over all octets (adjacent 8-bit pairs [A,B], final odd length is [A,0])
         * 2) apply 1's complement to this final sum
         *
         * Notes:
         * 1's complement is bitwise NOT of positive value.
         * Ensure that any carry bits are added back to avoid off-by-one errors
         *
         *
         * @param sourceIP - source IPv4 address
         * @param destIP - destination IPv4 address
         * @param protocol
         * @param udpLength
         * @param udpHeader
         * @param data
         * @return The checksum
         */
        public static short calculateUDPChecksum(byte[] sourceIP, byte[] destIP, byte protocol, short udpLength, byte[] udpHeader, byte[] data)
        {
            /** psuedo header for checksum calculation **/
            ByteBuffer psuedoHeaderBuffer = ByteBuffer.allocateDirect(sourceIP.length + destIP.length + 1 + 1 + udpLength + udpHeader.length + data.length);

            // source IPv4 Address (4 bytes)
            psuedoHeaderBuffer.put(sourceIP);

            // dest IPv4 Addss (4 bytes)
            psuedoHeaderBuffer.put(destIP);

            // zeroes (1 byte)
            psuedoHeaderBuffer.put((byte)0x00);

            // protocol (1 byte)
            psuedoHeaderBuffer.put(protocol);

            // udp length (2 bytes)
            psuedoHeaderBuffer.putShort(udpLength);

            // udp header (8 bytes)
            psuedoHeaderBuffer.put(udpHeader);

            // data (data.length bytes)
            psuedoHeaderBuffer.put(data);

            // get byte [] of ipv4 psuedo header
            psuedoHeaderBuffer.rewind();
            byte[] psuedoHeaderData = new byte[psuedoHeaderBuffer.remaining()];
            psuedoHeaderBuffer.get(psuedoHeaderData);

            // calculate checksum
            short checkSum = IPUtils.IPHeaderChecksum.calculateChecksum(psuedoHeaderData);

            return checkSum;
        }

        /**
         * Calculate the Internet Checksum of a buffer (RFC 1071 - http://www.faqs.org/rfcs/rfc1071.html)
         * Algorithm is
         * 1) apply a 16-bit 1's complement sum over all octets (adjacent 8-bit pairs [A,B], final odd length is [A,0])
         * 2) apply 1's complement to this final sum
         *
         * Notes:
         * 1's complement is bitwise NOT of positive value.
         * Ensure that any carry bits are added back to avoid off-by-one errors
         *
         *
         * @param buf The message
         * @return The checksum
         */
        public static short calculateChecksum(byte[] buf) {
            int length = buf.length;
            int i = 0;

            long sum = 0;
            long data;

            // Handle all pairs
            while (length > 1) {

                data = (((buf[i] << 8) & 0xFF00) | ((buf[i + 1]) & 0xFF));
                sum += data;
                // 1's complement carry bit correction in 16-bits (detecting sign extension)
                if ((sum & 0xFFFF0000) > 0) {
                    sum = sum & 0xFFFF;
                    sum += 1;
                }

                i += 2;
                length -= 2;
            }

            // Handle remaining byte in odd length buffers
            if (length > 0) {

                sum += (buf[i] << 8 & 0xFF00);
                // 1's complement carry bit correction in 16-bits (detecting sign extension)
                if ((sum & 0xFFFF0000) > 0) {
                    sum = sum & 0xFFFF;
                    sum += 1;
                }
            }

            // Final 1's complement value correction to 16-bits
            sum = ~sum;
            sum = sum & 0xFFFF;
            return (short)sum;

        }

    }
}
