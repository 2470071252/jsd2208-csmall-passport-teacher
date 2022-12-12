package cn.tedu.csmall.passport.controller;

import cn.tedu.csmall.passport.pojo.dto.AdminAddNewDTO;
import cn.tedu.csmall.passport.pojo.vo.AdminListItemVO;
import cn.tedu.csmall.passport.service.IAdminService;
import cn.tedu.csmall.passport.web.JsonResult;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
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
@Api(tags = "管理员管理模块")
public class AdminController {

    @Resource
    // @Autowired
    private IAdminService adminService;

    @Autowired
    public void setAdminService(IAdminService adminService) {
        System.out.println("AdminController.setAdminService");
        this.adminService = adminService;
    }

    //    public AdminController() {
//        log.debug("创建控制器对象：AdminController");
//    }
//
    @Autowired
    public AdminController(@Qualifier("adminServiceImpl") IAdminService adminService) {
        log.debug("创建控制器对象：AdminController，构造方法传入参数：{}", adminService);
        this.adminService = adminService;
    }

    // http://localhost:9081/admins/add-new
    @ApiOperation("添加管理员")
    @ApiOperationSupport(order = 100)
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
    @PostMapping("/{id:[0-9]+}/delete")
    public JsonResult delete(@PathVariable Long id) {
        log.debug("开始处理【根据id删除删除管理员】的请求，参数：{}", id);
        adminService.delete(id);
        return JsonResult.ok();
    }

    // http://localhost:9081/admins/9527/enable
    @ApiOperation("启用管理员")
    @ApiOperationSupport(order = 310)
    @ApiImplicitParam(name = "id", value = "管理员ID", required = true, dataType = "long")
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
    @PostMapping("/{id:[0-9]+}/disable")
    public JsonResult setDisable(@PathVariable Long id) {
        log.debug("开始处理【禁用管理员】的请求，参数：{}", id);
        adminService.setDisable(id);
        return JsonResult.ok();
    }

    // http://localhost:9081/admins
    @ApiOperation("查询管理员列表")
    @ApiOperationSupport(order = 420)
    @GetMapping("")
    public JsonResult list() {
        log.debug("开始处理【查询管理员列表】的请求，参数：无");
        List<AdminListItemVO> list = adminService.list();
        return JsonResult.ok(list);
    }

}
