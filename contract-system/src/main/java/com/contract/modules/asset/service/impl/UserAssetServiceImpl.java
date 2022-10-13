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
package com.contract.modules.asset.service.impl;

import com.contract.exception.BadRequestException;
import com.contract.modules.asset.domain.UserAsset;
import com.contract.modules.asset.repository.UserAssetRepository;
import com.contract.modules.asset.service.UserAssetService;
import com.contract.modules.asset.service.dto.UserAssetDto;
import com.contract.modules.asset.service.dto.UserAssetQueryCriteria;
import com.contract.modules.asset.service.mapstruct.UserAssetMapper;
import com.contract.utils.FileUtil;
import com.contract.utils.PageUtil;
import com.contract.utils.QueryHelp;
import com.contract.utils.ValidationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
* @website https://eladmin.vip
* @description 服务实现
* @author zmh
* @date 2022-10-13
**/
@Service
@RequiredArgsConstructor
public class UserAssetServiceImpl implements UserAssetService {

    private final UserAssetRepository userAssetRepository;
    private final UserAssetMapper userAssetMapper;

    @Override
    public Map<String,Object> queryAll(UserAssetQueryCriteria criteria, Pageable pageable){
        Page<UserAsset> page = userAssetRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root,criteria,criteriaBuilder),pageable);
        return PageUtil.toPage(page.map(userAssetMapper::toDto));
    }

    @Override
    public List<UserAssetDto> queryAll(UserAssetQueryCriteria criteria){
        return userAssetMapper.toDto(userAssetRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root,criteria,criteriaBuilder)));
    }

    @Override
    @Transactional
    public UserAssetDto findById(Long assetId) {
        UserAsset userAsset = userAssetRepository.findById(assetId).orElseGet(UserAsset::new);
        ValidationUtil.isNull(userAsset.getAssetId(),"UserAsset","assetId",assetId);
        return userAssetMapper.toDto(userAsset);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserAssetDto create(UserAsset resources) {
        return userAssetMapper.toDto(userAssetRepository.save(resources));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(UserAsset resources) {
        UserAsset userAsset = userAssetRepository.findById(resources.getAssetId()).orElseGet(UserAsset::new);
        ValidationUtil.isNull( userAsset.getAssetId(),"UserAsset","id",resources.getAssetId());
        userAsset.copy(resources);
        userAssetRepository.save(userAsset);
    }

    @Override
    public void deleteAll(Long[] ids) {
        for (Long assetId : ids) {
            userAssetRepository.deleteById(assetId);
        }
    }

    @Override
    public void download(List<UserAssetDto> all, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (UserAssetDto userAsset : all) {
            Map<String,Object> map = new LinkedHashMap<>();
            map.put("用户id", userAsset.getUserId());
            map.put("余额", userAsset.getBalance());
            list.add(map);
        }
        FileUtil.downloadExcel(list, response);
    }

    @Override
    public UserAssetDto findByUserId(Long userId) {
        return userAssetMapper.toDto(userAssetRepository.findByUserId(userId));
    }

    @Override
    public Boolean pay(Long userId, BigDecimal balance) {
        synchronized (this){
            UserAsset userAsset = userAssetRepository.findByUserId(userId);
            if(userAsset==null||userAsset.getBalance().compareTo(balance)<1){
                throw new BadRequestException("余额不足");
            }
            userAsset.setBalance(userAsset.getBalance().subtract(balance));
            userAssetRepository.save(userAsset);
            return true;
        }
    }

    @Override
    public Boolean addBalance(Long userId, BigDecimal balance) {
        synchronized (this){
            UserAsset userAsset = userAssetRepository.findByUserId(userId);
            if(userAsset==null){
                throw new BadRequestException("资金账户不存在");
            }
            userAsset.setBalance(userAsset.getBalance().add(balance));
            userAssetRepository.save(userAsset);
            return true;
        }
    }
}