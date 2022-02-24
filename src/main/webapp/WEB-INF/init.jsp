<%@ page language="java" contentType="text/html; charset=UTF-8" %>
<html>
<meta name="viewport" content="width=320">
  <head>
  <title>JSP Test</title>
  </head>
  <body>
    <h1>init</h1>
    <%= new java.text.SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new java.util.Date()) %>
    <form method=post>
     init password: <input type=password name=init_password><br>
    <input type="submit">
    </form>
  </body>
</html>