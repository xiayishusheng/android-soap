package my.openvpn.settings;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class pswuser extends Activity{
	
	/** Called when the activity is first created. */
	
	//定义全局变量
	public Boolean vpn_or_not=false;						//VPN开关
	public String vpn_URL;									//用来存放vpnURL链接
	public String ip_read = null;							//存储从配置文件获取IP地址
	public String port_read = null;							//存储从配置文件获取端口
	public String client_array[][]= new String[38][2];		//存储配置文件的二维数组
	public String ipAddr;									//getRand返回的本机IP地址
	public String rand;										//getRand返回的随机数
	public String signdata;									//getRand返回的signdata
	public String flag;										//getRand返回的flag标志
	public String sessionid;								//getRand返回的sessionid

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_pswuser);
        
        final Button Login=(Button)findViewById(R.id.login);
        final Button VPN=(Button)findViewById(R.id.dakaiVPN);
        final Button shutVPN = (Button)findViewById(R.id.shutdownvpn);
        final Button update = (Button)findViewById(R.id.update);
        
        update.setOnClickListener(new View.OnClickListener() {//下载更新
			@Override
			public void onClick(View v) {
				String downloadurl = "http://192.168.3.112:8080/downloads/myvpnsetting.apk";
				File down = downLoadFile(downloadurl);
				System.out.println(down);
				openFile(down);
			}
		});
        VPN.setOnClickListener(new View.OnClickListener() {//监听VPN按钮	
        	@Override public void onClick(View v) {
        		openVPN();
        		}
        	});
        shutVPN.setOnClickListener(new View.OnClickListener() {//关闭VPN
			@Override
			public void onClick(View v) {
				shutdownVPN();
			}
		});
        
        Login.setOnClickListener(new View.OnClickListener() {//监听登录按钮
			@Override
			public void onClick(View v) {
				ReadClientConfig();
				if(vpn_or_not){								//判断是否建立VPN
					ip_read="10.0.8.1";
					port_read="80";
				}
				else {
					ip_read=client_array[36][1];
					port_read="80";
				}
				if(Login.getText().equals("登录")){
					getRand(ip_read,port_read);					//然后————调用getRand方法
					authusername(ip_read,port_read);			//其次————调用authusername方法
					WriteClientConfig();						//最后————将客户端配置文件写回去
					OpenURL();									//调用OpenURL方法打开浏览器
					Login.setText("退出");
				}
				else {
					disconnect(ip_read,port_read);				//断开连接
					Login.setText("登录");
				}
			}
			
		});
    }
    
    public boolean onCreateOptionsMenu(Menu menu){//创建菜单
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater=getMenuInflater();
		inflater.inflate(R.menu.vpn_menu, menu);
		return true;
	}
    
    public boolean onOptionsItemSelected(MenuItem item){//判断点击的是那个菜单选项，然后触发相应的动作
		switch(item.getItemId()){
		case R.id.vpn_menu_config:						//如果点击配置菜单则跳转到配置页面
			Intent intent1 = new Intent();
			intent1.setClass(pswuser.this, vpn_config.class);
			pswuser.this.startActivity(intent1);
			return true;
		case R.id.vpn_menu_modify:						//如果点击修改密码则跳转到相应页面
			Intent intent2 = new Intent();
			intent2.setClass(pswuser.this, vpn_modify.class);
			pswuser.this.startActivity(intent2);
			return true;
		}
		return false;
	}
    
    //-----------------------------------------------------------------------
    public void ReadClientConfig(){//程序启动时首先将配置文件读入内存
		
		String tmp_string = null;
		int i=0;
		FileReader myFileReader = null;
		try {
			myFileReader = new FileReader("/sdcard/Client.ini");		//尝试打开文件
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		BufferedReader myBufferedReader = new BufferedReader(myFileReader);
		try {
			while((tmp_string = myBufferedReader.readLine())!= null) {
				client_array[i++] = tmp_string.split("=", 2);}			//将每一行以等号为界存入二维数组
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			myFileReader.close();										//关闭文件
		} catch (IOException e) {
			e.printStackTrace();
		}
		Log.v("ReadClientConfig", client_array[36][1]);
	}

	//-----------------------------------------------------------------------
	public void getRand(String ip,String port){//调用getRand
		
		String namespace = "http://"+ip+":"+port+"/soap/authentication/";					//命名空间
		String URL ="http://"+ip+":"+port+"/soap/soaps.php";								//WSDL的URL
		String methodName_getRand = "getRand";												//方法
		String soapAction_getRand = "http://"+ip+":"+port+"/soap/authentication/getRand";	//动作
		final EditText Username_VPN=(EditText)findViewById(R.id.Username);
		String username=Username_VPN.getText().toString();
		
		SoapObject soapObject_getRand = new SoapObject(namespace, methodName_getRand);
		soapObject_getRand.addProperty("certid", username);
		SoapSerializationEnvelope envelope_getRand = new SoapSerializationEnvelope(SoapEnvelope.VER11);
		envelope_getRand.bodyOut = soapObject_getRand;
		envelope_getRand.dotNet = true;
		HttpTransportSE transport_getRand = new HttpTransportSE(URL);
		transport_getRand.debug = true;
		try {
			transport_getRand.call(soapAction_getRand, envelope_getRand);
		}
		catch (Exception e) {e.printStackTrace();}
		SoapObject object_getRand = (SoapObject) envelope_getRand.bodyIn;
		// 获取返回的结果
	    String result_getRand = object_getRand.getProperty("return").toString();
	    String array_getRand[]=result_getRand.split(";",6);
	    String[] array_ipAddr=array_getRand[0].split("=",2);
	    ipAddr=array_ipAddr[1];										//ipAddr
	    String[] array_rand=array_getRand[1].split("=",2);
	    rand=array_rand[1];											//rand
	    String[] array_signdata=array_getRand[2].split("=",2);
	    signdata=array_signdata[1];									//signdata
	    String[] array_flag=array_getRand[3].split("=",2);
	    flag=array_flag[1];											//flag
	    String[] array_sessionid=array_getRand[4].split("=",2);
	    sessionid=array_sessionid[1];								//sessionid
	    System.out.println(ipAddr);									//输出获得的getrand返回值
	    System.out.println(rand);
	    System.out.println(signdata);
	    System.out.println(flag);
	    System.out.println(sessionid);
	    
	    int i=0;
	    for(i=0;i<flag.length();i++)
		{
	    	String tmpFlag;
			tmpFlag=flag.substring(i, i+1);
			
			switch(i)
			{
				case 0://服务器证书
					if(!client_array[i][1].equals(tmpFlag))					//如果两者不相等，将新数据赋给数组
					{
						client_array[i][1]=tmpFlag;
						client_array[i+17][1]=getConfig(ip,port,String.valueOf(i+1));
					}
					break;
					
				case 1://证书链
					break;
					
				case 2://CRL
					break;
					
				case 3://证书颁发者
					if(!client_array[i][1].equals(tmpFlag))					//如果两者不相等，将新数据赋给数组
					{
						client_array[i][1]=tmpFlag;
						client_array[i+17][1]=getConfig(ip,port,String.valueOf(i+1));
					}
					break;
					
			  	case 4://硬件接口调用方式
			  		client_array[i][1]=tmpFlag;
					break;
					
				case 5://获取证书唯一编号方式
					client_array[i][1]=tmpFlag;			
					break;
					
				case 6://过期提醒天数
					break;
					
				case 7://证书过期提醒天数  certExpiredDay
					break;
					
				case 8://登录方式
					break;
					
				case 9://心跳时间
					if(!client_array[i][1].equals(tmpFlag))					//如果两者不相等，将新数据赋给数组
					{
						client_array[i][1]=tmpFlag;
						client_array[i+17][1]=getConfig(ip,port,String.valueOf(i+1));
					}
					break;
					
				case 10://VPN标示
					break;
					
				case 11://VPN端口
					break;
					
				case 12://过期提醒内容
					break;
					
				case 13://获取证书唯一标示的内容
					if(!client_array[i][1].equals(tmpFlag))					//如果两者不相等，将新数据赋给数组
					{
						client_array[i][1]=tmpFlag;
						client_array[i+17][1]=getConfig(ip,port,String.valueOf(i+1));
					}
					break;
					
				case 14://监听进程的方式
					break;
					
	           case 15://客户端方式  0 都支持 1 B/S 2 C/S
	        	    client_array[i][1]=tmpFlag;
					break;
					
	           case 16://打开浏览器-----------------------------------------
	        	   if(!client_array[i][1].equals(tmpFlag))					//如果两者不相等，将新数据赋给数组
					{
						client_array[i][1]=tmpFlag;
						client_array[i+17][1]=getConfig(ip,port,String.valueOf(i+1));
					}
					break;
			}		
		}
	}
	
	//-----------------------------------------------------------------------
	public String getConfig(String ip,String port,String Flag_bit){//调用getConfig
		
		String namespace = "http://"+ip+":"+port+"/soap/authentication/";			//命名空间
		String URL ="http://"+ip+":"+port+"/soap/soaps.php";						//WSDL的URL
		String methodName_getConfig = "getConfig";									//getCongfig的方法
		String soapAction_getConfig = "http://"+ip+":"+port+"/soap/authentication/getConfig";//getConfig的动作
		
		SoapObject soapObject_getConfig = new SoapObject(namespace, methodName_getConfig);
		soapObject_getConfig.addProperty("type", Flag_bit);
		SoapSerializationEnvelope envelope_getConfig = new SoapSerializationEnvelope(SoapEnvelope.VER11);
		envelope_getConfig.bodyOut = soapObject_getConfig;
		envelope_getConfig.dotNet = true;
		HttpTransportSE transport_getConfig = new HttpTransportSE(URL);
		transport_getConfig.debug = true;
		try {
			transport_getConfig.call(soapAction_getConfig, envelope_getConfig);
		}
		catch (Exception e) {e.printStackTrace();}
		SoapObject object_getConfig = (SoapObject) envelope_getConfig.bodyIn;
	    String result_getConfig = object_getConfig.getProperty("return").toString();//获取返回的结果
		return result_getConfig;			//将获得的值返回
	}
	
	//-----------------------------------------------------------------------
	public void authusername(String ip,String port){//调用authusername
		
		String namespace = "http://"+ip+":"+port+"/soap/authentication/";	//命名空间
		String URL ="http://"+ip+":"+port+"/soap/soaps.php";				//WSDL的URL
		String methodName_authusername = "authusername";							//方法
    	String soapAction_authusername = "http://"+ip+":"+port+"/soap/authentication/authusername";//动作
    	
    	final EditText Username_VPN=(EditText)findViewById(R.id.Username);
        final EditText Password_VPN=(EditText)findViewById(R.id.Password);
    	String username=Username_VPN.getText().toString();
        String password=Password_VPN.getText().toString();
    	SoapObject soapObject_authusername = new SoapObject(namespace, methodName_authusername);
    	soapObject_authusername.addProperty("useranme", username);
    	soapObject_authusername.addProperty("password", password);
    	soapObject_authusername.addProperty("clientip", ip);
    	SoapSerializationEnvelope envelope_authusername = new SoapSerializationEnvelope(SoapEnvelope.VER11);
    	envelope_authusername.bodyOut = soapObject_authusername;
    	envelope_authusername.dotNet = true;
    	HttpTransportSE transport_authusername = new HttpTransportSE(URL);
    	transport_authusername.debug = true;
		try {
			transport_authusername.call(soapAction_authusername, envelope_authusername);
		}
		catch (Exception e) {e.printStackTrace();}
		SoapObject object_authusername = (SoapObject) envelope_authusername.bodyIn;
    	String result_authusername = object_authusername.getProperty("return").toString();// 获取返回的结果

    	System.out.println(result_authusername);
	}

	//-----------------------------------------------------------------------
	public void OpenURL(){//如果portURL不为空则打开浏览器
		
		if(getConfig(ip_read,port_read,"17")!=null){
		Uri uri=Uri.parse("http://"+ip_read+"/OcxAuth.php");		//打开认证页面
//		Uri uri=Uri.parse("http://"+ip_read+"/userAppAuth.php");	//打开应用页面
		Intent openurl=new Intent(Intent.ACTION_VIEW, uri);    
	    startActivity(openurl);
		}
	}

	//-----------------------------------------------------------------------
	public void WriteClientConfig(){//将客户端配置文件写入文件
		
		FileWriter myFileWriter=null;
		try {
			myFileWriter =new FileWriter("/sdcard/Client.ini");		//尝试打开文件
		} catch (IOException e) {
			e.printStackTrace();
		}
		int i;
		for(i=0;i<38;i++)											//向文件里写入信息
		{
			try {
				myFileWriter.write(client_array[i][0]);
				myFileWriter.write("=");
				myFileWriter.write(client_array[i][1]);
				myFileWriter.write("\n");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {														//关闭文件
			myFileWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Log.v("WriteClientConfig", client_array[36][1]);
	}
	
	//-----------------------------------------------------------------------
	public void disconnect(String ip,String port){//断开连接
		
		String namespace = "http://"+ip+":"+port+"/soap/authentication/";						//命名空间
		String URL ="http://"+ip+":"+port+"/soap/soaps.php";									//WSDL的URL
		String methodName_disconnect = "disconnect";											//方法
    	String soapAction_disconnect = "http://"+ip+":"+port+"/soap/authentication/disconnect";	//动作
		
		final EditText Username_VPN=(EditText)findViewById(R.id.Username);
		String username=Username_VPN.getText().toString();
		SoapObject soapObject_disconnect = new SoapObject(namespace, methodName_disconnect);
		soapObject_disconnect.addProperty("certid", username);
		SoapSerializationEnvelope envelope_disconnect = new SoapSerializationEnvelope(SoapEnvelope.VER11);
		envelope_disconnect.bodyOut = soapObject_disconnect;
		envelope_disconnect.dotNet = true;	
		HttpTransportSE transport_disconnect = new HttpTransportSE(URL);
		transport_disconnect.debug = true;
		try {
				transport_disconnect.call(soapAction_disconnect, envelope_disconnect);
			}
			catch (Exception e) {e.printStackTrace();}
//			SoapObject object_disconnect = (SoapObject) envelope_disconnect.bodyIn;
//		    String result = object_disconnect.getProperty("return").toString();
	}

	//-----------------------------------------------------------------------
	public void openVPN(){//打开openVPN

		String cmd = "su -c myopenvpn.sh &";
        Runtime run = Runtime.getRuntime();					//返回与当前 Java 应用程序相关的运行时对象  
        try {  
            run.exec(cmd);									//然后--启动VPN 
            System.out.println("VPN执行 ok!"); 
            vpn_or_not=true;
        } catch (Exception e) {
        	e.printStackTrace();  
        }  
    }
	
	public void shutdownVPN(){//关闭openVPN
		
		String cmd = "su -c killopenvpn.sh &";
    	Runtime run = Runtime.getRuntime();				  
        try {
            run.exec(cmd);					
            vpn_or_not=false;
        } catch (Exception e) {
        	e.printStackTrace();  
        }  
		
	}

    protected File downLoadFile(String httpUrl) { //下载apk程序代码
    	final String fileName = "vpn.apk";
        File tmpFile = new File("/sdcard/update");
        if (!tmpFile.exists()) {
        	tmpFile.mkdir();
        }
        final File file = new File("/sdcard/update/" + fileName);
        try {
        	URL url = new URL(httpUrl);
            try {
                 HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                 InputStream is = conn.getInputStream();
                 FileOutputStream fos = new FileOutputStream(file);
                 byte[] buf = new byte[1024];
                 conn.connect();
                 double count = 0;
                 if (conn.getResponseCode() >= 400) {
                 Toast.makeText(this, "连接超时", Toast.LENGTH_SHORT).show();
                 } 
                 else {
                	 while (count <= 100) {
                     if (is != null) {
                    	 int numRead = is.read(buf);
                         if (numRead <= 0) {
                              break;
                           } 
                           else {
                        	   fos.write(buf, 0, numRead);
                           }
                     } 
                     else {
                           break;
                          }

                      }
                 }
         conn.disconnect();
         fos.close();
         is.close();
         } catch (IOException e) {
           e.printStackTrace();
                            }
         } catch (MalformedURLException e) {
           e.printStackTrace();
           }
        return file;
}

    private void openFile(File file) {//安装APK程序代码
    	Log.e("OpenFile", file.getName());
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(android.content.Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(file),"application/vnd.android.package-archive");
        startActivity(intent);
        }
}
