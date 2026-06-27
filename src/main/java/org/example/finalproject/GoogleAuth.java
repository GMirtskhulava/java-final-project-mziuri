package org.example.finalproject;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.GoogleAuthenticatorQRGenerator;

public class GoogleAuth {
    private static final GoogleAuthenticator gAuth = new GoogleAuthenticator();
    private String secretKey;
    private String qrUrl;

    private GoogleAuth(String secretKey, String qrUrl) {
        setSecretKey(secretKey);
        setQrUrl(qrUrl);
    }

    public static GoogleAuth generateGAuth(String userEmail) {
        GoogleAuthenticatorKey key = gAuth.createCredentials();
        String qrUrl = GoogleAuthenticatorQRGenerator.getOtpAuthURL("JavaFX - SocNet", userEmail,key );

        return new GoogleAuth(key.getKey(), qrUrl);
    }

    public static boolean checkAuthCode(String secretKey, int verificationCode) {
        return gAuth.authorize(secretKey, verificationCode);
    }




    public String getSecretKey() {
        return secretKey;
    }
    public String getQrUrl() {
        return qrUrl;
    }

    public void setQrUrl(String qrUrl) {
        this.qrUrl = qrUrl;
    }
    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }
}