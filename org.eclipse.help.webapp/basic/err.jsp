<%@ page contentType="text/html; charset=UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<!--
 (c) Copyright IBM Corp. 2000, 2002.
 All Rights Reserved.
-->
<html>

<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title> Error </title>
</head>
<body>

	<%@ page isErrorPage="true" %>
	
	There was an error in your action:
	<p>
	<%= exception.toString() %>
	</P>
	
</body>
</html>

