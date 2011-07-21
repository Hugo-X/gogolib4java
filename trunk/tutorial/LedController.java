import kr.ac.scnu.cn.gogolib.GoGoClient;

public class LedController
{
    public static void main(String[] argv)throws Exception{
        // connect to local running GoGo Monitor        
        GoGoClient ggClient = new GoGoClient("localhost", 9873);
        
        //the argument should be 'on' or 'off'
        if( argv[0].equalsIgnoreCase("on"))
            ggClient.ledOn();
        if( argv[0].equalsIgnoreCase("off"))
             ggClient.ledOff();
             
        System.out.println(ggClient.receive());
        ggClient.close();
    }
}
