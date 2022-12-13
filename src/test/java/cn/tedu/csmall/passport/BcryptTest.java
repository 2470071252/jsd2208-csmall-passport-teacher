package cn.tedu.csmall.passport;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class BcryptTest {

    PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Test
    void encode() {
        String rawPassword = "123456";
        System.out.println("原文：" + rawPassword);

        long start = System.currentTimeMillis();
        for (int i = 0; i < 20; i++) {
            String encodedPassword = passwordEncoder.encode(rawPassword);
            System.out.println("密文：" + encodedPassword);
        }
        long end = System.currentTimeMillis();

        System.out.println("耗时：" + (end - start));
    }

    //    原文：123456
    //    密文：$2a$10$XGvx1Y/.B.fSUt2uS3m43OaFkgZCWs.isoLjXw5O1YTbX1QE001x6
    //    密文：$2a$10$m1XBX0V9Jk8sGO.oZVxF5O3nxRQ/bZjKMGuBn.og74ddrvNfkR1YC
    //    密文：$2a$10$65z1UUvAaNHeit4GgMN8auoEx5ZXYBJI9/bG.HYQiS5YgYkqeARlG
    //    密文：$2a$10$CSr3Js2mu1d/LSJiVTrLQ.11STmG9lFZvO4o5zmyTAu8xOlCjwyf6
    //    密文：$2a$10$WYI2xGW5wJCnG7jz6qOXruDPzS6o9tO9IBdbG3eQpPpbCsvOkl1NK
    //    密文：$2a$10$cs4HLJCvqD8PmHYqcANiiuRpXZMy4Pf3ubbG3EIaOZ.TqyDr5iLuu

    @Test
    void matches() {
        String rawPassword = "123456";
        System.out.println("原文：" + rawPassword);

        String encodedPassword = "$2a$10$cs4HLJCvqD8PmHYqcANiiuRpXZMy4Pf3ubbG3EIaOZ.TqyDr5iLuu";
        System.out.println("密文：" + encodedPassword);

        boolean matches = passwordEncoder.matches(rawPassword, encodedPassword);
        System.out.println("匹配结果：" + matches);
    }

}
