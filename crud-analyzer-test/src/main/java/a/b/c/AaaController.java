package a.b.c;

public class AaaController {

    AaaService service;

    public int create(AaaForm form) {

        return service.create(new AaaEntity());
    }

}
