package a.b.c;

import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/a")
public class AaaController {

    AaaService service;

    @RequestMapping(path = "b")
    public int create(AaaForm form) {

        return service.create(new AaaEntity());
    }

}
