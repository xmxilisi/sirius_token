package com.contract.modules.contract.repository

import com.contract.modules.contract.domain.LotteryRecord
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.sql.Timestamp
import java.util.*

/**
 * Created with IntelliJ IDEA.
 *
 * @author: Ivan
 * @date: 2022/10/12
 * @Version: 1.0
 * @Description:
 */
interface LotteryRecordRepository : JpaRepository<LotteryRecord, Long> {


    @Transactional
    @Modifying
    @Query("update LotteryRecord l set l.drawDate = ?1, l.type = ?2, l.markPrice = ?3 , l.status = ?4 where l.id = ?5 and l.status = ?6")
    fun lottery(
        drawDate: Timestamp, type: String,
        markPrice: BigDecimal,
        status: String, id: Long?, status1: String): Int

    @Transactional
    @Modifying
    @Query("update `seconds-contract`.sc_lottery_record l " +
            "set l.status = '1',l.start_time = now() " +
            "where l.status = '0' and TIMESTAMPDIFF(SECOND,create_time,NOW()) > 20"
        , nativeQuery = true)
    fun lockUp(): Int

    @Transactional
    @Modifying
    @Query("delete from `seconds-contract`.sc_lottery_record  where (status = 1)  and " +
            "(select count(*) from `seconds-contract`.sc_order where `seconds-contract`.sc_order.lottery_record_id = `seconds-contract`.sc_lottery_record.lottery_record_id) = 0", nativeQuery = true)
    fun deleteForNotUsed(): Int

    @Query(value = "SELECT * FROM `seconds-contract`.sc_lottery_record ORDER BY draw_date LIMIT 1", nativeQuery = true)
    fun findLast(): LotteryRecord

    fun findFirstBySymbolAndStatus(symbol: String,status: String): LotteryRecord
}