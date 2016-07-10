package crashavoidance.se.rit.datacollector.views;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import org.json.JSONException;

import java.io.IOException;

import crashavoidance.se.rit.datacollector.R;
import crashavoidance.se.rit.datacollector.service.DataCollectorServiceHandler;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "DataCollector";
    DataCollectorServiceHandler handler = new DataCollectorServiceHandler(this);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void viewDatabase(View v){
        Intent dbmanager = new Intent(this,AndroidDatabaseManager.class);
        startActivity(dbmanager);
    }

    public void post(View view) {
        if(!handler.isConnected())
            Toast.makeText(getBaseContext(), "Need internet connection to run this", Toast.LENGTH_LONG).show();
        else
            // call AsynTask to perform network operation on separate thread
            new HttpAsyncTask().execute();
    }

    private class HttpAsyncTask extends AsyncTask<String, Void, String> {
        String result = "";
        @Override
        protected String doInBackground(String... Params) {
            try {
                result = handler.POST();
            } catch (Exception e) {
                result = e.getMessage() + " Check log for details";
                Log.e(TAG, "Issue calling REST service", e);
            }
            return result;
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(getBaseContext(), "Result: " + result, Toast.LENGTH_LONG).show();
        }
    }
}
