
On my Windows 95 system the grid server crashes if the
oneway shutdown method is called by the client to
shutdown the server. On Windows NT it works well.
The reason is unknown.

If using non oneway functions for shutdown it works
even well on Windows 95. See db_query example.

