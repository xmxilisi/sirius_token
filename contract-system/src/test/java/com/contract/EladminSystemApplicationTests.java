package com.contract;


import com.contract.modules.asset.domain.UserAsset;
import com.contract.modules.asset.repository.UserAssetRepository;
import com.contract.modules.contract.service.LotteryRecordService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;

import java.math.BigDecimal;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class EladminSystemApplicationTests {

    @Autowired
    private LotteryRecordService lotteryRecordService;
    @Autowired
    private UserAssetRepository userAssetRepository;

    @Test
    @Rollback(false)
    public void getSt(){
        UserAsset userAsset = userAssetRepository.findByUserId(2L);
        userAsset.setBalance(new BigDecimal("987564"));
        System.out.println(userAsset.getUserId());
    }

    public static void main(String[] args) {

    }
}

