<%--
 (c) Copyright IBM Corp. 2000, 2002.
 All Rights Reserved.
--%>
<%@ include file="header.jsp"%>

<% 
	LayoutData data = new LayoutData(application,request);
	WebappPreferences prefs = data.getPrefs();
%>

<html>

<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title><%=WebappResources.getString("Help", request)%></title>
<script language="JavaScript">
var isMozilla = navigator.userAgent.indexOf('Mozilla') != -1 && parseInt(navigator.appVersion.substring(0,1)) >= 5;
var isMozilla10 = isMozilla && navigator.userAgent.indexOf('rv:1') != -1;
var isIE = navigator.userAgent.indexOf('MSIE') != -1;

/**
 * Views can call this to set the title on the content toolbar
 */
function setContentToolbarTitle(title)
{
	if(parent.ContentFrame.ToolbarFrame && parent.ContentFrame.ToolbarFrame.setTitle ){
		parent.ContentFrame.ToolbarFrame.setTitle(title);
	}
}

/**
 * Views can call this to set the title on the navigation toolbar
 */
function setNavToolbarTitle(title)
{
	if(ToolbarFrame && ToolbarFrame.setTitle ){
		ToolbarFrame.setTitle(title);
	}
}

/**
 * Shows specified view. Called from actions that switch the view */
function showView(view)
{
	// Note: assumes the same id shared by tabs and iframes
	ViewsFrame.showView(view);
	TabsFrame.showTab(view);
}

var temp;
var tempActiveId;
var tempView = "";

/**
 * Shows the TOC frame, loads appropriate TOC, and selects the topic
 */
function displayTocFor(topic)
{
	tempView = ViewsFrame.lastView;
	
	/******** HARD CODED VIEW NAME *********/
	showView("toc");
	
	var tocView = ViewsFrame.toc.ViewFrame;

	if (tocView.selectTopic && tocView.selectTopic(topic))
		return;
	else {
		// save the current navigation, so we can retrieve it when synch does not work
		saveNavigation();
		// we are using the full URL because this API is exposed to clients
		// (content page may want to autosynchronize)
		var tocURL = window.location.protocol + "//" +window.location.host  + "<%=request.getContextPath()%>" + "/advanced/tocView.jsp";
		tocView.location.replace(tocURL + "?topic="+topic+"&synch=yes");			
	}
}

function saveNavigation()
{
	/**** HARD CODED VIEW NAME *********/
	var tocView = ViewsFrame.toc.ViewFrame;
	
	if (tocView.oldActive) {
		tempActiveId = tocView.oldActive.id;
		tocView.oldActive.className = tocView.oldActiveClass;
		tocView.oldActive = null;
	}
		
	if (isIE)
		temp = tocView.document.body.innerHTML;
	else if (isMozilla)
		temp = tocView.document.documentElement.innerHTML;
}

function restoreNavigation()
{	
	// switch to saved view
	showView(tempView);

	/**** HARD CODED VIEW NAME *********/	
	var tocView = ViewsFrame.toc.ViewFrame;

	if (temp && (isIE || isMozilla10)){
		// Restore old navigation
		if (isIE)
			tocView.document.body.innerHTML = temp;
		else if (isMozilla10)
			tocView.document.getElementById("toc").contentDocument.documentElement.innerHTML = temp;
		
		if (tempActiveId)
			tocView.selectTopicById(tempActiveId);
	}else {
		// fail back case
		tocView.location.replace("tocView.jsp");
	}
}

</script>
</head>

<frameset onload="showView('<%=data.getVisibleView()%>')" id="navFrameset" rows="*,24"  framespacing="0" border="0"  frameborder="0" spacing="0"  scrolling="no">
   <frame name="ViewsFrame" src='<%="views.jsp"+data.getQuery()%>' marginwidth="0" marginheight="0" scrolling="no" frameborder="0" resize=yes>
   <frame name="TabsFrame" src='<%="tabs.jsp"+data.getQuery()%>' marginwidth="0" marginheight="0" scrolling="no" frameborder="0" noresize>
</frameset>

</html>