package uk.co.doyouagree;



import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.facebook.FacebookRequestError;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.RequestAsyncTask;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import uk.co.doyouagree.R;



@SuppressLint("SetJavaScriptEnabled")
public class PollResultsView extends Activity {
	String code = "";
	String userID = "";
	private WebView wv;
	private SeekBar sb;
	private TextView percent;
	private static final List<String> PERMISSIONS = Arrays.asList("publish_actions");
	private static final String PENDING_PUBLISH_KEY = "pendingPublishReauthorization";
	private boolean pendingPublishReauthorization = false;
	private ImageButton shareButton;
	private Button agreeButton;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_poll_results_view);
		// Show the Up button in the action bar.
		getActionBar().setDisplayHomeAsUpEnabled(true);
		wv = (WebView) findViewById(R.id.webView1);
		wv.getSettings().setJavaScriptEnabled(true);
		
		
		
		
		
		Intent intent = getIntent();
		code = intent.getStringExtra(EnterCode.ENTEREDCODE);
		if(code.equals("")) code ="NotACode";
		//System.out.println(code);
		
		userID = intent.getStringExtra(EnterCode.USERID);
		
		
		final Activity activity = this;

		String url = "http://www.doyouagree.co.uk/"+code+".html";
		
        wv.setWebViewClient(new WebViewClient() {
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Toast.makeText(activity, description, Toast.LENGTH_SHORT).show();
            }
        });

        wv.loadUrl(url);
      
		
			
		shareButton = (ImageButton) findViewById(R.id.shareButton);
		percent = (TextView) findViewById(R.id.textView3);
		agreeButton = (Button) findViewById(R.id.button1);
		
		sb = (SeekBar) findViewById(R.id.seekBar1);
		sb.setMax(100);
		sb.setProgress(50);
		sb.setEnabled(true);
		sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){

		    @Override
		    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		        
		        percent.setText("I Agree : "+String.valueOf(progress)+"%");
		        agreeButton.setEnabled(true);
		    }

		    @Override
		    public void onStartTrackingTouch(SeekBar seekBar) {

		    }

		    @Override
		    public void onStopTrackingTouch(SeekBar seekBar) {

		    }
		});
		
		if (savedInstanceState != null) {
		    pendingPublishReauthorization = 
		        savedInstanceState.getBoolean(PENDING_PUBLISH_KEY, false);
		}

		
	}
	
	
	public void goBack(View view) {
		 
		wv.loadUrl("about:blank");
		agreeButton.setEnabled(false);
		shareButton.setVisibility(View.INVISIBLE);
		Intent returnIntent = new Intent(this, EnterCode.class);
    	startActivity(returnIntent);
		 
	 }
	
	public void share(View view) {
		shareButton.setEnabled(false);
		publishStory();
		 
	 }
	
	public void sendResult(View view) {
		int val = sb.getProgress();
		sb.setEnabled(false);
		agreeButton.setEnabled(false);
		float sendVal = (float)val / 100;
		
		String resultUrl = "http://www.doyouagree.co.uk/storeResponse.php?code="+code+"&value="+sendVal+"&user="+userID;
			
	
		wv.loadUrl(resultUrl);
		
		 Log.i("FB Log","Fb ID : " + userID);
		 if(userID != null && !userID.isEmpty()){
		shareButton.setVisibility(View.VISIBLE);
		shareButton.setEnabled(true);
		 }
	}
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_poll_results_view, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
//			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void publishStory() {
	    Session session = Session.getActiveSession();
		
	    if (session != null){

	        // Check for publish permissions    
	        List<String> permissions = session.getPermissions();
	        if (!isSubsetOf(PERMISSIONS, permissions)) {
	            pendingPublishReauthorization = true;
	            Session.NewPermissionsRequest newPermissionsRequest = new Session
	                    .NewPermissionsRequest(this, PERMISSIONS);
	        session.requestNewPublishPermissions(newPermissionsRequest);
	            return;
	        }

	        String lnk = "http://www.doyouagree.co.uk/webGraph.php?code="+code;
	        Bundle postParams = new Bundle();
	        postParams.putString("name", "Do You Agree");
	        postParams.putString("caption", "Do you agree with ...");
	        postParams.putString("description", "The mobile app that makes it easy to see if you agree");
	        postParams.putString("link", lnk);
	        postParams.putString("picture", "http://www.doyouagree.co.uk/dya_logo.png");

	        Request.Callback callback= new Request.Callback() {
	            public void onCompleted(Response response) {
	                JSONObject graphResponse = response
	                                           .getGraphObject()
	                                           .getInnerJSONObject();
	                String postId = null;
	                try {
	                    postId = graphResponse.getString("id");
	                } catch (JSONException e) {
	                    Log.i("Fb JSON Exception",
	                        "JSON error "+ e.getMessage());
	                    
	                }
	                FacebookRequestError error = response.getError();
	                if (error != null) {
	                    Toast.makeText(getApplicationContext(),
	                         error.getErrorMessage(),
	                         Toast.LENGTH_SHORT).show();
	                    } else {
	                        Toast.makeText(getApplicationContext(), 
	                             postId,
	                             Toast.LENGTH_LONG).show();
	                }
	            }
	        };

	        Request request = new Request(session, "me/feed", postParams, 
	                              HttpMethod.POST, callback);

	        RequestAsyncTask task = new RequestAsyncTask(request);
	        task.execute();
	    }

	}
	private boolean isSubsetOf(Collection<String> subset, Collection<String> superset) {
	    for (String string : subset) {
	        if (!superset.contains(string)) {
	            return false;
	        }
	    }
	    return true;
	}
	@SuppressWarnings("unused")
	private void onSessionStateChange(Session session, SessionState state, Exception exception) {
	    if (state.isOpened()) {
	        shareButton.setVisibility(View.VISIBLE);
	        if (pendingPublishReauthorization && 
	                state.equals(SessionState.OPENED_TOKEN_UPDATED)) {
	            pendingPublishReauthorization = false;
	            publishStory();
	        }
	    } else if (state.isClosed()) {
	        shareButton.setVisibility(View.INVISIBLE);
	    }
	}
	@Override
	public void onSaveInstanceState(Bundle outState) {
	    super.onSaveInstanceState(outState);
	    outState.putBoolean(PENDING_PUBLISH_KEY, pendingPublishReauthorization);
//	    super.uiHelper.onSaveInstanceState(outState);
	}
	
	
}
