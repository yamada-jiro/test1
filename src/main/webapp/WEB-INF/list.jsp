<%@ page language="java" contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<html>
<meta name="viewport" content="width=660">

<%
String dir = request.getParameter("a");
String type = null;
if(session.getAttribute("userid").equals("test")){
	// it is available to show the pictures except type 0 by specifing url param 't'
	type = request.getParameter("t");
}
if(type==null || type.length() == 0){
	type = "0";
}
if(dir!=null && test.test.getImageList(java.net.URLDecoder.decode(dir,"UTF-8")
		,java.net.URLDecoder.decode(type,"UTF-8")).length()!=0){
  session.setAttribute("dir", java.net.URLDecoder.decode(dir,"UTF-8"));
%>
<head>
<script type="text/javascript">
var list=new Array(
		<%=test.test.getImageList(java.net.URLDecoder.decode(request.getParameter("a"),"UTF-8")
				,java.net.URLDecoder.decode(type,"UTF-8")) %>
);
var idx=0;
var num=1;
var c="?c=160";
var INIT=list.length;
var originalNum=INIT;
var originalIdx=0;
<s:if test="%{#session.userid == 'test' && #session.type != null && #session.type != ''}">
function updateType(i){
	var name = list[i] + c;
	if(c=="?c=160"){
		name = name +"&ut=x";
	}else{
		name = name +"?ut=x";
	}
	document.getElementById("img"+i).src = name;
}
</s:if>
function changeSize(i){
	<s:if test="%{#session.userid == 'test' && #session.type != null && #session.type != ''}">
	updateType(i);
	return;
	</s:if>
	//size change
	//make num 1 or back to original
	if(c=="?c=160"){
		originalIdx = idx;
		idx=i;
		c="";
		originalNum=num;
		num=1;
		document.getElementById("sizeChange").value='縮小';
	}else{
		idx = originalIdx;
		c="?c=160";
		num=originalNum;
		document.getElementById("sizeChange").value='拡大';
	}
	document.getElementById("imageNum").selectedIndex=num-1;
}
function init(){
	var show = "";
	for(i=0;i<list.length*2;i++){
//    	show=show+"<img src='"+list[i]+c+"' onclick='javascript:changeSize("+(idx+i)%list.length+");view();' id=img"+i+">";
    	show=show+"<img src='"+list[i%list.length]+c+"' onclick='javascript:changeSize("+(idx+i)+");view();' id=img"+i+">";
    }
    document.getElementById("b").innerHTML=show;
}
function view(){
    while(idx<0){
    	idx+=list.length;
    }
    idx%=list.length;
    var show=(idx+1)+"/"+list.length+"<br>";
	for(i=0;i<list.length*2;i++){
    	document.getElementById("img"+i).width=0;
    	document.getElementById("img"+i).height=0;
    }

	if(c=="?c=160"){
    	for(i=0;i<num;i++){
    		//document.getElementById("img"+(idx+i)%list.length).removeAttribute('width');
    		//document.getElementById("img"+(idx+i)%list.length).removeAttribute('height');
    		document.getElementById("img"+(idx+i)).removeAttribute('width');
    		document.getElementById("img"+(idx+i)).removeAttribute('height');
    	}
	}else{
    	//show=show+"<img src='"+list[(idx+i)%list.length]+c+"' onclick='javascript:changeSize("+(idx+i)%list.length+");view();'>";
    	for(i=0;i<num;i++){
	    	show=show+"<img src='"+list[(idx+i)%list.length]+c+"' onclick='javascript:changeSize("+(idx+i)%list.length+");view();move("+(idx+i)%list.length+");'>";
    	}
	}
	document.getElementById("a").innerHTML=show;
}
function setNum(selectobj){
	 num=parseInt(selectobj[selectobj.selectedIndex].value);
	 //originalNum=num;
}
function move(i){
	window.location.hash='img'+i;
}
</script>
</head><body onLoad='init();view();'>
同時表示枚数<select id=imageNum onChange="setNum(this);view();">
<script type="text/javascript">
//for(i=1;i<=100&&i<=list.length;i++){
for(i=1;i<=list.length;i++){
 if(list.length>=INIT && i==INIT){
	document.write('<option value='+i+' selected>'+i+'</option>');
	num=INIT;
 }else if(list.length<INIT && i==list.length){
	document.write('<option value='+i+' selected>'+i+'</option>');
	num=list.length;
 }else{
	 document.write('<option value='+i+'>'+i+'</option>');
 }
}
</script>
</select>
<input type="button" value="NEXT" onclick="javascript:idx+=num;view();">
<input type="button" value="BACK" onclick="javascript:idx-=num;view();">
<input type="button" value="拡大" onclick="javascript:if(c==''){c='?c=160';this.value='拡大';}else{c='';this.value='縮小';};view();" id=sizeChange>
<input type="button" value="戻る" onclick="javascript:document.getElementById('back2').click();">
<input type="button" value="ログアウト" onclick="javascript:document.getElementById('logout2').click();">
<label id=a></label>
<label id=b></label>
    <s:form method="post">
    <s:submit value="戻る" id="back2" />
    </s:form>
    <s:form method="post">
    <s:submit value="ログアウト" method="logout" id="logout2" />
    </s:form>
</body>
<%}%>
</html>