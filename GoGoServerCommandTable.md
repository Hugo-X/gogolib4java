## GoGo Server Command Table (GoGo Monitor 3.4.x) ##
| **Command** | **Parameter** | **Function** | **Successful Response** |
|:------------|:--------------|:-------------|:------------------------|
|setcom _**c**_ |c∈{1,2,3…}|set com port|S: ok|
|connect|  |connect to GoGo board|S: ok|
|disconnect|  |disconnect from GoGo board|S: ok|
|reconnect|  |connect to GoGo board|S: ok|
|talktoport _**p**_ |p∈{a,b,c,d}|select a motor to be controlled|S: ok|
|setpower _**l**_ |l∈{1,2,3,4,5,6,7}|set power level|S: ok |
|on|  |Same as GoGo Monitor button [ON](ON.md)|S: ok|
|off|  |Same as GoGo Monitor button [OFF](OFF.md)|S: ok|
|break|  |Same as GoGo Monitor button [Break](Break.md)|S: ok|
|coast|  |Same as GoGo Monitor button [Coast](Coast.md)|S: ok|
|thisway|  |Same as GoGo Monitor button [way](This.md)|S: ok|
|thatway|  |Same as GoGo Monitor button [way](That.md)|S: ok|
|rd|  |Same as GoGo Monitor button [RD](RD.md)|S: ok|
|beep|  |Ask GoGo Board beep|S: ok|
|ledon|  |Ask GoGo Board Turn user led on|S: ok|
|ledoff|  |Ask GoGo Board Turn user led off|S: ok|
|burston|  |Ask GoGo Board to run in Burst-On mode|S: ok|
|burstoff|  |Ask GoGo Board to run in Burst-Off mode|S: ok|
|sensor<em><b>i</b></em> |i∈{1,2,3,4,5,6,7,8}|Get the value of sensor i|S: The value of specified sensor|
|sensormax<em><b>i</b></em> |i∈{1,2,3,4,5,6,7,8}|Get the max value of sensor i|S: The max-value of specified sensor|
|sensormin<em><b>i</b></em> |i∈{1,2,3,4,5,6,7,8}|Get the min value of sensor i|S: The min-value of specified sensor|
|Any other|  |  |F: GoGo Monitor Error: Unrecognized command|