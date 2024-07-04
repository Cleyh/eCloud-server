package com.jidays.ecloud.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class TokenComp {
    private static final String SECRET_KEY = "MySecretKey";
    private static final long EXPIRATION_TIME = 86400000; // 1天

    public static String generateToken(int userID, String email, String role) {
        Date issuedAt = new Date();
        Date expiresAt = new Date(issuedAt.getTime() + EXPIRATION_TIME);

        return JWT.create()
                .withIssuedAt(issuedAt)
                .withExpiresAt(expiresAt)
                .withClaim("user_id", userID)
                .withClaim("email", email)
                .withClaim("role", role)
                .sign(Algorithm.HMAC256(SECRET_KEY));
    }

    public static Map<String, Object> isValidToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY);
            JWTVerifier verifier = JWT.require(algorithm)
                    .build(); //Reusable verifier instance
            DecodedJWT jwt = verifier.verify(token);

            // 检查 token 中是否包含特定的声明
            Integer userID = jwt.getClaim("user_id").asInt();
            String email = jwt.getClaim("email").asString();
            String role = jwt.getClaim("role").asString();

            if (userID == null || email == null || role == null) {
                return null;
            }
            // 如果所有声明都存在，返回包含这些声明的 Map
            Map<String, Object> claims = new HashMap<>();
            claims.put("user_id", userID);
            claims.put("email", email);
            claims.put("role", role);

            return claims;
        } catch (JWTVerificationException exception){
            //Invalid signature/claims
            return null;
        }
    }

    // Optional: Methods to extract information from token
    public static int getIDFromToken(String token) {
        return JWT.decode(token).getClaim("user_id").asInt();
    }

    public static String getEmailFromToken(String token) {
        return JWT.decode(token).getClaim("email").asString();
    }

    public static String getRoleFromToken(String token) {
        return JWT.decode(token).getClaim("role").asString();
    }
}
