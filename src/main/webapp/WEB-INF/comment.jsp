<%@ page language="java" contentType="text/html; charset=UTF-8" %>
<html>
<meta name="viewport" content="width=320">
  <head>
  <title>JSP Test2</title>
  </head>
  <body>
    <h1></h1>
    <%= new java.text.SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new java.util.Date()) %>
    <form method=post>
    enter your comment  <input type=password name=comment><br>
    <input type="submit">
    </form>
  </body>
</html>