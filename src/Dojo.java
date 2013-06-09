import java.io.*;
import java.net.*;
import java.util.HashMap;
public class Dojo{
	static File root=null,res=null;
	public static File getRootDir(){
		// try{
		// 	if(root==null)root=new File(System.getProperty("java.class.path")).getCanonicalFile().getParentFile();
		// 	return root;
		// }catch(Exception e){}
		return new File(".");
	}
	public static File getResDir(){
		if(res==null)res=new File(getRootDir(),"res");
		return res;
	}

	public static void start(String base,String name,HashMap<String,String>param)throws Exception{
		File jar=null;
		int ver=-1;
		for(File file:getRootDir().listFiles()){
			String fname=file.getName();
			if(fname.matches("dojo[0-9]+\\.jar")){
				int v=Integer.parseInt(fname.substring(4,fname.length()-4));
				if(ver<v){ver=v;jar=file;}
			}
		}
		System.out.println("version: "+ver);
		URL[]pathlist={new File(getResDir(),"dojo.jar").toURI().toURL(),jar.toURI().toURL()};
		URLClassLoader.newInstance(pathlist).loadClass("DojoAppletViewer").getMethod("startApplet",String.class,String.class,param.getClass()).invoke(null,base,name,param);
	}
}
