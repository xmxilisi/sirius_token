package com.contract.modules.contract.repository

import com.contract.modules.contract.domain.LotteryRecord
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.sql.Timestamp

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
    @Query("update LotteryRecord l set l.drawDate = ?1, l.markPrice = ?2 , l.status = ?3 where l.id = ?4 and l.status = ?5 and l.type = ?6")
    fun lottery(
        drawDate: Timestamp, markPrice: BigDecimal,
        status: String, id: Long?, status1: String, type : String): Int

    fun findFirstBySymbolAndStatusAndType(symbol: String, status: String, type: String): LotteryRecord

    @Modifying
    @Query("select * FROM `seconds-contract`.sc_lottery_record l where l.status = '0' and TIMESTAMPDIFF(SECOND,create_time,NOW()) > 20"
        , nativeQuery = true)
    fun lockUp(): List<LotteryRecord>

    fun findListByStatus(status: String): List<LotteryRecord>


    @Transactional
    @Modifying
    @Query("delete from `seconds-contract`.sc_lottery_record  where (status = 1)  and " +
            "(select count(*) from `seconds-contract`.sc_order where `seconds-contract`.sc_order.lottery_record_id = `seconds-contract`.sc_lottery_record.lottery_record_id) = 0", nativeQuery = true)
    fun deleteForNotUsed(): Int

    @Query(value = "SELECT * FROM `seconds-contract`.sc_lottery_record ORDER BY draw_date LIMIT 1", nativeQuery = true)
    fun findLast(): LotteryRecord

    fun findFirstBySymbolAndStatus(symbol: String,status: String): LotteryRecord
}