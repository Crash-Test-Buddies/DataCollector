package crashavoidance.se.rit.datacollector.service;

import android.app.IntentService;
import android.content.Intent;

/**
 * Created by Chris on 6/19/2016.
 * Handles intents sent by other apps to insert records into the
 * database
 */
public class DBHandlerService extends IntentService{

    private String serviceName;
    private final String action = "insertRecord";

    public DBHandlerService(){
        super("DBHandlerService");
        this.serviceName = serviceName;
    }

    /**
     * Handle intents specific to this service
     * @param intent
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent.getAction().equals(action)){

        }
    }
}
