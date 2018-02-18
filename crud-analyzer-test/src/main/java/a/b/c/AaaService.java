package a.b.c;

public class AaaService {

    AaaRepository repository;

    public int create(AaaEntity entity) {

        return repository.create(entity);
    }

    public int create(String entityString) {
        return repository.create(new AaaEntity());
    }

}
