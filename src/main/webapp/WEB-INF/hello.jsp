<%@page import="test.test"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<html>
<meta name="viewport" content="width=320">
  <head>
  <title>JSP Test</title>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
  </head>
  <body>
    <h1></h1>
    <br>Hello <%=session.getAttribute("userid")%> !!!
    <%= new java.text.SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new java.util.Date()) %>
    <s:form method="get">

		<select name="a">
	    <%
//	    String[] values = (String[])session.getAttribute("pages");
	    String[][] values = test.getDirs((String)session.getAttribute("userid"));
	    String dir = (String)session.getAttribute("dir");
	    for(int i=0;i<values.length;i++){
	    	String desc = "";
	    	if(values[i][1]!=null&&values[i][1].length()!=0){
	    		desc = "("+values[i][1]+")";
	    	}
            if(values[i][0].equals(dir)){
    	        out.println("<option value=\""+java.net.URLEncoder.encode(values[i][0],"UTF-8")+"\" selected>"+values[i][0]+desc+"</option>");
            }else{
    	        out.println("<option value=\""+java.net.URLEncoder.encode(values[i][0],"UTF-8")+"\">"+values[i][0]+desc+"</option>");
            }
	    }
	    %>
	    </select>
		<s:submit method="list" value="表示"/>

	</s:form>
    <s:form method="post">
		<s:submit method="user" value="設定"/>
		<s:submit method="logout" value="ログアウト" />
	</s:form>
  </body>
</html>