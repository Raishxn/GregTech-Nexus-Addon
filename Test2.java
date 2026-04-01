import java.lang.reflect.Method;
public class Test2 {
    public static void main(String[] args) throws Exception {
        Class<?> clazz = Class.forName("com.gregtechceu.gtceu.api.recipe.builder.StationRecipeBuilder");
        for(Method m : clazz.getMethods()) {
            System.out.println(m.getName());
        }
    }
}
