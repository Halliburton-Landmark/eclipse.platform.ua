<%@ page import="java.net.URLEncoder,org.eclipse.help.servlet.*,org.w3c.dom.*" errorPage="err.jsp" contentType="text/html; charset=UTF-8"%>

<% 
	// calls the utility class to initialize the application
	application.getRequestDispatcher("/servlet/org.eclipse.help.servlet.InitServlet").include(request,response);
%>


<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<!--
 (c) Copyright IBM Corp. 2000, 2002.
 All Rights Reserved.
-->
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<meta http-equiv="Pragma" content="no-cache">
<meta http-equiv="Expires" content="-1">
<base target="MainFrame">
<!--
<script language="JavaScript" src="list.js"></script>
-->

<style type="text/css">

BODY {
	font: 8pt Tahoma;
	margin-top:5px;
	margin-left:5px;
	padding:0;
	border:0;
	cursor:default;
}


A {
	text-decoration:none; 
	color:black; 
	padding:0px;
	white-space: nowrap;
	cursor:default;
}


TABLE {
	font: 8pt Tahoma;
	width:100%;
}


IMG {
	border:0px;
	margin:0px;
	padding:0px;
	margin-right:4px;
}



.list {
	padding:2px;
}
     
.active { 
	background:ButtonFace;
	padding:2px;
}

.label {
	margin-left:4px;
}


</style>

</head>


<body BGCOLOR="#FFFFFF">
 
<%
if(request.getParameter("contextId")!=null)
{
	// Load the links
	ContentUtil content = new ContentUtil(application, request);
	Element linksElement = content.loadLinks(request.getQueryString());
	if (linksElement == null){
		out.write(WebappResources.getString("Nothing_found", null));
		return;
	}
	
	// Generate list
	NodeList topics = linksElement.getElementsByTagName("topic");
	if (topics == null || topics.getLength() == 0){
		out.write(WebappResources.getString("Nothing_found", null));
		return;
	}
%>

<table id='list'  cellspacing='0' nowrap>

<%
	for (int i = 0; i < topics.getLength(); i++) 
	{
		Element topic = (Element)topics.item(i);
		String tocLabel = topic.getAttribute("toclabel");
		String label = topic.getAttribute("label");
		String href = topic.getAttribute("href");
		if (href != null && href.length() > 0) {
			// external href
			if (href.charAt(0) == '/')
				href = "content/help:" + href;
			else if (href.startsWith("file:/"))
				href = "content/" + href;
				
			if (href.indexOf('?') == -1)
				href +="?toc="+URLEncoder.encode(topic.getAttribute("toc"));
			else
				href += "&toc="+URLEncoder.encode(topic.getAttribute("toc"));			

		} else
			href = "javascript:void 0";
%>

<tr class='list' nowrap align=left>
	<td align='left' nowrap width="16">
		<img src="../images/topic.gif" border=0>
	</td>
	<td align='left' class='label' nowrap>
		<a href='<%=href%>' onclick='parent.parent.setToolbarTitle("<%=UrlUtil.JavaScriptEncode(tocLabel)%>")' title="<%=label%>"><%=label%></a>
	</td>
</tr>

<%
	}
%>

</table>

<%
}else{
	out.write(WebappResources.getString("pressF1", request));
}

// Highlight topic
String topic = request.getParameter("topic");
if (topic != null && topic.startsWith("/"))
	topic = request.getContextPath() + "/content/help:" + topic;
else if (topic != null && topic.startsWith("file:/"))
	topic = request.getContextPath() + "/content/" + topic;
%>

<script language="JavaScript">
/*
var topic = window.location.protocol + "//" +window.location.host + '<%=topic%>';
selectTopic(topic);
*/
</script>

</body>
</html>
