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
package com.contract.modules.asset.rest;

import com.contract.annotation.Log;
import com.contract.modules.asset.domain.UserAsset;
import com.contract.modules.asset.service.UserAssetService;
import com.contract.modules.asset.service.dto.UserAssetQueryCriteria;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
* @website https://eladmin.vip
* @author zmh
* @date 2022-10-13
**/
@RestController
@RequiredArgsConstructor
@Api(tags = "用户资金管理")
@RequestMapping("/api/userAsset")
public class UserAssetController {

    private final UserAssetService userAssetService;

    @Log("导出数据")
    @ApiOperation("导出数据")
    @GetMapping(value = "/download")
    @PreAuthorize("@el.check('userAsset:list')")
    public void exportUserAsset(HttpServletResponse response, UserAssetQueryCriteria criteria) throws IOException {
        userAssetService.download(userAssetService.queryAll(criteria), response);
    }

    @GetMapping
    @Log("查询用户资金")
    @ApiOperation("查询用户资金")
    @PreAuthorize("@el.check('userAsset:list')")
    public ResponseEntity<Object> queryUserAsset(UserAssetQueryCriteria criteria, Pageable pageable){
        return new ResponseEntity<>(userAssetService.queryAll(criteria,pageable),HttpStatus.OK);
    }

    @PostMapping
    @Log("新增用户资金")
    @ApiOperation("新增用户资金")
    @PreAuthorize("@el.check('userAsset:add')")
    public ResponseEntity<Object> createUserAsset(@Validated @RequestBody UserAsset resources){
        return new ResponseEntity<>(userAssetService.create(resources),HttpStatus.CREATED);
    }

    @PutMapping
    @Log("修改用户资金")
    @ApiOperation("修改用户资金")
    @PreAuthorize("@el.check('userAsset:edit')")
    public ResponseEntity<Object> updateUserAsset(@Validated @RequestBody UserAsset resources){
        userAssetService.update(resources);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping
    @Log("删除用户资金")
    @ApiOperation("删除用户资金")
    @PreAuthorize("@el.check('userAsset:del')")
    public ResponseEntity<Object> deleteUserAsset(@RequestBody Long[] ids) {
        userAssetService.deleteAll(ids);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}