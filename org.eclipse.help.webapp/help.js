/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
 
var isMozilla = navigator.userAgent.indexOf('Mozilla') != -1 && parseInt(navigator.appVersion.substring(0,1)) >= 5;
var isMozilla10 = isMozilla && navigator.userAgent.indexOf('rv:1') != -1;
var isIE = navigator.userAgent.indexOf('MSIE') != -1;

var framesLoaded = false;
var args = parseQueryString();

var tocTitle = null;
var currentNavFrame;
var lastTab = "";

var temp;
var tempActive;
var tempTab = "";

/**
 * Notification when frames are loaded
 */
function onloadFrameset()
{
	framesLoaded = true;

	// show the appropriate tab
	var tab = "toc";
	if (args && args["tab"])
	    tab = args["tab"];
	switchTab(tab);
	
}

    
/**
 * Parses the parameters passed to the url
 */
function parseQueryString (str) 
{
    str = str ? str : window.location.href;
    var longquery = str.split("?");
    if (longquery.length <= 1) return "";
    var query = longquery[1];
    var args = new Object();
    if (query) 
    {
        var fields = query.split('&');
        for (var f = 0; f < fields.length; f++) 
        {
            var field = fields[f].split('=');
            args[unescape(field[0].replace(/\+/g, ' '))] = unescape(field[1].replace(/\+/g, ' '));
        }
    }
    return args;
}

/**
 * Needs to be called from a subframe
 */
function setToolbarTitle(title)
{
	if(ToolbarFrame){
		ToolbarFrame.setTitle(title);
	}
}

/* 
 * Switch tabs.
 */ 
function switchTab(nav, newTitle)
{ 	
	if (nav == lastTab) 
		return;
		
	lastTab = nav;
	
	// set the title on the navigation toolbar to match the tab
  	if (newTitle)
     	NavToolbarFrame.document.getElementById("titleText").innerHTML = newTitle;
    else
    	NavToolbarFrame.document.getElementById("titleText").innerHTML = titleArray[nav];
       	
	// show appropriate frame
	this.currentNavFrame=nav;
 	var iframes = NavFrame.document.body.getElementsByTagName("IFRAME");
 	for (var i=0; i<iframes.length; i++)
 	{			
  		if (iframes[i].id != nav)
   			iframes[i].className = "hidden";
  		else
   			iframes[i].className = "visible";
 	}
 
 	// show the appropriate pressed tab
  	var buttons = TabsFrame.document.body.getElementsByTagName("TD");
  	for (var i=0; i<buttons.length; i++)
  	{
  		if (buttons[i].id == (nav + "Tab")) // Note: assumes the same id shared by tabs and iframes
			buttons[i].className = "pressed";
		else if (buttons[i].className == "pressed")
			buttons[i].className = "tab";
 	 }
}
 
 
/**
 * Shows the TOC frame, loads appropriate TOC, and selects the topic
 */
function displayTocFor(topic)
{
	tempTab = lastTab;
	switchTab("toc");
	
	// remove the query, if any
	var i = topic.indexOf('?');
	if (i != -1)
		topic = topic.substring(0, i);

	var selected = false;
	if (NavFrame.toc.selectTopic)
		selected = NavFrame.toc.selectTopic(topic);

	if (!selected) {
		// save the current navigation, so we can retrieve it when synch does not work
		saveNavigation();
		// we are using the full URL because this API is exposed to clients
		// (content page may want to autosynchronize)
		var tocURL = window.location.protocol + "//" +window.location.host  +contextPath + "/contents.jsp";
		NavFrame.toc.location.replace(tocURL + "?topic="+topic+"&synch=yes");			
	}
}

function saveNavigation()
{
	if (NavFrame.toc.location.href.indexOf("tocs.jsp") == -1) {
					
		if (NavFrame.toc.oldActive)
			tempActive = NavFrame.toc.oldActive;
		// on mozilla, we will not preserve selection, the object is no longer valid.
		// in the future, we could look up the topic, but this should suffice for now
		// Note: need newer mozilla version
		if (isMozilla){
			tempActive.className ="";
			tempActive=null;
		}
			
		if (isIE)
			temp = NavFrame.toc.document.body.innerHTML;
		else if (isMozilla)
			temp = NavFrame.document.getElementById("toc").contentDocument.documentElement.innerHTML;
	} else {
		temp = null;
	}
}

function restoreNavigation()
{	
	// turn to the right tab
	var oldTab = tempTab;
	
	switchTab(tempTab);
	
	if (temp && (isIE || isMozilla10)){
		// Restore old navigation
		if (isIE)
			NavFrame.toc.document.body.innerHTML = temp;
		else if (isMozilla10)
			NavFrame.document.getElementById("toc").contentDocument.documentElement.innerHTML = temp;
		
		if (tempActive)
			NavFrame.oldActive = tempActive;
	}else {
		// Show bookshelf
		NavFrame.toc.location.replace("contents.jsp");
	}
}


function doSearch(query)
{
	switchTab("search");
	if (!query || query == "") return;
	if (isIE)
		NavFrame.document.search.location.replace("search_results.jsp?"+query);
	else if (isMozilla)
		NavFrame.document.getElementById("search").src = "search_results.jsp?"+query; 
}
