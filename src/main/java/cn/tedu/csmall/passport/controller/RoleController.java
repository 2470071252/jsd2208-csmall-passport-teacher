package cn.tedu.csmall.passport.controller;

import cn.tedu.csmall.passport.pojo.vo.RoleListItemVO;
import cn.tedu.csmall.passport.service.IRoleService;
import cn.tedu.csmall.passport.web.JsonResult;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 处理角色相关请求的控制器
 *
 * @author java@tedu.cn
 * @version 0.0.1
 */
@Slf4j
@RestController
@RequestMapping("/roles")
@Api(tags = "2. 角色管理模块")
public class RoleController {

    @Autowired
    private IRoleService roleService;

    public RoleController() {
        log.debug("创建控制器对象：RoleController");
    }

    // http://localhost:9081/roles
    @ApiOperation("查询角色列表")
    @ApiOperationSupport(order = 420)
    @PreAuthorize("hasAuthority('/ams/admin/read')")
    @GetMapping("")
    public JsonResult list() {
        log.debug("开始处理【查询角色列表】的请求，无参数");
        List<RoleListItemVO> list = roleService.list();
        return JsonResult.ok(list);
    }

}
