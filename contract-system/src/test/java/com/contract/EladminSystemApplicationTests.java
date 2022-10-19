package com.contract;


import com.contract.modules.contract.service.LotteryRecordService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class EladminSystemApplicationTests {

    @Autowired
    private LotteryRecordService lotteryRecordService;

    @Test
    public void getSt(){
        lotteryRecordService.lottery("30");
    }

    public static void main(String[] args) {

    }
}

