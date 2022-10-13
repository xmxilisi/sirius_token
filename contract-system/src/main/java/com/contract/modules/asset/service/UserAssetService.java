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
package com.contract.modules.asset.service;
import com.contract.modules.asset.domain.UserAsset;
import com.contract.modules.asset.service.dto.UserAssetDto;
import com.contract.modules.asset.service.dto.UserAssetQueryCriteria;
import org.springframework.data.domain.Pageable;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
* @website https://eladmin.vip
* @description 服务接口
* @author zmh
* @date 2022-10-13
**/
public interface UserAssetService {

    /**
    * 查询数据分页
    * @param criteria 条件
    * @param pageable 分页参数
    * @return Map<String,Object>
    */
    Map<String,Object> queryAll(UserAssetQueryCriteria criteria, Pageable pageable);

    /**
    * 查询所有数据不分页
    * @param criteria 条件参数
    * @return List<UserAssetDto>
    */
    List<UserAssetDto> queryAll(UserAssetQueryCriteria criteria);

    /**
     * 根据ID查询
     * @param assetId ID
     * @return UserAssetDto
     */
    UserAssetDto findById(Long assetId);

    /**
    * 创建
    * @param resources /
    * @return UserAssetDto
    */
    UserAssetDto create(UserAsset resources);

    /**
    * 编辑
    * @param resources /
    */
    void update(UserAsset resources);

    /**
    * 多选删除
    * @param ids /
    */
    void deleteAll(Long[] ids);

    /**
    * 导出数据
    * @param all 待导出的数据
    * @param response /
    * @throws IOException /
    */
    void download(List<UserAssetDto> all, HttpServletResponse response) throws IOException;

    /**
     * 根据用户id查询
     * @param userId ID
     * @return UserAssetDto
     */
    UserAssetDto findByUserId(Long userId);

    /**
    * @description: 付款
    * @author zmh
    * @date 2022/10/13 18:15
    * @version
    */
    Boolean pay(Long userId, BigDecimal balance);

    /**
    * @description: 余额增加
    * @author zmh
    * @date 2022/10/13 18:15
    * @version
    */
    Boolean addBalance(Long userId, BigDecimal balance);
}