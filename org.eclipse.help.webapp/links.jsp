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

<title><%=WebappResources.getString("Links", request)%></title>

<style type="text/css">
BODY {
	background-color: Window;
	font: icon;
	margin-top:5px;
	margin-left:5px;
	padding:0;
	border:0;
	cursor:default;
}

A {
	text-decoration:none; 
	color:WindowText; 
	padding:0px;
	white-space: nowrap;
}

A:hover {
	text-decoration:underline; 
}

IMG {
	border:0px;
	margin:0px;
	padding:0px;
	margin-right:4px;
}

TABLE {
	background-color: Window;
	font: icon;
	width:100%;
}

.list {
	background-color: Window;
	padding:2px;
}
     
.active { 
	background:ButtonFace;
	width:100%;
	height:100%;
}

.label {
	margin-left:4px;
}

</style>

<base target="MainFrame">
<script language="JavaScript" src="list.js"></script>
<script language="JavaScript">		
var extraStyle = "";
if (isMozilla)
	extraStyle = "<style type='text/css'>.active, A.active:hover {background:WindowText;color:Window;} </style>";
 
document.write(extraStyle);
</script>

</head>


<body>
 
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

<table id='list'  cellspacing='0' >

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
			href = "about:blank";
%>

<tr class='list' id='r<%=i%>'>
	<td align='left' class='label' nowrap>
		<a id='a<%=i%>' href='<%=href%>' onclick='parent.parent.setToolbarTitle("<%=UrlUtil.JavaScriptEncode(tocLabel)%>")' title="<%=UrlUtil.htmlEncode(label)%>"><img src="images/topic.gif"><%=UrlUtil.htmlEncode(label)%></a>
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
// check if the topic URL starts with http
if (!'<%=topic%>'.indexOf("http")==0)
	selectTopic(window.location.protocol + "//" +window.location.host + '<%=topic%>');
else
	selectTopic('<%=topic%>');
</script>

</body>
</html>
