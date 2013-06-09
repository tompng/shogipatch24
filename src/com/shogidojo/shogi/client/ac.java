package com.shogidojo.shogi.client;

import com.shogidojo.shogi.command.*;
import com.shogidojo.shogi.common.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class ac extends Frame implements WindowListener, ActionListener, ItemListener, FocusListener, c{
	private Label labelHeader,labelPlayerInfo,labelTeaiwari,labelGameMode;
	private Button buttonPlay,buttonSorry;
	private Choice choice;
	private String userID;
	private boolean resumeFlag=false;
	private ShogiClient shogiclient;
	private boolean showFlag=false;

	public ac(ShogiClient paramShogiClient){
		super(ShogiClient.b("Challenge Response"));
		shogiclient=paramShogiClient;
		int m=-1;
		int n=0;
		GridBagLayout localGridBagLayout=new GridBagLayout();
		GridBagConstraints localGridBagConstraints=new GridBagConstraints();
		setLayout(localGridBagLayout);
		labelHeader=new Label(ShogiClient.b("Challenged header"));
		labelHeader.setSize(250, 20);
		labelPlayerInfo=new Label();
		labelTeaiwari=new Label();
		labelGameMode=new Label();
		Label labelMessage=new Label(ShogiClient.b("Will you play?"));
		labelMessage.setSize(200, 20);
		buttonPlay=new Button(ShogiClient.b("Play"));
		buttonSorry=new Button(ShogiClient.b("Sorry"));
		choice=new Choice();
		choice.addItem(ShogiClient.b("--reason for Sorry--"));
		choice.addItem(ShogiClient.b("for suspended game"));
		choice.addItem(ShogiClient.b("for reserved game"));
		choice.addItem(ShogiClient.b("for challenging other"));
		choice.addItem(ShogiClient.b("for busy"));
		choice.addItem(ShogiClient.b("another timelimited"));
		choice.addItem(ShogiClient.b("another handicap"));
		choice.addItem(ShogiClient.b("Masters Finished"));
		localGridBagConstraints.insets=new Insets(2, 5, 2, 5);
		localGridBagConstraints.anchor=17;
		layoutPut(-1, -1, 0, 1, localGridBagLayout, localGridBagConstraints, labelHeader, this);
		localGridBagConstraints.anchor=10;
		layoutPut(-1, -1, 0, 1, localGridBagLayout, localGridBagConstraints, labelPlayerInfo, this);
		layoutPut(-1, -1, 0, 1, localGridBagLayout, localGridBagConstraints, labelTeaiwari, this);
		localGridBagConstraints.anchor=17;
		layoutPut(-1, -1, 0, 1, localGridBagLayout, localGridBagConstraints, labelGameMode, this);
		layoutPut(-1, -1, 0, 1, localGridBagLayout, localGridBagConstraints, labelMessage, this);
		localGridBagConstraints.anchor=13;
		layoutPut(-1, -1, 0, 1, localGridBagLayout, localGridBagConstraints, choice, this);
		localGridBagConstraints.anchor=17;
		layoutPut(-1, -1, 1, 1, localGridBagLayout, localGridBagConstraints, buttonPlay, this);
		localGridBagConstraints.anchor=13;
		layoutPut(-1, -1, 1, 1, localGridBagLayout, localGridBagConstraints, buttonSorry, this);
	}

	private void layoutPut(int x, int y, int w, int h, GridBagLayout gridbag, GridBagConstraints constraints, Component component, Container container){
		constraints.gridx=x;
		constraints.gridy=y;
		constraints.gridwidth=w;
		constraints.gridheight=h;
		gridbag.setConstraints(component, constraints);
		container.add(component);
	}

	public void showFrame(){
		setLocation(20, 20);
		setVisible(true);
		toFront();
	}

	private void setGameModeLabel(String msgkey){
		labelGameMode.setSize(250,20);
		labelGameMode.setText(ShogiClient.b(msgkey));
		pack();
	}


	private void challenge(ChallengedCommand challenge){
		if (isShowing()){
			toFront();
			return;
		}
		userID=challenge.getSimpleUserInfo().getUserID();
		showFlag=true;
		SimpleUserInfo info=challenge.getSimpleUserInfo();
		String str1=ShogiClient.b(k.a(info.getTitle(), info.getStrengthStr()));
		String rate=info.getMemberType()==2?"("+info.getRating()+")":" ";
		String mobile="@mobile".equals(info.getLocality())?ShogiClient.b("@mobile"):"";
		labelPlayerInfo.setSize(250, 20);
		labelPlayerInfo.setText("< "+str1+rate+userID+" >"+mobile);
		pack();
		
		
		
		labelTeaiwari.setVisible(false);
		resumeFlag=challenge.getResumeFlag();
		switch (shogiclient.x()){
			case 8:
			case 9:
				if (!shogiclient.at().a(challenge.getSimpleUserInfo().getUserID()))break;
				resumeFlag=true;
				break;
		}
		if(resumeFlag){
			setGameModeLabel("Resumption request of suspended game! Please OK.");
			setBackground(Color.cyan);
		}else{
			int m=challenge.getGameCondition().getHandy();
			if (m!=0){
				String handyWho=ShogiClient.b(challenge.getSimpleUserInfo().getRating()>shogiclient.n()?"his side":"your side");
				String handyArray[]={"Hirate","Kyo ochi","Kaku ochi","Hi ochi","HiKyo ochi","2-mai ochi","4-mai ochi","6-mai ochi"};
				String handyString=handyArray[m<0||m>=handyArray.length?0:m];
				labelTeaiwari.setText(handyWho+" [[ "+ShogiClient.b(handyString)+" ]]");
				labelTeaiwari.setVisible(true);
			}
			String timemode="?";
			switch(challenge.getGameCondition().getTimeCondition2()){
				case 1:
					timemode="normal-timelimited";
					setBackground(new Color(250, 245, 175));
					break;
				case 9:
					timemode="short2-timelimited";
					setBackground(new Color(255, 153, 153));
					break;
				case 2:
					timemode="short-timelimited";
					setBackground(new Color(245, 185, 250));
					break;
				case 3:
					timemode="long-timelimited";
					setBackground(new Color(170, 250, 190));
					break;
				case 4:
					timemode="special";
					setBackground(new Color(125, 190, 255));
					break;
				case 5:
					timemode="1hour-timelimited";
					setBackground(new Color(125, 190, 255));
					break;
				case 6:
					timemode="90min.-timelimited";
					setBackground(new Color(125, 190, 255));
					break;
			}
			setGameModeLabel(timemode);
		}
		choice.select(0);
		buttonPlay.setEnabled(true);
		addWindowListener(this);
		buttonPlay.addActionListener(this);
		buttonSorry.addActionListener(this);
		choice.addItemListener(this);
		pack();
		
		
		int request=shogiclient.s();
		if(!resumeFlag&&3<=request&&request<=6){
			int condition=challenge.getGameCondition().getTimeCondition2();
			if((request==3&&condition!=3)||(request==4&&condition!=2)||(request==5&&condition!=1)||(request==6&&condition!=9)){
				shogiclient.g();
				choice.select(5);
				actionPerformed(new ActionEvent(buttonSorry,0,buttonPlay.getActionCommand()));
				return;
			}
		}
		
		showFrame();
		labelHeader.requestFocus();
		shogiclient.g();
	}

	private void cancel(){
		if (showFlag){
			shogiclient.f(userID);
			hideFrame();
		}
	}

	private void sendPlayCommand(){
		try{
			hideFrame();
			shogiclient.k().a(resumeFlag, true, 0);
			return;
		}
		catch (Exception e){
		}
	}

	private void sendSorryCommand(){
		try{
			int msgid;
			switch(choice.getSelectedIndex()){
				case 1:msgid=257;break;
				case 2:msgid=258;break;
				case 3:msgid=259;break;
				case 4:msgid=260;break;
				case 5:msgid=261;break;
				case 6:msgid=262;break;
				case 7:msgid=263;break;
				default:msgid=1;break;
			}
			hideFrame();
			shogiclient.k().a(resumeFlag, false, msgid);
			return;
		}
		catch (Exception e){}
	}

	private void hideFrame(){
		showFlag=false;
		if(!isShowing())return;
		removeWindowListener(this);
		buttonPlay.removeActionListener(this);
		buttonSorry.removeActionListener(this);
		choice.removeItemListener(this);
		setVisible(false);
		labelPlayerInfo.setText("");
	}
	
	public void a(ChallengedCommand challenge){
		if (shogiclient.m().startsWith("[")){
			shogiclient.k().a(false, false, 1);
			return;
		}
		switch (shogiclient.x()){
		case 4:
		case 6:
			if (shogiclient.e(challenge.getSimpleUserInfo().getUserID())){
				shogiclient.k().a(false, false, 1);
				return;
			}
			challenge(challenge);
			return;
		case 8:
		case 9:
			if (shogiclient.at().a(challenge.getSimpleUserInfo().getUserID())){
				challenge(challenge);
				return;
			}
			shogiclient.k().a(false, false, 257);
			return;
		}
	}

	public void a(ChallengeCanceledCommand cancel){
		switch (shogiclient.x()){
			case 4:
			case 6:
				cancel();
				return;
			case 8:
			case 9:
				shogiclient.at().e();
				cancel();
				return;
		}
	}

	public void actionPerformed(ActionEvent e){
		int mode=shogiclient.x();
		if (e.getSource()==buttonPlay){
			if(mode==4||mode==8)sendPlayCommand();
		}else if (e.getSource()==buttonSorry){
			if(mode==4)sendSorryCommand();
			else if(mode==8){
				sendSorryCommand();
				shogiclient.at().f();
			}
		}
	}

	public void itemStateChanged(ItemEvent paramItemEvent){
		if(choice.getSelectedIndex()==0)buttonPlay.setEnabled(true);
		else  buttonPlay.setEnabled(false);
	}
	public void windowClosing(WindowEvent paramWindowEvent){sendSorryCommand();}
	public void windowOpened(WindowEvent paramWindowEvent){}
	public void windowClosed(WindowEvent paramWindowEvent){}
	public void windowIconified(WindowEvent paramWindowEvent){}
	public void windowDeiconified(WindowEvent paramWindowEvent){}
	public void windowActivated(WindowEvent paramWindowEvent){}
	public void windowDeactivated(WindowEvent paramWindowEvent){}
	public void focusLost(FocusEvent paramFocusEvent){toFront();}
	public void focusGained(FocusEvent paramFocusEvent){}
}