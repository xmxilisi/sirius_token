/*
*  Copyright 2019-2020 Zheng Jie
*
*  Licensed under the Apache License, Version 2.0 (the "License");
*  you may not use this file except in compliance with the License.
*  You may obtain a copy of the License at
*
*  http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing, software
*  distributed under the License is distributed on an "AS IS" BASIS,
*  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*  See the License for the specific language governing permissions and
*  limitations under the License.
*/
package com.contract.modules.asset.repository;


import com.contract.modules.asset.domain.UserAsset;
import com.contract.modules.rule.domain.Rule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

/**
* @website https://eladmin.vip
* @author zmh
* @date 2022-10-13
**/
public interface UserAssetRepository extends JpaRepository<UserAsset, Long>, JpaSpecificationExecutor<UserAsset> {
    /**
     * 根据用户id查询资金余额
     * @return /
     */
    UserAsset findByUserId(Long userId);
}