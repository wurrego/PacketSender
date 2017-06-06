package vt.wurrego;

import com.google.common.util.concurrent.RateLimiter;
import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import vt.wurrego.utils.Logger;

import java.io.*;
import java.util.ArrayList;



/**
 * Packet Sender
 */
class PacketSender {

    /**
     * class parameters
     */
    final static double masterClock_Hz = 1.0/10.0;
    final static String TAG = " [" + PacketSender.class.getSimpleName() + "] ";

    /**
     * main() - Entry Point
     * @param args
     */
    public static void main(String[] args) {

        ContentInfo contentInfo =  loadContentInfo("contentDescriptors.json");

        // no content so quit
        if (contentInfo.contentDescriptors.size() < 1)
                return;

        ArrayList<ContentGenerator> contentGenerators_ThreadList = new ArrayList<ContentGenerator>();

        for (ContentInfo.ContentDescriptors cd : contentInfo.contentDescriptors ) {
            ContentGenerator cg = new ContentGenerator(cd, contentInfo.debug_level);
            cg.start();
            contentGenerators_ThreadList.add(cg);
        }

        // rateLimiter will uniformely distribute the packet sends per second
        RateLimiter rateLimiter = RateLimiter.create(masterClock_Hz);

        while (true) {

            // acquire rate token
            rateLimiter.acquire();

            Logger.log(true);
            Logger.log( TAG, contentGenerators_ThreadList.size() + " content generators alive:");


            for (ContentGenerator cg : contentGenerators_ThreadList ) {
               Logger.log( TAG, cg.getUserProvidedName() + " - Total Packets Sent: " + cg.get_PacketsSent_count() );
            }

        }


    }

    /**
     * getContentDescriptors - get content descriptors for json
     * @param filename
     * @return ContentInfo
     */
    static public ContentInfo loadContentInfo(String filename)
    {
        ContentInfo contentInfo = null;

        File file = new File(filename);
        Gson gson = new Gson();

        BufferedReader br = null;
        try {
            if (file.exists()) {
                br = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));
                contentInfo = gson.fromJson(br, ContentInfo.class);
            } else {
                Logger.log( TAG, "ERROR loading Content Descriptors - File Not Found.");
                return null;
            }
        } catch (FileNotFoundException e) {
            Logger.log( TAG, "ERROR loading Content Descriptors - File Not Found.");
            return null;
        } catch (JsonIOException e) {
            Logger.log( TAG, "ERROR loading Content Descriptors - JSON IO Exception.\n" + e);
            return null;
        } catch (JsonSyntaxException e) {
            Logger.log( TAG, "ERROR loading Content Descriptors - JSON Syntax Exception.\n" + e);
            return null;
        } finally {
            try {
                if (br != null)
                    br.close();
            } catch (IOException e) {
                Logger.log( TAG, "ERROR closing Content Descriptor File.");
                return null;
            }
        }

        return contentInfo;
    }




}
