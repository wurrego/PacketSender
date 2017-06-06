package vt.wurrego;

import java.util.List;

/**
 * Created by wurrego on 5/8/17.
 */
class ContentInfo {

    int debug_level;
    List<ContentDescriptors> contentDescriptors;

    class ContentDescriptors {
        String name;
        String dest_ip;
        int dest_port;
        int average_packets_per_second;
        int max_packets_per_second;
        boolean variable_rate;
        int clock_hz;
        int start_delay_milliseconds;
        boolean udp_packet;
        String packet_src_address;
        String packet_dst_address;
        short packet_src_port;
        short packet_dst_port;
        String content_file_path;
        int packet_mtu_size_bytes;
        boolean packet_variable_size;
    }
}

