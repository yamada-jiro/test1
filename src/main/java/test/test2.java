package test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class test2{
	public static void main(String[] args) throws Throwable{

		File dir = new File(args[0]);
		Connection con = test.getConnection();
		if(!test.checkComment(con,args[1],false)){
			System.out.println("コメント不一致");
			con.close();
			return;
		}
		if(args.length>=3){
			Key keyOfPrivateKey=test.getKeyOfprivateKey(con, "test", args[2]);
			insertImage(con, dir, keyOfPrivateKey);
		}else{
			insertImage(con, dir, null);
		}
		con.close();

	}

//	static int insertImageCount=0;
	static String insertImage(Connection con, File dir, Key keyOfPrivateKey) throws IOException, SQLException, ClassNotFoundException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException{
		Vector<String> messages = new Vector<String>();
		if(test.comment==null){
			String message = "ERROR(1):commentがnull　"+dir.getPath();
			System.out.println(message);
			return message;
		}

		if(!dir.exists()){
			String message = "ERROR(1-2):dirが存在しない　"+dir.getPath();
			System.out.println(message);
			return message;
		}
		if(dir.isFile()){
			String message = "ERROR(1-3):dirでない（ファイルです）　"+dir.getPath();
			System.out.println(message);
			return message;
		}

		Key commonKey=null;

		File[] files = dir.listFiles();
		int insertImageCount=0;
		int errorImageCount=0;
		for(int i=0;i<files.length;i++){
			if(files[i].isDirectory()){
				String message = insertImage(con, files[i], keyOfPrivateKey);
				System.out.println(message);
				messages.add(message);
				continue;
			}
			String[] splited = files[i].getName().toUpperCase().split("[.]");
			if(splited.length==1){
				continue;
			}
			//拡張子がjpegでない場合
//			if(splited.length==1||!splited[splited.length-1].equals("JPG")){
			if(splited.length==1){
				continue;
			}
			String ext = splited[splited.length-1];
			if(!ext.equals("JPG")&&!ext.equals("PNG")){
				continue;
			}
			FileInputStream in = new FileInputStream(files[i]);
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			byte[] b = new byte[1024];
			while(true){
				int n=in.read(b);
				if(n==-1){
					break;
				}
				out.write(b,0,n);
			}
			in.close();

			if(commonKey==null){
				String sql = "select common from test3 where id='test' and dir=?";
				PreparedStatement stmt = con.prepareStatement(sql);
				stmt.setString(1, files[i].getParentFile().getName());
				ResultSet rs=stmt.executeQuery();
				if(!rs.next()){
					commonKey=test.createKey(test.generateRondomKey());
					byte[] commonBytes=test.objectToBytes(commonKey);
					Key publicKey=test.getPublicKey(con,"test");
					byte[] commonBytesEncrypted=test.encryptRSA(commonBytes,publicKey);
//					sql = "insert into test3 values('test',?,?)";
					sql = "insert into test3 (id,dir,common) values('test',?,?)";
					PreparedStatement stmt2 = con.prepareStatement(sql);
					stmt2.setString(1, files[i].getParentFile().getName());
					stmt2.setBytes(2, commonBytesEncrypted);
					stmt2.execute();
					stmt2.close();
					rs.close();
					stmt.close();
				}else{
					rs.close();
					stmt.close();
					//commonKeyをDBから取得
					if(keyOfPrivateKey==null){
						String message = "commonKeyがnull";
						if(messages.size()!=0){
							for(int s=0;s<messages.size();s++){
								message += "<br>" + messages.get(s);
							}
						}
						return message;
					}
					Key privateKey=test.getPrivateKey(con, "test", keyOfPrivateKey);
					commonKey=test.getCommonKey(con, "test" , dir.getName(), privateKey);
//					String message = "ERROR(2):commonKeyが取得できません(取得機能が未実装)　"+dir.getPath();
//					System.out.println(message);
//					if(messages.size()!=0){
//						for(int s=0;s<messages.size();s++){
//							message += "<br>" + messages.get(s);
//						}
//					}
//					return message;
				}
			}

			String sql = "select count(*) from test where dir=? and name=? and width=640";
			PreparedStatement stmt = con.prepareStatement(sql);
			stmt.setString(1, files[i].getParentFile().getName());
			stmt.setString(2, files[i].getName());
			ResultSet rs = stmt.executeQuery();
			rs.next();
			int count = rs.getInt(1);
			rs.close();
			stmt.close();
			if(count==0){
				//String sql = "insert into test values(?,?,?,?)";
//				sql = "insert into test(dir,name,data,width) values(?,?,?,?)";
				sql = "insert into test(dir,name,data,width,type) values(?,?,?,?,0)";
				stmt = con.prepareStatement(sql);
				stmt.setString(1, files[i].getParentFile().getName());
				stmt.setString(2, files[i].getName());
				b = out.toByteArray();
//				b = test.changeImageSize(b,Integer.MAX_VALUE,640,true,"JPG");
				String[] splited2 = files[i].getName().split("[.]");
				String ext2 = splited2[splited2.length-1].toUpperCase();
				b = test.changeImageSize(b,Integer.MAX_VALUE,640,true,ext2);
				b = test.encodeAES(b,test.getCommentKey());//comment
				b = test.encodeAES(b,commonKey);//common
				stmt.setBytes(3, b);
				stmt.setInt(4, 640);
				stmt.execute();
//				try{
//					stmt.execute();
//				}catch(SQLException e){
//					errorImageCount++;
////					e.printStackTrace();
//					messages.add("処理失敗："+files[i].getName());
//				}
				stmt.close();
			}else{
				errorImageCount++;
				messages.add("既に存在："+files[i].getName());
			}

			System.out.println("処理件数(3)："+ ++insertImageCount+"　"+files[i].getName());
		}

		String message = "処理終了 処理件数："+ insertImageCount+"　エラー件数："+errorImageCount+"　"+dir.getPath();
		System.out.println(message);
		if(messages.size()!=0){
			for(int s=0;s<messages.size();s++){
				message += "<br>" + messages.get(s);
			}
		}
		return message;
	}

	static String updateDescription(Connection con, String dir, String description) throws SQLException{
		String sql = "update test3 set description=? where dir=?";
		PreparedStatement stmt = con.prepareStatement(sql);
		stmt.setString(1, description);
		stmt.setString(2, dir);
		int count = stmt.executeUpdate();
		stmt.close();
		return "更新件数="+count+" dir="+dir+" description="+description;
	}
	static String changeType(String type){
		return "Type="+type;
	}


}