package test;
import java.security.Key;
import java.sql.Connection;
import java.util.Map;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;
public class TestInterceptor extends AbstractInterceptor{
    @Override
    public String intercept(ActionInvocation arg0) throws Exception {
        Connection con=null;
        try{
            Map<String, Object> session = arg0.getInvocationContext().getSession();
            Map<String, Object> params = arg0.getInvocationContext().getParameters();

            con=test.getConnection();

            //テーブル存在チェック
            if(!test.initChecked){
            	if(test.checkTable(con)){
            		test.initChecked=true;
            	}else if(params.get("init_password")!=null){
            		test.createInitTableAndData(con, ((String[])params.get("init_password"))[0]);
            		test.initChecked=true;
            	}else{
            		return "init";
            	}
            }

            //コメント
            if(params.get("comment")!=null){

            	test.checkComment(con,((String[])params.get("comment"))[0],true);

            }
            if(test.comment==null){
            	con.close();
            	return "comment";
            }

            //タイムアウトチェック
            long time = 0;
            if(session.get("time")!=null){
            	time = (Long)session.get("time");
            	if(time+test.TIMEOUT_LOGIN<System.currentTimeMillis()){
                	test.clearSession(session);
            	}else{
            		//アクセス時間
            		session.put("time", System.currentTimeMillis());
            	}
            }
            String userid = (String)session.get("userid");
            //ログイン済みか
            if(userid==null){
                String[] userids = (String[])params.get("userid");
                String[] passwords = (String[])params.get("password");
                //初回アクセス時
                if(userids==null || userids==null){
                	con.close();
                    return "login";
                }
                //小文字化
                userids[0] = userids[0].toLowerCase();
                //ユーザー・パスワードの妥当性チェック
                Key keyOfprivateKey=test.getKeyOfprivateKey(con,userids[0],passwords[0]);
                if(keyOfprivateKey==null){
                	test.clearSession(session);
        			try {
        				Thread.sleep(1000L);
        			} catch (InterruptedException e) {
        				e.printStackTrace();
        			}
                	con.close();
                    return "login";
                }
//                String[] pages = test.getDirs(con,userids[0]);
                session.put("userid", userids[0]);
//                session.put("pages", pages);
                session.put("keyOfprivateKey", keyOfprivateKey);
            }
            //アクセス時間
            session.put("time", System.currentTimeMillis());
            con.close();
            return arg0.invoke();
           }catch(Exception e){
               e.printStackTrace();
               if(con!=null){
            	   con.close();
               }
               return "error";
           }
   }




}