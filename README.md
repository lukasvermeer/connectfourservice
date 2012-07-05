A bit of a wobbly hack now, but it works. I think the interface is nice, clean and simple this way. Service is available at [http://connectfourservice.appspot.com/](http://connectfourservice.appspot.com/). Example session below.

Request parameters (parsed to datatypes as indicated):

- __newgame__ _(bool "true" or "false")_: Starts a new game if true (false is default if not supplied). Can be combined with move, if you want to start.
- __move__ _(int)_: Column you want to play next. Start counting at 0 from left to right.
- __state__ _(string)_: Optional use for testing. Current board state (including the move or newgame you send). Cell values separated by commas; empty cells are represented by the empty string. Keep in mind JSON string is also terminated by a comma. Service will always assume its plays are represented by "0" and yours by "1".

Response parameters. 

- __response__ _(string)_: Can be either "move" or "error". (not sure this is really needed.)
move (int.toString): Column service wants to play next. Start counting at 0 from left to right.
- __message__ _(string)_: In case of error will contain reason. Can contain additional information during play.
- __state__ _(string)_: Optional use for testing (service will always return it). Current board state (including the latest move). Cell values separated by commas; empty cells are represented by the empty string. Keep in mind JSON string is also terminated by a comma. Service will always assume its plays are represented by "0" and yours by "1".

Standard HTTP cookie session is used to track session.

Service can also throw exceptions, but these are not handled by this protocol. May add this later if needed.

If you find any bugs or issues, please let me know; or fork, fix and push if you want it fixed quickly.

I've tweaked the minimax algorithm to go only two steps deep. In other words, it is now incredibly, horribly bad at playing the game. This makes the service quicker to play and easier to beat. Of course, I intend to make it a lot smarter later ... :-)

***

Example session using Telnet.

Max:~ lukas$ telnet connectfourservice.appspot.com 80
Trying 74.125.132.141...
Connected to appspot.l.google.com.
Escape character is '^]'.
GET /service?newgame=true&width=7&height=7 HTTP/1.1
host: connectfourservice.appspot.com

[we start a new game, let service move first]

HTTP/1.1 200 OK
Expires: Thu, 01 Jan 1970 00:00:00 GMT
Set-Cookie: JSESSIONID=6PrVlFhLZmgp8OU8Zrs82Q;Path=/
Content-Type: text/x-json;charset=UTF-8
Cache-Control: no-cache
Vary: Accept-Encoding
Date: Sat, 16 Jun 2012 11:14:00 GMT
Server: Google Frontend
Transfer-Encoding: chunked

5a
{"response":"move","state":",,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,0,,,,,","move":"1"}
0


GET /service?move=2 HTTP/1.1
host: connectfourservice.appspot.com
Cookie: JSESSIONID=6PrVlFhLZmgp8OU8Zrs82Q;Path=/

[we move in third column from the left]

HTTP/1.1 200 OK
Content-Type: text/x-json;charset=UTF-8
Cache-Control: no-cache
Vary: Accept-Encoding
Date: Sat, 16 Jun 2012 11:14:14 GMT
Server: Google Frontend
Transfer-Encoding: chunked

5c
{"response":"move","state":",,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,0,,,,,,,0,1,,,,","move":"1"}
0

GET /service?move=2 HTTP/1.1
host: connectfourservice.appspot.com
Cookie: JSESSIONID=6PrVlFhLZmgp8OU8Zrs82Q;Path=/

HTTP/1.1 200 OK
Content-Type: text/x-json;charset=UTF-8
Cache-Control: no-cache
Vary: Accept-Encoding
Date: Sat, 16 Jun 2012 11:14:16 GMT
Server: Google Frontend
Transfer-Encoding: chunked

5e
{"response":"move","state":",,,,,,,,,,,,,,,,,,,,,,,,,,,,,,0,,,,,,0,1,,,,,,0,1,,,,","move":"2"}
0

[service decides to block our stack in third column from the left]

GET /service?move=2 HTTP/1.1
host: connectfourservice.appspot.com
Cookie: JSESSIONID=6PrVlFhLZmgp8OU8Zrs82Q;Path=/

HTTP/1.1 200 OK
Content-Type: text/x-json;charset=UTF-8
Cache-Control: no-cache
Vary: Accept-Encoding
Date: Sat, 16 Jun 2012 11:14:17 GMT
Server: Google Frontend
Transfer-Encoding: chunked

60
{"response":"move","state":",,,,,,,,,,,,,,,,,,,,,,,1,,,,,,0,0,,,,,,0,1,,,,,,0,1,,,,","move":"1"}
0

GET /service?move=2 HTTP/1.1
host: connectfourservice.appspot.com
Cookie: JSESSIONID=6PrVlFhLZmgp8OU8Zrs82Q;Path=/

HTTP/1.1 200 OK
Content-Type: text/x-json;charset=UTF-8
Cache-Control: no-cache
Vary: Accept-Encoding
Date: Sat, 16 Jun 2012 11:14:19 GMT
Server: Google Frontend
Transfer-Encoding: chunked

80
{"response":"move","message":"Looks like I won!","state":",,,,,,,,,,,,,,,,1,,,,,,0,1,,,,,,0,0,,,,,,0,1,,,,,,0,1,,,,","move":"1"}
0

[service completes four in a row; gloats about it too]

GET /service?move=2 HTTP/1.1
host: connectfourservice.appspot.com
Cookie: JSESSIONID=6PrVlFhLZmgp8OU8Zrs82Q;Path=/

[we attempt to make a next move regardless]

HTTP/1.1 200 OK
Content-Type: text/x-json;charset=UTF-8
Cache-Control: no-cache
Vary: Accept-Encoding
Date: Sat, 16 Jun 2012 11:14:23 GMT
Server: Google Frontend
Transfer-Encoding: chunked

33
{"response":"error","message":"This game is over!"}
0

[service responds that game is already over and newgame=true is required to continue]