package com.shogidojo.shogi.client;


public class b7 extends Thread{
	bk shogi;
	int command;
	int timeout=1000;
	public b7(bk shogi,String state,String msg1,String msg2,int command){
		this.shogi=shogi;
		this.command=command;
		start();
		if(state==null)return;
		if(state.equals(ShogiClient.b("You lose"))){shogi.g().notifyWinLose(false);}
		if(state.equals(ShogiClient.b("You win"))){shogi.g().notifyWinLose(true);}
	}
	public void run(){
		try{Thread.sleep(timeout);}catch(Exception e){}
		shogi.b(command);
	}
}
