package cn.tedu.csmall.passport.controller;

import cn.tedu.csmall.passport.pojo.dto.AdminAddNewDTO;
import cn.tedu.csmall.passport.pojo.dto.AdminLoginDTO;
import cn.tedu.csmall.passport.pojo.vo.AdminListItemVO;
import cn.tedu.csmall.passport.security.LoginPrincipal;
import cn.tedu.csmall.passport.service.IAdminService;
import cn.tedu.csmall.passport.web.JsonResult;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

/**
 * 处理管理员相关请求的控制器
 *
 * @author java@tedu.cn
 * @version 0.0.1
 */
@Slf4j
@RestController
@RequestMapping("/admins")
@Api(tags = "1. 管理员管理模块")
public class AdminController {

    @Autowired
    private IAdminService adminService;

    public AdminController() {
        log.debug("创建控制器对象：AdminController");
    }

    // http://localhost:9081/admins/login
    @ApiOperation("管理员登录")
    @ApiOperationSupport(order = 50)
    @PostMapping("/login")
    public JsonResult login(AdminLoginDTO adminLoginDTO) {
        log.debug("开始处理【管理员登录】的请求，参数：{}", adminLoginDTO);
        String jwt = adminService.login(adminLoginDTO);
        return JsonResult.ok(jwt);
    }

    // http://localhost:9081/admins/add-new
    @ApiOperation("添加管理员")
    @ApiOperationSupport(order = 100)
    @PreAuthorize("hasAuthority('/ams/admin/add-new')")
    @PostMapping("/add-new")
    public JsonResult addNew(AdminAddNewDTO adminAddNewDTO) {
        log.debug("开始处理【添加管理员】的请求，参数：{}", adminAddNewDTO);
        adminService.addNew(adminAddNewDTO);
        return JsonResult.ok();
    }

    // http://localhost:9081/admins/9527/delete
    @ApiOperation("根据id删除删除管理员")
    @ApiOperationSupport(order = 200)
    @ApiImplicitParam(name = "id", value = "管理员ID", required = true, dataType = "long")
    @PreAuthorize("hasAuthority('/ams/admin/delete')")
    @PostMapping("/{id:[0-9]+}/delete")
    public JsonResult delete(@PathVariable Long id,
                             @ApiIgnore @AuthenticationPrincipal LoginPrincipal loginPrincipal) {
        log.debug("开始处理【根据id删除删除管理员】的请求，参数：{}", id);
        log.debug("当事人：{}", loginPrincipal);
        log.debug("当事人的ID：{}", loginPrincipal.getId());
        log.debug("当事人的用户名：{}", loginPrincipal.getUsername());
        adminService.delete(id);
        return JsonResult.ok();
    }

    // http://localhost:9081/admins/9527/enable
    @ApiOperation("启用管理员")
    @ApiOperationSupport(order = 310)
    @ApiImplicitParam(name = "id", value = "管理员ID", required = true, dataType = "long")
    @PreAuthorize("hasAuthority('/ams/admin/update')")
    @PostMapping("/{id:[0-9]+}/enable")
    public JsonResult setEnable(@PathVariable Long id) {
        log.debug("开始处理【启用管理员】的请求，参数：{}", id);
        adminService.setEnable(id);
        return JsonResult.ok();
    }

    // http://localhost:9081/admins/9527/disable
    @ApiOperation("禁用管理员")
    @ApiOperationSupport(order = 311)
    @ApiImplicitParam(name = "id", value = "管理员ID", required = true, dataType = "long")
    @PreAuthorize("hasAuthority('/ams/admin/update')")
    @PostMapping("/{id:[0-9]+}/disable")
    public JsonResult setDisable(@PathVariable Long id) {
        log.debug("开始处理【禁用管理员】的请求，参数：{}", id);
        adminService.setDisable(id);
        return JsonResult.ok();
    }

    // http://localhost:9081/admins
    @ApiOperation("查询管理员列表")
    @ApiOperationSupport(order = 420)
    @PreAuthorize("hasAuthority('/ams/admin/read')")
    @GetMapping("")
    public JsonResult list() {
        log.debug("开始处理【查询管理员列表】的请求，参数：无");
        List<AdminListItemVO> list = adminService.list();
        return JsonResult.ok(list);
    }

}
