package my.openvpn.settings;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class vpn_modify extends Activity{
	private String client_array3[][]= new String[38][2];
	private Button submitButton = null;
	private EditText modify_username = null;		//定义四个文本框
	private EditText modify_old_password = null;
	private EditText modify_new_password = null;
	private EditText modify_confirm_password = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.vpn_modify);
		
		String tmp_string = null;
		int i=0;
		FileReader myFileReader = null;
		try {
			myFileReader = new FileReader("/sdcard/Client.ini");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		BufferedReader myBufferedReader = new BufferedReader(myFileReader);
		try {
			while((tmp_string = myBufferedReader.readLine())!= null) {
				client_array3[i++] = tmp_string.split("=", 2);}
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			myFileReader.close();										//关闭文件
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		submitButton = (Button)findViewById(R.id.modify_submit);
		submitButton.setOnClickListener(new submitButtonListener());
	}
	
	class submitButtonListener implements OnClickListener{
		@Override
		public void onClick(View v) {
//			Modify();
		}
	}

	//-----------------------------------------------------------------------
	public void Modify(){//修改密码页面中的提交按钮功能	
		
		String ip_read = client_array3[36][1];											//存储从配置文件获取IP地址
		String port_read = client_array3[37][1];										//存储从配置文件获取端口
		String namespace = "http://"+client_array3[36][1]+":"+client_array3[37][1]+"/soap/authentication/";	//命名空间
		String URL ="http://"+ip_read+":"+port_read+"/soap/soaps.php";										//WSDL的URL
		String methodName_changeUsePw = "changeUsePw";														//方法
    	String soapAction_changeUsePw = "http://"+ip_read+":"+port_read+"/soap/authentication/changeUsePw";//动作
		
		modify_username = (EditText)findViewById(R.id.modify_username);
		modify_old_password = (EditText)findViewById(R.id.modify_old_password);
		modify_new_password = (EditText)findViewById(R.id.modify_new_password);
		modify_confirm_password = (EditText)findViewById(R.id.modify_confirm_password);
		String string_username=modify_username.getText().toString();
		String string_old_password=modify_old_password.getText().toString();
		String string_new_password=modify_new_password.getText().toString();
		String string_confirm_password=modify_confirm_password.getText().toString();
		
		if (string_new_password.equals(string_confirm_password)){	//如果新密码和确认密码相同则提交数据
		SoapObject soapObject_changeUsePw = new SoapObject(namespace, methodName_changeUsePw);
		soapObject_changeUsePw.addProperty("username",string_username);
		soapObject_changeUsePw.addProperty("passwordold",string_old_password);
		soapObject_changeUsePw.addProperty("passwordnew",string_new_password);
		SoapSerializationEnvelope envelope_changeUsePw = new SoapSerializationEnvelope(SoapEnvelope.VER11);
		envelope_changeUsePw.bodyOut = soapObject_changeUsePw;
		envelope_changeUsePw.dotNet = true;	
		HttpTransportSE transport_changeUsePw = new HttpTransportSE(URL);
		transport_changeUsePw.debug = true;
		try {
				transport_changeUsePw.call(soapAction_changeUsePw, envelope_changeUsePw);
			}
			catch (Exception e) {e.printStackTrace();}
//			SoapObject object_changeUsePw = (SoapObject) envelope_changeUsePw.bodyIn;
//		    String result = object_changeUsePw.getProperty("return").toString();
		
		
		//提示修改密码成功
		Toast toast = Toast.makeText( vpn_modify.this,"密码修改成功", Toast.LENGTH_SHORT);
		toast.show();
		}
		else{														//否则提示两次密码输入不相同
			Toast toast = Toast.makeText( vpn_modify.this,"两次密码输入不相同", Toast.LENGTH_SHORT);
			toast.show();
		}
	}
	
}