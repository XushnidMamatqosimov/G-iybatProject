package api.giybat.uz.controller;

import api.giybat.uz.dto.PostDTO;
import api.giybat.uz.util.SpringSecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/post")
@Tag(name = "PostController", description = "Controller for Posting APIs")
public class PostController {

    @PostMapping("/create")
    @Operation(summary = "create Post")
    public String create (PostDTO postDTO){
        System.out.println(SpringSecurityUtil.getCurrentUser());
        System.out.println(SpringSecurityUtil.getCurrentUserId());
        return "done";
    }

}
