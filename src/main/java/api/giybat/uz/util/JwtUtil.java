package api.giybat.uz.util;

import api.giybat.uz.dto.JwtDTO;
import api.giybat.uz.enums.ProfileRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.*;
import java.util.stream.Collectors;

public class JwtUtil {
    private static final int tokenLiveTime = 1000 * 3600 * 24;
    private static final String secretKey = "buyerdajudakattaotnikallasidaytextbolishikerakbutokenlengthchunmanimchayanayambilmadim";


    public static String encode(String username, Integer id, List<ProfileRole> roleList) {

        String strRoles = roleList.stream()
                .map(items -> items.name())
                .collect(Collectors.joining(", "));

        Map<String, String> claims = new HashMap<>();
        claims.put("role", strRoles);
        claims.put("id", String.valueOf(id));

        return Jwts
                .builder()
                .subject(username)
                .claims(claims)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + tokenLiveTime))
                .signWith(getSignInKey())
                .compact();

    }

    public static JwtDTO decode(String token) {
        Claims claims = Jwts
                .parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        String username = claims.getSubject();
        Integer id = Integer.valueOf((String) claims.get("id"));
        String strRoleList = claims.get("role", String.class);

        String[] roles = strRoleList.split(",");
        List<ProfileRole> roleList = new ArrayList<>();
        for (String role : roles) {
            roleList.add(ProfileRole.valueOf(role.trim()));
        }

        //List<ProfileRole> roleList2 = Arrays.stream(strRoleList.split(",")).map(ProfileRole::valueOf).toList();

        return new JwtDTO(username, id, roleList);
    }

    public static String encodeForEmailVerf(Integer id) {
        return Jwts
                .builder()
                .subject(String.valueOf(id))
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + (60 * 60 * 1000)))
                .signWith(getSignInKey())
                .compact();
    }

    public static Integer decodeRegVer(String token) {
        Claims claims = Jwts
                .parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return Integer.valueOf(claims.getSubject());
    }

    private static SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }


}
