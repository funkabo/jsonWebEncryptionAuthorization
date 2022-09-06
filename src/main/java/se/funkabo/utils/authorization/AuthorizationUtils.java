package se.funkabo.utils.authorization;

import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.MGF1ParameterSpec;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import se.funkabo.entity.Account;
import se.funkabo.utils.json.Base64toJson;
import se.funkabo.utils.key.KeyUtils;


@Component
public class AuthorizationUtils {
	
	@Autowired
	private KeyUtils keyUtils;

	public String encrypt(Account account) {
		RSAPublicKey publicKey = null;
		byte[] payload = getPayload(account);
		byte[] iv = null;
		byte[] cipherText = null;
		byte[] tag = null;
		byte[] encrypted = null;

		try { publicKey = keyUtils.getEncryptionKey();
			  SecretKey secretKey = setContentEncryptionKeySize(256); 
	      	  iv = setInitializationVector(96);
	      	  cipherText = encryptContent(secretKey, iv, payload);
	      	  tag = getAuthenticationTag(cipherText);
	      	  encrypted = encryptKey(secretKey, publicKey);
		} catch(InvalidKeyException | InvalidAlgorithmParameterException | NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException | ShortBufferException ex) {
			ex.getClass();
		}
		
		Gson gson = new GsonBuilder().create();
		
		Map<String,Object> tokenMap = new LinkedHashMap<String, Object>();
        tokenMap.put("typ", "JWT");
        tokenMap.put("alg", "RSA-OAEP");
        tokenMap.put("enc", "A256GCM");
        tokenMap.put("key", Base64.getEncoder().encodeToString(encrypted));
        tokenMap.put("iv", Base64.getEncoder().encodeToString(iv));
        tokenMap.put("ciphertext", Base64.getEncoder().encodeToString(cipherText));
        tokenMap.put("tag", Base64.getEncoder().encodeToString(tag));
        String json = gson.toJson(tokenMap);
        
        byte[] jsonBytes = json.getBytes(StandardCharsets.UTF_8); 
        return new String((Base64.getUrlEncoder().encodeToString(jsonBytes)));	
	}

	public String decrypt(String token) {
		RSAPrivateKey privateKey = null;
		String subject = "";
		String decodedToken = decodeToken(token);
		
		Gson base64Gson = new GsonBuilder().registerTypeAdapter(byte[].class, new Base64toJson()).create();		    
	    JsonElement jsonElements = new JsonParser().parse(decodedToken);
	    JsonObject jsonObjects = jsonElements.getAsJsonObject();
		
	    byte[] encryptedKey = base64Gson.fromJson(jsonObjects.get("key"), byte[].class);
	    byte[] initializationVector = base64Gson.fromJson(jsonObjects.get("iv"), byte[].class);
	    byte[] cipherText = base64Gson.fromJson(jsonObjects.get("ciphertext"), byte[].class);
	    byte[] tag = base64Gson.fromJson(jsonObjects.get("tag"), byte[].class); 

		try { privateKey = keyUtils.getDecryptionKey();
			  byte[] decryptedKey = decryptEncryptionKey(privateKey, encryptedKey);
			  SecretKey secretKey = new SecretKeySpec(decryptedKey, "AES");
		      byte[] payload = decryptContent(secretKey, initializationVector.length, tag.length, cipherText);	      		               
			  subject = parseSubject(payload);     	      
		} catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException | NoSuchAlgorithmException
				| NoSuchPaddingException | InvalidAlgorithmParameterException ex){
			ex.printStackTrace();
		}	
		return subject;
	}
    
	public static byte[] getPayload(Account account) {
		Gson gson = new GsonBuilder().create();	
		Map<String,Object> payloadMap = new LinkedHashMap<String, Object>();
		payloadMap.put("jti", UUID.randomUUID().toString());
		payloadMap.put("iss", "https://www.funkabo.se");
		payloadMap.put("iat", Date.from(ZonedDateTime.now(ZoneId.of("UTC")).toInstant()).toString());
		payloadMap.put("nbf", Date.from(ZonedDateTime.now(ZoneId.of("UTC")).toInstant().plusSeconds(2)).toString());
		payloadMap.put("exp", Date.from(ZonedDateTime.now(ZoneId.of("UTC")).toInstant().plusSeconds(3600)).toString());
		payloadMap.put("aud", "postman");
		payloadMap.put("sub", account.getUsername());						
		return gson.toJson(payloadMap).getBytes();
	}
	
	public static SecretKey setContentEncryptionKeySize(int keySize) {
		SecureRandom random = new SecureRandom();
		byte[] key = new byte[keySize / Byte.SIZE];
		  random.nextBytes(key);
		  return new SecretKeySpec(key, "AES");
	}
			
	public static byte[] setInitializationVector(int initVectorSize) {
		SecureRandom random = new SecureRandom();
		byte[] initVector = new byte[initVectorSize / Byte.SIZE]; 
		random.nextBytes(initVector);
		return initVector;
	}
		
	public static byte[] getAuthenticationTag(byte[] ciphertext){
		byte[] authenticationTag = Arrays.copyOfRange(ciphertext, ciphertext.length - (128 / Byte.SIZE), ciphertext.length); 
		return authenticationTag;
	}
	
	public static byte[] encryptContent(SecretKey cek, byte[] initializationVector, byte[] contentToEncrypt) throws InvalidKeyException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, ShortBufferException { 
		byte[] cipherText = null;
		try { Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
			  GCMParameterSpec gcmSpec = new GCMParameterSpec(16 * Byte.SIZE, initializationVector);
			  cipher.init(Cipher.ENCRYPT_MODE, cek, gcmSpec);
			  cipherText = new byte[initializationVector.length + cipher.getOutputSize(contentToEncrypt.length)];
			  for(int i=0; i < initializationVector.length; i++) {
		          cipherText[i] = initializationVector[i];
		      }
			  cipher.doFinal(contentToEncrypt, 0, contentToEncrypt.length, cipherText, initializationVector.length );
	    } catch (NoSuchPaddingException | InvalidAlgorithmParameterException | BadPaddingException | IllegalBlockSizeException | InvalidKeyException | NoSuchAlgorithmException | ShortBufferException ex) {
			  throw new IllegalStateException(ex.toString());
	    }
		return cipherText;
	}
	
	public static byte[] encryptKey(SecretKey contentEncryptionKey, RSAPublicKey publicKey)throws IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, InvalidKeyException  {
		byte[] encrypted = null;
		try{ Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
			 cipher.init(Cipher.ENCRYPT_MODE, publicKey, new OAEPParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT));
			 encrypted = cipher.doFinal(contentEncryptionKey.getEncoded());
		} catch(NoSuchPaddingException | BadPaddingException | IllegalBlockSizeException | InvalidKeyException | NoSuchAlgorithmException  ex) {
			 ex.printStackTrace();		
		}
		return encrypted;
	}
	
	public static String decodeToken(String token) {
		String requestToken = new String(token.getBytes(StandardCharsets.UTF_8));	
	    byte[] tokenBytes = Base64.getUrlDecoder().decode(requestToken);
	    return new String(tokenBytes);
	}
	
	public static String parseSubject(byte[] payload) {
		String load = new String(payload);			     
		JsonElement jsonElement = new JsonParser().parse(load);
		JsonObject  jsonObject = jsonElement.getAsJsonObject();				
		Gson object = new GsonBuilder().create(); 		               
		return object.fromJson(jsonObject.get("sub"), String.class);
	}
	
	public static JsonObject parsePayload(byte[] payload) {
		String load = new String(payload);			     
		JsonElement jsonElement = new JsonParser().parse(load);
		JsonObject  jsonObject = jsonElement.getAsJsonObject();				
		Gson object = new GsonBuilder().create(); 		               
		return object.fromJson(jsonObject.getAsJsonObject(), JsonObject.class);
	}
	
    public static byte[] decryptEncryptionKey(RSAPrivateKey privateKey, byte[] contentEncryptionKey) throws IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, InvalidKeyException {
    	byte[] decrypted = null;
    	try{ Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
			 cipher.init(Cipher.DECRYPT_MODE, privateKey, new OAEPParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT));
			 decrypted = cipher.doFinal(contentEncryptionKey);	   
		} catch (NoSuchPaddingException | BadPaddingException | IllegalBlockSizeException | InvalidKeyException | NoSuchAlgorithmException  ex) {
			ex.printStackTrace();		
		}
    	return decrypted;
	}
	
    public static byte[] decryptContent(SecretKey skey, int initializationVectorLength, int aTagLength, byte[] cipherText) throws BadPaddingException, IllegalBlockSizeException {
    	byte[] plainText = null;
    	try { byte[] iv = Arrays.copyOfRange(cipherText, 0, initializationVectorLength);
  	          Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");  
  	          GCMParameterSpec spec = new GCMParameterSpec(aTagLength * Byte.SIZE, iv);
  	          cipher.init(Cipher.DECRYPT_MODE, skey, spec);
  	          plainText = cipher.doFinal(cipherText, initializationVectorLength, cipherText.length - initializationVectorLength );   
  	      } catch (NoSuchPaddingException | InvalidAlgorithmParameterException | InvalidKeyException | NoSuchAlgorithmException ex) {
  	         throw new IllegalStateException(ex.toString());
  	      }
    	return plainText;  
  	  }
    
    public static void overwriteKey(byte[] contentEncryptionKey) {
		Arrays.fill(contentEncryptionKey,(byte) 0);
	}
		
}
