
package test;

import java.io.File;
import java.security.Key;
import java.sql.Connection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.interceptor.ServletRequestAware;
import org.apache.struts2.interceptor.ServletResponseAware;

import com.opensymphony.xwork2.ActionSupport;

public class test3 extends ActionSupport implements ServletRequestAware, ServletResponseAware{
    public String execute() throws Exception {
        return SUCCESS;
    }
    public String list() throws Exception {
        return "list";
    }
    public String user() throws Exception {
        return "user";
    }

    boolean isAuthError(){
    	String id=(String)request.getSession().getAttribute("userid");
    	if(!id.equals("test")){
    		request.getSession().setAttribute("userMessage", "you can not proceed this method!");
    		return true;
    	}
    	return false;
    }


    //（１）パスワードの変更
    public String changePassword() throws Exception {
    	String id=(String)request.getSession().getAttribute("userid");
    	String oldPassword=request.getParameterValues("oldPassword")[0];
    	String newPassword1=request.getParameterValues("newPassword1")[0];
    	String newPassword2=request.getParameterValues("newPassword2")[0];
    	if(!newPassword1.equals(newPassword2)){
    		request.getSession().setAttribute("userMessage", "new passwords are not matched");
    	}else{
        	boolean result=test4.changePassword(id, oldPassword, newPassword1);
        	if(result){
        		//セッションの認証情報を更新
        		Connection con = test.getConnection();
        		Key keyOfprivateKey=test.getKeyOfprivateKey(con,id,newPassword1);
        		con.close();
        		request.getSession().setAttribute("keyOfprivateKey", keyOfprivateKey);
        		//成功
        		request.getSession().setAttribute("userMessage", "changed successfully");
        	}else{
        		//失敗
        		request.getSession().setAttribute("userMessage", "old password is not correct");
        	}
    	}
    	return "user";
    }

    //（２）IDの削除
    public String deleteUser() throws Exception {
    	if(isAuthError()){
            return "user";
    	}
    	String id=request.getParameterValues("deleteid")[0];
    	id=java.net.URLDecoder.decode(id,"UTF-8");
    	test4.deleteId(id);
        return "user";
    }

  //（３）IDの追加
      public String addUser() throws Exception {
    	if(isAuthError()){
            return "user";
    	}
      	String id=request.getParameterValues("addid")[0];
      	String password=request.getParameterValues("password")[0];
      	String[] all=test4.getAllId();
      	for(int i=0;i<all.length;i++){
      		if(all[i].equals(id)){
      			//すでに登録済
      			return "user";
      		}
      	}
      	test4.addId(id, password);
          return "user";
      }


  //（４）権限付与・削除
    public String addIdToDir() throws Exception {
    	if(isAuthError()){
            return "user";
    	}
    	String masterId=(String)request.getSession().getAttribute("userid");
    	String massterPassword=request.getParameterValues("massterPassword")[0];
    	String id=request.getParameterValues("id")[0];
    	String dir=request.getParameterValues("dir")[0];
    	id=java.net.URLDecoder.decode(id,"UTF-8");
    	dir=java.net.URLDecoder.decode(dir,"UTF-8");
    	String[][] dirs=test.getDirs(id);
    	for(int i=0;i<dirs.length;i++){
    		if(dirs[i][0].equals(dir)){
    			//既に追加済み
    			return "user";
    		}
    	}
    	test6.addIdToDir(masterId, massterPassword, id, dir);
        return "user";
    }

//（４）権限付与・削除
	public String removeIdFromDir() throws Exception {
    	if(isAuthError()){
            return "user";
    	}
		String id=request.getParameterValues("id")[0];
		String dir=request.getParameterValues("dir")[0];
    	id=java.net.URLDecoder.decode(id,"UTF-8");
    	dir=java.net.URLDecoder.decode(dir,"UTF-8");
		test6.removeIdFromDir(id, dir);
	    return "user";
	}

    //（６）コメントの変更
    public String changeComment() throws Exception {
    	if(isAuthError()){
            return "user";
    	}
    	String id=(String)request.getSession().getAttribute("userid");
    	String password=request.getParameterValues("password")[0];
    	String oldComment=request.getParameterValues("oldComment")[0];
    	String newComment1=request.getParameterValues("newComment1")[0];
    	String newComment2=request.getParameterValues("newComment2")[0];
    	if(!newComment1.equals(newComment2)){
    		request.getSession().setAttribute("userMessage", "new comments are not matched");
        	return "user";
    	}
    	boolean result=test5.changeComment(id, password, oldComment, newComment1);
    	if(!result){
    		request.getSession().setAttribute("userMessage", "failed to change the comment");
    	}else{
    		request.getSession().setAttribute("userMessage", "changed successfully");
    	}
    	return "user";
    }

    //（７）画像のアップロード
    public String insertImages() throws Exception {
    	if(isAuthError()){
            return "user";
    	}
		File dir = new File(request.getParameterValues("imageDir")[0]);
//    	String massterPassword=request.getParameterValues("massterPassword")[0];
		Connection con = test.getConnection();
		Key keyOfprivateKey=(Key)request.getSession().getAttribute("keyOfprivateKey");
		String message = test2.insertImage(con, dir, keyOfprivateKey);
		request.getSession().setAttribute("userMessage", message);
		con.close();
		return "user";
    }
    //（８）
    public String updateDescription() throws Exception {
    	if(isAuthError()){
            return "user";
    	}
		Connection con = test.getConnection();
		String dir = request.getParameterValues("dir")[0];
    	dir=java.net.URLDecoder.decode(dir,"UTF-8");
		String description = request.getParameterValues("description")[0];
		String message = test2.updateDescription(con, dir, description);
		request.getSession().setAttribute("userMessage", message);
		con.close();
		return "user";
    }
    //（９）
    public String changeType() throws Exception {
    	if(isAuthError()){
            return "user";
    	}
		String type = request.getParameterValues("type")[0];
		type=java.net.URLDecoder.decode(type,"UTF-8");
		request.getSession().setAttribute("type", type);
		String message = test2.changeType(type);
		request.getSession().setAttribute("userMessage", message);
		return "user";
    }

    HttpServletResponse response;
	public void setServletResponse(HttpServletResponse response) {
		this.response = response;
	}
	HttpServletRequest request;
	public void setServletRequest(HttpServletRequest request) {
		this.request = request;
	}

	public String logout() throws Exception {
		test.clearSession(request.getSession());
		return "login";
	}
}