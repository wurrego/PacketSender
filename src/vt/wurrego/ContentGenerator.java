package vt.wurrego;

import com.google.common.util.concurrent.RateLimiter;
import vt.wurrego.utils.CommonEnums;
import vt.wurrego.utils.Logger;

import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Created by wurrego on 5/8/17.
 */
public class ContentGenerator extends Thread {

    /**
     * class parameters
     */
    private StateMachine state;
    private ArrayList<TxSocket> sockets_ThreadList;
    private ArrayList<TxSocket> sockets_killedThreadList;
    private boolean debugLogging;
    private int debugLevel;
    private boolean running;
    private String TAG;


    // statistics
    private int packetsSent;

    // Content Selector
    private ContentSelector contentSelector;

    // Content Descriptor
    private ContentInfo.ContentDescriptors contentDescriptor;


    public ContentGenerator(ContentInfo.ContentDescriptors cd, int debugLevel) {

        this.contentDescriptor = cd;

        // debug parameters
        TAG = " [" + this.getClass().getSimpleName() + " - " + this.getName() + " - " + contentDescriptor.name + "] ";
        this.debugLevel = debugLevel;
        debugLogging = false;

        if (debugLevel > 0)
            debugLogging = true;

        // init
        state = new StateMachine();
        sockets_ThreadList = new ArrayList<TxSocket>();
        sockets_killedThreadList = new ArrayList<TxSocket>();
        running = true;


        // statistics
        packetsSent = 0;

        // content selector
        contentSelector = new ContentSelector(contentDescriptor, debugLevel-1);


    }

    @Override
    public void interrupt() {
        if (debugLogging) {   Logger.log( TAG , "- Interrupted" ); }

        this.running = false;

    }

    @Override
    public void run() {

        // start delay
        try {
            Thread.sleep(contentDescriptor.start_delay_milliseconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // State Machine
        DecimalFormat df = new DecimalFormat("#.00");
        Logger.log( TAG ,"On Time: " + df.format(state.get_onTime(100)) + " / Off Time: " + df.format(state.get_offTime(100)) );

        // rateLimiter will uniformely distribute the packet sends per second
        RateLimiter rateLimiter = RateLimiter.create(contentDescriptor.clock_hz);

        while (running) {

            // acquire rate token
            rateLimiter.acquire();

            if (debugLogging) {  Logger.log( TAG , state.get_State().toString()); }
            CommonEnums.StateStatus nextState = state.get_nextState();
            updateState(nextState);


        }

    }

    /**
     * updateState - change operating state and system behavior
     * @param nextState
     */
    public void updateState(CommonEnums.StateStatus nextState) {

        // do transition from current state to next state, else do nothing
        if (state.get_State() != nextState)
        {
            // create reference to txSocket thread
            TxSocket txSocket = null;

            switch(nextState) {
                case OFF:
                    for (TxSocket aThread : sockets_ThreadList)
                    {
                        if (aThread != null) { aThread.interrupt(); sockets_killedThreadList.add(aThread); }
                    }
                    break;

                case IDLE:
                    for (TxSocket aThread : sockets_ThreadList)
                    {
                        if (aThread != null) { aThread.interrupt(); sockets_killedThreadList.add(aThread); }
                    }
                    break;

                case LOW:
                    for (TxSocket aThread : sockets_ThreadList)
                    {
                        if (aThread != null) { aThread.interrupt(); sockets_killedThreadList.add(aThread); }
                    }
                    txSocket = new TxSocket(this, contentDescriptor, debugLevel-1);
                    txSocket.start();
                    sockets_ThreadList.add(txSocket);
                    break;

                case HIGH:
                    for (TxSocket aThread : sockets_ThreadList)
                    {
                        if (aThread != null) { aThread.interrupt(); sockets_killedThreadList.add(aThread); }
                    }
                    txSocket = new TxSocket(this, contentDescriptor, debugLevel-1);
                    txSocket.start();
                    sockets_ThreadList.add(txSocket);
                    break;


                default:
                    for (TxSocket aThread : sockets_ThreadList)
                    {
                        if (aThread != null) { aThread.interrupt(); sockets_killedThreadList.add(aThread); }
                    }
                    Logger.log( TAG ,  "System in Unknown State." );

            }

            state.set_State(nextState);

            // clean up thread list
            for (TxSocket aThread : sockets_killedThreadList)
                sockets_ThreadList.remove(aThread);

            sockets_killedThreadList.clear();
        }
    }

    public int get_PacketsSent_count() {
        return packetsSent;
    }

    public void increment_PacketsSent() {
        this.packetsSent =  this.packetsSent+1;
    }

    public String getUserProvidedName() {
        return contentDescriptor.name;
    }

    public byte[] offerPacket(int mtuSize, boolean varyPacketSize) { return contentSelector.getContentPacket(mtuSize, varyPacketSize); }
}
