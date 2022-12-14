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
package com.contract.modules.security.rest;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.IdUtil;
import com.contract.annotation.Log;
import com.contract.annotation.rest.AnonymousDeleteMapping;
import com.contract.annotation.rest.AnonymousGetMapping;
import com.contract.annotation.rest.AnonymousPostMapping;
import com.contract.config.RsaProperties;
import com.contract.exception.BadRequestException;
import com.contract.exception.BusinessException;
import com.contract.modules.asset.domain.UserAsset;
import com.contract.modules.asset.service.UserAssetService;
import com.contract.modules.asset.service.dto.UserAssetDto;
import com.contract.modules.security.config.bean.LoginCodeEnum;
import com.contract.modules.security.config.bean.LoginProperties;
import com.contract.modules.security.config.bean.SecurityProperties;
import com.contract.modules.security.security.TokenProvider;
import com.contract.modules.security.service.OnlineUserService;
import com.contract.modules.security.service.dto.AuthUserDto;
import com.contract.modules.security.service.dto.JwtUserDto;
import com.contract.modules.security.service.dto.OnlineUserDto;
import com.contract.modules.system.domain.AppUser;
import com.contract.modules.system.domain.Dept;
import com.contract.modules.system.domain.User;
import com.contract.modules.system.domain.vo.UserPassVo;
import com.contract.modules.system.service.UserService;
import com.contract.modules.system.service.dto.UserDto;
import com.contract.utils.*;
import com.wf.captcha.base.Captcha;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author Zheng Jie
 * @date 2018-11-23
 * ???????????????token????????????????????????
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Api(tags = "app????????????????????????")
public class ApiAuthorizationController {
    private final SecurityProperties properties;
    private final RedisUtils redisUtils;
    private final OnlineUserService onlineUserService;
    private final TokenProvider tokenProvider;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;

    private final PasswordEncoder passwordEncoder;

    private final UserService userService;

    private final UserAssetService userAssetService;
    @Resource
    private LoginProperties loginProperties;

    @Log("????????????")
    @ApiOperation("????????????")
    @AnonymousPostMapping(value = "/login")
    public R<Object> login(@Validated @RequestBody AuthUserDto authUser, HttpServletRequest request) throws Exception {
        // ????????????
        String password = authUser.getPassword();
        // ???????????????
        /*String code = (String) redisUtils.get(authUser.getUuid());
        // ???????????????
        redisUtils.del(authUser.getUuid());
        if (StringUtils.isBlank(code)) {
            throw new BadRequestException("??????????????????????????????");
        }
        if (StringUtils.isBlank(authUser.getCode()) || !authUser.getCode().equalsIgnoreCase(code)) {
            throw new BadRequestException("???????????????");
        }*/
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(authUser.getUsername(), password);
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        // ????????????????????????????????????????????????
        // UserDetails userDetails = userDetailsService.loadUserByUsername(userInfo.getUsername());
        // Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        // SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = tokenProvider.createToken(authentication);
        final JwtUserDto jwtUserDto = (JwtUserDto) authentication.getPrincipal();
        // ???????????????????????????token????????????(???????????????????????????????????????????????????)
//        save(jwtUserDto, token, request);
        // ??????????????????
        onlineUserService.save(jwtUserDto, token, request);
        // ?????? token ??? ????????????
        Map<String, Object> authInfo = new HashMap<String, Object>(2) {{
            put("token", properties.getTokenStartWith() + token);
            put("user", jwtUserDto);
        }};
        if (loginProperties.isSingleLogin()) {
            //???????????????????????????token
            onlineUserService.checkLoginOnUser(authUser.getUsername(), token);
        }
        return R.Companion.ok(authInfo);
    }

    /**
     * ????????????????????????
     * @param jwtUserDto /
     * @param token /
     * @param request /
     */
    public void save(JwtUserDto jwtUserDto, String token, HttpServletRequest request){
        String dept = jwtUserDto.getUser().getDept().getName();
        String ip = StringUtils.getIp(request);
        String browser = StringUtils.getBrowser(request);
        String address = StringUtils.getCityInfo(ip);
        OnlineUserDto onlineUserDto = null;
        try {
            onlineUserDto = new OnlineUserDto(jwtUserDto.getUsername(), jwtUserDto.getUser().getNickName(), dept, browser , ip, address, EncryptUtils.desEncrypt(token), new Date());
        } catch (Exception e) {
            log.error(e.getMessage(),e);
        }
        redisUtils.set(properties.getOnlineKey() + token, onlineUserDto, 10000/1000);
    }

    @ApiOperation("??????????????????")
    @GetMapping(value = "/info")
    public R<Object> getUserInfo() {
        return R.Companion.ok(SecurityUtils.getCurrentUser());
    }

    @ApiOperation("???????????????")
    @AnonymousGetMapping(value = "/code")
    public ResponseEntity<Object> getCode() {
        // ?????????????????????
        Captcha captcha = loginProperties.getCaptcha();
        String uuid = properties.getCodeKey() + IdUtil.simpleUUID();
        //????????????????????? arithmetic???????????? >= 2 ??????captcha.text()??????????????????????????????
        String captchaValue = captcha.text();
        if (captcha.getCharType() - 1 == LoginCodeEnum.ARITHMETIC.ordinal() && captchaValue.contains(".")) {
            captchaValue = captchaValue.split("\\.")[0];
        }
        // ??????
        redisUtils.set(uuid, captchaValue, loginProperties.getLoginCode().getExpiration(), TimeUnit.MINUTES);
        // ???????????????
        Map<String, Object> imgResult = new HashMap<String, Object>(2) {{
            put("img", captcha.toBase64());
            put("uuid", uuid);
        }};
        return ResponseEntity.ok(imgResult);
    }

    @ApiOperation("????????????")
    @AnonymousDeleteMapping(value = "/logout")
    public ResponseEntity<Object> logout(HttpServletRequest request) {
        onlineUserService.logout(tokenProvider.getToken(request));
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Log("????????????")
    @ApiOperation("????????????")
    @AnonymousPostMapping(value = "/register")
    public R<Object> register(@Validated @RequestBody AppUser resources) throws Exception {
        if(StringUtils.isEmpty(resources.getUsername())){
            throw new BusinessException("?????????????????????");
        }
        if(StringUtils.isEmpty(resources.getPassword())){
            throw new BusinessException("??????????????????");
        }
//        String pwd = RsaUtils.decryptByPrivateKey(RsaProperties.privateKey,resources.getPassword());
        resources.setPassword(passwordEncoder.encode(resources.getPassword()));
        User user = BeanUtil.toBean(resources, User.class);
        //????????????
        Dept dept = new Dept();
        dept.setId(2l);
        user.setDept(dept);
        userService.register(user);
        UserAsset userAsset = new UserAsset();
        userAsset.setUserId(user.getId());
        userAsset.setBalance(new BigDecimal("0"));
        userAssetService.create(userAsset);
        return R.Companion.ok(null);
    }

    @ApiOperation("????????????")
    @PostMapping(value = "/updatePass")
    public ResponseEntity<Object> updateUserPass(@RequestBody UserPassVo passVo,HttpServletRequest request) throws Exception {
        String oldPass = passVo.getOldPass();
        String newPass = passVo.getNewPass();
        UserDto user = userService.findByName(SecurityUtils.getCurrentUsername());
        if(!passwordEncoder.matches(oldPass, user.getPassword())){
            throw new BusinessException("??????????????????????????????");
        }
        if(passwordEncoder.matches(newPass, user.getPassword())){
            throw new BusinessException("?????????????????????????????????");
        }
        userService.updatePass(user.getUsername(),passwordEncoder.encode(newPass));
        onlineUserService.logout(tokenProvider.getToken(request));
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
