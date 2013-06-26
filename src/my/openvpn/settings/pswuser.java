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
	
	//����ȫ�ֱ���
	public Boolean vpn_or_not=false;						//VPN����
	public String vpn_URL;									//�������vpnURL����
	public String ip_read = null;							//�洢�������ļ���ȡIP��ַ
	public String port_read = null;							//�洢�������ļ���ȡ�˿�
	public String client_array[][]= new String[38][2];		//�洢�����ļ��Ķ�ά����
	public String ipAddr;									//getRand���صı���IP��ַ
	public String rand;										//getRand���ص������
	public String signdata;									//getRand���ص�signdata
	public String flag;										//getRand���ص�flag��־
	public String sessionid;								//getRand���ص�sessionid

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_pswuser);
        
        final Button Login=(Button)findViewById(R.id.login);
        final Button VPN=(Button)findViewById(R.id.dakaiVPN);
        final Button shutVPN = (Button)findViewById(R.id.shutdownvpn);
        final Button update = (Button)findViewById(R.id.update);
        
        update.setOnClickListener(new View.OnClickListener() {//���ظ���
			@Override
			public void onClick(View v) {
				String downloadurl = "http://192.168.3.112:8080/downloads/myvpnsetting.apk";
				File down = downLoadFile(downloadurl);
				System.out.println(down);
				openFile(down);
			}
		});
        VPN.setOnClickListener(new View.OnClickListener() {//����VPN��ť	
        	@Override public void onClick(View v) {
        		openVPN();
        		}
        	});
        shutVPN.setOnClickListener(new View.OnClickListener() {//�ر�VPN
			@Override
			public void onClick(View v) {
				shutdownVPN();
			}
		});
        
        Login.setOnClickListener(new View.OnClickListener() {//������¼��ť
			@Override
			public void onClick(View v) {
				ReadClientConfig();
				if(vpn_or_not){								//�ж��Ƿ���VPN
					ip_read="10.0.8.1";
					port_read="80";
				}
				else {
					ip_read=client_array[36][1];
					port_read="80";
				}
				if(Login.getText().equals("��¼")){
					getRand(ip_read,port_read);					//Ȼ�󡪡���������getRand����
					authusername(ip_read,port_read);			//��Ρ�����������authusername����
					WriteClientConfig();						//��󡪡��������ͻ��������ļ�д��ȥ
					OpenURL();									//����OpenURL�����������
					Login.setText("�˳�");
				}
				else {
					disconnect(ip_read,port_read);				//�Ͽ�����
					Login.setText("��¼");
				}
			}
			
		});
    }
    
    public boolean onCreateOptionsMenu(Menu menu){//�����˵�
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater=getMenuInflater();
		inflater.inflate(R.menu.vpn_menu, menu);
		return true;
	}
    
    public boolean onOptionsItemSelected(MenuItem item){//�жϵ�������Ǹ��˵�ѡ�Ȼ�󴥷���Ӧ�Ķ���
		switch(item.getItemId()){
		case R.id.vpn_menu_config:						//���������ò˵�����ת������ҳ��
			Intent intent1 = new Intent();
			intent1.setClass(pswuser.this, vpn_config.class);
			pswuser.this.startActivity(intent1);
			return true;
		case R.id.vpn_menu_modify:						//�������޸���������ת����Ӧҳ��
			Intent intent2 = new Intent();
			intent2.setClass(pswuser.this, vpn_modify.class);
			pswuser.this.startActivity(intent2);
			return true;
		}
		return false;
	}
    
    //-----------------------------------------------------------------------
    public void ReadClientConfig(){//��������ʱ���Ƚ������ļ������ڴ�
		
		String tmp_string = null;
		int i=0;
		FileReader myFileReader = null;
		try {
			myFileReader = new FileReader("/sdcard/Client.ini");		//���Դ��ļ�
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		BufferedReader myBufferedReader = new BufferedReader(myFileReader);
		try {
			while((tmp_string = myBufferedReader.readLine())!= null) {
				client_array[i++] = tmp_string.split("=", 2);}			//��ÿһ���ԵȺ�Ϊ������ά����
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			myFileReader.close();										//�ر��ļ�
		} catch (IOException e) {
			e.printStackTrace();
		}
		Log.v("ReadClientConfig", client_array[36][1]);
	}

	//-----------------------------------------------------------------------
	public void getRand(String ip,String port){//����getRand
		
		String namespace = "http://"+ip+":"+port+"/soap/authentication/";					//�����ռ�
		String URL ="http://"+ip+":"+port+"/soap/soaps.php";								//WSDL��URL
		String methodName_getRand = "getRand";												//����
		String soapAction_getRand = "http://"+ip+":"+port+"/soap/authentication/getRand";	//����
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
		// ��ȡ���صĽ��
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
	    System.out.println(ipAddr);									//�����õ�getrand����ֵ
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
				case 0://������֤��
					if(!client_array[i][1].equals(tmpFlag))					//������߲���ȣ��������ݸ�������
					{
						client_array[i][1]=tmpFlag;
						client_array[i+17][1]=getConfig(ip,port,String.valueOf(i+1));
					}
					break;
					
				case 1://֤����
					break;
					
				case 2://CRL
					break;
					
				case 3://֤��䷢��
					if(!client_array[i][1].equals(tmpFlag))					//������߲���ȣ��������ݸ�������
					{
						client_array[i][1]=tmpFlag;
						client_array[i+17][1]=getConfig(ip,port,String.valueOf(i+1));
					}
					break;
					
			  	case 4://Ӳ���ӿڵ��÷�ʽ
			  		client_array[i][1]=tmpFlag;
					break;
					
				case 5://��ȡ֤��Ψһ��ŷ�ʽ
					client_array[i][1]=tmpFlag;			
					break;
					
				case 6://������������
					break;
					
				case 7://֤�������������  certExpiredDay
					break;
					
				case 8://��¼��ʽ
					break;
					
				case 9://����ʱ��
					if(!client_array[i][1].equals(tmpFlag))					//������߲���ȣ��������ݸ�������
					{
						client_array[i][1]=tmpFlag;
						client_array[i+17][1]=getConfig(ip,port,String.valueOf(i+1));
					}
					break;
					
				case 10://VPN��ʾ
					break;
					
				case 11://VPN�˿�
					break;
					
				case 12://������������
					break;
					
				case 13://��ȡ֤��Ψһ��ʾ������
					if(!client_array[i][1].equals(tmpFlag))					//������߲���ȣ��������ݸ�������
					{
						client_array[i][1]=tmpFlag;
						client_array[i+17][1]=getConfig(ip,port,String.valueOf(i+1));
					}
					break;
					
				case 14://�������̵ķ�ʽ
					break;
					
	           case 15://�ͻ��˷�ʽ  0 ��֧�� 1 B/S 2 C/S
	        	    client_array[i][1]=tmpFlag;
					break;
					
	           case 16://�������-----------------------------------------
	        	   if(!client_array[i][1].equals(tmpFlag))					//������߲���ȣ��������ݸ�������
					{
						client_array[i][1]=tmpFlag;
						client_array[i+17][1]=getConfig(ip,port,String.valueOf(i+1));
					}
					break;
			}		
		}
	}
	
	//-----------------------------------------------------------------------
	public String getConfig(String ip,String port,String Flag_bit){//����getConfig
		
		String namespace = "http://"+ip+":"+port+"/soap/authentication/";			//�����ռ�
		String URL ="http://"+ip+":"+port+"/soap/soaps.php";						//WSDL��URL
		String methodName_getConfig = "getConfig";									//getCongfig�ķ���
		String soapAction_getConfig = "http://"+ip+":"+port+"/soap/authentication/getConfig";//getConfig�Ķ���
		
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
	    String result_getConfig = object_getConfig.getProperty("return").toString();//��ȡ���صĽ��
		return result_getConfig;			//����õ�ֵ����
	}
	
	//-----------------------------------------------------------------------
	public void authusername(String ip,String port){//����authusername
		
		String namespace = "http://"+ip+":"+port+"/soap/authentication/";	//�����ռ�
		String URL ="http://"+ip+":"+port+"/soap/soaps.php";				//WSDL��URL
		String methodName_authusername = "authusername";							//����
    	String soapAction_authusername = "http://"+ip+":"+port+"/soap/authentication/authusername";//����
    	
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
    	String result_authusername = object_authusername.getProperty("return").toString();// ��ȡ���صĽ��

    	System.out.println(result_authusername);
	}

	//-----------------------------------------------------------------------
	public void OpenURL(){//���portURL��Ϊ����������
		
		if(getConfig(ip_read,port_read,"17")!=null){
		Uri uri=Uri.parse("http://"+ip_read+"/OcxAuth.php");		//����֤ҳ��
//		Uri uri=Uri.parse("http://"+ip_read+"/userAppAuth.php");	//��Ӧ��ҳ��
		Intent openurl=new Intent(Intent.ACTION_VIEW, uri);    
	    startActivity(openurl);
		}
	}

	//-----------------------------------------------------------------------
	public void WriteClientConfig(){//���ͻ��������ļ�д���ļ�
		
		FileWriter myFileWriter=null;
		try {
			myFileWriter =new FileWriter("/sdcard/Client.ini");		//���Դ��ļ�
		} catch (IOException e) {
			e.printStackTrace();
		}
		int i;
		for(i=0;i<38;i++)											//���ļ���д����Ϣ
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
		try {														//�ر��ļ�
			myFileWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Log.v("WriteClientConfig", client_array[36][1]);
	}
	
	//-----------------------------------------------------------------------
	public void disconnect(String ip,String port){//�Ͽ�����
		
		String namespace = "http://"+ip+":"+port+"/soap/authentication/";						//�����ռ�
		String URL ="http://"+ip+":"+port+"/soap/soaps.php";									//WSDL��URL
		String methodName_disconnect = "disconnect";											//����
    	String soapAction_disconnect = "http://"+ip+":"+port+"/soap/authentication/disconnect";	//����
		
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
	public void openVPN(){//��openVPN

		String cmd = "su -c myopenvpn.sh &";
        Runtime run = Runtime.getRuntime();					//�����뵱ǰ Java Ӧ�ó�����ص�����ʱ����  
        try {  
            run.exec(cmd);									//Ȼ��--����VPN 
            System.out.println("VPNִ�� ok!"); 
            vpn_or_not=true;
        } catch (Exception e) {
        	e.printStackTrace();  
        }  
    }
	
	public void shutdownVPN(){//�ر�openVPN
		
		String cmd = "su -c killopenvpn.sh &";
    	Runtime run = Runtime.getRuntime();				  
        try {
            run.exec(cmd);					
            vpn_or_not=false;
        } catch (Exception e) {
        	e.printStackTrace();  
        }  
		
	}

    protected File downLoadFile(String httpUrl) { //����apk�������
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
                 Toast.makeText(this, "���ӳ�ʱ", Toast.LENGTH_SHORT).show();
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

    private void openFile(File file) {//��װAPK�������
    	Log.e("OpenFile", file.getName());
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(android.content.Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(file),"application/vnd.android.package-archive");
        startActivity(intent);
        }
}
