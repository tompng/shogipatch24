public class TokyoDojo{
  public static void main(String args[])throws Exception{
    java.util.HashMap<String,String>param=new java.util.HashMap<String,String>();
    param.put("show5DTab","ON");
    param.put("rankSwitch","OFF");
    param.put("useCookie","ON");
    String base="http://internet2.shogidojo.net/dojo/";
    String name="TokyoDojo";
    Dojo.start(base,name,param);
  }
}
