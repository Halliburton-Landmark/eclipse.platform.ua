<%@ page import="java.net.URLEncoder,java.text.NumberFormat,org.eclipse.help.servlet.*,org.w3c.dom.*" errorPage="err.jsp" contentType="text/html; charset=UTF-8"%>

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

<title><%=WebappResources.getString("SearchResults", request)%></title>

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
	padding-left:2px;
	white-space: nowrap;
}

A:hover {
	text-decoration:underline; 
}


TABLE {
	background-color: Window;
	font: icon;
}

.score {
	padding-right:5px;
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

</style>


<base target="MainFrame">
<script language="JavaScript" src="list.js"></script>
<script language="JavaScript">		
var extraStyle = "";
if (isMozilla)
	extraStyle = "<style type='text/css'>.active, A.active:hover {background:WindowText;color:Window;} </style>";
 
document.write(extraStyle);
 
function refresh() 
{ 
	window.location.replace("search_results.jsp?<%=request.getQueryString()%>");
}
</script>


</head>

<body >

<%
if(request.getParameter("searchWord")!=null || request.getParameter("searchWordJS13")!=null)
{
	// Load the results
	ContentUtil content = new ContentUtil(application, request);
	String sQuery=request.getQueryString();
	sQuery=UrlUtil.changeParameterEncoding(sQuery, "searchWordJS13", "searchWord");
	sQuery=UrlUtil.changeParameterEncoding(sQuery, "scopeJS13", "scope");
	Element resultsElement = content.loadSearchResults(sQuery);
	if (resultsElement == null){
		out.write(WebappResources.getString("Nothing_found", request));
		return;
	}
	
	// If we did not receive a <toc> then display a progress monitor
	if (!resultsElement.getTagName().equals("toc"))
	{
		String percentage = resultsElement.getAttribute("indexed");
%>

<CENTER>
<TABLE BORDER='0'>
	<TR><TD><%=WebappResources.getString("Indexing", request)%></TD></TR>
	<TR><TD ALIGN='LEFT'>
		<DIV STYLE='width:100px;height:16px;border:1px solid WindowText;'>
			<DIV ID='divProgress' STYLE='width:<%=percentage%>px;height:100%;background-color:Highlight'></DIV>
		</DIV>
	</TD></TR>
	<TR><TD><%=percentage%>% <%=WebappResources.getString("complete", request)%></TD></TR>
	<TR><TD><br><%=WebappResources.getString("IndexingPleaseWait", request)%></TD></TR>
</TABLE>
</CENTER>
<script language='JavaScript'>
setTimeout('refresh()', 2000);
</script>
</body>
</html>

<%
		return;
	}
		
	// Generate results list
	NodeList topics = resultsElement.getElementsByTagName("topic");
	if (topics == null || topics.getLength() == 0){
		out.write(WebappResources.getString("Nothing_found", request));
		return;
	}
%>

<table id='list'  cellspacing='0' >

<%
	for (int i = 0; i < topics.getLength(); i++) 
	{
		Element topic = (Element)topics.item(i);
		// obtain document score
		String scoreString = topic.getAttribute("score");
		try {
			float score = Float.parseFloat(scoreString);
			NumberFormat percentFormat =
				NumberFormat.getPercentInstance(request.getLocale());
			scoreString = percentFormat.format(score);
		} catch (NumberFormatException nfe) {
			// will display original score string
		}

		String tocLabel = topic.getAttribute("toclabel");
		String label = topic.getAttribute("label");
		String href = topic.getAttribute("href");
		if (href != null && href.length() > 0) {
			// external href
			if (href.charAt(0) == '/')
				href = "content/help:" + href;
			if (href.indexOf('?') == -1)
				href +="?toc="+URLEncoder.encode(topic.getAttribute("toc"));
			else
				href += "&toc="+URLEncoder.encode(topic.getAttribute("toc"));			

		} else
			href = "about:blank";
%>

<tr class='list' id='r<%=i%>'>
	<td class='score' align='right'><%=scoreString%></td>
	<td align='left' class='label' nowrap>
		<a id='a<%=i%>' href='<%=href%>' onclick='parent.parent.setToolbarTitle("<%=UrlUtil.JavaScriptEncode(tocLabel)%>")' title="<%=UrlUtil.htmlEncode(label)%>"><%=UrlUtil.htmlEncode(label)%></a>
	</td>
</tr>

<%
	}
%>

</table>

<%
}else{
	out.write(WebappResources.getString("doSearch", request));
}

// Highlight topic
String topic = request.getParameter("topic");
if (topic != null && topic.startsWith("/"))
	topic = request.getContextPath() + "/content/help:" + topic;
else if (topic != null && topic.startsWith("file:/"))
	topic = request.getContextPath() + "/content/help:/" + topic;
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

