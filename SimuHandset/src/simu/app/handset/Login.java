package simu.app.handset;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVUser;

public class Login extends Activity {
	
	private EditText usernameEditText = null;
	private EditText userpasswordEditText = null;
	private Button loginbtn = null;
	private AVUser user = null;
	private String tag;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		initview();
		
		if (null != GetCurrentUser()) {
			Toast.makeText(this, "存在本地用户，跳过登录界面", 3000).show();
			Intent i = new Intent(this, CroutonDemo.class);
			startActivity(i);
			finish();
		}
	}

	private void initview() {
		// TODO Auto-generated method stub
		usernameEditText = (EditText)findViewById(R.id.username);
		userpasswordEditText = (EditText)findViewById(R.id.password);
		loginbtn = (Button)findViewById(R.id.loginbtn);
		
		loginbtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				login();
			}
		});
		
	}

	public void login() {
		// TODO Auto-generated method stub
		String username = usernameEditText.getText().toString().trim();
		String userpassword = userpasswordEditText.getText().toString().trim();
		if (username.equals("") || userpassword.equals("")) {
			return;
		}
		try {
			user = AVUser.logIn(username, userpassword);
			if (null != user) {
				Toast.makeText(this, "登录成功!!!", 3000).show();
				Intent i = new Intent(this, CroutonDemo.class);
				startActivity(i);
				finish();
			}
		} catch (AVException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Toast.makeText(this, "登录失败，请输入正确的用户信息", 3000).show();
	}

	private AVObject GetCurrentUser() {
		// TODO Auto-generated method stub
		AVUser currentuser = AVUser.getCurrentUser();
		return currentuser;
	}
}
