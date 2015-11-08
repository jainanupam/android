package com.coddicted.expensesview.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.coddicted.expensesview.Constants;
import com.coddicted.expensesview.MainActivity;
import com.coddicted.expensesview.R;
import com.coddicted.expensesview.Utility;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;


public class LoginActivity extends Activity {

    // Progress dialog object
    ProgressDialog prgDialog;
    // userName edit text object
    EditText userName;
    EditText password;

    // TextView object to display error message (if any)
    TextView errorMsg;

    // TAG for the class
    private static String TAG = "LoginActivityTAG";

    private static final int REGISTER_ACTIVITY_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Get all the edit view object instances
        this.userName = (EditText) findViewById(R.id.user_name);
        this.password = (EditText) findViewById(R.id.password);

        // Get the Text view object that would be used in case of failure/ errors
        this.errorMsg = (TextView) findViewById(R.id.login_error);

        // instantiate process dialog object
        prgDialog = new ProgressDialog(this);
        prgDialog.setTitle("Please wait...");
        prgDialog.setCancelable(false);
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

    // Custom code for REST Service calling
    public void loginUser(View view){
        // Get all the data values input by user
        String userName = this.userName.getText().toString();
        String password = this.password.getText().toString();

        // Instantiate HTTP Request param object
        RequestParams params = new RequestParams();

        if(Utility.isNotNull(userName) &&
                Utility.isNotNull(password)){
            // Add all the parameters to the HTTP Request param object
            params.put(Constants.USER_NAME, userName);
            params.put(Constants.PASSWORD, password);

            Log.i(TAG, "userName : " + userName);
            Log.i(TAG, "password : " + password);

            // Invoke the RESTful web service
            //invokeWS(params);

            // create explicit intent for the main screen activity
            Intent mainScreenIntent = new Intent(LoginActivity.this, MainActivity.class);

            // call the new activity
            startActivity(mainScreenIntent);
        } else {
            Toast.makeText(getApplicationContext(), "All fields are mandatory.",
                    Toast.LENGTH_LONG).show();
        }
    }

    // Start register user activity
    public void registerUser(){
        // Create exlicit intent for the register activity
        Intent registerActivityIntent = new Intent(LoginActivity.this, RegisterActivity.class);

        // Start the register activity
        startActivityForResult(registerActivityIntent, REGISTER_ACTIVITY_REQUEST_CODE);
    }

    /**
     * Method that performs RESTful webservice invocations
     *
     * @param params
     */
    public void invokeWS(RequestParams params){
        // Show Progress Dialog
        prgDialog.show();

        // Make AsyncHttpClient object
        AsyncHttpClient client = new AsyncHttpClient();

        // Make RESTful webservice call using AsyncHttpClient object
        client.get(Constants.LOGIN_USER_URL,params ,new AsyncHttpResponseHandler() {
            // When the response returned by REST has Http response code '200'
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                // Hide Progress Dialog
                prgDialog.hide();
                try {
                    // JSON Object
                    JSONObject obj = new JSONObject(new String(response));
                    // When the JSON response has status boolean value assigned with true
                    if(obj.getBoolean("status")){
                        Toast.makeText(getApplicationContext(), "Data submitted successfully!", Toast.LENGTH_LONG).show();
                        // Navigate to Home screen
                        //navigatetoHomeActivity();
                    }
                    // Else display error message
                    else{
                        errorMsg.setText(obj.getString("error_msg"));
                        Toast.makeText(getApplicationContext(), obj.getString("error_msg"), Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    Toast.makeText(getApplicationContext(), "Error Occured [Server's JSON response might be invalid]!", Toast.LENGTH_LONG).show();
                    e.printStackTrace();

                }
            }
            // When the response returned by REST has Http response code other than '200'
            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse,
                                  Throwable e) {
                // Hide Progress Dialog
                prgDialog.hide();
                // When Http response code is '404'
                if(statusCode == 404){
                    Toast.makeText(getApplicationContext(), "Requested resource not found", Toast.LENGTH_LONG).show();
                }
                // When Http response code is '500'
                else if(statusCode == 500){
                    Toast.makeText(getApplicationContext(), "Something went wrong at server end", Toast.LENGTH_LONG).show();
                }
                // When Http response code other than 404, 500
                else{
                    Toast.makeText(getApplicationContext(),
                            "Unexpected Error occcured! [Most common Error: " +
                                    "Device might not be connected to Internet or " +
                                    "remote server is not up and running]",
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void onActivityResult(){
        //TODO Complete the callback method
    }
}
