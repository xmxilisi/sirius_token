package com.contract.modules.contract.service.mapstruct;

import com.contract.base.BaseMapper;
import com.contract.modules.contract.domain.LotteryRecord;
import com.contract.modules.contract.service.dto.LotteryRecordDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * Created with IntelliJ IDEA.
 *
 * @author: Ivan
 * @date: 2022/10/18
 * @Version: 1.0
 * @Description:
 */
@Mapper(componentModel = "spring",unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LotteryRecordMapper extends BaseMapper<LotteryRecordDto, LotteryRecord> {

}