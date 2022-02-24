package test;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Random;
import java.util.Vector;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.apache.commons.codec.digest.DigestUtils;


public class test4 {

  public static void main(String[] args) throws Exception {
	  String id=args[0];
	  String password=args[1];

	  addId(id,password);

  }

  public static String[] getAllId() throws SQLException{
	  Connection con = test.getConnection();
	 Statement stmt= con.createStatement();
	 ResultSet rs=stmt.executeQuery("select id from test2 order by id");
	 Vector<String> ids=new Vector<String>();
	 while(rs.next()){
		 ids.add(rs.getString("id"));
	 }
	 rs.close();
	 stmt.close();
	  con.close();
	return ids.toArray(new String[ids.size()]);

  }


  public static void deleteId(String id) throws SQLException{

	  Connection con = test.getConnection();

	  con.setAutoCommit(false);

	  PreparedStatement stmt= con.prepareStatement("delete from test2 where id=?");
	 stmt.setString(1, id);
	  stmt.execute();
	 stmt.close();

	 stmt= con.prepareStatement("delete from test3 where id=?");
	 stmt.setString(1, id);
	  stmt.execute();
	 stmt.close();

	 con.commit();

	  con.close();

  }

  public static void addId(String id,String password) throws IOException, SQLException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException{

	  //小文字化
	  id = id.toLowerCase();

	  Connection con = test.getConnection();

	KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
	kpg.initialize(512);
	KeyPair kp = kpg.generateKeyPair();
	PublicKey pubk = kp.getPublic();
	PrivateKey prvk = kp.getPrivate();

	int number = Math.abs(new Random().nextInt())%1000000+6000000;

	byte[] b1 = test.objectToBytes(pubk);
	byte[] b2 = test.objectToBytes(prvk);
	byte[] privateEnrypted=test.encodeAES(b2, test.createKey(
			DigestUtils.md5(
					DigestUtils.sha256((password+test.KEY_OF_PRIVATE_KEY+number).getBytes("UTF-8"))
					)
			));

	String sql="insert into test2 values (?,?,?,?)";
	PreparedStatement stmt = con.prepareStatement(sql);
	stmt.setString(1, id);
	stmt.setBytes(2, DigestUtils.sha512((password+test.KEY_OF_PRIVATE_KEY+number).getBytes("UTF-8")));
	stmt.setBytes(3, b1);
	stmt.setBytes(4, privateEnrypted);
	stmt.execute();
	con.close();

  }

  public static boolean changePassword(String id,String oldPassword, String newPassword) throws SQLException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, IOException, ClassNotFoundException{
	  Connection con = test.getConnection();
	  Key keyOfPrivateKey=test.getKeyOfprivateKey(con, id, oldPassword);
	  if(keyOfPrivateKey==null){
		  con.close();
		  return false;
	  }
	  Key prvk=test.getPrivateKey(con, id, keyOfPrivateKey);
	  int number = Math.abs(new Random().nextInt())%1000000+6000000;
		byte[] b2 = test.objectToBytes(prvk);
		byte[] privateEnrypted=test.encodeAES(b2, test.createKey(
				DigestUtils.md5(
						DigestUtils.sha256((newPassword+test.KEY_OF_PRIVATE_KEY+number).getBytes("UTF-8"))
						)
				));

		String sql="update test2 set password=?, private=? where id=?";
		PreparedStatement stmt = con.prepareStatement(sql);
		stmt.setBytes(1, DigestUtils.sha512((newPassword+test.KEY_OF_PRIVATE_KEY+number).getBytes("UTF-8")));
		stmt.setBytes(2, privateEnrypted);
		stmt.setString(3, id);
		stmt.execute();

		con.close();

		return true;
  }


}