package netscape.javascript;
import java.io.*;
import java.util.*;
import java.applet.Applet;
public final class JSObject{
	static HashMap<String,String>cookie=new HashMap<String,String>();
	static File getRootDir(){
		try{
			return new File(System.getProperty("java.class.path")).getCanonicalFile().getParentFile();
		}catch(Exception e){}
		return new File(".");
	}
	static File cookieFile=new File(getRootDir(),"cookiefile.txt");
	static{
		try{
			if(cookieFile.isFile()){
				System.out.println(cookieFile.getAbsolutePath());
				BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(cookieFile)));
				while(true){
					String line=br.readLine();
					if(line==null)break;
					int i=line.indexOf(":");
					if(i<=0)continue;
					cookie.put(line.substring(0,i),line.substring(i+1));
				}
			}
		}catch(Exception e){}
	}
	public Object call(String paramString, Object[] paramArrayOfObject){
		if(paramString.equals("getCookie")){
			return cookie.get((String)paramArrayOfObject[0]);
		}else if(paramString.equals("setCookie")){
			cookie.put((String)paramArrayOfObject[0],(String)paramArrayOfObject[1]);
			saveCookie();
		}
		return null;
	}
	public static JSObject getWindow(Applet paramApplet){return new JSObject();}
	static void saveCookie(){
		try{
			FileOutputStream out=new FileOutputStream(cookieFile);
			for(Map.Entry<String,String>e:cookie.entrySet()){
				out.write((e.getKey()+":"+e.getValue()+"\n").getBytes());
			}
			out.close();
		}catch(Exception e){}
	}
}