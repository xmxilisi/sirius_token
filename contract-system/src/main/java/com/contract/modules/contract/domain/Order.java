package com.contract.modules.contract.domain;

import com.contract.base.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 *
 * @author: Ivan
 * @date: 2022/10/11
 * @Version: 1.0
 * @Description:
 */
@Entity
@Getter
@Setter
@Table(name = "contract_order")
public class Order extends BaseEntity implements Serializable {

    @Id
    @Column(name = "order_id")
    @ApiModelProperty(value = "ID", hidden = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
}
