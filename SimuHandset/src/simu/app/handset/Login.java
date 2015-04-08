package simu.app.handset;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.LogInCallback;
import com.avos.avoscloud.LogUtil.log;

public class Login extends Activity {

	private EditText usernameEditText = null;
	private EditText userpasswordEditText = null;
	private Button loginbtn = null;
	private AVUser user = null;
	private String tag;
	protected boolean isLogInSuccees = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		initview();
		
		AVUser user_new = new AVUser();
		user_new.setUsername("6371529");
		user_new.setPassword("520402");
		user_new.signUpInBackground(null);
		

		if (null != GetCurrentUser()) {
			Toast.makeText(this, "存在本地用户，跳过登录界面", 3000).show();
			Intent i = new Intent(this, CroutonDemo.class);
			startActivity(i);
			finish();
		}
	}

	private void initview() {
		// TODO Auto-generated method stub
		usernameEditText = (EditText) findViewById(R.id.username);
		userpasswordEditText = (EditText) findViewById(R.id.password);
		loginbtn = (Button) findViewById(R.id.loginbtn);

		loginbtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				login();
			}
		});

	}

	public void login() {
		String username = usernameEditText.getText().toString().trim();
		String userpassword = userpasswordEditText.getText().toString().trim();
		if (username.equals("") || userpassword.equals("")) {
			return;
		}
		
			AVUser.logInInBackground(username, userpassword,new LogInCallback<AVUser>() {
				@Override
				public void done(AVUser arg0, AVException arg1) {
					if (null != arg0) {
						isLogInSuccees  = true;
						Toast.makeText(Login.this, "登录成功 !!!", 3000).show();
						Intent it = new Intent();
						it.setClass(getApplicationContext(), CroutonDemo.class);
						startActivity(it);
						finish();
					}else {
						isLogInSuccees = false;
						Toast.makeText(Login.this, "登录失败，请填写正确的用户信息", 3000).show();
					}
				}
			});
			
		} 
		
	

	private AVObject GetCurrentUser() {
//		AVUser.logOut();
		AVUser currentuser = AVUser.getCurrentUser();
		return currentuser;
	}
}
