package com.awesome.reillyz.jsonapp;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

public class MainActivity extends ListActivity {

    public static final String TAG = MainActivity.class.getSimpleName();
    public static final String KEY_NAME = "name";
    public static final String KEY_ROLE = "role";
    protected JSONObject mDevsData;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(isNetworkAvalible()){
            GetDevsTask getDevsTask = new GetDevsTask();
            getDevsTask.execute();
        }
        else{
            Toast.makeText(this.getApplicationContext(),"Network Unavalible",Toast.LENGTH_LONG);

        }


    }

    private void logException(Exception e) {
        Log.e(TAG, "Exception Caught", e);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

/*    @Override
    protected void OnListItemClick(ListView l, View v, int position, long id)
    {

    }*/

    /**
     * Deals with the Data about developers retrieved
     */
    private void handelDevsData(){

        if(mDevsData==null){
            updateDisplayForError();
        }
        else{
            try{
                int count = mDevsData.getInt("count");
                JSONArray devs = mDevsData.getJSONArray("members");
                ArrayList<HashMap<String,String>> devsInfo = new ArrayList<HashMap<String, String>>();
                for(int i=0; i<count;i++){
                    JSONObject dev= devs.getJSONObject(i);
                    String name = Html.fromHtml(dev.getString(KEY_NAME)).toString();
                    String role = Html.fromHtml(dev.getString(KEY_ROLE)).toString();

                    HashMap<String,String> devInfo = new HashMap<>();
                    devInfo.put(KEY_NAME,name);
                    devInfo.put(KEY_ROLE,role);

                    devsInfo.add(devInfo);
                }

                String[] keys = {KEY_NAME, KEY_ROLE};
                int[] ids = {android.R.id.text1, android.R.id.text2};
                SimpleAdapter adapter = new SimpleAdapter(this, devsInfo,
                        android.R.layout.simple_list_item_2, keys, ids);

                setListAdapter(adapter);

            }catch(Exception E){
                logException(E);
            }
        }

    }


    /**
     * returns if the network is avalible
     *
     * @return true if network is avalible, false otherwise.
     */
    private boolean isNetworkAvalible(){
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        Boolean isAvalible = false;
        if (networkInfo != null && networkInfo.isConnected()) {
            isAvalible = true;
        }

        return isAvalible;

    }

    /**
     * Updates the display if an error occurs in data retrieval
     */
    private void updateDisplayForError() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.error_title));
        builder.setMessage(getString(R.string.error_message));
        builder.setPositiveButton(android.R.string.ok, null);
        AlertDialog dialog = builder.create();
        dialog.show();

        TextView emptyTextView = (TextView) getListView().getEmptyView();
        emptyTextView.setText(getString(R.string.no_items));
    }

    private class GetDevsTask extends AsyncTask<Object, Void, JSONObject>
    {

        @Override
        protected JSONObject doInBackground(Object... arg0) {
            int responseCode = -1;
            JSONObject jsonResponse = null;

            try {
                URL blogFeedUrl = new URL("http://www.cs.grinnell.edu/~owusumic17/android.json");
                HttpURLConnection connection = (HttpURLConnection) blogFeedUrl.openConnection();
                connection.connect();

                responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream inputStream = connection.getInputStream();
                    Reader reader = new InputStreamReader(inputStream);
                    int contentLength = connection.getContentLength();
                    char[] charArray = new char[contentLength];
                    reader.read(charArray);
                    String responseData = new String(charArray);

                    jsonResponse = new JSONObject(responseData);
                } else {
                    Log.i(TAG, "Bad response code: " + responseCode);
                }

            } catch (MalformedURLException e) {
                logException(e);

            } catch (IOException e) {
                logException(e);
            } catch (Exception e) {
                logException(e);
            }
            return jsonResponse;
        }


        @Override
        protected void onPostExecute(JSONObject result) {
            mDevsData=result;
            handelDevsData();
        }
    }


}
