package my.openvpn.settings;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class vpn_config extends Activity{
	private Button saveButton = null;
	private String array2[][]= new String[38][2];
	private EditText ipaddress=null;
	private EditText port=null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.vpn_config);
		
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
				array2[i++] = tmp_string.split("=", 2);}
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			myFileReader.close();										//关闭文件
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		ipaddress=(EditText)findViewById(R.id.ipaddress);
	    port=(EditText)findViewById(R.id.port);
	    ipaddress.setText(array2[36][1]);
	    port.setText(array2[37][1]);
	    
		saveButton = (Button)findViewById(R.id.save);
		saveButton.setOnClickListener(new saveButtonListener());
	}

	class saveButtonListener implements OnClickListener{//按钮监听
		@Override
		public void onClick(View v) {
	        WriteIpConfig();
		}
	}
	
	public void WriteIpConfig(){//点击保存按钮时触发
		
		ipaddress=(EditText)findViewById(R.id.ipaddress);
	    port=(EditText)findViewById(R.id.port);
	    String ip=ipaddress.getText().toString();
		String ports=port.getText().toString();
		int i=0;
		array2[36][1]=ip;
		array2[37][1]=ports;
		FileWriter f = null;
		try {
			f = new FileWriter("/sdcard/Client.ini");
		} catch (IOException e) {
			e.printStackTrace();
		}
		for(i=0;i<38;i++)
		{
			try {
				f.write(array2[i][0]);
				f.write("=");
				f.write(array2[i][1]);
				f.write("\n");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			f.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//提示写入成功
		Toast toast = Toast.makeText( vpn_config.this,"配置保存成功" , Toast.LENGTH_SHORT);
		toast.show();
	}
	
}
