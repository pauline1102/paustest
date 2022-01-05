package business;

import com.fasterxml.jackson.databind.ObjectMapper;
import data.LoginData;
import data.UserDAO;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.crypto.MacProvider;

import javax.crypto.spec.SecretKeySpec;
import javax.ws.rs.NotAuthorizedException;
import java.security.Key;
import java.util.Calendar;


public class JWTHandler {
    private static Key key;
    private static final int TOKEN_EXPIRY = 2880; //2 days

    public static String generateJwtToken(LoginData user) {
        Calendar expiry = Calendar.getInstance();
        expiry.add(Calendar.MINUTE, TOKEN_EXPIRY);
        return Jwts.builder()
                .setIssuer("GiraffeDeluxe")
                .claim("user", user)
                .signWith(SignatureAlgorithm.HS512, getKey())
                .setExpiration(expiry.getTime())
                .compact();
    }

    private static Key getKey() {
//Generate a secret key, if there is none specified in the environment - only use fixed key in development for debugging
        if (key == null) {
            if (System.getenv("JWT_SECRET_KEY") != null && System.getenv("JWT_SECRET_KEY") != "") {
                String string = System.getenv("JWT_SECRET_KEY");
                key = new SecretKeySpec(string.getBytes(), 0, string.length(), "HS512");
            } else {
                key = MacProvider.generateKey(SignatureAlgorithm.HS512);
            }
        }
        return key;
    }
    // Validering af token
    public static LoginData validate(String authentication) {
        System.out.println(authentication);
        if(authentication == null){
            throw new NotAuthorizedException("ingen header");
        }
        String[] tokenArray = authentication.split(" ");
        String token = tokenArray[tokenArray.length - 1];

        System.out.println(token);
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(getKey())
                    .parseClaimsJws(token)
                    .getBody();
            System.out.println("test2");
            ObjectMapper mapper = new ObjectMapper();
            LoginData user = mapper.convertValue(claims.get("user"), LoginData.class);
            System.out.println(user);
            return user;
        } catch (JwtException e){
            System.out.println(e.getClass() +":  "+ e.getMessage());
            throw new NotAuthorizedException(e.getMessage());
        }
    }
}

