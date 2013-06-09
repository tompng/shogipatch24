import java.awt.Frame;
public class OsakaDojo extends Frame{
  public static void main(String args[])throws Exception{
    java.util.HashMap<String,String>param=new java.util.HashMap<String,String>();
    param.put("show5DTab","ON");
    param.put("rankSwitch","OFF");
    param.put("useCookie","ON");
    param.put("relayTournament","ON");
    String base="http://internet8.shogidojo.net/dojo/";
    String name="OsakaDojo";
    Dojo.start(base,name,param);
  }
}
