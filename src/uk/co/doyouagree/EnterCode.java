package uk.co.doyouagree;


import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import com.facebook.*;
import com.facebook.model.*;
import uk.co.doyouagree.R;


public class EnterCode extends Activity {
	public final static String ENTEREDCODE = "com.example.EnterCode.enteredCode";
	public final static String USERID = "com.example.EnterCode.userID";
	private String usrID = "";
	private EditText code;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_code);
        
       
        
        // start Facebook Login
        Session.openActiveSession(this, true, new Session.StatusCallback() {

          // callback when session changes state
          @Override
          public void call(Session session, SessionState state, Exception exception) {
        	  if (session.isOpened()) {
        		// make request to the /me API
        		  Request.executeMeRequestAsync(session, new Request.GraphUserCallback() {

        		    // callback after Graph API response with user object
        		    @Override
        		    public void onCompleted(GraphUser user, Response response) {
        		    	if (user != null) {
        		    		TextView welcome = (TextView) findViewById(R.id.textView2);
        		    		welcome.setText("Hello " + user.getName() + "!");
        		    		usrID = user.getId();
        		    	}
        		    }
        		  });
        		  
        	  }

          }
        });
        
    }

    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_enter_code, menu);
        return true;
    }
    
    public void enterCode(View view) {
       // label
    	
    	code = (EditText) findViewById(R.id.editText1);
    	
    	Intent myIntent = new Intent(this, PollResultsView.class);
    	myIntent.putExtra(ENTEREDCODE, code.getText().toString());
    	myIntent.putExtra(USERID, usrID);
    	startActivity(myIntent);

    	
    }
    
}
