import java.io.*;
import java.util.*;
import java.net.*;
import java.awt.*;
import java.applet.*;
import javax.swing.*;
import com.shogidojo.shogi.client.*;
import com.shogidojo.shogi.client.Koma;
public class DojoAppletViewer implements AppletStub,AppletContext{
	URL baseURL=null;
	HashMap<String,String>parameter=null;
	DojoAppletViewer(String b,HashMap<String,String>p){parameter=p;try{baseURL=new URL(b);}catch(Exception e){}}
	public static void startApplet(String base,String name,HashMap<String,String>param){
		JFrame frame=new JFrame(name);
		frame.setDefaultCloseOperation(frame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		frame.setResizable(false);
		Insets insets=frame.getInsets();
		frame.setSize(570+insets.left+insets.right,420+insets.top+insets.bottom);
		frame.setLayout(null);
		ShogiClient applet=new ShogiClient();
		applet.h(0);//setDefaultSize:DodekaSize
		
		applet.setStub(new DojoAppletViewer(base,param));
		applet.setVisible(true);
		frame.add(applet);
		applet.setSize(570,420);
		MediaTracker tracker=new MediaTracker(applet);
		int imgid=0;
		try{
			bn.soundKomaDown=applet.getAudioClip(new URL(base+"komadown.wav"));
			bn.soundKomaUp=applet.getAudioClip(new URL(base+"komaup.wav"));
			bn.soundKomaMove=applet.getAudioClip(new URL(base+"komamove.wav"));
			
			bn.soundCastle=applet.getAudioClip(new URL(base+"castle.wav"));
			bn.soundCheck=applet.getAudioClip(new URL(base+"check.wav"));
			
			tracker.addImage(bn.josekiEffectImage=applet.getImage(new URL(base+"josekiCircle.png")),imgid++);
			tracker.addImage(bn.sparkleEffectImage=applet.getImage(new URL(base+"sparkleEffect.png")),imgid++);
			tracker.addImage(bn.timeImage=applet.getImage(new URL(base+"timeImage.png")),imgid++);
			tracker.addImage(bn.komaCountImage=applet.getImage(new URL(base+"komaCount.png")),imgid++);
			tracker.addImage(bn.nariSelectImage=applet.getImage(new URL(base+"nariSelect.png")),imgid++);
			tracker.addImage(bn.castleFrontEffectImage=applet.getImage(new URL(base+"castleFrontEffect.png")),imgid++);
			tracker.addImage(bn.castleBackEffectImage=applet.getImage(new URL(base+"castleBackEffect.png")),imgid++);
			tracker.addImage(bn.pentagonRedEffectImage=applet.getImage(new URL(base+"pentagonRedEffect.png")),imgid++);
			tracker.addImage(bn.pentagonEffectImage=applet.getImage(new URL(base+"pentagonEffect.png")),imgid++);
			tracker.addImage(bn.gatherEffectImage=applet.getImage(new URL(base+"gatherEffect.png")),imgid++);
			tracker.addImage(bn.splashRedEffectImage=applet.getImage(new URL(base+"splashRedEffect.png")),imgid++);
			tracker.addImage(bn.splashBlackEffectImage=applet.getImage(new URL(base+"splashBlackEffect.png")),imgid++);
			tracker.addImage(bn.splashWhiteEffectImage=applet.getImage(new URL(base+"splashWhiteEffect.png")),imgid++);
			tracker.addImage(bn.magicalCircleEffectImage=applet.getImage(new URL(base+"magicalCircle.png")),imgid++);
			tracker.addImage(bn.noiseEffectImage=applet.getImage(new URL(base+"noiseEffect.png")),imgid++);
			tracker.addImage(bn.lastMoveImage=applet.getImage(new URL(base+"lastmove.png")),imgid++);
			tracker.addImage(bn.textFontImage=applet.getImage(new URL(base+"font.png")),imgid++);
			tracker.addImage(bn.backgroundImage=applet.getImage(new URL(base+"bg.png")),imgid++);
			tracker.addImage(Koma.komaImage=applet.getImage(new URL(base+"koma.png")),imgid++);
			tracker.addImage(Koma.komaUpImage=applet.getImage(new URL(base+"komaup.png")),imgid++);
			
			bn.loadCastleFile();
			bn.loadJosekiFile();
			
			tracker.waitForAll();
		}catch(Exception e){e.printStackTrace();}
		applet.init();
		applet.start();
	}
	public void appletResize(int w,int h){}
	public AppletContext getAppletContext(){return this;}
	public URL getCodeBase(){return baseURL;}
	public URL getDocumentBase(){return baseURL;}
	public String getParameter(String name){return parameter.get(name);}
	public boolean isActive(){return true;}
	public Applet getApplet(String name){return null;}
	public Enumeration<Applet>getApplets(){return null;}
	public InputStream getStream(String key){return null;}
	public Iterator<String>getStreamKeys(){return null;}
	public void setStream(String key,InputStream stream){}
	public void showDocument(URL url){
		System.out.println("open "+url.toString());
	}
	public void showDocument(URL url,String target){
		System.out.println("open:"+target+" "+url.toString());
	}
	public void showStatus(String status){}
	private URL resourceURL(URL url,String ext,String extalt){
		try{
			String tmp[]=url.getFile().split("/");
			String fname=tmp[tmp.length-1];
			if(ext!=null&&extalt!=null&&fname.endsWith(ext)){
				fname=fname.substring(0,fname.length()-ext.length())+extalt;
			}
			System.out.println(url+"\n > "+new File(Dojo.getResDir(),fname).toURI().toURL());
			return new File(Dojo.getResDir(),fname).toURI().toURL();
		}catch(Exception e){return null;}
	}
	public AudioClip getAudioClip(URL url){
		return Applet.newAudioClip(resourceURL(url,".au",".wav"));
	}
	public Image getImage(URL url){
		return Toolkit.getDefaultToolkit().getImage(resourceURL(url,null,null));
	}
}