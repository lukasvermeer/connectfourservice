A bit of a wobbly hack now, but it works. I think the interface is nice, clean and simple this way. Service is available at [http://connectfourservice.appspot.com/](). Example session below.

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