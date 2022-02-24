<%@page import="test.test4"%>
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
<%
String message=(String)session.getAttribute("userMessage");
if(message!=null){
	session.removeAttribute("userMessage");
	out.println("<font color=red>"+message+"</font>");
	out.println("<br><br>");
}
%>

（１）パスワードの変更
		<s:form method="post">
     OLD Password: <input type=password name=oldPassword><br>
     NEW Password: <input type=password name=newPassword1><br>
     NEW Password: <input type=password name=newPassword2><br>
			<s:submit value="パスワードを変更" method="changePassword" />
		</s:form>

<s:if test="%{#session.userid == 'test'}">

（２）IDの削除<br>
		<s:form method="post">
			<select name="deleteid">
			<option selected></option>
				<%
				String[] ids=test4.getAllId();
				for(int i=0;i<ids.length;i++){
					if(ids[i].equals("test")){
						continue;
					}
					out.println("<option value=\""+java.net.URLEncoder.encode(ids[i],"UTF-8")+"\">"+ids[i]+"</option>");
				}
				%>
			</select>
		<s:submit value="ユーザー削除" method="deleteUser" />
	</s:form>


		<s:form method="post">
（３）IDの追加<br>
     UserID: <input type=text name=addid><br>
     Password: <input type=password name=password><br>
		<s:submit value="ユーザー登録" method="addUser" />

	</s:form>


（４）権限付与・削除<br>
		<s:form method="post">

			<select name="id">
			<option selected></option>
				<%
				String[] ids=test4.getAllId();
				for(int i=0;i<ids.length;i++){
					if(ids[i].equals("test")){
						continue;
					}

					if(ids[i].equals((String)session.getAttribute("userid"))){
						continue;
					}
					out.println("<option value=\""+java.net.URLEncoder.encode(ids[i],"UTF-8")+"\">"+ids[i]+"</option>");
				}
				%>
			</select>

			<select name="dir">
			<option selected></option>
				<%
				String[][] dirs=test.getDirs((String)session.getAttribute("userid"));
				for(int i=0;i<dirs.length;i++){
					out.println("<option value=\""+java.net.URLEncoder.encode(dirs[i][0],"UTF-8")+"\">"+dirs[i][0]+"</option>");
				}
				%>
			</select>

		<s:submit value="権限削除" method="removeIdFromDir" />

    <br>Your Password(権限付与のみ必要):<br><input type=password name=massterPassword><br>
		<s:submit value="権限付与" method="addIdToDir" />
	</s:form>

（５）ID・権限一覧<br>
<%
String[] ids=test4.getAllId();
for(int i=0;i<ids.length;i++){
	String[][] dirs=test.getDirs(ids[i]);
	out.println(ids[i]+"<br>");
	for(int s=0;s<dirs.length;s++){
		out.println("　"+dirs[s][0]+"<br>");
	}
}
%>

<br>（６）コメントの変更<br>
<s:form method="post">
     your password <input type=password name=password><br>
     old comment <input type=password name=oldComment><br>
     new comment <input type=password name=newComment1><br>
     new comment <input type=password name=newComment2><br>
<s:submit value="コメント変更" method="changeComment" />
</s:form>


<br>（７）画像のアップロード<br>※コメントの有効期限内に終わるようにしてください<br>
<!-- ローカルＨＤＤからアップロードするので現状は管理者ユーザーのみ -->
<s:form method="post">
     image dir <input type=text name=imageDir value="C:\Users\Administrator\Desktop\TEST_PHOTO"><br>
	<s:submit value="画像のアップロード" method="insertImages" />
</s:form>
（８）概要の修正<br>
		<s:form method="post">
			<select name="dir">
			<option selected></option>
				<%
				String[][] values=test.getDirs((String)session.getAttribute("userid"));
				for(int i=0;i<values.length;i++){
					String desc = "";
					if(values[i][1]!=null&&values[i][1].length()!=0){
						desc="("+values[i][1]+")";
					}
					//out.println("<option value=\""+java.net.URLEncoder.encode(dirs[i][0],"UTF-8")+"\">"+dirs[i][0]+description+"</option>");
		            String dir = (String)session.getAttribute("dir");
					if(values[i][0].equals(dir)){
		    	        out.println("<option value=\""+java.net.URLEncoder.encode(values[i][0],"UTF-8")+"\" selected>"+values[i][0]+desc+"</option>");
		            }else{
		    	        out.println("<option value=\""+java.net.URLEncoder.encode(values[i][0],"UTF-8")+"\">"+values[i][0]+desc+"</option>");
		            }
				}
				%>
			</select>
		<br><input type=text name=description>
		<s:submit value="修正" method="updateDescription" />
	</s:form>


	<br>（９）タイプの変更<br>
	<s:form method="post">
		<select name="type">
			<option selected></option>
				<%
				String[][] values= new String[][]{{"0","0"},{"1","1"}};
				for(int i=0;i<values.length;i++){
					String desc = "";
					if(values[i][1]!=null&&values[i][1].length()!=0){
						desc="("+values[i][1]+")";
					}
		            String type = (String)session.getAttribute("type");
					if(values[i][0].equals(type)){
		    	        out.println("<option value=\""+java.net.URLEncoder.encode(values[i][0],"UTF-8")+"\" selected>"+values[i][0]+desc+"</option>");
		            }else{
		    	        out.println("<option value=\""+java.net.URLEncoder.encode(values[i][0],"UTF-8")+"\">"+values[i][0]+desc+"</option>");
		            }
				}
				%>
			</select>
		<s:submit value="タイプ変更" method="changeType" />
	</s:form>
</s:if>

<s:form method="post">
    <s:submit value="戻る" />
		<s:submit value="ログアウト" method="logout" />
	</s:form>
  </body>
</html>