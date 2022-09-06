package se.funkabo.utils.key;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

@Configuration
public class KeyUtils {

	@Autowired
	ResourceLoader resourceLoader;
	
	public RSAPublicKey getEncryptionKey() {
		return getPublicKey();
	}
	
	public RSAPrivateKey getDecryptionKey() {
		return getPrivateKey();
	}
	
	private RSAPublicKey getPublicKey() {
		byte[] decodedKey = null;
		X509EncodedKeySpec spec = null;
		KeyFactory keyFactory = null;
		RSAPublicKey publicKey = null;
		Resource resource = resourceLoader.getResource("classpath:key/publicKey");
		try { decodedKey = Files.readAllBytes(Paths.get(resource.getURI()));
			  spec = new X509EncodedKeySpec(decodedKey);
			  keyFactory = KeyFactory.getInstance("RSA");
			  publicKey = (RSAPublicKey)keyFactory.generatePublic(spec);
		} catch (NoSuchAlgorithmException | InvalidKeySpecException | IOException ex) {
			ex.printStackTrace();
		}
		return publicKey;
	}
	
	private RSAPrivateKey getPrivateKey() {
		byte[] decodedKey = null;
		PKCS8EncodedKeySpec spec = null;
		KeyFactory keyFactory = null;
		RSAPrivateKey privateKey = null;
		Resource resource = resourceLoader.getResource("classpath:key/privateKey");
		try { decodedKey = Files.readAllBytes(Paths.get(resource.getURI()));
			  spec = new PKCS8EncodedKeySpec(decodedKey);
			  keyFactory = KeyFactory.getInstance("RSA");
			  privateKey = (RSAPrivateKey )keyFactory.generatePrivate(spec);
		} catch (NoSuchAlgorithmException | InvalidKeySpecException | IOException ex) {
			ex.printStackTrace();
		}
		return privateKey;
	}
		
}
