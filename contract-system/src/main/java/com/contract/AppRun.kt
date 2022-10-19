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
package com.contract

import com.contract.annotation.rest.AnonymousGetMapping
import com.contract.utils.SpringContextHolder
import io.swagger.annotations.Api
import org.apache.catalina.connector.Connector
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.ApplicationPidFileWriter
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory
import org.springframework.boot.web.servlet.server.ServletWebServerFactory
import org.springframework.context.annotation.Bean
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.transaction.annotation.EnableTransactionManagement
import org.springframework.web.bind.annotation.RestController

/**
 * 开启审计功能 -> @EnableJpaAuditing
 *
 * @author Zheng Jie
 * @date 2018/11/15 9:20:19
 */
@EnableAsync
@RestController
@Api(hidden = true)
@SpringBootApplication
@EnableTransactionManagement
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
open class AppRun {
    @Bean
    open fun springContextHolder(): SpringContextHolder {
        return SpringContextHolder()
    }

    @Bean
    open fun webServerFactory(): ServletWebServerFactory {
        val fa = TomcatServletWebServerFactory()
        fa.addConnectorCustomizers(TomcatConnectorCustomizer { connector: Connector ->
            connector.setProperty(
                "relaxedQueryChars",
                "[]{}"
            )
        })
        return fa
    }

    /**
     * 访问首页提示
     *
     * @return /
     */
    @AnonymousGetMapping("/")
    fun index(): String {
        return "Backend service started successfully"
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val springApplication = SpringApplication(AppRun::class.java)
            // 监控应用的PID，启动时可指定PID路径：--spring.pid.file=/home/eladmin/app.pid
            // 或者在 application.yml 添加文件路径，方便 kill，kill `cat /home/eladmin/app.pid`
            springApplication.addListeners(ApplicationPidFileWriter())
            springApplication.run(*args)
        }
    }
}