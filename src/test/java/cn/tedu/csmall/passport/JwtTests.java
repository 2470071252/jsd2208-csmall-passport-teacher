package cn.tedu.csmall.passport;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JwtTests {

    // eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJwaG9uZSI6IjEzODAwMTM4MDAxIiwiaWQiOjk1MjcsImV4cCI6MTY3MTA5NDExNywidXNlcm5hbWUiOiJ0ZXN0LWp3dCJ9.p5gQ6fg-mgAvDF3LllRBHwmnqkTVbHAAM8fDsMTIHuA
    // eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJwaG9uZSI6IjEzODAwMTM4MDAxIiwiaWQiOjk1MjcsImV4cCI6MTY3MTA5NTI3MiwidXNlcm5hbWUiOiJ0ZXN0LWp3dCJ9.9aHPOE-JLjCqd9sKEehoZzqGhz7hpsYcUwIzpiVdfmg

    // 是一个自定义的字符串，应该是一个保密数据，最低要求不少于4个字符，但推荐使用更加复杂的字符串
    String secretKey = "fdsFOj4tp9Dgvfd9t45rDkFSLKgfR8ou";

    @Test
    void generate() {
        // JWT的过期时间
        Date date = new Date(System.currentTimeMillis() + 5 * 60 * 1000);

        // 你要存入到JWT中的数据
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", 9527);
        claims.put("username", "test-jwt");
        claims.put("phone", "13800138001");

        String jwt = Jwts.builder() // 获取JwtBuilder，准备构建JWT数据
                // 【1】Header：主要配置alg（algorithm：算法）和typ（type：类型）属性
                .setHeaderParam("alg", "HS256")
                .setHeaderParam("typ", "JWT")
                // 【2】Payload：主要配置Claims，把你要存入的数据放进去
                .setClaims(claims)
                // 【3】Signature：主要配置JWT的过期时间、签名的算法和secretKey
                .setExpiration(date)
                .signWith(SignatureAlgorithm.HS256, secretKey)
                // 完成
                .compact(); // 得到JWT数据
        System.out.println(jwt);
    }

    @Test
    void parse() {
        // 需要被解析的JWT，在复制此数据时，切记不要多复制了换行符（\n）
        String jwt = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJwaG9uZSI6IjEzODAwMTM4MDAxIiwiaWQiOjk1MjcsImV4cCI6MTY3MTA5NTI3MiwidXNlcm5hbWUiOiJ0ZXN0LWp3dCJ9.9aHPOE-JLjCqd9sKEehoZzqGhz7hpsYcUwIzpiVdfmg";

        // 执行解析
        Claims claims = Jwts.parser() // 获得JWT解析工具
                .setSigningKey(secretKey)
                .parseClaimsJws(jwt)
                .getBody();

        // 从Claims中获取生成时存入的数据
        Object id = claims.get("id");
        Object username = claims.get("username");
        Object phone = claims.get("phone");
        System.out.println("id = " + id);
        System.out.println("username = " + username);
        System.out.println("phone = " + phone);
    }

}
