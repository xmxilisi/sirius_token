package com.contract.modules.contract.repository

import com.contract.modules.contract.domain.Order
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query

/**
 * Created with IntelliJ IDEA.
 *
 * @author: Ivan
 * @date: 2022/10/13
 * @Version: 1.0
 * @Description:
 */
interface OrderRepository : JpaRepository<Order?, Long?>, JpaSpecificationExecutor<Order?> {

    fun findListByUserId(userId: Long): List<Order>


    fun findListByVolumeAndStatusBetween(volume: String, status: String, status2: String): List<Order>

    fun findListByStatusBetween(status: String, status2: String): List<Order>


    fun findListByStatus(status: String): List<Order>

    fun findListBySecondAndStatus(second: String, status: String): List<Order>


    fun findListByVolume(volume: String?): List<Order>

    fun findListByVolumeAndSecond(volume: String, second: String): List<Order>

    fun findListByUserIdAndSecond(userId: Long, second: String): List<Order>

    fun findListByUserIdAndSecondOrderByCreateTimeDesc(userId: Long, second: String): List<Order>


    @Query("select o from Order o where o.second = ?1")
    fun findListBySecond(second: String): List<Order>

}