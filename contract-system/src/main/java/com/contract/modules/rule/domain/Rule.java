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
package com.contract.modules.rule.domain;

import lombok.Data;
import cn.hutool.core.bean.BeanUtil;
import io.swagger.annotations.ApiModelProperty;
import cn.hutool.core.bean.copier.CopyOptions;
import javax.persistence.*;
import javax.validation.constraints.*;
import java.sql.Timestamp;
import java.io.Serializable;

/**
* @website https://eladmin.vip
* @description /
* @author zmh
* @date 2022-10-12
**/
@Entity
@Data
@Table(name="sc_rule")
public class Rule implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "`rule_id`")
    @ApiModelProperty(value = "ruleId")
    private Long ruleId;

    @Column(name = "`describe`")
    @ApiModelProperty(value = "规则描述")
    private String describe;

    @Column(name = "`enabled`")
    @ApiModelProperty(value = "属性：0 正常 1 禁用")
    private Boolean enabled;

    @Column(name = "`create_by`")
    private String createBy;

    @Column(name = "`update_by`")
    private String updateBy;

    @Column(name = "`create_time`")
    private Timestamp createTime;

    @Column(name = "`update_time`")
    private Timestamp updateTime;

    public void copy(Rule source){
        BeanUtil.copyProperties(source,this, CopyOptions.create().setIgnoreNullValue(true));
    }
}
