# Deployment #

  1. Configure the java development environment first
  1. Download the gogolib.jar
  1. Use gogolib.jar as a library for your Java program


---

# Code and Compile #

  1. Add the package to your Java code where you need to use _gogolib_ by `import kr.ac.scnu.cn.gogolib.*`
  1. You can get the API specification and other documents about this library at [Downloads](http://code.google.com/p/gogolib4java/downloads/list?can=2&q=docs)
  1. Be sure when you compile your Java code, gogolib.jar is in your classpath


---

# Example1. SimpleGoGoClient #

### Content in SimpleGoGoClient.java ###
```
/*
* Put the gogolib.jar beside this source code
* Compile SimpleGoGoClient.java by command -> javac -cp gogolib.jar SimpleGoGoClient.java
*/
import kr.ac.scnu.cn.gogolib.GoGoClient;
import java.io.*;

public class SimpleGoGoClient
{	
	public static void main(String []argv) throws Exception
	{
		String strToServer, strFromServer;
		String	host;
		int port;
		GoGoClient ggClient;
		
		// default connection
		host="localhost";
		port=9873;
		
		if(argv.length>0){
			if(argv.length!=2){
				System.out.println("Incorrect arguments.");
				System.out.println("Usage: java -jar gogoClient.jar ip port | no arguments will connect to localhost:9873");
				return;
			}
			host=argv[0];
			port=Integer.parseInt(argv[1]);
		}
		
		ggClient = new GoGoClient(host, port);		
		
		if(ggClient == null)
			return;
			
		System.out.print("Connected with GoGo Server@"+host+":"+port+"(Type Q or q to Quit)\n>");
		
		BufferedReader inFromUser =	 new BufferedReader(new InputStreamReader(System.in));
		strToServer = inFromUser.readLine();
		
		while (!(strToServer.equals("Q") || strToServer.equals("q")))
		{		 
			//send
			System.out.println(">>>>>Sending "+strToServer+"...");		
			ggClient.send(strToServer);

			//recv
			strFromServer = ggClient.receive();
			System.out.println("<"+strFromServer);

			//next
			System.out.print(">");
			strToServer = inFromUser.readLine();
		}
	
	} 
}
```

### Compile Command ###
javac -cp gogolib.jar SimpleGoGoClient.java

### Execute Command ###
java -cp .;gogolib.jar SimpleGoGoClient

# Example 2 #
### Beep.java ###
```
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

```

### Compile Command ###
javac -cp gogolib.jar Beep.java

### Execute Command ###
java -cp .;gogolib.jar Beep

# Example 3 #
### LedController.java ###
```
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

```

### Compile Command ###
javac -cp gogolib.jar LedController.java

### Execute Command ###
java -cp .;gogolib.jar LedController