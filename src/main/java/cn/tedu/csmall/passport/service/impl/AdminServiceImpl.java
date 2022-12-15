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
import cn.tedu.csmall.passport.service.IAdminService;
import cn.tedu.csmall.passport.web.ServiceCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 处理管理员数据的业务实现类
 *
 * @author java@tedu.cn
 * @version 0.0.1
 */
@Slf4j
@Service
public class AdminServiceImpl implements IAdminService {

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
    public void login(AdminLoginDTO adminLoginDTO) {
        log.debug("开始处理【管理员登录】的业务，参数：{}", adminLoginDTO);
        // 执行认证
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                adminLoginDTO.getUsername(), adminLoginDTO.getPassword());
        Authentication authenticateResult
                = authenticationManager.authenticate(authentication);
        log.debug("认证通过！");
        log.debug("认证结果：{}", authenticateResult); // 注意：此认证结果中的Principal就是UserDetailsServiceImpl中返回的UserDetails对象

        // 将认证通过后得到的认证信息存入到SecurityContext中
        SecurityContext securityContext = SecurityContextHolder.getContext();
        securityContext.setAuthentication(authenticateResult);
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
        adminMapper.insert(admin);

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
        adminRoleMapper.insertBatch(adminRoleList);
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

        // 执行删除
        log.debug("即将执行删除数据，参数：{}", id);
        adminMapper.deleteById(id);
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
        adminMapper.update(admin);
    }

}
