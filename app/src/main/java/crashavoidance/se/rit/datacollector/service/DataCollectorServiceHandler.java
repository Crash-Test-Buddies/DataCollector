package crashavoidance.se.rit.datacollector.service;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import crashavoidance.se.rit.datacollector.persistence.PhoneContract;
import crashavoidance.se.rit.datacollector.persistence.StepTimerContract;
import crashavoidance.se.rit.datacollector.persistence.WifiDirectDBHelper;

/**
 * Created by Chris on 7/10/2016.
 */
public class DataCollectorServiceHandler {

    private static final String TAG = "DataCollectorService";
    WifiDirectDBHelper dbHelper;
    private static final String batchLimit = "1000";
    private static final String URL = "http://datacollector-wifidirect.rhcloud.com/DataCollectorService/rest/addTiming";
    // List to track which ids to update as they are sent to the service
    private ArrayList<Long> idsToUpdate = new ArrayList<Long>();
    Context context;
    public DataCollectorServiceHandler(Context context){
        this.context = context;
        dbHelper = new WifiDirectDBHelper(context);
    }

    /**
     * Post stepTimer and phone records to rest service to be entered into external database
     * @return
     * @throws JSONException
     * @throws IOException
     */
    public String POST() throws JSONException, IOException {
        InputStream inputStream = null;
        String result = "";

            // 1. create HttpClient
            HttpClient httpclient = new DefaultHttpClient();

            long numReadyRecords = dbHelper.getReadyStepCount();
            while (numReadyRecords > Long.parseLong(batchLimit)) {
                try {
                    // 2. make POST request to the given URL
                    HttpPost httpPost = new HttpPost(URL);

                    String json = "";

                    // 3. build jsonObject
                    JSONObject jsonObject = new JSONObject();
                    // Get step timer records and update their status
                    jsonObject.accumulate("timings", getAndUpdateStepTimerRecords());
                    jsonObject.accumulate("phone", getPhoneRecord());

                    // 4. convert JSONObject to JSON to String
                    json = jsonObject.toString();

                    // 5. set json to StringEntity
                    StringEntity se = new StringEntity(json);

                    // 6. set httpPost Entity
                    httpPost.setEntity(se);

                    // 7. Set some headers to inform server about the type of the content
                    httpPost.setHeader("Accept", "application/json");
                    httpPost.setHeader("Content-type", "application/json");

                    // 8. Execute POST request to the given URL
                    HttpResponse httpResponse = httpclient.execute(httpPost);

                    String response = EntityUtils.toString(httpResponse.getEntity());
                    int statusCode = httpResponse.getStatusLine().getStatusCode();
                    if (statusCode == 200) {
                        Log.i(TAG, "Service call completed successfully");
                        // Update stepTimer records to Uploaded so they won't be picked up again
                        dbHelper.updateStepTimerStatuses(idsToUpdate);
                        numReadyRecords -= Long.parseLong(batchLimit);
                    } else {
                        Log.e(TAG, "Issue calling service: " + response);
                    }
                } finally {
                    // Clear ids to update whether this failed or succeeded
                    idsToUpdate.clear();
                } // try
            } // While loop

        // 11. return result
        return result;
    }

    /**
     * Get batch of stepTimer records up to batchLimit variable and update the status in the db
     * @return JSONArray of stepTimer objects
     * @throws JSONException
     */
    private JSONArray getAndUpdateStepTimerRecords() throws JSONException {
        Cursor stepTimerRecs = dbHelper.getStepBatch(batchLimit);
        JSONArray stepTimerObjects = new JSONArray();
        while (stepTimerRecs.moveToNext()){
            JSONObject stepTimerObject = new JSONObject();
            stepTimerObject.accumulate("stepName",
                    stepTimerRecs.getString(stepTimerRecs.getColumnIndex(StepTimerContract.StepEntry.COLUMN_NAME_STEP_NAME)));
            stepTimerObject.accumulate("startTime",
                    stepTimerRecs.getString(stepTimerRecs.getColumnIndex(StepTimerContract.StepEntry.COLUMN_NAME_START_TIME)));
            stepTimerObject.accumulate("endTime",
                    stepTimerRecs.getString(stepTimerRecs.getColumnIndex(StepTimerContract.StepEntry.COLUMN_NAME_END_TIME)));
            stepTimerObject.accumulate("latitude",
                    stepTimerRecs.getString(stepTimerRecs.getColumnIndex(StepTimerContract.StepEntry.COLUMN_NAME_LATITUDE)));
            stepTimerObject.accumulate("longitude",
                    stepTimerRecs.getString(stepTimerRecs.getColumnIndex(StepTimerContract.StepEntry.COLUMN_NAME_LONGITUDE)));
            stepTimerObjects.put(stepTimerObject);
            // Add the id to the list of ids to update
            idsToUpdate.add(stepTimerRecs.getLong(stepTimerRecs.getColumnIndex(StepTimerContract.StepEntry._ID)));
        }
        return stepTimerObjects;
    }


    /**
     * Get Phone record from database and convert into JSON object
     * @return JSON Object representing phone information
     * @throws JSONException
     */
    private JSONObject getPhoneRecord() throws JSONException{
        Cursor phoneRec = dbHelper.getPhoneInfo();
        JSONObject phoneObject = new JSONObject();
        if (phoneRec.moveToFirst()){
            phoneObject.accumulate("brand",
                    phoneRec.getString(phoneRec.getColumnIndex(PhoneContract.PhoneEntry.COLUMN_NAME_BRAND)));
            phoneObject.accumulate("os",
                    phoneRec.getString(phoneRec.getColumnIndex(PhoneContract.PhoneEntry.COLUMN_NAME_OS)));
            phoneObject.accumulate("manufacturer",
                    phoneRec.getString(phoneRec.getColumnIndex(PhoneContract.PhoneEntry.COLUMN_NAME_MANUFACTURER)));
            phoneObject.accumulate("model",
                    phoneRec.getString(phoneRec.getColumnIndex(PhoneContract.PhoneEntry.COLUMN_NAME_MODEL)));
            phoneObject.accumulate("deviceAddress",
                    phoneRec.getString(phoneRec.getColumnIndex(PhoneContract.PhoneEntry.COLUMN_NAME_DEVICE_ADDRESS)));
        }
        return phoneObject;
    }

    /**
     * Check to see if we are connected to a network
     * @return true if connected, false if not
     */
    public boolean isConnected(){
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Activity.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected())
            return true;
        else
            return false;
    }


}
