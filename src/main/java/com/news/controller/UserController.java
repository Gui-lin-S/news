package com.news.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.news.domain.Article;
import com.news.domain.Comment;
import com.news.domain.Favorite;
import com.news.domain.User;
import com.news.service.ArticleService;
import com.news.service.CommentService;
import com.news.service.FavoriteService;
import com.news.service.UserService;
import com.news.utils.CheckCodeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author 归林
 * @date 2024/4/2
 */
@Slf4j
@Controller
@RequestMapping("/user")
public class UserController {
    @Autowired
    UserService userService;

    @Autowired
    ArticleService articleService;

    @Autowired
    FavoriteService favoriteService;

    @Autowired
    CommentService commentService;
    
    /**
     * 注册页面
     * @param model
     * @return
     */
    @RequestMapping("/register")
    public String register(Model model) {
        return "register";
    }

    /**
     * 提供注册页面验证码
     * @param response
     * @param request
     */
    @RequestMapping("/register/checkCode")
    @ResponseBody
    public void getCheck(HttpServletResponse response, HttpServletRequest request) {
        //调用验证码工具类提供验证码
        CheckCodeUtil.getCheckCode(request,response);
    }

    /**
     * 注册页面提交表单返回结果
     * @param map1
     * @param session
     * @return
     */
    @PostMapping("/register/result")
    @ResponseBody
    public Map sendRegisterResult(@RequestBody Map<String,String> map1
            , HttpSession session){

        Map map = userService.registerResult(map1, session);
        return map;
    }

    /**
     * 前端登录页面
     * @param model
     * @return
     */
    @RequestMapping("/login")
    public String userLogin(Model model){
        return "login";
    }

    /**
     * 前端登录页面结果返回
     * @param map1
     * @param session
     * @return
     */
    @PostMapping("/login/result")
    @ResponseBody
    public Map sendLoginResult(@RequestBody Map<String,String> map1
            ,HttpSession session){

        Map map = userService.loginResult(map1, session);
        log.info("用户属性{}",map1);
        log.info("map{}",map);
        return map;
    }

    /**
     * 前端用户账号退出
     * @param session
     * @return
     */
    @RequestMapping("/quit")
    public String userQuit(HttpSession session)
    {
        //销毁保存的session
        session.invalidate();
        return "redirect:/index";
    }

    /**
     * 个人主页，暂时无用
     * @param model
     * @return
     */
    @RequestMapping("/home")
    public String home(Model model) {

        return "home";
    }

    /**
     * 我的消息,暂时无用
     * @param model
     * @return
     */
    @RequestMapping("/message")
    public String message(Model model) {

        return "message";
    }

    /**
     * 个人设置页面
     * @param model
     * @param session
     * @return
     */
    @RequestMapping("/set")
    public String set(Model model,HttpSession session) {
        User user =(User) session.getAttribute("user");
        model.addAttribute("user",user);
        return "set";
    }

    /**
     * 用户中心，返回用户发帖和帖子收藏以及评论
     * @param model
     * @param session
     * @return
     */
    @RequestMapping("/user_center")
    public String userCenter(Model model,HttpSession session) {
        User user =(User) session.getAttribute("user");
        QueryWrapper<Article> wrapper = new QueryWrapper<>();
        wrapper.eq("author_id",user.getUid()).orderByDesc("create_time");
        List<Article> list = articleService.list(wrapper);
        model.addAttribute("lists",list);

        //返回用户的收藏
        List<Article> favArts = userService.getFavArt(user.getUid());
        model.addAttribute("favArts",favArts);
        //需要返回添加时间
        QueryWrapper<Favorite> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("uid",user.getUid()).orderByDesc("add_time");
        List<Favorite> favorites = favoriteService.list(queryWrapper);
        model.addAttribute("favorites",favorites);
        //返回用户评论
        QueryWrapper<Comment> comWrapper = new QueryWrapper<>();
        comWrapper.eq("uid",user.getUid()).orderByDesc("com_time");
        List<Comment> commentList = commentService.list(comWrapper);
        model.addAttribute("commentList",commentList);
        return "user_center";
    }

    /**
     * 进行用户个人新闻的删除
     * @param aid
     * @param session
     * @return
     */
    @RequestMapping("/delArt")
    @ResponseBody
    public Map delArt(Integer aid,HttpSession session){
        log.info("{}", aid);
        boolean b = articleService.removeById(aid);
        HashMap<String, Object> map = new HashMap<>();
        if (b){
            map.put("flag",200);
            map.put("msg","删除成功");
        }else {
            map.put("flag",400);
            map.put("msg","删除失败，请稍后尝试");
        }
        return map;
    }

    /**
     * 进行用户收藏夹的删除
     * @param aid
     * @param session
     * @return
     */
    @RequestMapping("/delFav")
    @ResponseBody
    public Map delFavorite(Integer aid,HttpSession session){
        User user =(User) session.getAttribute("user");
        QueryWrapper<Favorite> wrapper = new QueryWrapper<>();
        wrapper.eq("aid",aid).eq("uid",user.getUid());
        boolean b = favoriteService.remove(wrapper);
        HashMap<String, Object> map = new HashMap<>();
        if (b){
            map.put("flag",200);
            map.put("msg","删除成功");
        }else {
            map.put("flag",400);
            map.put("msg","删除失败，请稍后尝试");
        }
        return map;
    }

    /**
     * 进行用户评论的删除
     * @param comid
     * @param session
     * @return
     */
    @RequestMapping("/delCom")
    @ResponseBody
    public Map delComment(Integer comid,HttpSession session){
        User user =(User) session.getAttribute("user");
        QueryWrapper<Comment> wrapper = new QueryWrapper<>();
        wrapper.eq("comid",comid);
        boolean b = commentService.remove(wrapper);
        HashMap<String, Object> map = new HashMap<>();
        if (b){
            map.put("flag",200);
            map.put("msg","删除成功");
        }else {
            map.put("flag",400);
            map.put("msg","删除失败，请稍后尝试");
        }
        return map;
    }

    /**
     * 上传图片等资源文件
     * @param request
     * @param file
     * @param session
     * @return
     * @throws IOException
     */
    @ResponseBody
    @RequestMapping(value = "/uploadHeadImg")
    public Map uploadFile(HttpServletRequest request, @RequestParam("file") MultipartFile file, HttpSession session) throws IOException {
        Map<String, Object> map = new HashMap<>();
        Map<String, Object> map2 = new HashMap<>();
        if (file != null) {
            String webapp = "src/main/resources/";
            try {
                String substring = file.getOriginalFilename();
                String uuid = UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
                String uuidName = uuid + "." + substring.substring(substring.lastIndexOf(".") + 1);
                String fileName = "/static/upload_headImg/" + uuidName;
                File destFile = new File(webapp, fileName);
                // 生成upload目录
                File parentFile = destFile.getParentFile();
                if (!parentFile.exists()) {
                    parentFile.mkdirs();
                }
                // 把上传的临时图片，复制到当前项目的webapp路径
                FileCopyUtils.copy(file.getInputStream(), new FileOutputStream(destFile));
                map = new HashMap<>();
                map2 = new HashMap<>();
                map.put("code", 0);
                map.put("msg", "上传成功");
                map.put("data", map2);
                map2.put("src", "/upload_headImg/"+uuidName);
                map2.put("title", uuidName);
                log.info("图片地址为{}",uuidName);
                //将用户头像保存到数据库
                User user = (User) session.getAttribute("user");
                user.setHeadImage("/upload_headImg/"+uuidName);
                userService.updateById(user);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return map;
    }

    /**
     * 修改用户基本信息
     * @param user
     * @param session
     * @return
     */
    @PostMapping("/alterInfo")
    @ResponseBody
    public Map alterUserInfo(@RequestBody User user,HttpSession session){
        Map<String, Object> map = new HashMap<>();
        //从session取出已有数据然后替换，保存到数据库
        User sessionUser = (User) session.getAttribute("user");
        sessionUser.setTelephone(user.getTelephone());
        sessionUser.setEmail(user.getEmail());
        sessionUser.setSex(user.getSex());
        sessionUser.setNewsName(user.getNewsName());
        //更新用户信息
        boolean flag = userService.updateById(sessionUser);
        if (flag){
            //修改session
            session.setAttribute("user",sessionUser);
            map.put("flag",200);
        }

        return map;
    }

    /**
     * 修改用户密码
     * @param map1
     * @param session
     * @return
     */
    @PostMapping("/alterPassword")
    @ResponseBody
    public Map alterPassword(@RequestBody Map<String,String> map1, HttpSession session){
        Map map = userService.alterPass(map1, session);
        return map;
    }
}
