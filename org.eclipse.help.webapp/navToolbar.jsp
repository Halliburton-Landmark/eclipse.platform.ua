<%@ page import="org.eclipse.help.servlet.*" errorPage="err.jsp" contentType="text/html; charset=UTF-8"%>

<% 
	// calls the utility class to initialize the application
	application.getRequestDispatcher("/servlet/org.eclipse.help.servlet.InitServlet").include(request,response);
	WebappPreferences prefs = (new RequestData(application,request)).getPrefs();
%>


<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<!--
 (c) Copyright IBM Corp. 2000, 2002.
 All Rights Reserved.
-->
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">

<title><%=WebappResources.getString("Toolbar", request)%></title>

<style type="text/css">
/* need this one for Mozilla */
HTML { 
	width:100%;
	height:100%;
	margin:0px;
	padding:0px;
	border:0px;
 }
 
BODY {
	background:<%=prefs.getToolbarBackground()%>;
}

#titleText {
	font-weight:bold;
}
 
</style>

</head>

<body>
	<div id="textLayer" style="position:absolute; z-index:1; left:0; top:0; height:100%; width:100%;">
		<table width="100%" border="0" cellspacing="0" cellpadding="0" height="100%" style="padding-left:5;">
			<tr>
				<td style="font: <%=prefs.getToolbarFont()%>;">
					<div id="titleText">
						<%=WebappResources.getString("Content", request)%>
					</div>
				</td>
			</tr>
		</table>
	</div>
	<div id="borderLayer" style="position:absolute; z-index:2; left:0; top:0; height:100%; width:100%; ">
		<table width="100%" border="0" cellspacing="0" cellpadding="0" height="100% ">
			<tr>
				<td style="border:1px solid WindowText; border-left:0px;">
					&nbsp;
				</td>
			</tr>
		</table>
	</div>	
	<div id="iconLayer" style="position:absolute; z-index:3; left:0; top:0; height:100%; width:100%;">
		<table width="100%" border="0" cellspacing="0" cellpadding="0" height="100%" style="padding-top:4; padding-right:3;">
			<tr>
				<td>
					&nbsp;
				</td>
				<!--
				<td align="middle" width="20">
					<a  href="#" onclick="parent.showBookshelf(this); this.blur();" onmouseover="window.status='<%=WebappResources.getString("Bookshelf", request)%>';return true;" onmouseout="window.status='';">
						<img  id="bookshelfIcon" src="<%=prefs.getImagesDirectory()%>/home_cont.gif" alt='<%=WebappResources.getString("Bookshelf", request)%>' border="0">
					</a>
				</td>
				-->
			</tr>
		</table>
	</div>	
</body>
</html>