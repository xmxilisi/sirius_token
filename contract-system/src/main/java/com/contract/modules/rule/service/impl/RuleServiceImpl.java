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
package com.contract.modules.rule.service.impl;

import com.contract.modules.rule.domain.Rule;
import com.contract.modules.rule.repository.RuleRepository;
import com.contract.modules.rule.service.RuleService;
import com.contract.modules.rule.service.dto.RuleDto;
import com.contract.modules.rule.service.dto.RuleQueryCriteria;
import com.contract.modules.rule.service.mapstruct.RuleMapper;
import com.contract.utils.FileUtil;
import com.contract.utils.PageUtil;
import com.contract.utils.QueryHelp;
import com.contract.utils.ValidationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
* @website https://eladmin.vip
* @description 服务实现
* @author zmh
* @date 2022-10-12
**/
@Service
@RequiredArgsConstructor
public class RuleServiceImpl implements RuleService {

    private final RuleRepository ruleRepository;
    private final RuleMapper ruleMapper;

    @PersistenceContext
    private EntityManager em;

    @Override
    public Map<String,Object> queryAll(RuleQueryCriteria criteria, Pageable pageable){
        Page<Rule> page = ruleRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root,criteria,criteriaBuilder),pageable);
        return PageUtil.toPage(page.map(ruleMapper::toDto));
    }

    @Override
    public List<RuleDto> queryAll(RuleQueryCriteria criteria){
        return ruleMapper.toDto(ruleRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root,criteria,criteriaBuilder)));
    }

    @Override
    @Transactional
    public RuleDto findById(Long ruleId) {
        Rule rule = ruleRepository.findById(ruleId).orElseGet(Rule::new);
        ValidationUtil.isNull(rule.getRuleId(),"Rule","ruleId",ruleId);
        return ruleMapper.toDto(rule);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RuleDto create(Rule resources) {
        //只能有一个启用
        if(resources.getEnabled()==true){
            List<Rule> list = ruleRepository.findAll();
            list.forEach(Rule->{
                Rule.setEnabled(false);
            });
            ruleRepository.saveAll(list);
        }
        return ruleMapper.toDto(ruleRepository.save(resources));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Rule resources) {
        //只能有一个启用
        if(resources.getEnabled()==true){
            List<Rule> list = ruleRepository.findAll();
            list.forEach(Rule->{
                Rule.setEnabled(false);
            });
            ruleRepository.saveAll(list);
        }
        Rule rule = ruleRepository.findById(resources.getRuleId()).orElseGet(Rule::new);
        ValidationUtil.isNull( rule.getRuleId(),"Rule","id",resources.getRuleId());
        rule.copy(resources);
        ruleRepository.save(rule);
    }

    @Override
    public void deleteAll(Long[] ids) {
        for (Long ruleId : ids) {
            ruleRepository.deleteById(ruleId);
        }
    }

    @Override
    public void download(List<RuleDto> all, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (RuleDto rule : all) {
            Map<String,Object> map = new LinkedHashMap<>();
            map.put("规则描述", rule.getDescribe());
            map.put("属性：0 正常 1 禁用", rule.getEnabled());
            map.put("创建者", rule.getCreateBy());
            map.put("更新者", rule.getUpdateBy());
            map.put("创建日期", rule.getCreateTime());
            map.put("更新时间", rule.getUpdateTime());
            list.add(map);
        }
        FileUtil.downloadExcel(list, response);
    }

    @Override
    public RuleDto findUse() {
        return ruleMapper.toDto(ruleRepository.findByEnabled());
    }
}