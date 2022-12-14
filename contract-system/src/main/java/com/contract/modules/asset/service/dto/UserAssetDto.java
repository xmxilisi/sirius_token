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
package com.contract.modules.asset.service.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
* @website https://eladmin.vip
* @description /
* @author zmh
* @date 2022-10-13
**/
@Data
public class UserAssetDto implements Serializable {

    private Long assetId;

    /** 用户id */
    private Long userId;

    /** 余额 */
    private BigDecimal balance;
}