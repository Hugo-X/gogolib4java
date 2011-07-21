import kr.ac.scnu.cn.gogolib.GoGoClient;

public class Beep
{
    public static void main(String[] argv)throws Exception{
        // connect to local running GoGo Monitor        
        GoGoClient ggClient = new GoGoClient("localhost", 9873);
        
        
        ggClient.beep();
        System.out.println(ggClient.receive());
        
        ggClient.close();
    }
}
