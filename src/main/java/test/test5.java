package test;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.apache.commons.codec.digest.DigestUtils;

public class test5 {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Throwable{


		changeComment(args[0],args[1],args[2],args[3]);

	}


//	static int changeCommentCount;
	static boolean changeComment(String id,String password,String oldComment, String newComment) throws SQLException, ClassNotFoundException, IOException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException{
		Connection con=test.getConnection();
//		changeCommentCount=0;
		try{
			con.setAutoCommit(false);

			if(!test.checkComment(con,oldComment,false)){
				System.out.println("ERROR(1)");
				con.rollback();
				con.close();
				return false;
			}

			Key oldCommentKey=test.getCommentKey();

			String oldCommentHash=DigestUtils.sha512Hex(test.getCommentBytes(con,oldComment));
			String newCommentHash=DigestUtils.sha512Hex(test.generateCommentBytes(newComment));
			PreparedStatement stmt3=con.prepareStatement("UPDATE test4 SET commentHash=? WHERE commentHash=?");
			stmt3.setString(1, newCommentHash);
			stmt3.setString(2, oldCommentHash);
			stmt3.executeUpdate();
			stmt3.close();
			if(!test.checkComment(con,newComment,false)){
				System.out.println("ERROR(2)");
				con.rollback();
				con.close();
				return false;
			}

			Key newCommentKey=test.getCommentKey();

			Key keyOfPrivateKey=test.getKeyOfprivateKey(con,id, password);
			Key privateKey=test.getPrivateKey(con,id, keyOfPrivateKey);

			PreparedStatement stmt=con.prepareStatement("SELECT dir,name,width FROM test order by dir,name,width");
			ResultSet rs = stmt.executeQuery();
			String previousDir=null;
			Key commonKey=null;
			while(rs.next()){
				if(!rs.getString("dir").equals(previousDir)){
					previousDir=rs.getString("dir");
					commonKey=test.getCommonKey(con,id, rs.getString("dir"), privateKey);
				}

				String sql4 = "SELECT data FROM test WHERE dir=? AND name=? AND width=?";
				PreparedStatement stmt4 = con.prepareStatement(sql4);
				stmt4.setString(1, rs.getString("dir"));
				stmt4.setString(2, rs.getString("name"));
				stmt4.setInt(3, rs.getInt("width"));
				ResultSet rs4=stmt4.executeQuery();
				rs4.next();
				byte[] data=rs4.getBytes("data");
				rs4.close();
				stmt4.close();

				data=test.decodeAES(data, commonKey);
				data=test.decodeAES(data, oldCommentKey);
				data=test.encodeAES(data, newCommentKey);
				data=test.encodeAES(data, commonKey);
				PreparedStatement stmt2=con.prepareStatement("UPDATE test SET data=? WHERE dir=? AND name=? AND width=?");
				stmt2.setBytes(1, data);
				stmt2.setString(2, rs.getString("dir"));
				stmt2.setString(3, rs.getString("name"));
				stmt2.setInt(4, rs.getInt("width"));
				stmt2.executeUpdate();
				stmt2.close();
//				System.out.println("処理件数(4)："+ ++changeCommentCount);
			}
			rs.close();
			stmt.close();

			con.commit();
			con.close();
			return true;
		}catch(Throwable t){
			System.out.println("ERROR(3)");
			con.rollback();
			con.close();
			return false;
		}
	}

}