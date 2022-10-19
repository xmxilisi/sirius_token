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
package com.contract.config;

import com.contract.constants.Constants;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @author Zheng Jie
 * @description
 * @date 2021-11-22
 **/
@Data
@Component
public class ElAdminProperties {

    public static Boolean ipLocal;
    @Value("${spring.profiles.active}")
    public String environment;

    @Value("${ip.local-parsing}")
    public void setIpLocal(Boolean ipLocal) {
        ElAdminProperties.ipLocal = ipLocal;
    }

    @PostConstruct
    public void init(){
        if (! Constants.System.PROD.equals(environment)) {
            System.out.println("开启本地代理......");
            String proxyHost = "localhost";
            String proxyPort = "7890";
            System.setProperty("http.proxyHost", proxyHost);
            System.setProperty("http.proxyPort", proxyPort);
            // 对https也开启代理
            System.setProperty("https.proxyHost", proxyHost);
            System.setProperty("https.proxyPort", proxyPort);
        }
    }
}
