<%--
 Copyright (c) 2000, 2010 IBM Corporation and others.

 This program and the accompanying materials 
 are made available under the terms of the Eclipse Public License 2.0
 which accompanies this distribution, and is available at
 https://www.eclipse.org/legal/epl-2.0/

 SPDX-License-Identifier: EPL-2.0
 
 Contributors:
     IBM Corporation - initial API and implementation
--%>
<%@ include file="header.jsp"%>
<% 
	ToolbarData data = new ToolbarData(application,request, response);
	WebappPreferences prefs = data.getPrefs();
%>


<html lang="<%=ServletResources.getString("locale", request)%>">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">

<title><%=ServletResources.getString("Toolbar", request)%></title>
</head>
 
<body dir="<%=direction%>" bgcolor="<%=prefs.getBasicToolbarBackground()%>">
<%
	String title=data.getTitle();
	// search view is not called "advanced view"
	if("search".equals(request.getParameter("view"))){
		title=ServletResources.getString("SearchLabel", request);
	}
%>
	<b>
	<%=title%>
	</b>

</body>     
</html>

