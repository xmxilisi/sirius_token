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
package com.contract.modules.rule.rest;

import com.contract.annotation.Log;
import com.contract.modules.rule.domain.Rule;
import com.contract.modules.rule.service.RuleService;
import com.contract.modules.rule.service.dto.RuleQueryCriteria;
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
* @date 2022-10-12
**/
@RestController
@RequiredArgsConstructor
@Api(tags = "rule管理")
@RequestMapping("/api/rule")
public class RuleController {

    private final RuleService ruleService;

    @Log("导出数据")
    @ApiOperation("导出数据")
    @GetMapping(value = "/download")
    @PreAuthorize("@el.check('rule:list')")
    public void exportRule(HttpServletResponse response, RuleQueryCriteria criteria) throws IOException {
        ruleService.download(ruleService.queryAll(criteria), response);
    }

    @GetMapping
    @Log("查询rule")
    @ApiOperation("查询rule")
    @PreAuthorize("@el.check('rule:list')")
    public ResponseEntity<Object> queryRule(RuleQueryCriteria criteria, Pageable pageable){
        return new ResponseEntity<>(ruleService.queryAll(criteria,pageable),HttpStatus.OK);
    }

    @PostMapping
    @Log("新增rule")
    @ApiOperation("新增rule")
    @PreAuthorize("@el.check('rule:add')")
    public ResponseEntity<Object> createRule(@Validated @RequestBody Rule resources){
        return new ResponseEntity<>(ruleService.create(resources),HttpStatus.CREATED);
    }

    @PutMapping
    @Log("修改rule")
    @ApiOperation("修改rule")
    @PreAuthorize("@el.check('rule:edit')")
    public ResponseEntity<Object> updateRule(@Validated @RequestBody Rule resources){
        ruleService.update(resources);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping
    @Log("删除rule")
    @ApiOperation("删除rule")
    @PreAuthorize("@el.check('rule:del')")
    public ResponseEntity<Object> deleteRule(@RequestBody Long[] ids) {
        ruleService.deleteAll(ids);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping(value = "/findUse")
    @Log("查询启用的规则描述")
    @ApiOperation("查询启用的规则描述")
    @PreAuthorize("@el.check('rule:list')")
    public ResponseEntity<Object> findUse(){
        return new ResponseEntity<>(ruleService.findUse(),HttpStatus.OK);
    }
}