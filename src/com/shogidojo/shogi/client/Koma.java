package com.shogidojo.shogi.client;
import java.awt.*;

public class Koma{
	public static final int FU=8,KYO=7,KEI=6,GIN=5,KIN=4,KAKU=3,HI=2,OU=1;
	int koma;
	boolean player;
	boolean nari;
	boolean visible=true;
	Koma(int k,boolean p,boolean n){
		koma=k;player=p;nari=n;
	}
	Koma(int k){
		player=k<0?false:true;
		koma=Math.abs(k);
		if(koma>10){nari=true;koma-=10;}
		else nari=false;
	}
	public static Image komaImage,komaUpImage;
	private static int komaImageGetIX(int koma,boolean player,boolean nari){
		if(koma==FU)return nari?6:0;
		if(nari&&koma!=HI&&koma!=KAKU)return koma-2;
		return koma-1;
	}
	private static int komaImageGetIY(int koma,boolean player,boolean nari){
		return (koma==FU||nari?2:0)+(player?0:1);
	}
	void drawImage(Graphics g,int x,int y,int w,int h){
		int ix=komaImageGetIX(koma,player,nari),iy=komaImageGetIY(koma,player,nari);
		g.drawImage(komaImage,x,y,x+w,y+h,36*ix,40*iy,36*(ix+1),40*(iy+1),null);
	}
	void drawUpImage(Graphics g,int x,int y,int w,int h){
		int ix=komaImageGetIX(koma,player,nari),iy=komaImageGetIY(koma,player,nari);
		y-=h/5;
		g.drawImage(komaUpImage,x,y,x+w,y+h*5/4,36*ix,50*iy,36*(ix+1),50*(iy+1),null);
	}
	boolean canMoveTo(int dx,int dy){
		if(dx==0&&dy==0)return false;
		if(!player)dy*=-1;
		int k=(nari&&koma!=HI&&koma!=KAKU)?KIN:koma;
		switch(nari&&koma!=HI&&koma!=KAKU?KIN:koma){
			case KIN:return (dy==-1&&dx*dx<=1)||(dy==0&&dx*dx==1)||(dy==1&&dx==0);
			case GIN:return (dy==-1&&dx*dx<=1)||(dy==1&&dx*dx==1);
			case KEI:return dy==-2&&dx*dx==1;
			case KYO:return dx==0&&dy<0;
			case FU:return dx==0&&dy==-1;
			case OU:return dx*dx<=1&&dy*dy<=1;
			case HI:return (dx==0||dy==0)||(nari&&dx*dx<=1&&dy*dy<=1);
			case KAKU:return dx*dx==dy*dy||(nari&&dx*dx<=1&&dy*dy<=1);
			default:return false;
		}
	}
}

