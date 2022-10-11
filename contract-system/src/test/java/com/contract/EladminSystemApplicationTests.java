package com.contract;

import com.contract.modules.contract.utils.HttpClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.net.http.HttpResponse;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class EladminSystemApplicationTests {

    @Test
    public void contextLoads() {
        HttpResponse<String> stringHttpResponse =
                HttpClient.get("https://www.baidu.com");
        System.out.println(stringHttpResponse);
    }

    public static void main(String[] args) {

    }
}

