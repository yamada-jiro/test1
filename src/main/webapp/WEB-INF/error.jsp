<%@ page language="java" contentType="text/html; charset=UTF-8" %>
<html>
<meta name="viewport" content="width=320">
  <head>
  <title>JSP Test</title>
  <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
  </head>
  <body>
    <h1></h1>
    <h1>ERROR</h1>
    <%= new java.text.SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new java.util.Date()) %>
  </body>
</html>
