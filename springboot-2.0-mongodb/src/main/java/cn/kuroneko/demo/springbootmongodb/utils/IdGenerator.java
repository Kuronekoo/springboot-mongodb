package cn.kuroneko.demo.springbootmongodb.utils;
import java.util.UUID;

public class IdGenerator {
    public static String createNewId() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }
}