package com.shogidojo.shogi.client;

import com.shogidojo.shogi.common.*;
import java.applet.AudioClip;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.awt.geom.*;
import java.util.*;

class ResLoader{
	static File getRootDir(){
		try{
			return new File(System.getProperty("java.class.path")).getCanonicalFile().getParentFile();
		}catch(Exception e){}
		return new File(".");
	}
	static File getResFile(String path){
		File res=new File(getRootDir(),"res");
		return new File(res,path);
	}
}

class CheckLeaveChecker{
	int map[][]=new int[9][9];
	void init(int[][]m){
		for(int x=0;x<9;x++)for(int y=0;y<9;y++)map[x][y]=m[x][y];
	}
	boolean move(int x,int y,int x2,int y2){
		int prev=map[x2][y2];
		map[x2][y2]=map[x][y];
		map[x][y]=0;
		return prev==Koma.OU;
	}
}

interface CloneNode{public void updateClone();}
class CloneTextField extends TextField implements CloneNode{
	TextField textfield;
	CloneTextField(TextField t){
		textfield=t;
		setText(t.getText());
		setSize(t.getWidth(),t.getHeight());
		updateClone();
	}
	public void updateClone(){
		if(isEditable()!=textfield.isEditable())setEditable(textfield.isEditable());
		String text=getText(),text2=textfield.getText();
		if(text!=text2&&text!=null&&!text.equals(text2))setText(text2);
	}
}
class CloneCheckbox extends Checkbox implements CloneNode{
	Checkbox checkbox;
	CloneCheckbox(Checkbox c){
		checkbox=c;
		setLabel(c.getLabel());
		setState(c.getState());
		setSize(c.getWidth(),c.getHeight());
		updateClone();
	}
	public void updateClone(){
		if(getState()!=checkbox.getState())setState(checkbox.getState());
	}
}
class CloneButton extends Button implements ActionListener,CloneNode{
	Button button;
	CloneButton(Button b){
		button=b;
		setLabel(b.getLabel());
		setSize(b.getWidth(),b.getHeight());
		addActionListener(this);
		updateClone();
	}
	public void updateClone(){
		if(isEnabled()!=button.isEnabled())setEnabled(button.isEnabled());
	}
	public void actionPerformed(ActionEvent e){
		if(!button.isEnabled())return;
		ActionEvent event=new ActionEvent(button,e.getID(),button.getActionCommand());
		for(ActionListener listener:button.getActionListeners()){
			listener.actionPerformed(event);
		}
	}
}

class PlayerInfo{
	String atime,ctime,name,rate;
	boolean flagBW,flagTurn;
	public String toString(){return "{"+name+" "+rate+" "+atime+" / "+ctime+"}";}
	public boolean equals(Object o){
		if(o==null)return false;
		PlayerInfo p=(PlayerInfo)o;		
		return (atime==p.atime||(atime!=null&&atime.equals(p.atime)))
			&&(ctime==p.ctime||(ctime!=null&&ctime.equals(p.ctime)))
			&&(name==p.name||(name!=null&&name.equals(p.name)))
			&&(rate==p.rate||(rate!=null&&rate.equals(p.rate)))
			&&(flagBW==p.flagBW)&&(flagTurn==p.flagTurn);
	}
}
class Action{
	private boolean aliveFlag=true;
	final void terminate(){aliveFlag=false;}
	final boolean isAlive(){return aliveFlag;}
	boolean cancel(){return false;}
	void run(){}
}
class MKoma{
	MKoma(int k,boolean p,int n){koma=new Koma(k,p,false);count=n;}
	Koma koma;
	int count;
}
class MouseState{
	int x,y;
	boolean pressed;
	public MouseState(int x,int y,boolean p){this.x=x;this.y=y;pressed=p;}
}
class Effect{
	double zIndex;
	private boolean visibleFlag=true,aliveFlag=true;
	final void setVisible(boolean b){visibleFlag=b;}
	final boolean isVisible(){return visibleFlag;}
	final void terminate(){aliveFlag=false;}
	final boolean isAlive(){return aliveFlag;}
	void cancel(){}
	void run(MouseState mouse){run();}
	void run(){}
	void render(Graphics2D g){}
}
class EffectList{
	private static final int EFFECT_MAX=65536;
	private Effect array[]=new Effect[EFFECT_MAX];
	private int length=0;
	private int zindex_count=0;
	Comparator<Effect> comparator=new Comparator<Effect>(){public int compare(Effect e1,Effect e2){double x=e1.zIndex-e2.zIndex;return x>0?1:x<0?-1:0;}};
	synchronized void add(Effect e,int z){
		array[length++]=e;
		e.zIndex=z+(zindex_count++)/(double)0x80000000L;
	}
	synchronized void render(Graphics2D g){
		Arrays.sort(array,0,length,comparator);
		for(int i=0;i<length;i++){
			Effect e=array[i];
			if(e.isVisible())e.render(g);
		}
	}
	synchronized boolean runAll(MouseState mouse){
		boolean visible=false;
		int i=0;
		for(int j=0;j<length;j++){
			Effect e=array[j];
			array[j]=null;
			e.run(mouse);
			if(e.isAlive()){
				array[i++]=e;
				if(e.isVisible())visible=true;
			}
		}
		length=i;
		return visible;
	}
	synchronized void clear(){for(int i=0;i<length;i++)array[i]=null;length=0;}
}

class SplashEffect extends Effect{
	int X,Y,S,T=9;
	int count=-1;
	int color;
	static final int BLACK=0,RED=1,WHITE=2;
	SplashEffect(int x,int y,int s,int c){X=x;Y=y;S=s;color=c;}
	void run(){if(++count>=T)terminate();}
	void render(Graphics2D g){
		int x=X-S/2,y=Y-S/2,sx=count%3*256,sy=count/3*256;
		Image img=color==BLACK?bn.splashBlackEffectImage:color==RED?bn.splashRedEffectImage:bn.splashWhiteEffectImage;
		g.drawImage(img,x,y,x+S,y+S,sx,sy,sx+256,sy+256,null);
	}
}
class GatherEffect extends Effect{
	int X,Y,T=16;
	int count=-1;
	GatherEffect(int x,int y){X=x;Y=y;}
	void run(){if(++count>=T)terminate();}
	void render(Graphics2D g){
		Composite composite=g.getComposite();
		float t=(float)count/T;
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,1-t*t));
		int sx=120*(count%4),sy=120*(count/4);
		g.drawImage(bn.gatherEffectImage,X-60,Y-60,X+60,Y+60,sx,sy,sx+120,sy+120,null);
		g.setComposite(composite);
	}
}

class NoiseCellEffect extends Effect{
	int X,Y,W,H,T=20;
	int count=-1;
	NoiseCellEffect(int x,int y,int w,int h){X=x;Y=y;W=w;H=h;}
	void run(){if(++count>=T)terminate();}
	void render(Graphics2D g){
		Composite composite=g.getComposite();
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,(float)(T-count)/T));
		int sx=36*(count%16%4),sy=40*(count%16/4);
		g.drawImage(bn.noiseEffectImage,X,Y,X+W,Y+H,sx,sy,sx+36,sy+40,null);
		g.setComposite(composite);
	}
}

class MagicalKomaEffect extends Effect{
	int X,Y,W,H,R=100,T=18;
	double theta0,alpha;
	int count=-1,endCount=0;
	boolean cancelFlag=false;
	Koma koma;
	MagicalKomaEffect(int x,int y,int w,int h,double a,Koma k){
		X=x;Y=y;W=w;H=h;theta0=2*Math.PI*Math.random();alpha=a;koma=k;
	}
	void run(){count++;if(cancelFlag&&++endCount>=T)terminate();}
	void render(Graphics2D g){
		Composite composite=g.getComposite();
		double t=(double)count/T;
		int r=(int)(R*(1-Math.exp(-1.5*t-0.3)));
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,(float)(alpha*(1-(float)endCount/T))));
		AffineTransform trans=g.getTransform();
		g.translate(X+W/2,Y+H/2);
		g.rotate(Math.PI/4*t+theta0);
		g.drawImage(bn.magicalCircleEffectImage,-r,-r,2*r,2*r,null);
		g.setTransform(trans);
		for(int i=0;i<3;i++){
			double tt=Math.PI/4*t+Math.PI*2/3*i+theta0;
			g.translate(X+W/2+(r+(1-i)*r/4)*Math.cos(tt),Y+H/2-(r+(1-i)*r/4)*Math.sin(tt));
			g.rotate(Math.PI/4*t+5*i);
			int s=r/4+(1-i)*r/12;
			g.drawImage(bn.magicalCircleEffectImage,-s,-s,2*s,2*s,null);
			g.setTransform(trans);
		}
		if(!cancelFlag){
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,(float)Math.min((float)count/6,1)));
			koma.drawUpImage(g,X,Y,W,H);
		}
		g.setComposite(composite);
	}
	void cancel(){cancelFlag=true;}
}

class KomaMoveEffect extends Effect{
	int X1,Y1,X2,Y2,W,H;
	double dx,dy;
	Koma koma;
	double phase=0;
	KomaMoveEffect(int x1,int y1,int x2,int y2,int w,int h,Koma k){
		X1=x1;Y1=y1;X2=x2;Y2=y2;W=w;H=h;koma=k;
		dx=X2-X1;dy=Y2-Y1;
		double r=Math.sqrt(dx*dx+dy*dy);dx/=r;dy/=r;
	}
	void run(){
		if(phase>=3)terminate();
	}
	void render(Graphics2D g){
		Composite composite=g.getComposite();
		if(phase<1){
			double alpha=(1-phase),t=(phase*phase+phase)/2;
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,(float)alpha));
			koma.drawUpImage(g,X1+(int)(t*dx*W/2),Y1+(int)(t*dy*W/2),W,H);
		}else if(phase<2){
			double alpha=(phase-1),t=((2-phase)*(2-phase)+(2-phase))/2;
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,(float)alpha));
			koma.drawUpImage(g,X2-(int)(t*dx*W/2),Y2-(int)(t*dy*W/2),W,H);
		}else if(phase<3){
			koma.drawUpImage(g,X2,Y2,W,H);
		}
		g.setComposite(composite);
	}
}

public class bn extends Canvas implements MouseListener,MouseMotionListener,bj,bm{
	public static Image noiseEffectImage,magicalCircleEffectImage,splashBlackEffectImage,splashRedEffectImage,splashWhiteEffectImage,gatherEffectImage,pentagonEffectImage,pentagonRedEffectImage,castleBackEffectImage,castleFrontEffectImage,sparkleEffectImage,josekiEffectImage;
	public static AudioClip soundKomaDown,soundKomaUp,soundKomaMove,soundCheck,soundCastle;
	public static Image textFontImage,nariSelectImage,komaCountImage,timeImage;
	public static Image lastMoveImage;
	private Point lastMovePoint;
	private boolean currentPlayerBW=true;
	private bk shogi;
	private boolean flipFlag=true;
	
	private 	CheckLeaveChecker checkLeaveChecker=new CheckLeaveChecker();
	
	private int offsetX,offsetY,cellW,cellH,banW,banH,mkomaX,mkomaW,mkomaHOffset,W,H,canvasW,canvasH;
	private Image dblbufImage,banbufImage;
	public static Image backgroundImage;
	
	private Koma[][]map=new Koma[9][9];
	private MKoma[][]mkoma=new MKoma[2][9];
	private static enum MODE{NONE,SRCSELECT,DSTSELECT,NARISELECT,WAIT};
	private MODE mode;
	Point pointSrcSelect,pointDstSelect;
	
	EffectList effectList=new EffectList();
	boolean hadEffect=false;
	boolean banChanged=false;
	MouseState mouse=new MouseState(-1,-1,false);
	private void render(){
		boolean hasEffect=effectList.runAll(mouse);
		if(hadEffect||hasEffect||banChanged){
			Graphics2D g=(Graphics2D)dblbufImage.getGraphics();
			Graphics canvasGraphics=getGraphics();
			if(canvasGraphics==null)return;
			banChanged=false;
			g.drawImage(banbufImage,0,0,null);
			if(lastMovePoint!=null){
				g.drawImage(lastMoveImage,offsetX+cellW*lastMovePoint.x+1,offsetY+cellH*lastMovePoint.y+1,null);
			}
			effectList.render(g);
			g.dispose();
			paint(canvasGraphics);
		}
		hadEffect=hasEffect;
	}
	
	private LinkedList<Action> actionQueue=new LinkedList<Action>();
	private void addAction(Action action){synchronized(actionQueue){actionQueue.add(action);}}
	private Thread actionThread=null;
	private void actionThreadStart(){
		if(actionThread!=null)return;
		actionThread=new Thread(){public void run(){runLoop();}};
		actionThread.start();
	}
	private void actionThreadStop(){actionThread=null;}
	private PlayerInfo playerinfo0,playerinfo1;
	private void runLoop(){
		try{
		while(actionThread!=null){
			long time1=System.currentTimeMillis();
			synchronized(actionQueue){
				Action action=null;
				while(true){
					if(actionQueue.size()==0)break;
					action=actionQueue.getFirst();
					if(action.isAlive())break;
					actionQueue.removeFirst();
					action=null;
				}
				try{
					if(action!=null)action.run();
					defaultAction();
					render();
					PlayerInfo infoarr[]=getPlayerInfoArray();
					infoarr[0].flagBW=!flipFlag;
					infoarr[1].flagBW=flipFlag;
					infoarr[0].flagTurn=infoarr[0].flagBW==currentPlayerBW;
					infoarr[1].flagTurn=infoarr[1].flagBW==currentPlayerBW;
					if(!infoarr[1].equals(playerinfo0)){
						boolean tuflag=isPInfoTimeUpdated(playerinfo0,infoarr[1]);
						renderPlayerInfo(true,playerinfo0=infoarr[1],tuflag);
					}
					if(!infoarr[0].equals(playerinfo1)){
						boolean tuflag=isPInfoTimeUpdated(playerinfo1,infoarr[0]);
						renderPlayerInfo(false,playerinfo1=infoarr[0],tuflag);
					}
					resetLayout();
					for(int i=0;i<cloneNodes.length;i++)if(cloneNodes[i]!=null)cloneNodes[i].updateClone();
				}catch(Exception e){e.printStackTrace();}
			}
			long time2=System.currentTimeMillis();
			int sleeptime=20-(int)(time2-time1);
			if(sleeptime>0)try{Thread.sleep(sleeptime);}catch(Exception e){}
		}
		}catch(Exception e){e.printStackTrace();}
	}
	
	private boolean isPInfoTimeUpdated(PlayerInfo o,PlayerInfo n){
		if(o==null||o.equals(n))return false;
		if(o.name==null||!o.name.equals(n.name))return false;
		if(o.flagTurn!=n.flagTurn||o.flagBW!=n.flagBW)return false;
		return true;
	}
	
	private void defaultAction(){
		
	}
	
	public bn(bk shogidata){
		System.err.println("init");
		shogi=shogidata;
		addMouseListener(this);
		addMouseMotionListener(this);
		actionThreadStart();
	}
	
	private String explodeComponent(Component c,String tab){
		if(c.getClass().equals(Label.class))return tab+"Label: "+((Label)c).getText()+"\n";
		if(c.getClass().equals(TextField.class))return tab+"TextField: "+((TextField)c).getText()+"\n";
		if(!Container.class.isInstance(c))return tab+c.getClass().getName()+"\n";
		Container container=(Container)c;
		String data=tab+container.getClass().getName()+"{\n";
		for(int i=0;i<container.getComponentCount();i++){
			data+=explodeComponent(container.getComponent(i),tab+"\t");
		}
		return data+tab+"}\n";
	}
	private void printComponent(Container container){
		System.out.println(container.getClass()+"{");
		for(int i=0;i<container.getComponentCount();i++){
			Component c=container.getComponent(i);
			Class cls=c.getClass();
			if(cls==Label.class){
				System.out.println(cls+((Label)c).getText());
			}else if(cls==TextField.class){
				System.out.println(cls+((TextField)c).getText());
			}else if(cls==Panel.class||cls==Container.class){
				printComponent((Container)c);
			}else{
				System.out.println(cls);
			}
		}
		System.out.println("}");
	}
	private PlayerInfo[] getPlayerInfoArray(){
		PlayerInfo[]arr={new PlayerInfo(),new PlayerInfo()};
		try{
			Container container=getParent().getParent();
			for(int i=0;i<container.getComponentCount();i++){
				Component c=container.getComponent(i);
				if(c.getClass().equals(Label.class)){
					arr[arr[0].rate==null?0:1].rate=((Label)c).getText();
				}
			}
			for(int i=0;i<2;i++){
				Panel panel=(Panel)container.getComponent(2+i);
				int count=panel.getComponentCount();
				if(count==5){
					arr[i].name=((Label)panel.getComponent(4)).getText();
					arr[i].atime=((Label)panel.getComponent(1)).getText();
					arr[i].ctime=((Label)panel.getComponent(2)).getText();
				}
				if(count==4){
					arr[i].name=((Label)panel.getComponent(3)).getText();
					Label text=(Label)panel.getComponent(1);
					Color bg=text.getBackground();
					String time=text.getText();
					if(bg==null||bg.equals(Color.white)){arr[i].atime=time;arr[i].ctime=null;}
					else{arr[i].atime=null;arr[i].ctime=time;}
				}
			}
		}catch(Exception e){e.printStackTrace();}
		return arr;
	}
	private void deleteCoords(){
		try{
			Container container=getParent();
			if(container==null)return;
			for(int i=container.getComponentCount()-1;i>=0;i--){
				Component c=container.getComponent(i);
				if(c.getClass().equals(Label.class))container.remove(c);
			}
			container.setBackground(null);
		}catch(Exception e){e.printStackTrace();}
	}
	CloneNode[]cloneNodes=new CloneNode[6];
	private CloneNode createClone(Component c){
		if(Button.class.isInstance(c))return new CloneButton((Button)c);
		if(TextField.class.isInstance(c))return new CloneTextField((TextField)c);
		if(Checkbox.class.isInstance(c))return new CloneCheckbox((Checkbox)c);
		return null;
	}
	private void resetLayout(){
		for(int i=0;i<cloneNodes.length;i++){
			if(cloneNodes[i]==null)break;
			if(i==cloneNodes.length-1)return;
		}
		try{
			Container container=(Container)getParent().getParent();
			Panel headPanel=(Panel)container.getComponent(0);
			
			Panel endPanel=(Panel)container.getComponent(9);
			for(int i=2;i>=0;i--){
				if(cloneNodes[i]==null){
					Component c=endPanel.getComponent(i);
					if(c!=null){
						Component c2=(Component)(cloneNodes[i]=createClone(c));
						c2.setLocation(300+48*i,6);
						headPanel.add(c2);
						c.getParent().remove(c);
					}
				}
			}
			Panel footPanel=(Panel)container.getComponent(4);
			Panel vPanel=(Panel)container.getComponent(7);
			for(int i=2;i>=0;i--){
				if(cloneNodes[3+i]==null){
					Component c=vPanel.getComponent(i);
					if(c!=null){
						Component c2=(Component)(cloneNodes[3+i]=createClone(c));
						c2.setLocation(i==0?330:i==1?385:425,1);
						footPanel.add(c2);
						c.getParent().remove(c);
					}
				}
			}
			container.getComponent(2).setVisible(false);
			container.getComponent(3).setVisible(false);
			container.getComponent(8).setVisible(false);
			container.getComponent(10).setVisible(false);
			container.getComponent(11).setVisible(false);
			setFrameSize();
		}catch(Exception e){e.printStackTrace();}
	}
	public void update(Graphics g){paint(g);}
	public void paint(Graphics g){synchronized(actionQueue){g.drawImage(dblbufImage,0,0,canvasW,canvasH,this);}}
	
	private void renderBan(){
		Graphics2D g=(Graphics2D)banbufImage.getGraphics();
		g.drawImage(backgroundImage,0,0,W,H,null);
		for(int x=0;x<9;x++)for(int y=0;y<9;y++)renderCell(g,x,y);
		for(int i=0;i<9;i++){
			if(mkoma[0][i]!=null)renderMKoma(g,true,i);
			if(mkoma[1][i]!=null)renderMKoma(g,false,i);
		}
		renderPlayerInfo(true,playerinfo0,false);
		renderPlayerInfo(false,playerinfo1,false);
		g.dispose();
		banChanged=true;
	}
	private void renderCell(Graphics2D g,int x,int y){
		Shape shape=g.getClip();
		g.setClip(offsetX+x*cellW,offsetY+y*cellH,cellW,cellH);
		g.drawImage(backgroundImage,0,0,W,H,null);
		Koma k=map[x][y];
		if(k!=null)k.drawImage(g,offsetX+cellW*x,offsetY+cellH*y,cellW,cellH);
		g.setClip(shape);
	}
	private void renderCell(int x,int y){
		Graphics2D g=(Graphics2D)banbufImage.getGraphics();
		renderCell(g,x,y);
		g.dispose();
		banChanged=true;
	}
	private Point getKomaPoint(int x,int y){return new Point(offsetX+cellW*x,offsetY+cellH*y);}
	private Point getMKomaPoint(boolean player,int koma){
		if(player)return new Point(mkomaX+mkomaW*(koma-2),offsetY+banH+(offsetY-cellH)/2);
		else return new Point(W-mkomaX-mkomaW*(koma-1)+11,(offsetY-cellH)/2);
	}
	private void renderMKoma(boolean player,int koma){
		Graphics2D g=(Graphics2D)banbufImage.getGraphics();
		renderMKoma(g,player,koma);
		g.dispose();
		banChanged=true;
	}
	private void drawTimeString(Graphics2D g,int x,int y,int n1,int n2,boolean colon,boolean color){
		int sx=colon?200:220,sy=color?27:0,n;
		int w=17,cw=15;
		g.drawImage(timeImage,x-10,y-13,x+10,y+14,sx,sy,sx+20,sy+27,null);
		n=n1/10;
		g.drawImage(timeImage,x-cw-w-10,y-13,x-cw-w+10,y+14,20*n,sy,20*n+20,sy+27,null);
		n=n1%10;
		g.drawImage(timeImage,x-cw-10,y-13,x-cw+10,y+14,20*n,sy,20*n+20,sy+27,null);
		n=n2/10;
		g.drawImage(timeImage,x+cw-10,y-13,x+cw+10,y+14,20*n,sy,20*n+20,sy+27,null);
		n=n2%10;
		g.drawImage(timeImage,x+cw+w-10,y-13,x+cw+w+10,y+14,20*n,sy,20*n+20,sy+27,null);
	}
	private int parseInt(String s){
		try{
			if(s.charAt(0)==' ')s=s.substring(1);
			return Integer.parseInt(s);
		}catch(Exception e){return 0;}
	}
	private void renderPlayerInfo(boolean p,PlayerInfo info,boolean updateflag){
		Graphics2D g=(Graphics2D)banbufImage.getGraphics();
		Composite composite=g.getComposite();
		int w=100,h=66;
		int x=p?W-w:0,y=p?H-h:0;
		Shape shape=g.getClip();
		g.setClip(x,y,w,h);
		g.drawImage(backgroundImage,0,0,W,H,null);
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,info.flagTurn?1f:0.5f));
		g.setColor(Color.white);
		int time1=0,time2=0;
		boolean timecolor,timecolon;
		boolean effectFlag=false;
		if(info.atime!=null&&info.ctime!=null){
			time1=parseInt(info.atime);
			time2=parseInt(info.ctime);
			timecolon=false;timecolor=false;
			if(updateflag){
				if(time1==10||time1<=5)effectFlag=true;
			}
		}else{
			String s=info.atime!=null?info.atime:info.ctime;
			if(s==null)s="00:00";
			String sarr[]=s.split(":");
			if(sarr.length==1){time1=0;time2=parseInt(sarr[0]);}
			else if(sarr.length==2){
				time1=parseInt(sarr[0]);
				time2=parseInt(sarr[1]);
			}
			timecolon=true;timecolor=(info.atime==null);
			if(updateflag){
				if(timecolor){
					if(time2==10||time2<=5)effectFlag=true;
				}else{
					if(time2==30||time2==0)effectFlag=true;
				}
			}
		}
		System.out.println(info);
		
		g.setClip(x+3,y+3,w-6,h-6);
		drawTimeString(g,x+50,y+15,time1,time2,timecolon,timecolor);
		g.setColor(Color.white);
		g.setFont(new Font(Font.SANS_SERIF,Font.PLAIN,12));
		FontMetrics metrics=g.getFontMetrics();
		String name=(info.flagBW?"☗":"☖")+(info.name==null?"":info.name);
		String rate=info.rate==null?"":info.rate;
		int width=metrics.stringWidth(name);
		g.drawString(name,x+(width>w-6?3:50-width/2),y+40);
		width=metrics.stringWidth(rate);
		g.drawString(rate,x+50-width/2,y+58);
		
		g.setClip(shape);
		g.dispose();
		banChanged=true;
		if(effectFlag){synchronized(actionQueue){effectList.add(new SparkleEffect(20,32,x+8,y+3,100-2*8,25,20),400);}}
	}
	
	private void renderMKoma(Graphics2D g,boolean player,int koma){
		if(koma<=1)return;
		int d=player?1:-1;
		Point p=getMKomaPoint(player,koma);
		MKoma mk=mkoma[player?0:1][koma];
		Shape shape=g.getClip();
		if(player)g.setClip(p.x,p.y-mkomaHOffset,mkomaW,cellH+2*mkomaHOffset);
		else g.setClip(p.x-mkomaW+cellW,p.y-mkomaHOffset,mkomaW,cellH+2*mkomaHOffset);
		g.drawImage(backgroundImage,0,0,W,H,null);
		if(mk.count!=0){
			if(mk.count==1)mk.koma.drawImage(g,p.x,p.y,cellW,cellH);
			else if(mk.count<=4){
				for(int i=mk.count-1;i>=0;i--){
					mk.koma.drawImage(g,p.x+(mkomaW-cellW)/(mk.count-1)*i*d,p.y,cellW,cellH);
				}
			}else{
				mk.koma.drawImage(g,p.x,p.y,cellW,cellH);
				int w=MKomaCount.getWidth(mk.count);
				int x=p.x+cellW-w;
				int xmin=player?p.x:p.x-mkomaW+cellW;
				if(x<xmin)x=xmin;
				MKomaCount.draw(g,mk.count,x,p.y+cellH+mkomaHOffset-25);
			}
		}
		g.setClip(shape);
	}
	
	public void notifyWinLose(boolean flag){
		synchronized(actionQueue){
			LinkedList<Point>points=new LinkedList<Point>();
			for(int x=0;x<9;x++)for(int y=0;y<9;y++)if(map[x][y]!=null&&map[x][y].player==flag)points.add(new Point(x,y));
			CastleEffect effect=new CastleEffect(flag?"You Win":"You Lose",points.toArray(new Point[0]),offsetX,offsetY,W,H);
			effect.cancelCount=120;
			effectList.add(effect,400);
		}
	}
	
	public void a(Rectangle rect){try{resizeTo(rect);}catch(Exception e){e.printStackTrace();}}
	private void resizeTo(Rectangle rect){
		System.err.println(" resizeTo(Rect) ");
		canvasW=rect.width+1;
		canvasH=rect.height+1;
		W=451;
		H=505;
		cellW=36;
		cellH=40;
		banW=1+cellW*9;
		banH=1+cellH*9;
		offsetX=(W-banW)/2;
		offsetY=(H-banH)/2;
		
		mkomaW=cellW*4/3;
		mkomaHOffset=(offsetY-cellH)/6;
		mkomaX=16;//(W-mkomaW*7)/2+(mkomaW-cellW)/2;
		
		setSize(W,H);
		dblbufImage=shogi.c().createImage(W,H);
		banbufImage=shogi.c().createImage(W,H);
		renderBan();
		render();
		resetLayout();
		setFrameSize();
	}
	ComponentListener frameComponentListener=new ComponentListener(){
		public void componentHidden(ComponentEvent e){}
		public void componentShown(ComponentEvent e){}
		public void componentMoved(ComponentEvent e){}
		public void componentResized(ComponentEvent e){
			try{
				bk frame=(bk)getParent().getParent();
				if(canvasW!=W||canvasH!=H){frame.componentResized(e);return;}
				Insets insets=frame.getInsets();
				int top=insets.top+581;
				int height=frame.getHeight()-top-insets.bottom;
				if(height<128)height=128;
				height-=28;
				frame.getComponent(5).setSize(474,height);
				frame.getComponent(6).setLocation(0,top+height);
			}catch(Exception ex){ex.printStackTrace();}
		}
	};
	private void setFrameSize(){
		try{
			if(canvasW==W&&canvasH==H){
				bk frame=(bk)getParent().getParent();
				Insets insets=frame.getInsets();
				frame.setSize(insets.left+insets.right+491,insets.top+insets.bottom+581);
				boolean flag=true;
				for(ComponentListener listener:frame.getComponentListeners()){
					if(listener==frameComponentListener)flag=false;
					else frame.removeComponentListener(listener);
				}
				frame.addComponentListener(frameComponentListener);
			}
		}catch(Exception e){}
	}
	public void a(int mode,boolean player){System.err.println(" a(i,b) ");try{initKomaochi(mode,player);}catch(Exception e){e.printStackTrace();}}
	public void b(int mode,boolean player){System.err.println(" b(i,b) ");try{initKomaochi(mode,player);}catch(Exception e){e.printStackTrace();}}
	private void initKomaochi(int mode,boolean player){
		System.err.println(" initKomaochi("+mode+","+player+") ");
		int komaEmpty[]={0,0,0,0,0,0,0,0,0};
		resetBoardData(getKomaochiArray(mode,player),komaEmpty,komaEmpty,null);
		currentPlayerBW=mode==0;
		synchronized(actionQueue){effectList.add(new StartEffect(offsetX,offsetY,W,H),400);}
	}
	
	private int[][] getKomaochiArray(int mode,boolean player){
		int imap[][]=new int[9][9];
		for(int x=0;x<9;x++)for(int y=0;y<9;y++)imap[x][y]=0;
		int list[]={Koma.KYO,Koma.KEI,Koma.GIN,Koma.KIN,Koma.OU,Koma.KIN,Koma.GIN,Koma.KEI,Koma.KYO};
		for(int x=0;x<9;x++){
			imap[x][0]=-list[x];imap[x][8]=list[x];
			imap[x][2]=-Koma.FU;imap[x][6]=Koma.FU;
		}
		imap[1][7]=Koma.KAKU;
		imap[7][7]=Koma.HI;
		imap[7][1]=-Koma.KAKU;
		imap[1][1]=-Koma.HI;
		int ey0=player?0:8,ey1=player?1:7;
		int exky=player?0:8,exkk=player?7:1,exhi=player?1:7;
		switch(mode){
			case 0:break;
			case 1:imap[exky][ey0]=0;;break;
			case 2:imap[exkk][ey1]=0;break;
			case 3:imap[exhi][ey1]=0;break;
			case 4:imap[exhi][ey1]=imap[exky][ey0]=0;break;
			case 5:imap[exhi][ey1]=imap[exkk][ey1]=0;break;
			case 6:imap[exhi][ey1]=imap[exkk][ey1]=imap[0][ey0]=imap[8][ey0]=0;break;
			case 7:imap[exhi][ey1]=imap[exkk][ey1]=imap[0][ey0]=imap[8][ey0]=imap[1][ey0]=imap[7][ey0]=0;break;
		}
		return imap;
	}

	public void a(BoardInfo boardInfo){try{onBoardReset(boardInfo);}catch(Exception e){e.printStackTrace();}}
	private void onBoardReset(BoardInfo boardInfo){
		System.err.println(" onBoardReset(B) ");
		int[] data=boardInfo.getSquare();
		int imap[][]=new int[9][9];
		int last=boardInfo.getJustSquare();
		Point lastPoint;
		int koma[],ekoma[];
		if(flipFlag){
			for(int i=0;i<81;i++)imap[8-i%9][i/9]=data[i];
			lastPoint=new Point(8-last%9,last/9);
			koma=boardInfo.getBPieceInHand();
			ekoma=boardInfo.getWPieceInHand();
		}else{
			for(int i=0;i<81;i++)imap[i%9][8-i/9]=-data[i];
			lastPoint=new Point(last%9,8-last/9);
			koma=boardInfo.getWPieceInHand();
			ekoma=boardInfo.getBPieceInHand();
		}
		resetBoardData(imap,koma,ekoma,lastPoint);
	}
	private void checkLeaveTest(int x1,int y1,int x2,int y2){
		if(checkLeaveChecker.move(x1,y1,x2,y2)){
			shogi.h().setKingDied();
			shogi.i().setKingDied();
		}
	}
	private void resetBoardData(int[][]imap,int[]koma,int[]ekoma,Point last){
		deleteCoords();
		synchronized(actionQueue){
			checkLeaveChecker.init(imap);
			
			for(int x=0;x<9;x++)for(int y=0;y<9;y++){
				map[x][y]=imap[x][y]==0?null:new Koma(imap[x][y]);
			}
			for(int i=0;i<9;i++){
				mkoma[0][i]=new MKoma(i,true,koma[i]);
				mkoma[1][i]=new MKoma(i,false,ekoma[i]);
			}
			lastMovePoint=last;
			if(last!=null){
				currentPlayerBW=!map[last.x][last.y].player;
				if(!flipFlag)currentPlayerBW=!currentPlayerBW;
			}
			renderBan();
			effectList.clear();
			actionQueue.clear();
			render();
		}
	}
	
	public void a(MoveInfo moveInfo){try{onKomaMove(moveInfo);}catch(Exception e){e.printStackTrace();}}
	private void onKomaMove(MoveInfo moveInfo){
		PlayerInfo[]info=getPlayerInfoArray();
		
		System.err.println(" onKomaMove(M) ");
		Point pointFrom=moveInfo.getMoveFrom();
		Point pointTo=moveInfo.getMoveTo();
		int x=getXFlip(pointTo.x),y=getYFlip(pointTo.y);
		boolean nari=moveInfo.getPromotion();
		boolean player=moveInfo.getColorOfPlayer();
		if(!flipFlag)player=!player;
		if(pointFrom.x==0&&pointFrom.y==0){
			addAction(new KomaPutAction(x,y,moveInfo.getKindOfPiece(),player));
		}else{
			int x0=getXFlip(pointFrom.x),y0=getYFlip(pointFrom.y);
			addAction(new KomaMoveAction(x0,y0,x,y,nari));
			checkLeaveTest(x0,y0,x,y);
		}
	}
	
	public void a(MoveInfo moveInfo,b0 komadai){try{onKomaMove(moveInfo,komadai);}catch(Exception e){e.printStackTrace();}}
	private void onKomaMove(MoveInfo moveInfo,b0 komadai){
		System.err.println(" onKomaMove(M,b0) ");
		onKomaMove(moveInfo);
	}
	
	class MyKomaMoveAction extends Action{
		int ix,iy,x,y,getkoma;
		Koma koma;
		boolean joseki=false;
		MyKomaMoveAction(int x,int y,int ix,int iy,int k,boolean nari,int gk,boolean joseki){
			this.ix=ix;this.iy=iy;this.x=x;this.y=y;getkoma=gk;this.joseki=joseki;
			koma=new Koma(k,true,nari);
		}
		int count=0;
		KomaMoveEffect effect;
		Effect afterEffect=null;
		void run(){
			int N=6;
			if(count==0){
				effect=new KomaMoveEffect(x,y,offsetX+cellW*ix,offsetY+cellH*iy,cellW,cellH,koma);
				effectList.add(effect,110);
				
				soundKomaMove.play();
				effectList.add(new NoiseCellEffect(offsetX+cellW*ix,offsetY+cellH*iy,cellW,cellH),100);
				
				afterEffect=genCheckEffect(koma.player);
				if(afterEffect!=null)effectList.add(afterEffect,200);
				else{
					CastleEffect.CastleMatchData match=CastleEffect.matchCastle(map,koma.player,ix,iy);
					if(match!=null)effectList.add(afterEffect=new CastleEffect(match,offsetX,offsetY,W,H),200);
				}
			}
			if(count<3*N){
				effect.phase=count/(double)N;
			}else if(count==3*N){
				if(getkoma!=0){
					renderMKoma(true,getkoma);
					Point point=getMKomaPoint(true,getkoma);
					int pcnt=mkoma[0][getkoma].count-1;
					effectList.add(new GatherEffect(point.x+cellW/2+(pcnt==0?0:6),point.y+cellH/2),120);
				}
				renderCell(ix,iy);
				effect.terminate();
				effectList.add(new SplashEffect(offsetX+cellW*ix+cellW/2,offsetY+cellH*iy+cellH/2,256,afterEffect!=null?SplashEffect.WHITE:SplashEffect.BLACK),220);
				if(joseki)effectList.add(new JosekiEffect(offsetX+cellW*ix+cellW/2,offsetY+cellH*iy+cellH/2),210);
				soundKomaDown.play();
				lastMovePoint=new Point(ix,iy);
				currentPlayerBW=!map[ix][iy].player;
				if(!flipFlag)currentPlayerBW=!currentPlayerBW;
				
				if(afterEffect==null)terminate();
			}else{
				if(actionQueue.size()>=2){afterEffect.cancel();terminate();}
				else if(!afterEffect.isAlive())terminate();
			}
			count++;
		}
	}
	
	class KomaMoveAction extends Action{
		int x1,y1,x2,y2,N=6;
		boolean nari,joseki=false;
		int count=0;
		Koma koma,gkoma;
		KomaMoveAction(int x1,int y1,int x2,int y2,boolean nari){
			this.x1=x1;this.y1=y1;this.x2=x2;this.y2=y2;this.nari=nari;
		}
		KomaMoveEffect moveEffect=null;
		Effect magicalEffect=null;
		Effect afterEffect=null;
		boolean cancel(){
			if(count<=4*N)return false;
			afterEffect.cancel();terminate();
			return true;
		}
		void run(){
			if(count==0){
				koma=map[x1][y1];
				joseki=JosekiEffect.isJoseki(map,mkoma,koma.player,x1,y1,x2,y2,!koma.nari&&nari);
				System.out.println("joseki?"+joseki);
				map[x1][y1]=null;
				gkoma=map[x2][y2];
				map[x2][y2]=new Koma(koma.koma,koma.player,nari);
				renderCell(x1,y1);
				moveEffect=new KomaMoveEffect(offsetX+cellW*x1,offsetY+cellH*y1,offsetX+cellW*x2,offsetY+cellH*y2,cellW,cellH,koma);
				effectList.add(magicalEffect=new MagicalKomaEffect(offsetX+cellW*x1,offsetY+cellH*y1,cellW,cellH,1,koma),105);
				soundKomaUp.play();
			}else if(count==N){
				effectList.add(moveEffect,110);
				magicalEffect.cancel();
				soundKomaMove.play();
				effectList.add(new NoiseCellEffect(offsetX+cellW*x2,offsetY+cellH*y2,cellW,cellH),100);
				afterEffect=genCheckEffect(koma.player);
				if(afterEffect!=null)effectList.add(afterEffect,200);
				else{
					CastleEffect.CastleMatchData match=CastleEffect.matchCastle(map,koma.player,x2,y2);
					if(match!=null)effectList.add(afterEffect=new CastleEffect(match,offsetX,offsetY,W,H),200);
				}
			}else if(N<count&&count<4*N){
				moveEffect.phase=count/(double)N-1;
			}else if(count==4*N){
				if(gkoma!=null){
					boolean p=!gkoma.player;
					int pcnt=mkoma[p?0:1][gkoma.koma].count;
					mkoma[p?0:1][gkoma.koma].count++;
					renderMKoma(p,gkoma.koma);
					Point point=getMKomaPoint(p,gkoma.koma);
					effectList.add(new GatherEffect(point.x+cellW/2+(pcnt==0?0:p?6:-6),point.y+cellH/2),120);
				}
				effectList.add(new SplashEffect(offsetX+cellW*x2+cellW/2,offsetY+cellH*y2+cellH/2,256,afterEffect!=null?SplashEffect.WHITE:nari&&!koma.nari?SplashEffect.RED:SplashEffect.BLACK),220);
				if(joseki)effectList.add(new JosekiEffect(offsetX+cellW*x2+cellW/2,offsetY+cellH*y2+cellH/2),210);
				soundKomaDown.play();
				renderCell(x2,y2);
				moveEffect.terminate();
				lastMovePoint=new Point(x2,y2);
				currentPlayerBW=!map[x2][y2].player;
				if(!flipFlag)currentPlayerBW=!currentPlayerBW;
				if(afterEffect==null)terminate();
			}else if(count>4*N){
				if(actionQueue.size()>=2){afterEffect.cancel();terminate();}
				if(!afterEffect.isAlive())terminate();
			}
			count++;
		}
	}
	
	class KomaPutAction extends Action{
		int x,y,N=6;
		Koma koma;
		int count=0;
		boolean joseki=false;
		KomaPutAction(int x,int y,int k,boolean player){
			this.x=x;this.y=y;
			koma=new Koma(k,player,false);
		}
		Effect magicalEffect=null;
		KomaMoveEffect moveEffect;
		Effect afterEffect=null;
		boolean cancel(){
			if(count<=4*N)return false;
			afterEffect.cancel();terminate();
			return true;
		}
		void run(){
			if(count==0){
				joseki=JosekiEffect.isJoseki(map,mkoma,koma.player,x,y,koma.koma);
				mkoma[koma.player?0:1][koma.koma].count--;
				map[x][y]=koma;
				renderMKoma(koma.player,koma.koma);
				Point p=getMKomaPoint(koma.player,koma.koma);
				moveEffect=new KomaMoveEffect(p.x,p.y,offsetX+cellW*x,offsetY+cellH*y,cellW,cellH,koma);
				effectList.add(magicalEffect=new MagicalKomaEffect(p.x,p.y,cellW,cellH,1,koma),105);
				soundKomaUp.play();
			}else if(count==N){
				effectList.add(moveEffect,110);
				magicalEffect.cancel();
				soundKomaMove.play();
				effectList.add(new NoiseCellEffect(offsetX+cellW*x,offsetY+cellH*y,cellW,cellH),100);
				afterEffect=genCheckEffect(koma.player);
				if(afterEffect!=null)effectList.add(afterEffect,200);
				else{
					CastleEffect.CastleMatchData match=CastleEffect.matchCastle(map,koma.player,x,y);
					if(match!=null)effectList.add(afterEffect=new CastleEffect(match,offsetX,offsetY,W,H),200);
				}
			}else if(N<count&&count<4*N){
				moveEffect.phase=count/(double)N-1;
			}else if(count==4*N){
				renderCell(x,y);
				moveEffect.terminate();
				effectList.add(new SplashEffect(offsetX+cellW*x+cellW/2,offsetY+cellH*y+cellH/2,256,afterEffect!=null?SplashEffect.WHITE:SplashEffect.BLACK),220);
				if(joseki)effectList.add(new JosekiEffect(offsetX+cellW*x+cellW/2,offsetY+cellH*y+cellH/2),210);
				soundKomaDown.play();
				lastMovePoint=new Point(x,y);
				currentPlayerBW=!map[x][y].player;
				if(!flipFlag)currentPlayerBW=!currentPlayerBW;
				if(afterEffect==null)terminate();
			}else if(count>4*N){
				if(actionQueue.size()>=2){afterEffect.cancel();terminate();}
				else if(!afterEffect.isAlive())terminate();
			}
			count++;
		}
	}
	
	
	public void a(MoveInfo moveInfo,BoardInfo boardInfo){try{onBoardReset(boardInfo);}catch(Exception e){e.printStackTrace();}}
	public void a(){System.err.println(" a() ");}
	public void b(){System.err.println(" b() ");}
	public void a(int digit){System.err.println(" a("+digit+") ");}
	public void c(){System.err.println(" c() ");}
	public boolean e(){System.err.println(" e() ");return false;}
	public void d(){System.err.println(" d() ");if(shogi.e())ShogiClient.a(0).play();}
	public void f(){System.err.println(" f() ");if(shogi.e())ShogiClient.a(2).play();}//countdouwn2
	public void g(){System.err.println(" g() ");if(shogi.e())ShogiClient.a(1).play();}//countdown
	
	public void a(boolean flag){setFlip(flag);}
	private void setFlip(boolean flag){
		System.err.println(" setFlip("+flag+") ");
		synchronized(actionQueue){
			flipFlag=flag;
			effectList.clear();
			actionQueue.clear();
		}
	}

	public void b(boolean paramBoolean){//着手確認
		System.err.println(" b("+paramBoolean+") ");
	}

	private Point getPointFlip(int x,int y){return flipFlag?new Point(9-x,1+y):new Point(1+x,9-y);}
	private int getXFlip(int x){return flipFlag?9-x:x-1;}
	private int getYFlip(int y){return flipFlag?y-1:9-y;}
	private int getIndexFlip(int x,int y){return flipFlag?8-x+9*y:x+9*(8-y);}
	private int getXfromIndex(int i){return flipFlag?8-i%9:i%9;}
	private int getYfromIndex(int i){return flipFlag?i/9:8-i/9;}
	
	
	static final int MODE_MOVE=10;
	static final int MODE_WAITING=20;
	static final int MODE_WATCHING=100;
	static final int MODE_END=110;
	
	public void h(){
		System.err.println(" h() ");
		removeMouseListener(this);
		actionThreadStop();
	}
	
	private Point movePointSrc;
	private Effect moveSrcEffect,availableCellEffect;
	private Koma moveKomaSrc;
	private NariSelectEffect nariSelectEffect;
	private Point movePointDst;
	private String moveJosekiSrc;
	
	private Point getPointScreen(int x,int y){
		int ix=(int)Math.floor((float)(x-offsetX)/cellW);
		int iy=(int)Math.floor((float)(y-offsetY)/cellH);
		if(0<=ix&&ix<9&&0<=iy&&iy<9)return new Point(ix,iy);
		for(int i=2;i<=8;i++){
			Point p=getMKomaPoint(true,i);
			if(p.x<=x&&x<p.x+cellW&&p.y<=y&&y<p.y+cellH)return new Point(-1,i);
		}
		return null;
	}
	private void clickedSrc(Point point){
		if(point==null)return;
		moveJosekiSrc=JosekiEffect.getJoseki(map,mkoma,true);
		if(point.x<0){
			if(mkoma[0][point.y].count>0){
				movePointSrc=point;
				moveKomaSrc=new Koma(point.y,true,false);
				mkoma[0][point.y].count--;
				renderMKoma(true,point.y);
				Point p=getMKomaPoint(true,point.y);
				effectList.add(moveSrcEffect=new MagicalKomaEffect(p.x,p.y,cellW,cellH,0.3,moveKomaSrc),110);
			}else return;
		}else{
			if(map[point.x][point.y]!=null&&map[point.x][point.y].player){
				movePointSrc=point;
				moveKomaSrc=map[point.x][point.y];
				map[point.x][point.y]=null;
				renderCell(point.x,point.y);
				Point p=getKomaPoint(point.x,point.y);
				effectList.add(moveSrcEffect=new MagicalKomaEffect(p.x,p.y,cellW,cellH,0.3,moveKomaSrc),110);
			}else return;
		}
		LinkedList<Point>aPoints=new LinkedList<Point>();
		for(int x=0;x<9;x++)for(int y=0;y<9;y++){
			if(checkMove(point.x,point.y,x,y,moveKomaSrc))aPoints.add(getKomaPoint(x,y));
		}
		effectList.add(availableCellEffect=new AvailableCellEffect(aPoints),105);
	}
	private void clickedDst(Point point){
		if(point!=null&&checkMove(movePointSrc.x,movePointSrc.y,point.x,point.y,moveKomaSrc)){
			movePointDst=point;
			boolean nari=moveKomaSrc.nari;
			if(!nari&&moveKomaSrc.koma!=Koma.OU&&moveKomaSrc.koma!=Koma.KIN&&movePointSrc.x>=0&&(movePointSrc.y<=2||movePointDst.y<=2)){
				if((movePointDst.y==0&&(moveKomaSrc.koma==Koma.FU||moveKomaSrc.koma==Koma.KYO))
					||(movePointDst.y<=1&&moveKomaSrc.koma==Koma.KEI))nari=true;
				else{
					effectList.add(nariSelectEffect=new NariSelectEffect(offsetX+cellW*movePointDst.x,offsetY+cellH*movePointDst.y,moveKomaSrc.koma),120);
					return;
				}
			}
			Point p=movePointSrc.x<0?getMKomaPoint(true,movePointSrc.y):getKomaPoint(movePointSrc.x,movePointSrc.y);
			Koma gkoma=map[movePointDst.x][movePointDst.y];
			if(gkoma!=null)mkoma[0][gkoma.koma].count++;
			boolean joseki;
			if(movePointSrc.x<0)joseki=JosekiEffect.isJoseki(moveJosekiSrc,true,movePointDst.x,movePointDst.y,movePointSrc.y);
			else joseki=JosekiEffect.isJoseki(moveJosekiSrc,true,movePointSrc.x,movePointSrc.y,movePointDst.x,movePointDst.y,nari);
			actionQueue.add(new MyKomaMoveAction(p.x,p.y,movePointDst.x,movePointDst.y,moveKomaSrc.koma,moveKomaSrc.nari,gkoma!=null?gkoma.koma:0,joseki));
			if(movePointSrc.x>=0)checkLeaveTest(movePointSrc.x,movePointSrc.y,movePointDst.x,movePointDst.y);
			map[movePointDst.x][movePointDst.y]=moveKomaSrc;
			moveKomaSrc.nari=nari;
			Point originalPointSrc=movePointSrc.x<0?new Point(0,0):getPointFlip(movePointSrc.x,movePointSrc.y);
			Point originalPointDst=getPointFlip(movePointDst.x,movePointDst.y);
			shogi.j().updateInfo(moveKomaSrc.koma,originalPointSrc,originalPointDst,nari);
			shogi.m();
			shogi.a(MODE_WAITING);
			
			moveSrcEffect.cancel();
			availableCellEffect.cancel();
			availableCellEffect=null;
			moveSrcEffect=null;
			movePointDst=null;
			movePointSrc=null;
			moveKomaSrc=null;
			return;
		}
		
		if(movePointSrc.x<0){
			mkoma[0][movePointSrc.y].count++;
			renderMKoma(true,movePointSrc.y);
		}else{
			map[movePointSrc.x][movePointSrc.y]=moveKomaSrc;
			renderCell(movePointSrc.x,movePointSrc.y);
		}
		moveSrcEffect.cancel();
		availableCellEffect.cancel();
		boolean selectcancel=movePointSrc.equals(point);
		availableCellEffect=null;
		moveSrcEffect=null;
		movePointSrc=null;
		movePointDst=null;
		moveKomaSrc=null;
		if(!selectcancel)clickedSrc(point);
	}
	
	static final int NARI_OFFSET_LR=4,NARI_OFFSET_UD=4,NARI_OFFSET_MDL=4;
	private void clickedNari(int x,int y){
		int cx=x-(offsetX+cellW*movePointDst.x+cellW/2);
		int cy=y-(offsetY+cellH*movePointDst.y);
		if(cy<-NARI_OFFSET_UD||cy>=cellH+NARI_OFFSET_UD||Math.abs(cx)>NARI_OFFSET_LR+NARI_OFFSET_MDL/2+cellW){
			if(movePointSrc.x<0){
				mkoma[0][movePointSrc.y].count++;
				renderMKoma(true,movePointSrc.y);
			}else{
				map[movePointSrc.x][movePointSrc.y]=moveKomaSrc;
				renderCell(movePointSrc.x,movePointSrc.y);
			}
			nariSelectEffect.cancel();
			moveSrcEffect.cancel();
			availableCellEffect.cancel();
			availableCellEffect=null;
			movePointSrc=null;
			movePointDst=null;
			moveKomaSrc=null;
			moveSrcEffect=null;
			nariSelectEffect=null;
			return;
		}
		if(0<=cy&&cy<cellH&&NARI_OFFSET_MDL/2<Math.abs(cx)&&Math.abs(cx)<NARI_OFFSET_MDL/2+cellW){
			boolean nari=cx>0;
			nariSelectEffect.setClicked(nari);
			
			Point p=movePointSrc.x<0?getMKomaPoint(true,movePointSrc.y):getKomaPoint(movePointSrc.x,movePointSrc.y);
			Koma gkoma=map[movePointDst.x][movePointDst.y];
			if(gkoma!=null)mkoma[0][gkoma.koma].count++;
			boolean joseki=JosekiEffect.isJoseki(moveJosekiSrc,true,movePointSrc.x,movePointSrc.y,movePointDst.x,movePointDst.y,nari);
			actionQueue.add(new MyKomaMoveAction(p.x,p.y,movePointDst.x,movePointDst.y,moveKomaSrc.koma,moveKomaSrc.nari,gkoma!=null?gkoma.koma:0,joseki));
			checkLeaveTest(movePointSrc.x,movePointSrc.y,movePointDst.x,movePointDst.y);
			map[movePointDst.x][movePointDst.y]=moveKomaSrc;
			moveKomaSrc.nari=nari;
			Point originalPointSrc=movePointSrc.x<0?new Point(0,0):getPointFlip(movePointSrc.x,movePointSrc.y);
			Point originalPointDst=getPointFlip(movePointDst.x,movePointDst.y);
			shogi.j().updateInfo(moveKomaSrc.koma,originalPointSrc,originalPointDst,nari);
			shogi.m();
			shogi.a(MODE_WAITING);
			
			nariSelectEffect.cancel();
			moveSrcEffect.cancel();
			availableCellEffect.cancel();
			availableCellEffect=null;
			movePointSrc=null;
			movePointDst=null;
			moveKomaSrc=null;
			moveSrcEffect=null;
			nariSelectEffect=null;
		}
	}
	private boolean checkMove(int x1,int y1,int x2,int y2,Koma k){
		if(x2<0)return false;
		if(x1<0){
			if(map[x2][y2]!=null)return false;
			if(y2==0&&(k.koma==Koma.KEI||k.koma==Koma.KYO||k.koma==Koma.FU))return false;
			if(y2==1&&k.koma==Koma.KEI)return false;
			if(k.koma==Koma.FU){
				for(int y=0;y<9;y++){Koma kk=map[x2][y];if(kk!=null&&kk.koma==Koma.FU&&kk.player&&!kk.nari)return false;}
				Koma khit=map[x2][y2-1];
				if(khit!=null&&!khit.player&&khit.koma==Koma.OU){
					b1 b1map[][]=new b1[9][9];
					for(int x=0;x<9;x++)for(int y=0;y<9;y++){
						b1map[x][y]=map[x][y]==null?null:new b1(map[x][y].koma,map[x][y].player,map[x][y].nari);
					}
					return new bt(b1map).a(x2,y2);
				}
			}
			return true;
		}
		if(map[x2][y2]!=null&&map[x2][y2].player)return false;
		int dx=x2-x1,dy=y2-y1;
		if(!k.canMoveTo(dx,dy))return false;
		if(k.koma==Koma.KEI)return true;
		for(int ix=0,iy=0;ix!=dx||iy!=dy;){
			if((ix!=0||iy!=0)&&map[x1+ix][y1+iy]!=null)return false;
			ix+=dx>0?1:dx<0?-1:0;
			iy+=dy>0?1:dy<0?-1:0;
		}
		return true;
	}
	
	
	public void mousePressed(MouseEvent e){
		int x=e.getX()*W/canvasW,y=e.getY()*H/canvasH;
		mouse=new MouseState(x,y,true);
		try{
			synchronized(actionQueue){
				if(shogi.f()!=MODE_MOVE){clickedSrc(null);return;}
				int actionLength=actionQueue.size();
				if(actionLength>=2)return;
				Action currentAction=actionLength==0?null:actionQueue.getFirst();
				if(currentAction!=null&&!currentAction.cancel())return;
				if(movePointSrc==null)clickedSrc(getPointScreen(x,y));
				else if(movePointDst==null)clickedDst(getPointScreen(x,y));
				else clickedNari(x,y);
			}
		}catch(Exception ex){ex.printStackTrace();}
	}
	public void mouseClicked(MouseEvent e){}
	public void mouseEntered(MouseEvent e){}
	public void mouseExited(MouseEvent e){}
	public void mouseReleased(MouseEvent e){mouse=new MouseState(mouse.x,mouse.y,false);}
	public void mouseMoved(MouseEvent e){mouse=new MouseState(e.getX()*W/canvasW,e.getY()*H/canvasH,mouse.pressed);}
	public void mouseDragged(MouseEvent e){mouse=new MouseState(e.getX()*W/canvasW,e.getY()*H/canvasH,mouse.pressed);}
	
	CheckEffect genCheckEffect(boolean player){
		Point[]points=CheckEffect.checkTest(map,player);
		if(points==null)return null;
		for(int i=0;i<points.length;i++){
			points[i].x=offsetX+cellW*points[i].x+cellW/2;
			points[i].y=offsetY+cellH*points[i].y+cellH/2;
		}
		CheckEffect effect=new CheckEffect(points,W,H);
		return effect;
	}
	public static void loadCastleFile(){try{CastleEffect.loadData();}catch(Exception e){e.printStackTrace();}}
	public static void loadJosekiFile(){try{JosekiEffect.loadData();}catch(Exception e){e.printStackTrace();}}
}

class MKomaCount{
	static final int coords[][]={{14,18},{32,11},{43,16},{59,15},{74,18},{92,16},{108,16},{124,17},{141,16},{157,16},{0,14}};
	static int height=22;
	static int getWidth(int num){
		int w=14;
		while(num>0){
			w+=coords[num%10][1];
			num/=10;
		}
		return w;
	}
	static void draw(Graphics g,int num,int x,int y){
		g.drawImage(bn.komaCountImage,x,y,x+14,y+height,0,0,14,height,null);
		x+=14;
		int n=num>=10?num/10:num;
		int w=coords[n][1],sx=coords[n][0];
		g.drawImage(bn.komaCountImage,x,y,x+w,y+height,sx,0,sx+w,height,null);
		if(num<10)return;
		n=num%10;x+=w;
		w=coords[n][1];sx=coords[n][0];
		g.drawImage(bn.komaCountImage,x,y,x+w,y+height,sx,0,sx+w,height,null);
	}
}

class ImageFont{
	static final int numCoords[][]={{0,23},{23,37},{60,40},{100,39},{139,37},{176,39},{215,35},{250,38},{288,31},{319,37}};
	static final int alpha0Coords[][]={{0,38},{38,40},{78,40},{118,40},{158,39},{197,40},{237,24},{261,36},{297,36},{333,31},{364,40},{404,28},{432,37}};
	static final int alpha1Coords[][]={{0,35},{35,27},{62,29},{91,27},{118,31},{149,14},{163,31},{194,40},{234,32},{266,38},{304,40},{344,38},{382,40}};
	static final int height=40;
	static final int space=16;
	static int getCharWidth(char c){
		if(c==' ')return space;
		if('A'<=c&&c<='Z')c+='a'-'A';
		if('0'<=c&&c<='9')return numCoords[c-'0'][1];
		if('a'<=c&&c<='m')return alpha0Coords[c-'a'][1];
		if('n'<=c&&c<='z')return alpha1Coords[c-'n'][1];
		return 0;
	}
	static void drawChar(Graphics g,char c,int x,int y){
		int sx,sy,sw;
		if('A'<=c&&c<='Z')c+='a'-'A';
		if('0'<=c&&c<='9'){sy=0;int d[]=numCoords[c-'0'];sw=d[1];sx=d[0];}
		else if('a'<=c&&c<='m'){sy=height;int d[]=alpha0Coords[c-'a'];sw=d[1];sx=d[0];}
		else if('n'<=c&&c<='z'){sy=2*height;int d[]=alpha1Coords[c-'n'];sw=d[1];sx=d[0];}
		else return;
		g.drawImage(bn.textFontImage,x,y,x+sw,y+height,sx,sy,sx+sw,sy+height,null);
	}
	static int getTextWidth(String s){
		int width=0;
		for(int i=0;i<s.length();i++)width+=getCharWidth(s.charAt(i));
		return width;
	}
	static void drawText(Graphics g,String s,int x,int y){
		for(int i=0;i<s.length();i++){
			drawChar(g,s.charAt(i),x,y);
			x+=getCharWidth(s.charAt(i));
		}
	}
}

class CheckEffect extends Effect{
	int W,H;
	Point[]points;
	int P=20,T1=20;
	int count=-1;
	int cancelCount=60;
	CheckEffect(Point[]p,int w,int h){
		points=p;W=w;H=h;
	}
	void run(){
		if(++count>=cancelCount+P)terminate();
		if(count==T1)bn.soundCheck.play();
	}
	void cancel(){if(count<cancelCount)cancelCount=count;}
	void render(Graphics2D g){
		Composite composite=g.getComposite();
		double phase=(double)count/P;
		double cancelPhase=(double)cancelCount/P;
		double bgAlpha=0.5*(phase<1?phase:phase>cancelPhase?(cancelPhase+1-phase):1);
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,(float)(bgAlpha)));
		g.setColor(Color.black);
		g.fillRect(0,0,W,H);
		double alpha=phase-(double)T1/P;if(alpha>1)alpha=1;
		if(alpha>0){
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,(float)(alpha*bgAlpha)));
			for(int i=1;i<points.length;i++)renderPentagon(g,points[i].x,points[i].y,phase,false);
			renderPentagon(g,points[0].x,points[0].y,phase,true);
			g.setColor(Color.black);
			g.fillRect(0,H/2-30,W,60);
			String msg="check";
			ImageFont.drawText(g,msg,W/2-ImageFont.getTextWidth(msg)/2,H/2-20);
		}
		g.setComposite(composite);
	}
	void renderPentagon(Graphics2D g,int x,int y,double phase,boolean red){
			AffineTransform trans=g.getTransform();
			g.translate(x,y);
			g.rotate(0.5*Math.PI*phase);
			g.drawImage(red?bn.pentagonRedEffectImage:bn.pentagonEffectImage,-40,-40,null);
			g.setTransform(trans);
			g.translate(x,y);
			g.rotate(-0.4*Math.PI*phase);
			g.drawImage(red?bn.pentagonRedEffectImage:bn.pentagonEffectImage,-30,-30,60,60,null);
			g.setTransform(trans);
	}
	static Point[] checkTest(Koma[][]map,boolean player){
		int kx=-1,ky=-1;
		for(int x=0;x<9;x++)for(int y=0;y<9;y++){
			if(map[x][y]!=null&&map[x][y].koma==Koma.OU&&map[x][y].player==!player){kx=x;ky=y;break;}
		}
		if(kx<0)return null;
		LinkedList<Point>points=new LinkedList<Point>();
		int dirs[][]={{0,1},{1,1},{1,0},{1,-1},{0,-1},{-1,-1},{-1,0},{-1,1}};
		for(int n=0;n<8;n++){
			for(int i=1;i<9;i++){
				int x2=kx+dirs[n][0]*i,y2=ky+dirs[n][1]*i;
				if(x2<0||x2>=9||y2<0||y2>=9)break;
				if(map[x2][y2]!=null){
					if(map[x2][y2].player==player&&map[x2][y2].canMoveTo(kx-x2,ky-y2))points.add(new Point(x2,y2));
					break;
				}
			}
		}
		int keiY=player?ky+2:ky-2;
		if(0<=keiY&&keiY<9){
			if(kx+1<9&&map[kx+1][keiY]!=null&&map[kx+1][keiY].canMoveTo(-1,ky-keiY))points.add(new Point(kx+1,keiY));
			if(kx-1>=0&&map[kx-1][keiY]!=null&&map[kx-1][keiY].canMoveTo(+1,ky-keiY))points.add(new Point(kx-1,keiY));
		}
		if(points.size()==0)return null;
		points.addFirst(new Point(kx,ky));
		return points.toArray(new Point[0]);
	}
}



class CastleEffect extends Effect{
	int W,H,OX,OY,P=20;
	Point[]points;
	String name;
	int minXY,count=-1;
	int cancelCount=60;
	CastleEffect(String s,Point[]p,int ox,int oy,int w,int h){
		points=p;name=s;W=w;H=h;OX=ox;OY=oy;
		setMinXY();
	}
	CastleEffect(CastleMatchData match,int ox,int oy,int w,int h){
		points=match.points;name=match.name;W=w;H=h;OX=ox;OY=oy;
		setMinXY();
	}
	private void setMinXY(){
		minXY=-1;
		for(Point p:points)if(minXY==-1||p.x+p.y<minXY)minXY=p.x+p.y;
	}
	
	void cancel(){if(count<cancelCount)cancelCount=count;}
	void run(){
		if(++count>=cancelCount+P)terminate();
		if(count==P)bn.soundCastle.play();
	}
	void render(Graphics2D g){
		Composite composite=g.getComposite();
		double phase=(double)count/P;
		double cancelPhase=(double)cancelCount/P;
		double bgAlpha=0.5*(phase<1?phase:phase>cancelPhase?1+cancelPhase-phase:1);
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,(float)(bgAlpha)));
		g.setColor(Color.black);
		g.fillRect(0,0,W,H);
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,(float)(2*bgAlpha)));
		for(int i=0;i<2;i++){
			for(Point p:points){
				int x=p.x,y=p.y;
				int index=count-P-(x+y-minXY)*4;
				if(index<0)continue;
				if(index>12)index=12+(index-12)%24;
				int dx=OX+36*x-10,dy=OY+40*y-10;
				int sx=index%6*56,sy=index/6*60;
				Image img=i==0?bn.castleBackEffectImage:bn.castleFrontEffectImage;
				g.drawImage(img,dx,dy,dx+36+20,dy+40+20,sx,sy,sx+36+20,sy+40+20,null);
			}
		}
		float alpha=(float)(bgAlpha*(phase<1?0:phase<2?phase-1:1));
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,alpha));
		g.setColor(Color.black);
		g.fillRect(0,H/2-30,W,60);
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,2*alpha));
		ImageFont.drawText(g,name,W/2-ImageFont.getTextWidth(name)/2,H/2-20);
		g.setComposite(composite);
	}
	static void loadData()throws Exception{
		BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(ResLoader.getResFile("castle.txt")),"UTF-8"));
		String name=null;
		String ban=null;
		LinkedList<int[][]>list=new LinkedList<int[][]>();
		while(true){
			String line=br.readLine();
			boolean flag=line!=null&&line.length()>1&&line.charAt(0)=='%';
			if(!flag&&line!=null&&line.length()>=9){
				ban=ban==null?line:ban+"\n"+line;
				continue;
			}
			if(ban!=null){
				list.add(parseBanData(ban));
				ban=null;
			}
			if(!flag&&line!=null)continue;
			castleData.add(new CastleInfo(name,list.toArray(new int[0][][])));
			list.clear();
			if(line==null)break;
			if(flag)name=line.substring(1);
		}
	}
	static class CastleInfo{
		String name;int[][][]dataList;
		CastleInfo(String s,int[][][]a){name=s;dataList=a;}
	}
	static class CastleMatchData{
		String name;Point[]points;
		CastleMatchData(String n,int[][]arr,boolean player){
			int size=0;
			for(int i=0;i<arr.length;i++)if(arr[i][2]!=-1)size++;
			name=n;points=new Point[size];
			for(int i=0,j=0;i<arr.length;i++){
				if(arr[i][2]!=-1)points[j++]=new Point(player?arr[i][0]:8-arr[i][0],player?arr[i][1]:8-arr[i][1]);
			}
		}
	}
	static CastleMatchData matchCastle(Koma[][]map,boolean player,int x,int y){
		String maxmatchName=null;
		int[][]maxmatchArr=null;
		int maxmatchCount=0;
		for(CastleInfo info:castleData){
			String name=info.name;
			int[][][]arr=info.dataList;
			for(int i=0;i<arr.length;i++){
				boolean matchflag=true,moveflag=false;
				int count=0;
				for(int j=0;j<arr[i].length;j++){
					int ix=arr[i][j][0],iy=arr[i][j][1],ik=arr[i][j][2];
					if(!player){ix=8-ix;iy=8-iy;}
					Koma k=map[ix][iy];
					if(ik==-1){
						if(k!=null&&k.player==player&&(k.koma==Koma.KIN||k.koma==Koma.GIN)){matchflag=false;break;}
					}else if(ik==-2){
						if(k==null||k.player!=player||k.nari||(k.koma!=Koma.KIN&&k.koma!=Koma.GIN)){matchflag=false;break;}
						count++;
						if(ix==x&&iy==y)moveflag=true;
					}else{
						if(k==null||k.player!=player||k.nari||k.koma!=ik){matchflag=false;break;}
						count++;
						if(ix==x&&iy==y)moveflag=true;
					}
				}
				if(matchflag&&moveflag){
					if(maxmatchCount<count){
						maxmatchCount=count;
						maxmatchArr=arr[i];
						maxmatchName=name;
					}
				}
			}
		}
		if(maxmatchName==null)return null;
		return new CastleMatchData(maxmatchName,maxmatchArr,player);
	}
	static LinkedList<CastleInfo>castleData=new LinkedList<CastleInfo>();
	static int[][]parseBanData(String ban){
		String lines[]=ban.split("\n");
		LinkedList<int[]>list=new LinkedList<int[]>();
		for(int i=0;i<lines.length;i++){
			for(int j=0;j<lines[i].length();j++){
				char k=lines[i].charAt(j);
				int koma=0;
				switch(k){
					case '歩':koma=Koma.FU;break;
					case '桂':koma=Koma.KEI;break;
					case '香':koma=Koma.KYO;break;
					case '銀':koma=Koma.GIN;break;
					case '金':koma=Koma.KIN;break;
					case '飛':koma=Koma.HI;break;
					case '角':koma=Koma.KAKU;break;
					case '玉':koma=Koma.OU;break;
					case '＊':koma=-1;break;
					case '＄':koma=-2;break;
				}
				if(koma!=0){
					int arr[]={j,9-lines.length+i,koma};
					list.add(arr);
				}
			}
		}
		return list.toArray(new int[0][]);
	}
}

class NariSelectEffect extends Effect{
	int count=0,X,Y,E=-1;
	int W=36,H=40;
	Koma koma,nkoma;
	int mode;
	NariSelectEffect(int x,int y,int k){
		X=x;Y=y;
		koma=new Koma(k,true,false);
		nkoma=new Koma(k,true,true);
	}
	public void cancel(){if(E<0)E=count;}
	public void run(MouseState mouse){
		count++;
		if(E>=0&&count>E+10)terminate();
		if(mode==2||mode==-2)return;
		if(mouse.y<Y||Y+H<=mouse.y){mode=0;return;}
		if(X+W/2-2-W<=mouse.x&&mouse.x<X+W/2-2)mode=-1;
		if(X+W/2+2<=mouse.x&&mouse.x<X+W/2+2+W)mode=1;
	}
	public void render(Graphics2D g){
		Composite composite=g.getComposite();
		double alpha=(count<10?(double)count/10:1)*(E>=0?1-(double)(count-E)/10:1);
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,(float)(alpha*0.8)));
		g.drawImage(bn.nariSelectImage,X-W/2-6,Y-4,null);
		if(mode==-2){
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,(float)alpha/4));
			g.setColor(Color.white);
			g.fillRect(X-W/2-2,Y,W,H);
		}if(mode==-1){
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,(float)alpha/2));
			g.setColor(Color.white);
			g.fillRect(X-W/2-2,Y,W,H);
		}
		
		if(mode==2){
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,(float)alpha/4));
			g.setColor(Color.white);
			g.fillRect(X+W/2+2,Y,W,H);
		}if(mode==1){
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,(float)alpha/2));
			g.setColor(Color.white);
			g.fillRect(X+W/2+2,Y,W,H);
		}
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,(float)alpha));
		koma.drawImage(g,X-W/2-2,Y,W,H);
		nkoma.drawImage(g,X+W/2+2,Y,W,H);
	}
	void setClicked(boolean which){
		mode=which?2:-2;
	}
}
class AvailableCellEffect extends Effect{
	LinkedList<Point>pointList;
	AvailableCellEffect(LinkedList<Point>list){pointList=list;}
	int end=-1;
	int count=0;
	int W=35,H=39;
	MouseState mouse;
	public void cancel(){if(end<0)end=count;}
	public void run(MouseState mouse){
		count++;
		if(end>=0&&count>=end+10)terminate();
		this.mouse=mouse;
	}
	public void render(Graphics2D g){
		Composite composite=g.getComposite();
		double alpha=(0.6+0.4*(1+Math.sin(count/15.0))/2)*(count<30?count/30.0:1)*(end>=0?1-(count-end)/10.0:1);
		Composite ac=AlphaComposite.getInstance(AlphaComposite.SRC_OVER,(float)(alpha*0.2));
		Composite ac2=AlphaComposite.getInstance(AlphaComposite.SRC_OVER,(float)(alpha*0.4));
		g.setColor(Color.blue);
		for(Point p:pointList){
			if(p.x<=mouse.x&&mouse.x<=p.x+W&&p.y<=mouse.y&&mouse.y<=p.y+H)g.setComposite(ac2);
			else g.setComposite(ac);
			g.fillRect(p.x+1,p.y+1,W,H);
		}
		g.setComposite(composite);
	}
}


class StartEffect extends Effect{
	int T=60,OX,OY,W,H;
	int count=-1;
	StartEffect(int ox,int oy,int w,int h){
		OX=ox;OY=oy;W=w;H=h;
	}
	
	void run(){if(++count>=T)terminate();}
	
	void render(Graphics2D g){
		Composite composite=g.getComposite();
		if(count<=5){
			g.setColor(new Color(1f,1f,1f,(float)count/5));
			g.fillRect(0,0,W,H);
			return;
		}
		float alpha=count>T-20?(float)(T-count)/20:1;
		String msg="Game Start";
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,alpha));
		ImageFont.drawText(g,msg,W/2-ImageFont.getTextWidth(msg)/2,H/2-20);
		g.setComposite(composite);
		if(count<20){
			g.setColor(new Color(1f,1f,1f,(float)(20-count)/15));
			g.fillRect(0,0,W,H);
		}
	}
}

class SparkleEffect extends Effect{
	int T,N,X,Y,W,H,L;
	int count;
	double data[][];
	SparkleEffect(int time,int num,int x,int y,int w,int h,int len){
		T=time;N=num;X=x;Y=y;W=w;H=h;L=len;
		data=new double[N][8];
		for(int i=0;i<N;i++){
			data[i][0]=X+W*Math.random();
			data[i][1]=Y+H*Math.random();
			for(int j=2;j<8;j+=2){
				double r=Math.random(),t=2*Math.PI*Math.random();
				data[i][j]=r*Math.cos(t);
				data[i][j+1]=r*Math.sin(t);
			}
		}
	}
	void run(){if(++count>=T)terminate();}
	
	void render(Graphics2D g){
		Composite composite=g.getComposite();
		double t=(float)count/T;
		double t2=t*t,t3=t*t*t;
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,(float)(t*(1-t)*(1-t)*6)));
		for(int i=0;i<N;i++){
			int x=(int)(data[i][0]+L*(data[i][2]*t+data[i][4]*t2+data[i][6]*t3));
			int y=(int)(data[i][1]+L*(data[i][3]*t+data[i][5]*t2+data[i][7]*t3));
			int j=(int)(4*Math.random());
			g.drawImage(bn.sparkleEffectImage,x-6,y-6,x+6,y+6,12*j,0,12*j+12,12,null);
		}
		g.setComposite(composite);
	}
}

class JosekiEffect extends Effect{
	int X,Y,T=32,count=0;
	double theta0;
	JosekiEffect(int x,int y){X=x;Y=y;theta0=16*Math.PI*Math.random();}
	void run(){if(++count>=T)terminate();}
	void render(Graphics2D g){
		Composite composite=g.getComposite();
		double t=(double)count/T;
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,(float)(t*(1-t)*(1-t)*(1-t)*4)));
		AffineTransform trans=g.getTransform();
		g.translate(X,Y);
		g.rotate(Math.PI/2*t+theta0);
		g.drawImage(bn.josekiEffectImage,-32,-32,64,64,null);
		g.setTransform(trans);
		g.translate(X,Y);
		g.rotate(-Math.PI/2*t+10*theta0);
		g.scale(0.6,0.6);
		g.drawImage(bn.josekiEffectImage,-32,-32,64,64,null);
		g.setTransform(trans);
		g.setComposite(composite);
	}
	
	
	static boolean isJoseki(Koma[][]omap,MKoma[][]okoma,boolean player,int x,int y,int koma){
		return isJoseki(getJoseki(omap,okoma,player),player,x,y,koma);
	}
	static boolean isJoseki(String joseki,boolean player,int x,int y,int koma){
		if(!player)y=8-y;else x=8-x;
		if(joseki==null)return false;
		String s=":"+x+""+y+""+komaConvert(koma);
		System.out.println(s+" "+joseki);
		for(String j:joseki.split("/"))if(j.endsWith(s))return true;
		return false;
	}
	static boolean isJoseki(Koma[][]omap,MKoma[][]okoma,boolean player,int x1,int y1,int x2,int y2,boolean nari){
		return isJoseki(getJoseki(omap,okoma,player),player,x1,y1,x2,y2,nari);
	}
	static boolean isJoseki(String joseki,boolean player,int x1,int y1,int x2,int y2,boolean nari){
		if(!player){y1=8-y1;y2=8-y2;}
		else{x1=8-x1;x2=8-x2;}
		if(joseki==null)return false;
		String s=":"+x1+""+y1+""+x2+""+y2+(nari?"n":"");
		System.out.println(s+" "+joseki);
		for(String j:joseki.split("/"))if(j.endsWith(s))return true;
		return false;
	}
	static int komaConvert(int k){
		switch(k){
			case Koma.HI:return 8;
			case Koma.KAKU:return 7;
			case Koma.FU:return 1;
			case Koma.KYO:return 2;
			case Koma.KEI:return 3;
			case Koma.GIN:return 4;
			case Koma.KIN:return 5;
			case Koma.OU:return 6;
			default:return 0;
		}
	}
	static String getJoseki(Koma[][]omap,MKoma[][]okoma,boolean player){
		int[][]map=new int[9][9];
		int[][]koma=new int[2][8];
		for(int i=0;i<9;i++)for(int j=0;j<9;j++){
			Koma k=omap[i][j];
			if(k!=null)map[player?8-i:i][player?j:8-j]=(komaConvert(k.koma)+(k.nari?10:0))*(k.player?1:-1)*(player?1:-1);
		}
		for(int p=0;p<2;p++)for(int i=2;i<9;i++){
			int p2=player?p:1-p;
			koma[p2][komaConvert(okoma[p][i].koma.koma)-1]=okoma[p][i].count;
		}
		return josekiMap.get(new Integer(hashData(map,koma)));
	}
	static int hashData(int[][]map,int[][]koma){
		int a=1234,b=3456,c=5678;
		for(int i=0;i<9;i++)for(int j=0;j<9;j++){
			int k=map[i][j];
			a=(a*7183+1619*b+(b&0xff)*(c&0xfff)+173*k)&0xffff;
			b=(b*1573+2431*c+(c&0xff)*(a&0xfff)+119*k)&0xffff;
			c=(c*9971+1517*a+(a&0xff)*(b&0xfff)+313*k)&0xffff;
		}
		for(int i=0;i<8;i++){
			int n1=koma[0][i],n2=koma[1][i];
			a=(a*5411+1531*b+(b&0xff)*(c&0xfff)+473*n1+353*n2)&0xffff;
			b=(b*1297+1697*c+(c&0xff)*(a&0xfff)+219*n1+223*n2)&0xffff;
			c=(c*8153+2731*a+(a&0xff)*(b&0xfff)+331*n1+187*n2)&0xffff;
		}
		for(int i=0;i<9;i++)for(int j=0;j<9;j++){
			int k=map[i][j];
			a=(a*7183+1619*b+(b&0xff)*(c&0xfff)+173*k)&0xffff;
			b=(b*1573+2431*c+(c&0xff)*(a&0xfff)+119*k)&0xffff;
			c=(c*9971+1517*a+(a&0xff)*(b&0xfff)+313*k)&0xffff;
		}
		return ((a<<16)|c)^(b<<8);
	}	
	
	static HashMap<Integer,String>josekiMap=new HashMap<Integer,String>();
	static void loadData()throws Exception{
		BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(ResLoader.getResFile("joseki.txt"))));
		while(true){
			String line=br.readLine();
			if(line==null)break;
			if(line.length()<8)continue;
			josekiMap.put(new Integer((int)Long.parseLong(line.substring(0,8).toUpperCase(),16)),line.substring(9));
		}
		br.close();
	}
}
