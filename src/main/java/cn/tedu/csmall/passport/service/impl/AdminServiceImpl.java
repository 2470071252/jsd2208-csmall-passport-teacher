package cn.tedu.csmall.passport.service.impl;

import cn.tedu.csmall.passport.ex.ServiceException;
import cn.tedu.csmall.passport.mapper.AdminMapper;
import cn.tedu.csmall.passport.mapper.AdminRoleMapper;
import cn.tedu.csmall.passport.pojo.dto.AdminAddNewDTO;
import cn.tedu.csmall.passport.pojo.dto.AdminLoginDTO;
import cn.tedu.csmall.passport.pojo.entity.Admin;
import cn.tedu.csmall.passport.pojo.entity.AdminRole;
import cn.tedu.csmall.passport.pojo.vo.AdminListItemVO;
import cn.tedu.csmall.passport.pojo.vo.AdminStandardVO;
import cn.tedu.csmall.passport.security.AdminDetails;
import cn.tedu.csmall.passport.service.IAdminService;
import cn.tedu.csmall.passport.web.ServiceCode;
import com.alibaba.fastjson.JSON;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 处理管理员数据的业务实现类
 *
 * @author java@tedu.cn
 * @version 0.0.1
 */
@Slf4j
@Service
public class AdminServiceImpl implements IAdminService {

    @Value("${csmall.jwt.secret-key}")
    private String secretKey;
    @Value("${csmall.jwt.duration-in-minute}")
    private long durationInMinute;
    @Autowired
    private AdminMapper adminMapper;
    @Autowired
    private AdminRoleMapper adminRoleMapper;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private AuthenticationManager authenticationManager;

    public AdminServiceImpl() {
        log.debug("创建业务对象：AdminServiceImpl");
    }

    @Override
    public String login(AdminLoginDTO adminLoginDTO) {
        log.debug("开始处理【管理员登录】的业务，参数：{}", adminLoginDTO);
        // 执行认证
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                adminLoginDTO.getUsername(), adminLoginDTO.getPassword());
        Authentication authenticateResult
                = authenticationManager.authenticate(authentication);
        log.debug("认证通过！");
        log.debug("认证结果：{}", authenticateResult); // 注意：此认证结果中的Principal就是UserDetailsServiceImpl中返回的UserDetails对象

        // 从认证结果中取出将要存入到JWT中的数据
        Object principal = authenticateResult.getPrincipal();
        AdminDetails adminDetails = (AdminDetails) principal;
        Long id = adminDetails.getId();
        String username = adminDetails.getUsername();
        Collection<GrantedAuthority> authorities = adminDetails.getAuthorities();
        String authoritiesJsonString = JSON.toJSONString(authorities);
        log.debug("认证结果中的当事人ID：{}", id);
        log.debug("认证结果中的当事人username：{}", username);
        log.debug("认证结果中的当事人authorities：{}", authorities);
        log.debug("认证结果中的当事人authoritiesJsonString：{}", authoritiesJsonString);

        // 将认证通过后得到的认证信息存入到SecurityContext中
        // 【注意】注释以下2行代码后，在未完成JWT验证流程之前，用户的登录将不可用
        // SecurityContext securityContext = SecurityContextHolder.getContext();
        // securityContext.setAuthentication(authenticateResult);

        // ===== 生成并返回JWT =====
        // JWT的过期时间
        Date date = new Date(System.currentTimeMillis() + durationInMinute * 60 * 1000);
        // 你要存入到JWT中的数据
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", id);
        claims.put("username", username);
        claims.put("authorities", authoritiesJsonString);
        String jwt = Jwts.builder() // 获取JwtBuilder，准备构建JWT数据
                // 【1】Header：主要配置alg（algorithm：算法）和typ（type：类型）属性
                .setHeaderParam("alg", "HS256")
                .setHeaderParam("typ", "JWT")
                // 【2】Payload：主要配置Claims，把你要存入的数据放进去
                .setClaims(claims)
                // 【3】Signature：主要配置JWT的过期时间、签名的算法和secretKey
                .setExpiration(date)
                .signWith(SignatureAlgorithm.HS256, secretKey)
                // 完成
                .compact(); // 得到JWT数据
        log.debug("即将返回JWT数据：{}", jwt);
        return jwt;
    }

    @Override
    public void addNew(AdminAddNewDTO adminAddNewDTO) {
        log.debug("开始处理【添加管理员】的业务，参数：{}", adminAddNewDTO);
        {
            // 从参数对象中取出用户名
            String username = adminAddNewDTO.getUsername();
            // 调用adminMapper.countByUsername()执行统计
            int count = adminMapper.countByUsername(username);
            // 判断统计结果是否大于0
            if (count > 0) {
                // 是：抛出异常（ERR_CONFLICT）
                String message = "添加管理员失败，尝试使用的用户名已经被占用！";
                log.warn(message);
                throw new ServiceException(ServiceCode.ERR_CONFLICT, message);
            }
        }

        {
            // 从参数对象中取出手机号码
            String phone = adminAddNewDTO.getPhone();
            // 调用adminMapper.countByPhone()执行统计
            int count = adminMapper.countByPhone(phone);
            // 判断统计结果是否大于0
            if (count > 0) {
                // 是：抛出异常（ERR_CONFLICT）
                String message = "添加管理员失败，尝试使用的手机号码已经被占用！";
                log.warn(message);
                throw new ServiceException(ServiceCode.ERR_CONFLICT, message);
            }
        }

        {
            // 从参数对象中取出电子邮箱
            String email = adminAddNewDTO.getEmail();
            // 调用adminMapper.countByEmail()执行统计
            int count = adminMapper.countByEmail(email);
            // 判断统计结果是否大于0
            if (count > 0) {
                // 是：抛出异常（ERR_CONFLICT）
                String message = "添加管理员失败，尝试使用的电子邮箱已经被占用！";
                log.warn(message);
                throw new ServiceException(ServiceCode.ERR_CONFLICT, message);
            }
        }

        // 创建Admin对象
        Admin admin = new Admin();
        // 复制参数DTO对象中的属性到实体对象中
        BeanUtils.copyProperties(adminAddNewDTO, admin);
        // 将原密码加密
        String rawPassword = admin.getPassword();
        String encodedPassword = passwordEncoder.encode(rawPassword);
        admin.setPassword(encodedPassword);
        // 设置初始登录次数
        admin.setLoginCount(0);
        // 调用adminMapper.insert()方法插入管理员数据
        int rows = adminMapper.insert(admin);
        if (rows != 1) {
            String message = "添加管理员失败，服务器忙，请稍后再尝试！";
            log.warn(message);
            throw new ServiceException(ServiceCode.ERR_INSERT, message);
        }

        // 准备批量插入管理员与角色的关联数据
        Long adminId = admin.getId();
        Long[] roleIds = adminAddNewDTO.getRoleIds();
        AdminRole[] adminRoleList = new AdminRole[roleIds.length];
        for (int i = 0; i < roleIds.length; i++) {
            AdminRole adminRole = new AdminRole();
            adminRole.setAdminId(adminId);
            adminRole.setRoleId(roleIds[i]);
            adminRoleList[i] = adminRole;
        }
        rows = adminRoleMapper.insertBatch(adminRoleList);
        if (rows != roleIds.length) {
            String message = "添加管理员失败，服务器忙，请稍后再尝试！";
            log.warn(message);
            throw new ServiceException(ServiceCode.ERR_INSERT, message);
        }
    }

    @Override
    public void delete(Long id) {
        log.debug("开始处理【根据id删除删除管理员】的业务，参数：{}", id);
        // 检查尝试删除的数据是否存在
        Object queryResult = adminMapper.getStandardById(id);
        if (queryResult == null) {
            String message = "删除管理员失败，尝试访问的数据不存在！";
            log.warn(message);
            throw new ServiceException(ServiceCode.ERR_NOT_FOUND, message);
        }

        // 执行删除--管理员表
        log.debug("即将执行删除数据，参数：{}", id);
        int rows = adminMapper.deleteById(id);
        if (rows != 1) {
            String message = "删除管理员失败，服务器忙，请稍后再尝试！";
            log.warn(message);
            throw new ServiceException(ServiceCode.ERR_DELETE, message);
        }

        // 执行删除--管理员与角色的关联表
        rows = adminRoleMapper.deleteByAdminId(id);
        if (rows < 1) {
            String message = "删除管理员失败，服务器忙，请稍后再尝试！";
            log.warn(message);
            throw new ServiceException(ServiceCode.ERR_DELETE, message);
        }
    }

    @Override
    public void setEnable(Long id) {
        updateEnableById(id, 1);
    }

    @Override
    public void setDisable(Long id) {
        updateEnableById(id, 0);
    }

    @Override
    public List<AdminListItemVO> list() {
        log.debug("开始处理【查询管理员列表】的业务，参数：无");
        List<AdminListItemVO> list = adminMapper.list();
        return list;
    }

    private void updateEnableById(Long id, Integer enable) {
        String[] enableText = {"禁用", "启用"};
        log.debug("开始处理【{}管理员】的业务，ID：{}，目标状态：{}", enableText[enable], id, enable);
        // 检查数据是否存在
        AdminStandardVO queryResult = adminMapper.getStandardById(id);
        if (queryResult == null) {
            String message = enableText[enable] + "管理员失败，尝试访问的数据不存在！";
            log.warn(message);
            throw new ServiceException(ServiceCode.ERR_NOT_FOUND, message);
        }

        // 检查当前状态是否与参数表示的状态相同
        if (queryResult.getEnable().equals(enable)) {
            String message = enableText[enable] + "管理员失败，当前管理员已经处于"
                    + enableText[enable] + "状态！";
            log.warn(message);
            throw new ServiceException(ServiceCode.ERR_CONFLICT, message);
        }

        // 准备执行更新
        Admin admin = new Admin();
        admin.setId(id);
        admin.setEnable(enable);
        log.debug("即将修改数据，参数：{}", admin);
        int rows = adminMapper.update(admin);
        if (rows != 1) {
            String message = enableText[enable] + "管理员失败，服务器忙，请稍后再次尝试！";
            log.warn(message);
            throw new ServiceException(ServiceCode.ERR_UPDATE, message);
        }
    }

}
