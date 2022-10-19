package com.contract.modules.contract.service.dto;

import com.contract.annotation.Query;
import lombok.Data;

/**
 * Created with IntelliJ IDEA.
 *
 * @author: Ivan
 * @date: 2022/10/18
 * @Version: 1.0
 * @Description:
 */
@Data
public class LotteryRecordQueryCriteria {

    @Query
    private String symbol;
}
