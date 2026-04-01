import java.lang.reflect.Field;
public class Test3 {
    public static void main(String[] args) throws Exception {
        Class<?> clazz = Class.forName("com.gregtechceu.gtceu.common.data.GTMachines");
        for(Field f : clazz.getFields()) {
            if (f.getName().contains("ENERGY") || f.getName().contains("DYNAMO")) {
                System.out.println(f.getName() + " -> " + f.getType().getName());
            }
        }
    }
}
