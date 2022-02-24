package test;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.AreaAveragingScaleFilter;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;
import java.util.Vector;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.dbcp.BasicDataSource;
public class test extends HttpServlet{
	final public static long TIMEOUT_LOGIN=1000*60*10;
	final public static long TIMEOUT_COMMENT=1000*60*60*24*7;
	public static byte[] comment=null;
	public static boolean initChecked = false;
  public void doGet(HttpServletRequest request,
                    HttpServletResponse response)
    throws ServletException, IOException{
	Connection con = null;
	PreparedStatement stmt = null;
	ResultSet rs = null;
    try {

        //コメント
        if(test.comment==null){
        	response.setStatus(403);
        	return;
        }

    	long time = 0;
        if(request.getSession().getAttribute("time")!=null){
        	time = (Long)request.getSession().getAttribute("time");
        	//if(time+1000<System.currentTimeMillis()){
            if(time+TIMEOUT_LOGIN<System.currentTimeMillis()){
            	clearSession(request.getSession());
        	}else{
        		//アクセス時間
        		request.getSession().setAttribute("time", System.currentTimeMillis());
        	}
        }
    	String userid = (String)request.getSession().getAttribute("userid");
        //ログイン済みか
        if(userid==null){
        	response.setStatus(403);
        	return;
        }
        //権限があるか
//    	String[] pages = (String[])request.getSession().getAttribute("pages");
		con = getConnection();
    	String[][] pages = test.getDirs(con, userid);
    	String[] path = URLDecoder.decode(request.getRequestURI(),"UTF-8").split("/");
    	for(int i=0;i<pages.length;i++){
    		if(pages[i][0].equals(path[path.length-2])){
    			break;
    		}
    		if(i+1==pages.length){
    			response.setStatus(403);
    			con.close();
            	return;
    		}
    	}

    	//typeの更新
    	int typeUpdated = 0;
		if("test".equals(userid) && request.getParameter("ut")!=null
				&& request.getSession().getAttribute("type")!=null
				&& ((String)request.getSession().getAttribute("type")).length()!=0){
			stmt = con.prepareStatement("update test set type=? where dir=? and name=? and type != ?");
			stmt.setInt(1, Integer.valueOf((String)request.getSession().getAttribute("type")));
			stmt.setString(2, path[path.length-2]);
			stmt.setString(3, path[path.length-1]);
			stmt.setInt(4, Integer.valueOf((String)request.getSession().getAttribute("type")));
			typeUpdated = stmt.executeUpdate();
			stmt.close();
		}

    	String typeWhere = "";
    	if(!"test".equals(userid)){
    		typeWhere = " and type = 0 ";
    	}
		if("160".equals(request.getParameter("c"))){
			stmt = con.prepareStatement("select data from test where dir=? and name=? and width=160" + typeWhere);
		}else{
			stmt = con.prepareStatement("select data from test where dir=? and name=? and width=640" + typeWhere);
		}
		stmt.setString(1, path[path.length-2]);
		stmt.setString(2, path[path.length-1]);
		rs = stmt.executeQuery();
		byte[] b = null;
		String[] splited = path[path.length-1].split("[.]");
		String ext = splited[splited.length-1].toUpperCase();
		if(!rs.next()){
			if("160".equals(request.getParameter("c"))){
				rs.close();
				stmt.close();
				stmt = con.prepareStatement("select data, type from test where dir=? and name=? and width=640" + typeWhere);
				stmt.setString(1, path[path.length-2]);
				stmt.setString(2, path[path.length-1]);
				rs = stmt.executeQuery();
				if(!rs.next()){
					response.setStatus(404);
				}else{
					b=rs.getBytes(1);
					int type = rs.getInt(2);
					Key keyOfprivateKey=(Key)request.getSession().getAttribute("keyOfprivateKey");
					Key privateKey=getPrivateKey(con,(String)request.getSession().getAttribute("userid"), keyOfprivateKey);
					Key commonKey=getCommonKey(
							con,
							(String)request.getSession().getAttribute("userid"),
							(String)request.getSession().getAttribute("dir"),
							privateKey
						);

					b=test.decodeAES(b, commonKey);//common
					b=decodeAES(b,getCommentKey());//comment
					b=changeImageSize(b, 160, 160, false, ext);

					response.getOutputStream().write(b);

					rs.close();
					stmt.close();
					String sql = "insert into test values(?,?,?,?,?)";
					stmt = con.prepareStatement(sql);
					stmt.setString(1, path[path.length-2]);
					stmt.setString(2, path[path.length-1]);

					b = test.encodeAES(b,test.getCommentKey());//comment
					b = test.encodeAES(b,commonKey);//common

					stmt.setBytes(3, b);
					stmt.setInt(4, 160);
					stmt.setInt(5, type);
					stmt.execute();

				}
			}else{
				response.setStatus(404);
			}
		}else{
			b=rs.getBytes(1);
			Key keyOfprivateKey=(Key)request.getSession().getAttribute("keyOfprivateKey");
			Key privateKey=getPrivateKey(con,(String)request.getSession().getAttribute("userid"), keyOfprivateKey);
			Key commonKey=getCommonKey(
					con,
					(String)request.getSession().getAttribute("userid"),
					(String)request.getSession().getAttribute("dir"),
					privateKey
				);
			b=test.decodeAES(b, commonKey);//common
			b=decodeAES(b,getCommentKey());//comment
			if(typeUpdated!=0){
				b = setMessage(b, ext,"UPDATED:"+typeUpdated);
			}
			response.getOutputStream().write(b);
		}
		response.setHeader("Expires", "-1");
		response.setHeader("Pragma","no-cache");
		response.setHeader("Cache-Control","no-cache");

	} catch (Throwable t) {
		t.printStackTrace();
		response.setStatus(500);
	}finally{
		if(rs!=null){
			try {
				rs.close();
			} catch (Throwable tt) {
				tt.printStackTrace();
			}
		}
		if(stmt!=null){
			try {
				stmt.close();
			} catch (Throwable tt) {
				tt.printStackTrace();
			}
		}
		if(con!=null){
			try {
				con.close();
			} catch (Throwable tt) {
				tt.printStackTrace();
			}
		}
	}
  }
//  public static String getImageList(String dir) throws ClassNotFoundException, SQLException{
  public static String getImageList(String dir,String type) throws ClassNotFoundException, SQLException{
	  Connection con = getConnection();
//		PreparedStatement stmt = con.prepareStatement("select name from test where dir=? and width=640 order by name");
		PreparedStatement stmt = con.prepareStatement("select name from test where dir=? and width=640 and type = ? order by name");
		stmt.setString(1, dir);
		stmt.setInt(2, Integer.valueOf(type));
		ResultSet rs = stmt.executeQuery();
		String list = "";
		StringBuffer sb = new StringBuffer();
//		while(rs.next()){
//			if(list.length()!=0){
//				list+=",";
//			}
//			list+="\"./image/"+dir+"/"+rs.getString(1)+"\"";
//		}
		while(rs.next()){
			if(sb.length()!=0){
				sb.append(",");
			}
			sb.append("\"./image/"+dir+"/"+rs.getString(1)+"\"");
		}
		list=sb.toString();
		rs.close();
		stmt.close();
		con.close();
		return list;
  }
  static void clearSession(Map<String, Object> session){
	session.remove("userid");
  	session.remove("time");
  	session.remove("dir");
  	session.remove("keyOfprivateKey");
  	session.remove("type");
  }
  static void clearSession( HttpSession session){
	session.removeAttribute("userid");
  	session.removeAttribute("time");
  	session.removeAttribute("dir");
  	session.removeAttribute("keyOfprivateKey");
  	session.removeAttribute("type");
  }




  static BasicDataSource basicDataSource = null;
  static Connection getConnection() throws SQLException{
	  if(basicDataSource==null){
		  basicDataSource = new BasicDataSource();
//		  basicDataSource.setDriverClassName("org.postgresql.Driver");
//		  basicDataSource.setUrl("jdbc:postgresql://localhost:5432/test");
//		  basicDataSource.setUsername("test");
//		  basicDataSource.setPassword("test");
		  basicDataSource.setDriverClassName("org.h2.Driver");
		  basicDataSource.setUrl("jdbc:h2:./test");
		  basicDataSource.setUsername("sa");
		  basicDataSource.setPassword("");
	}
	 Connection con=basicDataSource.getConnection();
//	con.setAutoCommit(false);
	  return con;
  }



	public static Key getCommentKey() {
		return new SecretKeySpec(comment, "AES");
	}

	public static Key createKey(byte[] key) {
		return new SecretKeySpec(key, "AES");
	}

	public static byte[] encodeAES(byte[] src, Key skey) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		Cipher cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.ENCRYPT_MODE, skey);
		return cipher.doFinal(src);
	}
	public static byte[] decodeAES(byte[] src, Key skey) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		Cipher cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.DECRYPT_MODE, skey);
		return cipher.doFinal(src);
	}

	public static Key getPublicKey(Connection con,String id) throws SQLException, IOException, ClassNotFoundException{
		String sql = "select public from test2 where id=?";
		PreparedStatement stmt = con.prepareStatement(sql);
		stmt.setString(1, id);
		ResultSet rs=stmt.executeQuery();
		Key key=null;
		if(rs.next()){
			byte[] b=rs.getBytes("public");
			key=BytesToKey(b);
		}
		rs.close();
		stmt.close();
		return key;
	}

	public static Key getCommonKey(Connection con,String id, String dir, Key privateKey) throws SQLException, IOException, ClassNotFoundException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException{
		String sql = "select common from test3 where id=? and dir=?";
		PreparedStatement stmt = con.prepareStatement(sql);
		stmt.setString(1, id);
		stmt.setString(2, dir);
		ResultSet rs=stmt.executeQuery();
		Key key=null;
		if(rs.next()){
			byte[] b=rs.getBytes("common");
			byte[] bb=decryptRSA(b, privateKey);
			key=BytesToKey(bb);
		}
		rs.close();
		stmt.close();
		return key;
	}



	public static Key getPrivateKey(Connection con,String id, Key keyOfPrivateKey) throws SQLException, IOException, ClassNotFoundException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException{
//		System.out.println("id:"+id+" dir:"+dir);
//		Connection con = test.getConnection();
		String sql = "select private from test2 where id=?";
		PreparedStatement stmt = con.prepareStatement(sql);
		stmt.setString(1, id);
		ResultSet rs=stmt.executeQuery();
		Key key=null;
		if(rs.next()){
			byte[] b=rs.getBytes("private");
			byte[] bb=test.decodeAES(b, keyOfPrivateKey);
			key=BytesToKey(bb);
		}
		rs.close();
		stmt.close();
//		con.close();
		return key;
	}

	public static byte[] encryptRSA(byte[] b, Key key) throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IOException{
		ByteArrayInputStream in = new ByteArrayInputStream(b);
	    ByteArrayOutputStream out = new ByteArrayOutputStream();
	    while(true){
	    	byte[] bb = new byte[53];
	    	int n=in.read(bb);
	    	if(n==-1){
	    		break;
	    	}
	    	byte[] bbb = new byte[n];
	    	System.arraycopy(bb, 0, bbb, 0, n);
		    Cipher cipher = Cipher.getInstance("RSA");
		    cipher.init(Cipher.ENCRYPT_MODE, key);
		    bbb=cipher.doFinal(bbb);
	    	out.write(bbb);
	    }
	    return out.toByteArray();
	  }
	  public static byte[] decryptRSA(byte[] b, Key key) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, IOException{
		   ByteArrayInputStream in = new ByteArrayInputStream(b);
		    ByteArrayOutputStream out = new ByteArrayOutputStream();
		    while(true){
		    	byte[] bb = new byte[64];
		    	int n=in.read(bb);
		    	if(n==-1){
		    		break;
		    	}

		    	byte[] bbb = new byte[n];
		    	System.arraycopy(bb, 0, bbb, 0, n);
			    Cipher cipher = Cipher.getInstance("RSA");
			    cipher.init(Cipher.DECRYPT_MODE, key);
			    bbb=cipher.doFinal(bbb);
		    	out.write(bbb);
		    }
		    return out.toByteArray();
	  }

	public static byte[] generateRondomKey(){
		byte[] key=new byte[16];
		for(int i=0;i<key.length;i++){
			key[i]=(byte)(new java.util.Random().nextInt()%256);
		}
		return key;
	}

	public static byte[] setMessage(byte[] indata, String ext, String message) throws IOException{
		ByteArrayInputStream in = new ByteArrayInputStream(indata);
		BufferedImage buffered = ImageIO.read(in);
		Graphics graphics = buffered.createGraphics();
		graphics.setFont(new Font(graphics.getFont().getFontName(), Font.PLAIN, 24));
		graphics.drawString(message,0,24);
		ByteArrayOutputStream ba = new ByteArrayOutputStream();
		ImageIO.write(buffered, ext, ba);
		return ba.toByteArray();
	}
	public static byte[] changeImageSize(byte[] indata, int height, int width, boolean keepAspectRatio, String ext) throws IOException{
		ByteArrayInputStream in = new ByteArrayInputStream(indata);
		BufferedImage buffered = ImageIO.read(in);
		if(keepAspectRatio){
			if(((double)buffered.getHeight())/height<((double)buffered.getWidth())/width){
				height = 0;
			}else{
				width = 0;
			}
		}
		if(height<=0){
			height = (buffered.getHeight()*width)/buffered.getWidth();
		}
		if(width<=0){
			width = (buffered.getWidth()*height)/buffered.getHeight();
		}
	    ImageFilter filter = new AreaAveragingScaleFilter(width, height);
	    ImageProducer ip = new FilteredImageSource(buffered.getSource(), filter);
	    Image changed = Toolkit.getDefaultToolkit().createImage(ip);
	    buffered = new BufferedImage(changed.getWidth(null), changed.getHeight(null), BufferedImage.TYPE_INT_RGB);
		buffered.getGraphics().drawImage(changed, 0, 0, null);
		buffered.getGraphics().dispose();
		ByteArrayOutputStream ba = new ByteArrayOutputStream();
		// 20200404 start サムネイルjpgの圧縮率変更
//		ImageIO.write(buffered, ext, ba);
		if(!"JPG".equals(ext) || width!=160) {
			ImageIO.write(buffered, ext, ba);
		}else {
			ImageWriter writer = ImageIO.getImageWritersByFormatName("jpeg").next();
			ImageWriteParam param = writer.getDefaultWriteParam();
			param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
			param.setCompressionQuality(0.1f);
			ImageOutputStream ios = ImageIO.createImageOutputStream(ba);
			writer.setOutput(ios);
			writer.write(null, new IIOImage(buffered, null, null), param);
			writer.dispose();
		}
		// 20200404 end
		return ba.toByteArray();
	}

	  public static byte[] objectToBytes(Object obj) throws IOException{
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			new ObjectOutputStream(out).writeObject(obj);
			return out.toByteArray();
	  }

	  public static Key BytesToKey(byte[] b) throws IOException, ClassNotFoundException{
		  ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(b));
		return (Key)in.readObject();
	  }

	  final static String COMMENT_KEY="①②③④⑤⑥⑦⑧⑨⑩⑪⑫⑬⑭⑮⑯⑰⑱⑲⑳ⅠⅡⅢⅣⅤⅥⅦⅧⅨⅩ・㍉㌔㌢㍍㌘㌧㌃㌶㍑㍗㌍㌦㌣㌫㍊㌻㎜㎝㎞㎎㎏㏄㎡㍻〝〟№㏍℡㊤㊥㊦㊧㊨㈱㈲㈹㍾㍽㍼≒≡∫∮∑√⊥∠∟⊿∵∩∪纊褜鍈銈蓜俉炻昱棈鋹曻彅丨仡仼伀伃伹佖侒侊侚侔俍偀倢俿倞偆偰偂傔僴僘兊兤冝冾凬刕劜劦勀勛匀匇匤卲厓厲叝﨎咜咊咩哿喆坙坥垬埈埇﨏塚增墲夋奓奛奝奣妤妺孖寀甯寘寬尞岦岺峵崧嵓﨑嵂嵭嶸嶹巐弡弴彧德忞恝悅悊惞惕愠惲愑愷愰憘戓抦揵摠撝擎敎昀昕昻昉昮昞昤晥晗晙晴晳暙暠暲暿曺朎朗杦枻桒柀栁桄棏﨓楨﨔榘槢樰橫橆橳橾櫢櫤毖氿汜沆汯泚洄涇浯涖涬淏淸淲淼渹湜渧渼溿澈澵濵瀅瀇瀨炅炫焏焄煜煆煇凞燁燾犱犾猤猪獷玽珉珖珣珒琇珵琦琪琩琮瑢璉璟甁畯皂皜皞皛皦益睆劯砡硎硤硺礰礼神祥禔福禛竑竧靖竫箞精絈絜綷綠緖繒罇羡羽茁荢荿菇菶葈蒴蕓蕙蕫﨟薰蘒﨡蠇裵訒訷詹誧誾諟諸諶譓譿賰賴贒赶﨣軏﨤逸遧郞都鄕鄧釚釗釞釭釮釤釥鈆鈐鈊鈺鉀鈼鉎鉙鉑鈹鉧銧鉷鉸鋧鋗鋙鋐﨧鋕鋠鋓錥錡鋻﨨錞鋿錝錂鍰鍗鎤鏆鏞鏸鐱鑅鑈閒隆﨩隝隯霳霻靃靍靏靑靕顗顥飯飼餧館馞驎髙髜魵魲鮏鮱鮻鰀鵰鵫鶴鸙黑ⅰⅱⅲⅳⅴⅵⅶⅷⅸⅹ￢￤＇＂";

	  static byte[] generateCommentBytes(String comment) throws UnsupportedEncodingException{
		  int number = Math.abs(new Random().nextInt())%100000+500000;
		  return DigestUtils.md5(DigestUtils.sha512((comment+COMMENT_KEY+String.valueOf(number)).getBytes("UTF-8")));
	  }

	  static byte[] getCommentBytes(Connection con,String comment) throws SQLException, UnsupportedEncodingException{
	    	byte[] commentBytes=null;

	    	PreparedStatement stmt=con.prepareStatement("select commenthash from test4");
	    	ResultSet rs=stmt.executeQuery();

	    	ROOT:
	    	while(rs.next()){
	    		String commenthash=rs.getString("commenthash");
	    		//新方式
	    		for(int number=500000;number<600000;number++){
	    			byte[] tryBytes=DigestUtils.md5(DigestUtils.sha512((comment+COMMENT_KEY+String.valueOf(number)).getBytes("UTF-8")));
	    			String tryHash=DigestUtils.sha512Hex(tryBytes);
	    			if(tryHash.equals(commenthash)){
	    				commentBytes=tryBytes;
//	    				System.out.println("新方式");
	    				break ROOT;
	    			}
	    		}
	    		//旧方式
	    		byte[] tryBytes=DigestUtils.md5(DigestUtils.sha512((comment+"ああああ").getBytes("UTF-8")));
	  			String tryHash=DigestUtils.sha512Hex(tryBytes);
	  			if(tryHash.equals(commenthash)){
	  				commentBytes=tryBytes;
//    				System.out.println("旧方式");
	  				break ROOT;
	  			}
	    	}
	    	rs.close();
	    	stmt.close();

	    	return commentBytes;
	  }

	    static boolean checkComment(Connection con,String comment,boolean useCommentRemover) throws UnsupportedEncodingException, SQLException{

	    	byte[] commentBytes=getCommentBytes(con,comment);

	    	if(commentBytes!=null){
	    		test.comment=commentBytes;
	        	class CommentRemover extends Thread{
	        		public void run(){
	        			try {
	    					Thread.sleep(test.TIMEOUT_COMMENT);
	    				} catch (InterruptedException e) {
	    					e.printStackTrace();
	    				}
	        			test.comment=null;
	        		}
	        	}

				if(useCommentRemover){
					new CommentRemover().start();
				}
	        	return true;
	    	}

	    	return false;
	    }

	    static boolean checkTable(Connection con){
	    	boolean result = false;
	    	PreparedStatement stmt;
			try {
				//このＳＱＬがエラーにならなければテーブル一式と初期データが揃っているものとする
				stmt = con.prepareStatement("select count(*) from test");
				stmt.execute();
				stmt.close();
				result = true;
			} catch (SQLException e) {
			}
	    	return result;
	    }


		   public static String[][] getDirs(String id) throws SQLException, ClassNotFoundException, IOException{
			   Connection con = test.getConnection();
			   String[][] dirs=getDirs(con,id);
			   con.close();
			   return dirs;
		   }

	   public static String[][] getDirs( Connection con,String id) throws SQLException, ClassNotFoundException, IOException{
		   PreparedStatement stmt = con.prepareStatement("select dir, description from test3 where id=? order by dir");
			stmt.setString(1, id);
			ResultSet rs=stmt.executeQuery();
			Vector<String[]> dirs = new Vector<String[]>();
			String[][] result=null;
			while(rs.next()){
				dirs.add(new String[]{rs.getString("dir"),rs.getString("description")});
			}
			result = dirs.toArray(new String[dirs.size()][]);
			rs.close();
			stmt.close();
			return result;
	   }

	   final static String KEY_OF_PRIVATE_KEY="$%67RTty";

	   static Key getKeyOfprivateKey( Connection con,String id, String password) throws SQLException, ClassNotFoundException, IOException{
//		   Connection con = test.getConnection();
			PreparedStatement stmt = con.prepareStatement("select password from test2 where id=?");
			stmt.setString(1, id);
			ResultSet rs = stmt.executeQuery();
			Key result=null;
			if(rs.next()){
				byte[] hashedPassword=rs.getBytes("password");
				for(int number=6000000;number<7000000;number++){
					byte[] tryPassword=DigestUtils.sha512((password+KEY_OF_PRIVATE_KEY+number).getBytes("UTF-8"));
					//一致する場合
					if(Arrays.equals(hashedPassword, tryPassword)){
						result = test.createKey(
							DigestUtils.md5(
									DigestUtils.sha256((password+KEY_OF_PRIVATE_KEY+number).getBytes("UTF-8"))
									)
							);
						break;
					}
				}

			}
			rs.close();
			stmt.close();
			return result;
	   }

	   static void createInitTableAndData(Connection con, String initPassword) throws SQLException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, IOException{
			final String[] sqls = {
		"create table test(dir varchar(100), name varchar(100), data bytea, width int, type int, primary key(dir,name,width));",
		"create table test2(id varchar(100), password bytea, public bytea, private bytea, primary key(id));",
		"create table test3(id varchar(100), dir varchar(100), description varchar(100), common bytea, primary key(id,dir));",
		"create table test4(commenthash varchar(128), primary key(commenthash));",
		"insert into test4 values('bf6f26c550e0d710fba1b440b5ee7e84de2cf9b3364a4bbff5cb49c5891b9f44198e224c69f78de00c7ad0b658e8bb964c91389671de36945681f52c5b9ed8ad');"
			};

			//テーブルとデータ作成
			for(int i=0;i<sqls.length;i++){
				PreparedStatement stmt = con.prepareStatement(sqls[i]);
				stmt.execute();
				stmt.close();
			}

			//初期ユーザー
			test4.addId("test", initPassword);
	   }
}